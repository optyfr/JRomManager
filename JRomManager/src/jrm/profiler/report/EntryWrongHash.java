package jrm.profiler.report;

import jrm.profiler.data.Entity;
import jrm.profiler.data.Entry;

public class EntryWrongHash extends Note
{
	Entity entity;
	Entry entry;
	
	public EntryWrongHash(Entity entity, Entry entry)
	{
		this.entity = entity;
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		if(entry.md5 == null && entry.sha1 == null)
			return "[" + parent.machine.name + "] " + entry.file + " has wrong crc (got " + entry.crc + " vs " + entity.crc + ")";
		else if(entry.sha1 == null)
			return "[" + parent.machine.name + "] " + entry.file + " has wrong md5 (got " + entry.md5 + " vs " + entity.md5 + ")";
		else
			return "[" + parent.machine.name + "] " + entry.file + " has wrong sha1 (got " + entry.sha1 + " vs " + entity.sha1 + ")";
	}

}
