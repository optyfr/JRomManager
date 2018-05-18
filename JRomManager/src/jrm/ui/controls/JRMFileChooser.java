package jrm.ui.controls;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import jrm.Messages;

@SuppressWarnings("serial")
public class JRMFileChooser<V> extends JFileChooser
{
	public static class OneRootFileSystemView extends FileSystemView
	{
		File root;
		File[] roots = new File[1];

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
				e1.printStackTrace();
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
		};

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

	public interface CallBack<V>
	{
		public V call(JRMFileChooser<V> chooser);
	}

	public JRMFileChooser()
	{
		this(null, null, null, null, null, null, false);
	}

	public JRMFileChooser(final int type, final int mode)
	{
		this(type, mode, null, null, null, null, false);
	}

	public JRMFileChooser(final int type, final int mode, final File currdir)
	{
		this(type, mode, currdir, null, null, null, false);
	}

	public JRMFileChooser(final Integer type, final Integer mode, final File currdir, final File selected, final List<FileFilter> filters, final String title, final boolean multi)
	{
		super();
		setup(type, mode, currdir, selected, filters, title, multi);
	}

	public JRMFileChooser<V> setup(final Integer type, final Integer mode, final File currdir, final File selected, final List<FileFilter> filters, final String title, final boolean multi)
	{
		if(type != null)
			setDialogType(type);
		if(mode != null)
			setFileSelectionMode(mode);
		if(selected != null)
			setSelectedFile(selected);
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
		if(title != null)
			setDialogTitle(title);
		if(multi)
			setMultiSelectionEnabled(multi);
		return this;
	}

	public JRMFileChooser(final FileSystemView fsv)
	{
		super(fsv);
	}

	public V show(final Component parent, final CallBack<V> callback)
	{
		if(showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
			return callback.call(this);
		return null;
	}

	public V showOpen(final Component parent, final CallBack<V> callback)
	{
		setDialogType(JFileChooser.OPEN_DIALOG);
		return show(parent, callback);
	}

	public V showSave(final Component parent, final CallBack<V> callback)
	{
		setDialogType(JFileChooser.SAVE_DIALOG);
		return show(parent, callback);
	}
}
