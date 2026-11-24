package org.integritylens.api;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionFileTest {
    @Test
    void acceptsExactlyOneMegabyte() throws Exception {
        byte[] bytes = new byte[1_000_000];
        java.util.Arrays.fill(bytes, (byte) 'a');
        var file = SubmissionFile.read(request("Main.java", bytes));

        assertArrayEquals(bytes, file.bytes());
        assertEquals(1, file.lines());
        assertTrue(file.sha256().matches("[0-9a-f]{64}"));
        var tooLarge = assertThrows(ApiException.class,
                () -> SubmissionFile.read(request("Main.java", new byte[1_000_001])));
        assertEquals(413, tooLarge.status);
        assertEquals("FILE_TOO_LARGE", tooLarge.code);
    }

    @Test
    void preservesUtf8AndNewlines() throws Exception {
        byte[] bytes = "// Пример\r\nclass Main {}\n".getBytes(StandardCharsets.UTF_8);
        var file = SubmissionFile.read(request("../../Main.java", bytes));

        assertArrayEquals(bytes, file.bytes());
        assertEquals(2, file.lines());
    }

    @Test
    void rejectsMultipleFilesAndInvalidUtf8() {
        var multiple = request("Main.java", new byte[]{1});
        multiple.addFile(new MockMultipartFile("file", "Another.java", "text/plain", new byte[]{1}));
        assertEquals("MULTIPLE_FILES", assertThrows(ApiException.class, () -> SubmissionFile.read(multiple)).code);
        var invalid = request("Main.java", new byte[]{(byte) 0xc3, 0x28});
        assertEquals("INVALID_ENCODING", assertThrows(ApiException.class, () -> SubmissionFile.read(invalid)).code);
    }

    private static MockMultipartHttpServletRequest request(String filename, byte[] bytes) {
        var request = new MockMultipartHttpServletRequest();
        request.addFile(new MockMultipartFile("file", filename, "application/octet-stream", bytes));
        return request;
    }
}
