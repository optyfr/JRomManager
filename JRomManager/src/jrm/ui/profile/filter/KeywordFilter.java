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
package jrm.ui.profile.filter;

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

import jrm.locale.Messages;
import jrm.ui.profile.ProfileViewer;

// TODO: Auto-generated Javadoc
/**
 * The Class KeywordFilter.
 */
@SuppressWarnings("serial")
public class KeywordFilter extends JDialog
{
	
	/** The KW src. */
	private JList<String> KWSrc;
	
	/** The KW dst. */
	private JList<String> KWDst;
	
	/** The dstmodel. */
	private DefaultListModel<String> dstmodel = new DefaultListModel<String>();

	/**
	 * Instantiates a new keyword filter.
	 *
	 * @param owner the owner
	 * @param src the src
	 * @param callback the callback
	 */
	public KeywordFilter(Window owner, String[] src, CallBack callback)
	{
		super(owner, Messages.getString("KeywordFilter.Title"), ModalityType.APPLICATION_MODAL); //$NON-NLS-1$
		setIconImage(Toolkit.getDefaultToolkit().getImage(ProfileViewer.class.getResource("/jrm/resources/rom.png"))); //$NON-NLS-1$

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.SOUTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 217, 217, 0 };
		gbl_panel.rowHeights = new int[] { 23, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JButton btnCancel = new JButton(Messages.getString("KeywordFilter.Cancel")); //$NON-NLS-1$
		btnCancel.addActionListener((e) -> dispose());
		btnCancel.setHorizontalAlignment(SwingConstants.LEADING);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.WEST;
		gbc_btnCancel.fill = GridBagConstraints.VERTICAL;
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 0;
		panel.add(btnCancel, gbc_btnCancel);

		JButton btnFilter = new JButton(Messages.getString("KeywordFilter.Filter")); //$NON-NLS-1$
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
		scrollPane.setBorder(new TitledBorder(null, Messages.getString("KeywordFilter.Available"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))); //$NON-NLS-1$
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
		scrollPane_1.setBorder(new TitledBorder(null, Messages.getString("KeywordFilter.Used"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))); //$NON-NLS-1$
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

	/**
	 * Gets the filter.
	 *
	 * @return the filter
	 */
	public ArrayList<String> getFilter()
	{
		return Collections.list(dstmodel.elements());
	}
	
	/**
	 * The Interface CallBack.
	 */
	public interface CallBack
	{
		
		/**
		 * Call.
		 *
		 * @param filter the filter
		 */
		public void call(KeywordFilter filter);
	}
	
	/**
	 * The Class StringMoveHandler.
	 */
	private static class StringMoveHandler extends TransferHandler
	{
		
		/** The object array flavor. */
		private DataFlavor objectArrayFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Array of items"); //$NON-NLS-1$
		
		/** The list. */
		// We'll be moving the strings of this list
		private JList<String> list;

		/**
		 * Instantiates a new string move handler.
		 */
		// Clients should use a static factory method to instantiate the handler
		private StringMoveHandler()
		{
		}

		/**
		 * Creates the for.
		 *
		 * @param list the list
		 * @return the string move handler
		 */
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

		/**
		 * Adds the to list model.
		 *
		 * @param importedData the imported data
		 */
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
					System.err.println("Imported data contained something else than strings: " + elem); //$NON-NLS-1$
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

		/**
		 * Removes the from list model.
		 *
		 * @param dataToRemove the data to remove
		 */
		private void removeFromListModel(Object[] dataToRemove)
		{
			DefaultListModel<String> listModel = (DefaultListModel<String>) list.getModel();
			for (Object elemToRemove : dataToRemove)
			{
				boolean removedSuccessfully = listModel.removeElement(elemToRemove);
				if (!removedSuccessfully)
				{
					System.err.println("Source model did not contain exported data"); //$NON-NLS-1$
				}
			}
		}
	}

}
