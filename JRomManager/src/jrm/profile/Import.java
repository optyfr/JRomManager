package jrm.profile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.compress.utils.Sets;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jrm.misc.Log;

public class Import
{
	public final File org_file;
	public File file;
	public File roms_file, sl_file;
	public boolean is_mame = false;

	public Import(File file, boolean sl)
	{
		org_file = file;
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
		File xmldir = new File(workdir, "xmlfiles"); //$NON-NLS-1$
		xmldir.mkdir();

		String ext = FilenameUtils.getExtension(file.getName());
		if(!Sets.newHashSet("xml", "dat").contains(ext.toLowerCase()) && file.canExecute()) //$NON-NLS-1$
		{
			try
			{
				if((roms_file = importMame(file, false)) != null)
				{
					File tmpfile = File.createTempFile("JRM", ".jrm"); //$NON-NLS-1$ //$NON-NLS-2$
					DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
					Document doc = docBuilder.newDocument();
					Element rootElement = doc.createElement("JRomManager");
					doc.appendChild(rootElement);
					Element profile = doc.createElement("Profile");
					profile.setAttribute("roms", roms_file.getName());
					if(sl)
					{
						if((sl_file = importMame(file, true)) != null)
							profile.setAttribute("sl", sl_file.getName());
					}
					rootElement.appendChild(profile);
					TransformerFactory transformerFactory = TransformerFactory.newInstance();
					Transformer transformer = transformerFactory.newTransformer();
					DOMSource source = new DOMSource(doc);
					StreamResult result = new StreamResult(tmpfile);
					transformer.transform(source, result);
					this.file = tmpfile;
					this.is_mame = true;
				}

			}
			catch(DOMException | ParserConfigurationException | TransformerException | IOException e)
			{
				JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		else
			this.file = file;

	}

	public File importMame(File file, boolean sl)
	{
		// Log.info("Get dat file from Mame...");
		try
		{
			File tmpfile = File.createTempFile("JRM", sl ? ".jrm2" : ".jrm1"); //$NON-NLS-1$ //$NON-NLS-2$
			tmpfile.deleteOnExit();
			Process process = new ProcessBuilder(file.getAbsolutePath(), sl ? "-listsoftware" : "-listxml").directory(file.getAbsoluteFile().getParentFile()).start(); //$NON-NLS-1$

			try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpfile), Charset.forName("UTF-8"))); BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));)
			{
				String line;
				boolean xml = false;
				while(null != (line = in.readLine()))
				{
					if(line.startsWith("<?xml"))
						xml = true;
					if(xml)
						out.write(line + "\n");
				}
			}
			process.waitFor();
			return tmpfile;
		}
		catch(IOException e)
		{
			Log.err("Caught IO Exception", e); //$NON-NLS-1$
		}
		catch(InterruptedException e)
		{
			Log.err("Caught Interrupted Exception", e); //$NON-NLS-1$
		}
		return null;
	}
}
