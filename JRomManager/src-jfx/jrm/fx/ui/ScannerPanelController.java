package jrm.fx.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class ScannerPanelController implements Initializable
{
	private static final String DISK_ICON = "/jrm/resicons/icons/disk.png";
	
	@FXML private Button romsDestBtn;
	@FXML private TextField romsDest;
	@FXML private Button disksDestBtn;
	@FXML private TextField disksDest;
	@FXML private Button swDestBtn;
	@FXML private TextField swDest;
	@FXML private Button swDisksDestBtn;
	@FXML private TextField swDisksDest;
	@FXML private Button samplesDestBtn;
	@FXML private TextField samplesDest;
	@FXML private Button backupDestBtn;
	@FXML private TextField backupDest;

	@FXML private Button infosBtn;
	@FXML private Button scanBtn;
	@FXML private Button reportBtn;
	@FXML private Button fixBtn;
	@FXML private Button importBtn;
	@FXML private Button exportBtn;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		romsDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		disksDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		swDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		swDisksDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		samplesDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		backupDestBtn.setGraphic(new ImageView(MainFrame.getIcon(DISK_ICON)));
		infosBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/information.png")));
		scanBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/magnifier.png")));
		reportBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/report.png")));
		fixBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/tick.png")));
		importBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/table_refresh.png")));
		exportBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/table_save.png")));
	}
	
	@FXML void switchDisksDest(ActionEvent e)
	{
		if(e.getSource() instanceof CheckBox cb)
		{
			disksDest.setDisable(!cb.isSelected());
			disksDestBtn.setDisable(!cb.isSelected());
		}
	}

	@FXML void switchSWDest(ActionEvent e)
	{
		if(e.getSource() instanceof CheckBox cb)
		{
			swDest.setDisable(!cb.isSelected());
			swDestBtn.setDisable(!cb.isSelected());
		}
	}

	@FXML void switchSWDisksDest(ActionEvent e)
	{
		if(e.getSource() instanceof CheckBox cb)
		{
			swDisksDest.setDisable(!cb.isSelected());
			swDisksDestBtn.setDisable(!cb.isSelected());
		}
	}

	@FXML void switchSamplesDest(ActionEvent e)
	{
		if(e.getSource() instanceof CheckBox cb)
		{
			samplesDest.setDisable(!cb.isSelected());
			samplesDestBtn.setDisable(!cb.isSelected());
		}
	}

	@FXML void switchBackupDest(ActionEvent e)
	{
		if(e.getSource() instanceof CheckBox cb)
		{
			backupDest.setDisable(!cb.isSelected());
			backupDestBtn.setDisable(!cb.isSelected());
		}
	}

}
