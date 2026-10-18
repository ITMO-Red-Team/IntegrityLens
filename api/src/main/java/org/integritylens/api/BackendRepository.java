package org.integritylens.api;

import static org.integritylens.api.ApiModels.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BackendRepository {
    private final JdbcTemplate jdbc;

    record BatchRow(UUID id, String title, String language, int count, Instant createdAt) {}
    record SubmissionRow(UUID id, UUID batchId, String name, String language, int loc, String storageKey) {}
    record RunRow(UUID id, String method, String version, String status, Instant startedAt,
            Instant finishedAt, String error, String result) {}
    record FeedbackRow(UUID a, UUID b, Feedback value) {}

    public BackendRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    Optional<UUID> batchByHash(String hash) {
        return jdbc.query("select id from batches where archive_sha256 = ?",
                (rs, n) -> rs.getObject("id", UUID.class), hash).stream().findFirst();
    }

    void lockArchive(String hash) {
        jdbc.query("select pg_advisory_xact_lock(hashtextextended(?, 0))", rs -> {}, hash);
    }

    void createBatch(UUID id, String title, String language, String filename, String hash, int count) {
        jdbc.update("""
                insert into batches(id, title, language, archive_name, archive_sha256, submission_count)
                values (?, ?, ?, ?, ?, ?)
                """, id, title, language, filename, hash, count);
    }

    void createSubmission(UUID id, UUID batchId, String language, ArchiveReader.Source source, String key) {
        jdbc.update("""
                insert into submissions(id, batch_id, original_name, language, loc, size_bytes, sha256, storage_key)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """, id, batchId, source.name(), language, source.loc(), source.bytes().length, source.sha256(), key);
    }

    void createRun(UUID batchId, String method) {
        jdbc.update("insert into runs(id, batch_id, method) values (?, ?, ?)", UUID.randomUUID(), batchId, method);
    }

    BatchRow batch(UUID id) {
        return jdbc.query("select * from batches where id = ?", (rs, n) -> batchRow(rs), id)
                .stream().findFirst().orElseThrow(ApiException::notFound);
    }

    List<BatchRow> batches() {
        return jdbc.query("select * from batches order by created_at desc, id", (rs, n) -> batchRow(rs));
    }

    List<SubmissionRow> submissions(UUID batchId) {
        return jdbc.query("select * from submissions where batch_id = ? order by original_name, id",
                (rs, n) -> submissionRow(rs), batchId);
    }

    SubmissionRow submission(UUID id) {
        return jdbc.query("select * from submissions where id = ?", (rs, n) -> submissionRow(rs), id)
                .stream().findFirst().orElseThrow(ApiException::notFound);
    }

    List<RunRow> runs(UUID batchId) {
        return jdbc.query("select * from runs where batch_id = ? order by method", (rs, n) -> new RunRow(
                rs.getObject("id", UUID.class), rs.getString("method"), rs.getString("method_version"),
                rs.getString("status"), instant(rs, "started_at"), instant(rs, "finished_at"),
                rs.getString("error"), rs.getString("result")), batchId);
    }

    List<FeedbackRow> feedback(UUID batchId) {
        return jdbc.query("select * from feedback where batch_id = ?", (rs, n) -> new FeedbackRow(
                rs.getObject("a", UUID.class), rs.getObject("b", UUID.class), new Feedback(
                Decision.valueOf(rs.getString("decision")), rs.getString("comment"), instant(rs, "updated_at"))),
                batchId);
    }

    Feedback saveFeedback(UUID batchId, UUID a, UUID b, FeedbackRequest request) {
        return jdbc.queryForObject("""
                insert into feedback(batch_id, a, b, decision, comment) values (?, ?, ?, ?, ?)
                on conflict (batch_id, a, b) do update
                set decision = excluded.decision, comment = excluded.comment, updated_at = now()
                returning decision, comment, updated_at
                """, (rs, n) -> new Feedback(Decision.valueOf(rs.getString("decision")),
                rs.getString("comment"), instant(rs, "updated_at")), batchId, a, b,
                request.decision().name(), request.comment() == null ? "" : request.comment());
    }

    private BatchRow batchRow(ResultSet rs) throws SQLException {
        return new BatchRow(rs.getObject("id", UUID.class), rs.getString("title"),
                rs.getString("language"), rs.getInt("submission_count"), instant(rs, "created_at"));
    }

    private SubmissionRow submissionRow(ResultSet rs) throws SQLException {
        return new SubmissionRow(rs.getObject("id", UUID.class), rs.getObject("batch_id", UUID.class),
                rs.getString("original_name"), rs.getString("language"), rs.getInt("loc"), rs.getString("storage_key"));
    }

    private Instant instant(ResultSet rs, String column) throws SQLException {
        var value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }
}
