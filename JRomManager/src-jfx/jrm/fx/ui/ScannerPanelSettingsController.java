package jrm.fx.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import jrm.fx.ui.controls.DescriptorCellFactory;
import jrm.misc.ProfileSettings;
import jrm.misc.ProfileSettingsEnum;
import jrm.profile.scan.options.Descriptor;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import lombok.Getter;

public class ScannerPanelSettingsController implements Initializable
{

	@FXML	private CheckBox needSHA1Chkbx;
	@FXML	private CheckBox useParallelismChkbx;
	@FXML	private CheckBox createMissingSetsChkbx;
	@FXML	private CheckBox createOnlyCompleteChkbx;
	@FXML	private CheckBox ignoreUnneededContainersChkbx;
	@FXML	private CheckBox ignoreUnneededEntriesChkbx;
	@FXML	private CheckBox ignoreUnknownContainersChkbx;
	@FXML	private CheckBox useImplicitMergeChkbx;
	@FXML	private CheckBox ignoreMergeNameRomsChkbx;
	@FXML	private CheckBox ignoreMergeNameDisksChkbx;
	@FXML	private CheckBox excludeGamesChkbx;
	@FXML	private CheckBox excludeMachinesChkbx;
	@FXML	private CheckBox backupChkbx;
	@FXML	private ComboBox<Descriptor> compressionCbx;
	@FXML	private ComboBox<Descriptor> mergeModeCbx;
	@FXML	private ComboBox<Descriptor> collisionModeCbx;
	
	@FXML	private Pane settingsPane;
	
