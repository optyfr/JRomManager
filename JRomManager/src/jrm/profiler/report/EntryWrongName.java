package jrm.profiler.report;

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
		return "[" + parent.machine.name + "] wrong named rom (" + entry.getName() + "->" + entity.getName() + ")";
	}

}
