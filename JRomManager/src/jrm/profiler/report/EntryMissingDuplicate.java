package jrm.profiler.report;

import jrm.profiler.data.Entity;
import jrm.profiler.data.Entry;

public class EntryMissingDuplicate extends Note
{
	Entity entity;
	Entry entry;
	
	public EntryMissingDuplicate(Entity entity, Entry entry)
	{
		this.entity = entity;
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return "[" + parent.machine.name + "] duplicate " + entry.file + " >>> " + entity.getName();
	}

}
