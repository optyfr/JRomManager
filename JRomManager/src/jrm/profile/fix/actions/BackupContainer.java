package jrm.profile.fix.actions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

import jrm.compressors.zipfs.ZipFileSystemProvider;
import jrm.profile.data.Container;
import jrm.profile.data.Entry;
import jrm.profile.scan.options.FormatOptions;
import jrm.ui.ProgressHandler;

public class BackupContainer extends ContainerAction
{

	public BackupContainer(Container container)
	{
		super(container, FormatOptions.ZIP);
	}

	public static BackupContainer getInstance(BackupContainer action, final Container container)
	{
		if (action == null)
			action = new BackupContainer(container);
		return action;
	}

	private static Map<String, FileSystem> filesystems = new HashMap<String, FileSystem>();

	public static synchronized FileSystem getFS(Container container, EntryAction action) throws IOException
	{
		String crc2 = action.entry.crc.substring(0, 2);
		if (!filesystems.containsKey(crc2))
		{
			final File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
			final File backupdir = new File(workdir, "backup"); //$NON-NLS-1$
			final CRC32 crc = new CRC32();
			crc.update(container.file.getAbsoluteFile().getParent().getBytes());
			final File backupfile = new File(new File(backupdir, String.format("%08x", crc.getValue())), crc2 + ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
			backupfile.getParentFile().mkdirs();
			final Map<String, Object> env = new HashMap<>();
			if (!backupfile.exists())
				env.put("create", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			env.put("useTempFile", true); //$NON-NLS-1$
			env.put("compressionLevel", 1); //$NON-NLS-1$
			filesystems.put(crc2, new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + backupfile.toURI()), env)); //$NON-NLS-1$
		}
		return filesystems.get(crc2);
	}

	public static void closeAllFS()
	{
		filesystems.values().forEach(fs -> {
			try
			{
				synchronized (fs)
				{
					fs.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		});
		filesystems.clear();
	}

	@Override
	public boolean doAction(ProgressHandler handler)
	{
		try
		{
			int i = 0;
			if (entry_actions.size() == 0)
				for (Entry entry : container.getEntries())
					addAction(new BackupEntry(entry));
			for (final EntryAction action : entry_actions)
			{
				i++;
				final FileSystem fs = getFS(container, action);
				synchronized (fs)
				{
					if (!action.doAction(fs, handler, i, entry_actions.size()))
					{
						System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						return false;
					}
				}
			}
			return true;
		}
		catch (final Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}

}
