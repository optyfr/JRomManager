package jrm.fx.ui.misc;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;

import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.TransferMode;

public class DragNDrop
{
	private static final String STYLE_ACCEPT = "-fx-control-inner-background: #DDFFDD;";
	private static final String STYLE_REJECT = "-fx-control-inner-background: #FFDDDD;";
	
	private Control control;
	
	public DragNDrop(Control control)
	{
		this.control = control;
	}
	
	/**
	 * The Interface SetCallBack.
	 */
	@FunctionalInterface
	public interface SetCallBack
	{

		/**
		 * Call.
		 *
		 * @param txt
		 *            the txt
		 */
		public void call(String txt);
	}
	
	public interface SetFilesCallBack
	{
		public void call(List<File> files);
	}

	public void addFile(SetCallBack cb)
	{
		control.setOnDragOver(event -> {
			if (event.getGestureSource() != control)
			{
				final var db = event.getDragboard();
				if(db.hasFiles() && db.getFiles().size()==1 && Files.isRegularFile(db.getFiles().get(0).toPath()))
				{
					event.acceptTransferModes(TransferMode.COPY);
					control.setStyle(STYLE_ACCEPT);
					return;
				}
			}
			control.setStyle(STYLE_REJECT);
			event.consume();
		});
		
		control.setOnDragExited(event -> control.setStyle(""));

		control.setOnDragDropped(event -> {
			final var db = event.getDragboard();
			var success = false;
			if (db.hasFiles() && db.getFiles().size()==1 && Files.isRegularFile(db.getFiles().get(0).toPath()))
			{
				if(control instanceof TextInputControl tic)
					tic.setText(db.getFiles().get(0).toString());
				cb.call(db.getFiles().get(0).toString());
				success = true;
			}
			event.setDropCompleted(success);
			event.consume();
		});

	}

	public void addAny(SetFilesCallBack cb)
	{
		control.setOnDragOver(event -> {
			if (event.getGestureSource() != control)
			{
				final var db = event.getDragboard();
				if(db.hasFiles())
				{
					event.acceptTransferModes(TransferMode.COPY);
					control.setStyle(STYLE_ACCEPT);
					return;
				}
			}
			control.setStyle(STYLE_REJECT);
			event.consume();
		});
		
		control.setOnDragExited(event -> control.setStyle(""));

		control.setOnDragDropped(event -> {
			final var db = event.getDragboard();
			var success = false;
			if (db.hasFiles())
			{
				cb.call(db.getFiles());
				success = true;
			}
			event.setDropCompleted(success);
			event.consume();
		});

	}

	public void addDir(SetCallBack cb)
	{
		control.setOnDragOver(event -> {
			if (event.getGestureSource() != control)
			{
				final var db = event.getDragboard();
				if(db.hasFiles() && db.getFiles().size()==1 && Files.isDirectory(db.getFiles().get(0).toPath()))
				{
					event.acceptTransferModes(TransferMode.COPY);
					control.setStyle(STYLE_ACCEPT);
					return;
				}
			}
			control.setStyle(STYLE_REJECT);
			event.consume();
		});
		
		control.setOnDragExited(event -> control.setStyle(""));

		control.setOnDragDropped(event -> {
			final var db = event.getDragboard();
			var success = false;
			if (db.hasFiles() && db.getFiles().size()==1 && Files.isDirectory(db.getFiles().get(0).toPath()))
			{
				if(control instanceof TextInputControl tic)
					tic.setText(db.getFiles().get(0).toString());
				cb.call(db.getFiles().get(0).toString());
				success = true;
			}
			event.setDropCompleted(success);
			event.consume();
		});

	}
	
	public void addDirs(SetFilesCallBack cb)
	{
		control.setOnDragOver(event -> {
			if (event.getGestureSource() != control)
			{
				final var db = event.getDragboard();
				if(db.hasFiles() && db.getFiles().stream().filter(f -> !Files.isDirectory(f.toPath())).count()==0)
				{
					event.acceptTransferModes(TransferMode.COPY);
					control.setStyle(STYLE_ACCEPT);
					return;
				}
			}
			control.setStyle(STYLE_REJECT);
			event.consume();
		});

		control.setOnDragExited(event -> control.setStyle(""));

		control.setOnDragDropped(event -> {
			final var db = event.getDragboard();
			var success = false;
			if(db.hasFiles() && db.getFiles().stream().filter(f -> !Files.isDirectory(f.toPath())).count()==0)
			{
				if(control instanceof ListView)
				{
					@SuppressWarnings("unchecked")
					final var lv = (ListView<File>)control;
					final var set = new LinkedHashSet<>(lv.getItems());
					for(final var f : db.getFiles())
						if(!set.contains(f))
							lv.getItems().add(f);
					cb.call(lv.getItems());
				}
				else
					cb.call(db.getFiles());
				success = true;
			}
			event.setDropCompleted(success);
			event.consume();
		});
	}
	
}
