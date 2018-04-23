package jrm.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import jrm.Messages;
import jrm.profile.Profile;
import jrm.profile.data.*;

@SuppressWarnings("serial")
public class ProfileViewer extends JDialog
{
	private JTable tableEntity;
	private JTable tableW;
	private JTable tableWL;

	public ProfileViewer(Window owner, Profile profile)
	{
		super(owner);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ProfileViewer.class.getResource("/jrm/resources/rom.png")));
		setTitle(Messages.getString("ProfileViewer.this.title")); //$NON-NLS-1$

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		JPanel panelWare = new JPanel();
		splitPane.setLeftComponent(panelWare);
		panelWare.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPaneWLW = new JSplitPane();
		splitPaneWLW.setOneTouchExpandable(true);
		splitPaneWLW.setContinuousLayout(true);
		splitPaneWLW.setResizeWeight(0.25);
		panelWare.add(splitPaneWLW, BorderLayout.CENTER);

		tableEntity = new JTable();
		tableEntity.setPreferredScrollableViewportSize(new Dimension(1200, 300));
		tableEntity.setShowGrid(false);
		tableEntity.setShowHorizontalLines(false);
		tableEntity.setShowVerticalLines(false);
		tableEntity.setRowSelectionAllowed(false);
		tableEntity.setFillsViewportHeight(true);
		tableEntity.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JPanel panelW = new JPanel();
		splitPaneWLW.setRightComponent(panelW);
		// panelWare.add(panelSL, BorderLayout.NORTH);
		panelW.setLayout(new BorderLayout(0, 0));

		JToolBar toolBarW = new JToolBar();
		panelW.add(toolBarW, BorderLayout.SOUTH);

		JToggleButton tglbtnMissingW = new JToggleButton("");
		tglbtnMissingW.setSelected(true);
		tglbtnMissingW.setToolTipText(Messages.getString("ProfileViewer.tglbtnMissingW.toolTipText")); //$NON-NLS-1$
		tglbtnMissingW.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/folder_closed_red.png")));
		toolBarW.add(tglbtnMissingW);

		JToggleButton tglbtnPartialW = new JToggleButton("");
		tglbtnPartialW.setSelected(true);
		tglbtnPartialW.setToolTipText(Messages.getString("ProfileViewer.tglbtnPartialW.toolTipText")); //$NON-NLS-1$
		tglbtnPartialW.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/folder_closed_orange.png")));
		toolBarW.add(tglbtnPartialW);

		JToggleButton tglbtnCompleteW = new JToggleButton("");
		tglbtnCompleteW.setSelected(true);
		tglbtnCompleteW.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/folder_closed_green.png")));
		tglbtnCompleteW.setToolTipText(Messages.getString("ProfileViewer.tglbtnCompleteW.toolTipText")); //$NON-NLS-1$
		toolBarW.add(tglbtnCompleteW);

		tglbtnMissingW.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterW(tglbtnMissingW.isSelected(), tglbtnPartialW.isSelected(), tglbtnCompleteW.isSelected());
			}
		});
		tglbtnPartialW.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterW(tglbtnMissingW.isSelected(), tglbtnPartialW.isSelected(), tglbtnCompleteW.isSelected());
			}
		});
		tglbtnCompleteW.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterW(tglbtnMissingW.isSelected(), tglbtnPartialW.isSelected(), tglbtnCompleteW.isSelected());
			}
		});

		JScrollPane scrollPaneW = new JScrollPane();
		panelW.add(scrollPaneW);

		tableW = new JTable();
		scrollPaneW.setViewportView(tableW);
		tableW.setPreferredScrollableViewportSize(new Dimension(600, 400));
		tableW.setFillsViewportHeight(true);
		tableW.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableW.setShowGrid(false);
		tableW.setShowHorizontalLines(false);
		tableW.setShowVerticalLines(false);
		tableW.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JPanel panel = new JPanel();
		splitPaneWLW.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPaneWL = new JScrollPane();
		panel.add(scrollPaneWL);

		tableWL = new JTable();
		scrollPaneWL.setViewportView(tableWL);
		tableWL.setPreferredScrollableViewportSize(new Dimension(300, 400));
		tableWL.setModel(profile.softwarelist_list.size() > 0 ? profile.softwarelist_list : profile.machinelist_list);
		tableWL.setFillsViewportHeight(true);
		tableWL.setShowGrid(false);
		tableWL.setShowHorizontalLines(false);
		tableWL.setShowVerticalLines(false);
		tableWL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JToolBar toolBarWL = new JToolBar();
		panel.add(toolBarWL, BorderLayout.SOUTH);

		JToggleButton tglbtnMissingWL = new JToggleButton("");
		tglbtnMissingWL.setSelected(true);
		tglbtnMissingWL.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/disk_multiple_red.png")));
		tglbtnMissingWL.setToolTipText(Messages.getString("ProfileViewer.tglbtnMissingWL.toolTipText")); //$NON-NLS-1$
		toolBarWL.add(tglbtnMissingWL);

		JToggleButton tglbtnPartialWL = new JToggleButton("");
		tglbtnPartialWL.setSelected(true);
		tglbtnPartialWL.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/disk_multiple_orange.png")));
		tglbtnPartialWL.setToolTipText(Messages.getString("ProfileViewer.tglbtnPartialWL.toolTipText")); //$NON-NLS-1$
		toolBarWL.add(tglbtnPartialWL);

		JToggleButton tglbtnCompleteWL = new JToggleButton("");
		tglbtnCompleteWL.setSelected(true);
		tglbtnCompleteWL.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/disk_multiple_green.png")));
		tglbtnCompleteWL.setToolTipText(Messages.getString("ProfileViewer.tglbtnCompleteWL.toolTipText")); //$NON-NLS-1$
		toolBarWL.add(tglbtnCompleteWL);

		tglbtnMissingWL.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterWL(tglbtnMissingWL.isSelected(), tglbtnPartialWL.isSelected(), tglbtnCompleteWL.isSelected());
			}
		});
		tglbtnPartialWL.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterWL(tglbtnMissingWL.isSelected(), tglbtnPartialWL.isSelected(), tglbtnCompleteWL.isSelected());
			}
		});
		tglbtnCompleteWL.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterWL(tglbtnMissingWL.isSelected(), tglbtnPartialWL.isSelected(), tglbtnCompleteWL.isSelected());
			}
		});

		tableWL.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting())
				{
					ListSelectionModel model = (ListSelectionModel) e.getSource();
					TableModel tablemodel = (TableModel) tableWL.getModel();
					if(model != null && tablemodel != null)
					{
						if(!model.isSelectionEmpty())
						{
							AnywareList<?> anywarelist = (AnywareList<?>) tablemodel.getValueAt(model.getMinSelectionIndex(), 0);
							anywarelist.reset();
							tableW.setModel(anywarelist);
							if(tableW.getRowCount() > 0)
								tableW.setRowSelectionInterval(0, 0);
							for(int i = 0; i < tableW.getColumnModel().getColumnCount(); i++)
							{
								TableColumn column = tableW.getColumnModel().getColumn(i);
								column.setCellRenderer(anywarelist.getColumnRenderer(i));
								int width = anywarelist.getColumnWidth(i);
								if(width > 0)
								{
									column.setMinWidth(width / 2);
									column.setPreferredWidth(width);
								}
								else if(width < 0)
								{
									column.setMinWidth(-width);
									column.setMaxWidth(-width);
								}
							}
						}
						else
						{
							tableW.setModel(new DefaultTableModel());
							tableEntity.setModel(new DefaultTableModel());
						}
					}
				}
			}
		});
		tableW.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() == 2)
				{
					JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					if(row >= 0)
					{
						AnywareList<?> tablemodel = (AnywareList<?>) target.getModel();
						Object obj = tablemodel.getValueAt(row, target.columnAtPoint(e.getPoint()));
						if(obj instanceof Anyware)
						{
							row = tablemodel.find((Anyware) obj);
							if(row >= 0)
							{
								target.setRowSelectionInterval(row, row);
								target.scrollRectToVisible(target.getCellRect(row, 0, true));
							}
						}
					}
				}
			}
		});
		tableW.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting())
				{
					ListSelectionModel model = (ListSelectionModel) e.getSource();
					TableModel tablemodel = (TableModel) tableW.getModel();
					if(model != null && tablemodel != null)
					{
						if(!model.isSelectionEmpty())
						{
							Anyware anyware = (Anyware) tablemodel.getValueAt(model.getMinSelectionIndex(), 0);
							anyware.reset();
							tableEntity.setModel(anyware);
							for(int i = 0; i < tableEntity.getColumnModel().getColumnCount(); i++)
							{
								TableColumn column = tableEntity.getColumnModel().getColumn(i);
								column.setCellRenderer(Anyware.getColumnRenderer(i));
								int width = Anyware.getColumnWidth(i);
								if(width > 0)
								{
									column.setMinWidth(width / 2);
									column.setPreferredWidth(width);
								}
								else if(width < 0)
								{
									column.setMinWidth(-width);
									column.setMaxWidth(-width);
								}
							}
						}
						else
						{
							tableEntity.setModel(new DefaultTableModel());
						}
					}
				}
			}
		});

		JPanel panelEntity = new JPanel();
		splitPane.setRightComponent(panelEntity);
		panelEntity.setLayout(new BorderLayout(0, 0));

		JToolBar toolBarEntity = new JToolBar();
		panelEntity.add(toolBarEntity, BorderLayout.SOUTH);

		JToggleButton tglbtnBad = new JToggleButton("");
		tglbtnBad.setSelected(true);
		tglbtnBad.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/icons/bullet_red.png")));
		tglbtnBad.setToolTipText(Messages.getString("ProfileViewer.tglbtnBad.toolTipText")); //$NON-NLS-1$
		toolBarEntity.add(tglbtnBad);

		JToggleButton tglbtnOK = new JToggleButton("");
		tglbtnOK.setSelected(true);
		tglbtnOK.setToolTipText(Messages.getString("ProfileViewer.tglbtnOK.toolTipText")); //$NON-NLS-1$
		tglbtnOK.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/icons/bullet_green.png")));
		toolBarEntity.add(tglbtnOK);

		tglbtnBad.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterE(tglbtnBad.isSelected(), tglbtnOK.isSelected());
			}
		});
		tglbtnOK.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterE(tglbtnBad.isSelected(), tglbtnOK.isSelected());
			}
		});

		JScrollPane scrollPaneEntity = new JScrollPane();
		panelEntity.add(scrollPaneEntity, BorderLayout.CENTER);

		scrollPaneEntity.setViewportView(tableEntity);

		reset(profile);
		pack();
		setVisible(true);
	}

	public void setFilterWL(boolean missing, boolean partial, boolean complete)
	{
		EnumSet<AnywareStatus> filter = EnumSet.of(AnywareStatus.UNKNOWN);
		if(missing)
			filter.add(AnywareStatus.MISSING);
		if(partial)
			filter.add(AnywareStatus.PARTIAL);
		if(complete)
			filter.add(AnywareStatus.COMPLETE);
		((AnywareListList<?>) tableWL.getModel()).setFilter(filter);
		if(tableWL.getRowCount() > 0)
			tableWL.setRowSelectionInterval(0, 0);
	}

	public void setFilterW(boolean missing, boolean partial, boolean complete)
	{
		EnumSet<AnywareStatus> filter = EnumSet.of(AnywareStatus.UNKNOWN);
		if(missing)
			filter.add(AnywareStatus.MISSING);
		if(partial)
			filter.add(AnywareStatus.PARTIAL);
		if(complete)
			filter.add(AnywareStatus.COMPLETE);
		((AnywareList<?>) tableW.getModel()).setFilter(filter);
		if(tableW.getRowCount() > 0)
			tableW.setRowSelectionInterval(0, 0);
	}

	public void setFilterE(boolean ko, boolean ok)
	{
		EnumSet<EntityStatus> filter = EnumSet.of(EntityStatus.UNKNOWN);
		if(ko)
			filter.add(EntityStatus.KO);
		if(ok)
			filter.add(EntityStatus.OK);
		((Anyware) tableEntity.getModel()).setFilter(filter);
		if(tableEntity.getRowCount() > 0)
			tableEntity.setRowSelectionInterval(0, 0);
	}

	public void clear()
	{
		tableEntity.setModel(new DefaultTableModel());
		tableW.setModel(new DefaultTableModel());
		tableWL.setModel(new DefaultTableModel());
	}

	public void reset(Profile profile)
	{
		AnywareListList<?> model = profile.softwarelist_list.size() > 0 ? profile.softwarelist_list : profile.machinelist_list;
		model.reset();
		tableWL.setModel(model);
		for(int i = 0; i < tableWL.getColumnModel().getColumnCount(); i++)
		{
			TableColumn column = tableWL.getColumnModel().getColumn(i);
			column.setCellRenderer(model.getColumnRenderer(i));
			int width = model.getColumnWidth(i);
			if(width > 0)
			{
				column.setMinWidth(width / 2);
				column.setPreferredWidth(width);
			}
			else if(width < 0)
			{
				column.setMinWidth(-width);
				column.setMaxWidth(-width);
			}
		}
		if(tableWL.getRowCount()>0)
			tableWL.setRowSelectionInterval(0, 0);
	}

	public void reload()
	{
		TableModel tablemodel = (TableModel) tableWL.getModel();
		if(tablemodel instanceof AnywareListList<?>)
			((AnywareListList<?>) tablemodel).fireTableChanged(new TableModelEvent(tablemodel, 0, ((AnywareListList<?>) tablemodel).getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
		tablemodel = (TableModel) tableW.getModel();
		if(tablemodel instanceof AnywareList<?>)
			((AnywareList<?>) tablemodel).fireTableChanged(new TableModelEvent(tablemodel, 0, ((AnywareList<?>) tablemodel).getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
		tablemodel = (TableModel) tableEntity.getModel();
		if(tablemodel instanceof Anyware)
			((Anyware) tablemodel).fireTableChanged(new TableModelEvent(tablemodel, 0, ((Anyware) tablemodel).getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
	}

}
