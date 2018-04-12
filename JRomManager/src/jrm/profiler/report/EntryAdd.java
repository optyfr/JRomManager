package jrm.profiler.report;

import jrm.profiler.data.Entity;
import jrm.profiler.data.Entry;

public class EntryAdd extends Note
{
	Entity entity;
	Entry entry;
	
	public EntryAdd(Entity entity, Entry entry)
	{
		this.entity = entity;
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return "[" + parent.machine.name + "] " + entity.getName() + " <- " + entry.parent.file.getName() + "@" + entry.file;
	}

}