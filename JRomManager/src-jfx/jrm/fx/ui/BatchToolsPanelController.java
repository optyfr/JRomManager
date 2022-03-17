package jrm.fx.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jrm.aui.basic.SrcDstResult;
import jrm.batch.CompressorFormat;
import jrm.fx.ui.controls.DropCell;
import jrm.fx.ui.misc.DragNDrop;
import jrm.io.torrent.options.TrntChkMode;
import jrm.locale.Messages;
import jrm.misc.ProfileSettings;
import jrm.misc.SettingsEnum;
import jrm.security.PathAbstractor;

public class BatchToolsPanelController extends BaseController
{
	@FXML	Tab panelBatchToolsDat2Dir;
	@FXML	Tab panelBatchToolsDir2Torrent;
	@FXML	Tab panelBatchToolsCompressor;
	@FXML	Button btnBatchToolsDir2DatStart;
	@FXML	Button btnBatchToolsTrntChkStart;
	@FXML	Button btnBatchToolsCompressorStart;
	@FXML	Button btnBatchToolsCompressorClear;
	@FXML	ChoiceBox<TrntChkMode> cbbxBatchToolsTrntChk;
	@FXML	ChoiceBox<CompressorFormat> cbbxBatchToolsCompressorFormat;
	@FXML	CheckBox cbBatchToolsDat2DirDryRun;
	@FXML	CheckBox cbBatchToolsTrntChkDetectArchivedFolder;
	@FXML	CheckBox cbBatchToolsTrntChkRemoveUnknownFiles;
	@FXML	CheckBox cbBatchToolsTrntChkRemoveWrongSizedFiles;
	@FXML	CheckBox cbBatchToolsCompressorForce;
	@FXML	TableView<File> tvBatchToolsDat2DirSrc;
	@FXML	TableColumn<File, File> tvBatchToolsDat2DirSrcCol;
	@FXML	ContextMenu popupMenuSrc;
	@FXML	MenuItem mnDat2DirAddSrcDir;
	@FXML	MenuItem mnDat2DirDelSrcDir;
	@FXML	TableView<SrcDstResult> tvBatchToolsDat2DirDst;
	@FXML	TableColumn<SrcDstResult, String> tvBatchToolsDat2DirDstDatsCol;
	@FXML	TableColumn<SrcDstResult, String> tvBatchToolsDat2DirDstDirsCol;
	@FXML	TableColumn<SrcDstResult, String> tvBatchToolsDat2DirDstResultCol;
	@FXML	TableColumn<SrcDstResult, SrcDstResult> tvBatchToolsDat2DirDstDetailsCol;
	@FXML	TableColumn<SrcDstResult, Boolean> tvBatchToolsDat2DirDstSelCol;
	@FXML	ContextMenu popupMenuDst;
	@FXML	MenuItem mnDat2DirDelDstDat;
	@FXML	Menu mntmDat2DirDstPresets;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		panelBatchToolsDat2Dir.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/application_cascade.png")));
		panelBatchToolsDir2Torrent.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/drive_web.png")));
		panelBatchToolsCompressor.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/compress.png")));
		btnBatchToolsDir2DatStart.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_go.png")));
		btnBatchToolsTrntChkStart.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_go.png")));
		btnBatchToolsCompressorStart.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bullet_go.png")));
		btnBatchToolsCompressorClear.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/bin.png")));
		cbbxBatchToolsTrntChk.setItems(FXCollections.observableArrayList(TrntChkMode.values()));
		cbbxBatchToolsTrntChk.getSelectionModel().select(TrntChkMode.valueOf(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_mode)));
		cbbxBatchToolsTrntChk.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			session.getUser().getSettings().setProperty(SettingsEnum.trntchk_mode, newValue.toString());
			// cbRemoveWrongSizedFiles.setEnabled(newValue != TrntChkMode.FILENAME);
		});
		cbbxBatchToolsCompressorFormat.setItems(FXCollections.observableArrayList(CompressorFormat.values()));
		cbbxBatchToolsCompressorFormat.getSelectionModel().select(CompressorFormat.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.compressor_format))); // $NON-NLS-1$
		cbbxBatchToolsCompressorFormat.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.compressor_format, newValue.toString()));

		cbBatchToolsDat2DirDryRun.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_dry_run, Boolean.class));
		cbBatchToolsDat2DirDryRun.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.dat2dir_dry_run, newValue));

		cbBatchToolsTrntChkDetectArchivedFolder.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_detect_archived_folders, Boolean.class));
		cbBatchToolsTrntChkDetectArchivedFolder.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_detect_archived_folders, newValue));

		cbBatchToolsTrntChkRemoveWrongSizedFiles.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_remove_wrong_sized_files, Boolean.class));
		cbBatchToolsTrntChkRemoveWrongSizedFiles.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_remove_wrong_sized_files, newValue));

		cbBatchToolsTrntChkRemoveUnknownFiles.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_remove_unknown_files, Boolean.class));
		cbBatchToolsTrntChkRemoveUnknownFiles.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_remove_unknown_files, newValue));

		cbBatchToolsCompressorForce.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.compressor_force, Boolean.class));
		cbBatchToolsCompressorForce.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.compressor_force, newValue));

		new DragNDrop(tvBatchToolsDat2DirSrc).addDirs(dirs -> {
			tvBatchToolsDat2DirSrc.getItems().addAll(dirs);
			saveSrc();
		});
		tvBatchToolsDat2DirSrc.getItems().setAll(Stream.of(StringUtils.split(session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_srcdirs), '|')).filter(s->!s.isBlank()).map(File::new).toList());
		tvBatchToolsDat2DirSrcCol.setCellFactory(param -> new TableCell<File, File>()
		{
			@Override
			protected void updateItem(File item, boolean empty)
			{
				super.updateItem(item, empty);
				setText(empty?"":item.toString());
			}
		});
		tvBatchToolsDat2DirSrcCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public File getValue()
			{
				return param.getValue();
			}
		});
		popupMenuSrc.setOnShowing(e -> mnDat2DirDelSrcDir.setDisable(tvBatchToolsDat2DirSrc.getSelectionModel().isEmpty()));
		mnDat2DirDelSrcDir.setOnAction(e -> {
			tvBatchToolsDat2DirSrc.getItems().removeAll(tvBatchToolsDat2DirSrc.getSelectionModel().getSelectedItems());
			saveSrc();
		});
		mnDat2DirAddSrcDir.setOnAction(e -> chooseDir(tvBatchToolsDat2DirSrc, null, null, dir -> {
			tvBatchToolsDat2DirSrc.getItems().add(dir.toFile());
			saveSrc();
		}));
		tvBatchToolsDat2DirDst.getItems().setAll(SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_sdr)));
		tvBatchToolsDat2DirDstDatsCol.setCellFactory(param -> new DropCell(tvBatchToolsDat2DirDst, (sdrlist, files) -> {
			for (int i = 0; i < files.size(); i++)
				sdrlist.get(i).setSrc(PathAbstractor.getRelativePath(session, files.get(i).toPath()).toString());
			tvBatchToolsDat2DirDst.refresh();
			saveDst();
		}, false));
		tvBatchToolsDat2DirDstDatsCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public String getValue()
			{
				return param.getValue().getSrc();
			}
		});
		tvBatchToolsDat2DirDstDirsCol.setCellFactory(param -> new DropCell(tvBatchToolsDat2DirDst, (sdrlist, files) -> {
			for (int i = 0; i < files.size(); i++)
				sdrlist.get(i).setDst(PathAbstractor.getRelativePath(session, files.get(i).toPath()).toString());
			tvBatchToolsDat2DirDst.refresh();
			saveDst();
		}, true));
		tvBatchToolsDat2DirDstDirsCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public String getValue()
			{
				return param.getValue().getDst();
			}
		});
		tvBatchToolsDat2DirDstSelCol.setCellFactory(CheckBoxTableCell.forTableColumn(param -> {
			final var sdr = tvBatchToolsDat2DirDst.getItems().get(param);
			BooleanProperty observable = new SimpleBooleanProperty(sdr.isSelected());
			observable.addListener((obs, wasSelected, isNowSelected) -> {
				sdr.setSelected(isNowSelected);
				saveDst();
			});
			return observable;
		}));
		popupMenuDst.setOnShowing(e -> {
			mntmDat2DirDstPresets.setDisable(tvBatchToolsDat2DirDst.getSelectionModel().isEmpty());
			mnDat2DirDelDstDat.setDisable(tvBatchToolsDat2DirDst.getSelectionModel().isEmpty());
		});
	}

	private void saveDst()
	{
		session.getUser().getSettings().setProperty(SettingsEnum.dat2dir_sdr, SrcDstResult.toJSON(tvBatchToolsDat2DirDst.getItems()));
	}
	
	private void saveSrc()
	{
		session.getUser().getSettings().setProperty(SettingsEnum.dat2dir_srcdirs, String.join("|", tvBatchToolsDat2DirSrc.getItems().stream().map(File::getAbsolutePath).toList()));
	}
	
	@FXML void onCustomPresets(ActionEvent e)
	{
		try
		{
			if(!tvBatchToolsDat2DirDst.getSelectionModel().isEmpty())
				new CustomPresets((Stage)tvBatchToolsDat2DirDst.getScene().getWindow());
		}
		catch (IOException | URISyntaxException e1)
		{
			e1.printStackTrace();
		}
	}
	
	@FXML void onDelDst(ActionEvent e)
	{
		tvBatchToolsDat2DirDst.getItems().removeAll(tvBatchToolsDat2DirDst.getSelectionModel().getSelectedItems());
		saveDst();
	}
	
	@FXML void onAddDst(ActionEvent e)
	{
	}
	
	@FXML void onTZIPPresets(ActionEvent e)
	{
		for (final var sdr : tvBatchToolsDat2DirDst.getSelectionModel().getSelectedItems())
			ProfileSettings.TZIP(session, PathAbstractor.getAbsolutePath(session, sdr.getSrc()).toFile());
	}
	
	@FXML void onDIRPresets(ActionEvent e)
	{
		for (final var sdr : tvBatchToolsDat2DirDst.getSelectionModel().getSelectedItems())
			ProfileSettings.DIR(session, PathAbstractor.getAbsolutePath(session, sdr.getSrc()).toFile());
	}
	
	public class CustomPresets extends Stage
	{
		ScannerPanelSettingsController controller;
		
		public CustomPresets(Stage parent) throws IOException, URISyntaxException
		{
			super();
			initOwner(parent);
			initModality(Modality.WINDOW_MODAL);
			getIcons().add(parent.getIcons().get(0));
			setOnShowing(e -> {
			});
			setOnCloseRequest(e -> {
				hide();
			});
			final var loader = new FXMLLoader(getClass().getResource("ScannerPanelSettings.fxml").toURI().toURL(), Messages.getBundle());
			final var settings = loader.<ScrollPane>load();
			controller = loader.getController();
			final var root = new BorderPane(settings);
			final var cancel = new Button("Cancel");
			cancel.setOnAction(e -> close());
			final var ok = new Button("OK");
			ok.setOnAction(e -> save());
			final var bar = new HBox(cancel, ok);
			bar.setPadding(new Insets(5));
			bar.setSpacing(5);
			bar.setAlignment(Pos.CENTER_RIGHT);
			root.setBottom(bar);
			setScene(new Scene(root, 600, 400));
			sizeToScene();

			SrcDstResult entry = tvBatchToolsDat2DirDst.getSelectionModel().getSelectedItems().get(0);
			controller.initProfileSettings(session.getUser().getSettings().loadProfileSettings(PathAbstractor.getAbsolutePath(session, entry.getSrc()).toFile(), null));

			show();
		}
		
		private void save()
		{
			for (final var sdr : tvBatchToolsDat2DirDst.getSelectionModel().getSelectedItems())
				session.getUser().getSettings().saveProfileSettings(PathAbstractor.getAbsolutePath(session, sdr.getSrc()).toFile(), controller.getSettings());
			close();
		}

	}
}
