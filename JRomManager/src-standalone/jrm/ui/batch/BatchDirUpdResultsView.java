package jrm.ui.batch;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jrm.batch.DirUpdaterResults;
import jrm.batch.DirUpdaterResults.DirUpdaterResult;
import jrm.security.Session;
import jrm.ui.basic.EnhTableModel;
import jrm.ui.basic.JTableButton;
import jrm.ui.profile.report.ReportLite;

@SuppressWarnings("serial")
public class BatchDirUpdResultsView extends JScrollPane
{
	private JTable table;

	/**
	 * Create the panel.
	 */
	public BatchDirUpdResultsView(final Session session, DirUpdaterResults results)
	{
		table = new JTable();
		setViewportView(table);
		EnhTableModel model = new EnhTableModel()
		{
			private JTableButton buttons = new JTableButton();
			{
		        buttons.addHandler(new JTableButton.TableButtonPressedHandler() {
					
					@Override
					public void onButtonPress(int row, int column) {
						new ReportLite(session, SwingUtilities.getWindowAncestor(BatchDirUpdResultsView.this),results.getResults().get(row).getDat());
					}
				});
			}
			private final String[] headers = {"DAT/XML","Have","Miss","Total","Report"};
			private final Class<?>[] types = {String.class,String.class,String.class,String.class,String.class};
			private final int[] widths = {240, 40, 40, 40, -70};
			final TableCellRenderer[] renderers = {null,null,null,null,buttons};
			final TableCellEditor[] editors = {null,null,null,null,buttons};
			
			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex)
			{
			}
			
			@Override
			public void removeTableModelListener(TableModelListener l)
			{
			}
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex)
			{
				return columnIndex==4;
			}
			
			@Override
			public Object getValueAt(int rowIndex, int columnIndex)
			{
				if(results!=null)
				{
					DirUpdaterResult result = results.getResults().get(rowIndex);
					switch(columnIndex)
					{
						case 0:
							return result.getDat().toString();
						case 1:
							return result.getStats().set_create_complete + result.getStats().set_found_fixcomplete + result.getStats().set_found_ok;
						case 2:
							return result.getStats().set_create + result.getStats().set_found + result.getStats().set_missing - (result.getStats().set_create_complete + result.getStats().set_found_fixcomplete + result.getStats().set_found_ok);
						case 3:
							return result.getStats().set_create + result.getStats().set_found + result.getStats().set_missing;
						case 4:
							return "Report";
						default:
							return null;
					}
				}
				return null;
			}
			
			@Override
			public int getRowCount()
			{
				return results!=null?results.getResults().size():0;
			}
			
			@Override
			public String getColumnName(int columnIndex)
			{
				return headers[columnIndex];
			}
			
			@Override
			public int getColumnCount()
			{
				return headers.length;
			}
			
			@Override
			public Class<?> getColumnClass(int columnIndex)
			{
				return types[columnIndex];
			}
			
			@Override
			public void addTableModelListener(TableModelListener l)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public TableCellRenderer[] getCellRenderers()
			{
				return renderers;
			}

			@Override
			public TableCellEditor[] getCellEditors()
			{
				return editors;
			}

			@Override
			public int getColumnWidth(int columnIndex)
			{
				return widths[columnIndex];
			}

			@Override
			public String getColumnTT(int columnIndex)
			{
				// TODO Auto-generated method stub
				return headers[columnIndex];
			}
		};
		table.setModel(model);
		for(int i = 0; i < table.getColumnModel().getColumnCount(); i++)
			table.getColumnModel().getColumn(i).setCellRenderer(model.getCellRenderers()[i]);
		for(int i = 0; i < table.getColumnModel().getColumnCount(); i++)
			table.getColumnModel().getColumn(i).setCellEditor(model.getCellEditors()[i]);
		model.applyColumnsWidths(table);
	}

}
