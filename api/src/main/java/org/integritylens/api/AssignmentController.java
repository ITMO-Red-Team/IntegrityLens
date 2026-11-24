package org.integritylens.api;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.integritylens.api.Models.Assignment;
import static org.integritylens.api.Models.AssignmentSummary;

@RestController
@RequestMapping("/api/assignments")
class AssignmentController {
    private final AuthService auth;
    private final Assignments assignments;

    AssignmentController(AuthService auth, Assignments assignments) {
        this.auth = auth;
        this.assignments = assignments;
    }

    @GetMapping
    List<AssignmentSummary> list(@CookieValue(name = AuthService.COOKIE, required = false) String token) {
        return assignments.list(auth.require(token, "teacher").id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Assignment create(@CookieValue(name = AuthService.COOKIE, required = false) String token,
                      @RequestBody(required = false) Map<String, Object> body) {
        var teacher = auth.require(token, "teacher").id();
        var fields = Inputs.fields(body, "title", "sourceAssignmentIds");
        return assignments.create(teacher, Inputs.title(fields.get("title")),
                fields.containsKey("sourceAssignmentIds") ? Inputs.uuids(fields.get("sourceAssignmentIds")) : List.of());
    }

    @GetMapping("/{assignmentId}")
    Assignment get(@CookieValue(name = AuthService.COOKIE, required = false) String token,
                   @PathVariable String assignmentId) {
        return assignments.get(auth.require(token, "teacher").id(), Inputs.uuid(assignmentId));
    }

    @PutMapping("/{assignmentId}/sources")
    Assignment sources(@CookieValue(name = AuthService.COOKIE, required = false) String token,
                       @PathVariable String assignmentId, @RequestBody(required = false) Map<String, Object> body) {
        var teacher = auth.require(token, "teacher").id();
        var fields = Inputs.fields(body, "sourceAssignmentIds");
        return assignments.updateSources(teacher, Inputs.uuid(assignmentId), Inputs.uuids(fields.get("sourceAssignmentIds")));
    }

    @PostMapping("/{assignmentId}/students/{studentId}/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void reset(@CookieValue(name = AuthService.COOKIE, required = false) String token, @PathVariable String assignmentId,
               @PathVariable String studentId, @RequestBody(required = false) Map<String, Object> body) {
        var teacher = auth.require(token, "teacher").id();
        var fields = Inputs.fields(body, "expectedSubmissionId");
        assignments.reset(teacher, Inputs.uuid(assignmentId), Inputs.uuid(studentId),
                Inputs.uuid(fields.get("expectedSubmissionId")));
    }

    @DeleteMapping("/{assignmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@CookieValue(name = AuthService.COOKIE, required = false) String token, @PathVariable String assignmentId) {
        assignments.delete(auth.require(token, "teacher").id(), Inputs.uuid(assignmentId));
    }
}
