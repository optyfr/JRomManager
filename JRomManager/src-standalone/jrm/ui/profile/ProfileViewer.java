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
package jrm.ui.profile;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.SerializationUtils;

import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.AnywareListList;
import jrm.profile.data.AnywareStatus;
import jrm.profile.data.Device;
import jrm.profile.data.EntityStatus;
import jrm.profile.data.Machine;
import jrm.profile.data.MachineList;
import jrm.profile.data.Software;
import jrm.profile.data.SoftwareList;
import jrm.profile.manager.Export;
import jrm.profile.manager.Export.ExportType;
import jrm.profile.manager.ProfileNFOMame;
import jrm.profile.manager.ProfileNFOMame.MameStatus;
import jrm.security.Session;
import jrm.ui.basic.JRMFileChooser;
import jrm.ui.profile.data.AnywareListListModel;
import jrm.ui.profile.data.AnywareListModel;
import jrm.ui.profile.data.AnywareModel;
import jrm.ui.profile.data.MachineListListModel;
import jrm.ui.profile.data.MachineListModel;
import jrm.ui.profile.data.SoftwareListModel;
import jrm.ui.profile.filter.KeywordFilter;
import jrm.ui.progress.Progress;

// TODO: Auto-generated Javadoc
/**
 * The Class ProfileViewer.
 */
@SuppressWarnings("serial")
public class ProfileViewer extends JDialog
{
	
	/** The table entity. */
	private JTable tableEntity;
	
	/** The table W. */
	private JTable tableW;
	
	/** The table WL. */
	private JTable tableWL;
	
	/** The txt search. */
	private JTextField txtSearch;

	/**
	 * The Class keypref.
	 */
	private class keypref
	{
		
		/** The order. */
		int order;
		
		/** The wares. */
		List<Anyware> wares = new ArrayList<>();

		/**
		 * Instantiates a new keypref.
		 *
		 * @param order the order
		 * @param ware the ware
		 */
		private keypref(int order, Anyware ware)
		{
			this.order = order;
			add(ware);
		}
		
		/**
		 * Adds the.
		 *
		 * @param ware the ware
		 */
		private void add(Anyware ware)
		{
			ware.selected = true;
			this.wares.add(ware);
		}
		
		/**
		 * Clear.
		 */
		private void clear()
		{
			wares.forEach(w->w.selected=false);
			wares.clear();
		}
	}

