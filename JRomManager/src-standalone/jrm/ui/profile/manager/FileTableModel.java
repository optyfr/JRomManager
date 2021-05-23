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
package jrm.ui.profile.manager;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import jrm.locale.Messages;
import jrm.misc.HTMLRenderer;
import jrm.profile.manager.Dir;
import jrm.profile.manager.ProfileNFO;
import jrm.security.Session;

/**
 * The Class FileTableModel.
 *
 * @author optyfr
 */
// TODO: Auto-generated Javadoc
@SuppressWarnings("serial")
public class FileTableModel extends AbstractTableModel implements HTMLRenderer
{
	
	/** The curr dir. */
	public Dir curr_dir = null;

	/** The columns. */
	private final String[] columns = new String[] { Messages.getString("FileTableModel.Profile"), Messages.getString("FileTableModel.Version"), Messages.getString("FileTableModel.HaveSets"), Messages.getString("FileTableModel.HaveRoms"), Messages.getString("FileTableModel.HaveDisks"), Messages.getString("FileTableModel.Created"), Messages.getString("FileTableModel.Scanned"), Messages.getString("FileTableModel.Fixed") };  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	
	/** The columns class. */
	private final Class<?>[] columnsClass = new Class<?>[] { Object.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class };
	
	/** The columns widths. */
	public int[] columnsWidths = new int[] { 100, 50, -14, -14, -9, -19, -19, -19 };
	
	/** The rows. */
	private List<ProfileNFO> rows;
	
	private Session session;

	/**
	 * Instantiates a new file table model.
	 *
	 * @param dir the dir
	 */
	public FileTableModel(final Session session, final Dir dir)
	{
		super();
		populate(session, dir);
	}

	/**
	 * Instantiates a new file table model.
	 */
	public FileTableModel()
	{
		super();
	}

	/**
	 * Populate.
	 */
	public void populate(final Session session)
	{
		populate(session, curr_dir);
	}

	/**
	 * Populate.
	 *
	 * @param dir the dir
	 */
	public void populate(final Session session, final Dir dir)
	{
		this.session = session;
		curr_dir = dir;
		rows = ProfileNFO.list(session, dir.getFile());
		fireTableDataChanged();
	}

	@Override
	public boolean isCellEditable(final int row, final int column)
	{
		return column == 0;
	}

	/**
	 * Gets the file at.
	 *
	 * @param row the row
	 * @return the file at
	 */
	public File getFileAt(final int row)
	{
		return getNfoAt(row).file;
	}

	/**
	 * Gets the nfo at.
	 *
	 * @param row the row
	 * @return the nfo at
	 */
	public ProfileNFO getNfoAt(final int row)
	{
		return rows.get(row);
	}

	@Override
	public int getRowCount()
	{
		return rows.size();
	}

	@Override
	public int getColumnCount()
	{
		return columns.length;
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex)
	{
		return getValueAt(rows.get(rowIndex), columnIndex);
	}

	public Object getValueAt(ProfileNFO pnfo, final int columnIndex)
	{
		return getValueAt_(pnfo, columnIndex);
	}
	
	public static Object getValueAt_(ProfileNFO pnfo, final int columnIndex)
	{
		switch(columnIndex)
		{
			case 0:
				return pnfo.getName();
			case 1:
				return pnfo.getVersion();
			case 2:
				return pnfo.getHaveSets(); 
			case 3:
				return pnfo.getHaveRoms(); 
			case 4:
				return pnfo.getHaveDisks(); 
			case 5:
				return pnfo.getCreated();
			case 6:
				return pnfo.getScanned();
			case 7:
				return pnfo.getFixed();
		}
		return null;
	}
	
	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
	{
		if(columnIndex == 0)
		{
			final ProfileNFO pnfo = rows.get(rowIndex);
			Arrays.asList("", ".properties", ".cache").forEach(ext -> { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				final File oldfile = new File(curr_dir.getFile(), pnfo.name + ext);
				final File newfile = new File(curr_dir.getFile(), aValue + ext);
				oldfile.renameTo(newfile);
			});
			final File new_nfo_file = new File(curr_dir.getFile(), aValue.toString());
			if(session.curr_profile != null && session.curr_profile.getNfo().file.equals(pnfo.file))
				session.curr_profile.getNfo().relocate(session, new_nfo_file);
			pnfo.relocate(session, new_nfo_file);
			fireTableCellUpdated(rowIndex, rowIndex);
		}
	}

	@Override
	public String getColumnName(final int columnIndex)
	{
		return columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex)
	{
		return columnsClass[columnIndex];
	}

}
