package jrm.ui.batch;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.io.FilenameUtils;

import jrm.batch.Compressor;
import jrm.batch.CompressorFormat;
import jrm.locale.Messages;
import jrm.misc.HTMLRenderer;
import jrm.security.Session;
import jrm.ui.basic.EnhTableModel;
import jrm.ui.batch.BatchCompressorPanel.BatchCompressorTable.AddCallBack;
import jrm.ui.batch.BatchCompressorPanel.BatchCompressorTableModel.FileResult;
import jrm.ui.progress.Progress;
import one.util.streamex.StreamEx;

@SuppressWarnings("serial")
public class BatchCompressorPanel extends JPanel implements HTMLRenderer
{
	private BatchCompressorTable table;
	private JCheckBox chckbxForce;
	private JComboBox<CompressorFormat> comboBox;
	private JButton btnClear;
	
	static class BatchCompressorTableModel implements EnhTableModel
	{
		static class FileResult
		{
			File file;
			String result = "";
			
			public FileResult(File file)
			{
				this.file = file;
			}
		}
		
		private List<FileResult> data = new ArrayList<>();
	    private final EventListenerList listenerList = new EventListenerList();
		private final String[] columnNames = new String[] {Messages.getString("BatchCompressorPanel.File"), Messages.getString("BatchCompressorPanel.Status")}; //$NON-NLS-1$ //$NON-NLS-2$
		private final Class<?>[] columnTypes = new Class<?>[] { Object.class, String.class };
		private final TableCellRenderer[] cellRenderers = new TableCellRenderer[] { new DefaultTableCellRenderer()
		{
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
			{
				setBackground(Color.white);
				if (value instanceof File)
				{
					super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					setText(trimmedStringCalculator(((File) value).getPath(), table, this, table.getColumnModel().getColumn(column).getWidth() - 10));
					return this;
				}
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			}
		}, new DefaultTableCellRenderer()
		{
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
			{
				setBackground(Color.white);
				setHorizontalAlignment(TRAILING);
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			}
		} };
		private final int[] widths = {0, 0};
		private final String[] headers_tt = columnNames;

		private static String trimmedStringCalculator(String inputText, JTable table, JLabel component, int width)
		{
			String ellipses = "..."; //$NON-NLS-1$
			String textToBeDisplayed = ""; //$NON-NLS-1$
			FontMetrics fm = table.getFontMetrics(component.getFont());
			for (int i = inputText.length() - 1; i >= 0; i--)
				if (fm.stringWidth(ellipses + textToBeDisplayed) <= width)
					textToBeDisplayed = inputText.charAt(i) + textToBeDisplayed;
			if (!textToBeDisplayed.equals(inputText))
				return ellipses.concat(textToBeDisplayed);
			return inputText;
		}
	    
	 		@Override
		public int getRowCount()
		{
			return data.size();
		}

		@Override
		public int getColumnCount()
		{
			return columnTypes.length;
		}

		public List<FileResult> getData()
		{
			return data;
		}
		
		/**
		 * @param data initialize data
		 */
		public void setData(List<FileResult> data)
		{
			this.data = data;
			fireTableChanged(new TableModelEvent(this));
		}

		@Override
		public void addTableModelListener(final TableModelListener l)
		{
			listenerList.add(TableModelListener.class, l);
		}

		@Override
		public void removeTableModelListener(final TableModelListener l)
		{
			listenerList.remove(TableModelListener.class, l);
		}

		/**
		 * Sends TableChanged event to listeners
		 * @param e the {@link TableModelEvent} to send
		 */
		public void fireTableChanged(final TableModelEvent e)
		{
			final Object[] listeners = listenerList.getListenerList();
			for(int i = listeners.length - 2; i >= 0; i -= 2)
				if(listeners[i] == TableModelListener.class)
					((TableModelListener) listeners[i + 1]).tableChanged(e);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			switch (columnIndex)
			{
				case 0:
					return getData().get(rowIndex).file;
				case 1:
					return getData().get(rowIndex).result;
			}
			return null;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			return columnTypes[columnIndex];
		}
		
