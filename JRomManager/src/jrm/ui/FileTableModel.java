package jrm.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.io.FilenameUtils;

import jrm.Messages;
import jrm.misc.HTMLRenderer;
import jrm.profile.Profile;
import jrm.profile.ProfileNFO;

@SuppressWarnings("serial")
public class FileTableModel extends AbstractTableModel implements HTMLRenderer
{
	public DirNode.Dir curr_dir = null;

	private String[] columns = new String[] { Messages.getString("FileTableModel.Profile"), "Version", "HaveSets", "HaveRoms", "HaveDisks", "Created", "Scanned", "Fixed" };
	private Class<?>[] columnsClass = new Class<?>[] { String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class };
	public int[] columnsWidths = new int[] { 100, 50, -12, -14, -9, -19, -19, -19 };
	private List<ProfileNFO> rows = new ArrayList<>();

	public FileTableModel(DirNode.Dir dir)
	{
		super();
		populate(dir);
	}

	public FileTableModel()
	{
		super();
	}

	public void populate()
	{
		populate(curr_dir);
	}

	public void populate(DirNode.Dir dir)
	{
		this.curr_dir = dir;
		rows.clear();
		if(dir != null && dir.getFile().exists())
		{
			Arrays.asList(dir.getFile().listFiles(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					File f = new File(dir, name);
					if(f.isFile())
						if(!Arrays.asList("cache", "properties", "nfo").contains(FilenameUtils.getExtension(name))) //$NON-NLS-1$ //$NON-NLS-2$
							return true;
					return false;
				}
			})).stream().map(f -> {
				return ProfileNFO.load(f);
			}).forEach(pnfo -> {
				rows.add(pnfo);
			});
		}
		fireTableDataChanged();
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return column == 0;
	}

	public File getFileAt(int row)
	{
		return new File(curr_dir.getFile(), getValueAt(row, 0).toString());
	}

	public ProfileNFO getNfoAt(int row)
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
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		ProfileNFO pnfo = rows.get(rowIndex);
		switch(columnIndex)
		{
			case 0: return pnfo.name;
			case 1: return toHTML(pnfo.version==null?toGray("???"):pnfo.version);
			case 2: return toHTML(pnfo.haveSets==null?(pnfo.totalSets==null?toGray("?/?"):String.format("%s/%d", toGray("?"), pnfo.totalSets)):String.format("%s/%d", pnfo.haveSets==0&&pnfo.totalSets>0?toRed("0"):(pnfo.haveSets.equals(pnfo.totalSets)?toGreen(pnfo.haveSets+""):toOrange(pnfo.haveSets+"")), pnfo.totalSets));
			case 3: return toHTML(pnfo.haveRoms==null?(pnfo.totalRoms==null?toGray("?/?"):String.format("%s/%d", toGray("?"), pnfo.totalRoms)):String.format("%s/%d", pnfo.haveRoms==0&&pnfo.totalRoms>0?toRed("0"):(pnfo.haveRoms.equals(pnfo.totalRoms)?toGreen(pnfo.haveRoms+""):toOrange(pnfo.haveRoms+"")), pnfo.totalRoms));
			case 4: return toHTML(pnfo.haveDisks==null?(pnfo.totalDisks==null?toGray("?/?"):String.format("%s/%d", toGray("?"), pnfo.totalDisks)):String.format("%s/%d", pnfo.haveDisks==0&&pnfo.totalDisks>0?toRed("0"):(pnfo.haveDisks.equals(pnfo.totalDisks)?toGreen(pnfo.haveDisks+""):toOrange(pnfo.haveDisks+"")), pnfo.totalDisks));
			case 5: return toHTML(pnfo.created==null?toGray("????-??-?? ??:??:??"):new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pnfo.created));
			case 6: return toHTML(pnfo.scanned==null?toGray("????-??-?? ??:??:??"):new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pnfo.scanned));
			case 7: return toHTML(pnfo.fixed==null?toGray("????-??-?? ??:??:??"):new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(pnfo.fixed));
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		if(columnIndex==0)
		{
			ProfileNFO pnfo = rows.get(rowIndex);
			Arrays.asList("", ".properties",".cache").forEach(ext -> { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				File oldfile = new File(curr_dir.getFile(), pnfo.name + ext);
				File newfile = new File(curr_dir.getFile(), aValue + ext);
				oldfile.renameTo(newfile);
			});
			File new_nfo_file = new File(curr_dir.getFile(), aValue.toString());
			if(Profile.curr_profile!=null && Profile.curr_profile.nfo.file.equals(pnfo.file))
				Profile.curr_profile.nfo.relocate(new_nfo_file);
			pnfo.relocate(new_nfo_file);
			fireTableCellUpdated(rowIndex, rowIndex);
		}
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return columnsClass[columnIndex];
	}

}
