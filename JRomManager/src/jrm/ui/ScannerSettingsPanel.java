package jrm.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import jrm.locale.Messages;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;

@SuppressWarnings("serial")
public class ScannerSettingsPanel extends JPanel
{
	/** The scanner sub settings panel. */
	private JPanel scannerSubSettingsPanel;

	/** The chckbx create missing sets. */
	private JCheckBox chckbxCreateMissingSets;

	/** The chckbx create only complete. */
	private JCheckBox chckbxCreateOnlyComplete;

	/** The chckbx ignore unknown containers. */
	private JCheckBox chckbxIgnoreUnknownContainers;

	/** The chckbx ignore unneeded containers. */
	private JCheckBox chckbxIgnoreUnneededContainers;

	/** The chckbx ignore unneeded entries. */
	private JCheckBox chckbxIgnoreUnneededEntries;

	/** The chckbx need SHA 1. */
	private JCheckBox chckbxNeedSHA1;

	/** The chckbx use implicit merge. */
	private JCheckBox chckbxUseImplicitMerge;

	/** The chckbx use parallelism. */
	private JCheckBox chckbxUseParallelism;

	/** The chckbx ignore merge name roms. */
	private JCheckBox chckbxIgnoreMergeNameRoms;

	/** The chckbx ignore merge name disks. */
	private JCheckBox chckbxIgnoreMergeNameDisks;

	/** The chckbx exclude games. */
	private JCheckBox chckbxExcludeGames;

	/** The chckbx exclude machines. */
	private JCheckBox chckbxExcludeMachines;

	/** The chckbx backup. */
	private JCheckBox chckbxBackup;

	/** The cbbx merge mode. */
	private JComboBox<MergeOptions> cbbxMergeMode;

	/** The cb compression. */
	private JComboBox<FormatOptions> cbCompression;

	/** The cb hash collision. */
	private JComboBox<HashCollisionOptions> cbHashCollision;


