package jrm.fx.ui.misc;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link FileResult}.
 * Tests file result property binding and value management.
 */
@DisplayName("FileResult Unit Tests")
class FileResultTest {

    @Test
    @DisplayName("Should create FileResult with file path")
    void shouldCreateFileResultWithFilePath() {
        Path testPath = Paths.get("/test/file.txt");
        FileResult result = new FileResult(testPath);
        
        assertThat(result.getFile()).isEqualTo(testPath);
        assertThat(result.getResult()).isNull();
    }

    @Test
    @DisplayName("Should set and get file path")
    void shouldSetAndGetFilePath() {
        Path initialPath = Paths.get("/initial/path.txt");
        Path newPath = Paths.get("/new/path.txt");
        FileResult result = new FileResult(initialPath);
        
        result.setFile(newPath);
        
        assertThat(result.getFile()).isEqualTo(newPath);
    }

    @Test
    @DisplayName("Should set and get result message")
    void shouldSetAndGetResultMessage() {
        Path testPath = Paths.get("/test/file.txt");
        FileResult result = new FileResult(testPath);
        
        result.setResult("Success");
        
        assertThat(result.getResult()).isEqualTo("Success");
    }

    @Test
    @DisplayName("Should have file property")
    void shouldHaveFileProperty() {
        Path testPath = Paths.get("/test/file.txt");
        FileResult result = new FileResult(testPath);
        
        assertThat(result.fileProperty()).isNotNull();
        assertThat(result.fileProperty().get()).isEqualTo(testPath);
    }

    @Test
    @DisplayName("Should have result property")
    void shouldHaveResultProperty() {
        Path testPath = Paths.get("/test/file.txt");
        FileResult result = new FileResult(testPath);
        
        assertThat(result.resultProperty()).isNotNull();
    }

    @Test
    @DisplayName("Should update result property value")
    void shouldUpdateResultPropertyValue() {
        Path testPath = Paths.get("/test/file.txt");
        FileResult result = new FileResult(testPath);
        
        result.resultProperty().set("Updated result");
        
        assertThat(result.getResult()).isEqualTo("Updated result");
    }

    @Test
    @DisplayName("Should update file property value")
    void shouldUpdateFilePropertyValue() {
        Path initialPath = Paths.get("/initial/path.txt");
        Path newPath = Paths.get("/new/path.txt");
        FileResult result = new FileResult(initialPath);
        
        result.fileProperty().set(newPath);
        
        assertThat(result.getFile()).isEqualTo(newPath);
    }

    @Test
    @DisplayName("Should handle null file path")
    void shouldHandleNullFilePath() {
        FileResult result = new FileResult(null);
        
        assertThat(result.getFile()).isNull();
    }

    @Test
    @DisplayName("Should handle empty result message")
    void shouldHandleEmptyResultMessage() {
        Path testPath = Paths.get("/test/file.txt");
        FileResult result = new FileResult(testPath);
        
        result.setResult("");
        
        assertThat(result.getResult()).isEmpty();
    }
}
