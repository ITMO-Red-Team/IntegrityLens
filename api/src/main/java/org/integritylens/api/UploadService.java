package org.integritylens.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadService {
    private static final Logger LOG = LoggerFactory.getLogger(UploadService.class);
    static final List<String> METHODS = List.of("jplag", "structsim");
    private final BackendRepository repository;
    private final ObjectStorage storage;
    private final ArchiveReader reader;
    private final TransactionTemplate transaction;

    record Upload(UUID id, boolean created) {}

    public UploadService(BackendRepository repository, ObjectStorage storage, ArchiveReader reader,
            TransactionTemplate transaction) {
        this.repository = repository;
        this.storage = storage;
        this.reader = reader;
        this.transaction = transaction;
    }

    Upload upload(MultipartFile archive, String language, String title) throws IOException {
        if (!"java".equals(language)) {
            throw new ApiException(400, "UNSUPPORTED_LANGUAGE", "В MVP поддерживается только Java");
        }
        if (title != null && title.length() > 200) {
            throw new ApiException(400, "VALIDATION_ERROR", "Название длиннее 200 символов");
        }
        byte[] bytes;
        try (var input = archive.getInputStream()) {
            bytes = input.readNBytes(ArchiveReader.MAX_ARCHIVE + 1);
        }
        if (bytes.length > ArchiveReader.MAX_ARCHIVE) {
            throw new ApiException(413, "ARCHIVE_TOO_LARGE", "Размер архива превышает 10 МиБ");
        }
        String hash = ArchiveReader.sha256(bytes);
        var existing = repository.batchByHash(hash);
        if (existing.isPresent()) return new Upload(existing.get(), false);
        var sources = reader.read(bytes);
        List<String> uploaded = new ArrayList<>();
        try {
            return transaction.execute(tx -> {
                // The lock also serializes uploads before their unique batch row exists.
                repository.jdbc.execute("select pg_advisory_xact_lock(hashtextextended('" + hash + "', 0))");
                var duplicate = repository.batchByHash(hash);
                if (duplicate.isPresent()) return new Upload(duplicate.get(), false);
                UUID batchId = UUID.randomUUID();
                String filename = archive.getOriginalFilename() == null ? "archive.zip" : archive.getOriginalFilename();
                String batchTitle = title == null || title.isBlank() ? filename : title.strip();
                String archiveKey = "batches/" + batchId + "/archive.zip";
                uploaded.add(archiveKey);
                storage.put(archiveKey, bytes, "application/zip");
                repository.jdbc.update("""
                        insert into batches(id, title, language, archive_name, archive_sha256, submission_count)
                        values (?, ?, ?, ?, ?, ?)
                        """, batchId, batchTitle, language, filename, hash, sources.size());
                for (var source : sources) {
                    UUID id = UUID.randomUUID();
                    String key = "batches/" + batchId + "/submissions/" + id + "/source.java";
                    uploaded.add(key);
                    storage.put(key, source.bytes(), "text/plain; charset=utf-8");
                    repository.jdbc.update("""
                            insert into submissions(id, batch_id, original_name, language, loc, size_bytes, sha256, storage_key)
                            values (?, ?, ?, ?, ?, ?, ?, ?)
                            """, id, batchId, source.name(), language, source.loc(), source.bytes().length, source.sha256(), key);
                }
                for (String method : METHODS) {
                    repository.jdbc.update("insert into runs(id, batch_id, method) values (?, ?, ?)",
                            UUID.randomUUID(), batchId, method);
                }
                LOG.atInfo().addKeyValue("batchId", batchId).log("Batch uploaded");
                return new Upload(batchId, true);
            });
        } catch (RuntimeException ex) {
            for (String key : uploaded) {
                try {
                    storage.delete(key);
                } catch (RuntimeException cleanup) {
                    LOG.warn("Could not clean up object {}", key);
                }
            }
            throw ex;
        }
    }
}
