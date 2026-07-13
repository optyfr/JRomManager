package jrm.fx.ui.misc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link WindowState}.
 * Tests window state data class and serialization.
 */
@DisplayName("WindowState Unit Tests")
class WindowStateTest {

    @Test
    @DisplayName("Should create WindowState with default values")
    void shouldCreateWindowStateWithDefaultValues() {
        WindowState state = new WindowState();
        
        assertThat(state.getX()).isEqualTo(0.0);
        assertThat(state.getY()).isEqualTo(0.0);
        assertThat(state.getW()).isEqualTo(0.0);
        assertThat(state.getH()).isEqualTo(0.0);
        assertThat(state.isM()).isFalse();
        assertThat(state.isF()).isFalse();
        assertThat(state.isI()).isFalse();
    }

    @Test
    @DisplayName("Should set and get X coordinate")
    void shouldSetAndGetXCoordinate() {
        WindowState state = new WindowState();
        state.setX(100.5);
        
        assertThat(state.getX()).isEqualTo(100.5);
    }

    @Test
    @DisplayName("Should set and get Y coordinate")
    void shouldSetAndGetYCoordinate() {
        WindowState state = new WindowState();
        state.setY(200.75);
        
        assertThat(state.getY()).isEqualTo(200.75);
    }

    @Test
    @DisplayName("Should set and get width")
    void shouldSetAndGetWidth() {
        WindowState state = new WindowState();
        state.setW(800.0);
        
        assertThat(state.getW()).isEqualTo(800.0);
    }

    @Test
    @DisplayName("Should set and get height")
    void shouldSetAndGetHeight() {
        WindowState state = new WindowState();
        state.setH(600.0);
        
        assertThat(state.getH()).isEqualTo(600.0);
    }

    @Test
    @DisplayName("Should set and get maximized state")
    void shouldSetAndGetMaximizedState() {
        WindowState state = new WindowState();
        state.setM(true);
        
        assertThat(state.isM()).isTrue();
    }

    @Test
    @DisplayName("Should set and get full-screen state")
    void shouldSetAndGetFullScreenState() {
        WindowState state = new WindowState();
        state.setF(true);
        
        assertThat(state.isF()).isTrue();
    }

    @Test
    @DisplayName("Should set and get iconified state")
    void shouldSetAndGetIconifiedState() {
        WindowState state = new WindowState();
        state.setI(true);
        
        assertThat(state.isI()).isTrue();
    }

    @Test
    @DisplayName("Should handle all properties together")
    void shouldHandleAllPropertiesTogether() {
        WindowState state = new WindowState();
        state.setX(150.0);
        state.setY(250.0);
        state.setW(1024.0);
        state.setH(768.0);
        state.setM(true);
        state.setF(false);
        state.setI(false);
        
        assertThat(state.getX()).isEqualTo(150.0);
        assertThat(state.getY()).isEqualTo(250.0);
        assertThat(state.getW()).isEqualTo(1024.0);
        assertThat(state.getH()).isEqualTo(768.0);
        assertThat(state.isM()).isTrue();
        assertThat(state.isF()).isFalse();
        assertThat(state.isI()).isFalse();
    }

    @Test
    @DisplayName("Should handle negative coordinates")
    void shouldHandleNegativeCoordinates() {
        WindowState state = new WindowState();
        state.setX(-100.0);
        state.setY(-50.0);
        
        assertThat(state.getX()).isEqualTo(-100.0);
        assertThat(state.getY()).isEqualTo(-50.0);
    }

    @Test
    @DisplayName("Should handle zero dimensions")
    void shouldHandleZeroDimensions() {
        WindowState state = new WindowState();
        state.setW(0.0);
        state.setH(0.0);
        
        assertThat(state.getW()).isEqualTo(0.0);
        assertThat(state.getH()).isEqualTo(0.0);
    }
}
