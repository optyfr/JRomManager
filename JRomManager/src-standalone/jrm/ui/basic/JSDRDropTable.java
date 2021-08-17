package jrm.ui.basic;

import java.awt.Color;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;

import jrm.aui.basic.ResultColUpdater;
import jrm.aui.basic.SrcDstResult;
import jrm.aui.basic.SrcDstResult.SDRList;
import jrm.misc.Log;

@SuppressWarnings("serial")
public class JSDRDropTable extends JTable implements DropTargetListener, ResultColUpdater
{
	/** The color. */
	private final Color color;
	
	/**
	 * The model from {@link JTable#getModel()} to avoid cast to {@link SDRTableModel} each time
	 */
	private transient SDRTableModel model;
	
	/** The add call back. */
	private final transient AddDelCallBack addCallBack;
	
	/**
	 * The Interface AddDelCallBack.
	 */
	@FunctionalInterface
	public interface AddDelCallBack
	{
		public void call(@SuppressWarnings("exports") SDRList files);
	}

	public JSDRDropTable(SDRTableModel model, AddDelCallBack callback)
	{
		super(model);
		setCellSelectionEnabled(true);
		this.addCallBack = callback;
		this.model = model;
		for(int i = 0; i < getColumnModel().getColumnCount(); i++)
			getColumnModel().getColumn(i).setCellRenderer(model.getCellRenderers()[i]);
		for(int i = 0; i < getColumnModel().getColumnCount(); i++)
			getColumnModel().getColumn(i).setCellEditor(model.getCellEditors()[i]);
		color = getBackground();
		new DropTarget(this, this);
		this.model.addTableModelListener(e -> {
			if (e.getColumn() >= 0 && model.getColumnClass(e.getColumn()).equals(Boolean.class) && e.getType() == TableModelEvent.UPDATE)
				addCallBack.call(model.getData());
		});
	}
	
