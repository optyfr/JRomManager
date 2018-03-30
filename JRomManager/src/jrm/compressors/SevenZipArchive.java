package jrm.compressors;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import jrm.misc.FindCmd;
import jrm.misc.Settings;

public class SevenZipArchive implements Closeable, AutoCloseable
{
	File tempDir = null;
	File archive;

	public SevenZipArchive(File archive) throws IOException
	{
		this.archive = archive;
		this.tempDir = Files.createTempDirectory("JRM").toFile();
	}

	@Override
	public void close() throws IOException
	{
		FileUtils.deleteDirectory(tempDir);
	}

	public File extract(String entry) throws IOException
	{
		List<String> cmd = new ArrayList<>(Collections.singletonList(Settings.getProperty("7z_cmd", FindCmd.find7z())));
		String args = Settings.getProperty("7z_extract_args", SevenZipExtractOptions.SEVENZIP.toString());
		String[] argv = args.replace("%1", archive.getAbsolutePath()).replace("%2", entry).replace("%4", tempDir.getAbsolutePath()).split("\\s");
		Collections.addAll(cmd, argv);
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process process = pb.start();
		try
		{
			process.waitFor();
			File result = new File(tempDir, entry);
			if (result.exists())
				return result;
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public int add(File baseDir, String entry) throws IOException
	{
		List<String> cmd = new ArrayList<>(Collections.singletonList(Settings.getProperty("7z_cmd", FindCmd.find7z())));
		String args = Settings.getProperty("7z_add_args", SevenZipAddOptions.SEVENZIP_ULTRA.toString());
		String[] argv = args.replace("%1", archive.getAbsolutePath()).replace("%2", entry).replace("%4", baseDir.getAbsolutePath()).split("\\s");
		Collections.addAll(cmd, argv);
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process process = pb.start();
		try
		{
			return process.waitFor();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return -1;
	}

	public int delete(String entry) throws IOException
	{
		List<String> cmd = new ArrayList<>(Collections.singletonList(Settings.getProperty("7z_cmd", FindCmd.find7z())));
		String args = Settings.getProperty("7z_del_args", SevenZipDeleteOptions.SEVENZIP_ULTRA.toString());
		String[] argv = args.replace("%1", archive.getAbsolutePath()).replace("%2", entry).split("\\s");
		Collections.addAll(cmd, argv);
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process process = pb.start();
		try
		{
			return process.waitFor();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return -1;
	}

	public int rename(String entry, String newname) throws IOException
	{
		List<String> cmd = new ArrayList<>(Collections.singletonList(Settings.getProperty("7z_cmd", FindCmd.find7z())));
		String args = Settings.getProperty("7z_ren_args", SevenZipRenameOptions.SEVENZIP_ULTRA.toString());
		String[] argv = args.replace("%1", archive.getAbsolutePath()).replace("%2", entry).replace("%3", newname).split("\\s");
		Collections.addAll(cmd, argv);
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process process = pb.start();
		try
		{
			return process.waitFor();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return -1;
	}
}
