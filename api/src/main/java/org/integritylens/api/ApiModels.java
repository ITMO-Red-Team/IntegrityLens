package org.integritylens.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ApiModels {
    private ApiModels() {}

    public record Batch(UUID id, String title, String language,
            @Schema(allowableValues = {"queued", "processing", "done", "failed"}) String status,
            Instant createdAt, int submissionCount, int pairCount, List<Run> runs) {}

    public record Run(String method, String methodVersion,
            @Schema(allowableValues = {"queued", "running", "done", "failed"}) String status,
            Instant startedAt, Instant finishedAt, Integer parsed, Integer failed, String error) {}

    public record Reason(String code, String message) {}
    public record Fragment(List<Integer> a, List<Integer> b) {}
    public record MatchFragment(List<Integer> lines, List<Integer> withLines) {}
    public record Match(String with, double score, List<MatchFragment> fragments) {}
    public record SubmissionMethod(
            @Schema(allowableValues = {"pending", "ok", "failed"}) String status,
            Double score, List<Match> matches, Map<String, Object> stats, Reason reason) {}
    public record Submission(UUID id, String originalName, String language, int loc,
            Map<String, SubmissionMethod> methods) {}
    public record SubmissionInfo(String originalName, String language, int loc) {}
    public record Submissions(List<Submission> submissions, int total) {}
    public record PairScore(Double value, Map<String, Object> details, List<Fragment> fragments, Reason reason) {}
    public record PairSummary(String id, UUID a, UUID b, Map<String, PairScore> scores, Decision decision) {}
    public record Pairs(List<PairSummary> pairs, int total) {}
    public record PairDetail(String id, UUID batchId, UUID a, UUID b, Map<String, SubmissionInfo> submissions,
            Map<String, PairScore> scores, Decision decision, Feedback feedback) {}
    public enum Decision { confirmed, acceptable, false_positive }
    public record FeedbackRequest(@NotNull Decision decision, @Size(max = 10000) String comment) {}
    public record Feedback(Decision decision, String comment, Instant updatedAt) {}

    public record CoreSubmission(String id, String status, Double score, List<Match> matches,
            Map<String, Object> stats, Reason reason) {}
    public record CorePair(String a, String b, Double value, Map<String, Object> details,
            List<Fragment> fragments, Reason reason) {}
    public record CoreResult(String contract, String method, String methodVersion, Map<String, Object> params,
            List<CoreSubmission> submissions, List<CorePair> pairs, List<String> warnings) {}
}
