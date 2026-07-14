package jrm.misc;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link URIUtils} URI/path resolution and file reading helpers.
 */
@DisplayName("URIUtils tests")
class URIUtilsTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("getPath(String)")
    class GetPathFromString {

        @Test
        @DisplayName("should resolve plain file path string")
        void shouldResolvePlainFilePathString() {
            final var file = tempDir.resolve("file.txt").toFile();
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                throw new AssertionError(e);
            }

            final var path = URIUtils.getPath(file.getAbsolutePath());

            assertThat(path).exists();
            assertThat(path.toAbsolutePath()).hasToString(file.getAbsolutePath());
        }

        @Test
        @DisplayName("should resolve file URI string")
        void shouldResolveFileUriString() {
            final var file = tempDir.resolve("uri.txt").toFile();
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                throw new AssertionError(e);
            }

            final var path = URIUtils.getPath(file.toURI().toString());

            assertThat(path).exists();
        }
    }

    @Nested
    @DisplayName("getPath(URI)")
    class GetPathFromUri {

        @Test
        @DisplayName("should resolve file URI into existing path")
        void shouldResolveFileUriIntoExistingPath() {
            final var file = tempDir.resolve("from-uri.txt").toFile();
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                throw new AssertionError(e);
            }

            final var path = URIUtils.getPath(file.toURI());

            assertThat(path).exists();
            assertThat(path.toFile()).isEqualTo(file);
        }
    }

    @Nested
    @DisplayName("URIExists(String)")
    class UriExistsString {

        @Test
        @DisplayName("should return true when file exists")
        void shouldReturnTrueWhenFileExists() {
            final var file = tempDir.resolve("exists.txt").toFile();
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                throw new AssertionError(e);
            }

            assertThat(URIUtils.URIExists(file.getAbsolutePath())).isTrue();
        }

        @Test
        @DisplayName("should return false when file does not exist")
        void shouldReturnFalseWhenFileDoesNotExist() {
            final var missing = tempDir.resolve("missing.txt").toFile();

            assertThat(URIUtils.URIExists(missing.getAbsolutePath())).isFalse();
        }

        @Test
        @DisplayName("should return true for file URI of existing file")
        void shouldReturnTrueForFileUriOfExistingFile() {
            final var file = tempDir.resolve("uri-exists.txt").toFile();
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                throw new AssertionError(e);
            }

            assertThat(URIUtils.URIExists(file.toURI().toString())).isTrue();
        }
    }

    @Nested
    @DisplayName("URIExists(URI)")
    class UriExistsUri {

        @Test
        @DisplayName("should return true when URI points to existing file")
        void shouldReturnTrueWhenUriPointsToExistingFile() {
            final var file = tempDir.resolve("uri-obj.txt").toFile();
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                throw new AssertionError(e);
            }

            assertThat(URIUtils.URIExists(file.toURI())).isTrue();
        }

        @Test
        @DisplayName("should return false when URI points to non-existent file")
        void shouldReturnFalseWhenUriPointsToNonExistentFile() {
            final URI missingUri = tempDir.resolve("nope.txt").toUri();

            assertThat(URIUtils.URIExists(missingUri)).isFalse();
        }
    }

    @Nested
    @DisplayName("readString(String)")
    class ReadString {

        @Test
        @DisplayName("should read file content joined without separators")
        void shouldReadFileContentJoinedWithoutSeparators() throws IOException {
            final var file = tempDir.resolve("content.txt").toFile();
            Files.writeString(file.toPath(), "line1\nline2\nline3");

            final var content = URIUtils.readString(file.getAbsolutePath());

            assertThat(content).isEqualTo("line1line2line3");
        }

        @Test
        @DisplayName("should read single line file")
        void shouldReadSingleLineFile() throws IOException {
            final var file = tempDir.resolve("single.txt").toFile();
            Files.writeString(file.toPath(), "only-one-line");

            final var content = URIUtils.readString(file.getAbsolutePath());

            assertThat(content).isEqualTo("only-one-line");
        }

        @Test
        @DisplayName("should read empty file as empty string")
        void shouldReadEmptyFileAsEmptyString() throws IOException {
            final var file = tempDir.resolve("empty.txt").toFile();
            Files.createFile(file.toPath());

            final var content = URIUtils.readString(file.getAbsolutePath());

            assertThat(content).isEmpty();
        }
    }
}
