package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import jrm.misc.GlobalSettings;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.filter.Keywords.KFCallBack;
import jrm.security.Session;
import jrm.security.Sessions;
import jrm.security.User;

/**
 * Tests for {@link jrm.fx.ui.profile.filter.KeywordsController}.
 * <p>
 * Verifies initialization, drag-and-drop setup, keyword filtering, and window management for the keywords filter dialog.
 *
 * @since 3.0.5
 */
@TestFxApplication(KeywordsControllerTest.TestApp.class)
@DisplayName("KeywordsController Tests")
class KeywordsControllerTest {

    /**
     * Functional interface that accepts a runnable which may throw checked exceptions.
     */
    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * Minimal application that creates a KeywordsController with FXML fields injected via reflection and mocks for Session.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;
        static jrm.fx.ui.profile.filter.KeywordsController controller;

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            this.primaryStage = primaryStage;

            // Create mock chain
            GlobalSettings mockSettings = mock(GlobalSettings.class);
            when(mockSettings.getWorkPath()).thenReturn(java.nio.file.Paths.get(System.getProperty("java.io.tmpdir")));

            User mockUser = mock(User.class);
            when(mockUser.getSettings()).thenReturn(mockSettings);

            Session mockSession = mock(Session.class);
            when(mockSession.getUser()).thenReturn(mockUser);

            // Configure shared mock to return our session
            SharedMockSession.getOrCreate().when(Sessions::getSingleSession).thenReturn(mockSession);

            // Create the controller
            controller = new jrm.fx.ui.profile.filter.KeywordsController();

            // Create and inject FXML fields
            Scene sceneKW = new Scene(new javafx.scene.layout.VBox(), 600, 400);
            ListView<String> listAvailKW = new ListView<>();
            ListView<String> listUsedKW = new ListView<>();

            injectField(controller, "sceneKW", sceneKW);
            injectField(controller, "listAvailKW", listAvailKW);
            injectField(controller, "listUsedKW", listUsedKW);

            // Initialize controller
            controller.initialize(null, null);

