package jrm.profiler.report;

import jrm.Messages;
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
		return String.format(Messages.getString("EntryMissing.Missing"), parent.machine.name, entity.getName()); //$NON-NLS-1$
	}

}
