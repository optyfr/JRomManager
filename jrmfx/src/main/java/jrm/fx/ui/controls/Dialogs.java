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
import jrm.fx.ui.MainFrame;
import lombok.experimental.UtilityClass;

public @UtilityClass class Dialogs
{
	private static final String ICO = "/jrm/resicons/rom.png";

	public static void showError(Throwable e)
	{
		final var alert = new Alert(AlertType.ERROR);
		alert.getDialogPane().getScene().getStylesheets().add(alert.getClass().getResource("/jrm/fx/ui/MainFrame.css").toExternalForm());
		((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(MainFrame.getIcon(ICO));
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

	public static void showAlert(String message)
	{
		Alert alert = new Alert(AlertType.WARNING);
		alert.getDialogPane().getScene().getStylesheets().add(alert.getClass().getResource("/jrm/fx/ui/MainFrame.css").toExternalForm());
		((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(MainFrame.getIcon(ICO));
		alert.setTitle("Warning");

		// Header Text: null
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}

	public static Optional<ButtonType> showConfirmation(String title, String message, ButtonType... buttons)
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.getDialogPane().getScene().getStylesheets().add(alert.getClass().getResource("/jrm/fx/ui/MainFrame.css").toExternalForm());
		((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(MainFrame.getIcon(ICO));
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		if (buttons != null && buttons.length > 0)
		{
			alert.getButtonTypes().clear();
			alert.getButtonTypes().addAll(buttons);
		}
		return alert.showAndWait();
	}
	
	public static Optional<ButtonType> showConfirmation(String title, Node message, ButtonType... buttons)
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.getDialogPane().getScene().getStylesheets().add(alert.getClass().getResource("/jrm/fx/ui/MainFrame.css").toExternalForm());
		((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().add(MainFrame.getIcon(ICO));
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.getDialogPane().setContent(message);
		if (buttons != null && buttons.length > 0)
		{
			alert.getButtonTypes().clear();
			alert.getButtonTypes().addAll(buttons);
		}
		return alert.showAndWait();
	}
	
	private static String getStackTrace(Throwable e)
	{
		final var sw = new StringWriter();
		final var pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
}
