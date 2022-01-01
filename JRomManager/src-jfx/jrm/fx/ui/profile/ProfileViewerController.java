package jrm.fx.ui.profile;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import jrm.fx.ui.MainFrame;
import jrm.locale.Messages;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.AnywareStatus;
import jrm.profile.data.Entity;
import jrm.profile.data.MachineList;
import jrm.profile.data.SoftwareList;

public class ProfileViewerController implements Initializable
{
	@FXML private TableView<AnywareList<? extends Anyware>> tableWL;
	@FXML private TableColumn<AnywareList<? extends Anyware>, AnywareList<? extends Anyware>> tableWLName;
	@FXML private TableColumn<AnywareList<? extends Anyware>, String> tableWLDesc;
	@FXML private TableColumn<AnywareList<? extends Anyware>, String> tableWLHave;
	@FXML private TableView<Anyware> tableW;
	@FXML private TableView<Entity> tableEntity;

	private final Map<String,String> haveCache = new HashMap<>();
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		tableWL.setFixedCellSize(18);
		tableWLName.setCellFactory(p -> new TableCell<AnywareList<? extends Anyware>, AnywareList<? extends Anyware>>()
		{
			private static final Image diskMultipleGreen = MainFrame.getIcon("/jrm/resicons/disk_multiple_green.png"); //$NON-NLS-1$
			private static final Image diskMultipleOrange = MainFrame.getIcon("/jrm/resicons/disk_multiple_orange.png"); //$NON-NLS-1$
			private static final Image diskMultipleRed = MainFrame.getIcon("/jrm/resicons/disk_multiple_red.png"); //$NON-NLS-1$
			private static final Image diskMultipleGray = MainFrame.getIcon("/jrm/resicons/disk_multiple_gray.png"); //$NON-NLS-1$

			@Override
			protected void updateItem(AnywareList<? extends Anyware> item, boolean empty)
			{
				if (empty)
				{
					setText("");
					setGraphic(null);
				}
				else if (item instanceof SoftwareList sl)
				{
					setGraphic(new ImageView(switch (sl.getStatus())
					{
						case COMPLETE -> diskMultipleGreen;
						case PARTIAL -> diskMultipleOrange;
						case MISSING -> diskMultipleRed;
						case UNKNOWN -> diskMultipleGray;
						default -> diskMultipleGray;

					}));
					setText(sl.getName());
				}
				else if (item instanceof MachineList ml)
				{
					setGraphic(new ImageView(switch (ml.getStatus())
					{
						case COMPLETE -> diskMultipleGreen;
						case PARTIAL -> diskMultipleOrange;
						case MISSING -> diskMultipleRed;
						case UNKNOWN -> diskMultipleGray;
						default -> diskMultipleGray;

					}));
					setText(Messages.getString("MachineListListRenderer.*"));
				}
				setTooltip(new Tooltip(getText()));
				setFont(new Font(10));
			}
		});
		tableWLName.setCellValueFactory(p -> new ObservableValueBase<AnywareList<? extends Anyware>>()
		{
			@Override
			public AnywareList<? extends Anyware> getValue()
			{
				return p.getValue();
			}
		});
		tableWLDesc.setCellFactory(p -> new TableCell<AnywareList<? extends Anyware>, String>()
		{
			@Override
			protected void updateItem(String item, boolean empty)
			{
				if (empty)
					setText("");
				else
					setText(item);
				setTooltip(new Tooltip(getText()));
				setFont(new Font(10));
				setGraphic(null);
			}
		});
		tableWLDesc.setCellValueFactory(p -> new ObservableValueBase<String>()
		{
			@Override
			public String getValue()
			{
				if (p.getValue() instanceof SoftwareList sl)
					return sl.getDescription().toString();
				return Messages.getString("MachineListList.AllMachines");
			}
		});
		tableWLHave.setCellFactory(p -> new TableCell<AnywareList<? extends Anyware>, String>()
		{
			@Override
			protected void updateItem(String item, boolean empty)
			{
				if (empty)
					setText("");
				else
					setText(item);
				setFont(new Font(10));
				setTextAlignment(TextAlignment.CENTER);
				setAlignment(Pos.CENTER);
				setGraphic(null);
			}
		});
		tableWLHave.setCellValueFactory(p -> new ObservableValueBase<String>()
		{
			@Override
			public String getValue()
			{
				return haveCache.computeIfAbsent(p.getValue().getName(), k -> {
					final long[] ht = { 0, 0 };
					p.getValue().getFilteredStream().forEach(t -> {
						if (t.getStatus() == AnywareStatus.COMPLETE)
							ht[0]++;
						ht[1]++;
					});
					return String.format("%d/%d", ht[0], ht[1]);
				});
			}
		});
	}

	void clear()
	{
		tableWL.setItems(null);
		haveCache.clear();
	}

	void reload()
	{
		tableWL.refresh();
		haveCache.clear();
	}

	void reset(Profile profile)
	{
		clear();
		final var wl = FXCollections.<AnywareList<? extends Anyware>>observableArrayList();
		for (final var w : profile.getMachineListList().getFilteredList())
			wl.add(w);
		for (final var w : profile.getMachineListList().getSoftwareListList().getFilteredList())
			wl.add(w);
		tableWL.setItems(wl);
	}
}
