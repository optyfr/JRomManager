package jrm.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jrm.locale.Messages;
import jrm.misc.SettingsEnum;
import jrm.profile.filter.CatVer.Category;
import jrm.profile.filter.CatVer.Category.SubCategory;
import jrm.profile.filter.NPlayer;
import jrm.security.Session;
import jrm.ui.basic.JCheckBoxList;
import jrm.ui.basic.JCheckBoxTree;
import jrm.ui.basic.JFileDropMode;
import jrm.ui.basic.JFileDropTextField;
import jrm.ui.basic.JTextFieldHintUI;
import jrm.ui.basic.NGTreeNode;
import jrm.ui.profile.filter.CatVerModel;
import jrm.ui.profile.filter.CatVerNode;
import jrm.ui.profile.filter.CatVerNode.CategoryNode;
import jrm.ui.profile.filter.NPlayersModel;

@SuppressWarnings("serial")
public class ScannerAdvFilterPanel extends JPanel
{
	/** The list N players. */
	private JCheckBoxList<NPlayer> listNPlayers;

	/** The tf cat ver. */
	private JFileDropTextField tfCatVer;

	/** The tf N players. */
	private JFileDropTextField tfNPlayers;

	/** The tree cat ver. */
	private JCheckBoxTree treeCatVer;


	/**
	 * Create the panel.
	 */
	public ScannerAdvFilterPanel(final Session session)
	{
		final GridBagLayout gbl_scannerAdvFilters = new GridBagLayout();
		gbl_scannerAdvFilters.columnWidths = new int[] { 0, 0, 0 };
		gbl_scannerAdvFilters.rowHeights = new int[] { 0, 0, 0 };
		gbl_scannerAdvFilters.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_scannerAdvFilters.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		this.setLayout(gbl_scannerAdvFilters);

		tfNPlayers = new JFileDropTextField(txt -> {
			session.curr_profile.setProperty(SettingsEnum.filter_nplayers_ini, txt); //$NON-NLS-1$
			session.curr_profile.loadNPlayers(null);
			session.curr_profile.saveSettings();
			listNPlayers.setModel(new NPlayersModel(session.curr_profile.getNplayers()));
		});
		tfNPlayers.setMode(JFileDropMode.FILE);
		tfNPlayers.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropNPlayersIniHere"), Color.gray)); //$NON-NLS-1$
		tfNPlayers.setEditable(false);
		final GridBagConstraints gbc_tfNPlayers = new GridBagConstraints();
		gbc_tfNPlayers.insets = new Insets(0, 0, 5, 5);
		gbc_tfNPlayers.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfNPlayers.gridx = 0;
		gbc_tfNPlayers.gridy = 0;
		this.add(tfNPlayers, gbc_tfNPlayers);

		tfCatVer = new JFileDropTextField(txt -> {
			session.curr_profile.setProperty(SettingsEnum.filter_catver_ini, txt); //$NON-NLS-1$
			session.curr_profile.loadCatVer(null);
			session.curr_profile.saveSettings();
			treeCatVer.setModel(session.curr_profile.getCatver() != null ? new CatVerModel(new CatVerNode(session.curr_profile.getCatver())) : new CatVerModel());
		});
		tfCatVer.setMode(JFileDropMode.FILE);
		tfCatVer.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropCatVerIniHere"), Color.gray)); //$NON-NLS-1$
		tfCatVer.setEditable(false);
		final GridBagConstraints gbc_tfCatVer = new GridBagConstraints();
		gbc_tfCatVer.insets = new Insets(0, 0, 5, 0);
		gbc_tfCatVer.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfCatVer.gridx = 1;
		gbc_tfCatVer.gridy = 0;
		this.add(tfCatVer, gbc_tfCatVer);

		JScrollPane scrollPaneNPlayers = new JScrollPane();
		scrollPaneNPlayers.setViewportBorder(new TitledBorder(null, Messages.getString("MainFrame.NPlayers"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		final GridBagConstraints gbc_scrollPaneNPlayers = new GridBagConstraints();
		gbc_scrollPaneNPlayers.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPaneNPlayers.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneNPlayers.gridx = 0;
		gbc_scrollPaneNPlayers.gridy = 1;
		this.add(scrollPaneNPlayers, gbc_scrollPaneNPlayers);

		listNPlayers = new JCheckBoxList<>();
		listNPlayers.setCellRenderer(listNPlayers.new CellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(final JList<? extends NPlayer> list, final NPlayer value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				final JCheckBox checkbox = (JCheckBox) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				checkbox.setSelected(value.isSelected(session.curr_profile));
				return checkbox;
			}
		});
		listNPlayers.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting())
			{
				if (e.getFirstIndex() != -1)
				{
					for (int index = e.getFirstIndex(); index <= e.getLastIndex() && index < listNPlayers.getModel().getSize(); index++)
						listNPlayers.getModel().getElementAt(index).setSelected(session.curr_profile, listNPlayers.isSelectedIndex(index));
					if (MainFrame.profile_viewer != null)
						MainFrame.profile_viewer.reset(session.curr_profile);
				}
			}
		});
		listNPlayers.setEnabled(false);
		scrollPaneNPlayers.setViewportView(listNPlayers);

