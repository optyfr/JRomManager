module jrmfx
{
	requires javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;
	
	requires jrmcore;
	requires commons.cli;
	requires lombok;
	requires org.apache.commons.io;
	
	opens jrm.fx.ui to javafx.graphics, javafx.fxml;
}