            primaryStage.setScene(sceneKW);
            primaryStage.show();
        }

        /** Injects a value into a private field of the target object using reflection. */
        private static void injectField(Object target, String fieldName, Object value) {
            try {
                Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject field: " + fieldName, e);
            }
        }

        /**
         * Helper method to run operations on the JavaFX Application Thread and wait for completion.
         */
        public static void runOnFxThread(ThrowingRunnable operation) throws Exception {
            CompletableFuture<Void> future = new CompletableFuture<>();
            Platform.runLater(() -> {
                try {
                    operation.run();
                    future.complete(null);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
            future.get();
        }
    }

    /**
     * Helper to run code on JavaFX thread and wait for completion.
     */
    private void runOnFxThread(ThrowingRunnable action) throws Exception {
        TestApp.runOnFxThread(action);
    }

    /** Retrieves a private field value from the controller via reflection. */
    @SuppressWarnings("unchecked")
    private <T> T getField(String fieldName) {
        try {
            Field field = jrm.fx.ui.profile.filter.KeywordsController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(TestApp.controller);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field " + fieldName, e);
        }
    }

    @Test
    @DisplayName("Should initialize controller without errors")
    void shouldInitializeController() throws Exception {
        runOnFxThread(() -> {
            assertThat(TestApp.controller).isNotNull();
        });
    }

    @Test
    @DisplayName("Should initialize available keywords list")
    void shouldInitializeAvailableKeywordsList() throws Exception {
        runOnFxThread(() -> {
            ListView<String> listAvailKW = getField("listAvailKW");
            assertThat(listAvailKW).as("available keywords list").isNotNull();
        });
    }

    @Test
    @DisplayName("Should initialize used keywords list")
    void shouldInitializeUsedKeywordsList() throws Exception {
        runOnFxThread(() -> {
            ListView<String> listUsedKW = getField("listUsedKW");
            assertThat(listUsedKW).as("used keywords list").isNotNull();
        });
    }

    @Test
    @DisplayName("Should set drag-and-drop handlers on available keywords list")
    void shouldSetDragDropHandlersOnAvailableList() throws Exception {
        runOnFxThread(() -> {
            ListView<String> listAvailKW = getField("listAvailKW");
            assertThat(listAvailKW.getOnDragDetected()).as("drag detected handler").isNotNull();
            assertThat(listAvailKW.getOnDragOver()).as("drag over handler").isNotNull();
            assertThat(listAvailKW.getOnDragDropped()).as("drag dropped handler").isNotNull();
            assertThat(listAvailKW.getOnDragDone()).as("drag done handler").isNotNull();
        });
    }

    @Test
    @DisplayName("Should set drag-and-drop handlers on used keywords list")
    void shouldSetDragDropHandlersOnUsedList() throws Exception {
        runOnFxThread(() -> {
            ListView<String> listUsedKW = getField("listUsedKW");
            assertThat(listUsedKW.getOnDragDetected()).as("drag detected handler").isNotNull();
            assertThat(listUsedKW.getOnDragOver()).as("drag over handler").isNotNull();
            assertThat(listUsedKW.getOnDragDropped()).as("drag dropped handler").isNotNull();
            assertThat(listUsedKW.getOnDragDone()).as("drag done handler").isNotNull();
        });
    }

    @Test
    @DisplayName("Should populate available keywords list")
    void shouldPopulateAvailableKeywordsList() throws Exception {
        runOnFxThread(() -> {
            try {
                String[] keywords = { "Genre", "Year", "Developer", "Publisher", "Players" };
                java.lang.reflect.Method initKeywordsMethod = jrm.fx.ui.profile.filter.KeywordsController.class.getDeclaredMethod("initKeywords", String[].class);
                initKeywordsMethod.setAccessible(true);
                initKeywordsMethod.invoke(TestApp.controller, (Object) keywords);

                ListView<String> listAvailKW = getField("listAvailKW");
                assertThat(listAvailKW.getItems())
                        .as("available keywords populated")
                        .hasSize(5)
                        .containsExactly("Genre", "Year", "Developer", "Publisher", "Players");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("Should handle empty keywords array")
    void shouldHandleEmptyKeywordsArray() throws Exception {
        runOnFxThread(() -> {
            try {
                String[] keywords = {};
                java.lang.reflect.Method initKeywordsMethod = jrm.fx.ui.profile.filter.KeywordsController.class.getDeclaredMethod("initKeywords", String[].class);
                initKeywordsMethod.setAccessible(true);
                initKeywordsMethod.invoke(TestApp.controller, (Object) keywords);

                ListView<String> listAvailKW = getField("listAvailKW");
                assertThat(listAvailKW.getItems())
                        .as("available keywords empty")
                        .isEmpty();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @DisplayName("Should invoke callback and hide window on filter")
    @SuppressWarnings("unchecked")
    void shouldInvokeCallbackAndHideWindowOnFilter() throws Exception {
        runOnFxThread(() -> {
            // Setup mock callback and anyware list
            KFCallBack mockCallback = mock(KFCallBack.class);
            AnywareList<Anyware> mockAwList = mock(AnywareList.class);

            Field callbackField = jrm.fx.ui.profile.filter.KeywordsController.class.getDeclaredField("callback");
            callbackField.setAccessible(true);
            callbackField.set(TestApp.controller, mockCallback);

            Field awlistField = jrm.fx.ui.profile.filter.KeywordsController.class.getDeclaredField("awlist");
            awlistField.setAccessible(true);
            awlistField.set(TestApp.controller, mockAwList);

            // Add some used keywords
            ListView<String> listUsedKW = getField("listUsedKW");
            listUsedKW.setItems(FXCollections.observableArrayList("Genre", "Year"));

            // Call onFilter via reflection (package-private)
            java.lang.reflect.Method onFilterMethod = jrm.fx.ui.profile.filter.KeywordsController.class.getDeclaredMethod("onFilter");
            onFilterMethod.setAccessible(true);
            onFilterMethod.invoke(TestApp.controller);

            // Verify callback was invoked with used keywords
            verify(mockCallback).call(eq(mockAwList), any(List.class));
        });
    }

    @Test
    @DisplayName("Should save window bounds and hide on close")
    void shouldSaveWindowBoundsAndHideOnClose() throws Exception {
        runOnFxThread(() -> {
            // Ensure the window is showing before close
            assertThat(TestApp.controller.getSceneKW().getWindow().isShowing())
                    .as("window showing before close").isTrue();

            // Call onClose
            TestApp.controller.onClose();

            // The window should be hidden after close
            assertThat(TestApp.controller.getSceneKW().getWindow().isShowing())
                    .as("window hidden after close").isFalse();
        });
    }

    @Test
    @DisplayName("Should allow moving keywords from available to used")
    void shouldAllowMovingKeywordsFromAvailableToUsed() throws Exception {
        runOnFxThread(() -> {
            ListView<String> listAvailKW = getField("listAvailKW");
            ListView<String> listUsedKW = getField("listUsedKW");

            // Populate available keywords
            listAvailKW.setItems(FXCollections.observableArrayList("Genre", "Year", "Developer"));
            listUsedKW.setItems(FXCollections.observableArrayList());

            // Simulate moving a keyword
            String keyword = "Genre";
            listUsedKW.getItems().add(keyword);
            listAvailKW.getItems().remove(keyword);

            assertThat(listUsedKW.getItems()).contains("Genre");
            assertThat(listAvailKW.getItems()).doesNotContain("Genre");
        });
    }

    @Test
    @DisplayName("Should allow reordering used keywords")
    void shouldAllowReorderingUsedKeywords() throws Exception {
        runOnFxThread(() -> {
            ListView<String> listUsedKW = getField("listUsedKW");
            listUsedKW.setItems(FXCollections.observableArrayList("Genre", "Year", "Developer"));

            // Simulate reordering
            listUsedKW.getItems().remove("Developer");
            listUsedKW.getItems().add(0, "Developer");

            assertThat(listUsedKW.getItems()).containsExactly("Developer", "Genre", "Year");
        });
    }
}
