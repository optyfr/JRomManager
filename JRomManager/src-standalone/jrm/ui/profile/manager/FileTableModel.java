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
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.io.FilenameUtils;

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
	private final List<ProfileNFO> rows = new ArrayList<>();
	
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
		rows.clear();
		if(dir != null && dir.getFile().exists())
		{
			File filedir = dir.getFile();
			if(filedir!=null)
			{
				File[] files = filedir.listFiles((FilenameFilter) (dir1, name) -> {
					final File f = new File(dir1, name);
					if(f.isFile())
						if(!Arrays.asList("cache", "properties", "nfo", "jrm1", "jrm2").contains(FilenameUtils.getExtension(name))) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
							return true;
					return false;
				});
				if(files!=null)
				{
					Arrays.asList(files).stream().map(f -> {
						return ProfileNFO.load(session, f);
					}).forEach(pnfo -> {
						rows.add(pnfo);
					});
				}
			}
		}
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
		switch(columnIndex)
		{
			case 0:
				return pnfo.name;
			case 1:
				return toHTML(pnfo.stats.version == null ? toGray("???") : toNoBR(pnfo.stats.version)); //$NON-NLS-1$
			case 2:
				return toHTML(pnfo.stats.haveSets == null ? (pnfo.stats.totalSets == null ? toGray("?/?") : String.format("%s/%d", toGray("?"), pnfo.stats.totalSets)) : String.format("%s/%d", pnfo.stats.haveSets == 0 && pnfo.stats.totalSets > 0 ? toRed("0") : (pnfo.stats.haveSets.equals(pnfo.stats.totalSets) ? toGreen(pnfo.stats.haveSets + "") : toOrange(pnfo.stats.haveSets + "")), pnfo.stats.totalSets)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			case 3:
				return toHTML(pnfo.stats.haveRoms == null ? (pnfo.stats.totalRoms == null ? toGray("?/?") : String.format("%s/%d", toGray("?"), pnfo.stats.totalRoms)) : String.format("%s/%d", pnfo.stats.haveRoms == 0 && pnfo.stats.totalRoms > 0 ? toRed("0") : (pnfo.stats.haveRoms.equals(pnfo.stats.totalRoms) ? toGreen(pnfo.stats.haveRoms + "") : toOrange(pnfo.stats.haveRoms + "")), pnfo.stats.totalRoms)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			case 4:
				return toHTML(pnfo.stats.haveDisks == null ? (pnfo.stats.totalDisks == null ? toGray("?/?") : String.format("%s/%d", toGray("?"), pnfo.stats.totalDisks)) : String.format("%s/%d", pnfo.stats.haveDisks == 0 && pnfo.stats.totalDisks > 0 ? toRed("0") : (pnfo.stats.haveDisks.equals(pnfo.stats.totalDisks) ? toGreen(pnfo.stats.haveDisks + "") : toOrange(pnfo.stats.haveDisks + "")), pnfo.stats.totalDisks)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			case 5:
				return toHTML(pnfo.stats.created == null ? toGray("????-??-?? ??:??:??") : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pnfo.stats.created)); //$NON-NLS-1$ //$NON-NLS-2$
			case 6:
				return toHTML(pnfo.stats.scanned == null ? toGray("????-??-?? ??:??:??") : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pnfo.stats.scanned)); //$NON-NLS-1$ //$NON-NLS-2$
			case 7:
				return toHTML(pnfo.stats.fixed == null ? toGray("????-??-?? ??:??:??") : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pnfo.stats.fixed)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}
	
	private static FileTableModel static_ref = new FileTableModel();
	
	public static Object getValueAt_(ProfileNFO pnfo, final int columnIndex)
	{
		return static_ref.getValueAt(pnfo, columnIndex);
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
			if(session.curr_profile != null && session.curr_profile.nfo.file.equals(pnfo.file))
				session.curr_profile.nfo.relocate(session, new_nfo_file);
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
