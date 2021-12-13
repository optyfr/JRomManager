module jrmfx
{
	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;
	
	requires jrmcore;
	requires commons.cli;
	requires lombok;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires javafx.web;
	
	opens jrm.fx.ui to javafx.graphics, javafx.fxml;
}