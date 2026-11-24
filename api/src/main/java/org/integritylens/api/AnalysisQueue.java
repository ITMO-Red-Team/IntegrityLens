package org.integritylens.api;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class AnalysisQueue {
    private static final String SNAPSHOT = """
            select current_submission_id as submission_id, 'target' as scope
            from submission_slots where assignment_id = ? and current_submission_id is not null
            union all
            select slot.current_submission_id, 'source'
            from assignment_sources src
            join submission_slots slot on slot.assignment_id = src.source_assignment_id
            where src.assignment_id = ? and slot.current_submission_id is not null
            """;

    private final JdbcTemplate jdbc;
    private final String structsimVersion;
    private final String aiVersion;

    AnalysisQueue(JdbcTemplate jdbc, @Value("${integritylens.analysis.structsim-version}") String structsimVersion,
                  @Value("${integritylens.analysis.ai-version}") String aiVersion) {
        this.jdbc = jdbc;
        this.structsimVersion = structsimVersion;
        this.aiVersion = aiVersion;
    }

    void enqueueAi(UUID submissionId) {
        jdbc.update("""
                insert into analysis_runs(id, method, contract_version, method_version, submission_id)
                values (?, 'ai', 'integritylens.ai/1', ?, ?)
                """, UUID.randomUUID(), aiVersion, submissionId);
    }

    void enqueueMissingAi() {
        jdbc.update("""
                insert into analysis_runs(id, method, contract_version, method_version, submission_id)
                select gen_random_uuid(), 'ai', 'integritylens.ai/1', ?, slot.current_submission_id
                from submission_slots slot join assignments a on a.id = slot.assignment_id
                join submissions s on s.id = slot.current_submission_id
                where a.deleted_at is null
                and not exists (select 1 from analysis_runs r
                                where r.submission_id = slot.current_submission_id and r.method_version = ?)
                for key share of s skip locked
                on conflict (submission_id, method_version) do nothing
                """, aiVersion, aiVersion);
    }

    List<UUID> dependents(UUID source) {
        return jdbc.queryForList("""
                select assignment_id from assignment_sources where source_assignment_id = ? order by assignment_id
                """, UUID.class, source);
    }

    void submissionChanged(UUID assignment) {
        advance(assignment);
        dependents(assignment).forEach(this::advance);
    }

    void advance(UUID assignment) {
        var revisions = jdbc.query("""
                update assignments set similarity_revision = similarity_revision + 1
                where id = ? and deleted_at is null returning similarity_revision
                """, (rs, index) -> rs.getLong(1), assignment);
        if (revisions.isEmpty()) {
            return;
        }
        long revision = revisions.getFirst();
        cancelSimilarity(assignment);
        UUID run = UUID.randomUUID();
        int inserted = jdbc.update("""
                with inputs as (%s)
                insert into analysis_runs(id, method, contract_version, method_version, assignment_id, revision)
                select ?, 'structsim', 'integritylens.core/2', ?, ?, ?
                from inputs
                having count(*) >= 2 and count(*) filter (where scope = 'target') >= 1
                """.formatted(SNAPSHOT), assignment, assignment, run, structsimVersion, assignment, revision);
        if (inserted != 0) {
            jdbc.update("""
                    insert into analysis_run_inputs(run_id, submission_id, scope)
                    select ?, inputs.* from (%s) inputs
                    """.formatted(SNAPSHOT), run, assignment, assignment);
        }
    }

    void cancelSimilarity(UUID assignment) {
        jdbc.update("""
                update analysis_runs set status = 'cancelled', locked_by = null, heartbeat_at = null,
                    finished_at = now(), error_code = null
                where assignment_id = ? and method = 'structsim' and status in ('queued', 'running')
                """, assignment);
    }
}
