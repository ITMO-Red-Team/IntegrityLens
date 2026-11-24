package org.integritylens.api;

import static org.integritylens.api.ApiModels.*;
import static org.integritylens.api.BackendRepository.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class QueryService {
    private final BackendRepository repository;
    private final ObjectStorage storage;
    private final ObjectMapper mapper;

    record MethodResult(RunRow run, Map<String, CoreSubmission> submissions, Map<String, CorePair> pairs) {}
    record PairKey(UUID batchId, UUID a, UUID b) {}

    public QueryService(BackendRepository repository, ObjectStorage storage, ObjectMapper mapper) {
        this.repository = repository;
        this.storage = storage;
        this.mapper = mapper;
    }

    List<Batch> batches() {
        return repository.batches().stream().map(this::batch).toList();
    }

    Batch batch(UUID id) {
        return batch(repository.batch(id));
    }

    private Batch batch(BatchRow row) {
        var runs = repository.runs(row.id());
        var summaries = runs.stream().map(run -> {
            var result = result(run);
            Integer parsed = result == null ? null : (int) result.submissions().stream()
                    .filter(s -> "ok".equals(s.status())).count();
            Integer failed = result == null ? null : result.submissions().size() - parsed;
            return new Run(run.method(), run.version(), run.status(), run.startedAt(), run.finishedAt(),
                    parsed, failed, run.error());
        }).toList();
        String status;
        if (runs.stream().allMatch(r -> "queued".equals(r.status()))) status = "queued";
        else if (runs.stream().anyMatch(r -> "queued".equals(r.status()) || "running".equals(r.status()))) status = "processing";
        else if (runs.stream().anyMatch(r -> "done".equals(r.status()))) status = "done";
        else status = "failed";
        return new Batch(row.id(), row.title(), row.language(), status, row.createdAt(), row.count(),
                row.count() * (row.count() - 1) / 2, summaries);
    }

    Submissions submissions(UUID batchId) {
        repository.batch(batchId);
        var methods = methods(batchId);
        var submissions = repository.submissions(batchId).stream().map(row -> {
            Map<String, SubmissionMethod> values = new LinkedHashMap<>();
            for (var method : methods) {
                var reason = unavailable(method.run());
                if (reason != null) {
                    values.put(method.run().method(), new SubmissionMethod(
                            "RUN_NOT_FINISHED".equals(reason.code()) ? "pending" : "failed",
                            null, List.of(), Map.of(), reason));
                } else {
                    var entry = method.submissions().get(row.id().toString());
                    values.put(method.run().method(), new SubmissionMethod(entry.status(), entry.score(),
                            entry.matches() == null ? List.of() : entry.matches().stream().map(m -> new Match(
                                    m.with(), m.score(), m.fragments() == null ? List.of() : m.fragments())).toList(),
                            entry.stats() == null ? Map.of() : entry.stats(), entry.reason()));
                }
            }
            return new Submission(row.id(), row.name(), row.language(), row.loc(), values);
        }).toList();
        return new Submissions(submissions, submissions.size());
    }

    byte[] source(UUID id) {
        return storage.get(repository.submission(id).storageKey());
    }

    Pairs pairs(UUID batchId) {
        repository.batch(batchId);
        var methods = methods(batchId);
        var ids = repository.submissions(batchId).stream().map(s -> s.id().toString()).sorted().toList();
        Map<String, Feedback> feedback = feedback(batchId);
        List<PairSummary> pairs = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                String a = ids.get(i);
                String b = ids.get(j);
                String pair = a + ":" + b;
                var review = feedback.get(pair);
                pairs.add(new PairSummary(batchId + ":" + pair, UUID.fromString(a), UUID.fromString(b),
                        scores(methods, a, b, false), review == null ? null : review.decision()));
            }
        }
        return new Pairs(pairs, pairs.size());
    }

    PairDetail pair(String id) {
        PairKey key = pairKey(id);
        var a = repository.submission(key.a());
        var b = repository.submission(key.b());
        var feedback = feedback(key.batchId()).get(key.a() + ":" + key.b());
        return new PairDetail(id, key.batchId(), key.a(), key.b(), Map.of(
                key.a().toString(), new SubmissionInfo(a.name(), a.language(), a.loc()),
                key.b().toString(), new SubmissionInfo(b.name(), b.language(), b.loc())),
                scores(methods(key.batchId()), key.a().toString(), key.b().toString(), true),
                feedback == null ? null : feedback.decision(), feedback);
    }

    Feedback saveFeedback(String id, FeedbackRequest request) {
        var key = pairKey(id);
        return repository.saveFeedback(key.batchId(), key.a(), key.b(), request);
    }

    private PairKey pairKey(String id) {
        String[] parts = id.split(":", -1);
        if (parts.length != 3) throw ApiException.notFound();
        try {
            UUID batch = UUID.fromString(parts[0]);
            UUID a = UUID.fromString(parts[1]);
            UUID b = UUID.fromString(parts[2]);
            if (!batch.toString().equals(parts[0]) || !a.toString().equals(parts[1])
                    || !b.toString().equals(parts[2]) || parts[1].compareTo(parts[2]) >= 0
                    || !repository.submission(a).batchId().equals(batch)
                    || !repository.submission(b).batchId().equals(batch)) {
                throw ApiException.notFound();
            }
            return new PairKey(batch, a, b);
        } catch (IllegalArgumentException ex) {
            throw ApiException.notFound();
        }
    }

    private Map<String, PairScore> scores(List<MethodResult> methods, String a, String b, boolean fragments) {
        Map<String, PairScore> scores = new LinkedHashMap<>();
        for (var method : methods) {
            var reason = unavailable(method.run());
            if (reason != null) {
                scores.put(method.run().method(), new PairScore(null, Map.of(), List.of(), reason));
            } else {
                var entry = method.pairs().get(a + ":" + b);
                scores.put(method.run().method(), new PairScore(entry.value(),
                        entry.details() == null ? Map.of() : entry.details(),
                        fragments && entry.fragments() != null ? entry.fragments() : List.of(), entry.reason()));
            }
        }
        return scores;
    }

    private Map<String, Feedback> feedback(UUID batchId) {
        Map<String, Feedback> values = new LinkedHashMap<>();
        repository.feedback(batchId).forEach(f -> values.put(f.a() + ":" + f.b(), f.value()));
        return values;
    }

    private List<MethodResult> methods(UUID batchId) {
        return repository.runs(batchId).stream().map(run -> {
            Map<String, CoreSubmission> submissions = new LinkedHashMap<>();
            Map<String, CorePair> pairs = new LinkedHashMap<>();
            var result = result(run);
            if (result != null) {
                result.submissions().forEach(s -> submissions.put(s.id(), s));
                result.pairs().forEach(p -> pairs.put(p.a() + ":" + p.b(), p));
            }
            return new MethodResult(run, submissions, pairs);
        }).toList();
    }

    private CoreResult result(RunRow run) {
        return "done".equals(run.status()) && run.result() != null
                ? mapper.readValue(run.result(), CoreResult.class) : null;
    }

    private Reason unavailable(RunRow run) {
        if ("failed".equals(run.status())) return new Reason("RUN_FAILED", "Метод завершился с ошибкой");
        if (!"done".equals(run.status())) return new Reason("RUN_NOT_FINISHED", "Анализ ещё не завершён");
        return null;
    }
}
