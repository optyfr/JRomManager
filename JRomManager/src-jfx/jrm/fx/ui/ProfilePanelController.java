package jrm.fx.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class ProfilePanelController implements Initializable
{
	@FXML Button btnLoad;
	@FXML Button btnImportDat;
	@FXML Button btnImportSL;


	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		btnLoad.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/add.png")));
		btnImportDat.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/script_go.png")));
		btnImportSL.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/application_go.png")));
	}

}
