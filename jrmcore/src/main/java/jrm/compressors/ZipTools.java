package jrm.compressors;

public class ZipTools
{
	
	public static String toZipEntry(String name)
	{
		name = name.replace('\\', '/');
		if(name.startsWith("/"))
			name = name.substring(1);
		return name;
	}

	public static String toEntry(String name)
	{
		name = name.replace('\\', '/');
		if(!name.startsWith("/"))
			name = '/' + name;
		return name;
	}
}
