package jrm.cli;

import java.util.LinkedHashSet;
import java.util.stream.Stream;

public enum CMD
{
	CD("cd"),
	PWD("pwd"),
	SET("set"),
	LS("ls","list","dir"),
	RM("rm","del"),
	MD("md","mkdir"),
	QUIET("quiet"),
	VERBOSE("verbose"),
	PREFS("prefs", "env"),
	LOAD("load"),
	SETTINGS("settings","set"),
	SCAN("scan"),
	SCANRESULT("scanresult","scanresults"),
	FIX("fix"),
	DIRUPD8R("dirupdater","dirupd8r"),
	TRNTCHK("torrentchecker","trntchk"),
	COMPRESSOR("compressor","compress"),
	EXIT("exit","quit","bye"),
	HELP("help","?"),
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
	
	public Stream<String> allStrings()
	{
		return names.stream();
	}
}
