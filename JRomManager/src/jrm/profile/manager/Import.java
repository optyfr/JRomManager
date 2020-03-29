/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.compress.utils.Sets;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.DOMException;

import jrm.misc.Log;
import jrm.misc.UnitRenderer;
import jrm.security.Session;
import jrm.ui.progress.ProgressHandler;

/**
 * Import from Mame (and variants)
 * @author optyfr
 *
 */
public class Import implements UnitRenderer
{
	public final File org_file;
	public File file;
	public File roms_file, sl_file;
	public boolean is_mame = false;

	/**
	 * Will import from a file, it will be autodetected if it's mame or just a dat file, optionally also import software lists in case of a mame import
	 * @param file the file to analyze, and eventually extract from
	 * @param sl do we need to load software lists
	 */
	public Import(final Session session, final File file, final boolean sl, ProgressHandler progress)
	{
		org_file = file;
		final File workdir = session.getUser().getSettings().getWorkPath().toFile(); //$NON-NLS-1$
		final File xmldir = new File(workdir, "xmlfiles"); //$NON-NLS-1$
		xmldir.mkdir();

		final String ext = FilenameUtils.getExtension(file.getName());
		if(!Sets.newHashSet("xml", "dat").contains(ext.toLowerCase()) && file.canExecute()) //$NON-NLS-1$ //$NON-NLS-2$
		{
			try
			{
				if((roms_file = importMame(file, false, progress)) != null)
				{
					this.file = ProfileNFO.saveJrm(File.createTempFile("JRM", ".jrm"), roms_file, sl_file = sl ? importMame(file, true, progress) : null); //$NON-NLS-1$ //$NON-NLS-2$
					is_mame = true;
				}
			}
			catch(DOMException | ParserConfigurationException | TransformerException | IOException e)
			{
//				JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
				Log.err(e.getMessage(),e);
			}
		}
		else
			this.file = file;

	}

	/**
	 * Will import from mame, must be called only if we are sure that it's an import from mame
	 * @param file the mame exe file
	 * @param sl if true, will return software list imported file (.jrm2), otherwise will return roms list file (.jrm1}
	 * @return an existing temporary {@link File}, or null if failed
	 */
	public File importMame(final File file, final boolean sl, ProgressHandler progress)
	{
		// Log.info("Get dat file from Mame...");
		try
		{
			final File tmpfile = File.createTempFile("JRM", sl ? ".jrm2" : ".jrm1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			tmpfile.deleteOnExit();
			final Process process = new ProcessBuilder(file.getAbsolutePath(), sl ? "-listsoftware" : "-listxml").directory(file.getAbsoluteFile().getParentFile()).start(); //$NON-NLS-1$ //$NON-NLS-2$

			long linecnt = 0, size = 0;
			try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpfile), Charset.forName("UTF-8"))); BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));) //$NON-NLS-1$ //$NON-NLS-2$
			{
				String line;
				boolean xml = false;
				while(null != (line = in.readLine()))
				{
					// we need to skip occasional garbage before the xml start tag
					if(line.startsWith("<?xml")) //$NON-NLS-1$
						xml = true;
					if(xml)
					{
						out.write(line + "\n"); //$NON-NLS-1$
						size += line.getBytes("UTF-8").length;
						progress.setProgress(null, null, null, (sl ? "Reading Softwares list" : "Reading roms list") + " / " + (++linecnt) + " lines / " + humanReadableByteCount(size, false));
					}
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
