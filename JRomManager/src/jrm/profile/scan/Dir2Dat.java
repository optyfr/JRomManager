package jrm.profile.scan;

import java.io.File;
import java.util.EnumSet;

import jrm.profile.data.Container;
import jrm.profile.data.Entry;
import jrm.profile.scan.DirScan.Options;
import jrm.ui.progress.ProgressHandler;

public class Dir2Dat
{
	public Dir2Dat(File srcdir, File dstdat, final ProgressHandler progress, EnumSet<Options> options)
	{
		DirScan srcdir_scan  = new DirScan(srcdir, progress, options);
		for(Container c : srcdir_scan.getContainersIterable())
		{
			System.out.println(c);
			for(Entry e : c.getEntries())
			{
				System.out.println("\t"+e.crc+"\t"+e.md5+"\t"+e.sha1+"\t"+e.size+"\t"+e.getName());
			}
		}	
	}

}
