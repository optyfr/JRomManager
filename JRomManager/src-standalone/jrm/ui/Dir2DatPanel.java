package jrm.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import jrm.locale.Messages;
import jrm.profile.manager.Export.ExportType;
import jrm.profile.scan.Dir2Dat;
import jrm.profile.scan.DirScan;
import jrm.profile.scan.DirScan.Options;
import jrm.security.Session;
import jrm.ui.basic.JFileDropMode;
import jrm.ui.basic.JFileDropTextField;
import jrm.ui.basic.JRMFileChooser;
import jrm.ui.basic.JTextFieldHintUI;
import jrm.ui.progress.SwingWorkerProgress;

@SuppressWarnings("serial")
public class Dir2DatPanel extends JPanel
{
	private JFileDropTextField tfDir2DatSrc;
	private JFileDropTextField tfDir2DatDst;
	private final ButtonGroup Dir2DatFormatGroup = new ButtonGroup();
	private JTextField tfDir2DatName;
	private JTextField tfDir2DatDescription;
	private JTextField tfDir2DatVersion;
	private JTextField tfDir2DatAuthor;
	private JTextField tfDir2DatComment;
	private JTextField tfDir2DatCategory;
	private JTextField tfDir2DatDate;
	private JTextField tfDir2DatEMail;
	private JTextField tfDir2DatHomepage;
	private JTextField tfDir2DatURL;
	
