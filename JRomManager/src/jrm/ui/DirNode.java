package jrm.ui;

import java.io.File;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class DirNode extends DefaultMutableTreeNode
{
	public Dir dir;

	public DirNode(File root)
	{
		super(new Dir(root, "/"), true); //$NON-NLS-1$
		buildDirTree(this.dir = (Dir) getUserObject(), this);
	}

	public DirNode(Dir dir)
	{
		super(dir);
		this.dir = dir;
	}

	private void buildDirTree(Dir dir, DefaultMutableTreeNode node)
	{
		for(File file : dir.file.listFiles())
		{
			if(file.isDirectory())
			{
				DefaultMutableTreeNode newdir = new DirNode(new Dir(file));
				node.add(newdir);
				buildDirTree(new Dir(file), newdir);
			}

		}
	}
	
	public void reload()
	{
		this.removeAllChildren();
		buildDirTree(dir, this);
	}

	public DirNode find(File file)
	{
		return find(this, file);
	}

	public static DirNode find(DirNode root, File file)
	{
		File parent = file.isFile()?file.getParentFile():file;
		if(parent != null)
		{
			for (Enumeration<?> e = root.depthFirstEnumeration(); e.hasMoreElements();)
			{
				DirNode node = (DirNode) e.nextElement();
			    if (((DirNode.Dir)node.getUserObject()).getFile().equals(parent))
			        return node;
			}
			return find(root, parent.getParentFile());
		}
		return null;
	}
	
	public static class Dir
	{
		private File file;
		private String name;

		public Dir(File file)
		{
			this.file = file;
			this.name = file.getName();
			if(!this.file.exists())
				this.file.mkdirs();
		}

		public Dir(File file, String name)
		{
			this.file = file;
			this.name = name;
		}

		public File getFile()
		{
			return file;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
