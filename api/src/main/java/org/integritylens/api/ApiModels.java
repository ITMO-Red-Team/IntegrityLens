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

    @Schema(requiredProperties = {"id", "title", "language", "status", "createdAt", "submissionCount", "pairCount", "runs"})
    public record Batch(UUID id, String title, String language,
            @Schema(allowableValues = {"queued", "processing", "done", "failed"}) String status,
            Instant createdAt, int submissionCount, int pairCount, List<Run> runs) {}

    @Schema(requiredProperties = {"method", "methodVersion", "status", "startedAt", "finishedAt", "parsed", "failed", "error"})
    public record Run(@Schema(allowableValues = {"jplag", "structsim"}) String method,
            @Schema(nullable = true) String methodVersion,
            @Schema(allowableValues = {"queued", "running", "done", "failed"}) String status,
            @Schema(nullable = true) Instant startedAt, @Schema(nullable = true) Instant finishedAt,
            @Schema(nullable = true) Integer parsed, @Schema(nullable = true) Integer failed,
            @Schema(nullable = true) String error) {}

    @Schema(requiredProperties = {"code"})
    public record Reason(String code, @Schema(nullable = true) String message) {}
    @Schema(requiredProperties = {"a", "b"})
    public record Fragment(List<Integer> a, List<Integer> b) {}
    @Schema(requiredProperties = {"lines", "withLines"})
    public record MatchFragment(List<Integer> lines, List<Integer> withLines) {}
    @Schema(requiredProperties = {"with", "score", "fragments"})
    public record Match(String with, double score, List<MatchFragment> fragments) {}
    @Schema(requiredProperties = {"status", "score", "matches", "stats", "reason"})
    public record SubmissionMethod(
            @Schema(allowableValues = {"pending", "ok", "failed"}) String status,
            @Schema(nullable = true) Double score, List<Match> matches, Map<String, Object> stats,
            @Schema(nullable = true) Reason reason) {}
    @Schema(requiredProperties = {"id", "originalName", "language", "loc", "methods"})
    public record Submission(UUID id, String originalName, String language, int loc,
            Map<String, SubmissionMethod> methods) {}
    @Schema(requiredProperties = {"originalName", "language", "loc"})
    public record SubmissionInfo(String originalName, String language, int loc) {}
    @Schema(requiredProperties = {"submissions", "total"})
    public record Submissions(List<Submission> submissions, int total) {}
    @Schema(requiredProperties = {"value", "details", "fragments", "reason"})
    public record PairScore(@Schema(nullable = true) Double value, Map<String, Object> details,
            List<Fragment> fragments, @Schema(nullable = true) Reason reason) {}
    @Schema(requiredProperties = {"id", "a", "b", "scores", "decision"})
    public record PairSummary(String id, UUID a, UUID b, Map<String, PairScore> scores,
            @Schema(nullable = true) Decision decision) {}
    @Schema(requiredProperties = {"pairs", "total"})
    public record Pairs(List<PairSummary> pairs, int total) {}
    @Schema(requiredProperties = {"id", "batchId", "a", "b", "submissions", "scores", "decision", "feedback"})
    public record PairDetail(String id, UUID batchId, UUID a, UUID b, Map<String, SubmissionInfo> submissions,
            Map<String, PairScore> scores, @Schema(nullable = true) Decision decision,
            @Schema(nullable = true) Feedback feedback) {}
    public enum Decision { confirmed, acceptable, false_positive }
    public record FeedbackRequest(@NotNull Decision decision, @Schema(nullable = true) @Size(max = 10000) String comment) {}
    @Schema(requiredProperties = {"decision", "comment", "updatedAt"})
    public record Feedback(Decision decision, String comment, Instant updatedAt) {}

    public record CoreSubmission(String id, String status, Double score, List<Match> matches,
            Map<String, Object> stats, Reason reason) {}
    public record CorePair(String a, String b, Double value, Map<String, Object> details,
            List<Fragment> fragments, Reason reason) {}
    public record CoreResult(String contract, String method, String methodVersion, Map<String, Object> params,
            List<CoreSubmission> submissions, List<CorePair> pairs, List<String> warnings) {}
}
