package org.integritylens.api;

import java.util.Map;

public class ApiException extends RuntimeException {
    final int status;
    final String code;
    final Map<String, Object> details;

    public ApiException(int status, String code, String message) {
        this(status, code, message, Map.of());
    }

    public ApiException(int status, String code, String message, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }

    static ApiException notFound() {
        return new ApiException(404, "NOT_FOUND", "Объект не найден");
    }
}
