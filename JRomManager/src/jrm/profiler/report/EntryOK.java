package jrm.profiler.report;

import jrm.profiler.data.Entity;

public class EntryOK extends Note
{
	Entity entity;
	
	public EntryOK(Entity entity)
	{
		this.entity = entity;
	}

	@Override
	public String toString()
	{
		return "[" + parent.machine.name + "] " + entity.getName() + " is OK";
	}

}
