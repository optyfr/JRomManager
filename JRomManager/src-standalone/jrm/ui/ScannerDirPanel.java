package jrm.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.lang3.StringUtils;

import jrm.locale.Messages;
import jrm.misc.SettingsEnum;
import jrm.security.Session;
import jrm.ui.basic.JFileDropList;
import jrm.ui.basic.JFileDropMode;
import jrm.ui.basic.JFileDropTextField;
import jrm.ui.basic.JListHintUI;
import jrm.ui.basic.JRMFileChooser;
import jrm.ui.basic.JTextFieldHintUI;

@SuppressWarnings("serial")
public class ScannerDirPanel extends JPanel
{
	private static final String MAIN_FRAME_CHOOSE_SAMPLES_DESTINATION = "MainFrame.ChooseSamplesDestination";

	private static final String MAIN_FRAME_CHOOSE_SW_DISKS_DESTINATION = "MainFrame.ChooseSWDisksDestination";

	private static final String MAIN_FRAME_CHOOSE_SW_ROMS_DESTINATION = "MainFrame.ChooseSWRomsDestination";

	private static final String MAIN_FRAME_CHOOSE_DISKS_DESTINATION = "MainFrame.ChooseDisksDestination";

	private static final String MAIN_FRAME_CHOOSE_ROMS_DESTINATION = "MainFrame.ChooseRomsDestination";

	private static final String MAIN_FRAME_DROP_DIR_HINT = "MainFrame.DropDirHint";

	private static final String ICONS_DISK = "/jrm/resicons/icons/disk.png";

	/** The lbl disks dest. */
	private JCheckBox lblDisksDest;

	/** The lbl samples dest. */
	private JCheckBox lblSamplesDest;

	/** The lbl SW dest. */
	private JCheckBox lblSWDest;

	/** The lbl SW disks dest. */
	private JCheckBox lblSWDisksDest;

	/** The list src dir. */
	private JFileDropList listSrcDir;

	/** The mntm delete selected. */
	private JMenuItem mntmDeleteSelected;

	/** The tf disks dest. */
	private JFileDropTextField tfDisksDest;

	/** The tf samples dest. */
	private JFileDropTextField tfSamplesDest;

	/** The tf SW dest. */
	private JFileDropTextField tfSWDest;

	/** The tf SW disks dest. */
	private JFileDropTextField tfSWDisksDest;

	private JButton btnDisksDest;
	private JButton btnSWDest;
	private JButton btnSWDisksDest;
	private JButton btnSamplesDest;

	/** The txt roms dest. */
	JFileDropTextField txtRomsDest;

	/** The btn roms dest. */
	JButton btnRomsDest;


