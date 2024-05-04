package jrm.fx.ui.profile.report;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import jrm.fx.ui.MainFrame;
import jrm.fx.ui.controls.Dialogs;
import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.profile.data.Entity;
import jrm.profile.data.ExportMode;
import jrm.profile.manager.Export.ExportType;
import jrm.profile.report.ContainerTZip;
import jrm.profile.report.ContainerUnknown;
import jrm.profile.report.ContainerUnneeded;
import jrm.profile.report.EntryAdd;
import jrm.profile.report.EntryMissing;
import jrm.profile.report.EntryMissingDuplicate;
import jrm.profile.report.EntryOK;
import jrm.profile.report.EntryUnneeded;
import jrm.profile.report.EntryWrongHash;
import jrm.profile.report.EntryWrongName;
import jrm.profile.report.FilterOptions;
import jrm.profile.report.Note;
import jrm.profile.report.Report;
import jrm.profile.report.RomSuspiciousCRC;
import jrm.profile.report.SubjectSet;
import lombok.val;

public class ReportViewController implements Initializable
{
	@FXML protected TreeView<Object> treeview;
	@FXML private ContextMenu menu;
	@FXML private MenuItem openAllNodes;
	@FXML private MenuItem closeAllNodes;
	@FXML private CheckMenuItem showok;
	@FXML private CheckMenuItem hidemissing;
	@FXML private MenuItem detail;
	@FXML private MenuItem copyCrc;
	@FXML private MenuItem copySha1;
	@FXML private MenuItem copyName;
	@FXML private MenuItem searchWeb;
	@FXML private Button download;
	@FXML private MenuButton exportAs;
	
	private static final Set<FilterOptions> filterOptions = new HashSet<>();

