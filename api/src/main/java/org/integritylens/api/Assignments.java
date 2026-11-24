package org.integritylens.api;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import static org.integritylens.api.Models.*;

@Service
class Assignments {
    private final JdbcTemplate jdbc;
    private final NamedParameterJdbcTemplate named;
    private final AnalysisQueue queue;
    private final AnalysisRead read;
    private final String publicBase;

    Assignments(JdbcTemplate jdbc, NamedParameterJdbcTemplate named, AnalysisQueue queue, AnalysisRead read,
                @Value("${integritylens.public-base-url}") String publicBase) {
        this.jdbc = jdbc;
        this.named = named;
        this.queue = queue;
        this.read = read;
        URI uri = URI.create(publicBase);
        if (!List.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null
                || uri.getRawUserInfo() != null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("Set INTEGRITYLENS_PUBLIC_BASE_URL to an absolute frontend HTTP(S) URL");
        }
        this.publicBase = publicBase.replaceFirst("/+$", "");
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public List<AssignmentSummary> list(UUID teacher) {
        var counts = read.countsForTeacher(teacher);
        return jdbc.query("""
                select * from assignments where teacher_id = ? and deleted_at is null order by created_at desc, id
                """, (rs, index) -> {
            var row = assignment(rs);
            var count = counts.getOrDefault(row.id(), AnalysisRead.Counts.EMPTY);
            return new AssignmentSummary(row.id(), row.title(), row.createdAt(), count.total(), count.progress());
        }, teacher);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Assignment get(UUID teacher, UUID id) {
        return response(owned(teacher, id));
    }

    @Transactional
    public Assignment create(UUID teacher, String title, List<UUID> sources) {
        lockTeacher(teacher);
        validateSources(teacher, null, sources);
        UUID id = UUID.randomUUID();
        String token = Secrets.token();
        var row = jdbc.queryForObject("""
                insert into assignments(id, teacher_id, title, submission_token)
                values (?, ?, ?, ?) returning *
                """, (rs, index) -> assignment(rs), id, teacher, title, token);
        saveSources(id, sources);
        return response(row);
    }

    @Transactional
    public Assignment updateSources(UUID teacher, UUID id, List<UUID> sources) {
        lockTeacher(teacher);
        var row = owned(teacher, id);
        validateSources(teacher, id, sources);
        if (!new HashSet<>(sourceIds(id)).equals(new HashSet<>(sources))) {
            jdbc.update("delete from assignment_sources where assignment_id = ?", id);
            saveSources(id, sources);
            queue.advance(id);
        }
        return response(row);
    }

    @Transactional
    public void reset(UUID teacher, UUID assignment, UUID student, UUID expected) {
        lockTeacher(teacher);
        owned(teacher, assignment);
        UUID current = jdbc.query("""
                select current_submission_id from submission_slots
                where assignment_id = ? and student_id = ? and current_submission_id is not null
                """, (rs, index) -> rs.getObject(1, UUID.class), assignment, student)
                .stream().findFirst().orElseThrow(ApiException::notFound);
        if (!current.equals(expected)) {
            throw new ApiException(409, "SUBMISSION_CHANGED", "Текущая работа изменилась. Обновите список.");
        }
        jdbc.update("update submission_slots set can_submit = true where assignment_id = ? and student_id = ?",
                assignment, student);
    }

    @Transactional
    public void delete(UUID teacher, UUID id) {
        lockTeacher(teacher);
        int changed = jdbc.update("""
                update assignments set deleted_at = now() where id = ? and teacher_id = ? and deleted_at is null
                """, id, teacher);
        if (changed == 0) {
            if (!Boolean.TRUE.equals(jdbc.queryForObject("""
                    select exists(select 1 from assignments where id = ? and teacher_id = ?)
                    """, Boolean.class, id, teacher))) {
                throw ApiException.notFound();
            }
            return;
        }
        List<UUID> dependents = queue.dependents(id);
        jdbc.update("delete from assignment_sources where assignment_id = ? or source_assignment_id = ?", id, id);
        queue.cancelSimilarity(id);
        dependents.forEach(queue::advance);
    }

    AssignmentRow owned(UUID teacher, UUID id) {
        return jdbc.query("""
                select * from assignments where id = ? and teacher_id = ? and deleted_at is null
                """, (rs, index) -> assignment(rs), id, teacher).stream().findFirst().orElseThrow(ApiException::notFound);
    }

    AssignmentRow byToken(String token) {
        return jdbc.query("""
                select * from assignments where submission_token = ? and deleted_at is null
                """, (rs, index) -> assignment(rs), token).stream().findFirst().orElseThrow(ApiException::notFound);
    }

    void lockTeacher(UUID id) {
        // Источники принадлежат этому же преподавателю, поэтому их изменения делят одну блокировку
        jdbc.queryForObject("select id from users where id = ? for no key update", UUID.class, id);
    }

    private void validateSources(UUID teacher, UUID assignment, List<UUID> sources) {
        if (assignment != null && sources.contains(assignment)) {
            throw ApiException.invalid("Задание не может быть своим источником.");
        }
        if (!sources.isEmpty() && sources.size() != named.queryForObject("""
                select count(*) from assignments
                where id in (:sources) and teacher_id = :teacher and deleted_at is null
                """, java.util.Map.of("sources", sources, "teacher", teacher), Integer.class)) {
            throw ApiException.notFound();
        }
    }

    private void saveSources(UUID assignment, List<UUID> sources) {
        jdbc.batchUpdate("insert into assignment_sources(assignment_id, source_assignment_id) values (?, ?)",
                sources.stream().map(source -> new Object[]{assignment, source}).toList());
    }

    private List<UUID> sourceIds(UUID assignment) {
        return jdbc.queryForList("""
                select source_assignment_id from assignment_sources where assignment_id = ? order by source_assignment_id
                """, UUID.class, assignment);
    }

    private Assignment response(AssignmentRow row) {
        var count = read.countsForAssignment(row.id());
        return new Assignment(row.id(), row.title(), row.createdAt(), count.total(), count.progress(),
                publicBase + "/submit/" + row.token(), sourceIds(row.id()));
    }

    private static AssignmentRow assignment(ResultSet row) throws SQLException {
        return new AssignmentRow(row.getObject("id", UUID.class), row.getObject("teacher_id", UUID.class),
                row.getString("title"), row.getString("submission_token"), row.getLong("similarity_revision"),
                Inputs.instant(row, "created_at"));
    }
}
