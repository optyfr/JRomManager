package jrm.fx.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import jrm.fx.ui.controls.DescriptorCellFactory;
import jrm.fx.ui.controls.Dialogs;
import jrm.fx.ui.misc.DragNDrop;
import jrm.fx.ui.profile.ProfileViewer;
import jrm.fx.ui.progress.ProgressTask;
import jrm.fx.ui.status.NeutralToNodeFormatter;
import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.ProfileSettingsEnum;
import jrm.profile.Profile;
import jrm.profile.data.Driver;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.DisplayOrientation;
import jrm.profile.data.PropertyStub;
import jrm.profile.data.Software.Supported;
import jrm.profile.data.Source;
import jrm.profile.data.Systm;
import jrm.profile.filter.CatVer.Category;
import jrm.profile.filter.CatVer.Category.SubCategory;
import jrm.profile.filter.NPlayer;
import jrm.profile.filter.NPlayers;
import jrm.profile.fix.Fix;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.scan.Scan;
import jrm.profile.scan.options.Descriptor;
import jrm.profile.scan.options.ScanAutomation;
import jrm.security.PathAbstractor;
import jrm.security.Session;

public class ScannerPanelController extends BaseController implements ProfileLoader
{
	private static final String DISK_ICON = "/jrm/resicons/icons/disk.png";

	@FXML	private Tab dirTab;
	
	@FXML	private Button infosBtn;
	@FXML	private Button scanBtn;
	@FXML	private Button reportBtn;
	@FXML	private Button fixBtn;
	@FXML	private Button importBtn;
	@FXML	private Button exportBtn;

	@FXML	private Button romsDestBtn;
	@FXML	private TextField romsDest;
	@FXML	private CheckBox disksDestCB;
	@FXML	private Button disksDestBtn;
	@FXML	private TextField disksDest;
	@FXML	private CheckBox swDestCB;
	@FXML	private Button swDestBtn;
	@FXML	private TextField swDest;
	@FXML	private CheckBox swDisksDestCB;
	@FXML	private Button swDisksDestBtn;
	@FXML	private TextField swDisksDest;
	@FXML	private CheckBox samplesDestCB;
	@FXML	private Button samplesDestBtn;
	@FXML	private TextField samplesDest;
	@FXML	private CheckBox backupDestCB;
	@FXML	private Button backupDestBtn;
	@FXML	private TextField backupDest;
	
	@FXML	private ListView<File> srcList;
	@FXML	private ContextMenu srcListMenu;
	@FXML	private MenuItem srcListAddMenuItem;
	@FXML	private MenuItem srcListDelMenuItem;

	@FXML	private Tab settingsTab;

	@FXML	private ScannerPanelSettingsController scannerPanelSettingsController; 
	
	@FXML	private Tab filterTab;
	@FXML	private Tab advFilterTab;
	@FXML	private Tab automationTab;

	@FXML	private HBox profileinfoLbl;
	
	@FXML	private ListView<Systm> systemsFilter;
	@FXML	private ContextMenu systemsFilterMenu;
	@FXML	private MenuItem systemsFilterSelectAllMenuItem;
	@FXML	private MenuItem systemsFilterSelectAllBiosMenuItem;
	@FXML	private MenuItem systemsFilterSelectAllSoftwaresMenuItem;
	@FXML	private MenuItem systemsFilterUnselectAllMenuItem;
	@FXML	private MenuItem systemsFilterUnselectAllBiosMenuItem;
	@FXML	private MenuItem systemsFilterUnselectAllSoftwaresMenuItem;
	@FXML	private MenuItem systemsFilterInvertSelectionMenuItem;
	@FXML	private ListView<Source> sourcesFilter;
	@FXML	private ContextMenu sourcesFilterMenu;
	@FXML	private MenuItem sourcesFilterSelectAllMenuItem;
	@FXML	private MenuItem sourcesFilterUnselectAllMenuItem;
	@FXML	private MenuItem sourcesFilterInvertSelectionMenuItem;

	@FXML	private CheckBox chckbxIncludeClones;
	@FXML	private CheckBox chckbxIncludeDisks;
	@FXML	private CheckBox chckbxIncludeSamples;
	@FXML	private ComboBox<Driver.StatusType> cbbxDriverStatus;
	@FXML	private ComboBox<CabinetType> cbbxFilterCabinetType;
	@FXML	private ComboBox<DisplayOrientation> cbbxFilterDisplayOrientation;
	@FXML	private ComboBox<Supported> cbbxSWMinSupportedLvl;
	@FXML	private ComboBox<String> cbbxYearMin;
	@FXML	private ComboBox<String> cbbxYearMax;


