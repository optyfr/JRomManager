package jrm.fx.ui.profile.manager;

import java.io.File;

import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import jrm.fx.ui.MainFrame;
import jrm.profile.manager.Dir;

/**
 * A tree item representing a directory in the profile manager.
 * <p>
 * Displays a folder icon and recursively builds the directory tree structure.
 * Supports reloading the tree when the directory contents change.
 *
 * @since 2.5
 */
public class DirItem extends TreeItem<Dir> {

    /**
     * Constructs a directory item from a file.
     *
     * @param file the directory file
     */
    public DirItem(File file) {
        super(new Dir(file, "/"));
        setExpanded(true);
        ImageView i = new ImageView((MainFrame.getIcon("/jrm/resicons/folder_open.png")));
        i.setPreserveRatio(true);
        i.getStyleClass().add("icon");
        setGraphic(i);
        buildDirTree(getValue(), this);
    }

    /**
     * Constructs a directory item from a Dir.
     *
     * @param dir the directory
     */
    private DirItem(Dir dir) {
        super(dir);
        ImageView i = new ImageView((MainFrame.getIcon("/jrm/resicons/folder_open.png")));
        i.setPreserveRatio(true);
        i.getStyleClass().add("icon");
        setGraphic(i);
    }

    /**
     * Recursively builds the directory tree by iterating over the given directory's
     * subdirectories and adding child {@link DirItem} nodes.
     *
     * @param dir  the current directory node to explore
     * @param node the parent tree node to which children are added
     */
    private void buildDirTree(final Dir dir, final DirItem node) {
        if (dir == null)
            return;
        File dirfile = dir.getFile();
        if (dirfile != null && dirfile.isDirectory()) {
            File[] listFiles = dirfile.listFiles();
            if (listFiles != null) {
                for (final File file : listFiles) {
                    if (file != null && file.isDirectory()) {
                        final var newdir = new DirItem(new Dir(file));
                        node.getChildren().add(newdir);
                        buildDirTree(new Dir(file), newdir);
                    }

                }
            }
        }
    }

    /**
     * Clears all children and rebuilds the directory tree from the current node,
     * expanding the node if it is not a leaf.
     */
    public void reload() {
        getChildren().clear();
        buildDirTree(getValue(), this);
        if (!isLeaf())
            setExpanded(true);
    }

}
