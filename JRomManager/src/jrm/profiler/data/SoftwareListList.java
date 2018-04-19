package jrm.profiler.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public final class SoftwareListList extends AnywareListList<SoftwareList> implements Serializable
{
	private ArrayList<SoftwareList> sl_list = new ArrayList<>();
	public HashMap<String, SoftwareList> sl_byname = new HashMap<>();

	public SoftwareListList()
	{
		columns = new String[] { "name", "description" };
		columnsTypes = new Class<?>[] { String.class, String.class };
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		columns = new String[] {"name","description"};
		columnsTypes = new Class<?>[] {String.class,String.class};
	}
	
	@Override
	public int getRowCount()
	{
		return sl_list.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch(columnIndex)
		{
			case 0: return sl_list.get(rowIndex).name;
			case 1: return sl_list.get(rowIndex).description.toString();
		}
		return null;
	}

	public void sort()
	{
		sl_list.sort(null);
	}

	@Override
	protected List<SoftwareList> getList()
	{
		return sl_list;
	}
	

}
