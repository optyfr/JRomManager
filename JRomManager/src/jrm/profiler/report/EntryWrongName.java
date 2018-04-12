package jrm.profiler.report;

import jrm.Messages;
import jrm.profiler.data.Entity;
import jrm.profiler.data.Entry;

public class EntryWrongName extends Note
{
	Entity entity;
	Entry entry;
	
	public EntryWrongName(Entity entity, Entry entry)
	{
		this.entity = entity;
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryWrongName.Wrong"), parent.machine.name, entry.getName(), entity.getName()); //$NON-NLS-1$
	}

}
