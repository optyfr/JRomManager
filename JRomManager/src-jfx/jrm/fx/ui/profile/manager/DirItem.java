package jrm.fx.ui.profile.manager;

import java.io.File;

import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import jrm.fx.ui.MainFrame;
import jrm.profile.manager.Dir;


public class DirItem extends TreeItem<Dir>
{

	public DirItem(File file)
	{
		super(new Dir(file, "/"));
		setExpanded(true);
		setGraphic(new ImageView((MainFrame.getIcon("/jrm/resicons/folder_open.png"))));
		buildDirTree(getValue(), this);
	}

	private DirItem(Dir dir)
	{
		super(dir);
		setGraphic(new ImageView((MainFrame.getIcon("/jrm/resicons/folder_open.png"))));
	}
	
	/**
	 * Builds the dir tree.
	 *
	 * @param dir
	 *            the dir
	 * @param node
	 *            the node
	 */
	private void buildDirTree(final Dir dir, final DirItem node)
	{
		if (dir == null)
			return;
		File dirfile = dir.getFile();
		if (dirfile != null && dirfile.isDirectory())
		{
			File[] listFiles = dirfile.listFiles();
			if (listFiles != null)
			{
				for (final File file : listFiles)
				{
					if (file != null && file.isDirectory())
					{
						final var newdir = new DirItem(new Dir(file));
						node.getChildren().add(newdir);
						buildDirTree(new Dir(file), newdir);
					}

				}
			}
		}
	}

	/**
	 * Reload.
	 */
	public void reload()
	{
		getChildren().clear();
		buildDirTree(getValue(), this);
		if(!isLeaf())
			setExpanded(true);
	}
	
}
