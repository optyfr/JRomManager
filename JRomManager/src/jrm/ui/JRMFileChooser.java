package jrm.ui;

import java.awt.Component;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class JRMFileChooser<V> extends JFileChooser
{
	public interface CallBack<V>
	{
		public V call(JRMFileChooser<V> chooser);
	}

	public JRMFileChooser()
	{
		this(null, null, null, null, null, null);
	}

	public JRMFileChooser(int type, int mode)
	{
		this(type, mode, null, null, null, null);
	}

	public JRMFileChooser(int type, int mode, File currdir)
	{
		this(type, mode, currdir, null, null, null);
	}

	public JRMFileChooser(Integer type, Integer mode, File currdir, File selected, List<FileFilter> filters, String title)
	{
		super();
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
			for(FileFilter filter : filters)
				addChoosableFileFilter(filter);
		if(title != null)
			setDialogTitle(title);
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
