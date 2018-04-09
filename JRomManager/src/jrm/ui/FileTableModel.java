package jrm.ui;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FilenameUtils;

@SuppressWarnings("serial")
public class FileTableModel extends DefaultTableModel
{
	DirNode.Dir curr_dir = null;
	
	public FileTableModel(DirNode.Dir dir)
	{
		this();
		populate(dir);
	}

	public FileTableModel()
	{
		super(new String[0][1], new String[] { "Profile" });
	}

	public void populate()
	{
		populate(curr_dir);
	}

	public void populate(DirNode.Dir dir)
	{
		this.curr_dir = dir;
		if(dir!=null && dir.getFile().exists())
		{
			Object[][] objs = Arrays.asList(dir.getFile().listFiles()).stream().filter(f -> f.isFile() && !Arrays.asList("cache", "properties").contains(FilenameUtils.getExtension(f.getName()))).map(f -> new Object[] { f }).collect(Collectors.toList()).toArray(new Object[][] {});
			setDataVector(objs, new String[] { "Profile" });
		}
		else
			setDataVector(new Object[][] {}, new String[] { "Profile" });
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return false;
	}

}
