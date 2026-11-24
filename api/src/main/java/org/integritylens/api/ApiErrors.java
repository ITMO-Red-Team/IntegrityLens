package org.integritylens.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
class ApiErrors {
    private static final Logger LOG = LoggerFactory.getLogger(ApiErrors.class);

    record Error(String code, String message, @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, Object> details) {}

    @ExceptionHandler(ApiException.class)
    ResponseEntity<Error> api(ApiException ex) {
        return ResponseEntity.status(ex.status).contentType(MediaType.APPLICATION_JSON)
                .body(new Error(ex.code, ex.getMessage(), ex.details));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Error> tooLarge() {
        return api(SubmissionFile.tooLarge());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<Error> notFound() {
        return api(ApiException.notFound());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Error> methodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        HttpHeaders headers = ex.getHeaders();
        return ResponseEntity.status(405).headers(headers).contentType(MediaType.APPLICATION_JSON)
                .body(new Error("METHOD_NOT_ALLOWED", "Метод запроса не поддерживается.", Map.of()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MultipartException.class,
            HttpMediaTypeNotSupportedException.class})
    ResponseEntity<Error> invalid(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.equals("/api/auth/register") || path.equals("/api/auth/login")) {
            var error = Map.of("field", "email", "code", "INVALID_VALUE", "message", "Некорректный запрос.");
            return api(new ApiException(422, "VALIDATION_ERROR", "Проверьте заполнение полей.",
                    Map.of("fields", List.of(error))));
        }
        if (path.startsWith("/api/submit/") && request.getMethod().equals("POST")) {
            return api(SubmissionFile.required());
        }
        return api(ApiException.invalid("Некорректный запрос."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Error> unexpected(Exception ex) {
        LOG.error("Request failed: {} at {}", ex.getClass().getSimpleName(),
                ex.getStackTrace().length == 0 ? "unknown" : ex.getStackTrace()[0]);
        return api(new ApiException(503, "SERVICE_UNAVAILABLE", "Сервис временно недоступен."));
    }
}