	/**
	 * Create the panel.
	 */
	public ScannerSettingsPanel()
	{
		this.setBackground(UIManager.getColor("Panel.background")); //$NON-NLS-1$

		JPopupMenu popupMenu_4 = new JPopupMenu();
		MainFrame.addPopup(this, popupMenu_4);

		JMenu mnPresets = new JMenu(Messages.getString("MainFrame.mnPresets.text")); //$NON-NLS-1$
		popupMenu_4.add(mnPresets);

		JMenu mnPdMame = new JMenu(Messages.getString("MainFrame.mnPdMame.text")); //$NON-NLS-1$
		mnPresets.add(mnPdMame);

		JMenuItem mntmPleasuredome = new JMenuItem(Messages.getString("MainFrame.mntmPleasuredome.text")); //$NON-NLS-1$
		mnPdMame.add(mntmPleasuredome);

		JMenuItem mntmPdMameNon = new JMenuItem(Messages.getString("MainFrame.mntmPdMameNon.text")); //$NON-NLS-1$
		mnPdMame.add(mntmPdMameNon);

		JMenuItem mntmPdMameSplit = new JMenuItem(Messages.getString("MainFrame.mntmPdMameSplit.text")); //$NON-NLS-1$
		mnPdMame.add(mntmPdMameSplit);
		mntmPdMameSplit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				chckbxCreateMissingSets.setSelected(true);
				chckbxCreateOnlyComplete.setSelected(false);
				chckbxIgnoreUnneededContainers.setSelected(false);
				chckbxIgnoreUnneededEntries.setSelected(false);
				chckbxIgnoreUnknownContainers.setSelected(true); // Don't remove _ReadMe_.txt
				chckbxUseImplicitMerge.setSelected(true);
				chckbxIgnoreMergeNameDisks.setSelected(true);
				chckbxIgnoreMergeNameRoms.setSelected(false);
				cbCompression.setSelectedItem(FormatOptions.TZIP);
				cbbxMergeMode.setSelectedItem(MergeOptions.SPLIT);
			}
		});
		mntmPdMameNon.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				chckbxCreateMissingSets.setSelected(true);
				chckbxCreateOnlyComplete.setSelected(false);
				chckbxIgnoreUnneededContainers.setSelected(false);
				chckbxIgnoreUnneededEntries.setSelected(false);
				chckbxIgnoreUnknownContainers.setSelected(true); // Don't remove _ReadMe_.txt
				chckbxUseImplicitMerge.setSelected(true);
				chckbxIgnoreMergeNameDisks.setSelected(true);
				chckbxIgnoreMergeNameRoms.setSelected(false);
				cbCompression.setSelectedItem(FormatOptions.TZIP);
				cbbxMergeMode.setSelectedItem(MergeOptions.SUPERFULLNOMERGE);
			}
		});
		mntmPleasuredome.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				chckbxCreateMissingSets.setSelected(true);
				chckbxCreateOnlyComplete.setSelected(false);
				chckbxIgnoreUnneededContainers.setSelected(false);
				chckbxIgnoreUnneededEntries.setSelected(false);
				chckbxIgnoreUnknownContainers.setSelected(true);
				chckbxUseImplicitMerge.setSelected(true);
				chckbxIgnoreMergeNameDisks.setSelected(true); // Don't remove _ReadMe_.txt
				chckbxIgnoreMergeNameRoms.setSelected(false);
				cbCompression.setSelectedItem(FormatOptions.TZIP);
				cbbxMergeMode.setSelectedItem(MergeOptions.MERGE);
				cbHashCollision.setSelectedItem(HashCollisionOptions.HALFDUMB);
			}
		});
		final GridBagLayout gbl_scannerSettingsPanel = new GridBagLayout();
		gbl_scannerSettingsPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_scannerSettingsPanel.rowHeights = new int[] { 20, 20, 0, 0, 0, 0, 0, 20, 0 };
		gbl_scannerSettingsPanel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_scannerSettingsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		this.setLayout(gbl_scannerSettingsPanel);

		chckbxNeedSHA1 = new JCheckBox(Messages.getString("MainFrame.chckbxNeedSHA1.text")); //$NON-NLS-1$
		chckbxNeedSHA1.addItemListener(e -> Profile.curr_profile.setProperty("need_sha1_or_md5", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		chckbxNeedSHA1.setToolTipText(Messages.getString("MainFrame.chckbxNeedSHA1.toolTipText")); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxNeedSHA1 = new GridBagConstraints();
		gbc_chckbxNeedSHA1.fill = GridBagConstraints.BOTH;
		gbc_chckbxNeedSHA1.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNeedSHA1.gridx = 0;
		gbc_chckbxNeedSHA1.gridy = 0;
		this.add(chckbxNeedSHA1, gbc_chckbxNeedSHA1);

		chckbxUseParallelism = new JCheckBox(Messages.getString("MainFrame.chckbxUseParallelism.text")); //$NON-NLS-1$
		chckbxUseParallelism.addItemListener(e -> Profile.curr_profile.setProperty("use_parallelism", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$

		chckbxCreateMissingSets = new JCheckBox(Messages.getString("MainFrame.chckbxCreateMissingSets.text")); //$NON-NLS-1$
		chckbxCreateMissingSets.addItemListener(e -> {
			Profile.curr_profile.setProperty("create_mode", e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
			if (e.getStateChange() != ItemEvent.SELECTED)
				chckbxCreateOnlyComplete.setSelected(false);
			chckbxCreateOnlyComplete.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
		});
		final GridBagConstraints gbc_chckbxCreateMissingSets = new GridBagConstraints();
		gbc_chckbxCreateMissingSets.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxCreateMissingSets.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxCreateMissingSets.gridx = 1;
		gbc_chckbxCreateMissingSets.gridy = 0;
		this.add(chckbxCreateMissingSets, gbc_chckbxCreateMissingSets);
		chckbxUseParallelism.setToolTipText(Messages.getString("MainFrame.chckbxUseParallelism.toolTipText")); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxUseParallelism = new GridBagConstraints();
		gbc_chckbxUseParallelism.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxUseParallelism.fill = GridBagConstraints.BOTH;
		gbc_chckbxUseParallelism.gridx = 0;
		gbc_chckbxUseParallelism.gridy = 1;
		this.add(chckbxUseParallelism, gbc_chckbxUseParallelism);

		chckbxCreateOnlyComplete = new JCheckBox(Messages.getString("MainFrame.chckbxCreateOnlyComplete.text")); //$NON-NLS-1$
		chckbxCreateOnlyComplete.addItemListener(e -> Profile.curr_profile.setProperty("createfull_mode", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxCreateOnlyComplete = new GridBagConstraints();
		gbc_chckbxCreateOnlyComplete.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxCreateOnlyComplete.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxCreateOnlyComplete.gridx = 1;
		gbc_chckbxCreateOnlyComplete.gridy = 1;
		this.add(chckbxCreateOnlyComplete, gbc_chckbxCreateOnlyComplete);

		chckbxIgnoreUnneededContainers = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreUnneededContainers.text")); //$NON-NLS-1$
		chckbxIgnoreUnneededContainers.addItemListener(e -> Profile.curr_profile.setProperty("ignore_unneeded_containers", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxIgnoreUnneededContainers = new GridBagConstraints();
		gbc_chckbxIgnoreUnneededContainers.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIgnoreUnneededContainers.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIgnoreUnneededContainers.gridx = 0;
		gbc_chckbxIgnoreUnneededContainers.gridy = 2;
		this.add(chckbxIgnoreUnneededContainers, gbc_chckbxIgnoreUnneededContainers);

		chckbxIgnoreUnneededEntries = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreUnneededEntries.text")); //$NON-NLS-1$
		chckbxIgnoreUnneededEntries.addItemListener(e -> Profile.curr_profile.setProperty("ignore_unneeded_entries", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxIgnoreUnneededEntries = new GridBagConstraints();
		gbc_chckbxIgnoreUnneededEntries.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIgnoreUnneededEntries.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxIgnoreUnneededEntries.gridx = 1;
		gbc_chckbxIgnoreUnneededEntries.gridy = 2;
		this.add(chckbxIgnoreUnneededEntries, gbc_chckbxIgnoreUnneededEntries);

		chckbxIgnoreUnknownContainers = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreUnknownContainers.text")); //$NON-NLS-1$
		chckbxIgnoreUnknownContainers.addItemListener(e -> Profile.curr_profile.setProperty("ignore_unknown_containers", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxIgnoreUnknownContainers = new GridBagConstraints();
		gbc_chckbxIgnoreUnknownContainers.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIgnoreUnknownContainers.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIgnoreUnknownContainers.gridx = 0;
		gbc_chckbxIgnoreUnknownContainers.gridy = 3;
		this.add(chckbxIgnoreUnknownContainers, gbc_chckbxIgnoreUnknownContainers);

		chckbxUseImplicitMerge = new JCheckBox(Messages.getString("MainFrame.chckbxUseImplicitMerge.text")); //$NON-NLS-1$
		chckbxUseImplicitMerge.addItemListener(e -> Profile.curr_profile.setProperty("implicit_merge", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxUseImplicitMerge = new GridBagConstraints();
		gbc_chckbxUseImplicitMerge.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxUseImplicitMerge.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxUseImplicitMerge.gridx = 1;
		gbc_chckbxUseImplicitMerge.gridy = 3;
		this.add(chckbxUseImplicitMerge, gbc_chckbxUseImplicitMerge);

		chckbxIgnoreMergeNameRoms = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreMergeName.text")); //$NON-NLS-1$
		chckbxIgnoreMergeNameRoms.addItemListener(e -> Profile.curr_profile.setProperty("ignore_merge_name_roms", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_chckbxIgnoreMergeNameRoms = new GridBagConstraints();
		gbc_chckbxIgnoreMergeNameRoms.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIgnoreMergeNameRoms.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIgnoreMergeNameRoms.gridx = 0;
		gbc_chckbxIgnoreMergeNameRoms.gridy = 4;
		this.add(chckbxIgnoreMergeNameRoms, gbc_chckbxIgnoreMergeNameRoms);

		chckbxIgnoreMergeNameDisks = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreMergeName_1.text")); //$NON-NLS-1$
		chckbxIgnoreMergeNameDisks.addItemListener(e -> Profile.curr_profile.setProperty("ignore_merge_name_disks", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_chckbxIgnoreMergeNameDisks = new GridBagConstraints();
		gbc_chckbxIgnoreMergeNameDisks.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIgnoreMergeNameDisks.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxIgnoreMergeNameDisks.gridx = 1;
		gbc_chckbxIgnoreMergeNameDisks.gridy = 4;
		this.add(chckbxIgnoreMergeNameDisks, gbc_chckbxIgnoreMergeNameDisks);

		chckbxExcludeGames = new JCheckBox(Messages.getString("MainFrame.chckbxExcludeGames.text")); //$NON-NLS-1$
		chckbxExcludeGames.addItemListener(e -> Profile.curr_profile.setProperty("exclude_games", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_chckbxExcludeGames = new GridBagConstraints();
		gbc_chckbxExcludeGames.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxExcludeGames.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxExcludeGames.gridx = 0;
		gbc_chckbxExcludeGames.gridy = 5;
		this.add(chckbxExcludeGames, gbc_chckbxExcludeGames);

		chckbxExcludeMachines = new JCheckBox(Messages.getString("MainFrame.chckbxExcludeMachines.text")); //$NON-NLS-1$
		chckbxExcludeMachines.addItemListener(e -> Profile.curr_profile.setProperty("exclude_machines", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_chckbxExcludeMachines = new GridBagConstraints();
		gbc_chckbxExcludeMachines.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxExcludeMachines.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxExcludeMachines.gridx = 1;
		gbc_chckbxExcludeMachines.gridy = 5;
		this.add(chckbxExcludeMachines, gbc_chckbxExcludeMachines);

		chckbxBackup = new JCheckBox(Messages.getString("MainFrame.chckbxBackup.text")); //$NON-NLS-1$
		chckbxBackup.addItemListener(e -> Profile.curr_profile.setProperty("backup", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_chckbxBackup = new GridBagConstraints();
		gbc_chckbxBackup.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxBackup.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxBackup.gridx = 0;
		gbc_chckbxBackup.gridy = 6;
		this.add(chckbxBackup, gbc_chckbxBackup);

		scannerSubSettingsPanel = new JPanel();
		final GridBagConstraints gbc_scannerSubSettingsPanel = new GridBagConstraints();
		gbc_scannerSubSettingsPanel.gridwidth = 2;
		gbc_scannerSubSettingsPanel.fill = GridBagConstraints.BOTH;
		gbc_scannerSubSettingsPanel.gridx = 0;
		gbc_scannerSubSettingsPanel.gridy = 7;
		this.add(scannerSubSettingsPanel, gbc_scannerSubSettingsPanel);
		final GridBagLayout gbl_scannerSubSettingsPanel = new GridBagLayout();
		gbl_scannerSubSettingsPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_scannerSubSettingsPanel.rowHeights = new int[] { 0, 0, 0, 8, 100, 0 };
		gbl_scannerSubSettingsPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_scannerSubSettingsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		scannerSubSettingsPanel.setLayout(gbl_scannerSubSettingsPanel);

		JLabel lblCompression = new JLabel(Messages.getString("MainFrame.lblCompression.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblCompression = new GridBagConstraints();
		gbc_lblCompression.anchor = GridBagConstraints.EAST;
		gbc_lblCompression.insets = new Insets(0, 5, 5, 5);
		gbc_lblCompression.gridx = 0;
		gbc_lblCompression.gridy = 0;
		scannerSubSettingsPanel.add(lblCompression, gbc_lblCompression);

		cbCompression = new JComboBox<>();
		cbCompression.setModel(new DefaultComboBoxModel<>(FormatOptions.values()));
		cbCompression.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				setText(((FormatOptions) value).getDesc());
				return this;
			}
		});
		cbCompression.addActionListener(e -> Profile.curr_profile.settings.setProperty("format", cbCompression.getSelectedItem().toString())); //$NON-NLS-1$
		final GridBagConstraints gbc_cbCompression = new GridBagConstraints();
		gbc_cbCompression.gridwidth = 2;
		gbc_cbCompression.insets = new Insets(0, 0, 5, 5);
		gbc_cbCompression.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbCompression.gridx = 1;
		gbc_cbCompression.gridy = 0;
		scannerSubSettingsPanel.add(cbCompression, gbc_cbCompression);

		JLabel lblMergeMode = new JLabel(Messages.getString("MainFrame.lblMergeMode.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblMergeMode = new GridBagConstraints();
		gbc_lblMergeMode.insets = new Insets(0, 0, 5, 5);
		gbc_lblMergeMode.anchor = GridBagConstraints.EAST;
		gbc_lblMergeMode.gridx = 0;
		gbc_lblMergeMode.gridy = 1;
		scannerSubSettingsPanel.add(lblMergeMode, gbc_lblMergeMode);

		cbbxMergeMode = new JComboBox<>();
		final GridBagConstraints gbc_cbbxMergeMode = new GridBagConstraints();
		gbc_cbbxMergeMode.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxMergeMode.gridwidth = 2;
		gbc_cbbxMergeMode.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxMergeMode.gridx = 1;
		gbc_cbbxMergeMode.gridy = 1;
		scannerSubSettingsPanel.add(cbbxMergeMode, gbc_cbbxMergeMode);
		cbbxMergeMode.setToolTipText(Messages.getString("MainFrame.cbbxMergeMode.toolTipText")); //$NON-NLS-1$
		cbbxMergeMode.setModel(new DefaultComboBoxModel<>(MergeOptions.values()));
		cbbxMergeMode.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				setText(((MergeOptions) value).getDesc());
				return this;
			}
		});
		cbbxMergeMode.addActionListener(e -> {
			Profile.curr_profile.settings.setProperty("merge_mode", cbbxMergeMode.getSelectedItem().toString()); //$NON-NLS-1$
			cbHashCollision.setEnabled(((MergeOptions) cbbxMergeMode.getSelectedItem()).isMerge());
		});

		JLabel lblHashCollision = new JLabel(Messages.getString("MainFrame.lblHashCollision.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblHashCollision = new GridBagConstraints();
		gbc_lblHashCollision.insets = new Insets(0, 0, 5, 5);
		gbc_lblHashCollision.anchor = GridBagConstraints.EAST;
		gbc_lblHashCollision.gridx = 0;
		gbc_lblHashCollision.gridy = 2;
		scannerSubSettingsPanel.add(lblHashCollision, gbc_lblHashCollision);

		cbHashCollision = new JComboBox<>();
		cbHashCollision.setModel(new DefaultComboBoxModel<>(HashCollisionOptions.values()));
		cbHashCollision.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				setText(((HashCollisionOptions) value).getDesc());
				return this;
			}
		});
		cbHashCollision.addActionListener(e -> Profile.curr_profile.settings.setProperty("hash_collision_mode", cbHashCollision.getSelectedItem().toString())); //$NON-NLS-1$
		final GridBagConstraints gbc_cbHashCollision = new GridBagConstraints();
		gbc_cbHashCollision.gridwidth = 2;
		gbc_cbHashCollision.insets = new Insets(0, 0, 5, 5);
		gbc_cbHashCollision.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbHashCollision.gridx = 1;
		gbc_cbHashCollision.gridy = 2;
		scannerSubSettingsPanel.add(cbHashCollision, gbc_cbHashCollision);

	}
	
	public void initProfileSettings()
	{
		chckbxNeedSHA1.setSelected(Profile.curr_profile.getProperty("need_sha1_or_md5", false)); //$NON-NLS-1$
		chckbxUseParallelism.setSelected(Profile.curr_profile.getProperty("use_parallelism", false)); //$NON-NLS-1$
		chckbxCreateMissingSets.setSelected(Profile.curr_profile.getProperty("create_mode", false)); //$NON-NLS-1$
		chckbxCreateOnlyComplete.setSelected(Profile.curr_profile.getProperty("createfull_mode", false) && chckbxCreateMissingSets.isSelected()); //$NON-NLS-1$
		chckbxIgnoreUnneededContainers.setSelected(Profile.curr_profile.getProperty("ignore_unneeded_containers", false)); //$NON-NLS-1$
		chckbxIgnoreUnneededEntries.setSelected(Profile.curr_profile.getProperty("ignore_unneeded_entries", false)); //$NON-NLS-1$
		chckbxIgnoreUnknownContainers.setSelected(Profile.curr_profile.getProperty("ignore_unknown_containers", false)); //$NON-NLS-1$
		chckbxCreateOnlyComplete.setEnabled(chckbxCreateMissingSets.isSelected());
		chckbxUseImplicitMerge.setSelected(Profile.curr_profile.getProperty("implicit_merge", false)); //$NON-NLS-1$
		chckbxIgnoreMergeNameRoms.setSelected(Profile.curr_profile.getProperty("ignore_merge_name_roms", false)); //$NON-NLS-1$
		chckbxIgnoreMergeNameDisks.setSelected(Profile.curr_profile.getProperty("ignore_merge_name_disks", false)); //$NON-NLS-1$
		chckbxExcludeGames.setSelected(Profile.curr_profile.getProperty("exclude_games", false)); //$NON-NLS-1$
		chckbxExcludeMachines.setSelected(Profile.curr_profile.getProperty("exclude_machines", false)); //$NON-NLS-1$
		chckbxBackup.setSelected(Profile.curr_profile.getProperty("backup", true)); //$NON-NLS-1$
		
		cbCompression.setSelectedItem(FormatOptions.valueOf(Profile.curr_profile.settings.getProperty("format", FormatOptions.ZIP.toString()))); //$NON-NLS-1$
		cbbxMergeMode.setSelectedItem(MergeOptions.valueOf(Profile.curr_profile.settings.getProperty("merge_mode", MergeOptions.SPLIT.toString()))); //$NON-NLS-1$
		Anyware.merge_mode = (MergeOptions) cbbxMergeMode.getSelectedItem();
		cbHashCollision.setEnabled(((MergeOptions) cbbxMergeMode.getSelectedItem()).isMerge());
		cbHashCollision.setSelectedItem(HashCollisionOptions.valueOf(Profile.curr_profile.settings.getProperty("hash_collision_mode", HashCollisionOptions.SINGLEFILE.toString()))); //$NON-NLS-1$
		Anyware.hash_collision_mode = (HashCollisionOptions) cbHashCollision.getSelectedItem();
	}

}
