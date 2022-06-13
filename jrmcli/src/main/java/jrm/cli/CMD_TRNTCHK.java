package jrm.cli;

import java.util.LinkedHashSet;
import java.util.stream.Stream;

public enum CMD_TRNTCHK
{
	LSSDR("lssdr"),
	CLEARSDR("clearsdr"),
	ADDSDR("addsdr"),
	START("start"),
	HELP("help","?"),
	EMPTY(""),
	UNKNOWN();
	
	private LinkedHashSet<String> names = new LinkedHashSet<>();
	
	private CMD_TRNTCHK(String... names)
	{
		for(final String name : names)
			this.names.add(name.toLowerCase());
	}

	public static CMD_TRNTCHK of(String name)
	{
		for(CMD_TRNTCHK value : CMD_TRNTCHK.values())
			if(value.names.contains(name.toLowerCase()))
				return value;
		return UNKNOWN;
	}
	
	@Override
	public String toString()
	{
		return names.stream().findFirst().orElse(super.toString());
	}
	
	public Stream<String> allStrings()
	{
		return names.stream();
	}

}
