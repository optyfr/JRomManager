package jrm.profile.data;

import java.util.Optional;

import lombok.Getter;

@SuppressWarnings("serial")
public final class Source implements PropertyStub
{
	private final @Getter String name;
	private final @Getter int count;

	private final String propname;

	public Source(String name, int count)
	{
		this.name = name;
		this.count = count;
		this.propname = "filter.sources." + name.replace('/', '_').substring(0, Optional.of(name.lastIndexOf('.')).filter(idx -> idx > 0).orElse(name.length()));
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
