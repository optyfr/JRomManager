package jrm.profile.data;

import java.util.Optional;

import lombok.Getter;

@SuppressWarnings("serial")
public final class Source implements PropertyStub
{
	private final @Getter String name;
	private @Getter int count = 1;

	private final String propname;

	public Source(String name)
	{
		this.name = name;
		this.propname = "filter.sources." + name.replace('/', '_').substring(0, Optional.of(name.lastIndexOf('.')).filter(idx -> idx > 0).orElse(name.length()));
	}

	public Source inc()
	{
		count++;
		return this;
	}
	
	public String getPropertyName()
	{
		return propname;
	}

	public String toString()
	{
		return name + " (" + count + ")";
	}
}
