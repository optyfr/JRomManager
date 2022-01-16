package jrm.fx.ui.profile;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import jrm.fx.ui.MainFrame;
import jrm.locale.Messages;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.AnywareStatus;
import jrm.profile.data.Entity;
import jrm.profile.data.Machine;
import jrm.profile.data.MachineList;
import jrm.profile.data.Samples;
import jrm.profile.data.Software;
import jrm.profile.data.SoftwareList;

public class ProfileViewerController implements Initializable
{
	@FXML private TableView<AnywareList<? extends Anyware>> tableWL;
	@FXML private TableColumn<AnywareList<? extends Anyware>, AnywareList<? extends Anyware>> tableWLName;
	@FXML private TableColumn<AnywareList<? extends Anyware>, String> tableWLDesc;
	@FXML private TableColumn<AnywareList<? extends Anyware>, String> tableWLHave;
	@FXML private TableView<Anyware> tableW;
	private final TableColumn<Anyware, Machine> tableWMStatus = new TableColumn<>(Messages.getString("MachineListRenderer.Status"));
	private final TableColumn<Anyware, Machine> tableWMName = new TableColumn<>(Messages.getString("MachineListRenderer.Name"));
	private final TableColumn<Anyware, String> tableWMDescription = new TableColumn<>(Messages.getString("MachineListRenderer.Description"));
	private final TableColumn<Anyware, String> tableWMHave = new TableColumn<>(Messages.getString("MachineListRenderer.Have"));
	private final TableColumn<Anyware, Object> tableWMCloneOf = new TableColumn<>(Messages.getString("MachineListRenderer.CloneOf"));
	private final TableColumn<Anyware, Object> tableWMRomOf = new TableColumn<>(Messages.getString("MachineListRenderer.RomOf"));
	private final TableColumn<Anyware, Object> tableWMSampleOf = new TableColumn<>(Messages.getString("MachineListRenderer.SampleOf"));
	private final TableColumn<Anyware, CheckBox> tableWMSelected = new TableColumn<>(Messages.getString("MachineListRenderer.Selected"));
	private final TableColumn<Anyware, Software> tableWSStatus = new TableColumn<>(Messages.getString("SoftwareListRenderer.Status"));
	private final TableColumn<Anyware, Software> tableWSName = new TableColumn<>(Messages.getString("SoftwareListRenderer.Name"));
	private final TableColumn<Anyware, String> tableWSDescription = new TableColumn<>(Messages.getString("SoftwareListRenderer.Description"));
	private final TableColumn<Anyware, String> tableWSHave = new TableColumn<>(Messages.getString("SoftwareListRenderer.Have"));
	private final TableColumn<Anyware, Object> tableWSCloneOf = new TableColumn<>(Messages.getString("SoftwareListRenderer.CloneOf"));
	private final TableColumn<Anyware, Software> tableWSSelected = new TableColumn<>(Messages.getString("SoftwareListRenderer.Selected"));
	@FXML private TableView<Entity> tableEntity;

