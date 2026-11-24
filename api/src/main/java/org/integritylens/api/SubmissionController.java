package org.integritylens.api;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.integritylens.api.Models.*;

@RestController
@RequestMapping("/api")
class SubmissionController {
    private final AuthService auth;
    private final Assignments assignments;
    private final Submissions submissions;
    private final AnalysisRead analysis;

    SubmissionController(AuthService auth, Assignments assignments, Submissions submissions, AnalysisRead analysis) {
        this.auth = auth;
        this.assignments = assignments;
        this.submissions = submissions;
        this.analysis = analysis;
    }

    @GetMapping("/submit/{token}")
    SubmissionForm form(@CookieValue(name = AuthService.COOKIE, required = false) String session,
                        @PathVariable String token) {
        return submissions.form(token, auth.require(session, "student").id());
    }

    @PostMapping("/submit/{token}")
    @ResponseStatus(HttpStatus.CREATED)
    Receipt accept(@CookieValue(name = AuthService.COOKIE, required = false) String session, @PathVariable String token,
                   HttpServletRequest request) throws IOException {
        UUID student = auth.require(session, "student").id();
        return submissions.accept(token, student, SubmissionFile.read(request));
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    SubmissionList list(@CookieValue(name = AuthService.COOKIE, required = false) String session,
                        @PathVariable String assignmentId, @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String page, @RequestParam(required = false) String pageSize) {
        var assignment = owned(session, assignmentId);
        return analysis.list(assignment.id(), sort == null ? "similarity" : sort,
                positiveInt(page, 1, Integer.MAX_VALUE), positiveInt(pageSize, 20, 100));
    }

    @GetMapping("/assignments/{assignmentId}/submissions/{submissionId}")
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    SubmissionDetail detail(@CookieValue(name = AuthService.COOKIE, required = false) String session,
                            @PathVariable String assignmentId, @PathVariable String submissionId) {
        return analysis.detail(owned(session, assignmentId), Inputs.uuid(submissionId));
    }

    @GetMapping("/assignments/{assignmentId}/pairs/{a}/{b}")
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    PairResult pair(@CookieValue(name = AuthService.COOKIE, required = false) String session,
                    @PathVariable String assignmentId, @PathVariable String a, @PathVariable String b) {
        return analysis.pair(owned(session, assignmentId), Inputs.uuid(a), Inputs.uuid(b));
    }

    @GetMapping("/submissions/{submissionId}/source")
    ResponseEntity<byte[]> source(@CookieValue(name = AuthService.COOKIE, required = false) String session,
                                  @PathVariable String submissionId) {
        byte[] bytes = submissions.source(auth.require(session, "teacher").id(), Inputs.uuid(submissionId));
        return ResponseEntity.ok().contentType(new MediaType("text", "plain", StandardCharsets.UTF_8)).body(bytes);
    }

    private AssignmentRow owned(String session, String assignment) {
        return assignments.owned(auth.require(session, "teacher").id(), Inputs.uuid(assignment));
    }

    private static int positiveInt(String raw, int fallback, int max) {
        if (raw == null) {
            return fallback;
        }
        try {
            int value = Integer.parseInt(raw);
            if (value >= 1 && value <= max) {
                return value;
            }
        } catch (NumberFormatException ignored) {
        }
        throw ApiException.invalid("Некорректные параметры пагинации.");
    }
}
