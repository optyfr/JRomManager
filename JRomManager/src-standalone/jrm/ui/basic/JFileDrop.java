package jrm.ui.basic;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

interface JFileDrop extends DropTargetListener 
{
	JFileDropMode getMode();
	FilenameFilter getFilter();
	
	@Override
	default void dragOver(final DropTargetDragEvent dtde)
	{
		// do nothing
	}

	@Override
	default void dropActionChanged(final DropTargetDragEvent dtde)
	{
		// do nothing
	}


	/**
	 * @param transferable
	 * @return
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	default List<File> getTransferData(final Transferable transferable) throws UnsupportedFlavorException, IOException
	{
		return ((List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor)).stream().filter(f -> {
			if (getMode() == JFileDropMode.DIRECTORY && !f.isDirectory())
				return false;
			else if (getMode() == JFileDropMode.FILE && !f.isFile())
				return false;
			else if(getFilter() != null)
				return getFilter().accept(f.getParentFile(), f.getName());
			return true;
		}).collect(Collectors.toList());
	}

}
