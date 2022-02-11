/*
 * Copyright (C) 2018 optyfr
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.profile.manager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.security.Session;

import lombok.Getter;
import lombok.Setter;

/**
 * The Profile NFO file managing class with tolerant manual (de)serialization
 * 
 * @author optyfr
 */
public final class ProfileNFO implements Serializable, HTMLRenderer
{
	private static final String MAME_STR = "mame";
	private static final String STATS_STR = "stats";
	private static final String NAME_STR = "name";
	private static final String FILE_STR = "file";
	private static final String U = "?";
	private static final String U_OF_U = "?/?";
	private static final String N_OF_T = "%s/%d";
	private static final String JROMMANAGER_STR = "JRomManager";
	private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	private static final String UNKNOWN_DATE = "????-??-?? ??:??:??";

	private static final long serialVersionUID = 3L;

	/**
	 * The Profile {@link File} (can be a jrm, a dat, or an xml file)
	 */
	private @Getter File file = null;
	/**
	 * The name to show in GUI (equals to file name by default)
	 */
	private @Getter String name = null;
	/**
	 * The {@link ProfileNFOStats} stats sub class
	 */
	private @Getter ProfileNFOStats stats = new ProfileNFOStats();
	/**
	 * The {@link ProfileNFOMame} mame sub class
	 */
	private @Getter ProfileNFOMame mame = new ProfileNFOMame();
	
	/**
	 * Temporary new name
	 */
	private transient @Getter @Setter String newName = null;

	/**
	 * fields declaration for manual serialization
	 * 
	 * @serialField file
	 *                  File the file linked to this profile info
	 * @serialField name
	 *                  String the name of the profile
	 * @serialField stats
	 *                  ProfileNFOStats the stats related to the profile
	 * @serialField mame
	 *                  ProfileNFOMame the mame infos relates to the profile
	 */
	private static final ObjectStreamField[] serialPersistentFields = {	//NOSONAR 
		new ObjectStreamField(FILE_STR, File.class),
		new ObjectStreamField(NAME_STR, String.class),
		new ObjectStreamField(STATS_STR, ProfileNFOStats.class),
		new ObjectStreamField(MAME_STR, ProfileNFOMame.class)
	};

