package jrm.profile.fix.actions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.compressors.Archive;
import jrm.compressors.SevenZipArchive;
import jrm.compressors.ZipArchive;
import jrm.compressors.zipfs.ZipFileSystemProvider;
import jrm.compressors.zipfs.ZipLevel;
import jrm.compressors.zipfs.ZipTempThreshold;
import jrm.misc.Settings;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.ui.ProgressHandler;

public class OpenContainer extends ContainerAction
{
	private final long dataSize;

	public OpenContainer(final Container container, final FormatOptions format, final long dataSize)
	{
		super(container, format);
		this.dataSize = dataSize;
	}

	public static OpenContainer getInstance(OpenContainer action, final Container container, final FormatOptions format, final long dataSize)
	{
		if(action == null)
			action = new OpenContainer(container, format, dataSize);
		return action;
	}

	@Override
	public boolean doAction(final ProgressHandler handler)
	{
		handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("OpenContainer.Fixing")), toBlue(container.m.getFullName(container.file.getName())), toPurple(container.m.getDescription()))))); //$NON-NLS-1$
		if(container.getType() == Container.Type.ZIP)
		{
			if(format == FormatOptions.ZIP || format == FormatOptions.TZIP)
			{
				final Map<String, Object> env = new HashMap<>();
				env.put("useTempFile", dataSize > ZipTempThreshold.valueOf(Settings.getProperty("zip_temp_threshold", ZipTempThreshold._10MB.toString())).getThreshold()); //$NON-NLS-1$
				env.put("compressionLevel", format == FormatOptions.TZIP ? 1 : ZipLevel.valueOf(Settings.getProperty("zip_compression_level", ZipLevel.DEFAULT.toString())).getLevel());
				try(FileSystem fs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + container.file.toURI()), env);) //$NON-NLS-1$
				{
						for(final EntryAction action : entry_actions)
						{
							if(!action.doAction(fs, handler))
							{
								System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								return false;
							}
						}
						deleteEmptyFolders(fs.getPath("/")); //$NON-NLS-1$
						return true;
				}
				catch(final Throwable e)
				{
					e.printStackTrace();
				}
			}
			else if(format == FormatOptions.ZIPE)
			{
				try(Archive archive = new ZipArchive(container.file))
				{
					for(final EntryAction action : entry_actions)
					{
						if(!action.doAction(archive, handler))
						{
							System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							return false;
						}
					}
					return true;
				}
				catch(final Throwable e)
				{
					e.printStackTrace();
				}
			}
		}
		else if(container.getType() == Container.Type.SEVENZIP)
		{
			try(Archive archive = new SevenZipArchive(container.file))
			{
				for(final EntryAction action : entry_actions)
				{
					if(!action.doAction(archive, handler))
					{
						System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						return false;
					}
				}
				return true;
			}
			catch(final Throwable e)
			{
				e.printStackTrace();
			}
		}
		else if(container.getType() == Container.Type.DIR)
		{
			final Path target = container.file.toPath();
			for(final EntryAction action : entry_actions)
			{
				if(!action.doAction(target, handler))
				{
					System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return false;
				}
			}
			deleteEmptyFolders(container.file);
			return true;
		}
		return false;
	}

	public long deleteEmptyFolders(final File baseFolder)
	{
		long totalSize = 0;
		for(final File folder : baseFolder.listFiles())
		{
			if(folder.isDirectory())
				totalSize += deleteEmptyFolders(folder);
			else
				totalSize += folder.length();
		}
		if(totalSize == 0)
			baseFolder.delete();
		return totalSize;
	}

	public long deleteEmptyFolders(final Path baseFolder)
	{
		long totalSize = 0;
		try
		{
			for(final Path folder : Files.list(baseFolder).collect(Collectors.toList()))
			{
				if(Files.isDirectory(folder))
					totalSize += deleteEmptyFolders(folder);
				else
					totalSize += Files.size(folder);
			}
			if(totalSize == 0)
				Files.delete(baseFolder);
		}
		catch(final IOException e)
		{
			e.printStackTrace();
		}
		return totalSize;
	}

	@Override
	public String toString()
	{
		String str = Messages.getString("OpenContainer.Open") + container; //$NON-NLS-1$
		for(final EntryAction action : entry_actions)
			str += "\n\t" + action; //$NON-NLS-1$
		return str;
	}
}
