package jrm.ui.controls;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;

@SuppressWarnings("serial")
public class JFileDropList extends JList<File> implements DropTargetListener
{
	private final Color color;
	private final AddDelCallBack addCallBack;

	public interface AddDelCallBack
	{
		public void call(List<File> files);
	}

	public JFileDropList(final AddDelCallBack addCallBack)
	{
		super(new DefaultListModel<File>());
		color = getBackground();
		this.addCallBack = addCallBack;
		new DropTarget(this, this);
	}

	@Override
	public void dragEnter(final DropTargetDragEvent dtde)
	{
		final Transferable transferable = dtde.getTransferable();
		if(isEnabled() && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			setBackground(Color.decode("#DDFFDD")); //$NON-NLS-1$
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		}
		else
		{
			setBackground(Color.decode("#FFDDDD")); //$NON-NLS-1$
			dtde.rejectDrag();
		}
	}

	@Override
	public void dragOver(final DropTargetDragEvent dtde)
	{
	}

	@Override
	public void dropActionChanged(final DropTargetDragEvent dtde)
	{
	}

	@Override
	public void dragExit(final DropTargetEvent dte)
	{
		setBackground(color);
	}

	@Override
	public void drop(final DropTargetDropEvent dtde)
	{
		setBackground(color);
		try
		{
			final Transferable transferable = dtde.getTransferable();

			if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				@SuppressWarnings("unchecked")
				final
				List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
				add(files);
				dtde.getDropTargetContext().dropComplete(true);
			}
			else
				dtde.rejectDrop();
		}
		catch(final UnsupportedFlavorException e)
		{
			dtde.rejectDrop();
		}
		catch(final Exception e)
		{
			dtde.rejectDrop();
		}
	}

	public void add(final List<File> files)
	{
		for(final File file : files)
			getModel().addElement(file);
		addCallBack.call(Collections.list(getModel().elements()));
	}

	public void add(final File[] files)
	{
		for(final File file : files)
			getModel().addElement(file);
		addCallBack.call(Collections.list(getModel().elements()));
	}

	public void del(final List<File> files)
	{
		for(final File file : files)
			getModel().removeElement(file);
		addCallBack.call(Collections.list(getModel().elements()));
	}

	public void del(final File[] files)
	{
		for(final File file : files)
			getModel().removeElement(file);
		addCallBack.call(Collections.list(getModel().elements()));
	}

	@Override
	public DefaultListModel<File> getModel()
	{
		return (DefaultListModel<File>) super.getModel();
	}
}
