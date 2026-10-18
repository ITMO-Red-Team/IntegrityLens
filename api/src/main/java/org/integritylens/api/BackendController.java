package org.integritylens.api;

import static org.integritylens.api.ApiModels.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class BackendController {
    private final UploadService uploads;
    private final QueryService queries;

    public BackendController(UploadService uploads, QueryService queries) {
        this.uploads = uploads;
        this.queries = queries;
    }

    @Operation(summary = "Загрузить ZIP с 5–50 Java-работами",
            description = "Идентичные байты ZIP возвращают существующий пакет; первоначальный title сохраняется.")
    @ApiResponse(responseCode = "202", description = "Создан новый пакет")
    @ApiResponse(responseCode = "200", description = "Архив уже загружен")
    @PostMapping(value = "/batches", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Batch> upload(@RequestPart MultipartFile archive,
            @RequestParam @Schema(allowableValues = "java") String language,
            @RequestParam(required = false) String title) throws IOException {
        var result = uploads.upload(archive, language, title);
        return ResponseEntity.status(result.created() ? 202 : 200)
                .location(URI.create("/api/batches/" + result.id())).body(queries.batch(result.id()));
    }

    @GetMapping("/batches")
    public List<Batch> batches() {
        return queries.batches();
    }

    @GetMapping("/batches/{batchId}")
    public Batch batch(@PathVariable UUID batchId) {
        return queries.batch(batchId);
    }

    @GetMapping("/batches/{batchId}/submissions")
    public Submissions submissions(@PathVariable UUID batchId) {
        return queries.submissions(batchId);
    }

    @GetMapping(value = "/submissions/{submissionId}/source", produces = "text/plain;charset=UTF-8")
    public byte[] source(@PathVariable UUID submissionId) {
        return queries.source(submissionId);
    }

    @GetMapping("/batches/{batchId}/pairs")
    public Pairs pairs(@PathVariable UUID batchId) {
        return queries.pairs(batchId);
    }

    @GetMapping("/pairs/{pairId}")
    public PairDetail pair(@PathVariable String pairId) {
        return queries.pair(pairId);
    }

    @PutMapping("/pairs/{pairId}/feedback")
    public Feedback feedback(@PathVariable String pairId, @Valid @RequestBody FeedbackRequest body) {
        return queries.saveFeedback(pairId, body);
    }
}
