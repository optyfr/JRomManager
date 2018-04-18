package jrm.ui;

import javax.swing.JDialog;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.JToggleButton;
import javax.swing.JCheckBox;
import javax.swing.ImageIcon;
import java.awt.Toolkit;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import java.awt.Dimension;

@SuppressWarnings("serial")
public class ProfileViewer extends JDialog
{
	private JTable tableEntity;
	private JTable tableS;
	public ProfileViewer() {
		setIconImage(Toolkit.getDefaultToolkit().getImage(ProfileViewer.class.getResource("/jrm/resources/rom.png")));
		setTitle("Profile Viewer");
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		JPanel panelWare = new JPanel();
		splitPane.setLeftComponent(panelWare);
		panelWare.setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBarWare = new JToolBar();
		panelWare.add(toolBarWare, BorderLayout.SOUTH);
		
		JToggleButton tglbtnMissingWare = new JToggleButton("");
		tglbtnMissingWare.setToolTipText("Missing");
		tglbtnMissingWare.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/folder_closed_red.png")));
		toolBarWare.add(tglbtnMissingWare);
		
		JToggleButton tglbtnPartialWare = new JToggleButton("");
		tglbtnPartialWare.setToolTipText("Partial");
		tglbtnPartialWare.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/folder_closed_orange.png")));
		toolBarWare.add(tglbtnPartialWare);
		
		JToggleButton tglbtnComplete = new JToggleButton("");
		tglbtnComplete.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/folder_closed_green.png")));
		tglbtnComplete.setToolTipText("Complete");
		toolBarWare.add(tglbtnComplete);
		
		JSplitPane splitPaneSLS = new JSplitPane();
		splitPaneSLS.setOneTouchExpandable(true);
		splitPaneSLS.setContinuousLayout(true);
		splitPaneSLS.setResizeWeight(0.25);
		panelWare.add(splitPaneSLS, BorderLayout.CENTER);
		
		JScrollPane scrollPaneS = new JScrollPane();
		splitPaneSLS.setRightComponent(scrollPaneS);
		
		tableS = new JTable();
		tableS.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Name", "Description", "Size", "CloneOf" })
		{
			Class<?>[] columnTypes = new Class[] { Object.class, String.class, Long.class, Object.class };

			@Override
			public Class<?> getColumnClass(int columnIndex)
			{
				return columnTypes[columnIndex];
			}
		});
		tableS.setFillsViewportHeight(true);
		tableS.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableS.setShowGrid(false);
		tableS.setShowHorizontalLines(false);
		tableS.setShowVerticalLines(false);
		scrollPaneS.setViewportView(tableS);
		
		JScrollPane scrollPaneSL = new JScrollPane();
		splitPaneSLS.setLeftComponent(scrollPaneSL);
		
		JTable tableSL = new JTable();
		tableSL.setPreferredScrollableViewportSize(new Dimension(150, 400));
		tableSL.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Name", "Description" })
		{
			boolean[] columnEditables = new boolean[] { false, false };

			@Override
			public boolean isCellEditable(int row, int column)
			{
				return columnEditables[column];
			}
		});
		tableSL.getColumnModel().getColumn(0).setPreferredWidth(20);
		tableSL.getColumnModel().getColumn(0).setMinWidth(20);
		tableSL.getColumnModel().getColumn(1).setPreferredWidth(60);
		tableSL.setFillsViewportHeight(true);
		tableSL.setShowGrid(false);
		tableSL.setShowHorizontalLines(false);
		tableSL.setShowVerticalLines(false);
		tableSL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPaneSL.setViewportView(tableSL);
		
		JPanel panelEntity = new JPanel();
		splitPane.setRightComponent(panelEntity);
		panelEntity.setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBarEntity = new JToolBar();
		panelEntity.add(toolBarEntity, BorderLayout.SOUTH);
		
		JToggleButton tglbtnBad = new JToggleButton("");
		tglbtnBad.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/icons/bullet_red.png")));
		tglbtnBad.setToolTipText("Bad");
		toolBarEntity.add(tglbtnBad);
		
		JToggleButton tglbtnOK = new JToggleButton("");
		tglbtnOK.setToolTipText("OK");
		tglbtnOK.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/icons/bullet_green.png")));
		toolBarEntity.add(tglbtnOK);
		
		JScrollPane scrollPaneEntity = new JScrollPane();
		panelEntity.add(scrollPaneEntity, BorderLayout.CENTER);
		
		tableEntity = new JTable();
		tableEntity.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Name", "Size", "CRC", "MD5", "SHA-1" })
		{
			Class<?>[] columnTypes = new Class[] { Object.class, Long.class, String.class, String.class, String.class };

			@Override
			public Class<?> getColumnClass(int columnIndex)
			{
				return columnTypes[columnIndex];
			}

			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		});
		tableEntity.setShowGrid(false);
		tableEntity.setShowHorizontalLines(false);
		tableEntity.setShowVerticalLines(false);
		tableEntity.setRowSelectionAllowed(false);
		tableEntity.setFillsViewportHeight(true);
		scrollPaneEntity.setViewportView(tableEntity);
	}

}
