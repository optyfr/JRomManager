package jrm.fx.ui.profile.report;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

public class ReportLiteController implements Initializable
{
	@FXML ReportViewController viewController;

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		// TODO Auto-generated method stub
		
	}
	
	@FXML private void onClose(ActionEvent e)
	{
		((Stage)viewController.treeview.getScene().getWindow()).close();
	}

}
