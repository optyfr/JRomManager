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
import jrm.misc.SettingsEnum;
import jrm.profile.scan.options.Descriptor;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.security.Session;
import jrm.security.Sessions;

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
	
	private final Session session = Sessions.getSingleSession();

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		final Callback<ListView<Descriptor>, ListCell<Descriptor>> cellFactory = param -> new DescriptorCellFactory();
		needSHA1Chkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.need_sha1_or_md5, newValue));
		useParallelismChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.use_parallelism, newValue));
		createMissingSetsChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.create_mode, newValue));
		createOnlyCompleteChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.createfull_mode, newValue));
		ignoreUnneededContainersChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.ignore_unneeded_containers, newValue));
		ignoreUnneededEntriesChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.ignore_unneeded_entries, newValue));
		ignoreUnknownContainersChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.ignore_unknown_containers, newValue));
		useImplicitMergeChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.implicit_merge, newValue));
		ignoreMergeNameRomsChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.ignore_merge_name_roms, newValue));
		ignoreMergeNameDisksChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.ignore_merge_name_disks, newValue));
		excludeGamesChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.exclude_games, newValue));
		excludeMachinesChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.exclude_machines, newValue));
		backupChkbx.selectedProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.backup, newValue));
		compressionCbx.setItems(FXCollections.observableArrayList(FormatOptions.values()));
		compressionCbx.setCellFactory(cellFactory);
		compressionCbx.setButtonCell(cellFactory.call(null));
		compressionCbx.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.format, newValue.toString()));
		mergeModeCbx.setItems(FXCollections.observableArrayList(MergeOptions.values()));
		mergeModeCbx.setCellFactory(cellFactory);
		mergeModeCbx.setButtonCell(cellFactory.call(null));
		mergeModeCbx.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.merge_mode, newValue.toString()));
		collisionModeCbx.setItems(FXCollections.observableArrayList(HashCollisionOptions.values()));
		collisionModeCbx.setCellFactory(cellFactory);
		collisionModeCbx.setButtonCell(cellFactory.call(null));
		collisionModeCbx.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> session.getCurrProfile().setProperty(SettingsEnum.hash_collision_mode, newValue.toString()));
		settingsPane.setOnContextMenuRequested(event -> {
			catVerMenu.show(settingsPane, event.getScreenX(), event.getScreenY());
			event.consume();
		});
		settingsPane.setOnMousePressed(e -> catVerMenu.hide());
	}

	void initProfileSettings(Session session)
	{
		needSHA1Chkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.need_sha1_or_md5, false));
		useParallelismChkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.use_parallelism, true));
		createMissingSetsChkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.create_mode, true));
		createOnlyCompleteChkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.createfull_mode, false));
		ignoreUnneededContainersChkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.ignore_unneeded_containers, false));
		ignoreUnneededEntriesChkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.ignore_unneeded_entries, false));
		ignoreUnknownContainersChkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.ignore_unknown_containers, false));
		useImplicitMergeChkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.implicit_merge, false));
		ignoreMergeNameRomsChkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.ignore_merge_name_roms, false));
		ignoreMergeNameDisksChkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.ignore_merge_name_disks, false));
		excludeGamesChkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.exclude_games, false));
		excludeMachinesChkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.exclude_machines, false));
		backupChkbx.setSelected(session.getCurrProfile().getProperty(SettingsEnum.backup, false));
		compressionCbx.getSelectionModel().select(FormatOptions.valueOf(session.getCurrProfile().getProperty(SettingsEnum.format, FormatOptions.ZIP.toString())));
		mergeModeCbx.getSelectionModel().select(MergeOptions.valueOf(session.getCurrProfile().getProperty(SettingsEnum.merge_mode, MergeOptions.SPLIT.toString())));
		collisionModeCbx.getSelectionModel().select(HashCollisionOptions.valueOf(session.getCurrProfile().getProperty(SettingsEnum.hash_collision_mode, HashCollisionOptions.SINGLEFILE.toString())));
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
