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
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JTextField;
import javax.swing.text.Document;

import jrm.misc.Log;

/**
 * The Class JFileDropTextField.
 */
@SuppressWarnings("serial")
public class JFileDropTextField extends JTextField implements FocusListener, DropTargetListener
{
	
	/** The color. */
	private final Color color;
	
	/** The callback. */
	private final transient SetCallBack callback;
	
	/** The mode. */
	private JFileDropMode mode = JFileDropMode.FILE;
	
	/** The filter */
	private transient FilenameFilter filter = null;

	/**
	 * The Interface SetCallBack.
	 */
	@FunctionalInterface
	public interface SetCallBack
	{
		
		/**
		 * Call.
		 *
		 * @param txt the txt
		 */
		public void call(String txt);
	}

	/**
	 * Instantiates a new j file drop text field.
	 *
	 * @param callback the callback
	 * @throws HeadlessException the headless exception
	 */
	public JFileDropTextField(final SetCallBack callback) throws HeadlessException
	{
		this(null, "", 0, callback); //$NON-NLS-1$
	}

	/**
	 * Instantiates a new j file drop text field.
	 *
	 * @param text the text
	 * @param callback the callback
	 * @throws HeadlessException the headless exception
	 */
	public JFileDropTextField(final String text, final SetCallBack callback) throws HeadlessException
	{
		this(null, text, 0, callback);
	}

	/**
	 * Instantiates a new j file drop text field.
	 *
	 * @param columns the columns
	 * @param callback the callback
	 * @throws HeadlessException the headless exception
	 */
	public JFileDropTextField(final int columns, final SetCallBack callback) throws HeadlessException
	{
		this(null, "", columns, callback); //$NON-NLS-1$
	}

	/**
	 * Instantiates a new j file drop text field.
	 *
	 * @param text the text
	 * @param columns the columns
	 * @param callback the callback
	 * @throws HeadlessException the headless exception
	 */
	public JFileDropTextField(final String text, final int columns, final SetCallBack callback) throws HeadlessException
	{
		this(null, "", columns, callback); //$NON-NLS-1$
	}

	/**
	 * Instantiates a new j file drop text field.
	 *
	 * @param doc the doc
	 * @param text the text
	 * @param columns the columns
	 * @param callback the callback
	 */
	@SuppressWarnings("exports")
	public JFileDropTextField(final Document doc, final String text, final int columns, final SetCallBack callback)
	{
		super(doc, text, columns);
		this.callback = callback;
		color = JFileDropTextField.this.getBackground();
		addFocusListener(this);
		new DropTarget(this, this);
	}

	/**
	 * Sets the mode.
	 *
	 * @param mode the new mode
	 */
	public void setMode(JFileDropMode mode)
	{
		this.mode = mode;
	}

	public void setFilter(FilenameFilter filter)
	{
		this.filter = filter;
	}

	@SuppressWarnings("exports")
	@Override
	public void focusGained(final FocusEvent e)
	{
		// do nothing
	}

	@SuppressWarnings("exports")
	@Override
	public void focusLost(final FocusEvent e)
	{
		if (callback != null)
			callback.call(JFileDropTextField.this.getText());
	}

	@SuppressWarnings("exports")
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

	@SuppressWarnings("exports")
	@Override
	public void dragOver(final DropTargetDragEvent dtde)
	{
		// do nothing
	}

	@SuppressWarnings("exports")
	@Override
	public void dropActionChanged(final DropTargetDragEvent dtde)
	{
		// do nothing
	}

	@SuppressWarnings("exports")
	@Override
	public void dragExit(final DropTargetEvent dte)
	{
		JFileDropTextField.this.setBackground(color);
	}

	@SuppressWarnings("exports")
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
					else if(filter != null)
						return filter.accept(f.getParentFile(), f.getName());
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
			Log.warn(e.getMessage());
			dtde.rejectDrop();
		}
		catch (final Exception e)
		{
			Log.err(e.getMessage(), e);
			dtde.rejectDrop();
		}
	}

}
