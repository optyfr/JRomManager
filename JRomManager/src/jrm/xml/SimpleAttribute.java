package jrm.xml;

/**
 * A simple attribute only with {@link #name} and {@link #value}
 */
public final class SimpleAttribute
{
	final String name;
	final Object value;

	public SimpleAttribute(final String name, final Object value)
	{
		this.name = name;
		this.value = value;
	}
}