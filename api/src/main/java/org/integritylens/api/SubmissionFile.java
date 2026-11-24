package org.integritylens.api;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

record SubmissionFile(byte[] bytes, int lines, String sha256) {
    private static final int MAX_BYTES = 1_000_000;

    static SubmissionFile read(HttpServletRequest request) throws IOException {
        if (!(request instanceof MultipartHttpServletRequest multipart)) {
            throw required();
        }
        var files = multipart.getMultiFileMap();
        int count = files.values().stream().mapToInt(java.util.List::size).sum();
        if (count > 1) {
            throw new ApiException(422, "MULTIPLE_FILES", "Можно отправить только один файл.");
        }
        MultipartFile file = multipart.getFile("file");
        if (file == null) {
            throw required();
        }
        if (files.size() != 1 || !multipart.getParameterMap().isEmpty()) {
            throw ApiException.invalid("Допускается только поле file.");
        }
        if (file.isEmpty()) {
            throw new ApiException(422, "EMPTY_FILE", "Файл не должен быть пустым.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw tooLarge();
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".java")) {
            throw new ApiException(422, "INVALID_EXTENSION", "Допускается только файл с расширением .java.");
        }
        byte[] bytes = file.getBytes();
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException ex) {
            throw new ApiException(422, "INVALID_ENCODING", "Файл должен быть в кодировке UTF-8.");
        }
        return new SubmissionFile(bytes, (int) text.lines().count(), HexFormat.of().formatHex(Secrets.sha256(bytes)));
    }

    static ApiException required() {
        return new ApiException(422, "FILE_REQUIRED", "Выберите файл.");
    }

    static ApiException tooLarge() {
        return new ApiException(413, "FILE_TOO_LARGE", "Размер файла превышает 1 МБ.");
    }
}
