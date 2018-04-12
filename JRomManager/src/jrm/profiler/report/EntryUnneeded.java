package jrm.profiler.report;

import jrm.Messages;
import jrm.profiler.data.Entry;

public class EntryUnneeded extends Note
{
	Entry entry;

	public EntryUnneeded(Entry entry)
	{
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryUnneeded.Unneeded"), parent.machine.name, entry.file, entry.sha1); //$NON-NLS-1$
	}

}
