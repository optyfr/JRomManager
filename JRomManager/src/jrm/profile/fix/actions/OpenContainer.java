package jrm.profile.fix.actions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
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
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.ui.ProgressHandler;

public class OpenContainer extends ContainerAction
{

	public OpenContainer(Container container, FormatOptions format)
	{
		super(container, format);
	}

	public static OpenContainer getInstance(OpenContainer action, Container container, FormatOptions format)
	{
		if(action == null)
			action = new OpenContainer(container, format);
		return action;
	}

	@Override
	public boolean doAction(ProgressHandler handler)
	{
		handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("OpenContainer.Fixing")), toBlue(container.m.getFullName(container.file.getName())), toPurple(container.m.description))))); //$NON-NLS-1$
		if(container.getType() == Container.Type.ZIP)
		{
			if(format == FormatOptions.ZIP || format == FormatOptions.TZIP)
			{
				Map<String, Object> env = new HashMap<>();
				env.put("create", "false"); //$NON-NLS-1$ //$NON-NLS-2$
				env.put("useTempFile", Boolean.TRUE); //$NON-NLS-1$
				try(FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + container.file.toURI()), env);) //$NON-NLS-1$
				{
					for(EntryAction action : entry_actions)
					{
						if(!action.doAction(fs, handler))
						{
							System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed");
							return false;
						}
					}
					deleteEmptyFolders(fs.getPath("/")); //$NON-NLS-1$
					fs.close();
					return true;
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
			else if(format == FormatOptions.ZIPE)
			{
				try(Archive archive = new ZipArchive(container.file))
				{
					for(EntryAction action : entry_actions)
					{
						if(!action.doAction(archive, handler))
						{
							System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed");
							return false;
						}
					}
					return true;
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
		}
		else if(container.getType() == Container.Type.SEVENZIP)
		{
			try(Archive archive = new SevenZipArchive(container.file))
			{
				for(EntryAction action : entry_actions)
				{
					if(!action.doAction(archive, handler))
					{
						System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed");
						return false;
					}
				}
				return true;
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
		else if(container.getType() == Container.Type.DIR)
		{
			Path target = container.file.toPath();
			for(EntryAction action : entry_actions)
			{
				if(!action.doAction(target, handler))
				{
					System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed");
					return false;
				}
			}
			deleteEmptyFolders(container.file);
			return true;
		}
		return false;
	}

	public long deleteEmptyFolders(File baseFolder)
	{
		long totalSize = 0;
		for(File folder : baseFolder.listFiles())
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
	
	public long deleteEmptyFolders(Path baseFolder)
	{
		long totalSize = 0;
		try
		{
			for(Path folder : Files.list(baseFolder).collect(Collectors.toList()))
			{
				if(Files.isDirectory(folder))
					totalSize += deleteEmptyFolders(folder);
				else
					totalSize += Files.size(folder);
			}
			if(totalSize == 0)
				Files.delete(baseFolder);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return totalSize;
	}
	
	@Override
	public String toString()
	{
		String str = Messages.getString("OpenContainer.Open") + container; //$NON-NLS-1$
		for(EntryAction action : entry_actions)
			str += "\n\t" + action; //$NON-NLS-1$
		return str;
	}
}
