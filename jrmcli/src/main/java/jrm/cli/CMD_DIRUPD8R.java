package jrm.cli;

import java.util.LinkedHashSet;
import java.util.stream.Stream;

public enum CMD_DIRUPD8R
{
	LSSRC("lssrc"),
	LSSDR("lssdr"),
	CLEARSRC("clearsrc"),
	CLEARSDR("clearsdr"),
	ADDSRC("addsrc"),
	ADDSDR("addsdr"),
	START("start"),
	PRESETS("presets"),
	SETTINGS("settings"),
	HELP("help","?"),
	EMPTY(""),
	UNKNOWN();
	
	private LinkedHashSet<String> names = new LinkedHashSet<>();
	
	private CMD_DIRUPD8R(String... names)
	{
		for(final String name : names)
			this.names.add(name.toLowerCase());
	}

	public static CMD_DIRUPD8R of(String name)
	{
		for(CMD_DIRUPD8R value : CMD_DIRUPD8R.values())
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
