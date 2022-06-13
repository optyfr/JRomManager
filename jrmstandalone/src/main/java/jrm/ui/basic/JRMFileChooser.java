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
package jrm.ui.basic;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import jrm.locale.Messages;
import jrm.misc.Log;

/**
 * The Class JRMFileChooser.
 *
 * @param <V> the value type
 */
@SuppressWarnings("serial")
public class JRMFileChooser<V> extends JFileChooser
{
	
	/**
	 * The Class OneRootFileSystemView.
	 */
	public static class OneRootFileSystemView extends FileSystemView
	{
		
		/** The root. */
		File root;
		
		/** The roots. */
		File[] roots = new File[1];

		/**
		 * Instantiates a new one root file system view.
		 *
		 * @param root the root
		 */
		public OneRootFileSystemView(final File root)
		{
			try
			{
				this.root = root.getCanonicalFile();
				roots[0] = this.root;
			}
			catch(final IOException e1)
			{
				JOptionPane.showMessageDialog(null, e1, "Exception", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
				Log.err(e1.getMessage(),e1);
			}
		}

		@Override
		public File createNewFolder(final File containingDir) throws IOException
		{
			final File folder = new File(containingDir, Messages.getString("JRMFileChooser.NewFolder")); //$NON-NLS-1$
			folder.mkdir();
			return folder;
		}

		@Override
		public File getDefaultDirectory()
		{
			return root;
		}

		@Override
		public File getHomeDirectory()
		{
			return root;
		}

		@Override
		public File[] getRoots()
		{
			return roots;
		}
	}

	/**
	 * The Interface CallBack.
	 *
	 * @param <V> the value type
	 */
	public interface CallBack<V>
	{
		
		/**
		 * Call.
		 *
		 * @param chooser the chooser
		 * @return the v
		 */
		public V call(JRMFileChooser<V> chooser);
	}

	/**
	 * Instantiates a new JRM file chooser.
	 */
	public JRMFileChooser()
	{
		this(null, null, null, null, null, null, false);
	}

	/**
	 * Instantiates a new JRM file chooser.
	 *
	 * @param type the type
	 * @param mode the mode
	 */
	public JRMFileChooser(final int type, final int mode)
	{
		this(type, mode, null, null, null, null, false);
	}

	/**
	 * Instantiates a new JRM file chooser.
	 *
	 * @param type the type
	 * @param mode the mode
	 * @param currdir the currdir
	 */
	public JRMFileChooser(final int type, final int mode, final File currdir)
	{
		this(type, mode, currdir, null, null, null, false);
	}

	/**
	 * Instantiates a new JRM file chooser.
	 *
	 * @param type the type
	 * @param mode the mode
	 * @param currdir the currdir
	 * @param selected the selected
	 * @param filters the filters
	 * @param title the title
	 * @param multi the multi
	 */
	@SuppressWarnings("exports")
	public JRMFileChooser(final Integer type, final Integer mode, final File currdir, final File selected, final List<FileFilter> filters, final String title, final boolean multi)
	{
		super();
		setup(type, mode, currdir, selected, filters, title, multi);
	}

	/**
	 * Setup.
	 *
	 * @param type the type
	 * @param mode the mode
	 * @param currdir the currdir
	 * @param selected the selected
	 * @param filters the filters
	 * @param title the title
	 * @param multi the multi
	 * @return the JRM file chooser
	 */
	@SuppressWarnings("exports")
	public JRMFileChooser<V> setup(final Integer type, final Integer mode, final File currdir, final File selected, final List<FileFilter> filters, final String title, final boolean multi)
	{
		Optional.ofNullable(type).ifPresent(this::setDialogType);
		Optional.ofNullable(mode).ifPresent(this::setFileSelectionMode);
		Optional.ofNullable(selected).ifPresent(this::setSelectedFile);
		if(currdir != null && currdir.exists())
		{
			if(currdir.isFile())
				setSelectedFile(currdir);
			else
				setCurrentDirectory(currdir);
		}
		if(filters != null)
		{
			if(filters.size() == 1)
			{
				setFileFilter(filters.get(0));
				setAcceptAllFileFilterUsed(false);
			}
			else
				for(final FileFilter filter : filters)
				{
					addChoosableFileFilter(filter);
					setAcceptAllFileFilterUsed(false);
				}
		}
		Optional.ofNullable(title).ifPresent(this::setDialogTitle);
		if(multi)
			setMultiSelectionEnabled(multi);
		return this;
	}

	/**
	 * Instantiates a new JRM file chooser.
	 *
	 * @param fsv the fsv
	 */
	@SuppressWarnings("exports")
	public JRMFileChooser(final FileSystemView fsv)
	{
		super(fsv);
	}

	/**
	 * Show.
	 *
	 * @param parent the parent
	 * @param callback the callback
	 * @return the v
	 */
	@SuppressWarnings("exports")
	public V show(final Component parent, final CallBack<V> callback)
	{
		if(showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
			return callback.call(this);
		return null;
	}

	/**
	 * Show open.
	 *
	 * @param parent the parent
	 * @param callback the callback
	 * @return the v
	 */
	@SuppressWarnings("exports")
	public V showOpen(final Component parent, final CallBack<V> callback)
	{
		setDialogType(JFileChooser.OPEN_DIALOG);
		return show(parent, callback);
	}

	/**
	 * Show save.
	 *
	 * @param parent the parent
	 * @param callback the callback
	 * @return the v
	 */
	@SuppressWarnings("exports")
	public V showSave(final Component parent, final CallBack<V> callback)
	{
		setDialogType(JFileChooser.SAVE_DIALOG);
		return show(parent, callback);
	}
}
