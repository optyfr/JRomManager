package jrm.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import jrm.misc.ProfileSettings;
import jrm.misc.SettingsEnum;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import lombok.Getter;

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

		JPopupMenu popupMenu = new JPopupMenu();
		MainFrame.addPopup(this, popupMenu);

		JMenu mnPresets = new JMenu(Messages.getString("MainFrame.mnPresets.text")); //$NON-NLS-1$
		popupMenu.add(mnPresets);

		JMenu mnPdMame = new JMenu(Messages.getString("MainFrame.mnPdMame.text")); //$NON-NLS-1$
		mnPresets.add(mnPdMame);

		JMenuItem mntmPleasuredome = new JMenuItem(Messages.getString("MainFrame.mntmPleasuredome.text")); //$NON-NLS-1$
		mnPdMame.add(mntmPleasuredome);

		JMenuItem mntmPdMameNon = new JMenuItem(Messages.getString("MainFrame.mntmPdMameNon.text")); //$NON-NLS-1$
		mnPdMame.add(mntmPdMameNon);

		JMenuItem mntmPdMameSplit = new JMenuItem(Messages.getString("MainFrame.mntmPdMameSplit.text")); //$NON-NLS-1$
		mnPdMame.add(mntmPdMameSplit);
		
		mntmPdMameSplit.addActionListener(e -> {
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

		});
		mntmPdMameNon.addActionListener(e -> {
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
		});
		mntmPleasuredome.addActionListener(e -> {
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
		});
		final GridBagLayout gblScannerSettingsPanel = new GridBagLayout();
		gblScannerSettingsPanel.columnWidths = new int[] { 0, 0, 0 };
		gblScannerSettingsPanel.rowHeights = new int[] { 20, 20, 0, 0, 0, 0, 0, 20, 0 };
		gblScannerSettingsPanel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gblScannerSettingsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		this.setLayout(gblScannerSettingsPanel);

		chckbxNeedSHA1 = new JCheckBox(Messages.getString("MainFrame.chckbxNeedSHA1.text")); //$NON-NLS-1$
		chckbxNeedSHA1.addItemListener(e -> settings.setProperty(SettingsEnum.need_sha1_or_md5, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		chckbxNeedSHA1.setToolTipText(Messages.getString("MainFrame.chckbxNeedSHA1.toolTipText")); //$NON-NLS-1$
		final GridBagConstraints gbcChckbxNeedSHA1 = new GridBagConstraints();
		gbcChckbxNeedSHA1.fill = GridBagConstraints.BOTH;
		gbcChckbxNeedSHA1.insets = new Insets(0, 0, 5, 5);
		gbcChckbxNeedSHA1.gridx = 0;
		gbcChckbxNeedSHA1.gridy = 0;
		this.add(chckbxNeedSHA1, gbcChckbxNeedSHA1);

		chckbxUseParallelism = new JCheckBox(Messages.getString("MainFrame.chckbxUseParallelism.text")); //$NON-NLS-1$
		chckbxUseParallelism.addItemListener(e -> settings.setProperty(SettingsEnum.use_parallelism, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		chckbxUseParallelism.setToolTipText(Messages.getString("MainFrame.chckbxUseParallelism.toolTipText")); //$NON-NLS-1$
		final GridBagConstraints gbcChckbxUseParallelism = new GridBagConstraints();
		gbcChckbxUseParallelism.insets = new Insets(0, 0, 5, 5);
		gbcChckbxUseParallelism.fill = GridBagConstraints.BOTH;
		gbcChckbxUseParallelism.gridx = 0;
		gbcChckbxUseParallelism.gridy = 1;
		this.add(chckbxUseParallelism, gbcChckbxUseParallelism);

		chckbxCreateMissingSets = new JCheckBox(Messages.getString("MainFrame.chckbxCreateMissingSets.text")); //$NON-NLS-1$
		chckbxCreateMissingSets.addItemListener(e -> {
			settings.setProperty(SettingsEnum.create_mode, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
			if (e.getStateChange() != ItemEvent.SELECTED)
				chckbxCreateOnlyComplete.setSelected(false);
			chckbxCreateOnlyComplete.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
		});
		final GridBagConstraints gbcChckbxCreateMissingSets = new GridBagConstraints();
		gbcChckbxCreateMissingSets.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxCreateMissingSets.insets = new Insets(0, 0, 5, 0);
		gbcChckbxCreateMissingSets.gridx = 1;
		gbcChckbxCreateMissingSets.gridy = 0;
		this.add(chckbxCreateMissingSets, gbcChckbxCreateMissingSets);
		
		chckbxCreateOnlyComplete = new JCheckBox(Messages.getString("MainFrame.chckbxCreateOnlyComplete.text")); //$NON-NLS-1$
		chckbxCreateOnlyComplete.addItemListener(e -> settings.setProperty(SettingsEnum.createfull_mode, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbcChckbxCreateOnlyComplete = new GridBagConstraints();
		gbcChckbxCreateOnlyComplete.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxCreateOnlyComplete.insets = new Insets(0, 0, 5, 0);
		gbcChckbxCreateOnlyComplete.gridx = 1;
		gbcChckbxCreateOnlyComplete.gridy = 1;
		this.add(chckbxCreateOnlyComplete, gbcChckbxCreateOnlyComplete);

		chckbxIgnoreUnneededContainers = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreUnneededContainers.text")); //$NON-NLS-1$
		chckbxIgnoreUnneededContainers.addItemListener(e -> settings.setProperty(SettingsEnum.ignore_unneeded_containers, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbcChckbxIgnoreUnneededContainers = new GridBagConstraints();
		gbcChckbxIgnoreUnneededContainers.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxIgnoreUnneededContainers.insets = new Insets(0, 0, 5, 5);
		gbcChckbxIgnoreUnneededContainers.gridx = 0;
		gbcChckbxIgnoreUnneededContainers.gridy = 2;
		this.add(chckbxIgnoreUnneededContainers, gbcChckbxIgnoreUnneededContainers);

		chckbxIgnoreUnneededEntries = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreUnneededEntries.text")); //$NON-NLS-1$
		chckbxIgnoreUnneededEntries.addItemListener(e -> settings.setProperty(SettingsEnum.ignore_unneeded_entries, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbcchckbxIgnoreUnneededEntries = new GridBagConstraints();
		gbcchckbxIgnoreUnneededEntries.fill = GridBagConstraints.HORIZONTAL;
		gbcchckbxIgnoreUnneededEntries.insets = new Insets(0, 0, 5, 0);
		gbcchckbxIgnoreUnneededEntries.gridx = 1;
		gbcchckbxIgnoreUnneededEntries.gridy = 2;
		this.add(chckbxIgnoreUnneededEntries, gbcchckbxIgnoreUnneededEntries);

		chckbxIgnoreUnknownContainers = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreUnknownContainers.text")); //$NON-NLS-1$
		chckbxIgnoreUnknownContainers.addItemListener(e -> settings.setProperty(SettingsEnum.ignore_unknown_containers, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbcChckbxIgnoreUnknownContainers = new GridBagConstraints();
		gbcChckbxIgnoreUnknownContainers.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxIgnoreUnknownContainers.insets = new Insets(0, 0, 5, 5);
		gbcChckbxIgnoreUnknownContainers.gridx = 0;
		gbcChckbxIgnoreUnknownContainers.gridy = 3;
		this.add(chckbxIgnoreUnknownContainers, gbcChckbxIgnoreUnknownContainers);

		chckbxUseImplicitMerge = new JCheckBox(Messages.getString("MainFrame.chckbxUseImplicitMerge.text")); //$NON-NLS-1$
		chckbxUseImplicitMerge.addItemListener(e -> settings.setProperty(SettingsEnum.implicit_merge, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbcChckbxUseImplicitMerge = new GridBagConstraints();
		gbcChckbxUseImplicitMerge.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxUseImplicitMerge.insets = new Insets(0, 0, 5, 0);
		gbcChckbxUseImplicitMerge.gridx = 1;
		gbcChckbxUseImplicitMerge.gridy = 3;
		this.add(chckbxUseImplicitMerge, gbcChckbxUseImplicitMerge);

		chckbxIgnoreMergeNameRoms = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreMergeName.text")); //$NON-NLS-1$
		chckbxIgnoreMergeNameRoms.addItemListener(e -> settings.setProperty(SettingsEnum.ignore_merge_name_roms, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbcChckbxIgnoreMergeNameRoms = new GridBagConstraints();
		gbcChckbxIgnoreMergeNameRoms.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxIgnoreMergeNameRoms.insets = new Insets(0, 0, 5, 5);
		gbcChckbxIgnoreMergeNameRoms.gridx = 0;
		gbcChckbxIgnoreMergeNameRoms.gridy = 4;
		this.add(chckbxIgnoreMergeNameRoms, gbcChckbxIgnoreMergeNameRoms);

		chckbxIgnoreMergeNameDisks = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreMergeName_1.text")); //$NON-NLS-1$
		chckbxIgnoreMergeNameDisks.addItemListener(e -> settings.setProperty(SettingsEnum.ignore_merge_name_disks, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbcChckbxIgnoreMergeNameDisks = new GridBagConstraints();
		gbcChckbxIgnoreMergeNameDisks.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxIgnoreMergeNameDisks.insets = new Insets(0, 0, 5, 0);
		gbcChckbxIgnoreMergeNameDisks.gridx = 1;
		gbcChckbxIgnoreMergeNameDisks.gridy = 4;
		this.add(chckbxIgnoreMergeNameDisks, gbcChckbxIgnoreMergeNameDisks);

		chckbxExcludeGames = new JCheckBox(Messages.getString("MainFrame.chckbxExcludeGames.text")); //$NON-NLS-1$
		chckbxExcludeGames.addItemListener(e -> settings.setProperty(SettingsEnum.exclude_games, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbcChckbxExcludeGames = new GridBagConstraints();
		gbcChckbxExcludeGames.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxExcludeGames.insets = new Insets(0, 0, 5, 5);
		gbcChckbxExcludeGames.gridx = 0;
		gbcChckbxExcludeGames.gridy = 5;
		this.add(chckbxExcludeGames, gbcChckbxExcludeGames);

		chckbxExcludeMachines = new JCheckBox(Messages.getString("MainFrame.chckbxExcludeMachines.text")); //$NON-NLS-1$
		chckbxExcludeMachines.addItemListener(e -> settings.setProperty(SettingsEnum.exclude_machines, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbcChckbxExcludeMachines = new GridBagConstraints();
		gbcChckbxExcludeMachines.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxExcludeMachines.insets = new Insets(0, 0, 5, 0);
		gbcChckbxExcludeMachines.gridx = 1;
		gbcChckbxExcludeMachines.gridy = 5;
		this.add(chckbxExcludeMachines, gbcChckbxExcludeMachines);

		chckbxBackup = new JCheckBox(Messages.getString("MainFrame.chckbxBackup.text")); //$NON-NLS-1$
		chckbxBackup.addItemListener(e -> settings.setProperty(SettingsEnum.backup, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbcChckbxBackup = new GridBagConstraints();
		gbcChckbxBackup.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxBackup.insets = new Insets(0, 0, 5, 5);
		gbcChckbxBackup.gridx = 0;
		gbcChckbxBackup.gridy = 6;
		this.add(chckbxBackup, gbcChckbxBackup);

		scannerSubSettingsPanel = new JPanel();
		final GridBagConstraints gbcScannerSubSettingsPanel = new GridBagConstraints();
		gbcScannerSubSettingsPanel.gridwidth = 2;
		gbcScannerSubSettingsPanel.fill = GridBagConstraints.BOTH;
		gbcScannerSubSettingsPanel.gridx = 0;
		gbcScannerSubSettingsPanel.gridy = 7;
		this.add(scannerSubSettingsPanel, gbcScannerSubSettingsPanel);
		final GridBagLayout gblScannerSubSettingsPanel = new GridBagLayout();
		gblScannerSubSettingsPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gblScannerSubSettingsPanel.rowHeights = new int[] { 0, 0, 0, 8, 100, 0 };
		gblScannerSubSettingsPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gblScannerSubSettingsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		scannerSubSettingsPanel.setLayout(gblScannerSubSettingsPanel);

		JLabel lblCompression = new JLabel(Messages.getString("MainFrame.lblCompression.text")); //$NON-NLS-1$
		final GridBagConstraints gbcLblCompression = new GridBagConstraints();
		gbcLblCompression.anchor = GridBagConstraints.EAST;
		gbcLblCompression.insets = new Insets(0, 5, 5, 5);
		gbcLblCompression.gridx = 0;
		gbcLblCompression.gridy = 0;
		scannerSubSettingsPanel.add(lblCompression, gbcLblCompression);

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
		cbCompression.addActionListener(e -> settings.setProperty(SettingsEnum.format, cbCompression.getSelectedItem().toString())); //$NON-NLS-1$
		final GridBagConstraints gbcCBCompression = new GridBagConstraints();
		gbcCBCompression.gridwidth = 2;
		gbcCBCompression.insets = new Insets(0, 0, 5, 5);
		gbcCBCompression.fill = GridBagConstraints.HORIZONTAL;
		gbcCBCompression.gridx = 1;
		gbcCBCompression.gridy = 0;
		scannerSubSettingsPanel.add(cbCompression, gbcCBCompression);

		JLabel lblMergeMode = new JLabel(Messages.getString("MainFrame.lblMergeMode.text")); //$NON-NLS-1$
		final GridBagConstraints gbcLblMergeMode = new GridBagConstraints();
		gbcLblMergeMode.insets = new Insets(0, 0, 5, 5);
		gbcLblMergeMode.anchor = GridBagConstraints.EAST;
		gbcLblMergeMode.gridx = 0;
		gbcLblMergeMode.gridy = 1;
		scannerSubSettingsPanel.add(lblMergeMode, gbcLblMergeMode);

		cbbxMergeMode = new JComboBox<>();
		final GridBagConstraints gbcCbbxMergeMode = new GridBagConstraints();
		gbcCbbxMergeMode.insets = new Insets(0, 0, 5, 5);
		gbcCbbxMergeMode.gridwidth = 2;
		gbcCbbxMergeMode.fill = GridBagConstraints.HORIZONTAL;
		gbcCbbxMergeMode.gridx = 1;
		gbcCbbxMergeMode.gridy = 1;
		scannerSubSettingsPanel.add(cbbxMergeMode, gbcCbbxMergeMode);
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
			settings.setProperty(SettingsEnum.merge_mode, cbbxMergeMode.getSelectedItem().toString()); //$NON-NLS-1$
			cbHashCollision.setEnabled(((MergeOptions) cbbxMergeMode.getSelectedItem()).isMerge());
		});

		JLabel lblHashCollision = new JLabel(Messages.getString("MainFrame.lblHashCollision.text")); //$NON-NLS-1$
		final GridBagConstraints gbcLblHashCollision = new GridBagConstraints();
		gbcLblHashCollision.insets = new Insets(0, 0, 5, 5);
		gbcLblHashCollision.anchor = GridBagConstraints.EAST;
		gbcLblHashCollision.gridx = 0;
		gbcLblHashCollision.gridy = 2;
		scannerSubSettingsPanel.add(lblHashCollision, gbcLblHashCollision);

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
		cbHashCollision.addActionListener(e -> settings.setProperty(SettingsEnum.hash_collision_mode, cbHashCollision.getSelectedItem().toString())); //$NON-NLS-1$
		final GridBagConstraints gbcCbHashCollision = new GridBagConstraints();
		gbcCbHashCollision.gridwidth = 2;
		gbcCbHashCollision.insets = new Insets(0, 0, 5, 5);
		gbcCbHashCollision.fill = GridBagConstraints.HORIZONTAL;
		gbcCbHashCollision.gridx = 1;
		gbcCbHashCollision.gridy = 2;
		scannerSubSettingsPanel.add(cbHashCollision, gbcCbHashCollision);

	}
	
	private transient @Getter ProfileSettings settings; 
	
	public void initProfileSettings(@SuppressWarnings("exports") final ProfileSettings settings)
	{
		this.settings = settings;
		chckbxNeedSHA1.setSelected(settings.getProperty(SettingsEnum.need_sha1_or_md5, false)); //$NON-NLS-1$
		chckbxUseParallelism.setSelected(settings.getProperty(SettingsEnum.use_parallelism, false)); //$NON-NLS-1$
		chckbxCreateMissingSets.setSelected(settings.getProperty(SettingsEnum.create_mode, true)); //$NON-NLS-1$
		chckbxCreateOnlyComplete.setSelected(settings.getProperty(SettingsEnum.createfull_mode, false) && chckbxCreateMissingSets.isSelected()); //$NON-NLS-1$
		chckbxIgnoreUnneededContainers.setSelected(settings.getProperty(SettingsEnum.ignore_unneeded_containers, false)); //$NON-NLS-1$
		chckbxIgnoreUnneededEntries.setSelected(settings.getProperty(SettingsEnum.ignore_unneeded_entries, false)); //$NON-NLS-1$
		chckbxIgnoreUnknownContainers.setSelected(settings.getProperty(SettingsEnum.ignore_unknown_containers, false)); //$NON-NLS-1$
		chckbxUseImplicitMerge.setSelected(settings.getProperty(SettingsEnum.implicit_merge, false)); //$NON-NLS-1$
		chckbxIgnoreMergeNameRoms.setSelected(settings.getProperty(SettingsEnum.ignore_merge_name_roms, false)); //$NON-NLS-1$
		chckbxIgnoreMergeNameDisks.setSelected(settings.getProperty(SettingsEnum.ignore_merge_name_disks, false)); //$NON-NLS-1$
		chckbxExcludeGames.setSelected(settings.getProperty(SettingsEnum.exclude_games, false)); //$NON-NLS-1$
		chckbxExcludeMachines.setSelected(settings.getProperty(SettingsEnum.exclude_machines, false)); //$NON-NLS-1$
		chckbxBackup.setSelected(settings.getProperty(SettingsEnum.backup, true)); //$NON-NLS-1$
		
		cbCompression.setSelectedItem(FormatOptions.valueOf(settings.getProperty(SettingsEnum.format, FormatOptions.ZIP.toString()))); //$NON-NLS-1$
		cbbxMergeMode.setSelectedItem(MergeOptions.valueOf(settings.getProperty(SettingsEnum.merge_mode, MergeOptions.SPLIT.toString()))); //$NON-NLS-1$
		cbHashCollision.setSelectedItem(HashCollisionOptions.valueOf(settings.getProperty(SettingsEnum.hash_collision_mode, HashCollisionOptions.SINGLEFILE.toString()))); //$NON-NLS-1$

		cbHashCollision.setEnabled(((MergeOptions) cbbxMergeMode.getSelectedItem()).isMerge());
		chckbxCreateOnlyComplete.setEnabled(chckbxCreateMissingSets.isSelected());
}

}
