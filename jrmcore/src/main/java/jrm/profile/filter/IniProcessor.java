package jrm.profile.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

interface IniProcessor
{
	String getSection();
	
	@FunctionalInterface
	interface ProcessFileCallback
	{
		void apply(String[] kv);
	}
	
	default void processFile(File file, ProcessFileCallback cb) throws IOException
	{
		try (final var reader = new BufferedReader(new FileReader(file));)
		{
			String line;
			var inSection = false;
			while (null != (line = reader.readLine()))
			{
				if (line.equalsIgnoreCase(getSection())) // $NON-NLS-1$
					inSection = true;
				else if (line.startsWith("[") && inSection) //$NON-NLS-1$
					break;
				else if (inSection)
				{
					final String[] kv = StringUtils.split(line, '=');
					if (kv.length == 2)
					{
						cb.apply(kv);
					}
				}
			}
		}
	}

}
