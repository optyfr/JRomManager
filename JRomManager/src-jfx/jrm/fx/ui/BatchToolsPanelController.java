package jrm.fx.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import jrm.batch.CompressorFormat;
import jrm.io.torrent.options.TrntChkMode;
import jrm.misc.SettingsEnum;
import jrm.security.Session;
import jrm.security.Sessions;

public class BatchToolsPanelController implements Initializable
{
	@FXML Tab panelBatchToolsDat2Dir;
	@FXML Tab panelBatchToolsDir2Torrent;
	@FXML Tab panelBatchToolsCompressor;
	@FXML Button btnBatchToolsDir2DatStart;
	@FXML Button btnBatchToolsTrntChkStart;
	@FXML Button btnBatchToolsCompressorStart;
	@FXML Button btnBatchToolsCompressorClear;
	@FXML ChoiceBox<TrntChkMode> cbbxBatchToolsTrntChk;
	@FXML ChoiceBox<CompressorFormat> cbbxBatchToolsCompressorFormat;
	@FXML CheckBox cbBatchToolsDat2DirDryRun;
	@FXML CheckBox cbBatchToolsTrntChkDetectArchivedFolder;
	@FXML CheckBox cbBatchToolsTrntChkRemoveUnknownFiles;
	@FXML CheckBox cbBatchToolsTrntChkRemoveWrongSizedFiles;
	@FXML CheckBox cbBatchToolsCompressorForce;
	
	final Session session = Sessions.getSingleSession();
	
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
		cbbxBatchToolsTrntChk.getSelectionModel().select(TrntChkMode.valueOf(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_mode, TrntChkMode.FILENAME.toString())));
		cbbxBatchToolsTrntChk.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			session.getUser().getSettings().setProperty(SettingsEnum.trntchk_mode, newValue.toString());
		//	cbRemoveWrongSizedFiles.setEnabled(newValue != TrntChkMode.FILENAME);
		});
		cbbxBatchToolsCompressorFormat.setItems(FXCollections.observableArrayList(CompressorFormat.values()));
		cbbxBatchToolsCompressorFormat.getSelectionModel().select(CompressorFormat.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.compressor_format, CompressorFormat.TZIP.toString()))); //$NON-NLS-1$
		cbbxBatchToolsCompressorFormat.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.compressor_format, newValue.toString()));

		cbBatchToolsDat2DirDryRun.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_dry_run, false));
		cbBatchToolsDat2DirDryRun.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.dat2dir_dry_run, newValue));
		
		cbBatchToolsTrntChkDetectArchivedFolder.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_detect_archived_folders, false));
		cbBatchToolsTrntChkDetectArchivedFolder.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_detect_archived_folders, newValue));
		
		cbBatchToolsTrntChkRemoveWrongSizedFiles.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_remove_wrong_sized_files, false));
		cbBatchToolsTrntChkRemoveWrongSizedFiles.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_remove_wrong_sized_files, newValue));
		
		cbBatchToolsTrntChkRemoveUnknownFiles.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_remove_unknown_files, false));
		cbBatchToolsTrntChkRemoveUnknownFiles.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_remove_unknown_files, newValue));
		
		cbBatchToolsCompressorForce.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.compressor_force, false));
		cbBatchToolsCompressorForce.selectedProperty().addListener((observable, oldValue, newValue) -> session.getUser().getSettings().setProperty(SettingsEnum.compressor_force, newValue));
	}

}