		JPopupMenu popupMenuNPlay = new JPopupMenu();
		MainFrame.addPopup(listNPlayers, popupMenuNPlay);

		JMenuItem mntmSelectAllNPlay = new JMenuItem(Messages.getString("MainFrame.SelectAll")); //$NON-NLS-1$
		mntmSelectAllNPlay.addActionListener(e -> listNPlayers.selectAll());
		popupMenuNPlay.add(mntmSelectAllNPlay);

		JMenuItem mntmSelectNoneNPlay = new JMenuItem(Messages.getString("MainFrame.SelectNone")); //$NON-NLS-1$
		mntmSelectNoneNPlay.addActionListener(e -> listNPlayers.selectNone());
		popupMenuNPlay.add(mntmSelectNoneNPlay);

		JMenuItem mntmInvertSelectionNPlay = new JMenuItem(Messages.getString("MainFrame.InvertSelection")); //$NON-NLS-1$
		mntmInvertSelectionNPlay.addActionListener(e -> listNPlayers.selectInvert());
		popupMenuNPlay.add(mntmInvertSelectionNPlay);
		
		JSeparator separator_1 = new JSeparator();
		popupMenuNPlay.add(separator_1);
		
		JMenuItem mntmClearNPlayers = new JMenuItem(Messages.getString("ScannerAdvFilterPanel.mntmClear_1.text")); //$NON-NLS-1$
		mntmClearNPlayers.addActionListener((e)->{
			session.curr_profile.saveSettings();
			session.curr_profile.setNplayers(null);
			session.curr_profile.saveSettings();
			tfNPlayers.setText(null);
			listNPlayers.setModel(new DefaultListModel<>());
		});
		popupMenuNPlay.add(mntmClearNPlayers);

