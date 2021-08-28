package jrm.profile.report;

import jrm.profile.data.EntityBase;
import jrm.profile.data.Entry;

abstract class EntryExtNote extends EntryNote
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Wrong hash {@link Entry}
	 */
	final Entry entry;
	
	protected EntryExtNote(EntityBase entity, Entry entry)
	{
		super(entity);
		this.entry = entry;
	}
	
	@Override
	public String getDetail()
	{
		return getExpectedEntity(entity) + getCurrentEntry(entry);
	}
	
	@Override
	public String getName()
	{
		if(entity != null)
			return super.getName();
		return entry.getName();
	}
	
	@Override
	public String getCrc()
	{
		if(entity != null)
			return super.getCrc();
		return entry.getCrc();
	}

	@Override
	public String getMd5()
	{
		if(entity != null)
			return super.getMd5();
		return entry.getMd5();
	}

	@Override
	public String getSha1()
	{
		if(entity != null)
			return super.getSha1();
		return entry.getSha1();
	}

	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
}
