package org.integritylens.api;

import static org.integritylens.api.ApiModels.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Некорректный запрос",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrors.Error.class))),
        @ApiResponse(responseCode = "404", description = "Объект не найден",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrors.Error.class))),
        @ApiResponse(responseCode = "413", description = "Архив слишком большой",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrors.Error.class))),
        @ApiResponse(responseCode = "503", description = "Сервис недоступен",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrors.Error.class)))
})
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
            @RequestPart @Schema(allowableValues = "java") String language,
            @RequestPart(required = false) String title) throws IOException {
        var result = uploads.upload(archive, language, title);
        return ResponseEntity.status(result.created() ? 202 : 200)
                .location(URI.create("/api/batches/" + result.id())).body(queries.batch(result.id()));
    }

    @GetMapping("/batches")
    @ApiResponse(responseCode = "200", description = "Список пакетов")
    public List<Batch> batches() {
        return queries.batches();
    }

    @GetMapping("/batches/{batchId}")
    @ApiResponse(responseCode = "200", description = "Пакет и прогоны")
    public Batch batch(@PathVariable UUID batchId) {
        return queries.batch(batchId);
    }

    @GetMapping("/batches/{batchId}/submissions")
    @ApiResponse(responseCode = "200", description = "Работы пакета")
    public Submissions submissions(@PathVariable UUID batchId) {
        return queries.submissions(batchId);
    }

    @GetMapping(value = "/submissions/{submissionId}/source", produces = "text/plain;charset=UTF-8")
    @ApiResponse(responseCode = "200", description = "Исходный код в UTF-8",
            content = @Content(mediaType = "text/plain;charset=UTF-8", schema = @Schema(type = "string")))
    public byte[] source(@PathVariable UUID submissionId) {
        return queries.source(submissionId);
    }

    @GetMapping("/batches/{batchId}/pairs")
    @ApiResponse(responseCode = "200", description = "Пары пакета")
    public Pairs pairs(@PathVariable UUID batchId) {
        return queries.pairs(batchId);
    }

    @GetMapping("/pairs/{pairId}")
    @ApiResponse(responseCode = "200", description = "Пара с оценками и отзывом")
    public PairDetail pair(@PathVariable String pairId) {
        return queries.pair(pairId);
    }

    @PutMapping("/pairs/{pairId}/feedback")
    @ApiResponse(responseCode = "200", description = "Сохранённый отзыв")
    public Feedback feedback(@PathVariable String pairId, @Valid @RequestBody FeedbackRequest body) {
        return queries.saveFeedback(pairId, body);
    }
}
