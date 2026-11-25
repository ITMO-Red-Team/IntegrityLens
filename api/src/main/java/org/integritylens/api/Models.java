package org.integritylens.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

final class Models {
    record User(UUID id, String fullName, String role) {}

    record AssignmentRow(UUID id, UUID teacherId, String title, String token, long revision, Instant createdAt) {}

    record Progress(int completed, int failed) {}

    record AssignmentSummary(UUID id, String title, Instant createdAt, int submissionCount, Progress analysis) {}

    record Assignment(UUID id, String title, Instant createdAt, int submissionCount, Progress analysis,
                      String submissionUrl, List<UUID> sourceAssignmentIds) {}

    record Receipt(UUID submissionId, Instant submittedAt) {}

    record SubmissionForm(String title, String teacherFullName, boolean canSubmit, Receipt currentSubmission) {}

    record Reason(String code, String message) {}

    record ComparisonCoverage(int successful, int skipped) {}

    record Score(String status, Double value, @JsonInclude(JsonInclude.Include.NON_NULL) Reason reason,
                 @JsonInclude(JsonInclude.Include.NON_NULL) ComparisonCoverage comparisons) {
        Score withComparisons(int successful, int skipped) {
            return new Score(status, value, reason, new ComparisonCoverage(successful, skipped));
        }

        static Score ok(double value) {
            return new Score("ok", value, null, null);
        }

        static Score pending() {
            return new Score("pending", null, null, null);
        }

        static Score notApplicable() {
            return new Score("not_applicable", null, null, null);
        }

        static Score failed(String code) {
            String message = switch (code) {
                case "NOT_PARSED" -> "Не удалось разобрать файл как Java-код.";
                case "RUN_TIMEOUT" -> "Истекло время анализа.";
                default -> "Не удалось завершить анализ.";
            };
            return new Score("failed", null, new Reason(code, message), null);
        }
    }

    record SubmissionRow(UUID submissionId, UUID studentId, String fullName, Instant submittedAt,
                         Score similarity, Score ai) {}

    record SubmissionList(List<SubmissionRow> items, int page, int pageSize, int total) {}

    record SimilarWork(UUID submissionId, UUID assignmentId, String assignmentTitle, Score score) {}

    record SubmissionDetail(UUID submissionId, UUID studentId, String fullName, Instant submittedAt,
                            Score similarity, Score ai, JsonNode suspicious, List<SimilarWork> similar) {}

    record Fragment(JsonNode a, JsonNode b) {}

    record StructsimResult(Score score, List<Fragment> fragments, JsonNode details) {}

    record PairResult(UUID a, UUID b, Score score, Map<String, StructsimResult> methods) {}
}
