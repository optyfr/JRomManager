package jrm.fx.ui.profile;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jrm.fx.ui.MainFrame;
import jrm.fx.ui.controls.Dialogs;
import jrm.fx.ui.profile.filter.Keywords;
import jrm.fx.ui.progress.ProgressTask;
import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.ProfileSettingsEnum;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.AnywareStatus;
import jrm.profile.data.Disk;
import jrm.profile.data.Entity;
import jrm.profile.data.EntityBase;
import jrm.profile.data.EntityStatus;
import jrm.profile.data.Machine;
import jrm.profile.data.MachineList;
import jrm.profile.data.Rom;
import jrm.profile.data.Sample;
import jrm.profile.data.Samples;
import jrm.profile.data.Software;
import jrm.profile.data.SoftwareList;
import jrm.profile.filter.Keywords.KFCallBack;
import jrm.profile.manager.Export;
import jrm.profile.manager.Export.ExportType;
import jrm.profile.manager.ProfileNFOMame;
import jrm.profile.manager.ProfileNFOMame.MameStatus;
import jrm.security.Session;
import jrm.security.Sessions;
import lombok.val;

public class ProfileViewerController implements Initializable
{
	private static final String D_OF_D_FMT = "%d/%d";

	@FXML private TableView<AnywareList<? extends Anyware>> tableWL;
	@FXML private TableColumn<AnywareList<? extends Anyware>, AnywareList<? extends Anyware>> tableWLName;
	@FXML private TableColumn<AnywareList<? extends Anyware>, String> tableWLDesc;
	@FXML private TableColumn<AnywareList<? extends Anyware>, String> tableWLHave;
	@FXML private ToggleButton toggleWLUnknown;
	@FXML private ToggleButton toggleWLMissing;
	@FXML private ToggleButton toggleWLPartial;
	@FXML private ToggleButton toggleWLComplete;
	@FXML private TableView<Anyware> tableW;
	private final TableColumn<Anyware, Anyware> tableWMStatus = new TableColumn<>(Messages.getString("MachineListRenderer.Status"));
	private final TableColumn<Anyware, Machine> tableWMName = new TableColumn<>(Messages.getString("MachineListRenderer.Name"));
	private final TableColumn<Anyware, String> tableWMDescription = new TableColumn<>(Messages.getString("MachineListRenderer.Description"));
	private final TableColumn<Anyware, String> tableWMHave = new TableColumn<>(Messages.getString("MachineListRenderer.Have"));
	private final TableColumn<Anyware, Object> tableWMCloneOf = new TableColumn<>(Messages.getString("MachineListRenderer.CloneOf"));
	private final TableColumn<Anyware, Object> tableWMRomOf = new TableColumn<>(Messages.getString("MachineListRenderer.RomOf"));
	private final TableColumn<Anyware, Object> tableWMSampleOf = new TableColumn<>(Messages.getString("MachineListRenderer.SampleOf"));
	private final TableColumn<Anyware, CheckBox> tableWMSelected = new TableColumn<>(Messages.getString("MachineListRenderer.Selected"));
	private final TableColumn<Anyware, Anyware> tableWSStatus = new TableColumn<>(Messages.getString("SoftwareListRenderer.Status"));
	private final TableColumn<Anyware, String> tableWSName = new TableColumn<>(Messages.getString("SoftwareListRenderer.Name"));
	private final TableColumn<Anyware, String> tableWSDescription = new TableColumn<>(Messages.getString("SoftwareListRenderer.Description"));
	private final TableColumn<Anyware, String> tableWSHave = new TableColumn<>(Messages.getString("SoftwareListRenderer.Have"));
	private final TableColumn<Anyware, Object> tableWSCloneOf = new TableColumn<>(Messages.getString("SoftwareListRenderer.CloneOf"));
	private final TableColumn<Anyware, CheckBox> tableWSSelected = new TableColumn<>(Messages.getString("SoftwareListRenderer.Selected"));
	@FXML private ToggleButton toggleWUnknown;
	@FXML private ToggleButton toggleWMissing;
	@FXML private ToggleButton toggleWPartial;
	@FXML private ToggleButton toggleWComplete;
	@FXML private TextField search;
	@FXML private TableView<EntityBase> tableEntity;
	@FXML private TableColumn<EntityBase, EntityBase> tableEntityStatus;
	@FXML private TableColumn<EntityBase, EntityBase> tableEntityName;
	@FXML private TableColumn<EntityBase, Long> tableEntitySize;
	@FXML private TableColumn<EntityBase, String> tableEntityCRC;
	@FXML private TableColumn<EntityBase, String> tableEntityMD5;
	@FXML private TableColumn<EntityBase, String> tableEntitySHA1;
	@FXML private TableColumn<EntityBase, String> tableEntityMergeName;
	@FXML private TableColumn<EntityBase, Entity.Status> tableEntityDumpStatus;
	@FXML private ToggleButton toggleEntityUnknown;
	@FXML private ToggleButton toggleEntityKO;
	@FXML private ToggleButton toggleEntityOK;

