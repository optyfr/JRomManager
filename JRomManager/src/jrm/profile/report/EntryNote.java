package jrm.profile.report;

import jrm.profile.data.Entity;
import jrm.profile.data.EntityBase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class EntryNote extends Note
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * The related {@link EntityBase}
	 */
	protected final EntityBase entity;

	@Override
	public String getDetail()
	{
		return getExpectedEntity(entity);
	}

	@Override
	public String getName()
	{
		return entity.getBaseName();
	}

	@Override
	public String getCrc()
	{
		if(entity instanceof Entity)
			return ((Entity)entity).getCrc();
		return null;
	}

	@Override
	public String getMd5()
	{
		if(entity instanceof Entity)
			return ((Entity)entity).getMd5();
		return null;
	}

	@Override
	public String getSha1()
	{
		if(entity instanceof Entity)
			return ((Entity)entity).getSha1();
		return null;
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
