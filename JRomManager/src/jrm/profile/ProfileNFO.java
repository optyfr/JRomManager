package jrm.profile;

import java.io.*;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public final class ProfileNFO implements Serializable
{
	private static final long serialVersionUID = 1L;

	public File file = null;
	public String name = null;
	public ProfileNFOStats stats = new ProfileNFOStats();
	public ProfileNFOMame mame = new ProfileNFOMame();

	private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("file", File.class), new ObjectStreamField("name", String.class), new ObjectStreamField("stats", ProfileNFOStats.class), new ObjectStreamField("mame", ProfileNFOMame.class), }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("file", file); //$NON-NLS-1$
		fields.put("name", name); //$NON-NLS-1$
		fields.put("stats", stats); //$NON-NLS-1$
		fields.put("mame", mame); //$NON-NLS-1$
		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		file = (File) fields.get("file", null); //$NON-NLS-1$
		name = (String) fields.get("name", null); //$NON-NLS-1$
		stats = (ProfileNFOStats) fields.get("stats", new ProfileNFOStats()); //$NON-NLS-1$
		mame = (ProfileNFOMame) fields.get("mame", new ProfileNFOMame()); //$NON-NLS-1$
	}

	private ProfileNFO(final File file)
	{
		this.file = file;
		name = file.getName();
		stats.created = new Date();
		if(isJRM())
			loadJrm(file);
	}

	private static File getFileNfo(final File file)
	{
		return new File(file.getParentFile(), file.getName() + ".nfo"); //$NON-NLS-1$
	}

	public void relocate(final File file)
	{
		ProfileNFO.getFileNfo(this.file).delete();
		this.file = file;
		name = file.getName();
		save();
	}

	public static ProfileNFO load(final File file)
	{
		final File filenfo = ProfileNFO.getFileNfo(file);
		if(filenfo.lastModified() >= file.lastModified()) // $NON-NLS-1$
		{
			try(ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filenfo))))
			{
				return (ProfileNFO) ois.readObject();
			}
			catch(final Throwable e)
			{
			}
		}
		return new ProfileNFO(file);
	}

	public void save()
	{
		try
		{
			long modified = file.lastModified();
			saveJrm(file, mame.fileroms, mame.filesl);
			if(modified!=0)
				file.setLastModified(modified);
		}
		catch (ParserConfigurationException | TransformerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try(ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(ProfileNFO.getFileNfo(file)))))
		{
			oos.writeObject(this);
		}
		catch(final Throwable e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isJRM()
	{
		return FilenameUtils.getExtension(file.getName()).equals("jrm"); //$NON-NLS-1$
	}

	public void loadJrm(final File jrmfile)
	{
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		try
		{
			final SAXParser parser = factory.newSAXParser();
			parser.parse(jrmfile, new DefaultHandler()
			{
				private boolean in_jrm = false;

				@Override
				public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
				{
					if(qName.equalsIgnoreCase("JRomManager")) //$NON-NLS-1$
					{
						in_jrm = true;
					}
					else if(qName.equalsIgnoreCase("Profile") && in_jrm) //$NON-NLS-1$
					{
						for(int i = 0; i < attributes.getLength(); i++)
						{
							switch(attributes.getQName(i).toLowerCase())
							{
								case "roms": //$NON-NLS-1$
									mame.fileroms = new File(jrmfile.getParentFile(), attributes.getValue(i));
									break;
								case "sl": //$NON-NLS-1$
									mame.filesl = new File(jrmfile.getParentFile(), attributes.getValue(i));
									break;
							}
						}
					}
				}

				@Override
				public void endElement(final String uri, final String localName, final String qName) throws SAXException
				{
					if(qName.equalsIgnoreCase("JRomManager")) //$NON-NLS-1$
					{
						in_jrm = false;
					}
				}
			});
		}
		catch(ParserConfigurationException | SAXException | IOException e)
		{
			JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

	public static File saveJrm(final File JrmFile, final File roms_file, final File sl_file) throws ParserConfigurationException, TransformerException
	{
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		final Document doc = docBuilder.newDocument();
		final Element rootElement = doc.createElement("JRomManager"); //$NON-NLS-1$
		doc.appendChild(rootElement);
		final Element profile = doc.createElement("Profile"); //$NON-NLS-1$
		profile.setAttribute("roms", roms_file.getName()); //$NON-NLS-1$
		if(sl_file != null)
			profile.setAttribute("sl", sl_file.getName()); //$NON-NLS-1$
		rootElement.appendChild(profile);
		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		final Transformer transformer = transformerFactory.newTransformer();
		final DOMSource source = new DOMSource(doc);
		final StreamResult result = new StreamResult(JrmFile);
		transformer.transform(source, result);
		return JrmFile;
	}
	
	public boolean delete()
	{
		if(file.delete())
		{
			mame.delete();
			new File(file.getAbsolutePath() + ".cache").delete(); //$NON-NLS-1$
			new File(file.getAbsolutePath() + ".nfo").delete(); //$NON-NLS-1$
			new File(file.getAbsolutePath() + ".properties").delete(); //$NON-NLS-1$
			return true;
		}
		return false;
	}
}
