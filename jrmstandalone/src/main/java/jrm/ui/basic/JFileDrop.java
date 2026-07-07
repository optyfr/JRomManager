package jrm.ui.basic;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import jrm.misc.Log;

/**
 * Internal interface defining the common drop target behavior for file drag-and-drop components.
 * <p>
 * This interface extends {@link DropTargetListener} and provides shared logic for extracting,
 * filtering, and validating files dropped onto Swing components. It supports configurable
 * drop modes and filename filters.
 * </p>
 *
 * @see JFileDropList
 * @see JFileDropTextField
 * @see JFileDropMode
 */
interface JFileDrop extends DropTargetListener {
    /**
     * Returns the current drop mode controlling which file types are accepted.
     *
     * @return the {@link JFileDropMode}
     */
    JFileDropMode getMode();

    /**
     * Returns the filename filter applied to dropped files.
     *
     * @return the {@link FilenameFilter}, or {@code null} if no filter is set
     */
    FilenameFilter getFilter();

    @Override
    default void dragOver(final DropTargetDragEvent dtde) {
        // do nothing
    }

    @Override
    default void dropActionChanged(final DropTargetDragEvent dtde) {
        // do nothing
    }

    /**
     * Extracts and filters files from the given transferable based on the current mode and filter.
     *
     * @param transferable the {@link Transferable} containing the dropped data
     * @return a filtered list of {@link File} objects
     * @throws UnsupportedFlavorException if the data flavor is not supported
     * @throws IOException if an I/O error occurs during data extraction
     */
    @SuppressWarnings("unchecked")
    default List<File> getTransferData(final Transferable transferable) throws UnsupportedFlavorException, IOException {
        return ((List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor)).stream().filter(f -> {
            if (getMode() == JFileDropMode.DIRECTORY && !f.isDirectory())
                return false;
            else if (getMode() == JFileDropMode.FILE && !f.isFile())
                return false;
            else if (getFilter() != null)
                return getFilter().accept(f.getParentFile(), f.getName());
            return true;
        }).toList();
    }

    /**
     * Validates whether the given list of files is acceptable for this drop target.
     *
     * @param files the list of {@link File} objects to validate
     * @return {@code true} if the files are valid for this drop target, {@code false} otherwise
     */
    public boolean checkValid(final List<File> files);

    /**
     * Checks whether the transferable contains a supported data flavor.
     *
     * @param transferable the {@link Transferable} to check
     * @return {@code true} if the data flavor is supported, {@code false} otherwise
     */
    public boolean isFlavorSupported(final Transferable transferable);

    /**
     * Callback interface for processing files after a successful drop operation.
     */
    @FunctionalInterface
    interface CallBack {
        /**
         * Processes the list of files received from a drop operation.
         *
         * @param files the list of dropped {@link File} objects
         */
        void apply(List<File> files);
    }

    /**
     * Handles a drop event by extracting files, validating them, and invoking the callback.
     * <p>
     * If the transferable is supported and the files pass validation, the callback is invoked
     * and the drop is completed successfully. Otherwise, the drop is rejected.
     * </p>
     *
     * @param dtde the {@link DropTargetDropEvent}
     * @param cb the {@link CallBack} to invoke with the accepted files
     */
    default void drop(final DropTargetDropEvent dtde, final CallBack cb) {
        try {
            final var transferable = dtde.getTransferable();

            if (isFlavorSupported(transferable)) {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                final var files = getTransferData(transferable);
                if (checkValid(files)) {
                    cb.apply(files);
                    dtde.getDropTargetContext().dropComplete(true);
                } else
                    dtde.getDropTargetContext().dropComplete(false);
            } else
                dtde.rejectDrop();
        } catch (final UnsupportedFlavorException e) {
            Log.warn(e.getMessage());
            dtde.rejectDrop();
        } catch (final Exception e) {
            Log.err(e.getMessage(), e);
            dtde.rejectDrop();
        }
    }
}
