package jrm.profile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.compress.utils.Sets;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.DOMException;

import jrm.misc.Log;
import jrm.misc.Settings;

public class Import
{
	public final File org_file;
	public File file;
	public File roms_file, sl_file;
	public boolean is_mame = false;

	public Import(final File file, final boolean sl)
	{
		org_file = file;
		final File workdir = Settings.getWorkPath().toFile(); //$NON-NLS-1$
		final File xmldir = new File(workdir, "xmlfiles"); //$NON-NLS-1$
		xmldir.mkdir();

		final String ext = FilenameUtils.getExtension(file.getName());
		if(!Sets.newHashSet("xml", "dat").contains(ext.toLowerCase()) && file.canExecute()) //$NON-NLS-1$ //$NON-NLS-2$
		{
			try
			{
				if((roms_file = importMame(file, false)) != null)
				{
					this.file = ProfileNFO.saveJrm(File.createTempFile("JRM", ".jrm"), roms_file, sl_file = sl ? importMame(file, true) : null); //$NON-NLS-1$ //$NON-NLS-2$
					is_mame = true;
				}
			}
			catch(DOMException | ParserConfigurationException | TransformerException | IOException e)
			{
				JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
				e.printStackTrace();
			}
		}
		else
			this.file = file;

	}

	public File importMame(final File file, final boolean sl)
	{
		// Log.info("Get dat file from Mame...");
		try
		{
			final File tmpfile = File.createTempFile("JRM", sl ? ".jrm2" : ".jrm1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			tmpfile.deleteOnExit();
			final Process process = new ProcessBuilder(file.getAbsolutePath(), sl ? "-listsoftware" : "-listxml").directory(file.getAbsoluteFile().getParentFile()).start(); //$NON-NLS-1$ //$NON-NLS-2$

			try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpfile), Charset.forName("UTF-8"))); BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));) //$NON-NLS-1$ //$NON-NLS-2$
			{
				String line;
				boolean xml = false;
				while(null != (line = in.readLine()))
				{
					if(line.startsWith("<?xml")) //$NON-NLS-1$
						xml = true;
					if(xml)
						out.write(line + "\n"); //$NON-NLS-1$
				}
			}
			process.waitFor();
			return tmpfile;
		}
		catch(final IOException e)
		{
			Log.err("Caught IO Exception", e); //$NON-NLS-1$
		}
		catch(final InterruptedException e)
		{
			Log.err("Caught Interrupted Exception", e); //$NON-NLS-1$
		}
		return null;
	}
}