	@FXML private ContextMenu menuWL;
	@FXML private MenuItem mntmFilteredAsLogiqxDat;
	@FXML private MenuItem mntmFilteredAsMameDat;
	@FXML private MenuItem mntmFilteredAsSoftwareLists;
	@FXML private MenuItem mntmAllAsLogiqxDat;
	@FXML private MenuItem mntmAllAsMameDat;
	@FXML private MenuItem mntmAllAsSoftwareLists;
	@FXML private MenuItem mntmSelectedFilteredAsSoftwareLists;
	@FXML private MenuItem mntmSelectedAsSoftwareLists;
	@FXML private ContextMenu menuW;
	@FXML private MenuItem mntmSelectByKeywords;
	@FXML private MenuItem mntmSelectAll;
	@FXML private MenuItem mntmSelectNone;
	@FXML private MenuItem mntmSelectInvert;
	@FXML private ContextMenu menuEntity;
	@FXML private MenuItem mntmCopyCrc;
	@FXML private MenuItem mntmCopySha1;
	@FXML private MenuItem mntmCopyName;
	@FXML private MenuItem mntmSearchWeb;
	
	private static final Image diskMultipleGreen = MainFrame.getIcon("/jrm/resicons/disk_multiple_green.png"); //$NON-NLS-1$
	private static final Image diskMultipleOrange = MainFrame.getIcon("/jrm/resicons/disk_multiple_orange.png"); //$NON-NLS-1$
	private static final Image diskMultipleRed = MainFrame.getIcon("/jrm/resicons/disk_multiple_red.png"); //$NON-NLS-1$
	private static final Image diskMultipleGray = MainFrame.getIcon("/jrm/resicons/disk_multiple_gray.png"); //$NON-NLS-1$
	private static final Image folderClosedGreen = MainFrame.getIcon("/jrm/resicons/folder_closed_green.png"); //$NON-NLS-1$
	private static final Image folderClosedOrange = MainFrame.getIcon("/jrm/resicons/folder_closed_orange.png"); //$NON-NLS-1$
	private static final Image folderClosedRed = MainFrame.getIcon("/jrm/resicons/folder_closed_red.png"); //$NON-NLS-1$
	private static final Image folderClosedGray = MainFrame.getIcon("/jrm/resicons/folder_closed_gray.png"); //$NON-NLS-1$
	private static final Image bulletGreen = MainFrame.getIcon("/jrm/resicons/icons/bullet_green.png"); //$NON-NLS-1$
	private static final Image bulletRed = MainFrame.getIcon("/jrm/resicons/icons/bullet_red.png"); //$NON-NLS-1$
	private static final Image bulletBlack = MainFrame.getIcon("/jrm/resicons/icons/bullet_black.png"); //$NON-NLS-1$

	private final Map<String,String> haveCache = new HashMap<>();
	
	private Session session = Sessions.getSingleSession();
	
