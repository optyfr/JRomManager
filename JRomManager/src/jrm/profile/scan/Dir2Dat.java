package jrm.profile.scan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.EnumSet;

import javax.swing.JOptionPane;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;

import jrm.profile.data.Container;
import jrm.profile.data.Entry;
import jrm.profile.data.Entry.Type;
import jrm.profile.manager.Export;
import jrm.profile.manager.Export.ExportType;
import jrm.profile.scan.DirScan.Options;
import jrm.ui.progress.ProgressHandler;
import jrm.xml.EnhancedXMLStreamWriter;
import jrm.xml.SimpleAttribute;

public class Dir2Dat
{
	public Dir2Dat(File srcdir, File dstdat, final ProgressHandler progress, EnumSet<Options> options, ExportType type)
	{
		DirScan srcdir_scan  = new DirScan(srcdir, progress, options);
		write(dstdat, srcdir_scan, type);
	}

	private void write(final File dstdat, final DirScan scan, final ExportType type)
	{
		EnhancedXMLStreamWriter writer = null;
		try(FileOutputStream fos = new FileOutputStream(dstdat))
		{
			writer = new EnhancedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(fos, "UTF-8")); //$NON-NLS-1$
			writer.writeStartDocument("UTF-8","1.0"); //$NON-NLS-1$ //$NON-NLS-2$
			switch(type)
			{
				case MAME:
				{
					writer.writeDTD("<!DOCTYPE mame [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/mame.dtd"), Charset.forName("UTF-8")) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					writer.writeStartElement("mame"); //$NON-NLS-1$
					for(Container c : scan.getContainersIterable())
					{
						writer.writeStartElement("machine", //$NON-NLS-1$
							new SimpleAttribute("name", normalize(scan.getDir().toPath().relativize(c.file.toPath()).toString())), //$NON-NLS-1$
							new SimpleAttribute("isbios", null), //$NON-NLS-1$ //$NON-NLS-2$
							new SimpleAttribute("isdevice", null), //$NON-NLS-1$ //$NON-NLS-2$
							new SimpleAttribute("ismechanical", null), //$NON-NLS-1$ //$NON-NLS-2$
							new SimpleAttribute("cloneof", null), //$NON-NLS-1$
							new SimpleAttribute("romof", null), //$NON-NLS-1$
							new SimpleAttribute("sampleof", null) //$NON-NLS-1$
						);
						writer.writeElement("description", ""); //$NON-NLS-1$
						writer.writeElement("year", ""); //$NON-NLS-1$
						writer.writeElement("manufacturer", ""); //$NON-NLS-1$
						for(Entry e : c.getEntries())
						{
							if(e.type==Type.CHD)
							{
								writer.writeElement("disk", //$NON-NLS-1$
									new SimpleAttribute("name", normalize(e.getName())), //$NON-NLS-1$
									new SimpleAttribute("md5", e.md5), //$NON-NLS-1$
									new SimpleAttribute("sha1", e.sha1) //$NON-NLS-1$
								);
							}
							else
							{
								writer.writeElement("rom", //$NON-NLS-1$
									new SimpleAttribute("name", normalize(e.getName())), //$NON-NLS-1$
									new SimpleAttribute("size", e.size), //$NON-NLS-1$
									new SimpleAttribute("crc", e.crc), //$NON-NLS-1$
									new SimpleAttribute("md5", e.md5), //$NON-NLS-1$
									new SimpleAttribute("sha1", e.sha1) //$NON-NLS-1$
								);
							}
						}
						writer.writeEndElement();
					}
					writer.writeEndElement();
					break;
				}
				case DATAFILE:
				{
					writer.writeDTD("<!DOCTYPE datafile [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/datafile.dtd"), Charset.forName("UTF-8")) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					writer.writeStartElement("datafile"); //$NON-NLS-1$
					for(Container c : scan.getContainersIterable())
					{
						writer.writeStartElement("game", //$NON-NLS-1$
							new SimpleAttribute("name", normalize(scan.getDir().toPath().relativize(c.file.toPath()).toString())), //$NON-NLS-1$
							new SimpleAttribute("isbios", null), //$NON-NLS-1$ //$NON-NLS-2$
							new SimpleAttribute("cloneof", null), //$NON-NLS-1$
							new SimpleAttribute("romof", null), //$NON-NLS-1$
							new SimpleAttribute("sampleof", null) //$NON-NLS-1$
						);
						writer.writeElement("description", ""); //$NON-NLS-1$
						writer.writeElement("year", ""); //$NON-NLS-1$
						writer.writeElement("manufacturer", ""); //$NON-NLS-1$
						for(Entry e : c.getEntries())
						{
							if(e.type==Type.CHD)
							{
								writer.writeElement("disk", //$NON-NLS-1$
									new SimpleAttribute("name", normalize(e.getName())), //$NON-NLS-1$
									new SimpleAttribute("md5", e.md5), //$NON-NLS-1$
									new SimpleAttribute("sha1", e.sha1) //$NON-NLS-1$
								);
							}
							else
							{
								writer.writeElement("rom", //$NON-NLS-1$
									new SimpleAttribute("name", normalize(e.getName())), //$NON-NLS-1$
									new SimpleAttribute("size", e.size), //$NON-NLS-1$
									new SimpleAttribute("crc", e.crc), //$NON-NLS-1$
									new SimpleAttribute("md5", e.md5), //$NON-NLS-1$
									new SimpleAttribute("sha1", e.sha1), //$NON-NLS-1$
									new SimpleAttribute("offset", 0), //$NON-NLS-1$
									new SimpleAttribute("date", e.modified) //$NON-NLS-1$
								);
							}
						}
						writer.writeEndElement();
					}
					writer.writeEndElement();
					break;
				}
				case SOFTWARELIST:
				{
					writer.writeDTD("<!DOCTYPE softwarelists [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/softwarelists.dtd"), Charset.forName("UTF-8")) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					writer.writeStartElement("softwarelists"); //$NON-NLS-1$
					writer.writeEndElement();
					break;
				}
			}
			writer.writeEndDocument();
			writer.close();
		}
		catch(FactoryConfigurationError | XMLStreamException | IOException e)
		{
			JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			e.printStackTrace();
		}
	}
	
	/**
	 * Normalize char separator according platform default separator
	 * @param entry the entry to normalize
	 * @return the normalized entry
	 */
	private String normalize(final String entry)
	{
		if(File.separatorChar == '/')
			return entry.replace('\\', '/');
		return entry.replace('/', '\\');
	}
}
