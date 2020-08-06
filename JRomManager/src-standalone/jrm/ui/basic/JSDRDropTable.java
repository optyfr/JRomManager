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
import javax.swing.event.TableModelListener;
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
	private SDRTableModel model;
	
	/** The add call back. */
	private final AddDelCallBack addCallBack;
	
	/**
	 * The Interface AddDelCallBack.
	 */
	@FunctionalInterface
	public interface AddDelCallBack
	{
		public void call(SDRList files);
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
		this.model.addTableModelListener(new TableModelListener()
		{
			@Override
			public void tableChanged(TableModelEvent e)
			{
				if(e.getColumn()>=0 && model.getColumnClass(e.getColumn()).equals(Boolean.class) && e.getType()==TableModelEvent.UPDATE)
				{
					addCallBack.call(model.getData());
				}
			}
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
	public void dragEnter(DropTargetDragEvent dtde)
	{
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde)
	{
		final Transferable transferable = dtde.getTransferable();
		if (isEnabled() && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
            Point point = dtde.getLocation();
            int old_row = model.getCurrentRow();
            int old_col = model.getCurrentCol();
            int row = model.setCurrentRow(rowAtPoint(point));
            int col = model.setCurrentCol(columnAtPoint(point));
            if(old_col != col || old_row != row)
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
	            	if(old_row != -1)
	            		model.fireTableChanged(new TableModelEvent(model, old_row, old_row, old_col));
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
	public void dropActionChanged(DropTargetDragEvent dtde)
	{
	}

	@Override
	public void dragExit(DropTargetEvent dte)
	{
		model.setCurrentRow(-1);
		setBackground(color);
		model.fireTableChanged(new TableModelEvent(model));
	}

	@Override
	public void drop(DropTargetDropEvent dtde)
	{
		model.setCurrentRow(-1);
		setBackground(color);
		model.fireTableChanged(new TableModelEvent(model));
		try
		{
			final Transferable transferable = dtde.getTransferable();

			if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				
	            Point point = dtde.getLocation();
	            int row = rowAtPoint(point);
	            int col = columnAtPoint(point);

	            @SuppressWarnings("unchecked")
				final List<File> files = ((List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor)).stream().filter(f -> {
					FileFilter filter = null;
					if(col==1)
						filter = model.getDstFilter();
					else if(col==0)
						filter = model.getSrcFilter();
					if(filter!=null)
						return filter.accept(f);
					return true;
				}).collect(Collectors.toList());
				if (files.size() > 0)
				{
		            int start_size = model.getData().size();
	            	for(int i = 0; i < files.size(); i++)
	            	{
	            		File file = files.get(i);
	            		SrcDstResult line;
	            		if(row == -1 || row + i >= model.getData().size())
			            	model.getData().add(line = new SrcDstResult());
	            		else
	            			line = model.getData().get(row + i);
		            	if(col==1)
		            		line.dst = file.getPath();
		            	else
		            		line.src = file.getPath();
	            	}
	            	if(row != -1)
	            		model.fireTableChanged(new TableModelEvent(model, row, start_size-1, col));
	            	if(start_size != model.getData().size())
	            		model.fireTableChanged(new TableModelEvent(model, start_size, model.getData().size()-1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
	            	addCallBack.call(model.getData());
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
			Log.err(e.getMessage(),e);
			dtde.rejectDrop();
		}
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
	public void del(final List<SrcDstResult> sdrl)
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
