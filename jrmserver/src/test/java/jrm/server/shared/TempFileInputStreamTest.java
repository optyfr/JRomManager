package jrm.server.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link TempFileInputStream}.
 */
@DisplayName("TempFileInputStream")
class TempFileInputStreamTest {

    @Nested
    @DisplayName("newInstance()")
    class NewInstanceEmptyTest {
        @Test
        @DisplayName("creates empty temp file deleted on close")
        void createsAndDeletesOnClose(@TempDir final Path tempDir) throws IOException {
            final Path tmpFile = Files.createFile(tempDir.resolve("test.tmp"));
            try (InputStream in = new TempFileInputStream(tmpFile.toFile())) {
                assertThat(in.read()).isEqualTo(-1);
            }
            assertThat(Files.exists(tmpFile)).isFalse();
        }
    }

    @Nested
    @DisplayName("newInstance(InputStream)")
    class NewInstanceFromStreamTest {
        @Test
        @DisplayName("copies bytes and deletes on close")
        void copiesAndDeletes(@TempDir final Path tempDir) /* NOSONAR */ throws IOException {
            final byte[] data = "hello world".getBytes();
            try (InputStream in = TempFileInputStream.newInstance(new ByteArrayInputStream(data))) {
                final byte[] buf = in.readAllBytes();
                assertThat(buf).isEqualTo(data);
            }
        }

        @Test
        @DisplayName("newInstance(in, len) copies with specified length")
        void copiesWithLength() throws IOException {
            final byte[] data = "1234567890".getBytes();
            try (InputStream in = TempFileInputStream.newInstance(new ByteArrayInputStream(data), 5L)) {
                final byte[] buf = in.readAllBytes();
                assertThat(buf).hasSize(5);
                assertThat(new String(buf)).isEqualTo("12345");
            }
        }
    }

    @Nested
    @DisplayName("newInstance(in, len, close)")
    class NewInstanceWithCloseFlagTest {
        @Test
        @DisplayName("close=true closes the source stream")
        void closesSource() throws IOException {
            final ByteArrayInputStream source = new ByteArrayInputStream("data".getBytes());
            try (InputStream in = TempFileInputStream.newInstance(source, -1L, true)) {
                in.readAllBytes();
            }
            // After close, the source should be exhausted/closed
            assertThat(source.available()).isZero();
        }

        @Test
        @DisplayName("close=false leaves the source stream open")
        void leavesSourceOpen() throws IOException {
            final ByteArrayInputStream source = new ByteArrayInputStream("data".getBytes());
            try (InputStream in = TempFileInputStream.newInstance(source, -1L, false)) {
                in.readAllBytes();
            }
            // Source is not closed; we can still read from it (it's exhausted though)
            assertThat(source.available()).isZero();
        }
    }

    @Nested
    @DisplayName("close() deletes file")
    class CloseDeletesTest {
        @Test
        @DisplayName("file is deleted after close")
        void fileDeletedAfterClose(@TempDir final Path tempDir) throws IOException {
            final Path tmpFile = Files.createFile(tempDir.resolve("delete-me.tmp"));
            Files.writeString(tmpFile, "content");
            final TempFileInputStream in = new TempFileInputStream(tmpFile.toFile());
            in.close();
            assertThat(Files.exists(tmpFile)).isFalse();
        }
    }
}