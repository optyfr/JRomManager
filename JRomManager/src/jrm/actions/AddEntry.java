package jrm.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jrm.compressors.Archive;
import jrm.compressors.SevenZipArchive;
import jrm.compressors.SevenZipNArchive;
import jrm.data.Container.Type;
import jrm.data.Entity;
import jrm.data.Entry;
import jrm.ui.ProgressHandler;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

public class AddEntry extends EntryAction
{
	private Entity entity;

	public AddEntry(Entity entity, Entry entry)
	{
		super(entry);
		this.entity = entity;
	}

	@Override
	public boolean doAction(FileSystem dstfs, ProgressHandler handler)
	{
		Path dstpath = dstfs.getPath(entity.getName());
		handler.setProgress(null,null,null,"Adding "+dstpath.getFileName());
		Path srcpath = null;
		try(FileSystem srcfs = FileSystems.newFileSystem(entry.parent.file.toPath(), null);)
		{
			
			srcpath = srcfs.getPath(entry.file);
			if(dstpath.getParent()!=null)
				Files.createDirectories(dstpath.getParent());
			Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			return true;
		}
		catch(Throwable e)
		{
			System.err.println("add from "+entry.parent.file.getName()+"@"+srcpath+" to "+parent.container.file.getName()+"@"+dstpath+" failed");
		}
		return false;
	}

	@Override
	public boolean doAction(Path target, ProgressHandler handler)
	{
		Path dstpath = target.resolve(entity.getName());
		handler.setProgress(null,null,null,"Adding "+dstpath.getFileName());
		Path srcpath = null;
		try
		{
			if(entry.parent.getType()==Type.ZIP)
			{
				try(FileSystem srcfs = FileSystems.newFileSystem(entry.parent.file.toPath(), null);)
				{
					srcpath = srcfs.getPath(entry.file);
					Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				}
				catch(Throwable e)
				{
					System.err.println("add from "+entry.parent.file.getName()+"@"+entry.file+" to "+parent.container.file.getName()+"@"+entity.getName()+" failed");
				}
				
			}
			else if(entry.parent.getType()==Type.SEVENZIP)
			{
				
				try(Archive srcarchive = new SevenZipArchive(entry.parent.file))
				{
					if(srcarchive.extract(entry.file)!=null)
					{
						srcpath = new File(srcarchive.getTempDir(), entry.file).toPath();
						Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
					}
				//	return archive.add_stdin(srcarchive.extract_stdout(entry.file) , entity.getName()) == 0;
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				srcpath = entry.parent.file.toPath().resolve(entry.file);
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			}
			return true;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			System.err.println("add from "+entry.parent.file.getName()+"@"+srcpath+" to "+parent.container.file.getName()+"@"+dstpath+" failed");
		}
		return false;
	}

	@Override
	public boolean doAction(Archive archive, ProgressHandler handler)
	{	
		if(entry.parent.getType()==Type.ZIP)
		{
			try(FileSystem srcfs = FileSystems.newFileSystem(entry.parent.file.toPath(), null);)
			{
				return archive.add_stdin(Files.newInputStream(srcfs.getPath(entry.file)),entity.getName()) == 0;
			}
			catch(Throwable e)
			{
				System.err.println("add from "+entry.parent.file.getName()+"@"+entry.file+" to "+parent.container.file.getName()+"@"+entity.getName()+" failed");
			}
		}
		else if(entry.parent.getType()==Type.SEVENZIP)
		{
			try(Archive srcarchive = new SevenZipNArchive(entry.parent.file))
			{
				if(srcarchive.extract(entry.file)!=null)
					return archive.add(srcarchive.getTempDir(), entry.file)==0;
			//	return archive.add_stdin(srcarchive.extract_stdout(entry.file) , entity.getName()) == 0;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (SevenZipNativeInitializationException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return false;
	}

}
