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
	private final Color color;
	private final SetCallBack callback;

	public interface SetCallBack
	{
		public void call(String txt);
	}

	public JFileDropTextField(final SetCallBack callback) throws HeadlessException
	{
		this(null, "", 0, callback);
	}

	public JFileDropTextField(final String text, final SetCallBack callback) throws HeadlessException
	{
		this(null, text, 0, callback);
	}

	public JFileDropTextField(final int columns, final SetCallBack callback) throws HeadlessException
	{
		this(null, "", columns, callback);
	}

	public JFileDropTextField(final String text, final int columns, final SetCallBack callback) throws HeadlessException
	{
		this(null, "", columns, callback);
	}

	public JFileDropTextField(final Document doc, final String text, final int columns, final SetCallBack callback)
	{
		super(doc, text, columns);
		this.callback = callback;
		color = JFileDropTextField.this.getBackground();
		addFocusListener(this);
		new DropTarget(this, this);
	}

	@Override
	public void focusGained(final FocusEvent e)
	{
	}

	@Override
	public void focusLost(final FocusEvent e)
	{
		if(callback != null)
			callback.call(JFileDropTextField.this.getText());
	}

	@Override
	public void dragEnter(final DropTargetDragEvent dtde)
	{
		final Transferable transferable = dtde.getTransferable();
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
		JFileDropTextField.this.setBackground(color);
	}

	@Override
	public void drop(final DropTargetDropEvent dtde)
	{
		JFileDropTextField.this.setBackground(color);
		try
		{
			final Transferable transferable = dtde.getTransferable();

			if(JFileDropTextField.this.isEnabled() && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				@SuppressWarnings("unchecked")
				final
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
		catch(final UnsupportedFlavorException e)
		{
			dtde.rejectDrop();
		}
		catch(final Exception e)
		{
			dtde.rejectDrop();
		}
	}

}
