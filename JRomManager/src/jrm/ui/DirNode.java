package jrm.ui;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class DirNode extends DefaultMutableTreeNode
{
	public Dir dir;

	public DirNode(File root)
	{
		super(new Dir(root, "/"), true);
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

	public static class Dir
	{
		private File file;
		private String name;

		public Dir(File file)
		{
			this.file = file;
			this.name = file.getName();
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
