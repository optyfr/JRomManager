package jrm.fx.ui.controls;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jrm.fx.ui.JRMScene;
import jrm.fx.ui.MainFrame;
import lombok.experimental.UtilityClass;

/**
 * Utility class for displaying modal dialogs (error, warning, confirmation).
 * <p>
 * All dialogs are styled with the application icon and the current {@link JRMScene} style sheet.
 *
 * @since 2.5
 */
public @UtilityClass class Dialogs {
    /** The application icon resource path. */
    private static final String ICO = "/jrm/resicons/rom.png";

    /**
     * Shows an error dialog with the exception message and stack trace.
     *
     * @param e the exception to display
     */
    public static void showError(Throwable e) {
        final var alert = new Alert(AlertType.ERROR);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(MainFrame.getIcon(ICO));
        JRMScene.applySheet(alert.getDialogPane().getScene());
        alert.setTitle("Error");
        alert.setHeaderText(e.getMessage());

        final var dialogPaneContent = new VBox();

        final var label = new Label("Stack Trace:");

        final var stackTrace = getStackTrace(e);
        TextArea textArea = new TextArea();
        textArea.setText(stackTrace);

        dialogPaneContent.getChildren().addAll(label, textArea);

        alert.getDialogPane().setContent(dialogPaneContent);
        alert.showAndWait();
    }

    /**
     * Shows a warning dialog with the given message.
     *
     * @param message the message to display
     */
    public static void showAlert(String message) {
        Alert alert = new Alert(AlertType.WARNING);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(MainFrame.getIcon(ICO));
        JRMScene.applySheet(alert.getDialogPane().getScene());
        alert.setTitle("Warning");

        // Header Text: null
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog with the given title and message.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param buttons the button types to show, or {@code null} for defaults
     * @return the selected button type
     */
    public static Optional<ButtonType> showConfirmation(String title, String message, ButtonType... buttons) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(MainFrame.getIcon(ICO));
        JRMScene.applySheet(alert.getDialogPane().getScene());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (buttons != null && buttons.length > 0) {
            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(buttons);
        }
        return alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog with the given title and custom node content.
     *
     * @param title   the dialog title
     * @param message the custom node to display
     * @param buttons the button types to show, or {@code null} for defaults
     * @return the selected button type
     */
    public static Optional<ButtonType> showConfirmation(String title, Node message, ButtonType... buttons) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(MainFrame.getIcon(ICO));
        JRMScene.applySheet(alert.getDialogPane().getScene());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(message);
        if (buttons != null && buttons.length > 0) {
            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(buttons);
        }
        return alert.showAndWait();
    }

    /**
     * Extracts the stack trace from an exception as a string.
     *
     * @param e the exception
     * @return the formatted stack trace
     */
    private static String getStackTrace(Throwable e) {
        final var sw = new StringWriter();
        final var pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
