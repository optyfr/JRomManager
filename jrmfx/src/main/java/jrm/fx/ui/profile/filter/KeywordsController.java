package jrm.fx.ui.profile.filter;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import jrm.fx.ui.misc.Settings;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.filter.Keywords.KFCallBack;
import jrm.security.Session;
import jrm.security.Sessions;

public class KeywordsController implements Initializable {

	@FXML Scene sceneKW;
	@FXML ListView<String> listAvailKW;
	@FXML ListView<String> listUsedKW;
	
	private Session session;
	KFCallBack callback;
	AnywareList<? extends Anyware> awlist;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		session = Sessions.getSingleSession();

        EventHandler<MouseEvent> dragDetected = event -> {
			@SuppressWarnings("unchecked")
			ListView<String> list = (ListView<String>) event.getSource();
		    Dragboard db = list.startDragAndDrop(TransferMode.ANY);

		    ClipboardContent content = new ClipboardContent();
		    content.putString(list.getSelectionModel().getSelectedItem());
		    db.setContent(content);

		    event.consume();
		};
		
        EventHandler<DragEvent> dragOver = event -> {
		    if (event.getGestureSource() != event.getTarget() && event.getDragboard().hasString()) {
		        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
		    }

		    event.consume();
		};
        
        EventHandler<DragEvent> dragDropped = event -> {
		    @SuppressWarnings("unchecked")
			ListView<String> list = (ListView<String>) event.getGestureTarget();

		    Dragboard db = event.getDragboard();
		    boolean success = false;
		    if (db.hasString()) {
		        list.getItems().add(db.getString());
		        success = true;
		    }
		    event.setDropCompleted(success);
		    event.consume();
		};
		
        EventHandler<DragEvent> dragDone = event -> {
		    if (event.getTransferMode() == TransferMode.MOVE) {
		        @SuppressWarnings("unchecked")
				ListView<String> list = (ListView<String>) event.getGestureSource();
		        list.getItems().remove(event.getDragboard().getString());
		    }
		    event.consume();
		};		

        listAvailKW.setOnDragDetected(dragDetected);
        listAvailKW.setOnDragOver(dragOver);
        listAvailKW.setOnDragDropped(dragDropped);
        listAvailKW.setOnDragDone(dragDone);

        listUsedKW.setOnDragDetected(dragDetected);
        listUsedKW.setOnDragOver(dragOver);
        listUsedKW.setOnDragDropped(dragDropped);
        listUsedKW.setOnDragDone(dragDone);

	}

	@FXML public void onClose()
	{
		final var stage = (Stage)sceneKW.getWindow();
		session.getUser().getSettings().setProperty("Keywords.Bounds", Settings.toJson(stage));
		stage.hide();
	}
	
	@FXML public void onFilter()
	{
		callback.call(awlist, listUsedKW.getItems());
		sceneKW.getWindow().hide();
	}
	
	void initKeywords(String[] keywords)
	{
		listAvailKW.setItems(FXCollections.observableArrayList(keywords));
	}
	
	private final ObjectProperty<ListCell<String>> dragSource = new SimpleObjectProperty<>();
	
}
