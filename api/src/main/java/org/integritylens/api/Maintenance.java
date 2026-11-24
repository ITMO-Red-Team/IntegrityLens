package org.integritylens.api;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
class Maintenance {
    private static final Logger LOG = LoggerFactory.getLogger(Maintenance.class);
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final Assignments assignments;
    private final AnalysisQueue queue;
    private final ObjectStorage storage;

    Maintenance(JdbcTemplate jdbc, TransactionTemplate transactions, Assignments assignments, AnalysisQueue queue,
                ObjectStorage storage) {
        this.jdbc = jdbc;
        this.transactions = transactions;
        this.assignments = assignments;
        this.queue = queue;
        this.storage = storage;
    }

    @Scheduled(fixedDelayString = "${integritylens.maintenance-interval}")
    void sweep() {
        jdbc.update("delete from sessions where expires_at <= now()");
        var expired = jdbc.query("""
                select s.id, a.teacher_id, a.id as assignment_id
                from submissions s join assignments a on a.id = s.assignment_id
                where s.expires_at <= now() order by s.expires_at, s.id limit 100
                """, (rs, index) -> new Expired(rs.getObject("id", UUID.class), rs.getObject("teacher_id", UUID.class),
                rs.getObject("assignment_id", UUID.class)));
        for (Expired row : expired) {
            try {
                transactions.executeWithoutResult(status -> expire(row));
            } catch (RuntimeException ex) {
                LOG.warn("Could not expire a submission: {}", ex.getClass().getSimpleName());
            }
        }

        var keys = jdbc.queryForList("""
                select storage_key from object_deletions where queued_at <= now() - interval '1 minute'
                order by queued_at, storage_key limit 100
                """, String.class);
        for (String key : keys) {
            try {
                transactions.executeWithoutResult(status -> deleteObject(key));
            } catch (RuntimeException ex) {
                LOG.warn("Could not delete a stored object: {}", ex.getClass().getSimpleName());
            }
        }
        queue.enqueueMissingAi();
        for (UUID teacher : queue.outdatedSimilarityTeachers()) {
            try {
                transactions.executeWithoutResult(status -> {
                    assignments.lockTeacher(teacher);
                    queue.refreshSimilarityVersion(teacher);
                });
            } catch (RuntimeException ex) {
                LOG.warn("Could not refresh similarity version: {}", ex.getClass().getSimpleName());
            }
        }
    }

    private void expire(Expired row) {
        assignments.lockTeacher(row.teacher());
        var keys = jdbc.queryForList("""
                select storage_key from submissions where id = ? and expires_at <= now()
                """, String.class, row.submission());
        if (keys.isEmpty()) {
            return;
        }

        int changed = jdbc.update("""
                update submission_slots set can_submit = true, current_submission_id = null
                where assignment_id = ? and current_submission_id = ?
                """, row.assignment(), row.submission());
        if (changed != 0) {
            queue.submissionChanged(row.assignment());
        }
        jdbc.update("delete from submissions where id = ?", row.submission());
        jdbc.update("insert into object_deletions(storage_key) values (?) on conflict do nothing", keys.getFirst());
    }

    private void deleteObject(String key) {
        var locked = jdbc.queryForList("""
                select storage_key from object_deletions where storage_key = ? for update skip locked
                """, String.class, key);
        if (locked.isEmpty()) {
            return;
        }
        if (!Boolean.TRUE.equals(jdbc.queryForObject("""
                select exists(select 1 from submissions where storage_key = ?)
                """, Boolean.class, key))) {
            storage.delete(key);
        }
        jdbc.update("delete from object_deletions where storage_key = ?", key);
    }

    record Expired(UUID submission, UUID teacher, UUID assignment) {}
}
