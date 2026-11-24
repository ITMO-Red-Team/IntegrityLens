package org.integritylens.api;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.integritylens.api.Models.*;

@Component
class AnalysisRead {
    private static final String SCORES = """
            select a.id as assignment_id, s.id, s.student_id, u.full_name, s.submitted_at,
                case when sim.id is null then 'not_applicable'
                     when sim.status in ('queued', 'running') then 'pending'
                     when sim.status = 'done' and pairs.reason is null and pairs.value is not null then 'ok'
                     else 'failed' end as sim_status,
                coalesce(pairs.reason, sim.error_code, 'RESULT_MISSING') as sim_reason,
                pairs.value as sim_value,
                case when ai.id is null or ai.status in ('queued', 'running') then 'pending'
                     when ai.status = 'done' and result.score is not null then 'ok'
                     else 'failed' end as ai_status,
                coalesce(result.reason_code, ai.error_code, 'RESULT_MISSING') as ai_reason,
                result.score as ai_value, result.suspicious::text as suspicious
            from assignments a
            join submission_slots slot on slot.assignment_id = a.id
            join submissions s on s.id = slot.current_submission_id
            join users u on u.id = s.student_id
            left join analysis_runs sim on sim.assignment_id = a.id and sim.revision = a.similarity_revision
                and sim.method = 'structsim'
            left join lateral (
                select max(value) as value, min(reason_code) as reason from similarity_results p
                where p.run_id = sim.id and (p.a = s.id or p.b = s.id)
            ) pairs on true
            left join analysis_runs ai on ai.submission_id = s.id and ai.method = 'ai' and ai.method_version = ?
            left join ai_results result on result.run_id = ai.id
            """;

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final String aiVersion;

    AnalysisRead(JdbcTemplate jdbc, ObjectMapper json, @Value("${integritylens.analysis.ai-version}") String aiVersion) {
        this.jdbc = jdbc;
        this.json = json;
        this.aiVersion = aiVersion;
    }

    Map<UUID, Counts> countsForTeacher(UUID teacher) {
        return counts("a.teacher_id = ?", teacher);
    }

    Counts countsForAssignment(UUID assignment) {
        return counts("a.id = ?", assignment).getOrDefault(assignment, Counts.EMPTY);
    }

    private Map<UUID, Counts> counts(String filter, UUID id) {
        String sql = """
                select assignment_id, count(*) as total,
                    count(*) filter (where sim_status <> 'pending' and ai_status <> 'pending') as completed,
                    count(*) filter (where sim_status <> 'pending' and ai_status <> 'pending'
                                     and (sim_status = 'failed' or ai_status = 'failed')) as failed
                from (%s where a.deleted_at is null and %s) scores
                group by assignment_id
                """.formatted(SCORES, filter);
        return jdbc.query(sql, rs -> {
            Map<UUID, Counts> counts = new HashMap<>();
            while (rs.next()) {
                counts.put(rs.getObject("assignment_id", UUID.class),
                        new Counts(rs.getInt("total"), new Progress(rs.getInt("completed"), rs.getInt("failed"))));
            }
            return counts;
        }, aiVersion, id);
    }

    SubmissionList list(UUID assignment, String sort, int page, int pageSize) {
        String sortColumn = switch (sort) {
            case "ai" -> "ai";
            case "similarity" -> "sim";
            default -> throw ApiException.invalid("Допускается сортировка по similarity или ai.");
        };
        int total = jdbc.queryForObject("""
                select count(*) from submission_slots where assignment_id = ? and current_submission_id is not null
                """, Integer.class, assignment);
        long offset = (long) (page - 1) * pageSize;
        if (offset >= total) {
            return new SubmissionList(List.of(), page, pageSize, total);
        }
        String sql = """
                select * from (%s where a.id = ?) scores
                order by case when %s_status = 'ok' then %s_value end desc nulls last, id
                limit ? offset ?
                """.formatted(SCORES, sortColumn, sortColumn);
        List<SubmissionRow> items = jdbc.query(sql, (rs, index) -> row(rs), aiVersion, assignment, pageSize, offset);
        return new SubmissionList(items, page, pageSize, total);
    }

