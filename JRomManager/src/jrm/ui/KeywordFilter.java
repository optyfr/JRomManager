package jrm.ui;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;
import javax.swing.border.TitledBorder;

@SuppressWarnings("serial")
public class KeywordFilter extends JDialog
{
	private JList<String> KWSrc;
	private JList<String> KWDst;
	private DefaultListModel<String> dstmodel = new DefaultListModel<String>();

	public KeywordFilter(Window owner, String[] src, CallBack callback)
	{
		super(owner, "Keyword Filter", ModalityType.APPLICATION_MODAL);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ProfileViewer.class.getResource("/jrm/resources/rom.png"))); //$NON-NLS-1$

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 217, 217, 0 };
		gbl_panel.rowHeights = new int[] { 23, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener((e) -> dispose());
		btnCancel.setHorizontalAlignment(SwingConstants.LEADING);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.WEST;
		gbc_btnCancel.fill = GridBagConstraints.VERTICAL;
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 0;
		panel.add(btnCancel, gbc_btnCancel);

		JButton btnFilter = new JButton("Filter");
		btnFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				callback.call(KeywordFilter.this);
				dispose();
			}
		});
		btnFilter.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_btnFilter = new GridBagConstraints();
		gbc_btnFilter.insets = new Insets(5, 5, 5, 5);
		gbc_btnFilter.anchor = GridBagConstraints.EAST;
		gbc_btnFilter.fill = GridBagConstraints.VERTICAL;
		gbc_btnFilter.gridx = 1;
		gbc_btnFilter.gridy = 0;
		panel.add(btnFilter, gbc_btnFilter);

		JSplitPane KWSplitPane = new JSplitPane();
		KWSplitPane.setResizeWeight(0.5);
		KWSplitPane.setOneTouchExpandable(true);
		KWSplitPane.setContinuousLayout(true);
		getContentPane().add(KWSplitPane, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(new TitledBorder(null, "Available", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		KWSplitPane.setLeftComponent(scrollPane);

		KWSrc = new JList<String>();
		KWSrc.setVisibleRowCount(16);
		scrollPane.setViewportView(KWSrc);
		KWSrc.setDragEnabled(true);
		KWSrc.setDropMode(DropMode.INSERT);
		StringMoveHandler.createFor(KWSrc);
		DefaultListModel<String> model = new DefaultListModel<String>();
		for(String s : src)
			model.addElement(s);
		KWSrc.setModel(model);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBorder(new TitledBorder(null, "Used", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		KWSplitPane.setRightComponent(scrollPane_1);

		KWDst = new JList<String>();
		KWDst.setVisibleRowCount(16);
		scrollPane_1.setViewportView(KWDst);

		KWDst.setDragEnabled(true);
		KWDst.setDropMode(DropMode.INSERT);
		StringMoveHandler.createFor(KWDst);
		KWDst.setModel(dstmodel);
		
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	public ArrayList<String> getFilter()
	{
		return Collections.list(dstmodel.elements());
	}
	
	public interface CallBack
	{
		public void call(KeywordFilter filter);
	}
	
	private static class StringMoveHandler extends TransferHandler
	{
		private DataFlavor objectArrayFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items");
		// We'll be moving the strings of this list
		private JList<String> list;

		// Clients should use a static factory method to instantiate the handler
		private StringMoveHandler()
		{
		}

		public static StringMoveHandler createFor(JList<String> list)
		{
			StringMoveHandler handler = new StringMoveHandler();
			list.setTransferHandler(handler);
			handler.list = list;
			return handler;
		}

		@Override
		public boolean canImport(TransferSupport info)
		{
			return info.isDataFlavorSupported(objectArrayFlavor);
		}

		@Override
		public boolean importData(TransferSupport transferSupport)
		{
			Transferable t = transferSupport.getTransferable();
			boolean success = false;
			try
			{
				Object[] importedData = (Object[]) t.getTransferData(objectArrayFlavor);
				addToListModel(importedData);
				success = true;
			}
			catch (UnsupportedFlavorException | IOException e)
			{
				e.printStackTrace();
			}
			return success;
		}

		private void addToListModel(Object[] importedData)
		{
			JList.DropLocation loc = list.getDropLocation();
			int dropIndex = loc.getIndex();

			DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
			for (int i = 0; i < importedData.length; i++)
			{
				Object elem = importedData[i];
				if (elem instanceof String)
				{
					listModel.add(dropIndex + i, (String) elem);
				}
				else
				{
					System.err.println("Imported data contained something else than strings: " + elem);
				}
			}
		}

		@Override
		public int getSourceActions(JComponent c)
		{
			return TransferHandler.MOVE;
		}

		@Override
		public Transferable createTransferable(JComponent source)
		{
			// We need the values from the list as an object array, otherwise the data
			// flavor won't match in importData
			@SuppressWarnings("deprecation")
			Object[] valuesToTransfer = list.getSelectedValues();
			return new DataHandler(valuesToTransfer, objectArrayFlavor.getMimeType());
		}

		@Override
		protected void exportDone(JComponent source, Transferable data, int action)
		{
			if (action == TransferHandler.MOVE)
			{
				try
				{
					Object[] exportedData = (Object[]) data.getTransferData(objectArrayFlavor);
					removeFromListModel(exportedData);
				}
				catch (UnsupportedFlavorException | IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		private void removeFromListModel(Object[] dataToRemove)
		{
			DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
			for (Object elemToRemove : dataToRemove)
			{
				boolean removedSuccessfully = listModel.removeElement(elemToRemove);
				if (!removedSuccessfully)
				{
					System.err.println("Source model did not contain exported data");
				}
			}
		}
	}

}