		JScrollPane scrollPaneCatVer = new JScrollPane();
		scrollPaneCatVer.setViewportBorder(new TitledBorder(null, Messages.getString("MainFrame.Categories"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		final GridBagConstraints gbc_scrollPaneCatVer = new GridBagConstraints();
		gbc_scrollPaneCatVer.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneCatVer.gridx = 1;
		gbc_scrollPaneCatVer.gridy = 1;
		this.add(scrollPaneCatVer, gbc_scrollPaneCatVer);

		treeCatVer = new JCheckBoxTree(new CatVerModel());
		treeCatVer.addCheckChangeEventListener(event -> {
			session.curr_profile.saveSettings();
			if (MainFrame.profile_viewer != null)
				MainFrame.profile_viewer.reset(session.curr_profile);
		});
		treeCatVer.setEnabled(false);
		scrollPaneCatVer.setViewportView(treeCatVer);

		JPopupMenu popupMenuCat = new JPopupMenu();
		popupMenuCat.addPopupMenuListener(new PopupMenuListener()
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
			}
		});
		MainFrame.addPopup(treeCatVer, popupMenuCat);

		JMenu mnSelectCat = new JMenu(Messages.getString("MainFrame.Select")); //$NON-NLS-1$
		popupMenuCat.add(mnSelectCat);

		JMenuItem mntmSelectAllCat = new JMenuItem(Messages.getString("MainFrame.All")); //$NON-NLS-1$
		mntmSelectAllCat.addActionListener(e -> treeCatVer.selectAll());
		mnSelectCat.add(mntmSelectAllCat);

		JMenuItem mntmSelectMatureCat = new JMenuItem(Messages.getString("MainFrame.Mature")); //$NON-NLS-1$
		mntmSelectMatureCat.addActionListener(e -> {
			final List<NGTreeNode> mature_nodes = new ArrayList<>();
			for (final Category cat : session.curr_profile.getCatver())
			{
				final CatVerModel catvermodel = (CatVerModel)treeCatVer.getModel();
				final CategoryNode catnode = ((CatVerNode)catvermodel.getRoot()).getNode(cat);
				if (cat.name.endsWith("* Mature *")) //$NON-NLS-1$
					mature_nodes.add(catnode);
				else
					for (final SubCategory subcat : cat)
						if (subcat.name.endsWith("* Mature *")) //$NON-NLS-1$
							mature_nodes.add(catnode.getNode(subcat));
			}
			treeCatVer.select(mature_nodes.toArray(new NGTreeNode[0]));
		});
		mnSelectCat.add(mntmSelectMatureCat);

		JMenu mnUnselectCat = new JMenu(Messages.getString("MainFrame.Unselect")); //$NON-NLS-1$
		popupMenuCat.add(mnUnselectCat);

		JMenuItem mntmUnselectAllCat = new JMenuItem(Messages.getString("MainFrame.All")); //$NON-NLS-1$
		mntmUnselectAllCat.addActionListener(e -> treeCatVer.selectNone());
		mnUnselectCat.add(mntmUnselectAllCat);

		JMenuItem mntmUnselectMatureCat = new JMenuItem(Messages.getString("MainFrame.Mature")); //$NON-NLS-1$
		mntmUnselectMatureCat.addActionListener(e -> {
			final List<NGTreeNode> mature_nodes = new ArrayList<>();
			for (final Category cat : session.curr_profile.getCatver())
			{
				final CatVerModel catvermodel = (CatVerModel)treeCatVer.getModel();
				final CategoryNode catnode = ((CatVerNode)catvermodel.getRoot()).getNode(cat);
				if (cat.name.endsWith("* Mature *")) //$NON-NLS-1$
					mature_nodes.add(catnode);
				else
					for (final SubCategory subcat : cat)
						if (subcat.name.endsWith("* Mature *")) //$NON-NLS-1$
							mature_nodes.add(catnode.getNode(subcat));
			}
			treeCatVer.unselect(mature_nodes.toArray(new NGTreeNode[0]));
		});
		mnUnselectCat.add(mntmUnselectMatureCat);
		
		JSeparator separator = new JSeparator();
		popupMenuCat.add(separator);
		
		JMenuItem mntmClearCat = new JMenuItem(Messages.getString("ScannerAdvFilterPanel.mntmClear.text")); //$NON-NLS-1$
		mntmClearCat.addActionListener((e) -> {
			session.curr_profile.setProperty(SettingsEnum.filter_catver_ini, null); //$NON-NLS-1$
			session.curr_profile.setCatver(null);
			session.curr_profile.saveSettings();
			tfCatVer.setText(null);
			treeCatVer.setModel(new CatVerModel());
		});
		popupMenuCat.add(mntmClearCat);

	}

	public void initProfileSettings(final Session session)
	{
		tfNPlayers.setText(session.curr_profile.getNplayers() != null ? session.curr_profile.getNplayers().file.getAbsolutePath() : null);
		listNPlayers.setModel(new NPlayersModel(session.curr_profile.getNplayers()));
		tfCatVer.setText(session.curr_profile.getCatver() != null ? session.curr_profile.getCatver().file.getAbsolutePath() : null);
		treeCatVer.setModel(session.curr_profile.getCatver() != null ? new CatVerModel(new CatVerNode(session.curr_profile.getCatver())) : new CatVerModel());		
	}
}