	@Override
	protected JTableHeader createDefaultTableHeader()
	{
		return new JTableHeader(columnModel) {
            @Override
			public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex = columnModel.getColumn(index).getModelIndex();
                return ((SDRTableModel)getModel()).getColumnTT(realIndex);
            }
        };
	}
	
	@Override
	public void dragEnter(@SuppressWarnings("exports") DropTargetDragEvent dtde)
	{
		// do nothing
	}

	@Override
	public void dragOver(@SuppressWarnings("exports") DropTargetDragEvent dtde)
	{
		final Transferable transferable = dtde.getTransferable();
		if (isEnabled() && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
            Point point = dtde.getLocation();
            int oldRow = model.getCurrentRow();
            int oldCol = model.getCurrentCol();
            int row = model.setCurrentRow(rowAtPoint(point));
            int col = model.setCurrentCol(columnAtPoint(point));
            if(oldCol != col || oldRow != row)
            {
	            if(row==-1)
	            {
	    			model.setCurrentRow(-1);
	            	setBackground(Color.decode("#DDFFDD")); //$NON-NLS-1$
	        		model.fireTableChanged(new TableModelEvent(model));
	            }
	            else
	            {
	            	setBackground(Color.white); //$NON-NLS-1$
	            	model.fireTableChanged(new TableModelEvent(model, row, row, col));
	            	if(oldRow != -1)
	            		model.fireTableChanged(new TableModelEvent(model, oldRow, oldRow, oldCol));
	            }
            }
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		}
		else
		{
			model.setCurrentRow(-1);
			setBackground(Color.decode("#FFDDDD")); //$NON-NLS-1$
    		model.fireTableChanged(new TableModelEvent(model));
			dtde.rejectDrag();
		}
	}

	@Override
	public void dropActionChanged(@SuppressWarnings("exports") DropTargetDragEvent dtde)
	{
		// do nothing
	}

	@Override
	public void dragExit(@SuppressWarnings("exports") DropTargetEvent dte)
	{
		model.setCurrentRow(-1);
		setBackground(color);
		model.fireTableChanged(new TableModelEvent(model));
	}

	@Override
	public void drop(@SuppressWarnings("exports") DropTargetDropEvent dtde)
	{
		model.setCurrentRow(-1);
		setBackground(color);
		model.fireTableChanged(new TableModelEvent(model));
		try
		{
			final Transferable transferable = dtde.getTransferable();

			if (!transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
				dtde.rejectDrop();
				return;
			}

			dtde.acceptDrop(DnDConstants.ACTION_COPY);

			Point point = dtde.getLocation();
			int row = rowAtPoint(point);
			int col = columnAtPoint(point);

			@SuppressWarnings("unchecked")
			final List<File> files = ((List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor)).stream().filter(f -> dropFilter(col, f)).collect(Collectors.toList());
			if (!files.isEmpty())
			{
				final var startSize = model.getData().size();
				for (int i = 0; i < files.size(); i++)
					addFile(files.get(i), row, col, i);
				if (row != -1)
					model.fireTableChanged(new TableModelEvent(model, row, startSize - 1, col));
				if (startSize != model.getData().size())
					model.fireTableChanged(new TableModelEvent(model, startSize, model.getData().size() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
				addCallBack.call(model.getData());
				dtde.getDropTargetContext().dropComplete(true);
			}
			else
				dtde.getDropTargetContext().dropComplete(false);
		}
		catch (final UnsupportedFlavorException e)
		{
			dtde.rejectDrop();
		}
		catch (final Exception e)
		{
			Log.err(e.getMessage(), e);
			dtde.rejectDrop();
		}
	}

	/**
	 * @param file
	 * @param row
	 * @param col
	 * @param i
	 */
	private void addFile(File file, int row, int col, int i)
	{
		SrcDstResult line;
		if (row == -1 || row + i >= model.getData().size())
		{
			line = new SrcDstResult();
			model.getData().add(line);
		}
		else
			line = model.getData().get(row + i);
		if (col == 1)
			line.dst = file.getPath();
		else
			line.src = file.getPath();
	}

	/**
	 * @param col
	 * @param f
	 * @return
	 */
	private boolean dropFilter(int col, File f)
	{
		FileFilter filter = null;
		if(col==1)
			filter = model.getDstFilter();
		else if(col==0)
			filter = model.getSrcFilter();
		if(filter!=null)
			return filter.accept(f);
		return true;
	}
	
	public SDRTableModel getSDRModel()
	{
		return model;
	}

	/* (non-Javadoc)
	 * @see jrm.ui.basic.ResultColUpdater#updateResult(int, java.lang.String)
	 */
	@Override
	public void updateResult(int row, String result)
	{
		model.getData().get(row).result = result;
		model.fireTableChanged(new TableModelEvent(model, row, row, 2));
		addCallBack.call(model.getData());
	}
	
	/**
	 * Del.
	 *
	 * @param sdrl the data to delete
	 */
	public void del(@SuppressWarnings("exports") final List<SrcDstResult> sdrl)
	{
		for (final SrcDstResult sdr : sdrl)
			model.getData().remove(sdr);
		model.fireTableChanged(new TableModelEvent(model));
		addCallBack.call(model.getData());
	}
	
	/**
	 * get selected values as a {@link List} of {@link SrcDstResult}
	 * @return the {@link List} of {@link SrcDstResult} corresponding to selected values
	 */
	@SuppressWarnings("exports")
	public List<SrcDstResult> getSelectedValuesList()
	{
		int[] rows = getSelectedRows();
		List<SrcDstResult> list = new ArrayList<>();
		for(int row : rows)
			list.add(model.getData().get(row));
		return list;
	}

	@Override
	public void clearResults()
	{
		model.getData().forEach(r->r.result="");
		model.fireTableChanged(new TableModelEvent(model, 0, model.getRowCount()-1, 2));
		addCallBack.call(model.getData());
	}
	
	public void call()
	{
		addCallBack.call(model.getData());
	}
}
