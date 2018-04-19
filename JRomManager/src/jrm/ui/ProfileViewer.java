package jrm.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import jrm.profiler.Profile;
import jrm.profiler.data.*;

@SuppressWarnings("serial")
public class ProfileViewer extends JDialog
{
	private JTable tableEntity;
	private JTable tableS;
	public ProfileViewer(Window owner, Profile profile) {
		super(owner);
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
		
		tableEntity = new JTable();
		tableEntity.setPreferredScrollableViewportSize(new Dimension(1000, 300));
		tableEntity.setShowGrid(false);
		tableEntity.setShowHorizontalLines(false);
		tableEntity.setShowVerticalLines(false);
		tableEntity.setRowSelectionAllowed(false);
		tableEntity.setFillsViewportHeight(true);
		tableEntity.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		tableS = new JTable();
		tableS.setPreferredScrollableViewportSize(new Dimension(500, 400));
		tableS.setFillsViewportHeight(true);
		tableS.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableS.setShowGrid(false);
		tableS.setShowHorizontalLines(false);
		tableS.setShowVerticalLines(false);
		tableS.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tableS.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting())
				{
					ListSelectionModel model = (ListSelectionModel)e.getSource();
					TableModel tablemodel = (TableModel)tableS.getModel();
					if(model != null && tablemodel != null && !model.isSelectionEmpty())
					{
						if(tablemodel instanceof SoftwareList)
							tableEntity.setModel(((SoftwareList) tablemodel).get(model.getMinSelectionIndex()));
						else
							tableEntity.setModel(((MachineList) tablemodel).get(model.getMinSelectionIndex()));
						for(int i = 0; i < tableEntity.getColumnModel().getColumnCount(); i++)
						{
							TableColumn column = tableEntity.getColumnModel().getColumn(i);
							column.setCellRenderer(Anyware.getColumnRenderer(i));
							int width = Anyware.getColumnWidth(i);
							if(width>0)
								column.setPreferredWidth(width);
						}
					}
				}
			}
		});
		scrollPaneS.setViewportView(tableS);
		
		JScrollPane scrollPaneSL = new JScrollPane();
		splitPaneSLS.setLeftComponent(scrollPaneSL);
		
		JTable tableSL = new JTable();
		tableSL.setPreferredScrollableViewportSize(new Dimension(200, 400));
		tableSL.setModel(profile.softwarelist_list.size()>0?profile.softwarelist_list:profile.machinelist_list);
		tableSL.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableSL.getColumnModel().getColumn(1).setPreferredWidth(150);
		tableSL.setFillsViewportHeight(true);
		tableSL.setShowGrid(false);
		tableSL.setShowHorizontalLines(false);
		tableSL.setShowVerticalLines(false);
		tableSL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableSL.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting())
				{
					ListSelectionModel model = (ListSelectionModel)e.getSource();
					TableModel tablemodel = (TableModel)tableSL.getModel();
					if(model != null && tablemodel != null && !model.isSelectionEmpty())
					{
						if(tablemodel instanceof SoftwareListList)
							tableS.setModel(((SoftwareListList)tablemodel).get(model.getMinSelectionIndex()));
						else
							tableS.setModel(((MachineListList)tablemodel).get(model.getMinSelectionIndex()));
						if(tableS.getRowCount()>0)
							tableS.setRowSelectionInterval(0, 0);
						for(int i = 0; i < tableS.getColumnModel().getColumnCount(); i++)
						{
							TableColumn column = tableS.getColumnModel().getColumn(i);
							column.setCellRenderer(AnywareList.getColumnRenderer(i));
							int width = AnywareList.getColumnWidth(i);
							if(width>0)
							{
								column.setMinWidth(width/2);
								column.setPreferredWidth(width);
							}
							else if(width<0)
							{
								column.setMinWidth(-width);
								column.setMaxWidth(-width);
							}
						}
					}
				}
			}
		});
		tableSL.setRowSelectionInterval(0, 0);
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
		
		scrollPaneEntity.setViewportView(tableEntity);
		
		pack();
		setVisible(true);
	}

}
