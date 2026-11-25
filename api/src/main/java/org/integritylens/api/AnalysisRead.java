package org.integritylens.api;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.integritylens.api.Models.*;

@Component
class AnalysisRead {
    private static final String COUNTS = """
            with statuses as (
                select a.id as assignment_id,
                    case when sim.id is null then 'not_applicable'
                         when sim.status in ('queued', 'running') then 'pending'
                         when sim.status <> 'done' or pairs.blocked or not pairs.has_pairs then 'failed'
                         when pairs.has_score then 'ok'
                         when pairs.legacy_parse_failure then 'failed'
                         else 'not_applicable' end as sim_status,
                    case when ai.id is null or ai.status in ('queued', 'running') then 'pending'
                         when ai.status = 'done' and result.score is not null then 'ok'
                         else 'failed' end as ai_status
                from assignments a
                join submission_slots slot on slot.assignment_id = a.id
                join submissions s on s.id = slot.current_submission_id
                left join analysis_runs sim on sim.assignment_id = a.id and sim.revision = a.similarity_revision
                    and sim.method = 'structsim'
                left join lateral (
                    select count(*) > 0 as has_pairs, count(p.value) > 0 as has_score,
                        count(*) filter (where p.reason_code = 'NOT_PARSED') > 0 as legacy_parse_failure,
                        count(*) filter (where p.reason_code <> 'NOT_PARSED'
                            and (p.reason_code <> 'NOT_PARSED_A' or p.a = s.id)
                            and (p.reason_code <> 'NOT_PARSED_B' or p.b = s.id)) > 0 as blocked
                    from similarity_results p
                    where p.run_id = sim.id and (p.a = s.id or p.b = s.id)
                ) pairs on true
                left join analysis_runs ai on ai.submission_id = s.id and ai.method = 'ai'
                    and ai.method_version = :aiVersion
                left join ai_results result on result.run_id = ai.id
                where a.deleted_at is null
                    and (cast(:teacherId as uuid) is null or a.teacher_id = :teacherId)
                    and (cast(:assignmentId as uuid) is null or a.id = :assignmentId)
            )
            select assignment_id, count(*) as total,
                count(*) filter (where sim_status <> 'pending' and ai_status <> 'pending') as completed,
                count(*) filter (where sim_status <> 'pending' and ai_status <> 'pending'
                    and (sim_status = 'failed' or ai_status = 'failed')) as failed
            from statuses
            group by assignment_id
            """;
    private static final String SCORES = """
            with scores as (
                select s.id, s.student_id, u.full_name, s.submitted_at,
                    case when sim.id is null then 'not_applicable'
                         when sim.status in ('queued', 'running') then 'pending'
                         when sim.status <> 'done' or pairs.own_parse_failure
                             or pairs.other_reason is not null or not pairs.has_pairs then 'failed'
                         when pairs.value is not null then 'ok'
                         when pairs.legacy_parse_failure then 'failed'
                         else 'not_applicable' end as sim_status,
                    case when pairs.own_parse_failure then 'NOT_PARSED'
                         when pairs.other_reason is not null then pairs.other_reason
                         when pairs.legacy_parse_failure and pairs.value is null and exists (
                             select 1
                             from similarity_results failed_pair
                             join similarity_results scored_pair on scored_pair.run_id = failed_pair.run_id
                                 and scored_pair.value is not null
                                 and (case when failed_pair.a = s.id then failed_pair.b else failed_pair.a end)
                                     in (scored_pair.a, scored_pair.b)
                             where failed_pair.run_id = sim.id and failed_pair.reason_code = 'NOT_PARSED'
                                 and s.id in (failed_pair.a, failed_pair.b)
                         ) then 'NOT_PARSED'
                         when pairs.legacy_parse_failure then 'PAIR_NOT_PARSED'
                         else coalesce(sim.error_code, 'RESULT_MISSING') end as sim_reason,
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
                    select max(p.value) as value, count(*) > 0 as has_pairs,
                        min(p.reason_code) filter (where p.reason_code not in
                            ('NOT_PARSED', 'NOT_PARSED_A', 'NOT_PARSED_B', 'NOT_PARSED_BOTH')) as other_reason,
                        bool_or(p.reason_code = 'NOT_PARSED_BOTH'
                            or (p.reason_code = 'NOT_PARSED_A' and p.a = s.id)
                            or (p.reason_code = 'NOT_PARSED_B' and p.b = s.id)) as own_parse_failure,
                        bool_or(p.reason_code = 'NOT_PARSED') as legacy_parse_failure
                    from similarity_results p
                    where p.run_id = sim.id and (p.a = s.id or p.b = s.id)
                ) pairs on true
                left join analysis_runs ai on ai.submission_id = s.id and ai.method = 'ai'
                    and ai.method_version = :aiVersion
                left join ai_results result on result.run_id = ai.id
                where a.id = :assignmentId and (cast(:submissionId as uuid) is null or s.id = :submissionId)
            )
            select * from scores
            order by case
                when :sort = 'ai' and ai_status = 'ok' then ai_value
                when :sort = 'similarity' and sim_status = 'ok' then sim_value
                end desc nulls last, id
            limit :pageSize offset :offset
            """;

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper json;
    private final String aiVersion;

    AnalysisRead(NamedParameterJdbcTemplate jdbc, ObjectMapper json,
                 @Value("${integritylens.analysis.ai-version}") String aiVersion) {
        this.jdbc = jdbc;
        this.json = json;
        this.aiVersion = aiVersion;
    }

    Map<UUID, Counts> countsForTeacher(UUID teacher) {
        return counts(teacher, null);
    }

