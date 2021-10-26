open module jrmserver
{
	exports jrm.fullserver;
	exports jrm.fullserver.datasources;
	exports jrm.fullserver.db;
	exports jrm.fullserver.handlers;
	exports jrm.fullserver.security;
	exports jrm.server;
	exports jrm.server.handlers;
	exports jrm.server.shared;
	exports jrm.server.shared.actions;
	exports jrm.server.shared.datasources;
	exports jrm.server.shared.handlers;
	exports jrm.server.shared.lpr;
	
	requires java.xml;
	requires transitive java.sql;
	requires java.desktop;
	requires javax.servlet.api;
	requires com.sun.jna;
	
	requires com.google.gson;
	
	requires static lombok;
	
	requires commons.cli;
	requires commons.dbutils;
	requires minimal.json;
	
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires org.conscrypt;
	requires org.eclipse.jetty.alpn.server;
	requires org.eclipse.jetty.alpn.conscrypt.server;
	requires org.eclipse.jetty.server;
	requires org.eclipse.jetty.servlet;
	requires org.eclipse.jetty.security;
	requires org.eclipse.jetty.io;
	requires org.eclipse.jetty.http;
	requires org.eclipse.jetty.http2.common;
	requires org.eclipse.jetty.http2.hpack;
	requires org.eclipse.jetty.http2.server;
	requires org.eclipse.jetty.util;
	requires de.mkammerer.argon2.nolibs;
	requires de.mkammerer.argon2;
	requires jbcrypt;
	requires com.h2database;
	requires one.util.streamex;
	
	requires transitive jrmcore;
	requires res.icons;
}
