package profiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import misc.Log;

public class Import
{
	public Import(File file)
	{
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File xmldir  = new File(workdir,"xmlfiles");
		xmldir.mkdir();

		String ext = FilenameUtils.getExtension(file.getName());
		if(ext.equalsIgnoreCase("exe"))
		{
			Log.info("Get dat file from Mame...");
			try
			{
				File tmpfile = File.createTempFile("JRM", ".xml");
				tmpfile.deleteOnExit();
				Process process = new ProcessBuilder(file.getAbsolutePath(),"-listxml").directory(file.getAbsoluteFile().getParentFile()).redirectOutput(tmpfile).start();
				process.waitFor();
				FileUtils.copyFileToDirectory(tmpfile, xmldir);
			}
			catch(IOException e)
			{
				Log.err("Caught IO Exception", e);
			}
			catch(InterruptedException e)
			{
				Log.err("Caught Interrupted Exception", e);
			}
		}
		else
			try
			{
				FileUtils.copyFileToDirectory(file, xmldir);
			}
			catch(IOException e)
			{
				Log.err("Caught IO Exception", e);
			}
	}
	
	
}
