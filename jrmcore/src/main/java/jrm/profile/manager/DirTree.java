package jrm.profile.manager;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

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
		Optional.ofNullable(node.getData()).ifPresent(data -> 
			Optional.ofNullable(data.getFile()).filter(File::isDirectory).ifPresent(dirfile -> 
				Optional.ofNullable(dirfile.listFiles()).ifPresent(listFiles -> 
					Stream.of(listFiles).forEach(file -> 
						Optional.ofNullable(file).filter(File::isDirectory).ifPresent(f -> 
							buildDirTree(node.addChild(new Dir(f)))
						)
					)
				)
			)
		);
	}
}