	@FXML private TextField tfNPlayers;
	@FXML private ListView<NPlayer> listNPlayers; 
	@FXML private TextField tfCatVer;
	@FXML private TreeView<PropertyStub> treeCatVer; 
	@FXML private ComboBox<Descriptor> cbAutomation;
	@FXML private ContextMenu nPlayersMenu;
	@FXML private MenuItem nPlayersMenuItemAll;
	@FXML private MenuItem nPlayersMenuItemNone;
	@FXML private MenuItem nPlayersMenuItemInvert;
	@FXML private MenuItem nPlayersMenuItemClear;
	@FXML private ContextMenu catVerMenu;
	@FXML private MenuItem catVerMenuItemSelectAll;
	@FXML private MenuItem catVerMenuItemSelectMature;
	@FXML private MenuItem catVerMenuItemUnselectAll;
	@FXML private MenuItem catVerMenuItemUnselectMature;
	@FXML private MenuItem catVerMenuItemClear;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		ImageView diri = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/folder.png"));
		diri.setPreserveRatio(true);
		diri.getStyleClass().add("icon");
		dirTab.setGraphic(diri);
		ImageView settingsi = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/cog.png"));
		settingsi.setPreserveRatio(true);
		settingsi.getStyleClass().add("icon");
		settingsTab.setGraphic(settingsi);
		ImageView filteri = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/arrow_join.png"));
		filteri.setPreserveRatio(true);
		filteri.getStyleClass().add("icon");
		filterTab.setGraphic(filteri);
		ImageView advfilteri = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/arrow_in.png"));
		advfilteri.setPreserveRatio(true);
		advfilteri.getStyleClass().add("icon");
		advFilterTab.setGraphic(advfilteri);
		ImageView automationi = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/link.png"));
		automationi.setPreserveRatio(true);
		automationi.getStyleClass().add("icon");
		automationTab.setGraphic(automationi);
		
