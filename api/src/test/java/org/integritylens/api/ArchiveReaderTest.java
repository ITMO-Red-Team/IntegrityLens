package org.integritylens.api;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.junit.jupiter.api.Test;

class ArchiveReaderTest {
    private final ArchiveReader reader = new ArchiveReader();

    @Test
    void normalizesEncodingAndPreservesRelativeNames() throws Exception {
        var files = javaFiles(5);
        files.put("folder/Пример.java", "// Привет\nclass Main {}\n".getBytes(Charset.forName("windows-1251")));
        files.put("__MACOSX/._Main.java", new byte[0]);
        files.put(".DS_Store", new byte[0]);
        var sources = reader.read(zip(files, Charset.forName("CP866")));
        assertThat(sources).hasSize(6);
        var source = sources.stream().filter(s -> s.name().startsWith("folder/")).findFirst().orElseThrow();
        assertThat(new String(source.bytes(), StandardCharsets.UTF_8)).isEqualTo("// Привет\nclass Main {}\n");
        assertThat(source.loc()).isEqualTo(2);
        assertThat(source.sha256()).isEqualTo(ArchiveReader.sha256(source.bytes()));
    }

    @Test
    void rejectsMalformedAndTruncatedArchives() throws Exception {
        assertCode("NOT_A_ZIP", () -> reader.read("hello".getBytes()));
        byte[] archive = zip(javaFiles(5));
        assertCode("NOT_A_ZIP", () -> reader.read(java.util.Arrays.copyOf(archive, archive.length / 2)));
    }

    @Test
    void rejectsTooFewAndTooManyWorks() throws Exception {
        assertCode("FILE_COUNT_OUT_OF_RANGE", () -> reader.read(zip(javaFiles(4))));
        assertCode("FILE_COUNT_OUT_OF_RANGE", () -> reader.read(zip(javaFiles(51))));
    }

    @Test
    void rejectsUnsupportedFiles() throws Exception {
        var files = javaFiles(5);
        files.put("readme.txt", new byte[0]);
        assertCode("UNSUPPORTED_FILES", () -> reader.read(zip(files)));
    }

    @Test
    void rejectsUnsafePathsAndNestedArchives() throws Exception {
        for (String name : new String[]{"../Main.java", "/Main.java", "C:\\Main.java", "a\\..\\Main.java", "nested.zip"}) {
            var files = javaFiles(5);
            files.put(name, new byte[0]);
            assertCode("UNSAFE_ARCHIVE", () -> reader.read(zip(files)));
        }
    }

    @Test
    void rejectsSymlinks() throws Exception {
        var out = new ByteArrayOutputStream();
        try (var zip = new ZipArchiveOutputStream(out)) {
            var entry = new ZipArchiveEntry("Main.java");
            entry.setUnixMode(0120777);
            zip.putArchiveEntry(entry);
            zip.write("/etc/passwd".getBytes());
            zip.closeArchiveEntry();
        }
        assertCode("UNSAFE_ARCHIVE", () -> reader.read(out.toByteArray()));
    }

    @Test
    void rejectsBombsEvenInIgnoredFilesAndExcessEntries() throws Exception {
        var files = javaFiles(5);
        files.put(".DS_Store", new byte[ArchiveReader.MAX_FILE + 1]);
        assertCode("UNSAFE_ARCHIVE", () -> reader.read(zip(files)));
        assertCode("UNSAFE_ARCHIVE", () -> reader.read(zip(javaFiles(201))));
        assertCode("ARCHIVE_TOO_LARGE", () -> reader.read(new byte[ArchiveReader.MAX_ARCHIVE + 1]));
    }

    @Test
    void verifiesCrcInsteadOfTrustingHeaders() throws Exception {
        byte[] zip = zip(javaFiles(5));
        for (int i = 0; i < zip.length - 20; i++) {
            if (zip[i] == 0x50 && zip[i + 1] == 0x4b && zip[i + 2] == 1 && zip[i + 3] == 2) {
                zip[i + 16] ^= 1;
                break;
            }
        }
        assertCode("NOT_A_ZIP", () -> reader.read(zip));
    }

    static Map<String, byte[]> javaFiles(int count) {
        var files = new LinkedHashMap<String, byte[]>();
        for (int i = 0; i < count; i++) files.put("Main" + i + ".java", ("class Main" + i + " {}\n").getBytes());
        return files;
    }

    static byte[] zip(Map<String, byte[]> files) throws Exception {
        return zip(files, StandardCharsets.UTF_8);
    }

    static byte[] zip(Map<String, byte[]> files, Charset charset) throws Exception {
        var out = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(out, charset)) {
            for (var entry : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }

    private void assertCode(String code, org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call).isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.code).isEqualTo(code));
    }
}
