package jrm.profile.fix.actions;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.compressors.Archive;
import jrm.compressors.SevenZipArchive;
import jrm.compressors.ZipArchive;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.ui.ProgressHandler;

public class CreateContainer extends ContainerAction
{

	public CreateContainer(final Container container, final FormatOptions format)
	{
		super(container, format);
	}

	public static CreateContainer getInstance(CreateContainer action, final Container container, final FormatOptions format)
	{
		if(action == null)
			action = new CreateContainer(container, format);
		return action;
	}

	@Override
	public boolean doAction(final ProgressHandler handler)
	{
		handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("CreateContainer.Creating")), toBlue(container.m.getFullName(container.file.getName())), toPurple(container.m.description))))); //$NON-NLS-1$
		if(container.getType() == Container.Type.ZIP)
		{
			if(format == FormatOptions.ZIP || format == FormatOptions.TZIP)
			{
				final Map<String, Object> env = new HashMap<>();
				env.put("create", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				env.put("useTempFile", Boolean.TRUE); //$NON-NLS-1$
				try(FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + container.file.toURI()), env);) //$NON-NLS-1$
				{
					for(final EntryAction action : entry_actions)
						if(!action.doAction(fs, handler))
						{
							System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed");
							return false;
						}
					fs.close();
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
						if(!action.doAction(archive, handler))
						{
							System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed");
							return false;
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
					if(!action.doAction(archive, handler))
					{
						System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed");
						return false;
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
			try
			{
				final Path target = container.file.toPath();
				if(FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) //$NON-NLS-1$
					Files.createDirectories(target, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x"))); //$NON-NLS-1$
				else
					Files.createDirectories(target);
				for(final EntryAction action : entry_actions)
					if(!action.doAction(target, handler))
					{
						System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed");
						return false;
					}
				return true;
			}
			catch(final Throwable e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public String toString()
	{
		String str = Messages.getString("CreateContainer.Create") + container; //$NON-NLS-1$
		for(final EntryAction action : entry_actions)
			str += "\n\t" + action; //$NON-NLS-1$
		return str;
	}

}
