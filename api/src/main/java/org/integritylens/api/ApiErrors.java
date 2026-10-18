package org.integritylens.api;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ApiErrors {
    private static final Logger LOG = LoggerFactory.getLogger(ApiErrors.class);

    public record Error(String code, String message, Map<String, Object> details) {}

    @ExceptionHandler(ApiException.class)
    ResponseEntity<Error> api(ApiException ex) {
        return ResponseEntity.status(ex.status).body(new Error(ex.code, ex.getMessage(), ex.details));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Error> tooLarge() {
        return api(new ApiException(413, "ARCHIVE_TOO_LARGE", "Размер архива превышает 10 МиБ"));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class, MultipartException.class,
            HttpMediaTypeNotSupportedException.class})
    ResponseEntity<Error> invalid(Exception ex) {
        return api(new ApiException(400, "VALIDATION_ERROR", "Некорректный запрос"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Error> unexpected(Exception ex) {
        LOG.error("Request failed: {}", ex.getClass().getSimpleName());
        return api(new ApiException(503, "SERVICE_UNAVAILABLE", "Сервис временно недоступен"));
    }
}