		@Override
		public String getColumnName(int column)
		{
			return columnNames[column];
		}
		
		public TableCellRenderer getCellRenderer(int column)
		{
			return cellRenderers[column];
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return false;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			switch (columnIndex)
			{
				case 0:
					getData().get(rowIndex).file = (File)aValue;
					break;
				case 1:
					getData().get(rowIndex).result = (String)aValue;
					break;
			}
			fireTableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex, TableModelEvent.UPDATE));
		}

		@Override
		public TableCellRenderer[] getCellRenderers()
		{
			return cellRenderers;
		}

		@Override
		public int getColumnWidth(int columnIndex)
		{
			return widths[columnIndex];
		}

		@Override
		public String getColumnTT(int columnIndex)
		{
			return headers_tt[columnIndex];
		}
	}
	
	static class BatchCompressorTable extends JTable implements DropTargetListener
	{
		private BatchCompressorTableModel model;
		private Color color;

		/** The add call back. */
		private final AddCallBack callback;
		
		/**
		 * The Interface AddDelCallBack.
		 */
		@FunctionalInterface
		public interface AddCallBack
		{
			public void call(List<FileResult> files);
		}
		
		public BatchCompressorTable(BatchCompressorTableModel model, AddCallBack callback)
		{
			super(model);
			this.model=model;
			this.callback=callback;
			for(int i = 0; i < getColumnModel().getColumnCount(); i++)
				getColumnModel().getColumn(i).setCellRenderer(model.getCellRenderer(i));
			color = getBackground();
			new DropTarget(this, this);
			this.model.addTableModelListener(new TableModelListener()
			{
				@Override
				public void tableChanged(TableModelEvent e)
				{
					if(e.getColumn()>=0 && model.getColumnClass(e.getColumn()).equals(Boolean.class) && e.getType()==TableModelEvent.UPDATE)
					{
						callback.call(model.getData());
					}
				}
			});
			setFillsViewportHeight(true);
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
            	setBackground(Color.decode("#DDFFDD")); //$NON-NLS-1$
        		model.fireTableChanged(new TableModelEvent(model));
				dtde.acceptDrag(DnDConstants.ACTION_COPY);
			}
			else
			{
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
			setBackground(color);
			model.fireTableChanged(new TableModelEvent(model));
		}

		@Override
		public void drop(DropTargetDropEvent dtde)
		{
			setBackground(color);
			model.fireTableChanged(new TableModelEvent(model));
			try
			{
				final Transferable transferable = dtde.getTransferable();

				if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					
					FileFilter filter = new FileFilter()
					{
						String[] extensions = new String[] {"zip", "7z"};
						
						@Override
						public boolean accept(File pathname)
						{
							return FilenameUtils.isExtension(pathname.getName(), extensions);
						}
					};
		            @SuppressWarnings("unchecked")
					final List<File> files = ((List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor)).stream().filter(filter::accept).collect(Collectors.toList());
					if (files.size() > 0)
					{
						int start_size = model.getData().size();
						for (int i = 0; i < files.size(); i++)
							model.getData().add(new FileResult(files.get(i)));
						if (start_size != model.getData().size())
							model.fireTableChanged(new TableModelEvent(model, start_size, model.getData().size() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
						callback.call(model.getData());
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
				e.printStackTrace();
				dtde.rejectDrop();
			}
		}
		
	}
	
	public BatchCompressorPanel(Session session)
	{
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{450, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{300, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 5;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		table = new BatchCompressorTable(new BatchCompressorTableModel(),new AddCallBack()
		{
			@Override
			public void call(List<FileResult> files)
			{
				// TODO Auto-generated method stub
			}
		});
		scrollPane.setViewportView(table);
		
		comboBox = new JComboBox<>();
		comboBox.setModel(new DefaultComboBoxModel<>(CompressorFormat.values()));
		comboBox.setSelectedIndex(1);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 1;
		add(comboBox, gbc_comboBox);
		
		chckbxForce = new JCheckBox(Messages.getString("BatchCompressorPanel.Force")); //$NON-NLS-1$
		GridBagConstraints gbc_chckbxForce = new GridBagConstraints();
		gbc_chckbxForce.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxForce.gridx = 2;
		gbc_chckbxForce.gridy = 1;
		add(chckbxForce, gbc_chckbxForce);
		
		JButton btnStart = new JButton(Messages.getString("BatchCompressorPanel.Start")); //$NON-NLS-1$
		btnStart.addActionListener(e->{
			final Progress progress = new Progress(SwingUtilities.getWindowAncestor(this));
			progress.setInfos(Runtime.getRuntime().availableProcessors(), true);
			final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
			{
				@Override
				protected Void doInBackground() throws Exception
				{
					for(int i = 0; i < table.getRowCount(); i++)
						table.setValueAt("", i, 1);
					AtomicInteger cnt = new AtomicInteger();
					final Compressor compressor = new Compressor(session, cnt, table.getRowCount(), progress);
					StreamEx.of(table.model.getData().parallelStream().unordered()).takeWhile(p->!progress.isCancel()).forEach(fr->{
						final int i = table.model.getData().indexOf(fr);
						File file = fr.file;
						cnt.incrementAndGet();
						Compressor.UpdResultCallBack cb = txt -> table.setValueAt(txt, i, 1);
						Compressor.UpdSrcCallBack scb = src -> table.setValueAt(src, i, 0);
						switch((CompressorFormat)comboBox.getSelectedItem())
						{
							case SEVENZIP:
							{
								switch(FilenameUtils.getExtension(file.getName()))
								{
									case "zip":
										compressor.zip2SevenZip(file, cb, scb);
										break;
									case "7z":
										if(chckbxForce.isSelected())
											compressor.sevenZip2SevenZip(file, cb, scb);
										else
											cb.apply("Skipped");
										break;
									case "rar":
										compressor.sevenZip2SevenZip(file, cb, scb);
										break;
								}
								break;
							}
							case ZIP:
							{
								switch(FilenameUtils.getExtension(file.getName()))
								{
									case "rar":
									case "7z":
										compressor.sevenZip2Zip(file, false, cb, scb);
										break;
									case "zip":
										if(chckbxForce.isSelected())
											compressor.zip2Zip(file, cb, scb);
										else
											cb.apply("Skipped");
										break;
								}
								break;
							}
							case TZIP:
							{
								switch(FilenameUtils.getExtension(file.getName()))
								{
									case "rar":
									case "7z":
										file = compressor.sevenZip2Zip(file, true, cb, scb);
										if(file!=null && file.exists())
											compressor.zip2TZip(file, chckbxForce.isSelected(), cb);
										break;
									case "zip":
										compressor.zip2TZip(file, chckbxForce.isSelected(), cb);
										break;
								}
								break;
							}
						}
					});
					return null;
				}

				@Override
				protected void done()
				{
					progress.dispose();
				}

			};
			worker.execute();
			progress.setVisible(true);
		});
		
		btnClear = new JButton(Messages.getString("BatchCompressorPanel.btnClear.text")); //$NON-NLS-1$
		btnClear.addActionListener(e->table.model.setData(new ArrayList<>()));
		GridBagConstraints gbc_btnClear = new GridBagConstraints();
		gbc_btnClear.insets = new Insets(0, 0, 0, 5);
		gbc_btnClear.gridx = 3;
		gbc_btnClear.gridy = 1;
		add(btnClear, gbc_btnClear);
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.gridx = 4;
		gbc_btnStart.gridy = 1;
		add(btnStart, gbc_btnStart);
	}
	
	
}
