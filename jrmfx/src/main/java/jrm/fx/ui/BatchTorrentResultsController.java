package jrm.fx.ui;

import java.net.URL;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import jrm.batch.TrntChkReport;
import jrm.batch.TrntChkReport.Child;
import jrm.batch.TrntChkReport.Status;
import jrm.profile.report.FilterOptions;
import lombok.Getter;

public class BatchTorrentResultsController implements Initializable
{
	@FXML @Getter private TreeView<Child> treeview;
	@FXML private ContextMenu menu;
	@FXML private MenuItem openAllNodes;
	@FXML private MenuItem closeAllNodes;
	@FXML private CheckMenuItem showok;
	@FXML private CheckMenuItem hidemissing;
	
	private Font font = new Font(10);
	
	private static final EnumSet<FilterOptions> filterOptions = EnumSet.noneOf(FilterOptions.class);
	
	private TrntChkReport report;

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
					final var str = new StringBuilder(item.getData().getTitle());
					Optional.ofNullable(item.getData().getLength()).ifPresent(l -> str.append(" (" + l + ")"));
					Optional.ofNullable(item.getData().getStatus()).ifPresent(s -> str.append(" [" + s + "]"));
					setText(str.toString());
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
	
	public void setResult(TrntChkReport report)
	{
		this.report = report;
		build();
	}
	
	private void build()
	{
		treeview.setRoot(buildTree(null, report.filter(filterOptions)));
	}

	
	private TreeItem<Child> buildTree(TreeItem<Child> parent, List<Child> children)
	{
		final var p = parent == null ? new TreeItem<Child>() : parent;
		if (children != null)
			for(final var c : children)
				p.getChildren().add(buildTree(new TreeItem<>(c), c.getChildren()));
		return p;
	}

	@FXML private void showok(javafx.event.ActionEvent e)
	{
		if(showok.isSelected())
			filterOptions.add(FilterOptions.SHOWOK);
		else
			filterOptions.remove(FilterOptions.SHOWOK);
		build();
	}

	@FXML private void hidemissing(javafx.event.ActionEvent e)
	{
		if(hidemissing.isSelected())
			filterOptions.add(FilterOptions.HIDEMISSING);
		else
			filterOptions.remove(FilterOptions.HIDEMISSING);
		build();
	}

	@FXML
	private void openAllNodes(javafx.event.ActionEvent e)
	{
		final var root = treeview.getRoot();
		treeview.setRoot(null);
		for (TreeItem<?> child : root.getChildren())
			if (!child.isLeaf())
				child.setExpanded(true);
		treeview.setRoot(root);
	}

	@FXML
	private void closeAllNodes(javafx.event.ActionEvent e)
	{
		final var root = treeview.getRoot();
		treeview.setRoot(null);
		for (TreeItem<?> child : root.getChildren())
			if (!child.isLeaf())
				child.setExpanded(false);
		treeview.setRoot(root);
	}

}
