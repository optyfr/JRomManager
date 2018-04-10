package jrm.profiler.report;

import jrm.profiler.data.Entity;

public class EntryMissing extends Note
{
	Entity entity;
	
	public EntryMissing(Entity entity)
	{
		this.entity = entity;
	}

	@Override
	public String toString()
	{
		return "[" + parent.machine.name + "] " + entity.getName() + " is missing and not fixable";
	}

}
