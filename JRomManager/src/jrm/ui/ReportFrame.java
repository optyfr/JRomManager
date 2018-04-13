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
import jrm.profiler.report.FilterOptions;
import jrm.profiler.scan.Scan;

@SuppressWarnings("serial")
public class ReportFrame extends JDialog implements StatusHandler 
{
	private final JLabel lblStatus = new JLabel("");

	public ReportFrame(Window owner) throws HeadlessException
	{
		super(owner, Messages.getString("ReportFrame.Title"), ModalityType.MODELESS); //$NON-NLS-1$
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Settings.setProperty("ReportFrame.Bounds", Hex.encodeHexString(SerializationUtils.serialize(getBounds())));
			}
		});
		setTitle(Messages.getString("ReportFrame.Title")); //$NON-NLS-1$
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(400, 300));
		setIconImage(Toolkit.getDefaultToolkit().getImage(ReportFrame.class.getResource("/jrm/resources/rom.png")));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 784, 0 };
		gridBagLayout.rowHeights = new int[] { 280, 24, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);

		JTree tree = new JTree();
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setModel(Scan.report.getModel());
		tree.setCellRenderer(new ReportTreeCellRenderer());
		Scan.report.setStatusHandler(this);
		scrollPane.setViewportView(tree);

		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(tree, popupMenu);

		JMenuItem mntmOpenAllNodes = new JMenuItem(Messages.getString("ReportFrame.mntmOpenAllNodes.text")); //$NON-NLS-1$
		mntmOpenAllNodes.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int j = tree.getRowCount();
				int i = 0;
				while(i < j)
				{
					tree.expandRow(i);
					i += 1;
					j = tree.getRowCount();
				}
			}
		});
		popupMenu.add(mntmOpenAllNodes);

		JCheckBoxMenuItem chckbxmntmShowOkEntries = new JCheckBoxMenuItem(Messages.getString("ReportFrame.chckbxmntmShowOkEntries.text")); //$NON-NLS-1$
		chckbxmntmShowOkEntries.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				EnumSet<FilterOptions> options = Scan.report.getModel().getFilterOptions();
				if(e.getStateChange() == ItemEvent.SELECTED)
					options.add(FilterOptions.SHOWOK);
				else
					options.remove(FilterOptions.SHOWOK);
				Scan.report.getModel().filter(options.toArray(new FilterOptions[0]));
			}
		});
		popupMenu.add(chckbxmntmShowOkEntries);

		JCheckBoxMenuItem chckbxmntmHideFullyMissing = new JCheckBoxMenuItem(Messages.getString("ReportFrame.chckbxmntmHideFullyMissing.text")); //$NON-NLS-1$
		chckbxmntmHideFullyMissing.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				EnumSet<FilterOptions> options = Scan.report.getModel().getFilterOptions();
				if(e.getStateChange() == ItemEvent.SELECTED)
					options.add(FilterOptions.HIDEMISSING);
				else
					options.remove(FilterOptions.HIDEMISSING);
				Scan.report.getModel().filter(options.toArray(new FilterOptions[0]));
			}
		});
		popupMenu.add(chckbxmntmHideFullyMissing);
		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
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
			setBounds(SerializationUtils.deserialize(Hex.decodeHex(Settings.getProperty("ReportFrame.Bounds", Hex.encodeHexString(SerializationUtils.serialize(new Rectangle(10,10,800,600)))))));
		}
		catch(DecoderException e1)
		{
			e1.printStackTrace();
		}
	}

	private static void addPopup(Component component, final JPopupMenu popup)
	{
		component.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e)
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	@Override
	public void setStatus(String text)
	{
		lblStatus.setText(text);
	}
}
