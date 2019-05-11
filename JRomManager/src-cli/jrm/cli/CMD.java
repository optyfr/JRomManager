package jrm.cli;

import java.util.LinkedHashSet;

public enum CMD
{
	LS("ls","list","dir"),
	CD("cd"),
	LOAD("load"),
	PWD("pwd"),
	EXIT("exit","quit","bye"),
	PREFS("prefs", "env"),
	SETTINGS("settings","set"),
	EMPTY(""),
	UNKNOWN();
	
	private LinkedHashSet<String> names = new LinkedHashSet<>();
	
	private CMD(String... names)
	{
		for(final String name : names)
			this.names.add(name.toLowerCase());
	}
	
	public static CMD of(String name)
	{
		for(CMD value : CMD.values())
			if(value.names.contains(name.toLowerCase()))
				return value;
		return UNKNOWN;
	}
	
	@Override
	public String toString()
	{
		return names.stream().findFirst().orElse(super.toString());
	}
}
