package jrm.ui.controls;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.text.Document;

@SuppressWarnings("serial")
public class JFileDropTextField extends JTextField implements FocusListener, DropTargetListener
{
	private Color color;
	private SetCallBack callback;

	public interface SetCallBack
	{
		public void call(String txt);
	}

	public JFileDropTextField(SetCallBack callback) throws HeadlessException
	{
		this(null, "", 0, callback);
	}

	public JFileDropTextField(String text, SetCallBack callback) throws HeadlessException
	{
		this(null, text, 0, callback);
	}

	public JFileDropTextField(int columns, SetCallBack callback) throws HeadlessException
	{
		this(null, "", columns, callback);
	}

	public JFileDropTextField(String text, int columns, SetCallBack callback) throws HeadlessException
	{
		this(null, "", columns, callback);
	}

	public JFileDropTextField(Document doc, String text, int columns, SetCallBack callback)
	{
		super(doc, text, columns);
		this.callback = callback;
		this.color = JFileDropTextField.this.getBackground();
		this.addFocusListener(this);
		new DropTarget(this, this);
	}

	@Override
	public void focusGained(FocusEvent e)
	{
	}

	@Override
	public void focusLost(FocusEvent e)
	{
		if(callback != null)
			callback.call(JFileDropTextField.this.getText());
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde)
	{
		Transferable transferable = dtde.getTransferable();
		if(JFileDropTextField.this.isEnabled() && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			JFileDropTextField.this.setBackground(Color.decode("#DDFFDD"));
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		}
		else
		{
			JFileDropTextField.this.setBackground(Color.decode("#FFDDDD"));
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
		JFileDropTextField.this.setBackground(color);
	}

	@Override
	public void drop(DropTargetDropEvent dtde)
	{
		JFileDropTextField.this.setBackground(color);
		try
		{
			Transferable transferable = dtde.getTransferable();

			if(JFileDropTextField.this.isEnabled() && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
				if(files.size() == 1)
				{
					JFileDropTextField.this.setText(files.get(0).getAbsolutePath());
					callback.call(JFileDropTextField.this.getText());
				}
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

}