	private final Map<String,String> haveCache = new HashMap<>();
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		tableWL.setFixedCellSize(18);
		tableW.setFixedCellSize(18);
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
		tableWMStatus.setResizable(false);
		tableWMStatus.setSortable(false);
		tableWMStatus.setPrefWidth(20);
		tableWMStatus.setCellFactory(p -> new TableCell<Anyware, Machine>()
		{
			private static final Image folderClosedGreen = MainFrame.getIcon("/jrm/resicons/folder_closed_green.png"); //$NON-NLS-1$
			private static final Image folderClosedOrange = MainFrame.getIcon("/jrm/resicons/folder_closed_orange.png"); //$NON-NLS-1$
			private static final Image folderClosedRed = MainFrame.getIcon("/jrm/resicons/folder_closed_red.png"); //$NON-NLS-1$
			private static final Image folderClosedGray = MainFrame.getIcon("/jrm/resicons/folder_closed_gray.png"); //$NON-NLS-1$

			@Override
			protected void updateItem(Machine item, boolean empty)
			{
				super.updateItem(item, empty);
				if (!empty)
				{
					setGraphic(new ImageView(switch (item.getStatus())
					{
						case COMPLETE -> folderClosedGreen;
						case PARTIAL -> folderClosedOrange;
						case MISSING -> folderClosedRed;
						case UNKNOWN -> folderClosedGray;
						default -> folderClosedGray;

					}));
				}
			}
		});
		tableWMStatus.setCellValueFactory(p -> new ObservableValueBase<Machine>()
		{
			@Override
			public Machine getValue()
			{
				if(p.getValue() instanceof Machine m)
					return m;
				return null;
			}
		});
		tableWMName.setSortable(false);
		tableWMName.setPrefWidth(100);
		tableWMName.setCellFactory(p -> new TableCell<Anyware, Machine>()
		{
			private static final Image applicationOSXTerminal = MainFrame.getIcon("/jrm/resicons/icons/application_osx_terminal.png"); //$NON-NLS-1$
			private static final Image computer = MainFrame.getIcon("/jrm/resicons/icons/computer.png"); //$NON-NLS-1$
			private static final Image wrench = MainFrame.getIcon("/jrm/resicons/icons/wrench.png"); //$NON-NLS-1$
			private static final Image joystick = MainFrame.getIcon("/jrm/resicons/icons/joystick.png"); //$NON-NLS-1$
			
			@Override
			protected void updateItem(Machine item, boolean empty)
			{
				if (empty)
				{
					setText("");
					setGraphic(null);
				}
				else
				{
					setText(item.getBaseName());
					setTooltip(new Tooltip(item.getName()));
					if(item.isIsbios())
						setGraphic(new ImageView(applicationOSXTerminal));
					else if(item.isIsdevice())
						setGraphic(new ImageView(computer));
					else if(item.isIsmechanical())
						setGraphic(new ImageView(wrench));
					else
						setGraphic(new ImageView(joystick));
				}
				setFont(new Font(10));
			}
		});
		tableWMName.setCellValueFactory(tableWMStatus.getCellValueFactory());
		tableWMDescription.setSortable(false);
		tableWMDescription.setPrefWidth(200);
		tableWMDescription.setCellFactory(p -> new TableCell<Anyware, String>()
		{
			@Override
			protected void updateItem(String item, boolean empty)
			{
				if (empty)
					setText("");
				else
				{
					setText(item);
					setTooltip(new Tooltip(item));
				}
				setFont(new Font(10));
				setGraphic(null);
			}
		});
		tableWMDescription.setCellValueFactory(p -> new ObservableValueBase<String>()
		{
			@Override
			public String getValue()
			{
				return p.getValue().getDescription().toString();
			}
		});
		tableWMHave.setResizable(false);
		tableWMHave.setSortable(false);
		tableWMHave.setPrefWidth(45);
		tableWMHave.setCellFactory(p -> new TableCell<Anyware, String>()
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
		tableWMHave.setCellValueFactory(p -> new ObservableValueBase<String>()
		{
			@Override
			public String getValue()
			{
				if(p.getValue() instanceof Machine machine)
					return String.format("%d/%d", machine.countHave(), machine.countAll());
				return null;
			}
		});
		tableWMCloneOf.setSortable(false);
		tableWMCloneOf.setPrefWidth(100);
		tableWMCloneOf.setCellFactory(p -> new TableCell<Anyware, Object>()
		{
			private static final Image folderClosedGreen = MainFrame.getIcon("/jrm/resicons/folder_closed_green.png"); //$NON-NLS-1$
			private static final Image folderClosedOrange = MainFrame.getIcon("/jrm/resicons/folder_closed_orange.png"); //$NON-NLS-1$
			private static final Image folderClosedRed = MainFrame.getIcon("/jrm/resicons/folder_closed_red.png"); //$NON-NLS-1$
			private static final Image folderClosedGray = MainFrame.getIcon("/jrm/resicons/folder_closed_gray.png"); //$NON-NLS-1$

			@Override
			protected void updateItem(Object item, boolean empty)
			{
				if (item==null || empty)
				{
					setText("");
					setGraphic(null);
				}
				else
				{
					if(item instanceof Anyware aw)
					{
						setGraphic(new ImageView(switch (aw.getStatus())
						{
							case COMPLETE -> folderClosedGreen;
							case PARTIAL -> folderClosedOrange;
							case MISSING -> folderClosedRed;
							case UNKNOWN -> folderClosedGray;
							default -> folderClosedGray;
						}));
						setText(aw.getBaseName());
					}
					else
					{
						setGraphic(new ImageView(folderClosedGray));
						setText(item.toString());
					}
				}
				setFont(new Font(10));
			}
		});
		tableWMCloneOf.setCellValueFactory(p -> new ObservableValueBase<Object>()
		{
			@Override
			public Object getValue()
			{
				final AnywareList<? extends Anyware> machineList = tableWL.getSelectionModel().getSelectedItem();
				return Optional.ofNullable(p.getValue().getCloneof()).map(cloneof -> machineList.containsName(cloneof) ? machineList.getByName(cloneof) : cloneof).orElse(null);
			}
		});
		tableWMRomOf.setSortable(false);
		tableWMRomOf.setPrefWidth(100);
		tableWMRomOf.setCellFactory(tableWMCloneOf.getCellFactory());
		tableWMRomOf.setCellValueFactory(p -> new ObservableValueBase<Object>()
		{
			@Override
			public Object getValue()
			{
				if(p.getValue() instanceof Machine m)
				{
					final AnywareList<? extends Anyware> machineList = tableWL.getSelectionModel().getSelectedItem();
					return Optional.ofNullable(m.getRomof()).filter(romof -> !romof.equals(m.getCloneof())).map(romof -> machineList.containsName(romof) ? machineList.getByName(romof) : romof).orElse(null);
				}
				return null;
			}
		});
		tableWMSampleOf.setSortable(false);
		tableWMSampleOf.setPrefWidth(100);
		tableWMSampleOf.setCellFactory(p -> new TableCell<Anyware, Object>()
		{
			private static final Image folderClosedGreen = MainFrame.getIcon("/jrm/resicons/folder_closed_green.png"); //$NON-NLS-1$
			private static final Image folderClosedOrange = MainFrame.getIcon("/jrm/resicons/folder_closed_orange.png"); //$NON-NLS-1$
			private static final Image folderClosedRed = MainFrame.getIcon("/jrm/resicons/folder_closed_red.png"); //$NON-NLS-1$
			private static final Image folderClosedGray = MainFrame.getIcon("/jrm/resicons/folder_closed_gray.png"); //$NON-NLS-1$

			@Override
			protected void updateItem(Object item, boolean empty)
			{
				if (item==null || empty)
				{
					setText("");
					setGraphic(null);
				}
				else
				{
					if(item instanceof Samples s)
					{
						setGraphic(new ImageView(switch (s.getStatus())
						{
							case COMPLETE -> folderClosedGreen;
							case PARTIAL -> folderClosedOrange;
							case MISSING -> folderClosedRed;
							case UNKNOWN -> folderClosedGray;
							default -> folderClosedGray;
						}));
						setText(s.getBaseName());
					}
					else
					{
						setGraphic(new ImageView(folderClosedGray));
						setText(item.toString());
					}
				}
				setFont(new Font(10));
			}
		});
		tableWMSampleOf.setCellValueFactory(p -> new ObservableValueBase<Object>()
		{
			@Override
			public Object getValue()
			{
				if(p.getValue() instanceof Machine m)
				{
					final AnywareList<? extends Anyware> awList = tableWL.getSelectionModel().getSelectedItem();
					if(awList instanceof MachineList machineList)
						return Optional.ofNullable(m.getSampleof()).map(sampleof -> machineList.samplesets.containsName(sampleof) ? machineList.samplesets.getByName(sampleof) : sampleof).orElse(null);
				}
				return null;
			}
		});
		tableWMSelected.setResizable(false);
		tableWMSelected.setSortable(false);
		tableWMSelected.setPrefWidth(20);
		tableWMSelected.setCellValueFactory(p -> {
			final var aw = p.getValue();
			final var checkBox = new CheckBox();
			checkBox.selectedProperty().setValue(aw.isSelected());
			checkBox.selectedProperty().addListener((ov, oldVal, newVal) -> aw.setSelected(newVal));
			return new SimpleObjectProperty<>(checkBox);
		});
		tableWL.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			tableW.getColumns().clear();
			final var list = FXCollections.<Anyware>observableArrayList();
			if(newValue instanceof MachineList ml)
			{
				tableW.getColumns().add(tableWMStatus);
				tableW.getColumns().add(tableWMName);
				tableW.getColumns().add(tableWMDescription);
				tableW.getColumns().add(tableWMHave);
				tableW.getColumns().add(tableWMCloneOf);
				tableW.getColumns().add(tableWMRomOf);
				tableW.getColumns().add(tableWMSampleOf);
				tableW.getColumns().add(tableWMSelected);
				for (final var w : ml.getFilteredList())
					list.add(w);
			}
			else if(newValue instanceof SoftwareList sl)
			{
				tableW.getColumns().add(tableWSStatus);
				tableW.getColumns().add(tableWSName);
				tableW.getColumns().add(tableWSDescription);
				tableW.getColumns().add(tableWSHave);
				tableW.getColumns().add(tableWSCloneOf);
				tableW.getColumns().add(tableWSSelected);
				for (final var w : sl.getFilteredList())
					list.add(w);
			}
			tableW.setItems(list);
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
