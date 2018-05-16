package jrm.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

@SuppressWarnings("serial")
public class JRMFileChooser<V> extends JFileChooser
{
	public static class OneRootFileSystemView extends FileSystemView
	{
		File root;
		File[] roots = new File[1];
		
		public OneRootFileSystemView(File root)
		{
			try
			{
				this.root = root.getCanonicalFile();
				this.roots[0] = this.root;
			}
			catch(IOException e1)
			{
				JOptionPane.showMessageDialog(null, e1, "Exception", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}

		@Override
		public File createNewFolder(File containingDir) throws IOException
		{
			File folder = new File(containingDir, "New Folder");
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

	public JRMFileChooser(int type, int mode)
	{
		this(type, mode, null, null, null, null, false);
	}

	public JRMFileChooser(int type, int mode, File currdir)
	{
		this(type, mode, currdir, null, null, null, false);
	}

	public JRMFileChooser(Integer type, Integer mode, File currdir, File selected, List<FileFilter> filters, String title, boolean multi)
	{
		super();
		setup(type, mode, currdir, selected, filters, title, multi);
	}
	
	public JRMFileChooser<V> setup(Integer type, Integer mode, File currdir, File selected, List<FileFilter> filters, String title, boolean multi)
	{
		if(type!=null)
			setDialogType(type);
		if(mode!=null)
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
			if(filters.size()==1)
			{
				setFileFilter(filters.get(0));
				setAcceptAllFileFilterUsed(false);
			}
			else for(FileFilter filter : filters)
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
	
	public JRMFileChooser(FileSystemView fsv)
	{
		super(fsv);
	}
	
	public V show(Component parent, CallBack<V> callback)
	{
		if(showOpenDialog(parent) == JFileChooser.APPROVE_OPTION)
			return callback.call(this);
		return null;
	}
	
	public V showOpen(Component parent, CallBack<V> callback)
	{
		setDialogType(JFileChooser.OPEN_DIALOG);
		return show(parent, callback);
	}

	public V showSave(Component parent, CallBack<V> callback)
	{
		setDialogType(JFileChooser.SAVE_DIALOG);
		return show(parent, callback);
	}
}
