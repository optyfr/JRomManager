module jrmcore
{
	exports jrm.batch;
	exports jrm.compressors;
	exports jrm.compressors.sevenzipjbinding;
	exports jrm.digest;
	exports jrm.io.chd;
	exports jrm.io.torrent;
	exports jrm.io.torrent.bencoding.types;
	exports jrm.io.torrent.bencoding;
	exports jrm.io.torrent.options;
	exports jrm.locale;
	exports jrm.misc;
	exports jrm.profile;
	exports jrm.profile.data;
	exports jrm.profile.manager;
	exports jrm.profile.scan.options;
	exports jrm.profile.report;
	exports jrm.profile.filter;
	exports jrm.profile.fix;
	exports jrm.profile.fix.actions;
	exports jrm.profile.scan;
	exports jrm.security;
	exports jrm.aui.basic;
	exports jrm.aui.progress;
	exports jrm.aui.batch;
	exports jrm.aui.profile.report;
	exports jrm.xml;

	requires java.desktop;
	requires jdk.zipfs;
	requires transitive java.logging;
	requires transitive java.xml;
	requires static lombok;
	requires one.util.streamex;
	requires org.apache.commons.codec;
	requires org.apache.commons.compress;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires org.apache.commons.text;
	requires minimal.json;
	requires trrntzip;
	requires SevenZipJBindingAllInOne;
	requires java.management;
	requires zip4j;
}
