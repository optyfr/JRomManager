package jrm.fx.ui.profile.report;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import jrm.fx.ui.MainFrame;
import jrm.locale.Messages;
import jrm.profile.data.Entity;
import jrm.profile.report.ContainerTZip;
import jrm.profile.report.ContainerUnknown;
import jrm.profile.report.ContainerUnneeded;
import jrm.profile.report.EntryAdd;
import jrm.profile.report.EntryMissing;
import jrm.profile.report.EntryMissingDuplicate;
import jrm.profile.report.EntryOK;
import jrm.profile.report.EntryUnneeded;
import jrm.profile.report.Report;
import jrm.profile.report.RomSuspiciousCRC;
import jrm.profile.report.SubjectSet;

public class ReportViewController implements Initializable
{
	@FXML
	private TreeView<Object> treeview;

	private static final Pattern regex = Pattern.compile("%s");

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
	}

	public void setReport(Report report)
	{
		final var root = new TreeItem<Object>(report);
		for (final var s : report)
		{
			final var sitem = new TreeItem<Object>(s);
			for (final var n : s)
			{
				final var nitem = new TreeItem<Object>(n);
				sitem.getChildren().add(nitem);
			}
			root.getChildren().add(sitem);
		}
		treeview.setShowRoot(false);
		final String[] missing = regex.split(Messages.getString("SubjectSet.Missing"));
		final String[] unneeded = regex.split(Messages.getString("SubjectSet.Unneeded"));
		final String[] foundneedfixes = regex.split(Messages.getString("SubjectSet.FoundNeedFixes"));
		final String[] foundincomplete = regex.split(Messages.getString("SubjectSet.FoundIncomplete"));
		final String[] found = regex.split(Messages.getString("SubjectSet.Found"));
		final String[] missingtotallycreated = regex.split(Messages.getString("SubjectSet.MissingTotallyCreated"));
		final String[] missingpartiallycreated = regex.split(Messages.getString("SubjectSet.MissingPartiallyCreated"));
		final String[] unknown = regex.split(Messages.getString("SubjectSet.Unknown"));
		final String[] eadd = regex.split(Messages.getString("EntryAddAdd"));
		final String[] emissing = regex.split(Messages.getString("EntryMissing.Missing"));
		final String[] emissingdup = regex.split(Messages.getString("EntryMissingDuplicate.MissingDuplicate"));
		final String[] eok = regex.split(Messages.getString("EntryOK.OK"));
		final String[] eunneeded = regex.split(Messages.getString("EntryUnneeded.Unneeded"));
		treeview.setFixedCellSize(20);
		treeview.setCellFactory(p -> new TreeCell<>()
		{
			@Override
			protected void updateItem(Object item, boolean empty)
			{
				super.updateItem(item, empty);

				if (empty)
				{
					setGraphic(null);
					setText(null);
				}
				else
				{
					if (item instanceof SubjectSet s)
					{
						final String[] t = switch (s.getStatus())
						{
							case MISSING -> missing;
							case UNNEEDED -> unneeded;
							case FOUND -> s.hasNotes() ? (s.isFixable() ? foundneedfixes : foundincomplete) : found;
							case CREATE, CREATEFULL -> s.isFixable() ? missingtotallycreated : missingpartiallycreated;
							default -> unknown;
						};
						final var i = new ImageView(MainFrame.getIcon(getFolderIcon(s, false)));
						final var n = new Text(s.getWare().getFullName());
						n.setFill(Color.BLUE);
						final var d = new Text(s.getWare().getDescription().toString());
						d.setFill(Color.PURPLE);
						setGraphic(new HBox(i, new Text(t[0]), n, new Text(t[1]), d, new Text(t[2])));
						setText(null);
					}
					else if(item instanceof RomSuspiciousCRC s)
					{
						final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/information.png"));
						setGraphic(new HBox(i, new Text(s.toString())));
						setText(null);
					}
					else if(item instanceof ContainerTZip s)
					{
						final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/compress.png"));
						setGraphic(new HBox(i, new Text(s.toString())));
						setText(null);
					}
					else if(item instanceof ContainerUnknown s)
					{
						final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/error.png"));
						setGraphic(new HBox(i, new Text(s.toString())));
						setText(null);
					}
					else if(item instanceof ContainerUnneeded s)
					{
						final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/exclamation.png"));
						setGraphic(new HBox(i, new Text(s.toString())));
						setText(null);
					}
					else if(item instanceof EntryAdd s)
					{
						final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_blue.png"));
						final var n = new Text(s.getParent().getWare().getFullName());
						n.setFill(Color.BLUE);
						final var en = new Text(s.getEntity().getNormalizedName());
						en.setFont(Font.font(en.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, en.getFont().getSize()));
						final var ep = new Text(s.getEntry().getParent().getRelFile().toString());
						ep.setFont(Font.font(ep.getFont().getFamily(), FontPosture.ITALIC, ep.getFont().getSize()));
						final var ef = new Text(s.getEntry().getRelFile().toString());
						ef.setFont(Font.font(ef.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, ef.getFont().getSize()));
						setGraphic(new HBox(i, new Text(eadd[0]), n, new Text(eadd[1]), en, new Text(eadd[2]), ep, new Text(eadd[3]), ef));
						setText(null);
					}
					else if(item instanceof EntryMissing s)
					{
						final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_red.png"));
						final var n = new Text(s.getParent().getWare().getFullName());
						n.setFill(Color.BLUE);
						final var en = new Text(s.getEntity().getName());
						en.setFont(Font.font(en.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, en.getFont().getSize()));
						if (s.getEntity() instanceof Entity e)
						{
							final String hash;
							if (e.getSha1() != null)
								hash = e.getSha1();
							else if (e.getMd5() != null)
								hash = e.getMd5();
							else
								hash = e.getCrc();
							setGraphic(new HBox(i, new Text(emissing[0]), n, new Text(emissing[1]), en, new Text(emissing[2] + " (" + hash + ")")));
						}
						else
							setGraphic(new HBox(i, new Text(emissing[0]), n, new Text(emissing[1]), en, new Text(emissing[2])));
						setText(null);
					}
					else if(item instanceof EntryMissingDuplicate s)
					{
						final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_purple.png"));
						final var n = new Text(s.getParent().getWare().getFullName());
						n.setFill(Color.BLUE);
						final var ef = new Text(s.getEntry().getRelFile());
						ef.setFont(Font.font(ef.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, ef.getFont().getSize()));
						final var en = new Text(s.getEntity().getName());
						en.setFont(Font.font(en.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, en.getFont().getSize()));
						setGraphic(new HBox(i, new Text(emissingdup[0]), n, new Text(emissingdup[1]), ef, new Text(emissingdup[2]), en));
						setText(null);
					}
					else if(item instanceof EntryOK s)
					{
						final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_green.png"));
						final var n = new Text(s.getParent().getWare().getFullName());
						n.setFill(Color.BLUE);
						final var en = new Text(s.getEntity().getNormalizedName());
						en.setFont(Font.font(en.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, en.getFont().getSize()));
						setGraphic(new HBox(i, new Text(eok[0]), n, new Text(eok[1]), en, new Text(eok[2])));
						setText(null);
					}
					else if(item instanceof EntryUnneeded s)
					{
						final var i = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_black.png"));
						final var n = new Text(s.getParent().getWare().getFullName());
						n.setFill(Color.BLUE);
						final var ef = new Text(s.getEntry().getRelFile());
						ef.setFont(Font.font(ef.getFont().getFamily(), FontWeight.BOLD, FontPosture.REGULAR, ef.getFont().getSize()));
						final String hash;
						if (s.getEntry().getSha1() != null)
							hash = s.getEntry().getSha1();
						else if (s.getEntry().getMd5() != null)
							hash = s.getEntry().getMd5();
						else
							hash = s.getEntry().getCrc();
						setGraphic(new HBox(i, new Text(eunneeded[0]), n, new Text(eunneeded[1]), ef, new Text(eunneeded[2]), new Text(hash), new Text(eunneeded[3])));
						setText(null);
					}
					else
					{
						setGraphic(null);
						setText(item.toString());
					}
				}
			}
		});
		treeview.setRoot(root);
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
