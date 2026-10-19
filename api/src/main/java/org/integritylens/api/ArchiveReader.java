package org.integritylens.api;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.springframework.stereotype.Component;

@Component
public class ArchiveReader {
    static final int MAX_ARCHIVE = 10 * 1024 * 1024;
    static final int MAX_FILE = 1024 * 1024;
    static final int MAX_TOTAL = 50 * 1024 * 1024;

    record Source(String name, byte[] bytes, int loc, String sha256) {}

    List<Source> read(byte[] archive) {
        if (archive.length > MAX_ARCHIVE) {
            throw new ApiException(413, "ARCHIVE_TOO_LARGE", "Размер архива превышает 10 МиБ");
        }
        List<Source> sources = new ArrayList<>();
        List<String> unsupported = new ArrayList<>();
        Set<String> names = new HashSet<>();
        int entries = 0;
        int total = 0;
        try (var zip = ZipFile.builder().setSeekableByteChannel(new SeekableInMemoryByteChannel(archive))
                .setCharset(Charset.forName("CP866")).get()) {
            var iterator = zip.getEntries();
            while (iterator.hasMoreElements()) {
                var entry = iterator.nextElement();
                if (++entries > 200) {
                    throw unsafe("В архиве больше 200 записей");
                }
                String name = entry.getName().replace('\\', '/');
                if (name.startsWith("/") || name.matches("^[A-Za-z]:.*") || name.indexOf('\0') >= 0
                        || List.of(name.split("/")).contains("..") || entry.isUnixSymlink()
                        || (entry.getUnixMode() & 0170000) != 0
                        && (entry.getUnixMode() & 0170000) != 0100000
                        && (entry.getUnixMode() & 0170000) != 0040000) {
                    throw unsafe("В архиве обнаружен небезопасный путь или ссылка");
                }
                if (!zip.canReadEntryData(entry)) {
                    throw unsafe("Неподдерживаемая или зашифрованная запись ZIP");
                }
                byte[] bytes;
                try (var input = zip.getInputStream(entry)) {
                    bytes = input.readNBytes(MAX_FILE + 1);
                }
                total += bytes.length;
                if (bytes.length > MAX_FILE || total > MAX_TOTAL) {
                    throw unsafe("Превышен размер распакованных данных");
                }
                var crc = new CRC32();
                crc.update(bytes);
                if (entry.getCrc() != crc.getValue() || entry.getSize() != bytes.length) {
                    throw new IOException("Invalid ZIP entry");
                }
                if (entry.isDirectory() || name.startsWith("__MACOSX/")
                        || name.endsWith("/.DS_Store") || name.equals(".DS_Store")
                        || name.endsWith("/Thumbs.db") || name.equals("Thumbs.db")) {
                    continue;
                }
                if (!names.add(name)) {
                    throw unsafe("В архиве повторяются пути файлов");
                }
                if (name.toLowerCase(java.util.Locale.ROOT).matches(".*\\.(zip|jar|tar|gz|7z|rar)$")) {
                    throw unsafe("Вложенные архивы не поддерживаются");
                }
                if (!name.endsWith(".java")) {
                    if (unsupported.size() < 10) unsupported.add(name);
                    continue;
                }
                String text = decode(bytes);
                byte[] utf8 = text.getBytes(StandardCharsets.UTF_8);
                sources.add(new Source(name, utf8, Math.toIntExact(text.lines().count()), sha256(utf8)));
            }
        } catch (IOException | IllegalArgumentException ex) {
            throw new ApiException(400, "NOT_A_ZIP", "Файл не является корректным ZIP-архивом");
        }
        if (!unsupported.isEmpty()) {
            throw new ApiException(400, "UNSUPPORTED_FILES", "Допустимы только файлы .java",
                    Map.of("files", unsupported));
        }
        if (sources.size() < 5 || sources.size() > 50) {
            throw new ApiException(400, "FILE_COUNT_OUT_OF_RANGE", "Требуется от 5 до 50 работ",
                    Map.of("found", sources.size(), "min", 5, "max", 50));
        }
        sources.sort(Comparator.comparing(Source::name));
        return sources;
    }

    static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String decode(byte[] bytes) throws CharacterCodingException {
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException ex) {
            return Charset.forName("windows-1251").newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        }
    }

    private ApiException unsafe(String message) {
        return new ApiException(400, "UNSAFE_ARCHIVE", message);
    }
}
