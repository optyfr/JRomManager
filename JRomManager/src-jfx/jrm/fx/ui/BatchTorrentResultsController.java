package jrm.fx.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import jrm.batch.TrntChkReport.Child;
import jrm.batch.TrntChkReport.Status;
import lombok.Getter;

public class BatchTorrentResultsController implements Initializable
{
	@FXML @Getter private TreeView<Child> treeview;
	
	private Font font = new Font(10);
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		treeview.setCellFactory(param -> new TreeCell<>()
		{
			@Override
			protected void updateItem(Child item, boolean empty)
			{
				super.updateItem(item, empty);
				setFont(font);
				if (empty)
					setText("");
				else
				{
					setText(item.getData().getTitle() + " (" + item.getData().getLength() + ") [" + item.getData().getStatus() + "]");
					final Image icon; 
					if (!getTreeItem().isLeaf())
						icon = MainFrame.getIcon("/jrm/resicons/folder" + (getTreeItem().isExpanded() ? "_open" : "_closed") + statusColor(item.getData().getStatus()) + ".png");
					else
						icon = MainFrame.getIcon("/jrm/resicons/icons/bullet" + statusColor(item.getData().getStatus()) + ".png");
					setGraphic(new ImageView(icon));
				}

			}
		});
	}

	/**
	 * @param icon
	 * @param status
	 * @return
	 */
	protected String statusColor(final Status status)
	{
		switch (status)
		{
			case OK:
				return "_green";
			case MISSING:
				return "_red";
			case SHA1:
				return "_purple";
			case SIZE:
				return "_blue";
			case SKIPPED:
				return "_orange";
			case UNKNOWN:
				return "_gray";
			default:
				return "";
		}
	}

	@FXML private void onOK(ActionEvent e)
	{
		treeview.getScene().getWindow().hide();
	}
}
