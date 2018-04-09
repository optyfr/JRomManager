package jrm.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FilenameUtils;

@SuppressWarnings("serial")
public class FileTableModel extends DefaultTableModel implements TableModelListener
{
	public DirNode.Dir curr_dir = null;
	private List<String> backup = new ArrayList<>();
	
	public FileTableModel(DirNode.Dir dir)
	{
		this();
		populate(dir);
	}

	public FileTableModel()
	{
		super(new String[0][1], new String[] { "Profile" });
		addTableModelListener(this);
	}

	public void populate()
	{
		populate(curr_dir);
	}

	public void populate(DirNode.Dir dir)
	{
		this.curr_dir = dir;
		setRowCount(0);
		backup.clear();
		if(dir!=null && dir.getFile().exists())
		{
			Arrays.asList(dir.getFile().listFiles(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					File f = new File(dir,name);
					if(f.isFile())
						if(!Arrays.asList("cache", "properties").contains(FilenameUtils.getExtension(name)))
							return true;
					return false;
				}
			})).stream().map(f -> new String[] { f.getName() }).forEach(obj-> {
				addRow(obj);
				backup.add(((String[])obj)[0]);
			});
		}
		
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return column==0;
	}

	public File getFileAt(int row)
	{
		return new File(curr_dir.getFile(), getValueAt(row, 0).toString());
	}

	@Override
	public void tableChanged(TableModelEvent e)
	{
		if(e.getType()==TableModelEvent.UPDATE)
		{
			if(e.getColumn()==0)
			{
				String newname = getValueAt(e.getFirstRow(), 0).toString();
				Arrays.asList("",".cache",".properties").forEach(ext -> {
					File oldfile = new File(curr_dir.getFile(), backup.get(e.getFirstRow())+ext);
					File newfile = new File(curr_dir.getFile(), newname+ext);
					oldfile.renameTo(newfile);
				});
			}
		}
	}
	
	
}