	/**
	 * Manually write serialization
	 * 
	 * @serialData Use {@link ObjectOutputStream.PutField} object to add to
	 *             persistent fields buffer, then
	 *             {@link ObjectOutputStream#writeFields()}
	 * @param stream
	 *            the destination {@link ObjectOutputStream}
	 * @throws IOException
	 */
	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final var fields = stream.putFields();
		fields.put(FILE_STR, file); //$NON-NLS-1$
		fields.put(NAME_STR, name); //$NON-NLS-1$
		fields.put(STATS_STR, stats); //$NON-NLS-1$
		fields.put(MAME_STR, mame); //$NON-NLS-1$
		stream.writeFields();
	}

	/**
	 * Manually read serialization
	 * 
	 * @param stream
	 *            the destination {@link ObjectInputStream}
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		file = (File) fields.get(FILE_STR, null); //$NON-NLS-1$
		name = (String) fields.get(NAME_STR, null); //$NON-NLS-1$
		stats = (ProfileNFOStats) fields.get(STATS_STR, new ProfileNFOStats()); //$NON-NLS-1$
		mame = (ProfileNFOMame) fields.get(MAME_STR, new ProfileNFOMame()); //$NON-NLS-1$
	}

	/**
	 * internal constructor
	 * 
	 * @param file
	 *            the file to attach
	 */
	private ProfileNFO(final File file)
	{
		this.file = file;
		name = file.getName();
		stats.setCreated(new Date());
		if (isJRM())
			loadJrm(file);
	}

	/**
	 * return the nfo file derived from the attached file
	 * 
	 * @param file
	 *            the attached file candidate
	 * @return the nfo {@link File}
	 */
	private static File getFileNfo(final Session session, final File file)
	{
		return session.getUser().getSettings().getWorkFile(file.getParentFile(), file.getName(), ".nfo");
	}

	/**
	 * Delete NFO old location, change attached file, then save to new location
	 * 
	 * @param file
	 *            new file to attach
	 */
	public void relocate(final Session session, final File file)
	{
		try
		{
			Files.deleteIfExists(ProfileNFO.getFileNfo(session, this.file).toPath());
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
		this.file = file;
		name = file.getName();
		save(session);
	}

	/**
	 * Load or create a ProfileNFO from an attached file
	 * 
	 * @param file
	 *            the attached file from which to derive NFO file
	 * @return the {@link ProfileNFO}
	 */
	public static ProfileNFO load(final Session session, final File file)
	{
		final var filenfo = ProfileNFO.getFileNfo(session, file);
		if (filenfo.lastModified() >= file.lastModified()) // $NON-NLS-1$
		{
			try (final var ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filenfo))))
			{
				ProfileNFO nfo = (ProfileNFO) ois.readObject();
				if (nfo.file != null)
					return nfo;
			}
			catch (final Exception e)
			{
				Log.err(e.getMessage(),e);
			}
		}
		return new ProfileNFO(file);
	}

	/**
	 * Save this ProfileNFO, and save JRM if the attached file is a JRM type file
	 */
	public void save(final Session session)
	{
		if (isJRM())
			try
			{
				final var modified = Files.getLastModifiedTime(file.toPath());
				saveJrm(file, mame.getFileroms(), mame.getFilesl());
				Files.setLastModifiedTime(file.toPath(), modified);
			}
			catch (ParserConfigurationException | TransformerException | IOException e)
			{
				Log.err(e.getMessage(), e);
			}
		try (final var oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(ProfileNFO.getFileNfo(session, file)))))
		{
			oos.writeObject(this);
		}
		catch (final Exception e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	/**
	 * Does the attached file is a JRM file
	 * 
	 * @return true if it's a JRM file
	 */
	public boolean isJRM()
	{
		return FilenameUtils.getExtension(file.getName()).equals("jrm"); //$NON-NLS-1$
	}

	/**
	 * Load JRM file and fill up {@link #mame}
	 * 
	 * @param jrmfile
	 *            the JRM file to load
	 */
	public void loadJrm(final File jrmfile)
	{
		final var factory = SAXParserFactory.newInstance();
		try
		{
			final var parser = factory.newSAXParser();
			parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
			parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
			parser.parse(jrmfile, new DefaultHandler()
			{
				private boolean inJrm = false;

				@Override
				public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
				{
					if (qName.equalsIgnoreCase(JROMMANAGER_STR)) //$NON-NLS-1$
					{
						inJrm = true;
					}
					else if (qName.equalsIgnoreCase("Profile") && inJrm) //$NON-NLS-1$
					{
						for (var i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i).toLowerCase())
							{
								case "roms": //$NON-NLS-1$
									mame.setFileroms(new File(jrmfile.getParentFile(), attributes.getValue(i)));
									break;
								case "sl": //$NON-NLS-1$
									mame.setFilesl(new File(jrmfile.getParentFile(), attributes.getValue(i)));
									break;
								default:
									break;
							}
						}
					}
				}

				@Override
				public void endElement(final String uri, final String localName, final String qName) throws SAXException
				{
					if (qName.equalsIgnoreCase(JROMMANAGER_STR)) //$NON-NLS-1$
					{
						inJrm = false;
					}
				}
			});
		}
		catch (ParserConfigurationException | SAXException | IOException e)
		{
			// JOptionPane.showMessageDialog(null, e, "Exception",
			// JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			Log.err(e.getMessage(), e);
		}
	}

	/**
	 * save mame infos in JRM file
	 * 
	 * @param jrmFile
	 *            the JRM file to save
	 * @param romsFile
	 *            the mame roms file
	 * @param slFile
	 *            the software list file
	 * @return return the {@code JrmFile}
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static File saveJrm(final File jrmFile, final File romsFile, final File slFile) throws ParserConfigurationException, TransformerException
	{
		final var docFactory = DocumentBuilderFactory.newInstance();
		final var docBuilder = docFactory.newDocumentBuilder();
		final var doc = docBuilder.newDocument();
		final var rootElement = doc.createElement(JROMMANAGER_STR); //$NON-NLS-1$
		doc.appendChild(rootElement);
		final var profile = doc.createElement("Profile"); //$NON-NLS-1$
		profile.setAttribute("roms", romsFile.getName()); //$NON-NLS-1$
		if (slFile != null)
			profile.setAttribute("sl", slFile.getName()); //$NON-NLS-1$
		rootElement.appendChild(profile);
		final var transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
		transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ""); // Compliant
		final var transformer = transformerFactory.newTransformer();
		final var source = new DOMSource(doc);
		final var result = new StreamResult(jrmFile);
		transformer.transform(source, result);
		return jrmFile;
	}

	/**
	 * Delete all related files to
	 * 
	 * @return true on success
	 */
	public boolean delete()
	{
		try
		{
			if (Files.deleteIfExists(file.toPath()))
			{
				mame.delete();
				Files.deleteIfExists(Paths.get(file.getAbsolutePath() + ".cache"));
				Files.deleteIfExists(Paths.get(file.getAbsolutePath() + ".nfo"));
				Files.deleteIfExists(Paths.get(file.getAbsolutePath() + ".properties"));
				return true;
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
		return false;
	}

	public String getHTMLVersion()
	{
		return toHTML(Optional.ofNullable(stats.getVersion()).map(this::toNoBR).orElse(toGray("???"))); //$NON-NLS-1$
	}

	public String getHTMLHaveSets()
	{
		final String have;
		if(stats.getHaveSets() == null)
		{
			if((stats.getTotalSets() == null))
				have = toGray(U_OF_U);
			else
				have = String.format(N_OF_T, toGray(U), stats.getTotalSets());
		}
		else
		{
			final String n;
			if(stats.getHaveSets() == 0 && stats.getTotalSets() > 0)
				n = toRed("0");
			else if(stats.getHaveSets().equals(stats.getTotalSets()))
				n = toGreen(toStr(stats.getHaveSets()));
			else
				n = toOrange(toStr(stats.getHaveSets())); 
			have = String.format(N_OF_T, n, stats.getTotalSets());
		}
		return toHTML(have);
	}

	public String getHTMLHaveRoms()
	{
		final String have;
		if(stats.getHaveRoms() == null)
		{
			if(stats.getTotalRoms() == null)
				have = toGray(U_OF_U);
			else
				have = String.format(N_OF_T, toGray(U), stats.getTotalRoms());
		}
		else
		{
			final String n;
			if(stats.getHaveRoms() == 0 && stats.getTotalRoms() > 0)
				n = toRed("0");
			else if(stats.getHaveRoms().equals(stats.getTotalRoms()))
				n = toGreen(toStr(stats.getHaveRoms()));
			else
				n = toOrange(toStr(stats.getHaveRoms())); 
			have = String.format(N_OF_T, n, stats.getTotalRoms());
		}
		return toHTML(have);
	}

	public String getHTMLHaveDisks()
	{
		final String have;
		if(stats.getHaveDisks() == null)
		{
			if(stats.getTotalDisks() == null)
				have = toGray(U_OF_U);
			else
				have = String.format(N_OF_T, toGray(U), stats.getTotalDisks());
		}
		else
		{
			final String n;
			if(stats.getHaveDisks() == 0 && stats.getTotalDisks() > 0)
				n = toRed("0");
			else if(stats.getHaveDisks().equals(stats.getTotalDisks()))
				n = toGreen(toStr(stats.getHaveDisks()));
			else
				n = toOrange(toStr(stats.getHaveDisks()));
			have = String.format(N_OF_T, n, stats.getTotalDisks());
		}
		return toHTML(have);
	}

	public String getHTMLCreated()
	{
		return toHTML(stats.getCreated() == null ? toGray(UNKNOWN_DATE) : new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).format(stats.getCreated())); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getHTMLScanned()
	{
		return toHTML(stats.getScanned() == null ? toGray(UNKNOWN_DATE) : new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).format(stats.getScanned())); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getHTMLFixed()
	{
		return toHTML(stats.getFixed() == null ? toGray(UNKNOWN_DATE) : new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).format(stats.getFixed())); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static List<ProfileNFO> list(Session session, File dir)
	{
		List<ProfileNFO> rows = new ArrayList<>();
		if (dir != null && dir.exists())
		{
			final var filedir = dir;
			final File[] files = filedir.listFiles((dir1, name) -> {
				final var f = new File(dir1, name);
				return (f.isFile() && !Arrays.asList("cache", "properties", "nfo", "jrm1", "jrm2").contains(FilenameUtils.getExtension(name))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			});
			if (files != null)
			{
				Arrays.asList(files).stream().map(f -> ProfileNFO.load(session, f)).forEach(rows::add);
			}
		}
		return rows;
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
}
