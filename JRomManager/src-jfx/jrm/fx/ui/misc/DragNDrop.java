package jrm.fx.ui.misc;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class DragNDrop
{
	private Node node;
	private Background bg;
	
	public DragNDrop(Node node)
	{
		this.node = node;
		Region region = (Region) node.lookup(".content");
		this.bg = region.getBackground();
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

	public void addDragDropFile(SetCallBack cb)
	{
		node.setOnDragEntered(event -> {
			if (event.getGestureSource() != node && event.getDragboard().hasFiles())
			{
				event.acceptTransferModes(TransferMode.COPY);
				Region region = (Region) node.lookup(".content");
				region.setBackground(new Background(new BackgroundFill(Color.valueOf("#DDFFDD"), CornerRadii.EMPTY, Insets.EMPTY)));
			}
			else
			{
				Region region = (Region) node.lookup(".content");
				region.setBackground(new Background(new BackgroundFill(Color.valueOf("#FFDDDD"), CornerRadii.EMPTY, Insets.EMPTY)));
			}
			event.consume();
		});
		
		node.setOnDragExited(event -> {
			Region region = (Region) node.lookup(".content");
			region.setBackground(bg);
		});

		node.setOnDragDropped(event -> {
			Dragboard db = event.getDragboard();
			boolean success = false;
			if (db.hasFiles())
			{
				cb.call(db.getFiles().get(0).toString());
				success = true;
			}
			event.setDropCompleted(success);
			event.consume();
		});

	}
}
