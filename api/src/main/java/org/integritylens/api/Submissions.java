package org.integritylens.api;

import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import static org.integritylens.api.Models.Receipt;
import static org.integritylens.api.Models.SubmissionForm;

@Service
class Submissions {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final Assignments assignments;
    private final AnalysisQueue queue;
    private final ObjectStorage storage;

    Submissions(JdbcTemplate jdbc, TransactionTemplate transactions, Assignments assignments, AnalysisQueue queue,
                ObjectStorage storage) {
        this.jdbc = jdbc;
        this.transactions = transactions;
        this.assignments = assignments;
        this.queue = queue;
        this.storage = storage;
    }

    SubmissionForm form(String token, UUID student) {
        return jdbc.query("""
                select a.title, teacher.full_name, coalesce(slot.can_submit, true) as can_submit,
                    s.id as submission_id, s.submitted_at
                from assignments a join users teacher on teacher.id = a.teacher_id
                left join submission_slots slot on slot.assignment_id = a.id and slot.student_id = ?
                left join submissions s on s.id = slot.current_submission_id
                where a.submission_token = ? and a.deleted_at is null
                """, (rs, index) -> {
            UUID current = rs.getObject("submission_id", UUID.class);
            var receipt = current == null ? null : new Receipt(current, Inputs.instant(rs, "submitted_at"));
            return new SubmissionForm(rs.getString("title"), rs.getString("full_name"), rs.getBoolean("can_submit"),
                    receipt);
        }, student, token).stream().findFirst().orElseThrow(ApiException::notFound);
    }

    Receipt accept(String token, UUID student, SubmissionFile file) {
        var assignment = assignments.byToken(token);
        UUID id = UUID.randomUUID();
        String key = "submissions/" + id + "/source.java";
        // Ключ сохраняется до записи файла и снимается только вместе с подтверждённой сдачей
        jdbc.update("insert into object_deletions(storage_key) values (?)", key);
        return transactions.execute(status -> {
            jdbc.queryForObject("select storage_key from object_deletions where storage_key = ? for update",
                    String.class, key);
            assignments.lockTeacher(assignment.teacherId());
            assignments.owned(assignment.teacherId(), assignment.id());
            jdbc.update("""
                    insert into submission_slots(assignment_id, student_id) values (?, ?) on conflict do nothing
                    """, assignment.id(), student);
            if (!Boolean.TRUE.equals(jdbc.queryForObject("""
                    select can_submit from submission_slots where assignment_id = ? and student_id = ?
                    """, Boolean.class, assignment.id(), student))) {
                throw new ApiException(409, "SUBMISSION_ALREADY_ACCEPTED", "Работа уже принята.");
            }

            storage.put(key, file.bytes(), "text/plain; charset=utf-8");
            Receipt receipt = jdbc.queryForObject("""
                    with time as (select clock_timestamp() as accepted_at)
                    insert into submissions(id, assignment_id, student_id, storage_key, size_bytes, sha256, loc,
                                            submitted_at, expires_at)
                    select ?, ?, ?, ?, ?, ?, ?, accepted_at, accepted_at + interval '3 years' from time
                    returning submitted_at
                    """, (rs, index) -> new Receipt(id, Inputs.instant(rs, "submitted_at")),
                    id, assignment.id(), student, key, file.bytes().length, file.sha256(), file.lines());
            jdbc.update("""
                    update submission_slots set current_submission_id = ?, can_submit = false
                    where assignment_id = ? and student_id = ?
                    """, id, assignment.id(), student);
            queue.enqueueAi(id);
            queue.submissionChanged(assignment.id());
            jdbc.update("delete from object_deletions where storage_key = ?", key);
            return receipt;
        });
    }

    byte[] source(UUID teacher, UUID id) {
        String key = jdbc.query("""
                select s.storage_key from submissions s join assignments a on a.id = s.assignment_id
                join submission_slots slot on slot.current_submission_id = s.id
                where s.id = ? and a.teacher_id = ? and a.deleted_at is null
                """, (rs, index) -> rs.getString(1), id, teacher).stream().findFirst().orElseThrow(ApiException::notFound);
        return storage.get(key);
    }
}