		ImageView romsdstiv = new ImageView(MainFrame.getIcon(DISK_ICON));
		romsdstiv.setPreserveRatio(true);
		romsdstiv.getStyleClass().add("icon");
		romsDestBtn.setGraphic(romsdstiv);
		ImageView disksdstiv = new ImageView(MainFrame.getIcon(DISK_ICON));
		disksdstiv.setPreserveRatio(true);
		disksdstiv.getStyleClass().add("icon");
		disksDestBtn.setGraphic(disksdstiv);
		disksDestCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
			disksDest.setDisable(!newValue);
			disksDestBtn.setDisable(!newValue);
			session.getCurrProfile().setProperty(ProfileSettingsEnum.disks_dest_dir_enabled, newValue);
		});
		ImageView swdstiv = new ImageView(MainFrame.getIcon(DISK_ICON));
		swdstiv.setPreserveRatio(true);
		swdstiv.getStyleClass().add("icon");
		swDestBtn.setGraphic(swdstiv);
		swDestCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
			swDest.setDisable(!newValue);
			swDestBtn.setDisable(!newValue);
			session.getCurrProfile().setProperty(ProfileSettingsEnum.swroms_dest_dir_enabled, newValue);
		});
		ImageView swdiskdstiv = new ImageView(MainFrame.getIcon(DISK_ICON));
		swdiskdstiv.setPreserveRatio(true);
		swdiskdstiv.getStyleClass().add("icon");
		swDisksDestBtn.setGraphic(swdiskdstiv);
		swDisksDestCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
			swDisksDest.setDisable(!newValue);
			swDisksDestBtn.setDisable(!newValue);
			session.getCurrProfile().setProperty(ProfileSettingsEnum.swdisks_dest_dir_enabled, newValue);
		});
		ImageView samplesdstiv = new ImageView(MainFrame.getIcon(DISK_ICON));
		samplesdstiv.setPreserveRatio(true);
		samplesdstiv.getStyleClass().add("icon");
		samplesDestBtn.setGraphic(samplesdstiv);
		samplesDestCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
			samplesDest.setDisable(!newValue);
			samplesDestBtn.setDisable(!newValue);
			session.getCurrProfile().setProperty(ProfileSettingsEnum.samples_dest_dir_enabled, newValue);
		});
		ImageView basckupdstiv = new ImageView(MainFrame.getIcon(DISK_ICON));
		basckupdstiv.setPreserveRatio(true);
		basckupdstiv.getStyleClass().add("icon");
		backupDestBtn.setGraphic(basckupdstiv);
		backupDestCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
			backupDest.setDisable(!newValue);
			backupDestBtn.setDisable(!newValue);
			session.getCurrProfile().setProperty(ProfileSettingsEnum.backup_dest_dir_enabled, newValue);
		});
		ImageView infosiv = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/information.png"));
		infosiv.setPreserveRatio(true);
		infosiv.getStyleClass().add("icon");
		infosBtn.setGraphic(infosiv);
		ImageView scaniv = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/magnifier.png"));
		scaniv.setPreserveRatio(true);
		scaniv.getStyleClass().add("icon");
		scanBtn.setGraphic(scaniv);
		ImageView reportiv = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/report.png"));
		reportiv.setPreserveRatio(true);
		reportiv.getStyleClass().add("icon");
		reportBtn.setGraphic(reportiv);
		ImageView fixiv = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/tick.png"));
		fixiv.setPreserveRatio(true);
		fixiv.getStyleClass().add("icon");
		fixBtn.setGraphic(fixiv);
		ImageView importiv = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/table_refresh.png"));
		importiv.setPreserveRatio(true);
		importiv.getStyleClass().add("icon");
		importBtn.setGraphic(importiv);
		ImageView exportiv = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/table_save.png"));
		exportiv.setPreserveRatio(true);
		exportiv.getStyleClass().add("icon");
		exportBtn.setGraphic(exportiv);
		
		srcList.setCellFactory(param -> new ListCell<File>()
		{
			@Override
			protected void updateItem(File item, boolean empty)
			{
				super.updateItem(item, empty);

				if (empty)
				{
					setText(null);
					setOnMouseClicked(null);
				}
				else
				{
					setText(item.toString());
					setOnMouseClicked(ev -> {
						if (ev.getClickCount() == 2)
						{
							chooseSrc(item, ProfileSettingsEnum.src_dir, "MainFrame.ChooseRomsSource");
						}
					});
				}
			}
		});
		srcListMenu.setOnShowing(e -> srcListDelMenuItem.setDisable(srcList.getSelectionModel().getSelectedIndex() < 0));
		srcListDelMenuItem.setOnAction(e -> {
			srcList.getItems().removeAll(srcList.getSelectionModel().getSelectedItems());
			saveSrcList();
		});
		srcListAddMenuItem.setOnAction(e -> chooseSrc(null, ProfileSettingsEnum.src_dir, "MainFrame.ChooseRomsSource"));
		new DragNDrop(romsDest).addDir(txt -> session.getCurrProfile().setProperty(ProfileSettingsEnum.roms_dest_dir, txt));
		new DragNDrop(disksDest).addDir(txt -> session.getCurrProfile().setProperty(ProfileSettingsEnum.disks_dest_dir, txt));
		new DragNDrop(swDest).addDir(txt -> session.getCurrProfile().setProperty(ProfileSettingsEnum.swroms_dest_dir, txt));
		new DragNDrop(swDisksDest).addDir(txt -> session.getCurrProfile().setProperty(ProfileSettingsEnum.swdisks_dest_dir, txt));
		new DragNDrop(samplesDest).addDir(txt -> session.getCurrProfile().setProperty(ProfileSettingsEnum.samples_dest_dir, txt));
		new DragNDrop(backupDest).addDir(txt -> session.getCurrProfile().setProperty(ProfileSettingsEnum.backup_dest_dir, txt));
		new DragNDrop(srcList).addDirs(files -> session.getCurrProfile().setProperty(ProfileSettingsEnum.src_dir, String.join("|", files.stream().map(File::getAbsolutePath).toList())));
		
		systemsFilter.setCellFactory(CheckBoxListCell.forListView(item -> {
			BooleanProperty observable = new SimpleBooleanProperty(item.isSelected(session.getCurrProfile()));
			observable.addListener((obs, wasSelected, isNowSelected) -> {
				item.setSelected(session.getCurrProfile(), isNowSelected);
				ProfileViewer.getResetCounter().incrementAndGet();
			});
			return observable;
		}));

		sourcesFilter.setCellFactory(CheckBoxListCell.forListView(item -> {
			BooleanProperty observable = new SimpleBooleanProperty(item.isSelected(session.getCurrProfile()));
			observable.addListener((obs, wasSelected, isNowSelected) -> {
				item.setSelected(session.getCurrProfile(), isNowSelected);
				ProfileViewer.getResetCounter().incrementAndGet();
			});
			return observable;
		}));

		cbbxDriverStatus.setItems(FXCollections.observableArrayList(Driver.StatusType.values()));
		cbbxFilterCabinetType.setItems(FXCollections.observableArrayList(CabinetType.values()));
		cbbxFilterDisplayOrientation.setItems(FXCollections.observableArrayList(DisplayOrientation.values()));
		cbbxSWMinSupportedLvl.setItems(FXCollections.observableArrayList(Supported.values()));
		chckbxIncludeClones.setOnAction(e -> {
			session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_InclClones, chckbxIncludeClones.isSelected());
			ProfileViewer.getResetCounter().incrementAndGet();
		});
		chckbxIncludeDisks.setOnAction(e -> {
			session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_InclDisks, chckbxIncludeDisks.isSelected());
			ProfileViewer.getResetCounter().incrementAndGet();
		});
		chckbxIncludeSamples.setOnAction(e -> {
			session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_InclSamples, chckbxIncludeSamples.isSelected());
			ProfileViewer.getResetCounter().incrementAndGet();
		});
		cbbxFilterCabinetType.setOnAction(e -> {
			session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_CabinetType, cbbxFilterCabinetType.getValue().toString());
			ProfileViewer.getResetCounter().incrementAndGet();
		});
		cbbxFilterDisplayOrientation.setOnAction(e -> {
			session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_DisplayOrientation, cbbxFilterDisplayOrientation.getValue().toString());
			ProfileViewer.getResetCounter().incrementAndGet();
		});
		cbbxDriverStatus.setOnAction(e -> {
			session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_DriverStatus, cbbxDriverStatus.getValue().toString());
			ProfileViewer.getResetCounter().incrementAndGet();
		});
		cbbxSWMinSupportedLvl.setOnAction(e -> {
			session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_MinSoftwareSupportedLevel, cbbxSWMinSupportedLvl.getValue().toString());
			ProfileViewer.getResetCounter().incrementAndGet();
		});
		cbbxYearMin.setOnAction(e -> {
			session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_YearMin, cbbxYearMin.getValue());
			ProfileViewer.getResetCounter().incrementAndGet();
		});
		cbbxYearMax.setOnAction(e -> {
			session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_YearMax, cbbxYearMax.getValue());
			ProfileViewer.getResetCounter().incrementAndGet();
		});
		
		new DragNDrop(tfNPlayers).addFile(this::selectNPlayersFile);
		listNPlayers.setCellFactory(CheckBoxListCell.forListView(item -> {
			BooleanProperty observable = new SimpleBooleanProperty(item.isSelected(session.getCurrProfile()));
			observable.addListener((obs, wasSelected, isNowSelected) -> {
				item.setSelected(session.getCurrProfile(), isNowSelected);
				ProfileViewer.getResetCounter().incrementAndGet();
			});
			return observable;
		}));
		
		new DragNDrop(tfCatVer).addFile(this::selectCatVerFile);
		treeCatVer.setCellFactory(CheckBoxTreeCell.forTreeView(item -> {
			if (item instanceof CheckBoxTreeItem<?> i)
				return i.selectedProperty();
			return null;
		}, new StringConverter<TreeItem<PropertyStub>>()
		{

			@Override
			public String toString(TreeItem<PropertyStub> object)
			{
				return object.getValue().toString();
			}

			@Override
			public TreeItem<PropertyStub> fromString(String string)
			{
				return null;
			}
		}));
		
		cbAutomation.setItems(FXCollections.observableArrayList(ScanAutomation.values()));
		cbAutomation.setCellFactory(param -> new DescriptorCellFactory());
		cbAutomation.setButtonCell(cbAutomation.getCellFactory().call(null));
		cbAutomation.setOnAction(e -> session.getCurrProfile().setProperty(ProfileSettingsEnum.automation_scan, cbAutomation.getValue().toString()));
		
		importBtn.setOnAction(e -> {
			final var filters = Arrays.asList(new ExtensionFilter("Properties", "*.properties"));
			final var presets = session.getUser().getSettings().getWorkPath().resolve("presets");
			chooseOpenFile(importBtn, null, presets.toFile(), filters, file -> {
				session.getCurrProfile().loadSettings(PathAbstractor.getAbsolutePath(session, file.toString()).toFile());
				session.getCurrProfile().loadCatVer(null);
				session.getCurrProfile().loadNPlayers(null);
				initProfileSettings(session);
			});
		});
		
		exportBtn.setOnAction(e -> {
			final var filters = Arrays.asList(new ExtensionFilter("Properties", "*.properties"));
			final var presets = session.getUser().getSettings().getWorkPath().resolve("presets");
			try
			{
				Files.createDirectories(presets);
				chooseSaveFile(exportBtn, null, presets.toFile(), filters, file -> session.getCurrProfile().saveSettings(PathAbstractor.getAbsolutePath(session, file.toString()).toFile()));
			}
			catch (IOException e1)
			{
				Log.err(e1.getMessage(), e1);
			}
		});
	}
	
	@Override
	public void loadProfile(Session session, ProfileNFO profile)
	{
		if (session.getCurrProfile() != null)
			session.getCurrProfile().saveSettings();
		
		if (MainFrame.getProfileViewer() != null)
			MainFrame.getProfileViewer().clear();

		try
		{
			final var thread = new Thread(new ProgressTask<Profile>((Stage) romsDest.getScene().getWindow())
			{

				@Override
				protected Profile call() throws Exception
				{
					return Profile.load(session, profile, this);
				}

				@Override
				protected void succeeded()
				{
					try
					{
						final var profile = get();
						session.getReport().setProfile(session.getCurrProfile());
						MainFrame.getReportFrame().setNeedUpdate(true);
						
						ProfileViewer.getResetCounter().incrementAndGet();

						MainFrame.getController().getScannerPanelTab().setDisable(profile == null);
						scanBtn.setDisable(profile == null);
						fixBtn.setDisable(true);
						if (profile != null && session.getCurrProfile() != null)
						{
							profileinfoLbl.getChildren().setAll(NeutralToNodeFormatter.toNodes(session.getCurrProfile().getName()));
							systemsFilter.setItems(FXCollections.observableList(session.getCurrProfile().getSystems().getSystems()));
							sourcesFilter.setItems(FXCollections.observableList(session.getCurrProfile().getSources().getSrces()));
							initProfileSettings(session);
							MainFrame.getController().getTabPane().getSelectionModel().select(1);
							MainFrame.getController().getProfilePanelController().refreshList();
						}
						this.close();
					}
					catch (InterruptedException e)
					{
						this.close();
						Log.err(e.getMessage(), e);
						Thread.currentThread().interrupt();
					}
					catch (ExecutionException e)
					{
						this.close();
						Optional.ofNullable(e.getCause()).ifPresentOrElse(cause -> {
							Log.err(cause.getMessage(), cause);
							Dialogs.showError(cause);
						}, () -> {
							Log.err(e.getMessage(), e);
							Dialogs.showError(e);
						});
					}
				}

				@Override
				protected void failed()
				{
					if (getException() instanceof BreakException)
						Dialogs.showAlert("Cancelled");
					else
					{
						this.close();
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

	@FXML private void scan(ActionEvent e)
	{
		scan(session, true);
	}
	
	/**
	 * Scan.
	 */
	private void scan(final Session session, final boolean automate)
	{
		String txtdstdir = romsDest.getText();
		if (txtdstdir.isEmpty())
		{
			romsDestBtn.fire();
			txtdstdir = romsDest.getText();
		}
		if (txtdstdir.isEmpty())
			return;
		try
		{
			final var thread = new Thread(new ProgressTask<Scan>((Stage) romsDest.getScene().getWindow())
			{

				@Override
				protected Scan call() throws Exception
				{
					return new Scan(session.getCurrProfile(), this);
				}

				@Override
				protected void succeeded()
				{
					try
					{
						session.setCurrScan(get());
						fixBtn.setDisable(session.getCurrScan()==null || session.getCurrScan().actions.stream().mapToInt(Collection::size).sum() == 0);
						close();
						// update entries in profile viewer 
						if (MainFrame.getProfileViewer() != null)
							MainFrame.getProfileViewer().reload();
						ScanAutomation automation = ScanAutomation.valueOf(session.getCurrProfile().getSettings().getProperty(ProfileSettingsEnum.automation_scan));
						if(MainFrame.getReportFrame() != null)
						{
							if(automation.hasReport())
								MainFrame.getReportFrame().setVisible();
							MainFrame.getReportFrame().setNeedUpdate(true);
						}
						if (automate && !fixBtn.isDisabled() && automation.hasFix())
						{
							fix(session);
						}
					}
					catch (InterruptedException e)
					{
						this.close();
						Log.err(e.getMessage(), e);
						Thread.currentThread().interrupt();
					}
					catch (Exception e)
					{
						this.close();
						Optional.ofNullable(e.getCause()).ifPresentOrElse(cause -> {
							Log.err(cause.getMessage(), cause);
							Dialogs.showError(cause);
						}, () -> {
							Log.err(e.getMessage(), e);
							Dialogs.showError(e);
						});
					}
				}
				
				@Override
				protected void failed()
				{
					this.close();
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
	
	@FXML private void report(ActionEvent evt)
	{
		MainFrame.getReportFrame().setVisible();
	}
	
	@FXML private void fix(ActionEvent e)
	{
		fix(session);
	}

	/**
	 * Fix.
	 */
	private void fix(final Session session)
	{
		try
		{
			final var thread = new Thread(new ProgressTask<Fix>((Stage) romsDest.getScene().getWindow())
			{
				private boolean toFix = false;
				
				@Override
				protected Fix call() throws Exception
				{
					if (session.getCurrProfile().hasPropsChanged())
					{
						final var answer = Dialogs.showConfirmation(Messages.getString("MainFrame.WarnSettingsChanged"), Messages.getString("MainFrame.RescanBeforeFix"), ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
						if (answer.isPresent())
						{
							if (answer.get() == ButtonType.YES)
							{
								session.setCurrScan(new Scan(session.getCurrProfile(), this));
								toFix = session.getCurrScan().actions.stream().mapToInt(Collection::size).sum() > 0;
								if (!toFix)
									return null;
							}
							else if (answer.get() != ButtonType.NO)
								return null;
						}
					}
					final Fix fix = new Fix(session.getCurrProfile(), session.getCurrScan(), this);
					toFix = fix.getActionsRemain() > 0;
					return fix;
				}
				
				@Override
				protected void succeeded()
				{
					try
					{
						get();
						fixBtn.setDisable(!toFix);
						close();
						// update entries in profile viewer
						if (MainFrame.getProfileViewer() != null)
							MainFrame.getProfileViewer().reload();
						ScanAutomation automation = ScanAutomation.valueOf(session.getCurrProfile().getSettings().getProperty(ProfileSettingsEnum.automation_scan));
						if (automation.hasScanAgain())
							scan(session, false);
					}
					catch (InterruptedException e)
					{
						this.close();
						Log.err(e.getMessage(), e);
						Thread.currentThread().interrupt();
					}
					catch (Exception e)
					{
						this.close();
						Optional.ofNullable(e.getCause()).ifPresentOrElse(cause -> {
							Log.err(cause.getMessage(), cause);
							Dialogs.showError(cause);
						}, () -> {
							Log.err(e.getMessage(), e);
							Dialogs.showError(e);
						});
					}
				}
				
				@Override
				protected void failed()
				{
					if (getException() instanceof BreakException)
						Dialogs.showAlert("Cancelled");
					else
					{
						this.close();
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

	
	private void initProfileSettings(Session session)
	{
		romsDest.setText(session.getCurrProfile().getProperty(ProfileSettingsEnum.roms_dest_dir)); //$NON-NLS-1$ //$NON-NLS-2$
		disksDestCB.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.disks_dest_dir_enabled, Boolean.class)); //$NON-NLS-1$
		disksDest.setText(session.getCurrProfile().getProperty(ProfileSettingsEnum.disks_dest_dir)); //$NON-NLS-1$ //$NON-NLS-2$
		swDestCB.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.swroms_dest_dir_enabled, Boolean.class)); //$NON-NLS-1$
		swDest.setText(session.getCurrProfile().getProperty(ProfileSettingsEnum.swroms_dest_dir)); //$NON-NLS-1$ //$NON-NLS-2$
		swDisksDestCB.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.swdisks_dest_dir_enabled, Boolean.class)); //$NON-NLS-1$
		swDisksDest.setText(session.getCurrProfile().getProperty(ProfileSettingsEnum.swdisks_dest_dir)); //$NON-NLS-1$ //$NON-NLS-2$
		samplesDestCB.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.samples_dest_dir_enabled, Boolean.class)); //$NON-NLS-1$
		samplesDest.setText(session.getCurrProfile().getProperty(ProfileSettingsEnum.samples_dest_dir)); //$NON-NLS-1$ //$NON-NLS-2$
		backupDestCB.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.backup_dest_dir_enabled, Boolean.class)); //$NON-NLS-1$
		backupDest.setText(session.getCurrProfile().getProperty(ProfileSettingsEnum.backup_dest_dir)); //$NON-NLS-1$ //$NON-NLS-2$
		srcList.setItems(FXCollections.observableList(Stream.of(StringUtils.split(session.getCurrProfile().getProperty(ProfileSettingsEnum.src_dir),'|')).filter(s->!s.isEmpty()).map(File::new).collect(Collectors.toList())));

		scannerPanelSettingsController.initProfileSettings(session.getCurrProfile().getSettings());
		
		chckbxIncludeClones.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_InclClones, Boolean.class)); //$NON-NLS-1$
		chckbxIncludeDisks.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_InclDisks, Boolean.class)); //$NON-NLS-1$
		chckbxIncludeSamples.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_InclSamples, Boolean.class)); //$NON-NLS-1$
		cbbxDriverStatus.getSelectionModel().select(Driver.StatusType.valueOf(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_DriverStatus))); //$NON-NLS-1$
		cbbxFilterCabinetType.getSelectionModel().select(CabinetType.valueOf(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_CabinetType))); //$NON-NLS-1$
		cbbxFilterDisplayOrientation.getSelectionModel().select(DisplayOrientation.valueOf(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_DisplayOrientation))); //$NON-NLS-1$
		cbbxSWMinSupportedLvl.getSelectionModel().select(Supported.valueOf(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_MinSoftwareSupportedLevel))); //$NON-NLS-1$
		cbbxYearMin.setItems(FXCollections.observableArrayList(session.getCurrProfile().getYears()).sorted());
		cbbxYearMin.getSelectionModel().select(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_YearMin)); //$NON-NLS-1$
		cbbxYearMax.setItems(FXCollections.observableArrayList(session.getCurrProfile().getYears()).sorted());
		cbbxYearMax.getSelectionModel().select(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_YearMax)); //$NON-NLS-1$
		
		showNPlayers();
		showCatVer();
		
		cbAutomation.getSelectionModel().select(ScanAutomation.valueOf(session.getCurrProfile().getProperty(ProfileSettingsEnum.automation_scan)));
	}
	
	private void selectNPlayersFile(String file)
	{
		if(Files.isRegularFile(Path.of(file)))
		{
			session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_nplayers_ini, file);
			session.getCurrProfile().loadNPlayers(null);
			showNPlayers();
		}
	}
	
	private void showNPlayers()
	{
		tfNPlayers.setText(session.getCurrProfile().getNplayers() != null ? session.getCurrProfile().getNplayers().file.getAbsolutePath() : null);
		listNPlayers.setItems(Optional.ofNullable(session.getCurrProfile().getNplayers()).map(NPlayers::getListNPlayers).map(FXCollections::observableArrayList).orElse(null));
	}
	
	private void selectCatVerFile(String file)
	{
		if(Files.isRegularFile(Path.of(file)))
		{
			session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_catver_ini, file);
			session.getCurrProfile().loadCatVer(null);
			showCatVer();
		}		
	}
	
	private void showCatVer()
	{
		tfCatVer.setText(session.getCurrProfile().getCatver() != null ? session.getCurrProfile().getCatver().file.getAbsolutePath() : null);
		
		final var root = session.getCurrProfile().getCatver();
		if(root!=null)
		{
			final var rootitem = new CheckBoxTreeItem<PropertyStub>(root);
			rootitem.setExpanded(true);
			session.getCurrProfile().getCatver().forEach(cat -> {
				final var catitem = new CheckBoxTreeItem<PropertyStub>(cat);
				rootitem.getChildren().add(catitem);
				cat.forEach(subcat -> catitem.getChildren().add(new CheckBoxTreeItem<>(subcat)));
			});
			treeCatVer.setRoot(rootitem);
					
			rootitem.selectedProperty().addListener((observable, oldvalue, newvalue) -> root.setSelected(newvalue));
			rootitem.getChildren().forEach(catitem -> {
				((CheckBoxTreeItem<PropertyStub>) catitem).selectedProperty().addListener((observable, oldvalue, newvalue) -> ((Category) catitem.getValue()).setSelected(newvalue));
				catitem.getChildren().forEach(subcatitem -> {
					((CheckBoxTreeItem<PropertyStub>)subcatitem).selectedProperty().addListener((observable, oldvalue, newvalue) -> {
						((SubCategory)subcatitem.getValue()).setSelected(newvalue);
						treeCatVer.refresh();
						ProfileViewer.getResetCounter().incrementAndGet();
					});
					((CheckBoxTreeItem<PropertyStub>)subcatitem).setSelected(((SubCategory)subcatitem.getValue()).isSelected());
				});
			});
			
					
		}
		else
			treeCatVer.setRoot(null);
		
	}

	@FXML private void chooseRomsDest(ActionEvent e)
	{
		chooseAnyDest(romsDest, ProfileSettingsEnum.roms_dest_dir, "MainFrame.ChooseRomsDestination");
	}
	
	@FXML private void chooseDisksDest(ActionEvent e)
	{
		chooseAnyDest(disksDest, ProfileSettingsEnum.disks_dest_dir, "MainFrame.ChooseDisksDestination");
	}
	
	@FXML private void chooseSWRomsDest(ActionEvent e)
	{
		chooseAnyDest(swDest, ProfileSettingsEnum.swroms_dest_dir, "MainFrame.ChooseSWRomsDestination");
	}
	
	@FXML private void chooseSWDisksDest(ActionEvent e)
	{
		chooseAnyDest(swDisksDest, ProfileSettingsEnum.swdisks_dest_dir, "MainFrame.ChooseSWDisksDestination");
	}
	
	@FXML private void chooseSamplesDest(ActionEvent e)
	{
		chooseAnyDest(samplesDest, ProfileSettingsEnum.samples_dest_dir, "MainFrame.ChooseSamplesDestination");
	}
	
	@FXML private void chooseBackupDest(ActionEvent e)
	{
		chooseAnyDest(backupDest, ProfileSettingsEnum.backup_dest_dir, "MainFrame.ChooseBackupDestination");
	}
	
	private void chooseAnyDest(TextField tf, ProfileSettingsEnum ppt, String defPptName)
	{
		final var workdir = session.getUser().getSettings().getWorkPath().toFile();
		final var defdir = PathAbstractor.getAbsolutePath(session, session.getUser().getSettings().getProperty(defPptName, workdir.getAbsolutePath())).toFile();
		chooseDir(tf, tf.getText(), defdir, dir -> {
			tf.setText(dir.toString());
			session.getUser().getSettings().setProperty(defPptName, tf.getText()); // $NON-NLS-1$
			session.getCurrProfile().setProperty(ppt, tf.getText()); // $NON-NLS-1$
		});
	}
	
	private void chooseSrc(File oldDir, ProfileSettingsEnum ppt, String defPptName)
	{
		final var workdir = session.getUser().getSettings().getWorkPath().toFile();
		final var defdir = PathAbstractor.getAbsolutePath(session, session.getUser().getSettings().getProperty(defPptName, workdir.getAbsolutePath())).toFile();
		chooseDir(srcList, oldDir!=null?oldDir.toString():null, defdir, dir -> {
			var modified = false;
			if(oldDir!=null)
			{
				if(!oldDir.equals(dir.toFile()))
				{
					final var i = srcList.getItems().indexOf(oldDir);
					srcList.getItems().set(i, dir.toFile());
					modified = true;
				}
			}
			else
			{
				if(-1 == srcList.getItems().indexOf(dir.toFile()))
				{
					srcList.getItems().add(dir.toFile());
					modified = true;
				}
			}
			if(modified)
			{
				saveSrcList();
				session.getUser().getSettings().setProperty(defPptName, dir.toString()); // $NON-NLS-1$
			}
		});
	}
	
	private void saveSrcList()
	{
		session.getCurrProfile().setProperty(ProfileSettingsEnum.src_dir, String.join("|", srcList.getItems().stream().map(File::getAbsolutePath).toList()));
	}
	
	@FXML private void infos(ActionEvent evt)
	{
		if (MainFrame.getProfileViewer() == null)
		{
			try
			{
				MainFrame.setProfileViewer(new ProfileViewer((Stage)infosBtn.getScene().getWindow()));
			}
			catch (IOException | URISyntaxException e)
			{
				e.printStackTrace();
				Log.err(e.getMessage(), e);
			}
		}
		if (MainFrame.getProfileViewer() != null)
		{
			MainFrame.getProfileViewer().show();
			MainFrame.applyCSS();
			//MainFrame.getProfileViewer().reset(session.getCurrProfile());
			MainFrame.getProfileViewer().reload();
		}
	}
	
	@FXML
	private void systemsFilterSelectAll()
	{
		for (final var systm : session.getCurrProfile().getSystems())
			systm.setSelected(session.getCurrProfile(), true);
		systemsFilter.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	@FXML
	private void systemsFilterSelectAllBios()
	{
		for (final var systm : session.getCurrProfile().getSystems())
			if (systm.getType() == Systm.Type.BIOS)
				systm.setSelected(session.getCurrProfile(), true);
		systemsFilter.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	@FXML
	private void systemsFilterSelectAllSoftwares()
	{
		for (final var systm : session.getCurrProfile().getSystems())
			if (systm.getType() == Systm.Type.SOFTWARELIST)
				systm.setSelected(session.getCurrProfile(), true);
		systemsFilter.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	@FXML
	private void systemsFilterUnselectAll()
	{
		for (final var systm : session.getCurrProfile().getSystems())
			systm.setSelected(session.getCurrProfile(), false);
		systemsFilter.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	@FXML
	private void systemsFilterUnselectAllBios()
	{
		for (final var systm : session.getCurrProfile().getSystems())
			if (systm.getType() == Systm.Type.BIOS)
				systm.setSelected(session.getCurrProfile(), false);
		systemsFilter.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	@FXML
	private void systemsFilterUnselectAllSoftwares()
	{
		for (final var systm : session.getCurrProfile().getSystems())
			if (systm.getType() == Systm.Type.SOFTWARELIST)
				systm.setSelected(session.getCurrProfile(), false);
		systemsFilter.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	@FXML
	private void systemsFilterInvertSelection()
	{
		for(final var systm : session.getCurrProfile().getSystems())
			systm.setSelected(session.getCurrProfile(), !systm.isSelected(session.getCurrProfile()));
		systemsFilter.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	@FXML
	private void sourcesFilterSelectAll()
	{
		for (final var source : session.getCurrProfile().getSources())
			source.setSelected(session.getCurrProfile(), true);
		sourcesFilter.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	@FXML
	private void sourcesFilterUnselectAll()
	{
		for (final var source : session.getCurrProfile().getSources())
			source.setSelected(session.getCurrProfile(), false);
		sourcesFilter.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	@FXML
	private void sourcesFilterInvertSelection()
	{
		for (final var source : session.getCurrProfile().getSources())
			source.setSelected(session.getCurrProfile(), !source.isSelected(session.getCurrProfile()));
		sourcesFilter.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	@FXML void nPlayersListSelectAll()
	{
		for (final var nplayer : session.getCurrProfile().getNplayers())
			nplayer.setSelected(session.getCurrProfile(), true);
		listNPlayers.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}
	
	@FXML void nPlayersListSelectNone()
	{
		for (final var nplayer : session.getCurrProfile().getNplayers())
			nplayer.setSelected(session.getCurrProfile(), false);
		listNPlayers.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	@FXML void nPlayersListSelectInvert()
	{
		for (final var nplayer : session.getCurrProfile().getNplayers())
			nplayer.setSelected(session.getCurrProfile(), !nplayer.isSelected(session.getCurrProfile()));
		listNPlayers.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	@FXML void nPlayersListClear()
	{
		session.getCurrProfile().saveSettings();
		session.getCurrProfile().setNplayers(null);
		session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_nplayers_ini, null);
		session.getCurrProfile().saveSettings();
		tfNPlayers.setText(null);
		listNPlayers.setItems(null);
	}

	private Stream<CheckBoxTreeItem<PropertyStub>> streamSubCatItems()
	{
		return ((CheckBoxTreeItem<PropertyStub>) treeCatVer.getRoot()).getChildren().stream().flatMap(t -> t.getChildren().stream()).map(t -> (CheckBoxTreeItem<PropertyStub>) t);
	}
	
	@FXML void catVerListSelectAll()
	{
		streamSubCatItems().forEachOrdered(subcat -> subcat.setSelected(true));
		treeCatVer.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}
	
	@FXML void catVerListUnselectAll()
	{
		streamSubCatItems().forEachOrdered(subcat -> subcat.setSelected(false));
		treeCatVer.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}

	private static final String MATURE = "* Mature *";

	private Stream<CheckBoxTreeItem<PropertyStub>> streamMatureItems()
	{
		return streamSubCatItems().filter(t -> t.getValue() instanceof SubCategory subcat && (subcat.name.endsWith(MATURE) || subcat.getParent().name.endsWith(MATURE)));
	}

	@FXML void catVerListSelectMature()
	{
		streamMatureItems().forEachOrdered(subcat -> subcat.setSelected(true));
		treeCatVer.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}
	
	@FXML void catVerListUnselectMature()
	{
		streamMatureItems().forEachOrdered(subcat -> subcat.setSelected(false));
		treeCatVer.refresh();
		ProfileViewer.getResetCounter().incrementAndGet();
	}


	@FXML void catVerListClear()
	{
		session.getCurrProfile().saveSettings();
		session.getCurrProfile().setCatver(null);
		session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_catver_ini, null);
		session.getCurrProfile().saveSettings();
		tfCatVer.setText(null);
		treeCatVer.setRoot(null);
	}
}
