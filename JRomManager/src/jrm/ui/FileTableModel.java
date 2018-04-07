package jrm.ui;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class FileTableModel extends DefaultTableModel
{
	public FileTableModel(DirNode.Dir dir)
	{
		this();
		populate(dir);
	}

	public FileTableModel()
	{
		super(new String[0][1], new String[] {"Profile"});
	}

	public void populate(DirNode.Dir dir)
	{
		Object[][] objs = Arrays.asList(dir.getFile().listFiles()).stream().filter(File::isFile).map(f->new Object[] {f}).collect(Collectors.toList()).toArray(new Object[][] {});
		setDataVector(objs,new String[] {"Profile"});
	}
	
	@Override
	public boolean isCellEditable(int row, int column)
	{
		return false;
	}
	
}
