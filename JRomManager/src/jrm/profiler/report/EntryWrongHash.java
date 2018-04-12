package jrm.profiler.report;

import jrm.Messages;
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
			return String.format(Messages.getString("EntryWrongHash.Wrong"), parent.machine.name, entry.file, "CRC", entry.crc, entity.crc); //$NON-NLS-1$ //$NON-NLS-2$
		else if(entry.sha1 == null)
			return String.format(Messages.getString("EntryWrongHash.Wrong"), parent.machine.name, entry.file, "MD5", entry.md5, entity.md5); //$NON-NLS-1$ //$NON-NLS-2$
		else
			return String.format(Messages.getString("EntryWrongHash.Wrong"), parent.machine.name, entry.file, "SHA-1", entry.sha1, entity.sha1); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
