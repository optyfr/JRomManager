module jrmstandalone
{
	exports jrm;
	exports jrm.ui;
	exports jrm.ui.basic;
	exports jrm.ui.batch;
	exports jrm.ui.profile;
	exports jrm.ui.profile.data;
	exports jrm.ui.profile.filter;
	exports jrm.ui.profile.manager;
	exports jrm.ui.profile.report;
	exports jrm.ui.progress;
	
	requires java.desktop;
	
	requires commons.cli;
	requires minimal.json;

	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires org.apache.commons.codec;
	requires one.util.streamex;
	
	requires static lombok;
	
	requires jrmcore;
	requires jrmupdater;
	requires res.icons;
}