	@FXML 	private ContextMenu catVerMenu;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		final Callback<ListView<Descriptor>, ListCell<Descriptor>> cellFactory = param -> new DescriptorCellFactory();
		needSHA1Chkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.need_sha1_or_md5, newValue));
		useParallelismChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.use_parallelism, newValue));
		createMissingSetsChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.create_mode, newValue));
		createOnlyCompleteChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.createfull_mode, newValue));
		ignoreUnneededContainersChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.ignore_unneeded_containers, newValue));
		ignoreUnneededEntriesChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.ignore_unneeded_entries, newValue));
		ignoreUnknownContainersChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.ignore_unknown_containers, newValue));
		useImplicitMergeChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.implicit_merge, newValue));
		ignoreMergeNameRomsChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.ignore_merge_name_roms, newValue));
		ignoreMergeNameDisksChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.ignore_merge_name_disks, newValue));
		excludeGamesChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.exclude_games, newValue));
		excludeMachinesChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.exclude_machines, newValue));
		backupChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.backup, newValue));
		compressionCbx.setItems(FXCollections.observableArrayList(FormatOptions.values()));
		compressionCbx.setCellFactory(cellFactory);
		compressionCbx.setButtonCell(cellFactory.call(null));
		compressionCbx.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.format, newValue.toString()));
		mergeModeCbx.setItems(FXCollections.observableArrayList(MergeOptions.values()));
		mergeModeCbx.setCellFactory(cellFactory);
		mergeModeCbx.setButtonCell(cellFactory.call(null));
		mergeModeCbx.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.merge_mode, newValue.toString()));
		collisionModeCbx.setItems(FXCollections.observableArrayList(HashCollisionOptions.values()));
		collisionModeCbx.setCellFactory(cellFactory);
		collisionModeCbx.setButtonCell(cellFactory.call(null));
		collisionModeCbx.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> settings.setProperty(ProfileSettingsEnum.hash_collision_mode, newValue.toString()));
		settingsPane.setOnContextMenuRequested(event -> {
			catVerMenu.show(settingsPane, event.getScreenX(), event.getScreenY());
			event.consume();
		});
		settingsPane.setOnMousePressed(e -> catVerMenu.hide());
	}

	private @Getter ProfileSettings settings; 
	
	void initProfileSettings(final ProfileSettings settings)
	{
		this.settings = settings;
		needSHA1Chkbx.setSelected(settings.getProperty(ProfileSettingsEnum.need_sha1_or_md5, Boolean.class));
		useParallelismChkbx.setSelected(settings.getProperty(ProfileSettingsEnum.use_parallelism, Boolean.class));
		createMissingSetsChkbx.setSelected(settings.getProperty(ProfileSettingsEnum.create_mode, Boolean.class));
		createOnlyCompleteChkbx.setSelected(settings.getProperty(ProfileSettingsEnum.createfull_mode, Boolean.class));
		ignoreUnneededContainersChkbx.setSelected(settings.getProperty(ProfileSettingsEnum.ignore_unneeded_containers, Boolean.class));
		ignoreUnneededEntriesChkbx.setSelected(settings.getProperty(ProfileSettingsEnum.ignore_unneeded_entries, Boolean.class));
		ignoreUnknownContainersChkbx.setSelected(settings.getProperty(ProfileSettingsEnum.ignore_unknown_containers, Boolean.class));
		useImplicitMergeChkbx.setSelected(settings.getProperty(ProfileSettingsEnum.implicit_merge, Boolean.class));
		ignoreMergeNameRomsChkbx.setSelected(settings.getProperty(ProfileSettingsEnum.ignore_merge_name_roms, Boolean.class));
		ignoreMergeNameDisksChkbx.setSelected(settings.getProperty(ProfileSettingsEnum.ignore_merge_name_disks, Boolean.class));
		excludeGamesChkbx.setSelected(settings.getProperty(ProfileSettingsEnum.exclude_games, Boolean.class));
		excludeMachinesChkbx.setSelected(settings.getProperty(ProfileSettingsEnum.exclude_machines, Boolean.class));
		backupChkbx.setSelected(settings.getProperty(ProfileSettingsEnum.backup, Boolean.class));
		compressionCbx.getSelectionModel().select(FormatOptions.valueOf(settings.getProperty(ProfileSettingsEnum.format)));
		mergeModeCbx.getSelectionModel().select(MergeOptions.valueOf(settings.getProperty(ProfileSettingsEnum.merge_mode)));
		collisionModeCbx.getSelectionModel().select(HashCollisionOptions.valueOf(settings.getProperty(ProfileSettingsEnum.hash_collision_mode)));
	}
	
	@FXML private void pdMameMergedPreset()
	{
		createMissingSetsChkbx.setSelected(true);
		createOnlyCompleteChkbx.setSelected(false);
		ignoreUnneededContainersChkbx.setSelected(false);
		ignoreUnneededEntriesChkbx.setSelected(false);
		ignoreUnknownContainersChkbx.setSelected(true);
		useImplicitMergeChkbx.setSelected(true);
		ignoreMergeNameDisksChkbx.setSelected(true); // Don't remove _ReadMe_.txt
		ignoreMergeNameRomsChkbx.setSelected(false);
		compressionCbx.getSelectionModel().select(FormatOptions.TZIP);
		mergeModeCbx.getSelectionModel().select(MergeOptions.MERGE);
		collisionModeCbx.getSelectionModel().select(HashCollisionOptions.HALFDUMB);
	}
	
	@FXML private void pdMameNonMergedPreset()
	{
		createMissingSetsChkbx.setSelected(true);
		createOnlyCompleteChkbx.setSelected(false);
		ignoreUnneededContainersChkbx.setSelected(false);
		ignoreUnneededEntriesChkbx.setSelected(false);
		ignoreUnknownContainersChkbx.setSelected(true); // Don't remove _ReadMe_.txt
		useImplicitMergeChkbx.setSelected(true);
		ignoreMergeNameDisksChkbx.setSelected(true);
		ignoreMergeNameRomsChkbx.setSelected(false);
		compressionCbx.getSelectionModel().select(FormatOptions.TZIP);
		mergeModeCbx.getSelectionModel().select(MergeOptions.SUPERFULLNOMERGE);
	}
	
	@FXML private void pdMameSplitPreset()
	{
		createMissingSetsChkbx.setSelected(true);
		createOnlyCompleteChkbx.setSelected(false);
		ignoreUnneededContainersChkbx.setSelected(false);
		ignoreUnneededEntriesChkbx.setSelected(false);
		ignoreUnknownContainersChkbx.setSelected(true); // Don't remove _ReadMe_.txt
		useImplicitMergeChkbx.setSelected(true);
		ignoreMergeNameDisksChkbx.setSelected(true);
		ignoreMergeNameRomsChkbx.setSelected(false);
		compressionCbx.getSelectionModel().select(FormatOptions.TZIP);
		mergeModeCbx.getSelectionModel().select(MergeOptions.SPLIT);
	}
}
