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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 * The Class JFileDropList.
 */
@SuppressWarnings("serial")
public class JFileDropList extends JList<File> implements JFileDrop
{
	
	/** The color. */
	private final Color color;
	
	/** The add call back. */
	private final transient AddDelCallBack addCallBack;
	
	/** The mode. */
	private JFileDropMode mode = JFileDropMode.FILE;
	
	/** The filter */
	private transient FilenameFilter filter = null;

	/**
	 * The Interface AddDelCallBack.
	 */
	@FunctionalInterface
	public interface AddDelCallBack
	{
		
		/**
		 * Call.
		 *
		 * @param files the files
		 */
		public void call(List<File> files);
	}

	/**
	 * Instantiates a new j file drop list.
	 *
	 * @param addCallBack the add call back
	 */
	public JFileDropList(final AddDelCallBack addCallBack)
	{
		super(new DefaultListModel<>());
		color = getBackground();
		this.addCallBack = addCallBack;
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
	public void dragEnter(final DropTargetDragEvent dtde)
	{
		final Transferable transferable = dtde.getTransferable();
		if (isEnabled() && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
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

	@SuppressWarnings("exports")
	@Override
	public void dragExit(final DropTargetEvent dte)
	{
		setBackground(color);
	}

	@SuppressWarnings("exports")
	@Override
	public void drop(final DropTargetDropEvent dtde)
	{
		setBackground(color);
		drop(dtde, this::add);
	}

	@Override
	public boolean isFlavorSupported(Transferable transferable)
	{
		return transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}
	
	@Override
	public boolean checkValid(List<File> files)
	{
		return !files.isEmpty();
	}
	
	/**
	 * Adds the.
	 *
	 * @param files the files
	 */
	public void add(final List<File> files)
	{
		for (final File file : files)
			getModel().addElement(file);
		addCallBack.call(Collections.list(getModel().elements()));
	}

	/**
	 * Adds the.
	 *
	 * @param files the files
	 */
	public void add(final File[] files)
	{
		for (final File file : files)
			getModel().addElement(file);
		addCallBack.call(Collections.list(getModel().elements()));
	}

	/**
	 * Del.
	 *
	 * @param files the files
	 */
	public void del(final List<File> files)
	{
		for (final File file : files)
			getModel().removeElement(file);
		addCallBack.call(Collections.list(getModel().elements()));
	}

	/**
	 * Del.
	 *
	 * @param files the files
	 */
	public void del(final File[] files)
	{
		for (final File file : files)
			getModel().removeElement(file);
		addCallBack.call(Collections.list(getModel().elements()));
	}

	@Override
	public DefaultListModel<File> getModel()
	{
		return (DefaultListModel<File>) super.getModel();
	}

	@Override
	public JFileDropMode getMode()
	{
		return mode;
	}

	@Override
	public FilenameFilter getFilter()
	{
		return filter;
	}
}