    SubmissionDetail detail(AssignmentRow assignment, UUID submission) {
        return jdbc.query(SCORES + " where a.id = ? and s.id = ?", (rs, index) -> {
            var row = row(rs);
            return new SubmissionDetail(row.submissionId(), row.studentId(), row.fullName(), row.submittedAt(),
                    row.similarity(), row.ai(),
                    row.ai().status().equals("ok") ? json.readTree(rs.getString("suspicious")) : json.createArrayNode(),
                    similar(assignment, submission));
        }, aiVersion, assignment.id(), submission).stream().findFirst().orElseThrow(ApiException::notFound);
    }

    private List<SimilarWork> similar(AssignmentRow assignment, UUID submission) {
        return jdbc.query("""
                select s.id, s.assignment_id, a.title, p.value, p.reason_code
                from analysis_runs r
                join similarity_results p on p.run_id = r.id
                join submissions s on s.id = case when p.a = ? then p.b else p.a end
                join assignments a on a.id = s.assignment_id
                where r.assignment_id = ? and r.revision = ? and r.method = 'structsim' and r.status = 'done'
                    and (p.a = ? or p.b = ?)
                order by p.value desc nulls last, s.id
                """, (rs, index) -> new SimilarWork(rs.getObject("id", UUID.class),
                rs.getObject("assignment_id", UUID.class), rs.getString("title"), storedScore(rs)),
                submission, assignment.id(), assignment.revision(), submission, submission);
    }

    PairResult pair(AssignmentRow assignment, UUID a, UUID b) {
        if (a.equals(b)) {
            throw ApiException.notFound();
        }
        boolean reverse = a.toString().compareTo(b.toString()) > 0;
        return jdbc.query("""
                select r.status, r.error_code, p.value, p.reason_code, p.details::text, p.fragments::text
                from analysis_runs r
                join analysis_run_inputs a on a.run_id = r.id and a.submission_id = ?
                join analysis_run_inputs b on b.run_id = r.id and b.submission_id = ?
                left join similarity_results p on p.run_id = r.id and p.a = a.submission_id and p.b = b.submission_id
                where r.assignment_id = ? and r.revision = ? and r.method = 'structsim'
                    and (a.scope = 'target' or b.scope = 'target')
                """, (rs, index) -> {
            Score score = switch (rs.getString("status")) {
                case "queued", "running" -> Score.pending();
                case "done" -> storedScore(rs);
                default -> Score.failed(reason(rs.getString("error_code")));
            };
            var fragments = new ArrayList<Fragment>();
            JsonNode details = json.createObjectNode();
            if (score.status().equals("ok")) {
                details = json.readTree(rs.getString("details"));
                for (JsonNode fragment : json.readTree(rs.getString("fragments"))) {
                    fragments.add(new Fragment(fragment.get(reverse ? "b" : "a"), fragment.get(reverse ? "a" : "b")));
                }
            }
            return new PairResult(a, b, score, Map.of("structsim", new StructsimResult(score, fragments, details)));
        }, reverse ? b : a, reverse ? a : b, assignment.id(), assignment.revision())
                .stream().findFirst().orElseThrow(ApiException::notFound);
    }

    private static SubmissionRow row(ResultSet rs) throws SQLException {
        return new SubmissionRow(rs.getObject("id", UUID.class), rs.getObject("student_id", UUID.class),
                rs.getString("full_name"), Inputs.instant(rs, "submitted_at"), score(rs, "sim"), score(rs, "ai"));
    }

    private static Score score(ResultSet rs, String prefix) throws SQLException {
        return switch (rs.getString(prefix + "_status")) {
            case "ok" -> Score.ok(rs.getDouble(prefix + "_value"));
            case "pending" -> Score.pending();
            case "not_applicable" -> Score.notApplicable();
            default -> Score.failed(rs.getString(prefix + "_reason"));
        };
    }

    private static Score storedScore(ResultSet rs) throws SQLException {
        Double value = rs.getObject("value", Double.class);
        return value == null ? Score.failed(reason(rs.getString("reason_code"))) : Score.ok(value);
    }

    private static String reason(String code) {
        return code == null ? "RESULT_MISSING" : code;
    }

    record Counts(int total, Progress progress) {
        static final Counts EMPTY = new Counts(0, new Progress(0, 0));
    }
}
