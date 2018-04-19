package jrm.profiler.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public final class MachineListList extends AnywareListList<MachineList> implements Serializable
{
	private List<MachineList> ml_list = Collections.singletonList(new MachineList());

	public MachineListList()
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
		return ml_list.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch(columnIndex)
		{
			case 0: return "*";
			case 1: return "All Machines"; 
		}
		return null;
	}

	@Override
	protected List<MachineList> getList()
	{
		return ml_list;
	}

}