	private Report report; 

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		openAllNodes.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/folder_open.png")));
		closeAllNodes.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/folder_closed.png")));
		showok.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/folder_closed_green.png")));
		hidemissing.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/folder_closed_red.png")));
		menu.setOnShowing(e -> {
			final var item = treeview.getSelectionModel().getSelectedItem();
			final var disabled = !(item!=null && item.getValue() instanceof Note);
			detail.setDisable(disabled);
			copyCrc.setDisable(disabled);
			copySha1.setDisable(disabled);
			copyName.setDisable(disabled);
			searchWeb.setDisable(disabled);
		});
	}

	public void setReport(Report report)
	{
		this.report = report;
		build();
	}
	
	private void build()
	{
		final var root = new TreeItem<Object>(report);
		if(report!=null)
		{
			report.stream(filterOptions).forEachOrdered(s -> {
				final var sitem = new TreeItem<Object>(s);
				s.stream(filterOptions).forEach(n -> sitem.getChildren().add(new TreeItem<>(n)));
				root.getChildren().add(sitem);
			});
			download.setDisable(!Optional.ofNullable(report.getReportFile()).map(File::exists).orElse(false));
			exportAs.setDisable(report.getProfile()==null);
		}
		else
		{
			download.setDisable(true);
			exportAs.setDisable(true);
		}
		treeview.setShowRoot(false);
		treeview.setCellFactory(p -> new ReportTreeCell());
		treeview.setRoot(root);
	}

	@FXML private void detail(javafx.event.ActionEvent e)
	{
		if(treeview.getSelectionModel().getSelectedItem() != null && treeview.getSelectionModel().getSelectedItem().getValue() instanceof Note note)
			detail(note);
	}
	
	private static void detail(Note note)
	{
		final var node = new TextArea(note.getDetail());
		node.setEditable(false);
		Dialogs.showConfirmation("Detail", node, ButtonType.OK);
	}
	
	@FXML private void copyCrc(javafx.event.ActionEvent e)
	{
		if(treeview.getSelectionModel().getSelectedItem() != null && treeview.getSelectionModel().getSelectedItem().getValue() instanceof Note note)
		{
			final var content = new ClipboardContent();
			content.putString(note.getCrc());
			Clipboard.getSystemClipboard().setContent(content);
		}
	}
	
	@FXML private void copySha1(javafx.event.ActionEvent e)
	{
		if(treeview.getSelectionModel().getSelectedItem() != null && treeview.getSelectionModel().getSelectedItem().getValue() instanceof Note note)
		{
			final var content = new ClipboardContent();
			content.putString(note.getSha1());
			Clipboard.getSystemClipboard().setContent(content);
		}
	}
	
	@FXML private void copyName(javafx.event.ActionEvent e)
	{
		if(treeview.getSelectionModel().getSelectedItem() != null && treeview.getSelectionModel().getSelectedItem().getValue() instanceof Note note)
		{
			final var content = new ClipboardContent();
			content.putString(note.getName());
			Clipboard.getSystemClipboard().setContent(content);
		}
	}

	@FXML private void searchWeb(javafx.event.ActionEvent e)
	{
		if(treeview.getSelectionModel().getSelectedItem() != null && treeview.getSelectionModel().getSelectedItem().getValue() instanceof Note note)
		{
			try
			{
				val name = note.getName();
				val crc = note.getCrc();
				val sha1 = note.getSha1();
				val hash = Optional.ofNullable(Optional.ofNullable(crc).orElse(sha1)).map(h -> '+' + h).orElse("");
				MainFrame.getApplication().getHostServices().showDocument(new URI("https://www.google.com/search?q=" + URLEncoder.encode('"' + name + '"', "UTF-8") + hash).toString());
			}
			catch (IOException | URISyntaxException e1)
			{
				Log.err(e1.getMessage(), e1);
			}
		}		
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

	@FXML
	private void download(javafx.event.ActionEvent e)
	{
		if (report == null)
			return;
		try
		{
			Desktop.getDesktop().open(report.getReportFile());
		}
		catch (IOException e1)
		{
			Log.err(e1.getMessage(), e1);
		}
	}

	@FXML
	private void exportFilteredAsLogiqxDat(javafx.event.ActionEvent e)
	{
		MainFrame.export(treeview.getScene().getWindow(), report.getProfile().getSession(), ExportType.DATAFILE, EnumSet.of(ExportMode.FILTERED), null);
	}
	
	@FXML
	private void exportFilteredAsMameDat(javafx.event.ActionEvent e)
	{
		MainFrame.export(treeview.getScene().getWindow(), report.getProfile().getSession(), ExportType.MAME, EnumSet.of(ExportMode.FILTERED), null);
	}
	
	@FXML
	private void exportFilteredAsSoftwareLists(javafx.event.ActionEvent e)
	{
		MainFrame.export(treeview.getScene().getWindow(), report.getProfile().getSession(), ExportType.SOFTWARELIST, EnumSet.of(ExportMode.FILTERED), null);
	}
	
	@FXML
	private void exportAllAsLogiqxDat(javafx.event.ActionEvent e)
	{
		MainFrame.export(treeview.getScene().getWindow(), report.getProfile().getSession(), ExportType.DATAFILE, EnumSet.of(ExportMode.ALL), null);
	}
	
	@FXML
	private void exportAllAsMameDat(javafx.event.ActionEvent e)
	{
		MainFrame.export(treeview.getScene().getWindow(), report.getProfile().getSession(), ExportType.MAME, EnumSet.of(ExportMode.ALL), null);
	}
	
	@FXML
	private void exportAllAsSoftwareLists(javafx.event.ActionEvent e)
	{
		MainFrame.export(treeview.getScene().getWindow(), report.getProfile().getSession(), ExportType.SOFTWARELIST, EnumSet.of(ExportMode.ALL), null);
	}
	
	private static final class ReportTreeCell extends TreeCell<Object>
	{
		private static final String FX_FONT_WEIGHT_BOLD = "-fx-font-weight: bold;";

		private static final Pattern regex = Pattern.compile("%s");
		
		private static final String[] missing = regex.split(Messages.getString("SubjectSet.Missing"));
		private static final String[] unneeded = regex.split(Messages.getString("SubjectSet.Unneeded"));
		private static final String[] foundneedfixes = regex.split(Messages.getString("SubjectSet.FoundNeedFixes"));
		private static final String[] foundincomplete = regex.split(Messages.getString("SubjectSet.FoundIncomplete"));
		private static final String[] found = regex.split(Messages.getString("SubjectSet.Found"));
		private static final String[] missingtotallycreated = regex.split(Messages.getString("SubjectSet.MissingTotallyCreated"));
		private static final String[] missingpartiallycreated = regex.split(Messages.getString("SubjectSet.MissingPartiallyCreated"));
		private static final String[] unknown = regex.split(Messages.getString("SubjectSet.Unknown"));
		private static final String[] eadd = regex.split(Messages.getString("EntryAddAdd"));
		private static final String[] emissing = regex.split(Messages.getString("EntryMissing.Missing"));
		private static final String[] emissingdup = regex.split(Messages.getString("EntryMissingDuplicate.MissingDuplicate"));
		private static final String[] eok = regex.split(Messages.getString("EntryOK.OK"));
		private static final String[] eunneeded = regex.split(Messages.getString("EntryUnneeded.Unneeded"));
		private static final String[] ewronghash = regex.split(Messages.getString("EntryWrongHash.Wrong"));
		private static final String[] ewrongname = regex.split(Messages.getString("EntryWrongName.Wrong"));

		public ReportTreeCell()
		{
			setOnMouseClicked(e -> {
				final var ti = getTreeItem();
				if (ti == null || e.getClickCount() < 2)
					return;
				if(ti.getValue() instanceof Note note)
					detail(note);
			});
		}
		
		@Override
		protected void updateItem(Object item, boolean empty)
		{
			super.updateItem(item, empty);

			if (empty)
			{
				setGraphic(null);
				setText(null);
			}
			else if (item instanceof SubjectSet s)
				updateSubjectSet(s);
			else if(item instanceof RomSuspiciousCRC s)
				updateRomSuspiciousCRC(s);
			else if(item instanceof ContainerTZip s)
				updateContainerTZip(s);
			else if(item instanceof ContainerUnknown s)
				updateContainerUnknown(s);
			else if(item instanceof ContainerUnneeded s)
				updateContainerUnneeded(s);
			else if(item instanceof EntryAdd s)
				updateEntryAdd(s);
			else if(item instanceof EntryMissing s)
				updateEntryMissing(s);
			else if(item instanceof EntryMissingDuplicate s)
				updateEntryMissingDuplicate(s);
			else if(item instanceof EntryOK s)
				updateEntryOK(s);
			else if(item instanceof EntryUnneeded s)
				updateEntryUnneeded(s);
			else if(item instanceof EntryWrongHash s)
				updateEntryWrongHash(s);
			else if(item instanceof EntryWrongName s)
				updateEntryWrongName(s);
			else
			{
				setGraphic(null);
				setText(item.toString());
			}
			setAlignment(Pos.CENTER_LEFT);
		}

		/**
		 * @param s
		 */
		private void updateContainerUnneeded(ContainerUnneeded s)
		{
			final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/exclamation.png"));
			i.getStyleClass().add("icon");
			HBox hBox = new HBox(i, new Text(s.toString()));
			hBox.setAlignment(Pos.CENTER_LEFT);
			setGraphic(hBox);
			setText(null);
		}

		/**
		 * @param s
		 */
		private void updateContainerUnknown(ContainerUnknown s)
		{
			final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/error.png"));
			i.getStyleClass().add("icon");
			HBox hBox = new HBox(i, new Text(s.toString()));
			hBox.setAlignment(Pos.CENTER_LEFT);
			setGraphic(hBox);
			setText(null);
		}

		/**
		 * @param s
		 */
		private void updateContainerTZip(ContainerTZip s)
		{
			final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/compress.png"));
			i.getStyleClass().add("icon");
			HBox hBox = new HBox(i, new Text(s.toString()));
			hBox.setAlignment(Pos.CENTER_LEFT);
			setGraphic(hBox);
			setText(null);
		}

		/**
		 * @param s
		 */
		private void updateRomSuspiciousCRC(RomSuspiciousCRC s)
		{
			final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/information.png"));
			i.getStyleClass().add("icon");
			HBox hBox = new HBox(i, new Text(s.toString()));
			hBox.setAlignment(Pos.CENTER_LEFT);
			setGraphic(hBox);
			setText(null);
		}

		/**
		 * @param s
		 */
		private void updateEntryWrongName(EntryWrongName s)
		{
			final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_pink.png"));
			final var n = new Text(s.getParent().getWare().getFullName());
			n.setFill(Color.BLUE);
			final var en = new Text(s.getEntry().getName());
			en.styleProperty().bind(new SimpleStringProperty(FX_FONT_WEIGHT_BOLD));
			final var enn = new Text(s.getEntity().getNormalizedName());
			enn.styleProperty().bind(new SimpleStringProperty(FX_FONT_WEIGHT_BOLD));
			i.getStyleClass().add("icon");
			HBox hBox = new HBox(i, new Text(ewrongname[0]), n, new Text(ewrongname[1]), en, new Text(ewrongname[2]), enn, new Text(ewrongname[3]));
			hBox.setAlignment(Pos.CENTER_LEFT);
			setGraphic(hBox);
			setText(null);
		}

		/**
		 * @param s
		 */
		private void updateEntryWrongHash(EntryWrongHash s)
		{
			final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_orange.png"));
			final var n = new Text(s.getParent().getWare().getFullName());
			n.setFill(Color.BLUE);
			final var ef = new Text(s.getEntry().getRelFile());
			ef.styleProperty().bind(new SimpleStringProperty(FX_FONT_WEIGHT_BOLD));
			final String hashname;
			final String ehash;
			final String hash;
			if(s.getEntry().getMd5() == null && s.getEntry().getSha1() == null)
			{
				hashname = "CRC";
				ehash = s.getEntry().getCrc();
				hash = s.getCrc();
			}
			else if(s.getEntry().getSha1() == null)
			{
				hashname = "MD5";
				ehash = s.getEntry().getMd5();
				hash = s.getMd5();
			}
			else
			{
				hashname = "SHA-1";
				ehash = s.getEntry().getSha1();
				hash = s.getSha1();
			}
			i.getStyleClass().add("icon");
			HBox hBox = new HBox(i, new Text(ewronghash[0]), n, new Text(ewronghash[1]), ef, new Text(ewronghash[2]), new Text(hashname), new Text(ewronghash[3]), new Text(ehash), new Text(ewronghash[4]), new Text(hash), new Text(ewronghash[5]));
			hBox.setAlignment(Pos.CENTER_LEFT);
			setGraphic(hBox);
			setText(null);
		}

		/**
		 * @param s
		 */
		private void updateEntryUnneeded(EntryUnneeded s)
		{
			final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_black.png"));
			final var n = new Text(s.getParent().getWare().getFullName());
			n.setFill(Color.BLUE);
			final var ef = new Text(s.getEntry().getRelFile());
			ef.styleProperty().bind(new SimpleStringProperty(FX_FONT_WEIGHT_BOLD));
			final String hash;
			if (s.getEntry().getSha1() != null)
				hash = s.getEntry().getSha1();
			else if (s.getEntry().getMd5() != null)
				hash = s.getEntry().getMd5();
			else
				hash = s.getEntry().getCrc();
			i.getStyleClass().add("icon");
			HBox hBox = new HBox(i, new Text(eunneeded[0]), n, new Text(eunneeded[1]), ef, new Text(eunneeded[2]), new Text(hash), new Text(eunneeded[3]));
			hBox.setAlignment(Pos.CENTER_LEFT);
			setGraphic(hBox);
			setText(null);
		}

		/**
		 * @param s
		 */
		private void updateEntryOK(EntryOK s)
		{
			final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_green.png"));
			final var n = new Text(s.getParent().getWare().getFullName());
			n.setFill(Color.BLUE);
			final var en = new Text(s.getEntity().getNormalizedName());
			en.styleProperty().bind(new SimpleStringProperty(FX_FONT_WEIGHT_BOLD));
			i.getStyleClass().add("icon");
			HBox hBox = new HBox(i, new Text(eok[0]), n, new Text(eok[1]), en, new Text(eok[2]));
			hBox.setAlignment(Pos.CENTER_LEFT);
			setGraphic(hBox);
			setText(null);
		}

		/**
		 * @param s
		 */
		private void updateEntryMissingDuplicate(EntryMissingDuplicate s)
		{
			final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_purple.png"));
			final var n = new Text(s.getParent().getWare().getFullName());
			n.setFill(Color.BLUE);
			final var ef = new Text(s.getEntry().getRelFile());
			ef.styleProperty().bind(new SimpleStringProperty(FX_FONT_WEIGHT_BOLD));
			final var en = new Text(s.getEntity().getName());
			en.styleProperty().bind(new SimpleStringProperty(FX_FONT_WEIGHT_BOLD));
			i.getStyleClass().add("icon");
			HBox hBox = new HBox(i, new Text(emissingdup[0]), n, new Text(emissingdup[1]), ef, new Text(emissingdup[2]), en);
			hBox.setAlignment(Pos.CENTER_LEFT);
			setGraphic(hBox);
			setText(null);
		}

		/**
		 * @param s
		 */
		private void updateEntryMissing(EntryMissing s)
		{
			final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_red.png"));
			final var n = new Text(s.getParent().getWare().getFullName());
			n.setFill(Color.BLUE);
			final var en = new Text(s.getEntity().getName());
			en.styleProperty().bind(new SimpleStringProperty(FX_FONT_WEIGHT_BOLD));
			if (s.getEntity() instanceof Entity e)
			{
				final String hash;
				if (e.getSha1() != null)
					hash = e.getSha1();
				else if (e.getMd5() != null)
					hash = e.getMd5();
				else
					hash = e.getCrc();
				i.getStyleClass().add("icon");
				HBox hBox = new HBox(i, new Text(emissing[0]), n, new Text(emissing[1]), en, new Text(emissing[2] + " (" + hash + ")"));
				hBox.setAlignment(Pos.CENTER_LEFT);
				setGraphic(hBox);
			}
			else
			{
				i.getStyleClass().add("icon");
				HBox hBox = new HBox(i, new Text(emissing[0]), n, new Text(emissing[1]), en, new Text(emissing[2]));
				hBox.setAlignment(Pos.CENTER_LEFT);
				setGraphic(hBox);
			}
			setText(null);
		}

		/**
		 * @param s
		 */
		private void updateEntryAdd(EntryAdd s)
		{
			final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_blue.png"));
			final var n = new Text(s.getParent().getWare().getFullName());
			n.setFill(Color.BLUE);
			final var en = new Text(s.getEntity().getNormalizedName());
			en.styleProperty().bind(new SimpleStringProperty(FX_FONT_WEIGHT_BOLD));
			final var ep = new Text(s.getEntry().getParent().getRelFile().toString());
			ep.styleProperty().bind(new SimpleStringProperty("-fx-font-style: italic;"));
			final var ef = new Text(s.getEntry().getRelFile());
			ef.styleProperty().bind(new SimpleStringProperty(FX_FONT_WEIGHT_BOLD));
			i.getStyleClass().add("icon");
			HBox hBox = new HBox(i, new Text(eadd[0]), n, new Text(eadd[1]), en, new Text(eadd[2]), ep, new Text(eadd[3]), ef);
			hBox.setAlignment(Pos.CENTER_LEFT);
			setGraphic(hBox);
			setText(null);
		}

		/**
		 * @param s
		 */
		private void updateSubjectSet(SubjectSet s)
		{
			final String[] t = switch (s.getStatus())
			{
				case MISSING -> missing;
				case UNNEEDED -> unneeded;
				case FOUND -> s.hasNotes() ? (s.isFixable() ? foundneedfixes : foundincomplete) : found;
				case CREATE, CREATEFULL -> s.isFixable() ? missingtotallycreated : missingpartiallycreated;
				default -> unknown;
			};
			final ImageView i;
			if (s.getNotes().isEmpty() && SubjectSet.Status.FOUND.equals(s.getStatus()))
				i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_green.png"));
			else
				i = new ImageView(MainFrame.getIcon(getFolderIcon(s, false)));
			i.getStyleClass().add("icon");
			final var n = new Text(s.getWare().getFullName());
			n.setFill(Color.BLUE);
			final var d = new Text(s.getWare().getDescription().toString());
			d.setFill(Color.PURPLE);
			final var hBox = new HBox(i, new Text(t[0]), n, new Text(t[1]), d, new Text(t[2]));
			hBox.setAlignment(Pos.CENTER_LEFT);
			HBox.setMargin(i, new Insets(0, 2, 0, 0));
			setGraphic(hBox);
			setText(null);
		}
		
		/**
		 * @param value
		 * @param expanded
		 * @return
		 */
		private String getFolderIcon(Object value, final boolean expanded)
		{
			String icon = "/jrm/resicons/folder"; //$NON-NLS-1$
			icon += expanded ? "_open" : "_closed";
			if (value instanceof SubjectSet s)
			{
				icon += switch (s.getStatus())
				{
					case FOUND -> s.hasNotes() ? (s.isFixable() ? "_purple" : "_orange") : "_green";
					case CREATE, CREATEFULL -> s.isFixable() ? "_blue" : "_orange";
					case MISSING -> "_red"; //$NON-NLS-1$
					case UNNEEDED -> "_gray"; //$NON-NLS-1$
					default -> "";
				};
			}
			icon += ".png"; //$NON-NLS-1$
			return icon;
		}

	}

}
