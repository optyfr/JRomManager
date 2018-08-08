/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.ui.basic;

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
import java.util.stream.Collectors;

import javax.swing.JTextField;
import javax.swing.text.Document;

@SuppressWarnings("serial")
public class JFileDropTextField extends JTextField implements FocusListener, DropTargetListener
{
	private final Color color;
	private final SetCallBack callback;
	private JFileDropMode mode = JFileDropMode.FILE;

	public interface SetCallBack
	{
		public void call(String txt);
	}

	public JFileDropTextField(final SetCallBack callback) throws HeadlessException
	{
		this(null, "", 0, callback); //$NON-NLS-1$
	}

	public JFileDropTextField(final String text, final SetCallBack callback) throws HeadlessException
	{
		this(null, text, 0, callback);
	}

	public JFileDropTextField(final int columns, final SetCallBack callback) throws HeadlessException
	{
		this(null, "", columns, callback); //$NON-NLS-1$
	}

	public JFileDropTextField(final String text, final int columns, final SetCallBack callback) throws HeadlessException
	{
		this(null, "", columns, callback); //$NON-NLS-1$
	}

	public JFileDropTextField(final Document doc, final String text, final int columns, final SetCallBack callback)
	{
		super(doc, text, columns);
		this.callback = callback;
		color = JFileDropTextField.this.getBackground();
		addFocusListener(this);
		new DropTarget(this, this);
	}

	public void setMode(JFileDropMode mode)
	{
		this.mode = mode;
	}

	@Override
	public void focusGained(final FocusEvent e)
	{
	}

	@Override
	public void focusLost(final FocusEvent e)
	{
		if (callback != null)
			callback.call(JFileDropTextField.this.getText());
	}

	@Override
	public void dragEnter(final DropTargetDragEvent dtde)
	{
		final Transferable transferable = dtde.getTransferable();
		if (JFileDropTextField.this.isEnabled() && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
			JFileDropTextField.this.setBackground(Color.decode("#DDFFDD")); //$NON-NLS-1$
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		}
		else
		{
			JFileDropTextField.this.setBackground(Color.decode("#FFDDDD")); //$NON-NLS-1$
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

			if (JFileDropTextField.this.isEnabled() && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				@SuppressWarnings("unchecked")
				final List<File> files = ((List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor)).stream().filter(f -> {
					if (mode == JFileDropMode.DIRECTORY && !f.isDirectory())
						return false;
					else if (mode == JFileDropMode.FILE && !f.isFile())
						return false;
					return true;
				}).collect(Collectors.toList());
				if (files.size() == 1)
				{
					JFileDropTextField.this.setText(files.get(0).getAbsolutePath());
					callback.call(JFileDropTextField.this.getText());
					dtde.getDropTargetContext().dropComplete(true);
				}
				else
					dtde.getDropTargetContext().dropComplete(false);
			}
			else
				dtde.rejectDrop();
		}
		catch (final UnsupportedFlavorException e)
		{
			dtde.rejectDrop();
		}
		catch (final Exception e)
		{
			dtde.rejectDrop();
		}
	}

}
