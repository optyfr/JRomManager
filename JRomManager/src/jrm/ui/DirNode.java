/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.ui;

import java.io.File;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class DirNode extends DefaultMutableTreeNode
{
	public Dir dir;

	public DirNode(final File root)
	{
		super(new Dir(root, "/"), true); //$NON-NLS-1$
		buildDirTree(dir = (Dir) getUserObject(), this);
	}

	public DirNode(final Dir dir)
	{
		super(dir);
		this.dir = dir;
	}

	private void buildDirTree(final Dir dir, final DefaultMutableTreeNode node)
	{
		for(final File file : dir.file.listFiles())
		{
			if(file.isDirectory())
			{
				final DefaultMutableTreeNode newdir = new DirNode(new Dir(file));
				node.add(newdir);
				buildDirTree(new Dir(file), newdir);
			}

		}
	}

	public void reload()
	{
		removeAllChildren();
		buildDirTree(dir, this);
	}

	public DirNode find(final File file)
	{
		return DirNode.find(this, file);
	}

	public static DirNode find(final DirNode root, final File file)
	{
		final File parent = file.isFile()?file.getParentFile():file;
		if(parent != null)
		{
			for (final Enumeration<?> e = root.depthFirstEnumeration(); e.hasMoreElements();)
			{
				final DirNode node = (DirNode) e.nextElement();
				if (((DirNode.Dir)node.getUserObject()).getFile().equals(parent))
					return node;
			}
			return DirNode.find(root, parent.getParentFile());
		}
		return null;
	}

	public static class Dir
	{
		private final File file;
		private final String name;

		public Dir(final File file)
		{
			this.file = file;
			name = file.getName();
			if(!this.file.exists())
				this.file.mkdirs();
		}

		public Dir(final File file, final String name)
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
