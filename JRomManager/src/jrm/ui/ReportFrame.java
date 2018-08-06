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
package jrm.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.EnumSet;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.SerializationUtils;

import jrm.Messages;
import jrm.misc.Settings;
import jrm.profile.report.FilterOptions;
import jrm.profile.scan.Scan;

@SuppressWarnings("serial")
public class ReportFrame extends JDialog implements StatusHandler
{
	private final JLabel lblStatus = new JLabel(""); //$NON-NLS-1$

	public ReportFrame(final Window owner) throws HeadlessException
	{
		super(); //$NON-NLS-1$
		setTitle(Messages.getString("ReportFrame.Title"));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				Settings.setProperty("ReportFrame.Bounds", Hex.encodeHexString(SerializationUtils.serialize(getBounds()))); //$NON-NLS-1$
			}
		});
		setTitle(Messages.getString("ReportFrame.Title")); //$NON-NLS-1$
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(400, 300));
		setIconImage(Toolkit.getDefaultToolkit().getImage(ReportFrame.class.getResource("/jrm/resources/rom.png"))); //$NON-NLS-1$
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 784, 0 };
		gridBagLayout.rowHeights = new int[] { 280, 24, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		final JScrollPane scrollPane = new JScrollPane();
		final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);

		final JTree tree = new JTree();
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setModel(Scan.report.getModel());
		tree.setCellRenderer(new ReportTreeCellRenderer());
		Scan.report.setStatusHandler(this);
		scrollPane.setViewportView(tree);

		final JPopupMenu popupMenu = new JPopupMenu();
		ReportFrame.addPopup(tree, popupMenu);

		final JMenuItem mntmOpenAllNodes = new JMenuItem(Messages.getString("ReportFrame.mntmOpenAllNodes.text")); //$NON-NLS-1$
		mntmOpenAllNodes.setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_open.png"))); //$NON-NLS-1$
		mntmOpenAllNodes.addActionListener(e -> {
			tree.invalidate();
			int j = tree.getRowCount();
			int i = 0;
			while(i < j)
			{
				tree.expandRow(i);
				i += 1;
				j = tree.getRowCount();
			}
			tree.validate();
		});
		popupMenu.add(mntmOpenAllNodes);

		final JCheckBoxMenuItem chckbxmntmShowOkEntries = new JCheckBoxMenuItem(Messages.getString("ReportFrame.chckbxmntmShowOkEntries.text")); //$NON-NLS-1$
		chckbxmntmShowOkEntries.setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_green.png"))); //$NON-NLS-1$
		chckbxmntmShowOkEntries.addItemListener(e -> {
			final EnumSet<FilterOptions> options = Scan.report.getModel().getFilterOptions();
			if(e.getStateChange() == ItemEvent.SELECTED)
				options.add(FilterOptions.SHOWOK);
			else
				options.remove(FilterOptions.SHOWOK);
			Scan.report.getModel().filter(options.toArray(new FilterOptions[0]));
		});

		final JMenuItem mntmCloseAllNodes = new JMenuItem(Messages.getString("ReportFrame.mntmCloseAllNodes.text")); //$NON-NLS-1$
		mntmCloseAllNodes.addActionListener(e -> {
			tree.invalidate();
			int j = tree.getRowCount();
			int i = 0;
			while(i < j)
			{
				tree.collapseRow(i);
				i += 1;
				j = tree.getRowCount();
			}
			tree.validate();
		});
		mntmCloseAllNodes.setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed.png"))); //$NON-NLS-1$
		popupMenu.add(mntmCloseAllNodes);
		popupMenu.add(chckbxmntmShowOkEntries);

		final JCheckBoxMenuItem chckbxmntmHideFullyMissing = new JCheckBoxMenuItem(Messages.getString("ReportFrame.chckbxmntmHideFullyMissing.text")); //$NON-NLS-1$
		chckbxmntmHideFullyMissing.setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_red.png"))); //$NON-NLS-1$
		chckbxmntmHideFullyMissing.addItemListener(e -> {
			final EnumSet<FilterOptions> options = Scan.report.getModel().getFilterOptions();
			if(e.getStateChange() == ItemEvent.SELECTED)
				options.add(FilterOptions.HIDEMISSING);
			else
				options.remove(FilterOptions.HIDEMISSING);
			Scan.report.getModel().filter(options.toArray(new FilterOptions[0]));
		});
		popupMenu.add(chckbxmntmHideFullyMissing);
		final GridBagConstraints gbc_lblStatus = new GridBagConstraints();
		gbc_lblStatus.ipadx = 2;
		gbc_lblStatus.insets = new Insets(2, 2, 2, 2);
		gbc_lblStatus.fill = GridBagConstraints.BOTH;
		gbc_lblStatus.gridx = 0;
		gbc_lblStatus.gridy = 1;
		lblStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		getContentPane().add(lblStatus, gbc_lblStatus);

		pack();

		try
		{
			setBounds(SerializationUtils.deserialize(Hex.decodeHex(Settings.getProperty("ReportFrame.Bounds", Hex.encodeHexString(SerializationUtils.serialize(new Rectangle(10,10,800,600))))))); //$NON-NLS-1$
		}
		catch(final DecoderException e1)
		{
			e1.printStackTrace();
		}
	}

	private static void addPopup(final Component component, final JPopupMenu popup)
	{
		component.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			private void showMenu(final MouseEvent e)
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	@Override
	public void setStatus(final String text)
	{
		lblStatus.setText(text);
	}
}
