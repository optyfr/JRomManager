module jrmfx
{
	exports jrm.fx;
	exports jrm.fx.ui;
	exports jrm.fx.ui.controls;
	exports jrm.fx.ui.profile.manager;
	exports jrm.fx.ui.progress;
	exports jrm.fx.ui.status;

	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.web;
	
	requires com.google.gson;
	
	requires jcommander;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires org.apache.commons.text;
	
	requires static lombok;

	requires jrmcore;
	requires res.icons;
	requires java.xml;

	opens jrm.fx.ui to javafx.graphics, javafx.fxml;
	opens jrm.fx.ui.progress to javafx.graphics, javafx.fxml;
}
