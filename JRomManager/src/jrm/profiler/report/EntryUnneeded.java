package jrm.profiler.report;

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
		return "[" + parent.machine.name + "] " + entry.file + " unneeded (sha1=" + entry.sha1 + ")";
	}

}
