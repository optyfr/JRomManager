package jrm.compressors.sevenzipjbinding;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;

import jrm.compressors.Archive;
import jrm.misc.IOUtils;
import jrm.misc.Log;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.experimental.Accessors;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;

@Accessors(chain = true)
public abstract class NArchiveBase implements Archive, Closeables
{
	private @Getter @Setter IInArchive iInArchive = null;
	private @Getter @Setter IInStream iInStream = null;

	/*
	 * This is where all directives are stored until final archive modification upon
	 * archive closing
	 */
	private final @Getter List<String> toAdd = new ArrayList<>();
	private final @Getter HashSet<String> toDelete = new HashSet<>();
	private final @Getter HashMap<String, String> toRename = new HashMap<>();
	private final @Getter HashMap<String, String> toCopy = new HashMap<>();

	private final @Getter List<Closeable> closeables = new ArrayList<>();

	private File tempDir = null;

	@Override
	public void addCloseables(Closeable closeable)
	{
		closeables.add(closeable);
	}

	@Override
	public void close() throws IOException
	{
		for (val closeable : closeables)
			closeable.close();
		closeables.clear();
	}

	public File getTempDir() throws IOException
	{
		if (tempDir == null)
			tempDir = IOUtils.createTempDirectory("JRM").toFile(); //$NON-NLS-1$
		return tempDir;
	}

	protected void clearTempDir()
	{
		try
		{
			if (tempDir != null)
				FileUtils.deleteDirectory(tempDir);
		}
		catch (final Exception e)
		{
			Log.err(e.getMessage(), e);
		}
	}

}