    Counts countsForAssignment(UUID assignment) {
        return counts(null, assignment).getOrDefault(assignment, Counts.EMPTY);
    }

    private Map<UUID, Counts> counts(UUID teacher, UUID assignment) {
        var params = new MapSqlParameterSource().addValue("aiVersion", aiVersion)
                .addValue("teacherId", teacher).addValue("assignmentId", assignment);
        return jdbc.query(COUNTS, params, rs -> {
            Map<UUID, Counts> counts = new HashMap<>();
            while (rs.next()) {
                counts.put(rs.getObject("assignment_id", UUID.class),
                        new Counts(rs.getInt("total"), new Progress(rs.getInt("completed"), rs.getInt("failed"))));
            }
            return counts;
        });
    }

    SubmissionList list(UUID assignment, String sort, int page, int pageSize) {
        if (!sort.equals("ai") && !sort.equals("similarity")) {
            throw ApiException.invalid("Допускается сортировка по similarity или ai.");
        }
        int total = jdbc.queryForObject("""
                select count(*) from submission_slots where assignment_id = :assignmentId and current_submission_id is not null
                """, Map.of("assignmentId", assignment), Integer.class);
        long offset = (long) (page - 1) * pageSize;
        if (offset >= total) {
            return new SubmissionList(List.of(), page, pageSize, total);
        }
        List<SubmissionRow> items = jdbc.query(SCORES, scoreParams(assignment, null, sort, pageSize, offset),
                (rs, index) -> row(rs));
        return new SubmissionList(items, page, pageSize, total);
    }

    SubmissionDetail detail(AssignmentRow assignment, UUID submission) {
        return jdbc.query(SCORES, scoreParams(assignment.id(), submission, "similarity", 1, 0), (rs, index) -> {
            var row = row(rs);
            return new SubmissionDetail(row.submissionId(), row.studentId(), row.fullName(), row.submittedAt(),
                    row.similarity(), row.ai(),
                    row.ai().status().equals("ok") ? json.readTree(rs.getString("suspicious")) : json.createArrayNode(),
                    similar(assignment, submission));
        }).stream().findFirst().orElseThrow(ApiException::notFound);
    }

    private MapSqlParameterSource scoreParams(UUID assignment, UUID submission, String sort, int pageSize, long offset) {
        return new MapSqlParameterSource().addValue("aiVersion", aiVersion).addValue("assignmentId", assignment)
                .addValue("submissionId", submission).addValue("sort", sort)
                .addValue("pageSize", pageSize).addValue("offset", offset);
    }

    private List<SimilarWork> similar(AssignmentRow assignment, UUID submission) {
        var params = Map.of("assignmentId", assignment.id(), "revision", assignment.revision(),
                "submissionId", submission);
        return jdbc.query("""
                select s.id, s.assignment_id, a.title, p.value, p.reason_code, p.a <> :submissionId as reversed
                from analysis_runs r
                join similarity_results p on p.run_id = r.id
                join submissions s on s.id = case when p.a = :submissionId then p.b else p.a end
                join assignments a on a.id = s.assignment_id
                where r.assignment_id = :assignmentId and r.revision = :revision
                    and r.method = 'structsim' and r.status = 'done'
                    and (p.a = :submissionId or p.b = :submissionId)
                order by p.value desc nulls last, s.id
                """, params, (rs, index) -> new SimilarWork(rs.getObject("id", UUID.class),
                rs.getObject("assignment_id", UUID.class), rs.getString("title"), storedScore(rs, rs.getBoolean("reversed"))));
    }

    PairResult pair(AssignmentRow assignment, UUID a, UUID b) {
        if (a.equals(b)) {
            throw ApiException.notFound();
        }
        boolean reverse = a.toString().compareTo(b.toString()) > 0;
        var params = Map.of("assignmentId", assignment.id(), "revision", assignment.revision(),
                "a", reverse ? b : a, "b", reverse ? a : b);
        return jdbc.query("""
                select r.status, r.error_code, p.value, p.reason_code, p.details::text, p.fragments::text
                from analysis_runs r
                join analysis_run_inputs a on a.run_id = r.id and a.submission_id = :a
                join analysis_run_inputs b on b.run_id = r.id and b.submission_id = :b
                left join similarity_results p on p.run_id = r.id and p.a = a.submission_id and p.b = b.submission_id
                where r.assignment_id = :assignmentId and r.revision = :revision and r.method = 'structsim'
                    and (a.scope = 'target' or b.scope = 'target')
                """, params, (rs, index) -> {
            Score score = switch (rs.getString("status")) {
                case "queued", "running" -> Score.pending();
                case "done" -> storedScore(rs, reverse);
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
        }).stream().findFirst().orElseThrow(ApiException::notFound);
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

    private static Score storedScore(ResultSet rs, boolean reverse) throws SQLException {
        Double value = rs.getObject("value", Double.class);
        if (value != null) {
            return Score.ok(value);
        }
        String code = reason(rs.getString("reason_code"));
        return Score.failed(switch (code) {
            case "NOT_PARSED" -> "PAIR_NOT_PARSED";
            case "NOT_PARSED_A" -> reverse ? "NOT_PARSED_B" : code;
            case "NOT_PARSED_B" -> reverse ? "NOT_PARSED_A" : code;
            default -> code;
        });
    }

    private static String reason(String code) {
        return code == null ? "RESULT_MISSING" : code;
    }

    record Counts(int total, Progress progress) {
        static final Counts EMPTY = new Counts(0, new Progress(0, 0));
    }
}
