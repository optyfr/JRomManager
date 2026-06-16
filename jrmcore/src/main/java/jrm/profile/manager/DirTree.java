package jrm.profile.manager;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

import jrm.misc.Tree;

/**
 * Models a tree structure of filesystem directories. Extends the generic {@link Tree} container with {@link Dir} elements to allow
 * hierarchical traversal and rendering of physical or virtual folder structures.
 * 
 * @author optyfr
 */
public class DirTree extends Tree<Dir> {
    /**
     * Constructs a new directory tree with a specified root directory node without performing recursive scanning.
     * 
     * @param rootData the directory instance serving as the root node
     */
    public DirTree(Dir rootData) {
        super(rootData);
    }

    /**
     * Constructs a new directory tree starting from a physical filesystem root folder. This triggers a recursive filesystem
     * discovery to build the complete node hierarchy.
     * 
     * @param root the physical root folder on disk
     */
    public DirTree(final File root) {
        super(new Dir(root, "/")); //$NON-NLS-1$
        buildDirTree(getRoot());
    }

    /**
     * Recursively scans the filesystem for directories starting from the given node, attaching any discovered subdirectories as
     * child nodes in the tree structure.
     * 
     * @param node the tree node containing the directory to explore
     */
    private void buildDirTree(Node<Dir> node) {
        Optional.ofNullable(node.getData()).ifPresent(
                data -> Optional.ofNullable(data.getFile()).filter(File::isDirectory).ifPresent(dirfile -> Optional.ofNullable(dirfile.listFiles()).ifPresent(listFiles -> Stream
                        .of(listFiles).forEach(file -> Optional.ofNullable(file).filter(File::isDirectory).ifPresent(f -> buildDirTree(node.addChild(new Dir(f))))))));
    }
}
