package jrm.profile.manager;

import java.io.File;
import jrm.misc.Tree;

public class DirTree extends Tree<Dir>
{
	public DirTree(Dir rootData)
	{
		super(rootData);
	}

	public DirTree(final File root)
	{
		super(new Dir(root, "/")); //$NON-NLS-1$
		buildDirTree(getRoot());
	}

	private void buildDirTree(Node<Dir> node)
	{
		if (node.getData() != null)
		{
			File dirfile = node.getData().getFile();
			if (dirfile != null && dirfile.isDirectory())
			{
				File[] listFiles = dirfile.listFiles();
				if (listFiles != null)
				{
					for (final File file : listFiles)
						if (file != null && file.isDirectory())
							buildDirTree(node.addChild(new Dir(file)));
				}
			}
		}
	}
}
