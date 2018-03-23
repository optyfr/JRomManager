package profiler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import actions.AddRom;
import actions.CreateSet;
import actions.OpenSet;
import data.Archive;
import data.Container;
import data.Directory;
import data.Entry;
import data.Machine;
import data.Rom;
import misc.Log;
import ui.ProgressHandler;

public class Scan
{

	public ArrayList<actions.SetAction> actions = new ArrayList<>(); 

	
	public Scan(Profile profile, File dir, ProgressHandler handler)
	{
		ArrayList<Container> containers = new ArrayList<>();
		HashMap<String, Container> containers_byname = new HashMap<>();
		ArrayList<Container> unknown = new ArrayList<>();
		HashMap<String, Entry> entries_bycrc = new HashMap<>();

		
		Path path = Paths.get(dir.getAbsolutePath());
		
		/*
		 * Loading scan cache
		 */
		containers_byname = load(dir, handler);
		
		/*
		 * List files;
		 */
		handler.setProgress("Listing files...",-1);
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path))
		{
			Container c;
			for(Path p : stream)
			{
				File file  = p.toFile();
				if(null==(c=containers_byname.get(file.getName())) || c.modified!=file.lastModified())
				{
					if(file.isFile())
						containers.add(c = new Archive(file));
					else if(file.isDirectory())
						containers.add(c= new Directory(file));
					else
						c = null;
					if(c != null)
					{
						c.up2date = true;
						containers_byname.put(file.getName(), c);
					}
				}
				else
				{
					c.up2date = true;
					containers.add(c);
				}
				handler.setProgress("Listing files... ("+containers.size()+")",-1);
			}
			if(containers_byname.entrySet().removeIf(entry -> !entry.getValue().up2date))
				Log.info("Removed some scache elements");
		}
		catch(IOException e)
		{
			Log.err("IOException when listing", e);
		}
		catch(Throwable e)
		{
			Log.err("Other Exception when listing", e);
		}
		try 
		{
			int i = 0;
			handler.setProgress("Scanning files", i, containers.size());
			for(Container c : containers)
			{
				File f = c.file;
				if(c instanceof Archive)
				{
					String name = FilenameUtils.getBaseName(f.toString());
					if(c.loaded==0)
					{
						String ext = FilenameUtils.getExtension(f.toString());
						if(ext.equalsIgnoreCase("zip"))
						{
							try(FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);)
							{
								final Path root = fs.getPath("/");
								Files.walkFileTree(root, new SimpleFileVisitor<Path>()
								{
									@Override
									public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
									{
										Map<String,Object> attr = Files.readAttributes(file, "zip:*");
										Entry entry = new Entry(file.toString());
										c.add(entry);
										entry.size = (Long)attr.get("size");
										entry.crc = String.format("%08x",attr.get("crc"));
										entries_bycrc.put(entry.crc, entry);
										return FileVisitResult.CONTINUE;
									}
		
									@Override
									public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
									{
										return FileVisitResult.CONTINUE;
									}
								});
								c.loaded = 1;
							}
						}
					}
					else
					{
						for(Entry entry : c.entries)
							entries_bycrc.put(entry.crc, entry);
					}
					if(!profile.machines_byname.containsKey(name))
						unknown.add(c);
					
				}
				else if(c instanceof Directory)
				{
					String name = FilenameUtils.getBaseName(f.toString());
					if(!profile.machines_byname.containsKey(name))
						unknown.add(c);
					
				}
				handler.setProgress("Scanning "+f.getName(), ++i, null, i+"/"+containers.size()+" ("+(int)(i*100.0/containers.size())+"%)");
			}
			
			
		}
		catch(IOException e)
		{
			Log.err("IOException when scanning", e);
		}
		catch(Throwable e)
		{
			Log.err("Other Exception when listing", e);
		}
		
		save(dir, containers_byname);
		
		ArrayList<actions.SetAction> create_actions = new ArrayList<>();
		ArrayList<actions.SetAction> update_actions = new ArrayList<>();
		
		int i = 0;
		handler.setProgress("Writing report", i, profile.machines.size());
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File reportdir = new File(workdir,"reports");
		reportdir.mkdirs();
		File report = new File(reportdir,"report.log");
		try(PrintWriter report_w = new PrintWriter(report))
		{
			int missing_set = 0;
			for(Machine m : profile.machines) if(!m.isdevice && m.roms.size()>0)
			{
				Container c;
				if(null!=(c=containers_byname.get(m.name+".zip")))
				{
					OpenSet updateset = new OpenSet(new Archive(new File(dir,m.name+".zip")));
					for(Rom r : m.roms)
					{
						if((r.bios==null || m.isbios) && !r.status.equals("nodump") && !(m.romof != null && r.merge!=null))
						{
							ArrayList<Entry> roms_found = new ArrayList<>();
							Entry found = null;
							for(Entry e : c.entries)
							{
								if(r.crc ==null || r.crc.equals("0"))
								{
									if(r.crc == null)
										report_w.println("["+m.name+"] "+r.name+" has not crc");
									if(r.name.equals(new File(e.file).getName()))
									{
										found = e;
										break;
									}
								}
								else if(r.crc.equals(e.crc))
								{
									if(!r.name.equals(new File(e.file).getName()))
										report_w.println("["+m.name+"] wrong named rom ("+e.file+"->"+r.name+")");
									found = e;
									break;
								}
								else if(r.name.equals(new File(e.file).getName()))
								{
									report_w.println("["+m.name+"] wrong crc (got "+e.crc+" vs "+r.crc+")");
									found = e;
									break;
								}
							}
							if(found==null)
							{
								String submsg = "";
								if(null!=(found=entries_bycrc.get(r.crc)))
								{
									submsg += "\t["+m.name+"] "+r.name+" <- "+found.parent.file.getName()+"@"+found.file+"\n";
									updateset.addRomAction(new AddRom(r, found));
									roms_found.add(found);
								}
								else
								{
									submsg += "\t["+m.name+"] "+r.name+" is missing and not fixable\n";  
								}
							}
							else
								roms_found.add(found);
							List<Entry> unneeded = c.entries.stream().filter(not(new HashSet<>(roms_found)::contains)).collect(Collectors.toList());
							for(Entry e : unneeded)
							{
								report_w.println("["+m.name+"] "+e.file+" unneeded ("+e.crc+")");
							}
						}
					}
					if(updateset.roms.size()>0)
						update_actions.add(updateset);
				}
				else
				{
					missing_set++;
					ArrayList<Entry> roms_found = new ArrayList<>();
					boolean partial = false;
					String submsg = "";
					CreateSet createset = new CreateSet(new Archive(new File(dir,m.name+".zip")));
					create_actions.add(createset);
					for(Rom r : m.roms)
					{
						if((r.bios==null || m.isbios) && !r.status.equals("nodump") && !(m.romof != null && r.merge!=null))
						{
							Entry found = null;
							if(r.crc !=null && !r.crc.equals("0") && null!=(found=entries_bycrc.get(r.crc)))
							{
								submsg += "\t["+m.name+"] "+r.name+" <- "+found.parent.file.getName()+"@"+found.file+"\n";
								createset.addRomAction(new AddRom(r, found));
								roms_found.add(found);
							}
							else
							{
								submsg += "\t["+m.name+"] "+r.name+" is missing and not fixable\n";  
								partial = true;
							}
						}
					}
					String msg = "["+m.name+"] is missing";
					if(roms_found.size()>0)
						msg += ", but can "+(partial?"partially":"totally")+" be recreated :\n"+submsg;
					report_w.println(msg);
				}
				handler.setProgress(null, ++i);
			}
			report_w.println("Missing sets : "+missing_set+"/"+profile.machines.size());
		}
		catch(FileNotFoundException e)
		{
			Log.err("Report Exception", e);
		}
		catch(Throwable e)
		{
			Log.err("Other Exception when listing", e);
		}

		actions.addAll(create_actions);
		actions.addAll(update_actions);
		
	}
	
	private static <T> Predicate<T> not(Predicate<T> predicate) {
	    return predicate.negate();
	}
	
	private static File getCacheFile(File file)
	{
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File cachedir = new File(workdir,"cache");
		cachedir.mkdirs();
		return new File(cachedir, file.getAbsolutePath().hashCode()+".scache");
	}
	
	private void save(File file, Object obj)
	{
		try(ObjectOutputStream oos  = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getCacheFile(file)))))
		{
			oos.writeObject(obj);
		}
		catch(Throwable e)
		{
		}
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Container> load(File file, ProgressHandler handler)
	{
		File cachefile = getCacheFile(file);
		try(ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cachefile))))
		{
			handler.setProgress("Loading scan cache...", -1);
			return (HashMap<String, Container>)ois.readObject();
		}
		catch(Throwable e)
		{
		}
		return new HashMap<>();
	}
}