	private static final Font monospaced = Font.font("Monospaced", 10);

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		initTableWL();
		initTableW();
		initTableE();
	}

	/**
	 * 
	 */
	private void initTableE()
	{
		tableEntityStatus.setCellFactory(p -> new TableCell<EntityBase, EntityBase>()
		{
			@Override
			protected void updateItem(EntityBase item, boolean empty)
			{
				if (item==null || empty)
				{
					setText("");
					setGraphic(null);
				}
				else
				{
					setGraphic(new ImageView(switch(item.getStatus())
					{
						case KO -> bulletRed;
						case OK -> bulletGreen;
						case UNKNOWN -> bulletBlack;
						default -> bulletBlack;
					}));
				}
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				setFont(new Font(10));
			}
		});
		tableEntityStatus.setCellValueFactory(p -> new ObservableValueBase<EntityBase>()
		{
			@Override
			public EntityBase getValue()
			{
				return p.getValue();
			}
		});
		tableEntityName.setCellFactory(p -> new TableCell<EntityBase, EntityBase>()
		{
			final Image romSmall = MainFrame.getIcon("/jrm/resicons/rom_small.png"); //$NON-NLS-1$
			final Image drive = MainFrame.getIcon("/jrm/resicons/icons/drive.png"); //$NON-NLS-1$
			final Image sound = MainFrame.getIcon("/jrm/resicons/icons/sound.png"); //$NON-NLS-1$
			
			@Override
			protected void updateItem(EntityBase item, boolean empty)
			{
				if (item==null || empty)
				{
					setText("");
					setGraphic(null);
				}
				else
				{
					setText(item.getBaseName());
					if(item instanceof Rom)
						setGraphic(new ImageView(romSmall));
					else if(item instanceof Disk)
						setGraphic(new ImageView(drive));
					else if(item instanceof Sample)
						setGraphic(new ImageView(sound));
				}
				setFont(new Font(10));
			}
		});
		tableEntityName.setCellValueFactory(tableEntityStatus.getCellValueFactory());
		tableEntitySize.setMinWidth(getWidth(12));
		tableEntitySize.setPrefWidth(tableEntitySize.getMinWidth());
		tableEntitySize.setCellFactory(p -> new TableCell<EntityBase, Long>()
		{
			@Override
			protected void updateItem(Long item, boolean empty)
			{
				if (item == null || empty)
					setText("");
				else
					setText(item.toString());
				setFont(new Font(10));
				setTextAlignment(TextAlignment.RIGHT);
				setAlignment(Pos.CENTER_RIGHT);
				setGraphic(null);
			}
		});
		tableEntitySize.setCellValueFactory(p -> new ObservableValueBase<Long>()
		{
			@Override
			public Long getValue()
			{
				if(p.getValue() instanceof Rom r)
					return r.getSize();
				return null;
			}
		});
		tableEntityCRC.setMinWidth(getWidth(10, monospaced));
		tableEntityCRC.setPrefWidth(tableEntityCRC.getMinWidth());
		tableEntityCRC.setCellFactory(p -> new TableCell<EntityBase, String>()
		{
			@Override
			protected void updateItem(String item, boolean empty)
			{
				if (item == null || empty)
					setText("");
				else
					setText(item);
				setFont(monospaced);
				setGraphic(null);
			}
		});
		tableEntityCRC.setCellValueFactory(p -> new ObservableValueBase<String>()
		{
			@Override
			public String getValue()
			{
				if(p.getValue() instanceof Rom r)
					return r.getCrc();
				else if(p.getValue() instanceof Disk d)
					return d.getCrc();
				return null;
			}
		});
		tableEntityMD5.setMinWidth(getWidth(34, monospaced));
		tableEntityMD5.setPrefWidth(tableEntityMD5.getMinWidth());
		tableEntityMD5.setCellFactory(p -> new TableCell<EntityBase, String>()
		{
			@Override
			protected void updateItem(String item, boolean empty)
			{
				if (item == null || empty)
					setText("");
				else
					setText(item);
				setFont(monospaced);
				setGraphic(null);
			}
		});
		tableEntityMD5.setCellValueFactory(p -> new ObservableValueBase<String>()
		{
			@Override
			public String getValue()
			{
				if(p.getValue() instanceof Rom r)
					return r.getMd5();
				else if(p.getValue() instanceof Disk d)
					return d.getMd5();
				return null;
			}
		});
		tableEntitySHA1.setMinWidth(getWidth(42, monospaced));
		tableEntitySHA1.setPrefWidth(tableEntitySHA1.getMinWidth());
		tableEntitySHA1.setCellFactory(p -> new TableCell<EntityBase, String>()
		{
			@Override
			protected void updateItem(String item, boolean empty)
			{
				if (item == null || empty)
					setText("");
				else
					setText(item);
				setFont(monospaced);
				setGraphic(null);
			}
		});
		tableEntitySHA1.setCellValueFactory(p -> new ObservableValueBase<String>()
		{
			@Override
			public String getValue()
			{
				if(p.getValue() instanceof Rom r)
					return r.getSha1();
				else if(p.getValue() instanceof Disk d)
					return d.getSha1();
				return null;
			}
		});
		tableEntityMergeName.setCellValueFactory(p -> new ObservableValueBase<String>()
		{
			@Override
			public String getValue()
			{
				if(p.getValue() instanceof Rom r)
					return r.getMerge();
				else if(p.getValue() instanceof Disk d)
					return d.getMerge();
				return null;
			}
		});
		tableEntityDumpStatus.setCellFactory(p -> new TableCell<EntityBase, Entity.Status>()
		{
			private static final Image verified = MainFrame.getIcon("/jrm/resicons/icons/star.png"); //$NON-NLS-1$
			private static final Image good = MainFrame.getIcon("/jrm/resicons/icons/tick.png"); //$NON-NLS-1$
			private static final Image baddump = MainFrame.getIcon("/jrm/resicons/icons/delete.png"); //$NON-NLS-1$
			private static final Image nodump = MainFrame.getIcon("/jrm/resicons/icons/error.png"); //$NON-NLS-1$
			
			@Override
			protected void updateItem(Entity.Status item, boolean empty)
			{
				if (item==null || empty)
				{
					setText("");
					setGraphic(null);
				}
				else
				{
					setGraphic(new ImageView(switch(item)
					{
						case baddump -> baddump;
						case good -> good;
						case nodump -> nodump;
						case verified -> verified;
						default -> null;
					}));
				}
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				setFont(new Font(10));
			}
		});
		tableEntityDumpStatus.setCellValueFactory(p -> new ObservableValueBase<Entity.Status>()
		{
			@Override
			public Entity.Status getValue()
			{
				if(p.getValue() instanceof Rom r)
					return r.getDumpStatus();
				else if(p.getValue() instanceof Disk d)
					return d.getDumpStatus();
				return null;
			}
		});
		toggleEntityUnknown.setGraphic(new ImageView(bulletBlack));
		toggleEntityKO.setGraphic(new ImageView(bulletRed));
		toggleEntityOK.setGraphic(new ImageView(bulletGreen));
		menuEntity.setOnShowing(e -> {
			final boolean has_selected_entity = tableEntity.getSelectionModel().getSelectedItem()!=null;
			mntmCopyCrc.setDisable(!has_selected_entity);
			mntmCopySha1.setDisable(!has_selected_entity);
			mntmCopyName.setDisable(!has_selected_entity);
			mntmSearchWeb.setDisable(!has_selected_entity);
		});
	}

	/**
	 * @param ware
	 * @param profile
	 * @param mame
	 * @return
	 */
	private void getMameArgsMachine(final Anyware ware, final Profile profile, final ProfileNFOMame mame, ArrayList<String> args)
	{
		final List<String> rompaths = new ArrayList<>(Collections.singletonList(profile.getProperty(ProfileSettingsEnum.roms_dest_dir))); //$NON-NLS-1$ //$NON-NLS-2$
		if (Boolean.TRUE.equals(profile.getProperty(ProfileSettingsEnum.disks_dest_dir_enabled, Boolean.class))) //$NON-NLS-1$
			rompaths.add(profile.getProperty(ProfileSettingsEnum.disks_dest_dir)); //$NON-NLS-1$ //$NON-NLS-2$
		args.add(mame.getFile().getAbsolutePath());
		args.add(ware.getBaseName());
		args.add("-homepath");
		args.add(mame.getFile().getParent());
		args.add("-rompath");
		args.add(rompaths.stream().collect(Collectors.joining(";")));
	}

	/**
	 * @param ware
	 * @param profile
	 * @param mame
	 * @param args
	 * @return
	 * @throws HeadlessException
	 */
	private void getMameArgsSofware(final Anyware ware, final Profile profile, final ProfileNFOMame mame, ArrayList<String> args) throws HeadlessException
	{
		final List<String> rompaths = new ArrayList<>(Collections.singletonList(profile.getProperty(ProfileSettingsEnum.roms_dest_dir))); //$NON-NLS-1$ //$NON-NLS-2$
		if (Boolean.TRUE.equals(profile.getProperty(ProfileSettingsEnum.swroms_dest_dir_enabled, Boolean.class))) //$NON-NLS-1$
			rompaths.add(profile.getProperty(ProfileSettingsEnum.swroms_dest_dir)); //$NON-NLS-1$ //$NON-NLS-2$
		if (Boolean.TRUE.equals(profile.getProperty(ProfileSettingsEnum.disks_dest_dir_enabled, Boolean.class))) //$NON-NLS-1$
			rompaths.add(profile.getProperty(ProfileSettingsEnum.disks_dest_dir)); //$NON-NLS-1$ //$NON-NLS-2$
		if (Boolean.TRUE.equals(profile.getProperty(ProfileSettingsEnum.swdisks_dest_dir_enabled, Boolean.class))) //$NON-NLS-1$
			rompaths.add(profile.getProperty(ProfileSettingsEnum.swdisks_dest_dir)); //$NON-NLS-1$ //$NON-NLS-2$
		Log.debug(()->((Software) ware).getSl().getBaseName() + ", " + ((Software) ware).getCompatibility()); //$NON-NLS-1$
		final var machines = new ChoiceDialog<Machine>(null, profile.getMachineListList().getSortedMachines(((Software) ware).getSl().getBaseName(), ((Software) ware).getCompatibility()));
		final var machine = machines.showAndWait();
		machine.ifPresent(m -> {
			final var device = new StringBuilder(); //$NON-NLS-1$
			for(final var dev : m.getDevices())
			{
				if (Objects.equals(((Software) ware).getParts().get(0).getIntrface(), dev.getIntrface()) && dev.getInstance() != null)
				{
					device.append("-" + dev.getInstance().getName()); //$NON-NLS-1$
					break;
				}
			}
			Log.debug(()->"-> " + m.getBaseName() + " " + device + " " + ware.getBaseName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			args.add(mame.getFile().getAbsolutePath());
			args.add(m.getBaseName());
			args.add(device.toString());
			args.add(ware.getBaseName());
			args.add("-homepath");
			args.add(mame.getFile().getParent());
			args.add("-rompath");
			args.add(rompaths.stream().collect(Collectors.joining(";")));
		});
	}

	/**
	 * @param ware
	 * @param profile
	 * @throws HeadlessException
	 */
	private void launchMame(final Anyware ware, final Profile profile) throws HeadlessException
	{
		final ProfileNFOMame mame = profile.getNfo().getMame();
		final var args = new ArrayList<String>();
		if (ware instanceof Software)
		{
			getMameArgsSofware(ware, profile, mame, args);
		}
		else
		{
			getMameArgsMachine(ware, profile, mame, args);
		}
		if (!args.isEmpty())
		{
			final ProcessBuilder pb = new ProcessBuilder(args).directory(mame.getFile().getParentFile()).redirectErrorStream(true).redirectOutput(new File(mame.getFile().getParentFile(), "JRomManager.log")); //$NON-NLS-1$
			try
			{
				pb.start().waitFor();
			}
			catch (IOException e1)
			{
				Dialogs.showError(e1);
			}
			catch (InterruptedException e1)
			{
				Dialogs.showError(e1);
				Thread.currentThread().interrupt();
			}
		}
	}
	/**
	 * 
	 */
	private void initTableW()
	{
		tableW.setFixedCellSize(18);
		tableWMStatus.setResizable(false);
		tableWMStatus.setSortable(false);
		tableWMStatus.setPrefWidth(20);
		tableWMStatus.setCellFactory(p -> new TableCell<Anyware, Anyware>()
		{
			@Override
			protected void updateItem(Anyware item, boolean empty)
			{
				if (item==null || empty)
					setGraphic(null);
				else
					setGraphic(new ImageView(getStatusIcon(item.getStatus())));
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				setText("");
			}
		});
		tableWMStatus.setCellValueFactory(p -> new ObservableValueBase<Anyware>()
		{
			@Override
			public Anyware getValue()
			{
				return p.getValue();
			}
		});
		tableWMName.setMinWidth(50);
		tableWMName.setPrefWidth(100);
		tableWMName.setCellFactory(p -> {
			final var cell = new TableCell<Anyware, Machine>() {
				private static final Image applicationOSXTerminal = MainFrame.getIcon("/jrm/resicons/icons/application_osx_terminal.png"); //$NON-NLS-1$
				private static final Image computer = MainFrame.getIcon("/jrm/resicons/icons/computer.png"); //$NON-NLS-1$
				private static final Image wrench = MainFrame.getIcon("/jrm/resicons/icons/wrench.png"); //$NON-NLS-1$
				private static final Image joystick = MainFrame.getIcon("/jrm/resicons/icons/joystick.png"); //$NON-NLS-1$

				@Override
				protected void updateItem(Machine item, boolean empty) {
					if (empty) {
						setText("");
						setGraphic(null);
					} else {
						setText(item.getBaseName());
						setUserData(item);
						setTooltip(new Tooltip(item.getName()));
						if (item.isIsbios())
							setGraphic(new ImageView(applicationOSXTerminal));
						else if (item.isIsdevice())
							setGraphic(new ImageView(computer));
						else if (item.isIsmechanical())
							setGraphic(new ImageView(wrench));
						else
							setGraphic(new ImageView(joystick));
					}
					setFont(new Font(10));
				}
			};
			cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
				if (event.getClickCount() > 1 && (event.getSource() instanceof TableCell<?, ?> c
						&& (c.getUserData() instanceof Machine ware))) {
					if (ware.getStatus() == AnywareStatus.COMPLETE) {
						if (session.getCurrProfile() != null) {
							final var profile = session.getCurrProfile();
							if (profile.getNfo().getMame().getStatus() == MameStatus.UPTODATE)
								launchMame(ware, profile);
							else
								Dialogs.showAlert(
										String.format(Messages.getString("ProfileViewer.MameNotAvailableOrObsolete"),
												profile.getNfo().getMame().getStatus()));
						} else
							Dialogs.showAlert(Messages.getString("ProfileViewer.NoProfile"));
					} else
						Dialogs.showAlert(String.format(Messages.getString("ProfileViewer.CantLaunchIncompleteSet"), ware.getStatus()));
				}
			});
			return cell;
		});
		tableWMName.setCellValueFactory(p -> new ObservableValueBase<Machine>()
		{
			@Override
			public Machine getValue()
			{
				if(p.getValue() instanceof Machine m)
					return m;
				return null;
			}
		});
		tableWMName.setSortable(true);
		tableWMDescription.setMinWidth(100);
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
		tableWMDescription.setSortable(true);
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
					return String.format(D_OF_D_FMT, machine.countHave(), machine.countAll());
				return null;
			}
		});
		tableWMCloneOf.setSortable(false);
		tableWMCloneOf.setMinWidth(50);
		tableWMCloneOf.setPrefWidth(100);
		tableWMCloneOf.setCellFactory(p -> {
			final var cell = new TableCell<Anyware, Object>()
			{
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
							setGraphic(new ImageView(getStatusIcon(aw.getStatus())));
							setUserData(aw);
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
			};
			cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
				if (event.getClickCount() > 1 && event.getSource() instanceof TableCell<?, ?> c && (c.getUserData() instanceof Anyware ware)) {
					final var sm = tableW.getSelectionModel();
					sm.clearSelection();
					sm.select(ware);
					tableW.scrollTo(ware);
				}	
			});
			return cell;
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
		tableWMRomOf.setMinWidth(50);
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
		tableWMSampleOf.setMinWidth(50);
		tableWMSampleOf.setPrefWidth(100);
		tableWMSampleOf.setCellFactory(p -> new TableCell<Anyware, Object>()
		{
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
						setGraphic(new ImageView(getStatusIcon(s.getStatus())));
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
		tableWSStatus.setResizable(false);
		tableWSStatus.setSortable(false);
		tableWSStatus.setPrefWidth(20);
		tableWSStatus.setCellFactory(tableWMStatus.getCellFactory());
		tableWSStatus.setCellValueFactory(tableWMStatus.getCellValueFactory());
		tableWSName.setSortable(true);
		tableWSName.setMinWidth(50);
		tableWSName.setPrefWidth(100);
		tableWSName.setCellFactory(tableWMDescription.getCellFactory());
		tableWSName.setCellValueFactory(p -> new ObservableValueBase<String>()
		{
			@Override
			public String getValue()
			{
				return p.getValue().getBaseName();
			}
		});
		tableWSDescription.setSortable(true);
		tableWSDescription.setMinWidth(200);
		tableWSDescription.setPrefWidth(400);
		tableWSDescription.setCellFactory(tableWMDescription.getCellFactory());
		tableWSDescription.setCellValueFactory(tableWMDescription.getCellValueFactory());
		tableWSHave.setResizable(false);
		tableWSHave.setSortable(false);
		tableWSHave.setPrefWidth(45);
		tableWSHave.setCellFactory(tableWMHave.getCellFactory());
		tableWSHave.setCellValueFactory(p -> new ObservableValueBase<String>()
		{
			@Override
			public String getValue()
			{
				if(p.getValue() instanceof Software software)
					return String.format(D_OF_D_FMT, software.countHave(), software.countAll());
				return null;
			}
		});
		tableWSCloneOf.setSortable(false);
		tableWSCloneOf.setMinWidth(50);
		tableWSCloneOf.setPrefWidth(100);
		tableWSCloneOf.setCellFactory(tableWMCloneOf.getCellFactory());
		tableWSCloneOf.setCellValueFactory(p -> new ObservableValueBase<Object>()
		{
			@Override
			public Object getValue()
			{
				final AnywareList<? extends Anyware> softwareList = tableWL.getSelectionModel().getSelectedItem();
				return p.getValue().getCloneof() != null ? softwareList.getByName(p.getValue().getCloneof()) : null;
			}
		});
		tableWSSelected.setResizable(false);
		tableWSSelected.setSortable(false);
		tableWSSelected.setPrefWidth(20);
		tableWSSelected.setCellValueFactory(tableWMSelected.getCellValueFactory());
		tableW.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> reloadE(newValue));
		toggleWUnknown.setGraphic(new ImageView(folderClosedGray));
		toggleWMissing.setGraphic(new ImageView(folderClosedRed));
		toggleWPartial.setGraphic(new ImageView(folderClosedOrange));
		toggleWComplete.setGraphic(new ImageView(folderClosedGreen));
		
		search.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(searchPredicate(newValue)));
	}

	/**
	 * @param newValue
	 * @return
	 */
	private Predicate<? super Anyware> searchPredicate(String newValue)
	{
		return t -> {
			if (newValue == null || newValue.isEmpty())
				return true;
			final var lcase = newValue.toLowerCase();
			return t.getBaseName().toLowerCase().contains(lcase) || t.getDescription().toString().toLowerCase().contains(lcase);
		};
	}

	
	
	/**
	 * 
	 */
	private void initTableWL()
	{
		tableWL.setFixedCellSize(18);
		tableWLName.setCellFactory(p -> new TableCellWLName());
		tableWLName.setCellValueFactory(ValueWLName::new);
		tableWLName.setSortable(true);
		tableWLDesc.setCellFactory(p -> new TableCellWLDesc());
		tableWLDesc.setCellValueFactory(ValueWLDesc::new);
		tableWLHave.setCellFactory(p -> new TableCellWLHave());
		tableWLHave.setCellValueFactory(ValueWLHave::new);
		tableWL.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> reloadW(newValue));
		toggleWLUnknown.setGraphic(new ImageView(diskMultipleGray));
		toggleWLMissing.setGraphic(new ImageView(diskMultipleRed));
		toggleWLPartial.setGraphic(new ImageView(diskMultipleOrange));
		toggleWLComplete.setGraphic(new ImageView(diskMultipleGreen));
		menuWL.setOnShowing(e -> {
			final boolean has_machines = session.getCurrProfile().getMachineListList().getList().stream().mapToInt(ml -> ml.getList().size()).sum() > 0;
			final boolean has_filtered_machines = session.getCurrProfile().getMachineListList().getFilteredStream().mapToInt(m -> (int) m.countAll()).sum() > 0;
			final boolean has_selected_swlist = tableWL.getSelectionModel().getSelectedItems().size() == 1 && tableWL.getSelectionModel().getSelectedItem() instanceof SoftwareList;
			mntmAllAsMameDat.setDisable(!has_machines);
			mntmAllAsLogiqxDat.setDisable(!has_machines);
			mntmAllAsSoftwareLists.setDisable(session.getCurrProfile().getMachineListList().getSoftwareListList().isEmpty());
			mntmFilteredAsMameDat.setDisable(!has_filtered_machines);
			mntmFilteredAsLogiqxDat.setDisable(!has_filtered_machines);
			mntmFilteredAsSoftwareLists.setDisable(session.getCurrProfile().getMachineListList().getSoftwareListList().getFilteredStream().count() == 0);
			mntmSelectedAsSoftwareLists.setDisable(!has_selected_swlist);
			mntmSelectedFilteredAsSoftwareLists.setDisable(!has_selected_swlist);
		});
	}

	/**
	 * @param newValue
	 */
	private void reloadE(Anyware newValue)
	{
		final var list = FXCollections.<EntityBase>observableArrayList();
		if (newValue != null)
		{
			newValue.resetCache();
			for (final var e : newValue.getEntities())
				list.add(e);
		}
		tableEntity.setItems(list);
	}

	private FilteredList<Anyware> filteredData;
	
	/**
	 * 
	 */
	private void reloadW(AnywareList<? extends Anyware> newValue)
	{
		tableW.getColumns().clear();
		final var list = FXCollections.<Anyware>observableArrayList();
		if (newValue != null)
		{
			newValue.resetCache();
			if (newValue instanceof MachineList ml)
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
			else if (newValue instanceof SoftwareList sl)
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
		}
		filteredData = new FilteredList<>(list, searchPredicate(search.getText()));
		tableW.setItems(filteredData);
		tableW.getSelectionModel().select(0);
	}

	@FXML void diskMultipleFilter(ActionEvent e)
	{
		setFilterWL(toggleWLUnknown.isSelected(), toggleWLMissing.isSelected(), toggleWLPartial.isSelected(), toggleWLComplete.isSelected());
	}

	@FXML void folderFilter(ActionEvent e)
	{
		setFilterW(toggleWUnknown.isSelected(), toggleWMissing.isSelected(), toggleWPartial.isSelected(), toggleWComplete.isSelected());
	}

	@FXML void bulletFilter(ActionEvent e)
	{
		setFilterE(toggleEntityUnknown.isSelected(), toggleEntityKO.isSelected(), toggleEntityOK.isSelected());
	}

	private void setFilterWL(final boolean unknown, final boolean missing, final boolean partial, final boolean complete)
	{
		final EnumSet<AnywareStatus> filter = EnumSet.noneOf(AnywareStatus.class);
		if (unknown)
			filter.add(AnywareStatus.UNKNOWN);
		if (missing)
			filter.add(AnywareStatus.MISSING);
		if (partial)
			filter.add(AnywareStatus.PARTIAL);
		if (complete)
			filter.add(AnywareStatus.COMPLETE);
		session.getCurrProfile().setFilterListLists(filter);
		reset(session.getCurrProfile());
	}

	private void setFilterW(final boolean unknown, final boolean missing, final boolean partial, final boolean complete)
	{
		final EnumSet<AnywareStatus> filter = EnumSet.noneOf(AnywareStatus.class);
		if (unknown)
			filter.add(AnywareStatus.UNKNOWN);
		if (missing)
			filter.add(AnywareStatus.MISSING);
		if (partial)
			filter.add(AnywareStatus.PARTIAL);
		if (complete)
			filter.add(AnywareStatus.COMPLETE);
		session.getCurrProfile().setFilterList(filter);
		final var item = tableWL.getSelectionModel().getSelectedItem();
		if(item!=null)
			reloadW(item);
	}

	private void setFilterE(final boolean unknown, final boolean missing, final boolean complete)
	{
		final EnumSet<EntityStatus> filter = EnumSet.noneOf(EntityStatus.class);
		if (unknown)
			filter.add(EntityStatus.UNKNOWN);
		if (missing)
			filter.add(EntityStatus.KO);
		if (complete)
			filter.add(EntityStatus.OK);
		session.getCurrProfile().setFilterEntities(filter);
		final var item = tableW.getSelectionModel().getSelectedItem();
		if(item!=null)
			reloadE(item);
	}

	private static Image getStatusIcon(AnywareStatus status)
	{
		return switch (status)
		{
			case COMPLETE -> folderClosedGreen;
			case PARTIAL -> folderClosedOrange;
			case MISSING -> folderClosedRed;
			case UNKNOWN -> folderClosedGray;
			default -> folderClosedGray;
		};
	}

	private double getWidth(int digits)
	{
		return getWidth(digits, null);
	}

	private double getWidth(int digits, Font font)
	{
		final var format = "%0" + digits + "d";
		final var text = new Text(String.format(format, 0));
		text.setFont(font==null?new Font(10):font);
		return text.getBoundsInLocal().getWidth();
	}

	
	void clear()
	{
		tableEntity.setItems(FXCollections.observableArrayList());
		tableW.setItems(FXCollections.observableArrayList());
		tableWL.setItems(FXCollections.observableArrayList());
		haveCache.clear();
	}

	void reload()
	{
		tableWL.refresh();
		haveCache.clear();
		tableW.refresh();
		tableEntity.refresh();
	}

	void reset(Profile profile)
	{
		final var selected = tableWL.getSelectionModel().getSelectedItem();
		clear();
		final var wl = FXCollections.<AnywareList<? extends Anyware>>observableArrayList();
		profile.getMachineListList().resetCache();
		for (final var w : profile.getMachineListList().getFilteredList())
			wl.add(w);
		profile.getMachineListList().getSoftwareListList().resetCache();
		for (final var w : profile.getMachineListList().getSoftwareListList().getFilteredList())
			wl.add(w);
		tableWL.setItems(wl);
		if (selected != null)
		{
			int index = tableWL.getItems().indexOf(selected);
			if(index>=0)
				tableWL.getSelectionModel().select(index);
		}
		else
			tableWL.getSelectionModel().select(0);
		tableWL.refresh();
	//	reloadW(tableWL.getSelectionModel().getSelectedItem());
	}

	private class KW extends jrm.profile.filter.Keywords
	{

		@Override
		protected void showFilter(String[] keywords, KFCallBack callback) {
			try {
				new Keywords((ProfileViewer) tableWL.getScene().getWindow(), keywords, tableWL.getSelectionModel().getSelectedItem(), callback);
			} catch (URISyntaxException | IOException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		protected void updateList() {
			tableW.refresh();
		}
		
	}

	@FXML private void selectByKeywords(ActionEvent e)
	{
		final var lst = tableWL.getSelectionModel().getSelectedItem();
		new KW().filter(lst);
	}

	@FXML private void selectNone(ActionEvent e)
	{
		tableW.getItems().forEach(ware -> ware.setSelected(false));
		tableW.refresh();
		
	}

	@FXML private void selectAll(ActionEvent e)
	{
		tableW.getItems().forEach(ware -> ware.setSelected(true));
		tableW.refresh();
	}

	@FXML private void selectInvert(ActionEvent e)
	{
		tableW.getItems().forEach(ware -> ware.setSelected(!ware.isSelected()));
		tableW.refresh();
	}

	@FXML private void copyCrc(javafx.event.ActionEvent e)
	{
		if(tableEntity.getSelectionModel().getSelectedItem() != null && tableEntity.getSelectionModel().getSelectedItem() instanceof Entity entity)
		{
			final var content = new ClipboardContent();
			content.putString(entity.getCrc());
			Clipboard.getSystemClipboard().setContent(content);
		}
	}
	
	@FXML private void copySha1(javafx.event.ActionEvent e)
	{
		if(tableEntity.getSelectionModel().getSelectedItem() != null && tableEntity.getSelectionModel().getSelectedItem() instanceof Entity entity)
		{
			final var content = new ClipboardContent();
			content.putString(entity.getSha1());
			Clipboard.getSystemClipboard().setContent(content);
		}
	}
	
	@FXML private void copyName(javafx.event.ActionEvent e)
	{
		if(tableEntity.getSelectionModel().getSelectedItem() != null && tableEntity.getSelectionModel().getSelectedItem() instanceof Entity entity)
		{
			final var content = new ClipboardContent();
			content.putString(entity.getName());
			Clipboard.getSystemClipboard().setContent(content);
		}
	}

	@FXML private void searchWeb(javafx.event.ActionEvent e)
	{
		if(tableEntity.getSelectionModel().getSelectedItem() != null && tableEntity.getSelectionModel().getSelectedItem() instanceof Entity entity)
		{
			try
			{
				val name = entity.getName();
				val crc = entity.getCrc();
				val sha1 = entity.getSha1();
				val hash = Optional.ofNullable(Optional.ofNullable(crc).orElse(sha1)).map(h -> '+' + h).orElse("");
				MainFrame.getApplication().getHostServices().showDocument(new URI("https://www.google.com/search?q=" + URLEncoder.encode('"' + name + '"', "UTF-8") + hash).toString());
			}
			catch (IOException | URISyntaxException e1)
			{
				Log.err(e1.getMessage(), e1);
			}
		}		
	}

	@FXML private void exportFilteredAsLogiqxDat(ActionEvent e)
	{
		export(ExportType.DATAFILE, true, null);
	}

	@FXML private void exportFilteredAsMameDat(ActionEvent e)
	{
		export(ExportType.MAME, true, null);
	}

	@FXML private void exportFilteredAsSoftwareLists(ActionEvent e)
	{
		export(ExportType.SOFTWARELIST, true, null);
	}

	@FXML private void exportAllAsLogiqxDat(ActionEvent e)
	{
		export(ExportType.DATAFILE, false, null);
	}

	@FXML private void exportAllAsMameDat(ActionEvent e)
	{
		export(ExportType.MAME, false, null);
	}

	@FXML private void exportAllAsSoftwareLists(ActionEvent e)
	{
		export(ExportType.SOFTWARELIST, false, null);
	}

	@FXML private void exportSelectedFilteredAsSoftwareLists(ActionEvent e)
	{
		if(tableWL.getSelectionModel().getSelectedItem() instanceof SoftwareList sl)
			export(ExportType.SOFTWARELIST, true, sl);
	}

	@FXML private void exportSelectedAsSoftwareLists(ActionEvent e)
	{
		if(tableWL.getSelectionModel().getSelectedItem() instanceof SoftwareList sl)
			export(ExportType.SOFTWARELIST, false, sl);
	}

	private final class ValueWLHave extends ObservableValueBase<String>
	{
		private final CellDataFeatures<AnywareList<? extends Anyware>, String> p;

		private ValueWLHave(CellDataFeatures<AnywareList<? extends Anyware>, String> p)
		{
			this.p = p;
		}

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
				return String.format(D_OF_D_FMT, ht[0], ht[1]);
			});
		}
	}

	private static final class TableCellWLHave extends TableCell<AnywareList<? extends Anyware>, String>
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
	}

	private static final class ValueWLDesc extends ObservableValueBase<String>
	{
		private final CellDataFeatures<AnywareList<? extends Anyware>, String> p;

		private ValueWLDesc(CellDataFeatures<AnywareList<? extends Anyware>, String> p)
		{
			this.p = p;
		}

		@Override
		public String getValue()
		{
			if (p.getValue() instanceof SoftwareList sl)
				return sl.getDescription().toString();
			return Messages.getString("MachineListList.AllMachines");
		}
	}

	private static final class TableCellWLDesc extends TableCell<AnywareList<? extends Anyware>, String>
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
	}

	private static final class ValueWLName extends ObservableValueBase<AnywareList<? extends Anyware>>
	{
		private final CellDataFeatures<AnywareList<? extends Anyware>, AnywareList<? extends Anyware>> p;

		private ValueWLName(CellDataFeatures<AnywareList<? extends Anyware>, AnywareList<? extends Anyware>> p)
		{
			this.p = p;
		}

		@Override
		public AnywareList<? extends Anyware> getValue()
		{
			return p.getValue();
		}
	}

	private static final class TableCellWLName extends TableCell<AnywareList<? extends Anyware>, AnywareList<? extends Anyware>>
	{
		@Override
		protected void updateItem(AnywareList<? extends Anyware> item, boolean empty)
		{
			if (empty)
			{
				setText("");
				setGraphic(null);
			}
			else
			{
				setGraphic(new ImageView(switch (item.getStatus())
				{
					case COMPLETE -> diskMultipleGreen;
					case PARTIAL -> diskMultipleOrange;
					case MISSING -> diskMultipleRed;
					case UNKNOWN -> diskMultipleGray;
					default -> diskMultipleGray;

				}));
				if (item instanceof SoftwareList sl)
					setText(sl.getName());
				else if (item instanceof MachineList)
					setText(Messages.getString("MachineListListRenderer.*"));
			}
			setTooltip(new Tooltip(getText()));
			setFont(new Font(10));
		}
	}

	private void export(final ExportType type, final boolean filtered, final SoftwareList selection)
	{
		final var chooser = new FileChooser();
		Optional.ofNullable(session.getUser().getSettings().getProperty("MainFrame.ChooseExeOrDatToExport", (String) null)).map(File::new).ifPresent(chooser::setInitialDirectory);
		chooser.setTitle(Messages.getString("ProfileViewer.ChooseDestinationFile"));
		final var fnef = new FileChooser.ExtensionFilter(Messages.getString("MainFrame.DatFile"), "xml", "dat"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		chooser.getExtensionFilters().add(fnef);
		chooser.setSelectedExtensionFilter(fnef);
		final var chosen = chooser.showSaveDialog(tableWL.getScene().getWindow());
		if (chosen != null)
		{
			try
			{
				final var thread = new Thread(new ProgressTask<Void>((Stage) tableWL.getScene().getWindow())
				{
					@Override
					protected Void call() throws Exception
					{
						Export.export(session.getCurrProfile(), chosen, type, filtered, selection, this);
						return null;
					}
					
					@Override
					protected void succeeded()
					{
						close();
					}

					@Override
					protected void failed()
					{
						close();
						if (getException() instanceof BreakException)
							Dialogs.showAlert("Cancelled");
						else
						{
							Optional.ofNullable(getException().getCause()).ifPresentOrElse(cause -> {
								Log.err(cause.getMessage(), cause);
								Dialogs.showError(cause);
							}, () -> {
								Log.err(getException().getMessage(), getException());
								Dialogs.showError(getException());
							});
						}
					}
				});
				thread.setDaemon(true);
				thread.start();
			}
			catch (IOException | URISyntaxException e)
			{
				Log.err(e.getMessage(), e);
				Dialogs.showError(e);
			}
		}
	}
}
