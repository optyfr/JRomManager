package jrm.fx.ui.misc;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.TransferMode;

public class DragNDrop
{
	private final String STYLE_ACCEPT;
	private final String STYLE_REJECT;
	
	private Control control;
	
	public DragNDrop(Control control)
	{
		if(control instanceof Cell)
		{
			STYLE_ACCEPT = "-fx-background-color: #DDFFDD;";
			STYLE_REJECT = "-fx-background-color: #FFDDDD;";
		}
		else
		{
			STYLE_ACCEPT = "-fx-control-inner-background: #DDFFDD;";
			STYLE_REJECT = "-fx-control-inner-background: #FFDDDD;";
		}
		this.control = control;
	}
	
	@FunctionalInterface
	public interface SetCallBack
	{
		public void call(String txt);
	}
	
	public interface SetFilesCallBack
	{
		public void call(List<File> files);
	}

	public void addFile(SetCallBack cb)
	{
		addFiltered(f -> Files.isRegularFile(f.toPath()), cb);
	}

	public void addAny(SetFilesCallBack cb)
	{
		addFiltered(f -> true, cb);
	}

	public void addDir(SetCallBack cb)
	{
		addFiltered(f -> Files.isDirectory(f.toPath()), cb);
	}
	
	public void addDirs(SetFilesCallBack cb)
	{
		addFiltered(f -> Files.isDirectory(f.toPath()), cb);
	}
	
	public void addFiltered(Predicate<File> filter, SetFilesCallBack cb)
	{
		control.setOnDragOver(event -> {
			if (event.getGestureSource() != control)
			{
				final var db = event.getDragboard();
				if(db.hasFiles() && db.getFiles().stream().filter(f -> !filter.test(f)).count()==0)
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
			if(db.hasFiles() && db.getFiles().stream().filter(f -> !filter.test(f)).count()==0)
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

	public void addFiltered(Predicate<File> filter, SetCallBack cb)
	{
		control.setOnDragOver(event -> {
			if (event.getGestureSource() != control)
			{
				final var db = event.getDragboard();
				if (db.hasFiles() && db.getFiles().size() == 1 && filter.test(db.getFiles().get(0)))
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
			if (db.hasFiles() && db.getFiles().size() == 1 && filter.test(db.getFiles().get(0)))
			{
				if (control instanceof TextInputControl tic)
					tic.setText(db.getFiles().get(0).toString());
				cb.call(db.getFiles().get(0).toString());
				success = true;
			}
			event.setDropCompleted(success);
			event.consume();
		});
	}
}