	/**
	 * Create the panel.
	 */
	public ScannerDirPanel(@SuppressWarnings("exports") final Session session)
	{
		final GridBagLayout gblScannerDirectories = new GridBagLayout();
		gblScannerDirectories.columnWidths = new int[] { 109, 65, 0, 0 };
		gblScannerDirectories.rowHeights = new int[] { 26, 0, 0, 0, 0, 0, 0 };
		gblScannerDirectories.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gblScannerDirectories.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		this.setLayout(gblScannerDirectories);

		JLabel lblRomsDest = new JLabel(Messages.getString("MainFrame.lblRomsDest.text")); //$NON-NLS-1$
		lblRomsDest.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbcLblRomsDest = new GridBagConstraints();
		gbcLblRomsDest.fill = GridBagConstraints.HORIZONTAL;
		gbcLblRomsDest.insets = new Insets(5, 0, 5, 5);
		gbcLblRomsDest.gridx = 0;
		gbcLblRomsDest.gridy = 0;
		this.add(lblRomsDest, gbcLblRomsDest);

		txtRomsDest = new JFileDropTextField(txt -> session.getCurrProfile().setProperty(SettingsEnum.roms_dest_dir, txt)); //$NON-NLS-1$
		txtRomsDest.setMode(JFileDropMode.DIRECTORY);
		txtRomsDest.setUI(new JTextFieldHintUI(Messages.getString(MAIN_FRAME_DROP_DIR_HINT), Color.gray)); //$NON-NLS-1$
		txtRomsDest.setColumns(10);
		final GridBagConstraints gbcTxtRomsDest = new GridBagConstraints();
		gbcTxtRomsDest.fill = GridBagConstraints.BOTH;
		gbcTxtRomsDest.insets = new Insets(5, 0, 5, 0);
		gbcTxtRomsDest.gridx = 1;
		gbcTxtRomsDest.gridy = 0;
		this.add(txtRomsDest, gbcTxtRomsDest);

		btnRomsDest = new JButton(""); //$NON-NLS-1$
		final GridBagConstraints gbcBtnRomsDest = new GridBagConstraints();
		gbcBtnRomsDest.anchor = GridBagConstraints.NORTHWEST;
		gbcBtnRomsDest.insets = new Insets(5, 0, 5, 5);
		gbcBtnRomsDest.gridx = 2;
		gbcBtnRomsDest.gridy = 0;
		this.add(btnRomsDest, gbcBtnRomsDest);
		btnRomsDest.setIcon(MainFrame.getIcon(ICONS_DISK)); //$NON-NLS-1$
		btnRomsDest.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.getCurrProfile().getProperty(MAIN_FRAME_CHOOSE_ROMS_DESTINATION, workdir.getAbsolutePath())), new File(txtRomsDest.getText()), null, Messages.getString(MAIN_FRAME_CHOOSE_ROMS_DESTINATION), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$ //$NON-NLS-2$
				session.getCurrProfile().setProperty(MAIN_FRAME_CHOOSE_ROMS_DESTINATION, chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				txtRomsDest.setText(chooser.getSelectedFile().getAbsolutePath());
				session.getCurrProfile().setProperty(SettingsEnum.roms_dest_dir, txtRomsDest.getText()); //$NON-NLS-1$
				return null;
			});
		});

		lblDisksDest = new JCheckBox(Messages.getString("MainFrame.lblDisksDest.text")); //$NON-NLS-1$
		lblDisksDest.addItemListener(e -> {
			tfDisksDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btnDisksDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			session.getCurrProfile().setProperty(SettingsEnum.disks_dest_dir_enabled, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		lblDisksDest.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbcLblDisksDest = new GridBagConstraints();
		gbcLblDisksDest.fill = GridBagConstraints.HORIZONTAL;
		gbcLblDisksDest.insets = new Insets(0, 0, 5, 5);
		gbcLblDisksDest.gridx = 0;
		gbcLblDisksDest.gridy = 1;
		this.add(lblDisksDest, gbcLblDisksDest);

		tfDisksDest = new JFileDropTextField(txt -> session.getCurrProfile().setProperty(SettingsEnum.disks_dest_dir, txt)); //$NON-NLS-1$
		tfDisksDest.setMode(JFileDropMode.DIRECTORY);
		tfDisksDest.setEnabled(false);
		tfDisksDest.setUI(new JTextFieldHintUI(Messages.getString(MAIN_FRAME_DROP_DIR_HINT), Color.gray)); //$NON-NLS-1$
		tfDisksDest.setText(""); //$NON-NLS-1$
		final GridBagConstraints gbcTFDisksDest = new GridBagConstraints();
		gbcTFDisksDest.insets = new Insets(0, 0, 5, 0);
		gbcTFDisksDest.fill = GridBagConstraints.BOTH;
		gbcTFDisksDest.gridx = 1;
		gbcTFDisksDest.gridy = 1;
		this.add(tfDisksDest, gbcTFDisksDest);
		tfDisksDest.setColumns(10);

		btnDisksDest = new JButton(""); //$NON-NLS-1$
		btnDisksDest.setEnabled(false);
		btnDisksDest.setIcon(MainFrame.getIcon(ICONS_DISK)); //$NON-NLS-1$
		final GridBagConstraints gbcBtnDisksDest = new GridBagConstraints();
		gbcBtnDisksDest.insets = new Insets(0, 0, 5, 5);
		gbcBtnDisksDest.gridx = 2;
		gbcBtnDisksDest.gridy = 1;
		btnDisksDest.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.getCurrProfile().getProperty(MAIN_FRAME_CHOOSE_DISKS_DESTINATION, workdir.getAbsolutePath())), new File(tfDisksDest.getText()), null, Messages.getString(MAIN_FRAME_CHOOSE_DISKS_DESTINATION), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				session.getCurrProfile().setProperty(MAIN_FRAME_CHOOSE_DISKS_DESTINATION, chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfDisksDest.setText(chooser.getSelectedFile().getAbsolutePath());
				session.getCurrProfile().setProperty(SettingsEnum.disks_dest_dir, tfDisksDest.getText()); //$NON-NLS-1$
				return null;
			});
		});
		this.add(btnDisksDest, gbcBtnDisksDest);

		lblSWDest = new JCheckBox(Messages.getString("MainFrame.chckbxSoftwareDest.text")); //$NON-NLS-1$
		lblSWDest.addItemListener(e -> {
			tfSWDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btnSWDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			session.getCurrProfile().setProperty(SettingsEnum.swroms_dest_dir_enabled, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		lblSWDest.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbcLblSWDest = new GridBagConstraints();
		gbcLblSWDest.fill = GridBagConstraints.HORIZONTAL;
		gbcLblSWDest.insets = new Insets(0, 0, 5, 5);
		gbcLblSWDest.gridx = 0;
		gbcLblSWDest.gridy = 2;
		this.add(lblSWDest, gbcLblSWDest);

		tfSWDest = new JFileDropTextField(txt -> session.getCurrProfile().setProperty(SettingsEnum.swroms_dest_dir, txt)); //$NON-NLS-1$
		tfSWDest.setMode(JFileDropMode.DIRECTORY);
		tfSWDest.setEnabled(false);
		tfSWDest.setUI(new JTextFieldHintUI(Messages.getString(MAIN_FRAME_DROP_DIR_HINT), Color.gray)); //$NON-NLS-1$
		tfSWDest.setText(""); //$NON-NLS-1$
		final GridBagConstraints gbcTFSWDest = new GridBagConstraints();
		gbcTFSWDest.insets = new Insets(0, 0, 5, 0);
		gbcTFSWDest.fill = GridBagConstraints.BOTH;
		gbcTFSWDest.gridx = 1;
		gbcTFSWDest.gridy = 2;
		this.add(tfSWDest, gbcTFSWDest);
		tfSWDest.setColumns(10);

		btnSWDest = new JButton(""); //$NON-NLS-1$
		btnSWDest.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.getCurrProfile().getProperty(MAIN_FRAME_CHOOSE_SW_ROMS_DESTINATION, workdir.getAbsolutePath())), new File(tfSWDest.getText()), null, Messages.getString(MAIN_FRAME_CHOOSE_SW_ROMS_DESTINATION), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				session.getCurrProfile().setProperty(MAIN_FRAME_CHOOSE_SW_ROMS_DESTINATION, chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfSWDest.setText(chooser.getSelectedFile().getAbsolutePath());
				session.getCurrProfile().setProperty(SettingsEnum.swroms_dest_dir, tfSWDest.getText()); //$NON-NLS-1$
				return null;
			});
		});
		btnSWDest.setEnabled(false);
		btnSWDest.setIcon(MainFrame.getIcon(ICONS_DISK)); //$NON-NLS-1$
		final GridBagConstraints gbcBtnSWDest = new GridBagConstraints();
		gbcBtnSWDest.insets = new Insets(0, 0, 5, 5);
		gbcBtnSWDest.gridx = 2;
		gbcBtnSWDest.gridy = 2;
		this.add(btnSWDest, gbcBtnSWDest);

		lblSWDisksDest = new JCheckBox(Messages.getString("MainFrame.chckbxSwdisksdest.text")); //$NON-NLS-1$
		lblSWDisksDest.addItemListener(e -> {
			tfSWDisksDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btnSWDisksDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			session.getCurrProfile().setProperty(SettingsEnum.swdisks_dest_dir_enabled, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		final GridBagConstraints gbcLblSWDisksDest = new GridBagConstraints();
		gbcLblSWDisksDest.insets = new Insets(0, 0, 5, 5);
		gbcLblSWDisksDest.gridx = 0;
		gbcLblSWDisksDest.gridy = 3;
		this.add(lblSWDisksDest, gbcLblSWDisksDest);

		tfSWDisksDest = new JFileDropTextField(txt -> session.getCurrProfile().setProperty(SettingsEnum.swdisks_dest_dir, txt)); //$NON-NLS-1$
		tfSWDisksDest.setMode(JFileDropMode.DIRECTORY);
		tfSWDisksDest.setEnabled(false);
		tfSWDisksDest.setUI(new JTextFieldHintUI(Messages.getString(MAIN_FRAME_DROP_DIR_HINT), Color.gray)); //$NON-NLS-1$
		tfSWDisksDest.setText(""); //$NON-NLS-1$
		final GridBagConstraints gbcTFSWDisksDest = new GridBagConstraints();
		gbcTFSWDisksDest.insets = new Insets(0, 0, 5, 0);
		gbcTFSWDisksDest.fill = GridBagConstraints.BOTH;
		gbcTFSWDisksDest.gridx = 1;
		gbcTFSWDisksDest.gridy = 3;
		this.add(tfSWDisksDest, gbcTFSWDisksDest);
		tfSWDisksDest.setColumns(10);

		btnSWDisksDest = new JButton(""); //$NON-NLS-1$
		btnSWDisksDest.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Boolean>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.getCurrProfile().getProperty(MAIN_FRAME_CHOOSE_SW_DISKS_DESTINATION, workdir.getAbsolutePath())), new File(tfSWDisksDest.getText()), null, Messages.getString(MAIN_FRAME_CHOOSE_SW_DISKS_DESTINATION), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				session.getCurrProfile().setProperty(MAIN_FRAME_CHOOSE_SW_DISKS_DESTINATION, chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfSWDisksDest.setText(chooser.getSelectedFile().getAbsolutePath());
				session.getCurrProfile().setProperty(SettingsEnum.swdisks_dest_dir, tfSWDisksDest.getText()); //$NON-NLS-1$
				return true;
			});
		});
		btnSWDisksDest.setEnabled(false);
		btnSWDisksDest.setIcon(MainFrame.getIcon(ICONS_DISK)); //$NON-NLS-1$
		final GridBagConstraints gbcBtnSWDisksDest = new GridBagConstraints();
		gbcBtnSWDisksDest.insets = new Insets(0, 0, 5, 5);
		gbcBtnSWDisksDest.gridx = 2;
		gbcBtnSWDisksDest.gridy = 3;
		this.add(btnSWDisksDest, gbcBtnSWDisksDest);

		lblSamplesDest = new JCheckBox(Messages.getString("MainFrame.lblSamplesDest.text")); //$NON-NLS-1$
		lblSamplesDest.addItemListener(e -> {
			tfSamplesDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btnSamplesDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			session.getCurrProfile().setProperty(SettingsEnum.samples_dest_dir_enabled, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		lblSamplesDest.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbcLblSamplesDest = new GridBagConstraints();
		gbcLblSamplesDest.fill = GridBagConstraints.HORIZONTAL;
		gbcLblSamplesDest.insets = new Insets(0, 0, 5, 5);
		gbcLblSamplesDest.gridx = 0;
		gbcLblSamplesDest.gridy = 4;
		this.add(lblSamplesDest, gbcLblSamplesDest);

		tfSamplesDest = new JFileDropTextField(txt -> session.getCurrProfile().setProperty(SettingsEnum.samples_dest_dir, txt)); //$NON-NLS-1$
		tfSamplesDest.setMode(JFileDropMode.DIRECTORY);
		tfSamplesDest.setEnabled(false);
		tfSamplesDest.setUI(new JTextFieldHintUI(Messages.getString(MAIN_FRAME_DROP_DIR_HINT), Color.gray)); //$NON-NLS-1$
		tfSamplesDest.setText(""); //$NON-NLS-1$
		final GridBagConstraints gbcTFSamplesDest = new GridBagConstraints();
		gbcTFSamplesDest.insets = new Insets(0, 0, 5, 0);
		gbcTFSamplesDest.fill = GridBagConstraints.BOTH;
		gbcTFSamplesDest.gridx = 1;
		gbcTFSamplesDest.gridy = 4;
		this.add(tfSamplesDest, gbcTFSamplesDest);
		tfSamplesDest.setColumns(10);

		btnSamplesDest = new JButton(""); //$NON-NLS-1$
		btnSamplesDest.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Boolean>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.getCurrProfile().getProperty(MAIN_FRAME_CHOOSE_SAMPLES_DESTINATION, workdir.getAbsolutePath())), new File(tfSamplesDest.getText()), null, Messages.getString(MAIN_FRAME_CHOOSE_SAMPLES_DESTINATION), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				session.getCurrProfile().setProperty(MAIN_FRAME_CHOOSE_SAMPLES_DESTINATION, chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfSamplesDest.setText(chooser.getSelectedFile().getAbsolutePath());
				session.getCurrProfile().setProperty(SettingsEnum.samples_dest_dir, tfSamplesDest.getText()); //$NON-NLS-1$
				return true;
			});
		});
		btnSamplesDest.setEnabled(false);
		btnSamplesDest.setIcon(MainFrame.getIcon(ICONS_DISK)); //$NON-NLS-1$
		final GridBagConstraints gbcBtnSamplesDest = new GridBagConstraints();
		gbcBtnSamplesDest.insets = new Insets(0, 0, 5, 5);
		gbcBtnSamplesDest.gridx = 2;
		gbcBtnSamplesDest.gridy = 4;
		this.add(btnSamplesDest, gbcBtnSamplesDest);

		JLabel lblSrcDir = new JLabel(Messages.getString("MainFrame.lblSrcDir.text")); //$NON-NLS-1$
		lblSrcDir.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbcLblSrcDir = new GridBagConstraints();
		gbcLblSrcDir.fill = GridBagConstraints.HORIZONTAL;
		gbcLblSrcDir.anchor = GridBagConstraints.NORTH;
		gbcLblSrcDir.insets = new Insets(0, 0, 0, 5);
		gbcLblSrcDir.gridx = 0;
		gbcLblSrcDir.gridy = 5;
		this.add(lblSrcDir, gbcLblSrcDir);

		listSrcDir = new JFileDropList(files -> session.getCurrProfile().setProperty(SettingsEnum.src_dir, String.join("|", files.stream().map(File::getAbsolutePath).collect(Collectors.toList())))); // $NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-2$ // $NON-NLS-2$
		listSrcDir.setMode(JFileDropMode.DIRECTORY);
		listSrcDir.setUI(new JListHintUI(Messages.getString(MAIN_FRAME_DROP_DIR_HINT), Color.gray)); //$NON-NLS-1$
		final GridBagConstraints gbcListSrcDir = new GridBagConstraints();
		gbcListSrcDir.insets = new Insets(0, 0, 5, 5);
		gbcListSrcDir.gridwidth = 2;
		gbcListSrcDir.fill = GridBagConstraints.BOTH;
		gbcListSrcDir.gridx = 1;
		gbcListSrcDir.gridy = 5;
		this.add(listSrcDir, gbcListSrcDir);
		listSrcDir.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuCanceled(final PopupMenuEvent e)
			{
				// do nothing
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e)
			{
				// do nothing
			}

			@Override
			public void popupMenuWillBecomeVisible(final PopupMenuEvent e)
			{
				mntmDeleteSelected.setEnabled(!listSrcDir.getSelectedValuesList().isEmpty());
			}
		});
		MainFrame.addPopup(listSrcDir, popupMenu);

		mntmDeleteSelected = new JMenuItem(Messages.getString("MainFrame.mntmDeleteSelected.text")); //$NON-NLS-1$
		mntmDeleteSelected.addActionListener(e -> listSrcDir.del(listSrcDir.getSelectedValuesList()));
		mntmDeleteSelected.setIcon(MainFrame.getIcon("/jrm/resicons/icons/folder_delete.png")); //$NON-NLS-1$
		popupMenu.add(mntmDeleteSelected);

		JMenuItem mntmAddDirectory = new JMenuItem(Messages.getString("MainFrame.mntmAddDirectory.text")); //$NON-NLS-1$
		mntmAddDirectory.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Boolean>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.getCurrProfile().getProperty("MainFrame.ChooseRomsSource", workdir.getAbsolutePath())), null, null, null, true).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$
				session.getCurrProfile().setProperty("MainFrame.ChooseRomsSource", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				listSrcDir.add(chooser.getSelectedFiles());
				return true;
			});
		});
		mntmAddDirectory.setIcon(MainFrame.getIcon("/jrm/resicons/icons/folder_add.png")); //$NON-NLS-1$
		popupMenu.add(mntmAddDirectory);


	}
	
	public void initProfileSettings(@SuppressWarnings("exports") final Session session)
	{
		txtRomsDest.setText(session.getCurrProfile().getProperty(SettingsEnum.roms_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		lblDisksDest.setSelected(session.getCurrProfile().getProperty(SettingsEnum.disks_dest_dir_enabled, false)); //$NON-NLS-1$
		tfDisksDest.setText(session.getCurrProfile().getProperty(SettingsEnum.disks_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		lblSWDest.setSelected(session.getCurrProfile().getProperty(SettingsEnum.swroms_dest_dir_enabled, false)); //$NON-NLS-1$
		tfSWDest.setText(session.getCurrProfile().getProperty(SettingsEnum.swroms_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		lblSWDisksDest.setSelected(session.getCurrProfile().getProperty(SettingsEnum.swdisks_dest_dir_enabled, false)); //$NON-NLS-1$
		tfSWDisksDest.setText(session.getCurrProfile().getProperty(SettingsEnum.swdisks_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		lblSamplesDest.setSelected(session.getCurrProfile().getProperty(SettingsEnum.samples_dest_dir_enabled, false)); //$NON-NLS-1$
		tfSamplesDest.setText(session.getCurrProfile().getProperty(SettingsEnum.samples_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		listSrcDir.getModel().removeAllElements();
		for (final String s : StringUtils.split(session.getCurrProfile().getProperty(SettingsEnum.src_dir, ""),'|')) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (!s.isEmpty())
				listSrcDir.getModel().addElement(new File(s));
		
	}

}
