package jrm.ui.controls;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;

@SuppressWarnings("serial")
public class JFileDropList extends JList<File> implements DropTargetListener
{
	private Color color;
	private AddDelCallBack addCallBack;

	public interface AddDelCallBack
	{
		public void call(List<File> files);
	}

	public JFileDropList(AddDelCallBack addCallBack)
	{
		super(new DefaultListModel<File>());
		this.color = getBackground();
		this.addCallBack = addCallBack;
		new DropTarget(this, this);
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde)
	{
		Transferable transferable = dtde.getTransferable();
		if(isEnabled() && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			setBackground(Color.decode("#DDFFDD"));
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		}
		else
		{
			setBackground(Color.decode("#FFDDDD"));
			dtde.rejectDrag();
		}
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde)
	{
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde)
	{
	}

	@Override
	public void dragExit(DropTargetEvent dte)
	{
		setBackground(color);
	}

	@Override
	public void drop(DropTargetDropEvent dtde)
	{
		setBackground(color);
		try
		{
			Transferable transferable = dtde.getTransferable();

			if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);
				add(files);
				dtde.getDropTargetContext().dropComplete(true);
			}
			else
				dtde.rejectDrop();
		}
		catch(UnsupportedFlavorException e)
		{
			dtde.rejectDrop();
		}
		catch(Exception e)
		{
			dtde.rejectDrop();
		}
	}

	public void add(List<File> files)
	{
		for(File file : files)
			getModel().addElement(file);
		addCallBack.call(Collections.list(getModel().elements()));
	}
	
	public void add(File[] files)
	{
		for(File file : files)
			getModel().addElement(file);
		addCallBack.call(Collections.list(getModel().elements()));
	}
	
	public void del(List<File> files)
	{
		for(File file : files)
			getModel().removeElement(file);
		addCallBack.call(Collections.list(getModel().elements()));
	}
	
	public void del(File[] files)
	{
		for(File file : files)
			getModel().removeElement(file);
		addCallBack.call(Collections.list(getModel().elements()));
	}
	
	@Override
	public DefaultListModel<File> getModel()
	{
		return (DefaultListModel<File>) super.getModel();
	}
}
