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

public class ScannerPanelController extends BaseController implements ProfileLoader {
    private static final String DISK_ICON = "/jrm/resicons/icons/disk.png";

    @FXML
    private Tab dirTab;

    @FXML
    private Button infosBtn;
    @FXML
    private Button scanBtn;
    @FXML
    private Button reportBtn;
    @FXML
    private Button fixBtn;
    @FXML
    private Button importBtn;
    @FXML
    private Button exportBtn;

    @FXML
    private Button romsDestBtn;
    @FXML
    private TextField romsDest;
    @FXML
    private CheckBox disksDestCB;
    @FXML
    private Button disksDestBtn;
    @FXML
    private TextField disksDest;
    @FXML
    private CheckBox swDestCB;
    @FXML
    private Button swDestBtn;
    @FXML
    private TextField swDest;
    @FXML
    private CheckBox swDisksDestCB;
    @FXML
    private Button swDisksDestBtn;
    @FXML
    private TextField swDisksDest;
    @FXML
    private CheckBox samplesDestCB;
    @FXML
    private Button samplesDestBtn;
    @FXML
    private TextField samplesDest;
    @FXML
    private CheckBox backupDestCB;
    @FXML
    private Button backupDestBtn;
    @FXML
    private TextField backupDest;

    @FXML
    private ListView<File> srcList;
    @FXML
    private ContextMenu srcListMenu;
    @FXML
    private MenuItem srcListAddMenuItem;
    @FXML
    private MenuItem srcListDelMenuItem;

    @FXML
    private Tab settingsTab;

    @FXML
    private ScannerPanelSettingsController scannerPanelSettingsController;

    @FXML
    private Tab filterTab;
    @FXML
    private Tab advFilterTab;
    @FXML
    private Tab automationTab;

    @FXML
    private HBox profileinfoLbl;

    @FXML
    private ListView<Systm> systemsFilter;
    @FXML
    private ContextMenu systemsFilterMenu;
    @FXML
    private MenuItem systemsFilterSelectAllMenuItem;
    @FXML
    private MenuItem systemsFilterSelectAllBiosMenuItem;
    @FXML
    private MenuItem systemsFilterSelectAllSoftwaresMenuItem;
    @FXML
    private MenuItem systemsFilterUnselectAllMenuItem;
    @FXML
    private MenuItem systemsFilterUnselectAllBiosMenuItem;
    @FXML
    private MenuItem systemsFilterUnselectAllSoftwaresMenuItem;
    @FXML
    private MenuItem systemsFilterInvertSelectionMenuItem;
    @FXML
    private ListView<Source> sourcesFilter;
    @FXML
    private ContextMenu sourcesFilterMenu;
    @FXML
    private MenuItem sourcesFilterSelectAllMenuItem;
    @FXML
    private MenuItem sourcesFilterUnselectAllMenuItem;
    @FXML
    private MenuItem sourcesFilterInvertSelectionMenuItem;

    @FXML
    private CheckBox chckbxIncludeClones;
    @FXML
    private CheckBox chckbxIncludeDisks;
    @FXML
    private CheckBox chckbxIncludeSamples;
    @FXML
    private ComboBox<Driver.StatusType> cbbxDriverStatus;
    @FXML
    private ComboBox<CabinetType> cbbxFilterCabinetType;
    @FXML
    private ComboBox<DisplayOrientation> cbbxFilterDisplayOrientation;
    @FXML
    private ComboBox<Supported> cbbxSWMinSupportedLvl;
    @FXML
    private ComboBox<String> cbbxYearMin;
    @FXML
    private ComboBox<String> cbbxYearMax;