	/**
	 * Instantiates a new profile viewer.
	 *
	 * @param owner the owner
	 * @param profile the profile
	 */
	public ProfileViewer(final Session session, final Window owner, final Profile profile)
	{
		super();
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				session.getUser().settings.setProperty("ProfileViewer.Bounds", Hex.encodeHexString(SerializationUtils.serialize(getBounds()))); //$NON-NLS-1$
			}
		});
		setIconImage(Toolkit.getDefaultToolkit().getImage(ProfileViewer.class.getResource("/jrm/resicons/rom.png"))); //$NON-NLS-1$
		setTitle(Messages.getString("ProfileViewer.this.title")); //$NON-NLS-1$

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		final JPanel panelWare = new JPanel();
		splitPane.setLeftComponent(panelWare);
		panelWare.setLayout(new BorderLayout(0, 0));

		final JSplitPane splitPaneWLW = new JSplitPane();
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

		final JPanel panelW = new JPanel();
		splitPaneWLW.setRightComponent(panelW);
		// panelWare.add(panelSL, BorderLayout.NORTH);
		panelW.setLayout(new BorderLayout(0, 0));

		final JToolBar toolBarW = new JToolBar();
		panelW.add(toolBarW, BorderLayout.SOUTH);

		final JToggleButton tglbtnMissingW = new JToggleButton(""); //$NON-NLS-1$
		tglbtnMissingW.setSelected(true);
		tglbtnMissingW.setToolTipText(Messages.getString("ProfileViewer.tglbtnMissingW.toolTipText")); //$NON-NLS-1$
		tglbtnMissingW.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resicons/folder_closed_red.png"))); //$NON-NLS-1$
		toolBarW.add(tglbtnMissingW);

		final JToggleButton tglbtnPartialW = new JToggleButton(""); //$NON-NLS-1$
		tglbtnPartialW.setSelected(true);
		tglbtnPartialW.setToolTipText(Messages.getString("ProfileViewer.tglbtnPartialW.toolTipText")); //$NON-NLS-1$
		tglbtnPartialW.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resicons/folder_closed_orange.png"))); //$NON-NLS-1$
		toolBarW.add(tglbtnPartialW);

		final JToggleButton tglbtnCompleteW = new JToggleButton(""); //$NON-NLS-1$
		tglbtnCompleteW.setSelected(true);
		tglbtnCompleteW.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resicons/folder_closed_green.png"))); //$NON-NLS-1$
		tglbtnCompleteW.setToolTipText(Messages.getString("ProfileViewer.tglbtnCompleteW.toolTipText")); //$NON-NLS-1$
		toolBarW.add(tglbtnCompleteW);

		final JPanel panel_1 = new JPanel();
		panel_1.setBorder(null);
		toolBarW.add(panel_1);
		final GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 286, 166, 0 };
		gbl_panel_1.rowHeights = new int[] { 20, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		final JLabel lblSearch = new JLabel(Messages.getString("ProfileViewer.lblSearch.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblSearch = new GridBagConstraints();
		gbc_lblSearch.insets = new Insets(0, 0, 0, 5);
		gbc_lblSearch.anchor = GridBagConstraints.EAST;
		gbc_lblSearch.gridx = 0;
		gbc_lblSearch.gridy = 0;
		panel_1.add(lblSearch, gbc_lblSearch);

		txtSearch = new JTextField();
		txtSearch.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(final KeyEvent e)
			{
				final String search = txtSearch.getText();
				@SuppressWarnings("unchecked")
				final int row = ((AnywareList<Anyware>) tableW.getModel()).find(search);
				if (row >= 0)
				{
					tableW.setRowSelectionInterval(row, row);
					tableW.scrollRectToVisible(tableW.getCellRect(row, 0, true));
				}
			}
		});
		final GridBagConstraints gbc_txtSearch = new GridBagConstraints();
		gbc_txtSearch.fill = GridBagConstraints.VERTICAL;
		gbc_txtSearch.anchor = GridBagConstraints.WEST;
		gbc_txtSearch.gridx = 1;
		gbc_txtSearch.gridy = 0;
		panel_1.add(txtSearch, gbc_txtSearch);
		txtSearch.setText(""); //$NON-NLS-1$
		txtSearch.setColumns(20);

		tglbtnMissingW.addItemListener(e -> setFilterW(tglbtnMissingW.isSelected(), tglbtnPartialW.isSelected(), tglbtnCompleteW.isSelected()));
		tglbtnPartialW.addItemListener(e -> setFilterW(tglbtnMissingW.isSelected(), tglbtnPartialW.isSelected(), tglbtnCompleteW.isSelected()));
		tglbtnCompleteW.addItemListener(e -> setFilterW(tglbtnMissingW.isSelected(), tglbtnPartialW.isSelected(), tglbtnCompleteW.isSelected()));

		final JScrollPane scrollPaneW = new JScrollPane();
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

		JPopupMenu popupWMenu = new JPopupMenu();
		addPopup(tableW, popupWMenu);

		JMenuItem mntmCollectKeywords = new JMenuItem(Messages.getString("ProfileViewer.mntmCollectKeywords.text")); //$NON-NLS-1$
		mntmCollectKeywords.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final AnywareListModel list = (AnywareListModel) tableW.getModel();
				final Pattern pattern = Pattern.compile("^(.*?)(\\(.*\\))+"); //$NON-NLS-1$
				final Pattern pattern_parenthesis = Pattern.compile("\\((.*?)\\)"); //$NON-NLS-1$
				final Pattern pattern_split = Pattern.compile(","); //$NON-NLS-1$
				final Pattern pattern_alpha = Pattern.compile("^[a-zA-Z]*$"); //$NON-NLS-1$
				final HashSet<String> keywords = new HashSet<>();
				list.getList().getFilteredStream().forEach(ware -> {
					final Matcher matcher = pattern.matcher(ware.getDescription());
					if (matcher.find())
					{
						if (matcher.groupCount() > 1 && matcher.group(2) != null)
						{
							final Matcher matcher_parenthesis = pattern_parenthesis.matcher(matcher.group(2));
							while (matcher_parenthesis.find())
							{
								Arrays.asList(pattern_split.split(matcher_parenthesis.group(1))).stream().map(s -> s.trim().toLowerCase()).filter(pattern_alpha.asPredicate()).forEach(keywords::add);
							}
						}
					}
				});
				new KeywordFilter(ProfileViewer.this, keywords.stream().sorted((s1, s2) -> {
					return s1.length() == s2.length() ? s1.compareToIgnoreCase(s2) : s1.length() - s2.length();
				}).toArray(size -> new String[size]), f -> {
					ArrayList<String> filter = f.getFilter();
					HashMap<String, keypref> prefmap = new HashMap<>();
					list.getList().getFilteredStream().forEach(ware -> {
						final Matcher matcher = pattern.matcher(ware.getDescription());
						keywords.clear();
						if (matcher.find())
						{
							if (matcher.groupCount() > 1 && matcher.group(2) != null)
							{
								final Matcher matcher_parenthesis = pattern_parenthesis.matcher(matcher.group(2));
								while (matcher_parenthesis.find())
								{
									Arrays.asList(pattern_split.split(matcher_parenthesis.group(1))).stream().map(s -> s.trim().toLowerCase()).filter(pattern_alpha.asPredicate()).forEach(keywords::add);
								}
							}
							ware.selected = false;
							for (int i = 0; i < filter.size(); i++)
							{
								if (keywords.contains(filter.get(i)))
								{
									if (prefmap.containsKey(matcher.group(1)))
									{
										keypref pref = prefmap.get(matcher.group(1));
										if (i < pref.order)
										{
											pref.clear();
											prefmap.put(matcher.group(1), new keypref(i, ware));
										}
										else if (i == pref.order)
											pref.add(ware);
									}
									else
										prefmap.put(matcher.group(1), new keypref(i, ware));

									break;
								}
							}
						}
						else
						{
							if (!prefmap.containsKey(ware.getDescription().toString()))
								prefmap.put(ware.getDescription().toString(), new keypref(Integer.MAX_VALUE, ware));
						}
					});
					list.fireTableChanged(new TableModelEvent(list, 0, list.getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
				});
			}
		});
		popupWMenu.add(mntmCollectKeywords);

		JMenuItem mntmSelectNone = new JMenuItem(Messages.getString("ProfileViewer.mntmSelectNone.text")); //$NON-NLS-1$
		mntmSelectNone.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final AnywareListModel list = (AnywareListModel) tableW.getModel();
				list.getList().getFilteredStream().forEach(ware -> {
					ware.selected = false;
				});
				list.fireTableChanged(new TableModelEvent(list, 0, list.getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
			}
		});
		popupWMenu.add(mntmSelectNone);

		JMenuItem mntmSelectAll = new JMenuItem(Messages.getString("ProfileViewer.mntmSelectAll.text")); //$NON-NLS-1$
		mntmSelectAll.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final AnywareListModel list = (AnywareListModel) tableW.getModel();
				list.getList().getFilteredStream().forEach(ware -> {
					ware.selected = true;
				});
				list.fireTableChanged(new TableModelEvent(list, 0, list.getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
			}
		});
		popupWMenu.add(mntmSelectAll);

		JMenuItem mntmSelectInvert = new JMenuItem(Messages.getString("ProfileViewer.mntmSelectInvert.text")); //$NON-NLS-1$
		mntmSelectInvert.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final AnywareListModel list = (AnywareListModel) tableW.getModel();
				list.getList().getFilteredStream().forEach(ware -> {
					ware.selected ^= true;
				});
				list.fireTableChanged(new TableModelEvent(list, 0, list.getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
			}
		});
		popupWMenu.add(mntmSelectInvert);

		final JPanel panel = new JPanel();
		splitPaneWLW.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		final JScrollPane scrollPaneWL = new JScrollPane();
		panel.add(scrollPaneWL);

		tableWL = new JTable();
		scrollPaneWL.setViewportView(tableWL);
		tableWL.setPreferredScrollableViewportSize(new Dimension(300, 400));
		tableWL.setModel(new MachineListListModel(profile.machinelist_list));
		tableWL.setTableHeader(new JTableHeader(tableWL.getColumnModel())
		{
			@Override
			public String getToolTipText(final MouseEvent e)
			{
				return columnModel.getColumn(columnModel.getColumnIndexAtX(e.getPoint().x)).getHeaderValue().toString();
			}
		});
		tableWL.setFillsViewportHeight(true);
		tableWL.setShowGrid(false);
		tableWL.setShowHorizontalLines(false);
		tableWL.setShowVerticalLines(false);
		tableWL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		final JPopupMenu popupMenu = new JPopupMenu();
		ProfileViewer.addPopup(tableWL, popupMenu);

		final JMenu mnExportAll = new JMenu(Messages.getString("ProfileViewer.ExportAll")); //$NON-NLS-1$
		popupMenu.add(mnExportAll);

		final JMenu mnExportAllFiltered = new JMenu(Messages.getString("ProfileViewer.Filtered")); //$NON-NLS-1$
		mnExportAll.add(mnExportAllFiltered);

		final JMenuItem mntmFilteredAsLogiqxDat = new JMenuItem(Messages.getString("ProfileViewer.AsLogiqxDat")); //$NON-NLS-1$
		mnExportAllFiltered.add(mntmFilteredAsLogiqxDat);
		mntmFilteredAsLogiqxDat.addActionListener(e -> export(session, ExportType.DATAFILE, true, null));

		final JMenuItem mntmFilteredAsMameDat = new JMenuItem(Messages.getString("ProfileViewer.AsMameDat")); //$NON-NLS-1$
		mnExportAllFiltered.add(mntmFilteredAsMameDat);
		mntmFilteredAsMameDat.addActionListener(e -> export(session, ExportType.MAME, true, null));

		final JMenuItem mntmFilteredAsSoftwareLists = new JMenuItem(Messages.getString("ProfileViewer.AsSWListsDat")); //$NON-NLS-1$
		mnExportAllFiltered.add(mntmFilteredAsSoftwareLists);
		mntmFilteredAsSoftwareLists.addActionListener(e -> export(session, ExportType.SOFTWARELIST, true, null));

		final JMenuItem mntmAllAsLogiqxDat = new JMenuItem(Messages.getString("ProfileViewer.AsLogiqxDat")); //$NON-NLS-1$
		mntmAllAsLogiqxDat.setEnabled(false);
		mnExportAll.add(mntmAllAsLogiqxDat);
		mntmAllAsLogiqxDat.addActionListener(e -> export(session, ExportType.DATAFILE, false, null));

		final JMenuItem mntmAllAsMameDat = new JMenuItem(Messages.getString("ProfileViewer.AsMameDat")); //$NON-NLS-1$
		mntmAllAsMameDat.setEnabled(false);
		mnExportAll.add(mntmAllAsMameDat);
		mntmAllAsMameDat.addActionListener(e -> export(session, ExportType.MAME, false, null));

		final JMenuItem mntmAllAsSoftwareLists = new JMenuItem(Messages.getString("ProfileViewer.AsSWListsDat")); //$NON-NLS-1$
		mntmAllAsSoftwareLists.setEnabled(false);
		mnExportAll.add(mntmAllAsSoftwareLists);
		mntmAllAsSoftwareLists.addActionListener(e -> export(session, ExportType.SOFTWARELIST, false, null));

		final JMenu mnExportSelected = new JMenu(Messages.getString("ProfileViewer.ExportSelected")); //$NON-NLS-1$
		popupMenu.add(mnExportSelected);

		final JMenu mnExportSelectedFiltered = new JMenu(Messages.getString("ProfileViewer.Filtered")); //$NON-NLS-1$
		mnExportSelected.add(mnExportSelectedFiltered);

		final JMenuItem mntmSelectedFilteredAsSoftwareList = new JMenuItem(Messages.getString("ProfileViewer.AsSWListDat")); //$NON-NLS-1$
		mnExportSelectedFiltered.add(mntmSelectedFilteredAsSoftwareList);
		mntmSelectedFilteredAsSoftwareList.addActionListener(e -> export(session, ExportType.SOFTWARELIST, true, (SoftwareList) tableWL.getModel().getValueAt(tableWL.getSelectedRow(), 0)));

		final JMenuItem mntmSelectedAsSoftwareLists = new JMenuItem(Messages.getString("ProfileViewer.AsSWListsDat")); //$NON-NLS-1$
		mntmSelectedAsSoftwareLists.setEnabled(false);
		mnExportSelected.add(mntmSelectedAsSoftwareLists);
		mntmSelectedAsSoftwareLists.addActionListener(e -> export(session, ExportType.SOFTWARELIST, false, (SoftwareList) tableWL.getModel().getValueAt(tableWL.getSelectedRow(), 0)));

		popupMenu.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuCanceled(final PopupMenuEvent e)
			{
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e)
			{
			}

			@Override
			public void popupMenuWillBecomeVisible(final PopupMenuEvent e)
			{
				final boolean has_machines = session.curr_profile.machinelist_list.getList().stream().mapToInt(ml -> ml.getList().size()).sum() > 0;
				final boolean has_filtered_machines = session.curr_profile.machinelist_list.getFilteredStream().mapToInt(m -> (int) m.countAll()).sum() > 0;
				final boolean has_selected_swlist = tableWL.getSelectedRowCount() == 1 && tableWL.getModel() instanceof AnywareListList<?> && ((MachineListListModel) tableWL.getModel()).getValueAt(tableWL.getSelectedRow(), 0) instanceof SoftwareList;
				mntmAllAsMameDat.setEnabled(has_machines);
				mntmAllAsLogiqxDat.setEnabled(has_machines);
				mntmAllAsSoftwareLists.setEnabled(session.curr_profile.machinelist_list.softwarelist_list.size() > 0);
				mntmFilteredAsMameDat.setEnabled(has_filtered_machines);
				mntmFilteredAsLogiqxDat.setEnabled(has_filtered_machines);
				mntmFilteredAsSoftwareLists.setEnabled(session.curr_profile.machinelist_list.softwarelist_list.getFilteredStream().count() > 0);
				mntmSelectedAsSoftwareLists.setEnabled(has_selected_swlist);
				mntmSelectedFilteredAsSoftwareList.setEnabled(has_selected_swlist);
			}
		});

		final JToolBar toolBarWL = new JToolBar();
		panel.add(toolBarWL, BorderLayout.SOUTH);

		final JToggleButton tglbtnMissingWL = new JToggleButton(""); //$NON-NLS-1$
		tglbtnMissingWL.setSelected(true);
		tglbtnMissingWL.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resicons/disk_multiple_red.png"))); //$NON-NLS-1$
		tglbtnMissingWL.setToolTipText(Messages.getString("ProfileViewer.tglbtnMissingWL.toolTipText")); //$NON-NLS-1$
		toolBarWL.add(tglbtnMissingWL);

		final JToggleButton tglbtnPartialWL = new JToggleButton(""); //$NON-NLS-1$
		tglbtnPartialWL.setSelected(true);
		tglbtnPartialWL.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resicons/disk_multiple_orange.png"))); //$NON-NLS-1$
		tglbtnPartialWL.setToolTipText(Messages.getString("ProfileViewer.tglbtnPartialWL.toolTipText")); //$NON-NLS-1$
		toolBarWL.add(tglbtnPartialWL);

		final JToggleButton tglbtnCompleteWL = new JToggleButton(""); //$NON-NLS-1$
		tglbtnCompleteWL.setSelected(true);
		tglbtnCompleteWL.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resicons/disk_multiple_green.png"))); //$NON-NLS-1$
		tglbtnCompleteWL.setToolTipText(Messages.getString("ProfileViewer.tglbtnCompleteWL.toolTipText")); //$NON-NLS-1$
		toolBarWL.add(tglbtnCompleteWL);

		tglbtnMissingWL.addItemListener(e -> setFilterWL(tglbtnMissingWL.isSelected(), tglbtnPartialWL.isSelected(), tglbtnCompleteWL.isSelected()));
		tglbtnPartialWL.addItemListener(e -> setFilterWL(tglbtnMissingWL.isSelected(), tglbtnPartialWL.isSelected(), tglbtnCompleteWL.isSelected()));
		tglbtnCompleteWL.addItemListener(e -> setFilterWL(tglbtnMissingWL.isSelected(), tglbtnPartialWL.isSelected(), tglbtnCompleteWL.isSelected()));

		tableWL.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting())
			{
				final ListSelectionModel model = (ListSelectionModel) e.getSource();
				final TableModel tablemodel = tableWL.getModel();
				if (model != null && tablemodel != null)
				{
					if (!model.isSelectionEmpty())
					{
						final AnywareList<?> awlist = (AnywareList<?>) tablemodel.getValueAt(model.getMinSelectionIndex(), 0);
						final AnywareListModel anywarelist = awlist instanceof MachineList ? new MachineListModel((MachineList)awlist) : new SoftwareListModel((SoftwareList)awlist); 
						anywarelist.reset();
						tableW.setModel(anywarelist);
						tableW.setTableHeader(new JTableHeader(tableW.getColumnModel())
						{
							@Override
							public String getToolTipText(final MouseEvent e)
							{
								return columnModel.getColumn(columnModel.getColumnIndexAtX(e.getPoint().x)).getHeaderValue().toString();
							}
						});
						if (tableW.getRowCount() > 0)
							tableW.setRowSelectionInterval(0, 0);
						for (int i = 0; i < tableW.getColumnModel().getColumnCount(); i++)
						{
							final TableColumn column = tableW.getColumnModel().getColumn(i);
							column.setCellRenderer(anywarelist.getColumnRenderer(i));
							final int width = anywarelist.getColumnWidth(i);
							if (width > 0)
							{
								column.setMinWidth(width / 2);
								column.setPreferredWidth(width);
							}
							else if (width < 0)
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
		});
		tableW.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(final MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					final JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					if (row >= 0)
					{
						final AnywareListModel tablemodel = (AnywareListModel) target.getModel();
						final int column = target.columnAtPoint(e.getPoint());
						final Object obj = tablemodel.getValueAt(row, column);
						if (obj instanceof Anyware)
						{
							final Anyware ware = (Anyware) obj;
							if (column > 1)
							{
								row = tablemodel.getList().find(ware);
								if (row >= 0)
								{
									target.setRowSelectionInterval(row, row);
									target.scrollRectToVisible(target.getCellRect(row, 0, true));
								}
							}
							else if (ware.getStatus() == AnywareStatus.COMPLETE)
							{
								if (session.curr_profile != null)
								{
									final Profile profile = session.curr_profile;
									if (profile.nfo.mame.getStatus() == MameStatus.UPTODATE)
									{
										final ProfileNFOMame mame = profile.nfo.mame;
										String[] args = null;
										if (ware instanceof Software)
										{
											final List<String> rompaths = new ArrayList<>(Collections.singletonList(profile.getProperty(SettingsEnum.roms_dest_dir, ""))); //$NON-NLS-1$ //$NON-NLS-2$
											if (profile.getProperty(SettingsEnum.swroms_dest_dir_enabled, false)) //$NON-NLS-1$
												rompaths.add(profile.getProperty(SettingsEnum.swroms_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
											if (profile.getProperty(SettingsEnum.disks_dest_dir_enabled, false)) //$NON-NLS-1$
												rompaths.add(profile.getProperty(SettingsEnum.disks_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
											if (profile.getProperty(SettingsEnum.swdisks_dest_dir_enabled, false)) //$NON-NLS-1$
												rompaths.add(profile.getProperty(SettingsEnum.swdisks_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
											Log.debug(()->((Software) ware).sl.getBaseName() + ", " + ((Software) ware).compatibility); //$NON-NLS-1$
											JList<Machine> machines = new JList<Machine>(profile.machinelist_list.getSortedMachines(((Software) ware).sl.getBaseName(), ((Software) ware).compatibility).toArray(new Machine[0]));
											machines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
											if (machines.getModel().getSize() > 0)
												machines.setSelectedIndex(0);
											JOptionPane.showMessageDialog(ProfileViewer.this, machines);
											final Machine machine = machines.getSelectedValue();
											if (machine != null)
											{
												StringBuffer device = new StringBuffer(); //$NON-NLS-1$
												for(Device dev : machine.devices)
												{
													if(Objects.equals(((Software) ware).parts.get(0).intrface,dev.intrface))
													{
														if(dev.instance!=null)
														{
															device.append("-" + dev.instance.name); //$NON-NLS-1$
															break;
														}
													}
												}
												Log.debug(()->"-> " + machine.getBaseName() + " " + device + " " + ware.getBaseName()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
												args = new String[] { mame.getFile().getAbsolutePath(), machine.getBaseName(), device.toString(), ware.getBaseName(), "-homepath", mame.getFile().getParent(), "-rompath", rompaths.stream().collect(Collectors.joining(";")) }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											}
										}
										else
										{
											final List<String> rompaths = new ArrayList<>(Collections.singletonList(profile.getProperty(SettingsEnum.roms_dest_dir, ""))); //$NON-NLS-1$ //$NON-NLS-2$
											if (profile.getProperty(SettingsEnum.disks_dest_dir_enabled, false)) //$NON-NLS-1$
												rompaths.add(profile.getProperty(SettingsEnum.disks_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
											args = new String[] { mame.getFile().getAbsolutePath(), ware.getBaseName(), "-homepath", mame.getFile().getParent(), "-rompath", rompaths.stream().collect(Collectors.joining(";")) }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
										}
										if (args != null)
										{
											final ProcessBuilder pb = new ProcessBuilder(args).directory(mame.getFile().getParentFile()).redirectErrorStream(true).redirectOutput(new File(mame.getFile().getParentFile(), "JRomManager.log")); //$NON-NLS-1$
											try
											{
												pb.start().waitFor();
											}
											catch (InterruptedException | IOException e1)
											{
												JOptionPane.showMessageDialog(ProfileViewer.this, e1.getMessage(), Messages.getString("ProfileViewer.Exception"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
											}
										}
									}
									else
									{
										JOptionPane.showMessageDialog(ProfileViewer.this, String.format(Messages.getString("ProfileViewer.MameNotAvailableOrObsolete"), profile.nfo.mame.getStatus()), Messages.getString("ProfileViewer.Error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
									}
								}
								else
								{
									JOptionPane.showMessageDialog(ProfileViewer.this, Messages.getString("ProfileViewer.NoProfile"), Messages.getString("ProfileViewer.Error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
								}
							}
							else
							{
								JOptionPane.showMessageDialog(ProfileViewer.this, String.format(Messages.getString("ProfileViewer.CantLaunchIncompleteSet"), ware.getStatus()), Messages.getString("ProfileViewer.Error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
							}
						}
					}
				}
			}
		});
		tableW.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting())
			{
				final ListSelectionModel model = (ListSelectionModel) e.getSource();
				final TableModel tablemodel = tableW.getModel();
				if (model != null && tablemodel != null)
				{
					if (!model.isSelectionEmpty())
					{
						final AnywareModel anyware = new AnywareModel((Anyware) tablemodel.getValueAt(model.getMinSelectionIndex(), 0));
						anyware.reset();
						tableEntity.setModel(anyware);
						tableEntity.setTableHeader(new JTableHeader(tableEntity.getColumnModel())
						{
							@Override
							public String getToolTipText(final MouseEvent e)
							{
								return columnModel.getColumn(columnModel.getColumnIndexAtX(e.getPoint().x)).getHeaderValue().toString();
							}
						});
						for (int i = 0; i < tableEntity.getColumnModel().getColumnCount(); i++)
						{
							final TableColumn column = tableEntity.getColumnModel().getColumn(i);
							column.setCellRenderer(anyware.getColumnRenderer(i));
							final int width = anyware.getColumnWidth(i);
							if (width > 0)
							{
								column.setMinWidth(width / 2);
								column.setPreferredWidth(width);
							}
							else if (width < 0)
							{
								final Component component = column.getCellRenderer().getTableCellRendererComponent(tableEntity, null, false, false, 0, i);
								final int pixwidth = component.getFontMetrics(component.getFont()).stringWidth(String.format("%0" + (-width) + "d", 0)); //$NON-NLS-1$ //$NON-NLS-2$
								column.setMinWidth(pixwidth / 2);
								column.setPreferredWidth(pixwidth);
								column.setMaxWidth(pixwidth);
							}
						}
					}
					else
					{
						tableEntity.setModel(new DefaultTableModel());
					}
				}
			}
		});

		final JPanel panelEntity = new JPanel();
		splitPane.setRightComponent(panelEntity);
		panelEntity.setLayout(new BorderLayout(0, 0));

		final JToolBar toolBarEntity = new JToolBar();
		panelEntity.add(toolBarEntity, BorderLayout.SOUTH);

		final JToggleButton tglbtnBad = new JToggleButton(""); //$NON-NLS-1$
		tglbtnBad.setSelected(true);
		tglbtnBad.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resicons/icons/bullet_red.png"))); //$NON-NLS-1$
		tglbtnBad.setToolTipText(Messages.getString("ProfileViewer.tglbtnBad.toolTipText")); //$NON-NLS-1$
		toolBarEntity.add(tglbtnBad);

		final JToggleButton tglbtnOK = new JToggleButton(""); //$NON-NLS-1$
		tglbtnOK.setSelected(true);
		tglbtnOK.setToolTipText(Messages.getString("ProfileViewer.tglbtnOK.toolTipText")); //$NON-NLS-1$
		tglbtnOK.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resicons/icons/bullet_green.png"))); //$NON-NLS-1$
		toolBarEntity.add(tglbtnOK);

		tglbtnBad.addItemListener(e -> setFilterE(tglbtnBad.isSelected(), tglbtnOK.isSelected()));
		tglbtnOK.addItemListener(e -> setFilterE(tglbtnBad.isSelected(), tglbtnOK.isSelected()));

		final JScrollPane scrollPaneEntity = new JScrollPane();
		panelEntity.add(scrollPaneEntity, BorderLayout.CENTER);

		scrollPaneEntity.setViewportView(tableEntity);

		reset(profile);
		pack();
		try
		{
			setBounds(SerializationUtils.deserialize(Hex.decodeHex(session.getUser().settings.getProperty("ProfileViewer.Bounds", Hex.encodeHexString(SerializationUtils.serialize(getBounds())))))); //$NON-NLS-1$
		}
		catch (final DecoderException e1)
		{
			Log.err(e1.getMessage(),e1);
		}
		setVisible(true);
	}

	/**
	 * Export.
	 *
	 * @param type the type
	 * @param filtered the filtered
	 * @param selection the selection
	 */
	private void export(final Session session, final ExportType type, final boolean filtered, final SoftwareList selection)
	{
		new Thread(() -> {
			final FileNameExtensionFilter fnef = new FileNameExtensionFilter(Messages.getString("MainFrame.DatFile"), "xml", "dat"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			new JRMFileChooser<Void>(JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY, Optional.ofNullable(session.getUser().settings.getProperty("MainFrame.ChooseExeOrDatToExport", (String) null)).map(File::new).orElse(null), null, Arrays.asList(fnef), Messages.getString("ProfileViewer.ChooseDestinationFile"), false).show(ProfileViewer.this, chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				final Progress progress = new Progress(ProfileViewer.this);
				progress.setVisible(true);
				session.getUser().settings.setProperty("MainFrame.ChooseExeOrDatToExport", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				final File selectedfile = chooser.getSelectedFile();
				final File file = fnef.accept(selectedfile) ? selectedfile : new File(selectedfile.getAbsolutePath() + ".xml"); //$NON-NLS-1$
				new Export(session.curr_profile, file, type, filtered, selection, progress);
				progress.dispose();
				return null;
			});
		}).start();
	}

	/**
	 * Sets the filter WL.
	 *
	 * @param missing the missing
	 * @param partial the partial
	 * @param complete the complete
	 */
	public void setFilterWL(final boolean missing, final boolean partial, final boolean complete)
	{
		final EnumSet<AnywareStatus> filter = EnumSet.of(AnywareStatus.UNKNOWN);
		if (missing)
			filter.add(AnywareStatus.MISSING);
		if (partial)
			filter.add(AnywareStatus.PARTIAL);
		if (complete)
			filter.add(AnywareStatus.COMPLETE);
		((AnywareListListModel) tableWL.getModel()).setFilter(filter);
		if (tableWL.getRowCount() > 0)
			tableWL.setRowSelectionInterval(0, 0);
	}

	/**
	 * Sets the filter W.
	 *
	 * @param missing the missing
	 * @param partial the partial
	 * @param complete the complete
	 */
	public void setFilterW(final boolean missing, final boolean partial, final boolean complete)
	{
		final EnumSet<AnywareStatus> filter = EnumSet.of(AnywareStatus.UNKNOWN);
		if (missing)
			filter.add(AnywareStatus.MISSING);
		if (partial)
			filter.add(AnywareStatus.PARTIAL);
		if (complete)
			filter.add(AnywareStatus.COMPLETE);
		((AnywareListModel)tableW.getModel()).setFilter(filter);
		if (tableW.getRowCount() > 0)
			tableW.setRowSelectionInterval(0, 0);
	}

	/**
	 * Sets the filter E.
	 *
	 * @param ko the ko
	 * @param ok the ok
	 */
	public void setFilterE(final boolean ko, final boolean ok)
	{
		final EnumSet<EntityStatus> filter = EnumSet.of(EntityStatus.UNKNOWN);
		if (ko)
			filter.add(EntityStatus.KO);
		if (ok)
			filter.add(EntityStatus.OK);
		((AnywareModel) tableEntity.getModel()).setFilter(filter);
		if (tableEntity.getRowCount() > 0)
			tableEntity.setRowSelectionInterval(0, 0);
	}

	/**
	 * Clear.
	 */
	public void clear()
	{
		tableEntity.setModel(new DefaultTableModel());
		tableW.setModel(new DefaultTableModel());
		tableWL.setModel(new DefaultTableModel());
	}

	/**
	 * Reset.
	 *
	 * @param profile the profile
	 */
	public void reset(final Profile profile)
	{
		final MachineListListModel model = new MachineListListModel(profile.machinelist_list);
		model.reset();
		if (tableWL.getModel() != model)
			tableWL.setModel(model);
		for (int i = 0; i < tableWL.getColumnModel().getColumnCount(); i++)
		{
			final TableColumn column = tableWL.getColumnModel().getColumn(i);
			column.setCellRenderer(model.getColumnRenderer(i));
			final int width = model.getColumnWidth(i);
			if (width > 0)
			{
				column.setMinWidth(width / 2);
				column.setPreferredWidth(width);
			}
			else if (width < 0)
			{
				column.setMinWidth(-width);
				column.setMaxWidth(-width);
			}
		}
		if (tableWL.getRowCount() > 0)
			tableWL.setRowSelectionInterval(0, 0);
	}

	/**
	 * Reload.
	 */
	public void reload()
	{
		TableModel tablemodel = tableWL.getModel();
		if (tablemodel instanceof MachineListListModel)
			((MachineListListModel) tablemodel).fireTableChanged(new TableModelEvent(tablemodel, 0, ((MachineListListModel) tablemodel).getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
		tablemodel = tableW.getModel();
		if (tablemodel instanceof AnywareListModel)
			((AnywareListModel) tablemodel).fireTableChanged(new TableModelEvent(tablemodel, 0, ((AnywareListModel) tablemodel).getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
		tablemodel = tableEntity.getModel();
		if (tablemodel instanceof AnywareModel)
			((AnywareModel) tablemodel).fireTableChanged(new TableModelEvent(tablemodel, 0, ((AnywareModel) tablemodel).getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
	}

	/**
	 * Adds the popup.
	 *
	 * @param component the component
	 * @param popup the popup
	 */
	private static void addPopup(final Component component, final JPopupMenu popup)
	{
		component.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e)
			{
				if (e.isPopupTrigger())
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
}