	/**
	 * Create the panel.
	 */
	public Dir2DatPanel(final Session session)
	{
		GridBagLayout gbl_dir2datTab = new GridBagLayout();
		gbl_dir2datTab.columnWidths = new int[] { 0, 0, 0 };
		gbl_dir2datTab.rowHeights = new int[] { 0, 0, 0 };
		gbl_dir2datTab.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_dir2datTab.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		this.setLayout(gbl_dir2datTab);

		JPanel panelDir2DatOptions = new JPanel();
		panelDir2DatOptions.setBorder(new TitledBorder(null, Messages.getString("MainFrame.Options"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		GridBagConstraints gbc_panelDir2DatOptions = new GridBagConstraints();
		gbc_panelDir2DatOptions.insets = new Insets(0, 5, 5, 5);
		gbc_panelDir2DatOptions.fill = GridBagConstraints.BOTH;
		gbc_panelDir2DatOptions.gridx = 0;
		gbc_panelDir2DatOptions.gridy = 0;
		this.add(panelDir2DatOptions, gbc_panelDir2DatOptions);
		GridBagLayout gbl_panelDir2DatOptions = new GridBagLayout();
		gbl_panelDir2DatOptions.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelDir2DatOptions.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelDir2DatOptions.columnWeights = new double[] { 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelDir2DatOptions.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelDir2DatOptions.setLayout(gbl_panelDir2DatOptions);

		JCheckBox cbDir2DatScanSubfolders = new JCheckBox(Messages.getString("MainFrame.chckbxScanSubfolders.text")); //$NON-NLS-1$
		cbDir2DatScanSubfolders.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_scan_subfolders, true)); //$NON-NLS-1$
		cbDir2DatScanSubfolders.addItemListener(e -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_scan_subfolders, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_cbDir2DatScanSubfolders = new GridBagConstraints();
		gbc_cbDir2DatScanSubfolders.anchor = GridBagConstraints.WEST;
		gbc_cbDir2DatScanSubfolders.insets = new Insets(0, 0, 5, 5);
		gbc_cbDir2DatScanSubfolders.gridx = 1;
		gbc_cbDir2DatScanSubfolders.gridy = 1;
		panelDir2DatOptions.add(cbDir2DatScanSubfolders, gbc_cbDir2DatScanSubfolders);

		JCheckBox cbDir2DatDeepScan = new JCheckBox(Messages.getString("MainFrame.chckbxDeepScanFor.text")); //$NON-NLS-1$
		cbDir2DatDeepScan.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_deep_scan, false)); //$NON-NLS-1$
		cbDir2DatDeepScan.addItemListener(e -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_deep_scan, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_cbDir2DatDeepScan = new GridBagConstraints();
		gbc_cbDir2DatDeepScan.anchor = GridBagConstraints.WEST;
		gbc_cbDir2DatDeepScan.insets = new Insets(0, 0, 5, 5);
		gbc_cbDir2DatDeepScan.gridx = 1;
		gbc_cbDir2DatDeepScan.gridy = 2;
		panelDir2DatOptions.add(cbDir2DatDeepScan, gbc_cbDir2DatDeepScan);

		JCheckBox cbDir2DatAddMd5 = new JCheckBox(Messages.getString("MainFrame.chckbxAddMd.text")); //$NON-NLS-1$
		cbDir2DatAddMd5.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_add_md5, false)); //$NON-NLS-1$
		cbDir2DatAddMd5.addItemListener(e -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_add_md5, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_cbDir2DatAddMd5 = new GridBagConstraints();
		gbc_cbDir2DatAddMd5.anchor = GridBagConstraints.WEST;
		gbc_cbDir2DatAddMd5.insets = new Insets(0, 0, 5, 5);
		gbc_cbDir2DatAddMd5.gridx = 1;
		gbc_cbDir2DatAddMd5.gridy = 3;
		panelDir2DatOptions.add(cbDir2DatAddMd5, gbc_cbDir2DatAddMd5);

		JCheckBox cbDir2DatAddSha1 = new JCheckBox(Messages.getString("MainFrame.chckbxAddShamd.text")); //$NON-NLS-1$
		cbDir2DatAddSha1.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_add_sha1, false)); //$NON-NLS-1$
		cbDir2DatAddSha1.addItemListener(e -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_add_sha1, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_cbDir2DatAddSha1 = new GridBagConstraints();
		gbc_cbDir2DatAddSha1.anchor = GridBagConstraints.WEST;
		gbc_cbDir2DatAddSha1.insets = new Insets(0, 0, 5, 5);
		gbc_cbDir2DatAddSha1.gridx = 1;
		gbc_cbDir2DatAddSha1.gridy = 4;
		panelDir2DatOptions.add(cbDir2DatAddSha1, gbc_cbDir2DatAddSha1);

		JCheckBox cbDir2DatJunkSubfolders = new JCheckBox(Messages.getString("MainFrame.chckbxJunkSubfolders.text")); //$NON-NLS-1$
		cbDir2DatJunkSubfolders.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_junk_folders, false)); //$NON-NLS-1$
		cbDir2DatJunkSubfolders.addItemListener(e -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_junk_folders, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_cbDir2DatJunkSubfolders = new GridBagConstraints();
		gbc_cbDir2DatJunkSubfolders.anchor = GridBagConstraints.WEST;
		gbc_cbDir2DatJunkSubfolders.insets = new Insets(0, 0, 5, 5);
		gbc_cbDir2DatJunkSubfolders.gridx = 1;
		gbc_cbDir2DatJunkSubfolders.gridy = 5;
		panelDir2DatOptions.add(cbDir2DatJunkSubfolders, gbc_cbDir2DatJunkSubfolders);

		JCheckBox cbDir2DatDoNotScan = new JCheckBox(Messages.getString("MainFrame.chckbxDoNotScan.text")); //$NON-NLS-1$
		cbDir2DatDoNotScan.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_do_not_scan_archives, false)); //$NON-NLS-1$
		cbDir2DatDoNotScan.addItemListener(e -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_do_not_scan_archives, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_cbDir2DatDoNotScan = new GridBagConstraints();
		gbc_cbDir2DatDoNotScan.anchor = GridBagConstraints.WEST;
		gbc_cbDir2DatDoNotScan.insets = new Insets(0, 0, 5, 5);
		gbc_cbDir2DatDoNotScan.gridx = 1;
		gbc_cbDir2DatDoNotScan.gridy = 6;
		panelDir2DatOptions.add(cbDir2DatDoNotScan, gbc_cbDir2DatDoNotScan);

		JCheckBox cbDir2DatMatchCurrentProfile = new JCheckBox(Messages.getString("MainFrame.chckbxMatchCurrentProfile.text")); //$NON-NLS-1$
		cbDir2DatMatchCurrentProfile.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_match_profile, false)); //$NON-NLS-1$
		cbDir2DatMatchCurrentProfile.addItemListener(e -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_match_profile, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_cbDir2DatMatchCurrentProfile = new GridBagConstraints();
		gbc_cbDir2DatMatchCurrentProfile.anchor = GridBagConstraints.WEST;
		gbc_cbDir2DatMatchCurrentProfile.insets = new Insets(0, 0, 5, 5);
		gbc_cbDir2DatMatchCurrentProfile.gridx = 1;
		gbc_cbDir2DatMatchCurrentProfile.gridy = 7;
		panelDir2DatOptions.add(cbDir2DatMatchCurrentProfile, gbc_cbDir2DatMatchCurrentProfile);

		JCheckBox cbDir2DatIncludeEmptyDirs = new JCheckBox(Messages.getString("MainFrame.chckbxIncludeEmptyDirs.text")); //$NON-NLS-1$
		cbDir2DatIncludeEmptyDirs.setSelected(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_include_empty_dirs, false)); //$NON-NLS-1$
		cbDir2DatIncludeEmptyDirs.addItemListener(e -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_include_empty_dirs, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_cbDir2DatIncludeEmptyDirs = new GridBagConstraints();
		gbc_cbDir2DatIncludeEmptyDirs.anchor = GridBagConstraints.WEST;
		gbc_cbDir2DatIncludeEmptyDirs.insets = new Insets(0, 0, 5, 5);
		gbc_cbDir2DatIncludeEmptyDirs.gridx = 1;
		gbc_cbDir2DatIncludeEmptyDirs.gridy = 8;
		panelDir2DatOptions.add(cbDir2DatIncludeEmptyDirs, gbc_cbDir2DatIncludeEmptyDirs);

		JPanel panelDir2DatHeaders = new JPanel();
		panelDir2DatHeaders.setBorder(new TitledBorder(null, Messages.getString("MainFrame.Headers"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		GridBagConstraints gbc_panelDir2DatHeaders = new GridBagConstraints();
		gbc_panelDir2DatHeaders.insets = new Insets(0, 0, 5, 5);
		gbc_panelDir2DatHeaders.fill = GridBagConstraints.BOTH;
		gbc_panelDir2DatHeaders.gridx = 1;
		gbc_panelDir2DatHeaders.gridy = 0;
		this.add(panelDir2DatHeaders, gbc_panelDir2DatHeaders);
		GridBagLayout gbl_panelDir2DatHeaders = new GridBagLayout();
		gbl_panelDir2DatHeaders.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelDir2DatHeaders.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelDir2DatHeaders.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelDir2DatHeaders.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelDir2DatHeaders.setLayout(gbl_panelDir2DatHeaders);

		JLabel lblDir2DatName = new JLabel(Messages.getString("MainFrame.lblName.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDir2DatName = new GridBagConstraints();
		gbc_lblDir2DatName.anchor = GridBagConstraints.EAST;
		gbc_lblDir2DatName.insets = new Insets(0, 0, 5, 5);
		gbc_lblDir2DatName.gridx = 0;
		gbc_lblDir2DatName.gridy = 1;
		panelDir2DatHeaders.add(lblDir2DatName, gbc_lblDir2DatName);

		tfDir2DatName = new JTextField();
		tfDir2DatName.setText(""); //$NON-NLS-1$
		GridBagConstraints gbc_tfDir2DatName = new GridBagConstraints();
		gbc_tfDir2DatName.insets = new Insets(0, 0, 5, 0);
		gbc_tfDir2DatName.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfDir2DatName.gridx = 1;
		gbc_tfDir2DatName.gridy = 1;
		panelDir2DatHeaders.add(tfDir2DatName, gbc_tfDir2DatName);
		tfDir2DatName.setColumns(20);

		JLabel lblDir2DatDescription = new JLabel(Messages.getString("MainFrame.lblDescription.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDir2DatDescription = new GridBagConstraints();
		gbc_lblDir2DatDescription.anchor = GridBagConstraints.EAST;
		gbc_lblDir2DatDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDir2DatDescription.gridx = 0;
		gbc_lblDir2DatDescription.gridy = 2;
		panelDir2DatHeaders.add(lblDir2DatDescription, gbc_lblDir2DatDescription);

		tfDir2DatDescription = new JTextField();
		tfDir2DatDescription.setText(""); //$NON-NLS-1$
		tfDir2DatDescription.setColumns(20);
		GridBagConstraints gbc_tfDir2DatDescription = new GridBagConstraints();
		gbc_tfDir2DatDescription.insets = new Insets(0, 0, 5, 0);
		gbc_tfDir2DatDescription.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfDir2DatDescription.gridx = 1;
		gbc_tfDir2DatDescription.gridy = 2;
		panelDir2DatHeaders.add(tfDir2DatDescription, gbc_tfDir2DatDescription);

		JLabel lblDir2DatVersion = new JLabel(Messages.getString("MainFrame.lblVersion.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDir2DatVersion = new GridBagConstraints();
		gbc_lblDir2DatVersion.anchor = GridBagConstraints.EAST;
		gbc_lblDir2DatVersion.insets = new Insets(0, 0, 5, 5);
		gbc_lblDir2DatVersion.gridx = 0;
		gbc_lblDir2DatVersion.gridy = 3;
		panelDir2DatHeaders.add(lblDir2DatVersion, gbc_lblDir2DatVersion);

		tfDir2DatVersion = new JTextField();
		tfDir2DatVersion.setText(""); //$NON-NLS-1$
		tfDir2DatVersion.setColumns(20);
		GridBagConstraints gbc_tfDir2DatVersion = new GridBagConstraints();
		gbc_tfDir2DatVersion.insets = new Insets(0, 0, 5, 0);
		gbc_tfDir2DatVersion.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfDir2DatVersion.gridx = 1;
		gbc_tfDir2DatVersion.gridy = 3;
		panelDir2DatHeaders.add(tfDir2DatVersion, gbc_tfDir2DatVersion);

		JLabel lblDir2DatAuthor = new JLabel(Messages.getString("MainFrame.lblAuthor.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDir2DatAuthor = new GridBagConstraints();
		gbc_lblDir2DatAuthor.anchor = GridBagConstraints.EAST;
		gbc_lblDir2DatAuthor.insets = new Insets(0, 0, 5, 5);
		gbc_lblDir2DatAuthor.gridx = 0;
		gbc_lblDir2DatAuthor.gridy = 4;
		panelDir2DatHeaders.add(lblDir2DatAuthor, gbc_lblDir2DatAuthor);

		tfDir2DatAuthor = new JTextField();
		tfDir2DatAuthor.setText(""); //$NON-NLS-1$
		tfDir2DatAuthor.setColumns(20);
		GridBagConstraints gbc_tfDir2DatAuthor = new GridBagConstraints();
		gbc_tfDir2DatAuthor.insets = new Insets(0, 0, 5, 0);
		gbc_tfDir2DatAuthor.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfDir2DatAuthor.gridx = 1;
		gbc_tfDir2DatAuthor.gridy = 4;
		panelDir2DatHeaders.add(tfDir2DatAuthor, gbc_tfDir2DatAuthor);

		JLabel lblDir2DatComment = new JLabel(Messages.getString("MainFrame.lblComment.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDir2DatComment = new GridBagConstraints();
		gbc_lblDir2DatComment.insets = new Insets(0, 0, 5, 5);
		gbc_lblDir2DatComment.anchor = GridBagConstraints.EAST;
		gbc_lblDir2DatComment.gridx = 0;
		gbc_lblDir2DatComment.gridy = 5;
		panelDir2DatHeaders.add(lblDir2DatComment, gbc_lblDir2DatComment);

		tfDir2DatComment = new JTextField();
		tfDir2DatComment.setText(""); //$NON-NLS-1$
		tfDir2DatComment.setColumns(20);
		GridBagConstraints gbc_tfDir2DatComment = new GridBagConstraints();
		gbc_tfDir2DatComment.insets = new Insets(0, 0, 5, 0);
		gbc_tfDir2DatComment.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfDir2DatComment.gridx = 1;
		gbc_tfDir2DatComment.gridy = 5;
		panelDir2DatHeaders.add(tfDir2DatComment, gbc_tfDir2DatComment);

		JLabel lblDir2DatCategory = new JLabel(Messages.getString("MainFrame.lblCategory.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDir2DatCategory = new GridBagConstraints();
		gbc_lblDir2DatCategory.anchor = GridBagConstraints.EAST;
		gbc_lblDir2DatCategory.insets = new Insets(0, 0, 5, 5);
		gbc_lblDir2DatCategory.gridx = 0;
		gbc_lblDir2DatCategory.gridy = 6;
		panelDir2DatHeaders.add(lblDir2DatCategory, gbc_lblDir2DatCategory);

		tfDir2DatCategory = new JTextField();
		tfDir2DatCategory.setText(""); //$NON-NLS-1$
		tfDir2DatCategory.setColumns(20);
		GridBagConstraints gbc_tfDir2DatCategory = new GridBagConstraints();
		gbc_tfDir2DatCategory.insets = new Insets(0, 0, 5, 0);
		gbc_tfDir2DatCategory.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfDir2DatCategory.gridx = 1;
		gbc_tfDir2DatCategory.gridy = 6;
		panelDir2DatHeaders.add(tfDir2DatCategory, gbc_tfDir2DatCategory);

		JLabel lblDir2DatDate = new JLabel(Messages.getString("MainFrame.lblDate.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDir2DatDate = new GridBagConstraints();
		gbc_lblDir2DatDate.anchor = GridBagConstraints.EAST;
		gbc_lblDir2DatDate.insets = new Insets(0, 0, 5, 5);
		gbc_lblDir2DatDate.gridx = 0;
		gbc_lblDir2DatDate.gridy = 7;
		panelDir2DatHeaders.add(lblDir2DatDate, gbc_lblDir2DatDate);

		tfDir2DatDate = new JTextField();
		tfDir2DatDate.setText(""); //$NON-NLS-1$
		tfDir2DatDate.setColumns(20);
		GridBagConstraints gbc_tfDir2DatDate = new GridBagConstraints();
		gbc_tfDir2DatDate.insets = new Insets(0, 0, 5, 0);
		gbc_tfDir2DatDate.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfDir2DatDate.gridx = 1;
		gbc_tfDir2DatDate.gridy = 7;
		panelDir2DatHeaders.add(tfDir2DatDate, gbc_tfDir2DatDate);

		JLabel lblDir2DatEmail = new JLabel(Messages.getString("MainFrame.lblEmail.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDir2DatEmail = new GridBagConstraints();
		gbc_lblDir2DatEmail.anchor = GridBagConstraints.EAST;
		gbc_lblDir2DatEmail.insets = new Insets(0, 0, 5, 5);
		gbc_lblDir2DatEmail.gridx = 0;
		gbc_lblDir2DatEmail.gridy = 8;
		panelDir2DatHeaders.add(lblDir2DatEmail, gbc_lblDir2DatEmail);

		tfDir2DatEMail = new JTextField();
		tfDir2DatEMail.setText(""); //$NON-NLS-1$
		tfDir2DatEMail.setColumns(20);
		GridBagConstraints gbc_tfDir2DatEMail = new GridBagConstraints();
		gbc_tfDir2DatEMail.insets = new Insets(0, 0, 5, 0);
		gbc_tfDir2DatEMail.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfDir2DatEMail.gridx = 1;
		gbc_tfDir2DatEMail.gridy = 8;
		panelDir2DatHeaders.add(tfDir2DatEMail, gbc_tfDir2DatEMail);

		JLabel lblDir2DatHomepage = new JLabel(Messages.getString("MainFrame.lblHomepage.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDir2DatHomepage = new GridBagConstraints();
		gbc_lblDir2DatHomepage.anchor = GridBagConstraints.EAST;
		gbc_lblDir2DatHomepage.insets = new Insets(0, 0, 5, 5);
		gbc_lblDir2DatHomepage.gridx = 0;
		gbc_lblDir2DatHomepage.gridy = 9;
		panelDir2DatHeaders.add(lblDir2DatHomepage, gbc_lblDir2DatHomepage);

		tfDir2DatHomepage = new JTextField();
		tfDir2DatHomepage.setText(""); //$NON-NLS-1$
		tfDir2DatHomepage.setColumns(20);
		GridBagConstraints gbc_tfDir2DatHomepage = new GridBagConstraints();
		gbc_tfDir2DatHomepage.insets = new Insets(0, 0, 5, 0);
		gbc_tfDir2DatHomepage.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfDir2DatHomepage.gridx = 1;
		gbc_tfDir2DatHomepage.gridy = 9;
		panelDir2DatHeaders.add(tfDir2DatHomepage, gbc_tfDir2DatHomepage);

		JLabel lblDir2DatUrl = new JLabel(Messages.getString("MainFrame.lblUrl.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDir2DatUrl = new GridBagConstraints();
		gbc_lblDir2DatUrl.insets = new Insets(0, 0, 5, 5);
		gbc_lblDir2DatUrl.anchor = GridBagConstraints.EAST;
		gbc_lblDir2DatUrl.gridx = 0;
		gbc_lblDir2DatUrl.gridy = 10;
		panelDir2DatHeaders.add(lblDir2DatUrl, gbc_lblDir2DatUrl);

		tfDir2DatURL = new JTextField();
		tfDir2DatURL.setText(""); //$NON-NLS-1$
		tfDir2DatURL.setColumns(20);
		GridBagConstraints gbc_tfDir2DatURL = new GridBagConstraints();
		gbc_tfDir2DatURL.insets = new Insets(0, 0, 5, 0);
		gbc_tfDir2DatURL.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfDir2DatURL.gridx = 1;
		gbc_tfDir2DatURL.gridy = 10;
		panelDir2DatHeaders.add(tfDir2DatURL, gbc_tfDir2DatURL);

		JPanel panelDir2DatIO = new JPanel();
		panelDir2DatIO.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), Messages.getString("MainFrame.IO"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))); //$NON-NLS-1$ //$NON-NLS-2$
		GridBagConstraints gbc_panelDir2DatIO = new GridBagConstraints();
		gbc_panelDir2DatIO.insets = new Insets(0, 5, 5, 5);
		gbc_panelDir2DatIO.gridwidth = 2;
		gbc_panelDir2DatIO.fill = GridBagConstraints.BOTH;
		gbc_panelDir2DatIO.gridx = 0;
		gbc_panelDir2DatIO.gridy = 1;
		this.add(panelDir2DatIO, gbc_panelDir2DatIO);
		GridBagLayout gbl_panelDir2DatIO = new GridBagLayout();
		gbl_panelDir2DatIO.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelDir2DatIO.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_panelDir2DatIO.columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelDir2DatIO.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelDir2DatIO.setLayout(gbl_panelDir2DatIO);

		JLabel lblDir2DatSrc = new JLabel(Messages.getString("MainFrame.lblSrcDir_1.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDir2DatSrc = new GridBagConstraints();
		gbc_lblDir2DatSrc.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDir2DatSrc.insets = new Insets(0, 5, 5, 5);
		gbc_lblDir2DatSrc.gridx = 0;
		gbc_lblDir2DatSrc.gridy = 0;
		panelDir2DatIO.add(lblDir2DatSrc, gbc_lblDir2DatSrc);

		tfDir2DatSrc = new JFileDropTextField(txt -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_src_dir, txt)); //$NON-NLS-1$
		tfDir2DatSrc.setText(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_src_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		tfDir2DatSrc.setMode(JFileDropMode.DIRECTORY);
		tfDir2DatSrc.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tfDir2DatSrc.setColumns(10);
		GridBagConstraints gbc_tfDir2DatSrc = new GridBagConstraints();
		gbc_tfDir2DatSrc.gridwidth = 3;
		gbc_tfDir2DatSrc.insets = new Insets(0, 0, 5, 0);
		gbc_tfDir2DatSrc.fill = GridBagConstraints.BOTH;
		gbc_tfDir2DatSrc.gridx = 1;
		gbc_tfDir2DatSrc.gridy = 0;
		panelDir2DatIO.add(tfDir2DatSrc, gbc_tfDir2DatSrc);

		JButton btnDir2DatSrc = new JButton(""); //$NON-NLS-1$
		btnDir2DatSrc.setIcon(MainFrame.getIcon("/jrm/resicons/icons/disk.png")); //$NON-NLS-1$
		btnDir2DatSrc.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.getUser().getSettings().getProperty("MainFrame.ChooseDatSrc", workdir.getAbsolutePath())), new File(tfDir2DatSrc.getText()), null, Messages.getString("MainFrame.ChooseDatSrc"), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$ //$NON-NLS-2$
				session.getUser().getSettings().setProperty("MainFrame.ChooseDatSrc", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfDir2DatSrc.setText(chooser.getSelectedFile().getAbsolutePath());
				session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_src_dir, tfDir2DatSrc.getText()); //$NON-NLS-1$
				return null;
			});
		});
		GridBagConstraints gbc_btnDir2DatSrc = new GridBagConstraints();
		gbc_btnDir2DatSrc.insets = new Insets(0, 0, 5, 5);
		gbc_btnDir2DatSrc.gridx = 4;
		gbc_btnDir2DatSrc.gridy = 0;
		panelDir2DatIO.add(btnDir2DatSrc, gbc_btnDir2DatSrc);

		JButton btnDir2DatGenerate = new JButton(Messages.getString("MainFrame.btnGenerate.text")); //$NON-NLS-1$
		btnDir2DatGenerate.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				dir2dat(session);
			}
		});
		GridBagConstraints gbc_btnDir2DatGenerate = new GridBagConstraints();
		gbc_btnDir2DatGenerate.fill = GridBagConstraints.BOTH;
		gbc_btnDir2DatGenerate.gridheight = 3;
		gbc_btnDir2DatGenerate.gridx = 5;
		gbc_btnDir2DatGenerate.gridy = 0;
		panelDir2DatIO.add(btnDir2DatGenerate, gbc_btnDir2DatGenerate);

		JLabel lblDir2DatDst = new JLabel(Messages.getString("MainFrame.lblDstDat.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblDir2DatDst = new GridBagConstraints();
		gbc_lblDir2DatDst.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDir2DatDst.insets = new Insets(0, 5, 5, 5);
		gbc_lblDir2DatDst.gridx = 0;
		gbc_lblDir2DatDst.gridy = 1;
		panelDir2DatIO.add(lblDir2DatDst, gbc_lblDir2DatDst);

		tfDir2DatDst = new JFileDropTextField(txt -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_dst_file, txt)); //$NON-NLS-1$
		tfDir2DatDst.setText(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_dst_file, "")); //$NON-NLS-1$ //$NON-NLS-2$
		tfDir2DatDst.setMode(JFileDropMode.FILE);
		tfDir2DatDst.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropFileHint"), Color.gray)); //$NON-NLS-1$
		tfDir2DatDst.setColumns(10);
		GridBagConstraints gbc_tfDir2DatDst = new GridBagConstraints();
		gbc_tfDir2DatDst.gridwidth = 3;
		gbc_tfDir2DatDst.insets = new Insets(0, 0, 5, 0);
		gbc_tfDir2DatDst.fill = GridBagConstraints.BOTH;
		gbc_tfDir2DatDst.gridx = 1;
		gbc_tfDir2DatDst.gridy = 1;
		panelDir2DatIO.add(tfDir2DatDst, gbc_tfDir2DatDst);

		JButton btnDir2DatDst = new JButton(""); //$NON-NLS-1$
		btnDir2DatDst.setIcon(MainFrame.getIcon("/jrm/resicons/icons/disk.png")); //$NON-NLS-1$
		btnDir2DatDst.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Void>(JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY, new File(session.getUser().getSettings().getProperty("MainFrame.ChooseDatDst", workdir.getAbsolutePath())), new File(tfDir2DatDst.getText()), null, Messages.getString("MainFrame.ChooseDatDst"), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$ //$NON-NLS-2$
				session.getUser().getSettings().setProperty("MainFrame.ChooseDatDst", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfDir2DatDst.setText(chooser.getSelectedFile().getAbsolutePath());
				session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_dst_file, tfDir2DatDst.getText()); //$NON-NLS-1$
				return null;
			});
		});
		GridBagConstraints gbc_btnDir2DatDst = new GridBagConstraints();
		gbc_btnDir2DatDst.insets = new Insets(0, 0, 5, 5);
		gbc_btnDir2DatDst.gridx = 4;
		gbc_btnDir2DatDst.gridy = 1;
		panelDir2DatIO.add(btnDir2DatDst, gbc_btnDir2DatDst);

		JLabel lblDir2DatFormat = new JLabel(Messages.getString("MainFrame.lblFormat.text")); //$NON-NLS-1$
		lblDir2DatFormat.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblDir2DatFormat = new GridBagConstraints();
		gbc_lblDir2DatFormat.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDir2DatFormat.insets = new Insets(0, 0, 0, 5);
		gbc_lblDir2DatFormat.gridx = 0;
		gbc_lblDir2DatFormat.gridy = 2;
		panelDir2DatIO.add(lblDir2DatFormat, gbc_lblDir2DatFormat);

		JRadioButton rdbtnDir2DatMame = new JRadioButton(Messages.getString("MainFrame.rdbtnMame.text")); //$NON-NLS-1$
		rdbtnDir2DatMame.setSelected(ExportType.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_format, ExportType.MAME.toString())) == ExportType.MAME); //$NON-NLS-1$
		rdbtnDir2DatMame.addActionListener(e -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_format, ExportType.MAME.toString())); //$NON-NLS-1$
		Dir2DatFormatGroup.add(rdbtnDir2DatMame);
		GridBagConstraints gbc_rdbtnDir2DatMame = new GridBagConstraints();
		gbc_rdbtnDir2DatMame.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnDir2DatMame.gridx = 1;
		gbc_rdbtnDir2DatMame.gridy = 2;
		panelDir2DatIO.add(rdbtnDir2DatMame, gbc_rdbtnDir2DatMame);

		JRadioButton rdbtnDir2DatLogiqxDat = new JRadioButton(Messages.getString("MainFrame.rdbtnLogiqxDat.text")); //$NON-NLS-1$
		rdbtnDir2DatLogiqxDat.setSelected(ExportType.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_format, ExportType.MAME.toString())) == ExportType.DATAFILE); //$NON-NLS-1$
		rdbtnDir2DatLogiqxDat.addActionListener(e -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_format, ExportType.DATAFILE.toString())); //$NON-NLS-1$
		Dir2DatFormatGroup.add(rdbtnDir2DatLogiqxDat);
		GridBagConstraints gbc_rdbtnDir2DatLogiqxDat = new GridBagConstraints();
		gbc_rdbtnDir2DatLogiqxDat.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnDir2DatLogiqxDat.gridx = 2;
		gbc_rdbtnDir2DatLogiqxDat.gridy = 2;
		panelDir2DatIO.add(rdbtnDir2DatLogiqxDat, gbc_rdbtnDir2DatLogiqxDat);

		JRadioButton rdbtnDir2DatSwList = new JRadioButton(Messages.getString("MainFrame.rdbtnSwList.text")); //$NON-NLS-1$
		rdbtnDir2DatSwList.setSelected(ExportType.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_format, ExportType.MAME.toString())) == ExportType.SOFTWARELIST); //$NON-NLS-1$
		rdbtnDir2DatSwList.addActionListener(e -> session.getUser().getSettings().setProperty(jrm.misc.SettingsEnum.dir2dat_format, ExportType.SOFTWARELIST.toString())); //$NON-NLS-1$
		Dir2DatFormatGroup.add(rdbtnDir2DatSwList);
		GridBagConstraints gbc_rdbtnDir2DatSwList = new GridBagConstraints();
		gbc_rdbtnDir2DatSwList.gridwidth = 2;
		gbc_rdbtnDir2DatSwList.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnDir2DatSwList.gridx = 3;
		gbc_rdbtnDir2DatSwList.gridy = 2;
		panelDir2DatIO.add(rdbtnDir2DatSwList, gbc_rdbtnDir2DatSwList);
	}

	/**
	 * Dir2Dat
	 */
	private void dir2dat(final Session session)
	{
		new SwingWorkerProgress<Void, Void>(SwingUtilities.getWindowAncestor(this))
		{
			@Override
			protected Void doInBackground() throws Exception
			{
				final String src = tfDir2DatSrc.getText();
				final String dst = tfDir2DatDst.getText();
				if (src != null && src.length() > 0 && dst != null && dst.length() > 0)
				{
					final File srcdir = new File(src);
					if (srcdir.isDirectory())
					{
						final File dstdat = new File(dst);
						if (dstdat.getParentFile().isDirectory() && (dstdat.exists() || dstdat.createNewFile()))
						{
							EnumSet<DirScan.Options> options = EnumSet.of(Options.USE_PARALLELISM, Options.MD5_DISKS, Options.SHA1_DISKS);
							if (session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_scan_subfolders, true)) //$NON-NLS-1$
								options.add(Options.RECURSE);
							if (!session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_deep_scan, false)) //$NON-NLS-1$
								options.add(Options.IS_DEST);
							if (session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_add_md5, false)) //$NON-NLS-1$
								options.add(Options.NEED_MD5);
							if (session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_add_sha1, false)) //$NON-NLS-1$
								options.add(Options.NEED_SHA1);
							if (session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_junk_folders, false)) //$NON-NLS-1$
								options.add(Options.JUNK_SUBFOLDERS);
							if (session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_do_not_scan_archives, false)) //$NON-NLS-1$
								options.add(Options.ARCHIVES_AND_CHD_AS_ROMS);
							if (session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_match_profile, false)) //$NON-NLS-1$
								options.add(Options.MATCH_PROFILE);
							if (session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_include_empty_dirs, false)) //$NON-NLS-1$
								options.add(Options.EMPTY_DIRS);
							final ExportType type = ExportType.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_format, ExportType.MAME.toString())); //$NON-NLS-1$
							HashMap<String, String> headers = new HashMap<>();
							headers.put("name", tfDir2DatName.getText()); //$NON-NLS-1$
							headers.put("description", tfDir2DatDescription.getText()); //$NON-NLS-1$
							headers.put("version", tfDir2DatVersion.getText()); //$NON-NLS-1$
							headers.put("author", tfDir2DatAuthor.getText()); //$NON-NLS-1$
							headers.put("comment", tfDir2DatComment.getText()); //$NON-NLS-1$
							headers.put("category", tfDir2DatCategory.getText()); //$NON-NLS-1$
							headers.put("date", tfDir2DatDate.getText()); //$NON-NLS-1$
							headers.put("email", tfDir2DatEMail.getText()); //$NON-NLS-1$
							headers.put("homepage", tfDir2DatHomepage.getText()); //$NON-NLS-1$
							headers.put("url", tfDir2DatURL.getText()); //$NON-NLS-1$
							new Dir2Dat(session, srcdir, dstdat, this, options, type, headers);
						}
					}
				}
				return null;
			}

			@Override
			protected void done()
			{
				close();
			}

		}.execute();;
		
	}

}