    @FXML
    private TextField tfNPlayers;
    @FXML
    private ListView<NPlayer> listNPlayers;
    @FXML
    private TextField tfCatVer;
    @FXML
    private TreeView<PropertyStub> treeCatVer;
    @FXML
    private ComboBox<Descriptor> cbAutomation;
    @FXML
    private ContextMenu nPlayersMenu;
    @FXML
    private MenuItem nPlayersMenuItemAll;
    @FXML
    private MenuItem nPlayersMenuItemNone;
    @FXML
    private MenuItem nPlayersMenuItemInvert;
    @FXML
    private MenuItem nPlayersMenuItemClear;
    @FXML
    private ContextMenu catVerMenu;
    @FXML
    private MenuItem catVerMenuItemSelectAll;
    @FXML
    private MenuItem catVerMenuItemSelectMature;
    @FXML
    private MenuItem catVerMenuItemUnselectAll;
    @FXML
    private MenuItem catVerMenuItemUnselectMature;
    @FXML
    private MenuItem catVerMenuItemClear;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTabIcons();
        initDestinations();
        initActionButtons();
        initSrcList();
        initDestDragNDrop();
        initFilters();
        initNPlayers();
        initCatVer();
        initAutomation();
        initImportExport();
    }

    private void initTabIcons() {
        dirTab.setGraphic(IconHelper.createIcon("/jrm/resicons/icons/folder.png"));
        settingsTab.setGraphic(IconHelper.createIcon("/jrm/resicons/icons/cog.png"));
        filterTab.setGraphic(IconHelper.createIcon("/jrm/resicons/icons/arrow_join.png"));
        advFilterTab.setGraphic(IconHelper.createIcon("/jrm/resicons/icons/arrow_in.png"));
        automationTab.setGraphic(IconHelper.createIcon("/jrm/resicons/icons/link.png"));
    }

    private void initDestinations() {
        romsDestBtn.setGraphic(IconHelper.createIcon(DISK_ICON));
        disksDestBtn.setGraphic(IconHelper.createIcon(DISK_ICON));
        initDestCheckBox(disksDestCB, disksDest, disksDestBtn, ProfileSettingsEnum.disks_dest_dir_enabled);
        swDestBtn.setGraphic(IconHelper.createIcon(DISK_ICON));
        initDestCheckBox(swDestCB, swDest, swDestBtn, ProfileSettingsEnum.swroms_dest_dir_enabled);
        swDisksDestBtn.setGraphic(IconHelper.createIcon(DISK_ICON));
        initDestCheckBox(swDisksDestCB, swDisksDest, swDisksDestBtn, ProfileSettingsEnum.swdisks_dest_dir_enabled);
        samplesDestBtn.setGraphic(IconHelper.createIcon(DISK_ICON));
        initDestCheckBox(samplesDestCB, samplesDest, samplesDestBtn, ProfileSettingsEnum.samples_dest_dir_enabled);
        backupDestBtn.setGraphic(IconHelper.createIcon(DISK_ICON));
        initDestCheckBox(backupDestCB, backupDest, backupDestBtn, ProfileSettingsEnum.backup_dest_dir_enabled);
    }

    private void initActionButtons() {
        infosBtn.setGraphic(IconHelper.createIcon("/jrm/resicons/icons/information.png"));
        scanBtn.setGraphic(IconHelper.createIcon("/jrm/resicons/icons/magnifier.png"));
        reportBtn.setGraphic(IconHelper.createIcon("/jrm/resicons/icons/report.png"));
        fixBtn.setGraphic(IconHelper.createIcon("/jrm/resicons/icons/tick.png"));
        importBtn.setGraphic(IconHelper.createIcon("/jrm/resicons/icons/table_refresh.png"));
        exportBtn.setGraphic(IconHelper.createIcon("/jrm/resicons/icons/table_save.png"));
    }

    private void initSrcList() {
        srcList.setCellFactory(_ -> new ListCell<File>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                    setOnMouseClicked(null);
                } else {
                    setText(item.toString());
                    setOnMouseClicked(ev -> {
                        if (ev.getClickCount() == 2) {
                            chooseSrc(item, ProfileSettingsEnum.src_dir, "MainFrame.ChooseRomsSource");
                        }
                    });
                }
            }
        });
        srcListMenu.setOnShowing(_ -> srcListDelMenuItem.setDisable(srcList.getSelectionModel().getSelectedIndex() < 0));
        srcListDelMenuItem.setOnAction(_ -> {
            srcList.getItems().removeAll(srcList.getSelectionModel().getSelectedItems());
            saveSrcList();
        });
        srcListAddMenuItem.setOnAction(_ -> chooseSrc(null, ProfileSettingsEnum.src_dir, "MainFrame.ChooseRomsSource"));
    }

    private void initDestDragNDrop() {
        new DragNDrop(romsDest).addDir(txt -> session.getCurrProfile().setProperty(ProfileSettingsEnum.roms_dest_dir, txt));
        new DragNDrop(disksDest).addDir(txt -> session.getCurrProfile().setProperty(ProfileSettingsEnum.disks_dest_dir, txt));
        new DragNDrop(swDest).addDir(txt -> session.getCurrProfile().setProperty(ProfileSettingsEnum.swroms_dest_dir, txt));
        new DragNDrop(swDisksDest).addDir(txt -> session.getCurrProfile().setProperty(ProfileSettingsEnum.swdisks_dest_dir, txt));
        new DragNDrop(samplesDest).addDir(txt -> session.getCurrProfile().setProperty(ProfileSettingsEnum.samples_dest_dir, txt));
        new DragNDrop(backupDest).addDir(txt -> session.getCurrProfile().setProperty(ProfileSettingsEnum.backup_dest_dir, txt));
        new DragNDrop(srcList)
                .addDirs(files -> session.getCurrProfile().setProperty(ProfileSettingsEnum.src_dir, String.join("|", files.stream().map(File::getAbsolutePath).toList())));
    }

    private void initFilters() {
        systemsFilter.setCellFactory(CheckBoxListCell.forListView(item -> {
            BooleanProperty observable = new SimpleBooleanProperty(item.isSelected(session.getCurrProfile()));
            observable.addListener((_, _, isNowSelected) -> {
                item.setSelected(session.getCurrProfile(), isNowSelected);
                ProfileViewer.getResetCounter().incrementAndGet();
            });
            return observable;
        }));

        sourcesFilter.setCellFactory(CheckBoxListCell.forListView(item -> {
            BooleanProperty observable = new SimpleBooleanProperty(item.isSelected(session.getCurrProfile()));
            observable.addListener((_, _, isNowSelected) -> {
                item.setSelected(session.getCurrProfile(), isNowSelected);
                ProfileViewer.getResetCounter().incrementAndGet();
            });
            return observable;
        }));

        cbbxDriverStatus.setItems(FXCollections.observableArrayList(Driver.StatusType.values()));
        cbbxFilterCabinetType.setItems(FXCollections.observableArrayList(CabinetType.values()));
        cbbxFilterDisplayOrientation.setItems(FXCollections.observableArrayList(DisplayOrientation.values()));
        cbbxSWMinSupportedLvl.setItems(FXCollections.observableArrayList(Supported.values()));
        chckbxIncludeClones.setOnAction(_ -> {
            session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_InclClones, chckbxIncludeClones.isSelected());
            ProfileViewer.getResetCounter().incrementAndGet();
        });
        chckbxIncludeDisks.setOnAction(_ -> {
            session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_InclDisks, chckbxIncludeDisks.isSelected());
            ProfileViewer.getResetCounter().incrementAndGet();
        });
        chckbxIncludeSamples.setOnAction(_ -> {
            session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_InclSamples, chckbxIncludeSamples.isSelected());
            ProfileViewer.getResetCounter().incrementAndGet();
        });
        cbbxFilterCabinetType.setOnAction(_ -> {
            session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_CabinetType, cbbxFilterCabinetType.getValue().toString());
            ProfileViewer.getResetCounter().incrementAndGet();
        });
        cbbxFilterDisplayOrientation.setOnAction(_ -> {
            session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_DisplayOrientation, cbbxFilterDisplayOrientation.getValue().toString());
            ProfileViewer.getResetCounter().incrementAndGet();
        });
        cbbxDriverStatus.setOnAction(_ -> {
            session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_DriverStatus, cbbxDriverStatus.getValue().toString());
            ProfileViewer.getResetCounter().incrementAndGet();
        });
        cbbxSWMinSupportedLvl.setOnAction(_ -> {
            session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_MinSoftwareSupportedLevel, cbbxSWMinSupportedLvl.getValue().toString());
            ProfileViewer.getResetCounter().incrementAndGet();
        });
        cbbxYearMin.setOnAction(_ -> {
            session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_YearMin, cbbxYearMin.getValue());
            ProfileViewer.getResetCounter().incrementAndGet();
        });
        cbbxYearMax.setOnAction(_ -> {
            session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_YearMax, cbbxYearMax.getValue());
            ProfileViewer.getResetCounter().incrementAndGet();
        });
    }

    private void initNPlayers() {
        new DragNDrop(tfNPlayers).addFile(this::selectNPlayersFile);
        listNPlayers.setCellFactory(CheckBoxListCell.forListView(item -> {
            BooleanProperty observable = new SimpleBooleanProperty(item.isSelected(session.getCurrProfile()));
            observable.addListener((_, _, isNowSelected) -> {
                item.setSelected(session.getCurrProfile(), isNowSelected);
                ProfileViewer.getResetCounter().incrementAndGet();
            });
            return observable;
        }));
    }

    private void initCatVer() {
        new DragNDrop(tfCatVer).addFile(this::selectCatVerFile);
        treeCatVer.setCellFactory(CheckBoxTreeCell.forTreeView(item -> {
            if (item instanceof CheckBoxTreeItem<?> i)
                return i.selectedProperty();
            return null;
        }, new StringConverter<TreeItem<PropertyStub>>() {

            @Override
            public String toString(TreeItem<PropertyStub> object) {
                return object.getValue().toString();
            }

            @Override
            public TreeItem<PropertyStub> fromString(String string) {
                return null;
            }
        }));
    }

    private void initAutomation() {
        cbAutomation.setItems(FXCollections.observableArrayList(ScanAutomation.values()));
        cbAutomation.setCellFactory(_ -> new DescriptorCellFactory());
        cbAutomation.setButtonCell(cbAutomation.getCellFactory().call(null));
        cbAutomation.setOnAction(_ -> session.getCurrProfile().setProperty(ProfileSettingsEnum.automation_scan, cbAutomation.getValue().toString()));
    }

    private void initImportExport() {
        importBtn.setOnAction(_ -> {
            final var filters = Arrays.asList(new ExtensionFilter("Properties", "*.properties"));
            final var presets = session.getUser().getSettings().getWorkPath().resolve("presets");
            chooseOpenFile(importBtn, null, presets.toFile(), filters, file -> {
                session.getCurrProfile().loadSettings(PathAbstractor.getAbsolutePath(session, file.toString()).toFile());
                session.getCurrProfile().loadCatVer(null);
                session.getCurrProfile().loadNPlayers(null);
                initProfileSettings(session);
            });
        });

        exportBtn.setOnAction(_ -> {
            final var filters = Arrays.asList(new ExtensionFilter("Properties", "*.properties"));
            final var presets = session.getUser().getSettings().getWorkPath().resolve("presets");
            try {
                Files.createDirectories(presets);
                chooseSaveFile(exportBtn, null, presets.toFile(), filters,
                        file -> session.getCurrProfile().saveSettings(PathAbstractor.getAbsolutePath(session, file.toString()).toFile()));
            } catch (IOException e1) {
                Log.err(e1.getMessage(), e1);
            }
        });
    }

    @Override
    public void loadProfile(Session session, ProfileNFO profile) {
        if (session.getCurrProfile() != null)
            session.getCurrProfile().saveSettings();

        if (MainFrame.getProfileViewer() != null)
            MainFrame.getProfileViewer().clear();

        ProgressTaskRunner.run((Stage) romsDest.getScene().getWindow(), stage -> new ProgressTask<Profile>(stage) {
            @Override
            protected Profile call() throws Exception {
                return Profile.load(session, profile, this);
            }

            @Override
            protected void succeeded() {
                try {
                    final var profile = get();
                    session.getReport().setProfile(session.getCurrProfile());
                    MainFrame.getReportFrame().setNeedUpdate(true);

                    ProfileViewer.getResetCounter().incrementAndGet();

                    MainFrame.getController().getScannerPanelTab().setDisable(profile == null);
                    scanBtn.setDisable(profile == null);
                    fixBtn.setDisable(true);
                    if (profile != null && session.getCurrProfile() != null) {
                        profileinfoLbl.getChildren().setAll(NeutralToNodeFormatter.toNodes(session.getCurrProfile().getName()));
                        systemsFilter.setItems(FXCollections.observableList(session.getCurrProfile().getSystems().getSystems()));
                        sourcesFilter.setItems(FXCollections.observableList(session.getCurrProfile().getSources().getSrces()));
                        initProfileSettings(session);
                        MainFrame.getController().getTabPane().getSelectionModel().select(1);
                        MainFrame.getController().getProfilePanelController().refreshList();
                    }
                    this.close();
                } catch (InterruptedException e) // NOSONAR
                {
                    ProgressTaskRunner.handleInterruptedException(this, e);
                } catch (ExecutionException e) {
                    ProgressTaskRunner.handleExecutionException(this, e);
                }
            }

            @Override
            protected void failed() {
                ProgressTaskRunner.handleFailedException(this, getException());
            }
        });
    }

    @FXML
    private void scan(ActionEvent e) {
        scan(session, true);
    }

    /**
     * Scan.
     */
    private void scan(final Session session, final boolean automate) {
        String txtdstdir = romsDest.getText();
        if (txtdstdir.isEmpty()) {
            romsDestBtn.fire();
            txtdstdir = romsDest.getText();
        }
        if (txtdstdir.isEmpty())
            return;
        ProgressTaskRunner.run((Stage) romsDest.getScene().getWindow(), stage -> scanProgressTask(session, automate, stage));
    }

    private ProgressTask<Scan> scanProgressTask(final Session session, final boolean automate, Stage stage) throws IOException, URISyntaxException {
        return new ProgressTask<Scan>(stage) {
            @Override
            protected Scan call() throws Exception {
                return new Scan(session.getCurrProfile(), this);
            }

            @Override
            protected void succeeded() {
                try {
                    handleScanSuccess(session, automate, get(), this);
                } catch (InterruptedException e) // NOSONAR
                {
                    ProgressTaskRunner.handleInterruptedException(this, e);
                } catch (Exception e) {
                    ProgressTaskRunner.handleExecutionException(this, e);
                }
            }

            @Override
            protected void failed() {
                ProgressTaskRunner.handleFailedException(this, getException());
            }
        };
    }

    private void handleScanSuccess(final Session session, final boolean automate, final Scan scan, final ProgressTask<Scan> task) {
        session.setCurrScan(scan);
        updateFixButtonState(session);
        task.close();
        updateProfileViewer();
        handleReportFrame(session);
        if (automate && shouldAutoFix(session)) {
            fix(session);
        }
    }

    private void updateFixButtonState(final Session session) {
        boolean hasActions = session.getCurrScan() != null 
            && session.getCurrScan().actions.stream().mapToInt(Collection::size).sum() > 0;
        fixBtn.setDisable(!hasActions);
    }

    private void handleReportFrame(final Session session) {
        if (MainFrame.getReportFrame() == null)
            return;
        ScanAutomation automation = ScanAutomation.valueOf(
            session.getCurrProfile().getSettings().getProperty(ProfileSettingsEnum.automation_scan));
        if (automation.hasReport())
            MainFrame.getReportFrame().setVisible();
        MainFrame.getReportFrame().setNeedUpdate(true);
    }

    private boolean shouldAutoFix(final Session session) {
        ScanAutomation automation = ScanAutomation.valueOf(
            session.getCurrProfile().getSettings().getProperty(ProfileSettingsEnum.automation_scan));
        return !fixBtn.isDisabled() && automation.hasFix();
    }

    @FXML
    private void report(ActionEvent evt) {
        MainFrame.getReportFrame().setVisible();
    }

    @FXML
    private void fix(ActionEvent e) {
        fix(session);
    }

    /**
     * Fix.
     */
    private void fix(final Session session) {
        ProgressTaskRunner.run((Stage) romsDest.getScene().getWindow(), stage -> new ProgressTask<Fix>(stage) {
            private boolean toFix = false;

            @Override
            protected Fix call() throws Exception {
                if (!confirmRescanIfNeeded(session, this))
                    return null;
                final Fix fix = new Fix(session.getCurrProfile(), session.getCurrScan(), this);
                toFix = fix.getActionsRemain() > 0;
                return fix;
            }

            @Override
            protected void succeeded() {
                try {
                    get();
                    fixBtn.setDisable(!toFix);
                    close();
                    updateProfileViewer();
                    runScanAutomation(session);
                } catch (InterruptedException e) //NOSONAR
                {
                    ProgressTaskRunner.handleInterruptedException(this, e);
                } catch (Exception e) {
                    ProgressTaskRunner.handleExecutionException(this, e);
                }
            }

            @Override
            protected void failed() {
                ProgressTaskRunner.handleFailedException(this, getException());
            }
        });
    }

    /**
     * Ask user to rescan before fix if settings changed.
     * @return true to proceed with fix, false to cancel
     */
    private boolean confirmRescanIfNeeded(final Session session, final ProgressTask<?> task) throws Exception /*NOSONAR*/ {
        if (!session.getCurrProfile().hasPropsChanged())
            return true;
        final var answer = Dialogs.showConfirmation(Messages.getString("MainFrame.WarnSettingsChanged"), Messages.getString("MainFrame.RescanBeforeFix"),
                ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        if (answer.isEmpty())
            return true;
        if (answer.get() == ButtonType.CANCEL)
            return false;
        if (answer.get() == ButtonType.YES) {
            session.setCurrScan(new Scan(session.getCurrProfile(), task));
            return session.getCurrScan().actions.stream().mapToInt(Collection::size).sum() > 0;
        }
        return true;
    }

    private void updateProfileViewer() {
        if (MainFrame.getProfileViewer() != null)
            MainFrame.getProfileViewer().reload();
    }

    private void runScanAutomation(final Session session) {
        ScanAutomation automation = ScanAutomation.valueOf(session.getCurrProfile().getSettings().getProperty(ProfileSettingsEnum.automation_scan));
        if (automation.hasScanAgain())
            scan(session, false);
    }

    private void initProfileSettings(Session session) {
        initDestSettings(session);
        initSrcListSettings(session);
        scannerPanelSettingsController.initProfileSettings(session.getCurrProfile().getSettings());
        initFilterSettings(session);
        showNPlayers();
        showCatVer();
        initAutomationSettings(session);
    }

    private void initDestSettings(Session session) {
        romsDest.setText(session.getCurrProfile().getProperty(ProfileSettingsEnum.roms_dest_dir));
        disksDestCB.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.disks_dest_dir_enabled, Boolean.class));
        disksDest.setText(session.getCurrProfile().getProperty(ProfileSettingsEnum.disks_dest_dir));
        swDestCB.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.swroms_dest_dir_enabled, Boolean.class));
        swDest.setText(session.getCurrProfile().getProperty(ProfileSettingsEnum.swroms_dest_dir));
        swDisksDestCB.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.swdisks_dest_dir_enabled, Boolean.class));
        swDisksDest.setText(session.getCurrProfile().getProperty(ProfileSettingsEnum.swdisks_dest_dir));
        samplesDestCB.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.samples_dest_dir_enabled, Boolean.class));
        samplesDest.setText(session.getCurrProfile().getProperty(ProfileSettingsEnum.samples_dest_dir));
        backupDestCB.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.backup_dest_dir_enabled, Boolean.class));
        backupDest.setText(session.getCurrProfile().getProperty(ProfileSettingsEnum.backup_dest_dir));
    }

    private void initSrcListSettings(Session session) {
        srcList.setItems(FXCollections.observableList(Stream.of(StringUtils.split(session.getCurrProfile().getProperty(ProfileSettingsEnum.src_dir), '|'))
                .filter(s -> !s.isEmpty())
                .map(File::new)
                .collect(Collectors.toList()))); //NOSONAR
    }

    private void initFilterSettings(Session session) {
        chckbxIncludeClones.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_InclClones, Boolean.class));
        chckbxIncludeDisks.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_InclDisks, Boolean.class));
        chckbxIncludeSamples.setSelected(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_InclSamples, Boolean.class));
        cbbxDriverStatus.getSelectionModel().select(Driver.StatusType.valueOf(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_DriverStatus)));
        cbbxFilterCabinetType.getSelectionModel().select(CabinetType.valueOf(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_CabinetType)));
        cbbxFilterDisplayOrientation.getSelectionModel().select(DisplayOrientation.valueOf(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_DisplayOrientation)));
        cbbxSWMinSupportedLvl.getSelectionModel().select(Supported.valueOf(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_MinSoftwareSupportedLevel)));
        cbbxYearMin.setItems(FXCollections.observableArrayList(session.getCurrProfile().getYears()).sorted());
        cbbxYearMin.getSelectionModel().select(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_YearMin));
        cbbxYearMax.setItems(FXCollections.observableArrayList(session.getCurrProfile().getYears()).sorted());
        cbbxYearMax.getSelectionModel().select(session.getCurrProfile().getProperty(ProfileSettingsEnum.filter_YearMax));
    }

    private void initAutomationSettings(Session session) {
        cbAutomation.getSelectionModel().select(ScanAutomation.valueOf(session.getCurrProfile().getProperty(ProfileSettingsEnum.automation_scan)));
    }

    private void selectNPlayersFile(String file) {
        if (Files.isRegularFile(Path.of(file))) {
            session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_nplayers_ini, file);
            session.getCurrProfile().loadNPlayers(null);
            showNPlayers();
        }
    }

    private void showNPlayers() {
        tfNPlayers.setText(session.getCurrProfile().getNplayers() != null ? session.getCurrProfile().getNplayers().file.getAbsolutePath() : null);
        listNPlayers.setItems(Optional.ofNullable(session.getCurrProfile().getNplayers()).map(NPlayers::getListNPlayers).map(FXCollections::observableArrayList).orElse(null));
    }

    private void selectCatVerFile(String file) {
        if (Files.isRegularFile(Path.of(file))) {
            session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_catver_ini, file);
            session.getCurrProfile().loadCatVer(null);
            showCatVer();
        }
    }

    private void showCatVer() {
        tfCatVer.setText(session.getCurrProfile().getCatver() != null ? session.getCurrProfile().getCatver().file.getAbsolutePath() : null);

        final var root = session.getCurrProfile().getCatver();
        if (root != null) {
            final var rootitem = new CheckBoxTreeItem<PropertyStub>(root);
            rootitem.setExpanded(true);
            session.getCurrProfile().getCatver().forEach(cat -> {
                final var catitem = new CheckBoxTreeItem<PropertyStub>(cat);
                rootitem.getChildren().add(catitem);
                cat.forEach(subcat -> catitem.getChildren().add(new CheckBoxTreeItem<>(subcat)));
            });
            treeCatVer.setRoot(rootitem);

            rootitem.selectedProperty().addListener((_, _, newvalue) -> root.setSelected(newvalue));
            rootitem.getChildren().forEach(catitem -> {
                ((CheckBoxTreeItem<PropertyStub>) catitem).selectedProperty()
                        .addListener((_, _, newvalue) -> ((Category) catitem.getValue()).setSelected(newvalue));
                catitem.getChildren().forEach(subcatitem -> {
                    ((CheckBoxTreeItem<PropertyStub>) subcatitem).selectedProperty().addListener((_, _, newvalue) -> {
                        ((SubCategory) subcatitem.getValue()).setSelected(newvalue);
                        treeCatVer.refresh();
                        ProfileViewer.getResetCounter().incrementAndGet();
                    });
                    ((CheckBoxTreeItem<PropertyStub>) subcatitem).setSelected(((SubCategory) subcatitem.getValue()).isSelected());
                });
            });

        } else
            treeCatVer.setRoot(null);

    }

    @FXML
    private void chooseRomsDest(ActionEvent e) {
        chooseAnyDest(romsDest, ProfileSettingsEnum.roms_dest_dir, "MainFrame.ChooseRomsDestination");
    }

    @FXML
    private void chooseDisksDest(ActionEvent e) {
        chooseAnyDest(disksDest, ProfileSettingsEnum.disks_dest_dir, "MainFrame.ChooseDisksDestination");
    }

    @FXML
    private void chooseSWRomsDest(ActionEvent e) {
        chooseAnyDest(swDest, ProfileSettingsEnum.swroms_dest_dir, "MainFrame.ChooseSWRomsDestination");
    }

    @FXML
    private void chooseSWDisksDest(ActionEvent e) {
        chooseAnyDest(swDisksDest, ProfileSettingsEnum.swdisks_dest_dir, "MainFrame.ChooseSWDisksDestination");
    }

    @FXML
    private void chooseSamplesDest(ActionEvent e) {
        chooseAnyDest(samplesDest, ProfileSettingsEnum.samples_dest_dir, "MainFrame.ChooseSamplesDestination");
    }

    @FXML
    private void chooseBackupDest(ActionEvent e) {
        chooseAnyDest(backupDest, ProfileSettingsEnum.backup_dest_dir, "MainFrame.ChooseBackupDestination");
    }

    private void chooseAnyDest(TextField tf, ProfileSettingsEnum ppt, String defPptName) {
        final var workdir = session.getUser().getSettings().getWorkPath().toFile();
        final var defdir = PathAbstractor.getAbsolutePath(session, session.getUser().getSettings().getProperty(defPptName, workdir.getAbsolutePath())).toFile();
        chooseDir(tf, tf.getText(), defdir, dir -> {
            tf.setText(dir.toString());
            session.getUser().getSettings().setProperty(defPptName, tf.getText()); // $NON-NLS-1$
            session.getCurrProfile().setProperty(ppt, tf.getText()); // $NON-NLS-1$
        });
    }

    private void chooseSrc(File oldDir, ProfileSettingsEnum ppt, String defPptName) /*NOSONAR*/ {
        final var workdir = session.getUser().getSettings().getWorkPath().toFile();
        final var defdir = PathAbstractor.getAbsolutePath(session, session.getUser().getSettings().getProperty(defPptName, workdir.getAbsolutePath())).toFile();
        chooseDir(srcList, oldDir != null ? oldDir.toString() : null, defdir, dir -> {
            var modified = false;
            if (oldDir != null) {
                if (!oldDir.equals(dir.toFile())) {
                    final var i = srcList.getItems().indexOf(oldDir);
                    srcList.getItems().set(i, dir.toFile());
                    modified = true;
                }
            } else {
                if (-1 == srcList.getItems().indexOf(dir.toFile())) {
                    srcList.getItems().add(dir.toFile());
                    modified = true;
                }
            }
            if (modified) {
                saveSrcList();
                session.getUser().getSettings().setProperty(defPptName, dir.toString()); // $NON-NLS-1$
            }
        });
    }

    private void saveSrcList() {
        session.getCurrProfile().setProperty(ProfileSettingsEnum.src_dir, String.join("|", srcList.getItems().stream().map(File::getAbsolutePath).toList()));
    }

    @FXML
    private void infos(ActionEvent evt) {
        if (MainFrame.getProfileViewer() == null) {
            try {
                MainFrame.setProfileViewer(new ProfileViewer((Stage) infosBtn.getScene().getWindow()));
            } catch (IOException | URISyntaxException e) {
                Log.err(e.getMessage(), e);
            }
        }
        if (MainFrame.getProfileViewer() != null) {
            MainFrame.getProfileViewer().show();
            MainFrame.applyCSS();
            MainFrame.getProfileViewer().reload();
        }
    }

    @FXML
    private void systemsFilterSelectAll() {
        FilterSelectionHelper.selectAll(session.getCurrProfile().getSystems(), session.getCurrProfile(),
            Systm::setSelected, systemsFilter);
    }

    @FXML
    private void systemsFilterSelectAllBios() {
        FilterSelectionHelper.selectFiltered(session.getCurrProfile().getSystems(), session.getCurrProfile(),
            Systm::setSelected, s -> s.getType() == Systm.Type.BIOS, systemsFilter);
    }

    @FXML
    private void systemsFilterSelectAllSoftwares() {
        FilterSelectionHelper.selectFiltered(session.getCurrProfile().getSystems(), session.getCurrProfile(),
            Systm::setSelected, s -> s.getType() == Systm.Type.SOFTWARELIST, systemsFilter);
    }

    @FXML
    private void systemsFilterUnselectAll() {
        FilterSelectionHelper.unselectAll(session.getCurrProfile().getSystems(), session.getCurrProfile(),
            Systm::setSelected, systemsFilter);
    }

    @FXML
    private void systemsFilterUnselectAllBios() {
        FilterSelectionHelper.unselectFiltered(session.getCurrProfile().getSystems(), session.getCurrProfile(),
            Systm::setSelected, s -> s.getType() == Systm.Type.BIOS, systemsFilter);
    }

    @FXML
    private void systemsFilterUnselectAllSoftwares() {
        FilterSelectionHelper.unselectFiltered(session.getCurrProfile().getSystems(), session.getCurrProfile(),
            Systm::setSelected, s -> s.getType() == Systm.Type.SOFTWARELIST, systemsFilter);
    }

    @FXML
    private void systemsFilterInvertSelection() {
        FilterSelectionHelper.invertSelection(session.getCurrProfile().getSystems(), session.getCurrProfile(),
            Systm::setSelected, Systm::isSelected, systemsFilter);
    }

    @FXML
    private void sourcesFilterSelectAll() {
        FilterSelectionHelper.selectAll(session.getCurrProfile().getSources(), session.getCurrProfile(),
            Source::setSelected, sourcesFilter);
    }

    @FXML
    private void sourcesFilterUnselectAll() {
        FilterSelectionHelper.unselectAll(session.getCurrProfile().getSources(), session.getCurrProfile(),
            Source::setSelected, sourcesFilter);
    }

    @FXML
    private void sourcesFilterInvertSelection() {
        FilterSelectionHelper.invertSelection(session.getCurrProfile().getSources(), session.getCurrProfile(),
            Source::setSelected, Source::isSelected, sourcesFilter);
    }

    @FXML
    void nPlayersListSelectAll() {
        FilterSelectionHelper.selectAll(session.getCurrProfile().getNplayers(), session.getCurrProfile(),
            NPlayer::setSelected, listNPlayers);
    }

    @FXML
    void nPlayersListSelectNone() {
        FilterSelectionHelper.unselectAll(session.getCurrProfile().getNplayers(), session.getCurrProfile(),
            NPlayer::setSelected, listNPlayers);
    }

    @FXML
    void nPlayersListSelectInvert() {
        FilterSelectionHelper.invertSelection(session.getCurrProfile().getNplayers(), session.getCurrProfile(),
            NPlayer::setSelected, NPlayer::isSelected, listNPlayers);
    }

    @FXML
    void nPlayersListClear() {
        session.getCurrProfile().saveSettings();
        session.getCurrProfile().setNplayers(null);
        session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_nplayers_ini, null);
        session.getCurrProfile().saveSettings();
        tfNPlayers.setText(null);
        listNPlayers.setItems(null);
    }

    private Stream<CheckBoxTreeItem<PropertyStub>> streamSubCatItems() {
        return (treeCatVer.getRoot()).getChildren().stream().flatMap(t -> t.getChildren().stream()).map(t -> (CheckBoxTreeItem<PropertyStub>) t);
    }

    @FXML
    void catVerListSelectAll() {
        streamSubCatItems().forEachOrdered(subcat -> subcat.setSelected(true));
        treeCatVer.refresh();
        ProfileViewer.getResetCounter().incrementAndGet();
    }

    @FXML
    void catVerListUnselectAll() {
        streamSubCatItems().forEachOrdered(subcat -> subcat.setSelected(false));
        treeCatVer.refresh();
        ProfileViewer.getResetCounter().incrementAndGet();
    }

    private static final String MATURE = "* Mature *";

    private Stream<CheckBoxTreeItem<PropertyStub>> streamMatureItems() {
        return streamSubCatItems().filter(t -> t.getValue() instanceof SubCategory subcat && (subcat.name.endsWith(MATURE) || subcat.getParent().name.endsWith(MATURE)));
    }

    @FXML
    void catVerListSelectMature() {
        streamMatureItems().forEachOrdered(subcat -> subcat.setSelected(true));
        treeCatVer.refresh();
        ProfileViewer.getResetCounter().incrementAndGet();
    }

    @FXML
    void catVerListUnselectMature() {
        streamMatureItems().forEachOrdered(subcat -> subcat.setSelected(false));
        treeCatVer.refresh();
        ProfileViewer.getResetCounter().incrementAndGet();
    }

    @FXML
    void catVerListClear() {
        session.getCurrProfile().saveSettings();
        session.getCurrProfile().setCatver(null);
        session.getCurrProfile().setProperty(ProfileSettingsEnum.filter_catver_ini, null);
        session.getCurrProfile().saveSettings();
        tfCatVer.setText(null);
        treeCatVer.setRoot(null);
    }

    private void initDestCheckBox(CheckBox cb, TextField tf, Button btn, ProfileSettingsEnum enabledProp) {
        cb.selectedProperty().addListener((_, _, newValue) -> {
            tf.setDisable(!newValue);
            btn.setDisable(!newValue);
            session.getCurrProfile().setProperty(enabledProp, newValue);
        });
    }
}
