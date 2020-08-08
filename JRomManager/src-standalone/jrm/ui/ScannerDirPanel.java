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

	private JButton btDisksDest;
	private JButton btnSWDest;
	private JButton btSWDisksDest;
	private JButton btSamplesDest;

	/** The txt roms dest. */
	JFileDropTextField txtRomsDest;

	/** The btn roms dest. */
	JButton btnRomsDest;


	/**
	 * Create the panel.
	 */
	public ScannerDirPanel(final Session session)
	{
		final GridBagLayout gbl_scannerDirectories = new GridBagLayout();
		gbl_scannerDirectories.columnWidths = new int[] { 109, 65, 0, 0 };
		gbl_scannerDirectories.rowHeights = new int[] { 26, 0, 0, 0, 0, 0, 0 };
		gbl_scannerDirectories.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_scannerDirectories.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		this.setLayout(gbl_scannerDirectories);

		JLabel lblRomsDest = new JLabel(Messages.getString("MainFrame.lblRomsDest.text")); //$NON-NLS-1$
		lblRomsDest.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbc_lblRomsDest = new GridBagConstraints();
		gbc_lblRomsDest.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblRomsDest.insets = new Insets(5, 0, 5, 5);
		gbc_lblRomsDest.gridx = 0;
		gbc_lblRomsDest.gridy = 0;
		this.add(lblRomsDest, gbc_lblRomsDest);

		txtRomsDest = new JFileDropTextField(txt -> session.curr_profile.setProperty(SettingsEnum.roms_dest_dir, txt)); //$NON-NLS-1$
		txtRomsDest.setMode(JFileDropMode.DIRECTORY);
		txtRomsDest.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		txtRomsDest.setColumns(10);
		final GridBagConstraints gbc_txtRomsDest = new GridBagConstraints();
		gbc_txtRomsDest.fill = GridBagConstraints.BOTH;
		gbc_txtRomsDest.insets = new Insets(5, 0, 5, 0);
		gbc_txtRomsDest.gridx = 1;
		gbc_txtRomsDest.gridy = 0;
		this.add(txtRomsDest, gbc_txtRomsDest);

		btnRomsDest = new JButton(""); //$NON-NLS-1$
		final GridBagConstraints gbc_btnRomsDest = new GridBagConstraints();
		gbc_btnRomsDest.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnRomsDest.insets = new Insets(5, 0, 5, 5);
		gbc_btnRomsDest.gridx = 2;
		gbc_btnRomsDest.gridy = 0;
		this.add(btnRomsDest, gbc_btnRomsDest);
		btnRomsDest.setIcon(MainFrame.getIcon("/jrm/resicons/icons/disk.png")); //$NON-NLS-1$
		btnRomsDest.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.curr_profile.getProperty("MainFrame.ChooseRomsDestination", workdir.getAbsolutePath())), new File(txtRomsDest.getText()), null, Messages.getString("MainFrame.ChooseRomsDestination"), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$ //$NON-NLS-2$
				session.curr_profile.setProperty("MainFrame.ChooseRomsDestination", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				txtRomsDest.setText(chooser.getSelectedFile().getAbsolutePath());
				session.curr_profile.setProperty(SettingsEnum.roms_dest_dir, txtRomsDest.getText()); //$NON-NLS-1$
				return null;
			});
		});

		lblDisksDest = new JCheckBox(Messages.getString("MainFrame.lblDisksDest.text")); //$NON-NLS-1$
		lblDisksDest.addItemListener(e -> {
			tfDisksDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btDisksDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			session.curr_profile.setProperty(SettingsEnum.disks_dest_dir_enabled, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		lblDisksDest.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbc_lblDisksDest = new GridBagConstraints();
		gbc_lblDisksDest.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDisksDest.insets = new Insets(0, 0, 5, 5);
		gbc_lblDisksDest.gridx = 0;
		gbc_lblDisksDest.gridy = 1;
		this.add(lblDisksDest, gbc_lblDisksDest);

		tfDisksDest = new JFileDropTextField(txt -> session.curr_profile.setProperty(SettingsEnum.disks_dest_dir, txt)); //$NON-NLS-1$
		tfDisksDest.setMode(JFileDropMode.DIRECTORY);
		tfDisksDest.setEnabled(false);
		tfDisksDest.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tfDisksDest.setText(""); //$NON-NLS-1$
		final GridBagConstraints gbc_tfDisksDest = new GridBagConstraints();
		gbc_tfDisksDest.insets = new Insets(0, 0, 5, 0);
		gbc_tfDisksDest.fill = GridBagConstraints.BOTH;
		gbc_tfDisksDest.gridx = 1;
		gbc_tfDisksDest.gridy = 1;
		this.add(tfDisksDest, gbc_tfDisksDest);
		tfDisksDest.setColumns(10);

		btDisksDest = new JButton(""); //$NON-NLS-1$
		btDisksDest.setEnabled(false);
		btDisksDest.setIcon(MainFrame.getIcon("/jrm/resicons/icons/disk.png")); //$NON-NLS-1$
		final GridBagConstraints gbc_btDisksDest = new GridBagConstraints();
		gbc_btDisksDest.insets = new Insets(0, 0, 5, 5);
		gbc_btDisksDest.gridx = 2;
		gbc_btDisksDest.gridy = 1;
		btDisksDest.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.curr_profile.getProperty("MainFrame.ChooseDisksDestination", workdir.getAbsolutePath())), new File(tfDisksDest.getText()), null, Messages.getString("MainFrame.ChooseDisksDestination"), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				session.curr_profile.setProperty("MainFrame.ChooseDisksDestination", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfDisksDest.setText(chooser.getSelectedFile().getAbsolutePath());
				session.curr_profile.setProperty(SettingsEnum.disks_dest_dir, tfDisksDest.getText()); //$NON-NLS-1$
				return null;
			});
		});
		this.add(btDisksDest, gbc_btDisksDest);

		lblSWDest = new JCheckBox(Messages.getString("MainFrame.chckbxSoftwareDest.text")); //$NON-NLS-1$
		lblSWDest.addItemListener(e -> {
			tfSWDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btnSWDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			session.curr_profile.setProperty(SettingsEnum.swroms_dest_dir_enabled, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		lblSWDest.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbc_lblSWDest = new GridBagConstraints();
		gbc_lblSWDest.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSWDest.insets = new Insets(0, 0, 5, 5);
		gbc_lblSWDest.gridx = 0;
		gbc_lblSWDest.gridy = 2;
		this.add(lblSWDest, gbc_lblSWDest);

		tfSWDest = new JFileDropTextField(txt -> session.curr_profile.setProperty(SettingsEnum.swroms_dest_dir, txt)); //$NON-NLS-1$
		tfSWDest.setMode(JFileDropMode.DIRECTORY);
		tfSWDest.setEnabled(false);
		tfSWDest.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tfSWDest.setText(""); //$NON-NLS-1$
		final GridBagConstraints gbc_tfSWDest = new GridBagConstraints();
		gbc_tfSWDest.insets = new Insets(0, 0, 5, 0);
		gbc_tfSWDest.fill = GridBagConstraints.BOTH;
		gbc_tfSWDest.gridx = 1;
		gbc_tfSWDest.gridy = 2;
		this.add(tfSWDest, gbc_tfSWDest);
		tfSWDest.setColumns(10);

		btnSWDest = new JButton(""); //$NON-NLS-1$
		btnSWDest.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.curr_profile.getProperty("MainFrame.ChooseSWRomsDestination", workdir.getAbsolutePath())), new File(tfSWDest.getText()), null, Messages.getString("MainFrame.ChooseSWRomsDestination"), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				session.curr_profile.setProperty("MainFrame.ChooseSWRomsDestination", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfSWDest.setText(chooser.getSelectedFile().getAbsolutePath());
				session.curr_profile.setProperty(SettingsEnum.swroms_dest_dir, tfSWDest.getText()); //$NON-NLS-1$
				return null;
			});
		});
		btnSWDest.setEnabled(false);
		btnSWDest.setIcon(MainFrame.getIcon("/jrm/resicons/icons/disk.png")); //$NON-NLS-1$
		final GridBagConstraints gbc_btnSWDest = new GridBagConstraints();
		gbc_btnSWDest.insets = new Insets(0, 0, 5, 5);
		gbc_btnSWDest.gridx = 2;
		gbc_btnSWDest.gridy = 2;
		this.add(btnSWDest, gbc_btnSWDest);

		lblSWDisksDest = new JCheckBox(Messages.getString("MainFrame.chckbxSwdisksdest.text")); //$NON-NLS-1$
		lblSWDisksDest.addItemListener(e -> {
			tfSWDisksDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btSWDisksDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			session.curr_profile.setProperty(SettingsEnum.swdisks_dest_dir_enabled, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		final GridBagConstraints gbc_lblSWDisksDest = new GridBagConstraints();
		gbc_lblSWDisksDest.insets = new Insets(0, 0, 5, 5);
		gbc_lblSWDisksDest.gridx = 0;
		gbc_lblSWDisksDest.gridy = 3;
		this.add(lblSWDisksDest, gbc_lblSWDisksDest);

		tfSWDisksDest = new JFileDropTextField(txt -> session.curr_profile.setProperty(SettingsEnum.swdisks_dest_dir, txt)); //$NON-NLS-1$
		tfSWDisksDest.setMode(JFileDropMode.DIRECTORY);
		tfSWDisksDest.setEnabled(false);
		tfSWDisksDest.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tfSWDisksDest.setText(""); //$NON-NLS-1$
		final GridBagConstraints gbc_tfSWDisksDest = new GridBagConstraints();
		gbc_tfSWDisksDest.insets = new Insets(0, 0, 5, 0);
		gbc_tfSWDisksDest.fill = GridBagConstraints.BOTH;
		gbc_tfSWDisksDest.gridx = 1;
		gbc_tfSWDisksDest.gridy = 3;
		this.add(tfSWDisksDest, gbc_tfSWDisksDest);
		tfSWDisksDest.setColumns(10);

		btSWDisksDest = new JButton(""); //$NON-NLS-1$
		btSWDisksDest.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Boolean>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.curr_profile.getProperty("MainFrame.ChooseSWDisksDestination", workdir.getAbsolutePath())), new File(tfSWDisksDest.getText()), null, Messages.getString("MainFrame.ChooseSWDisksDestination"), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				session.curr_profile.setProperty("MainFrame.ChooseSWDisksDestination", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfSWDisksDest.setText(chooser.getSelectedFile().getAbsolutePath());
				session.curr_profile.setProperty(SettingsEnum.swdisks_dest_dir, tfSWDisksDest.getText()); //$NON-NLS-1$
				return true;
			});
		});
		btSWDisksDest.setEnabled(false);
		btSWDisksDest.setIcon(MainFrame.getIcon("/jrm/resicons/icons/disk.png")); //$NON-NLS-1$
		final GridBagConstraints gbc_btSWDisksDest = new GridBagConstraints();
		gbc_btSWDisksDest.insets = new Insets(0, 0, 5, 5);
		gbc_btSWDisksDest.gridx = 2;
		gbc_btSWDisksDest.gridy = 3;
		this.add(btSWDisksDest, gbc_btSWDisksDest);

		lblSamplesDest = new JCheckBox(Messages.getString("MainFrame.lblSamplesDest.text")); //$NON-NLS-1$
		lblSamplesDest.addItemListener(e -> {
			tfSamplesDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btSamplesDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			session.curr_profile.setProperty(SettingsEnum.samples_dest_dir_enabled, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		lblSamplesDest.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbc_lblSamplesDest = new GridBagConstraints();
		gbc_lblSamplesDest.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSamplesDest.insets = new Insets(0, 0, 5, 5);
		gbc_lblSamplesDest.gridx = 0;
		gbc_lblSamplesDest.gridy = 4;
		this.add(lblSamplesDest, gbc_lblSamplesDest);

		tfSamplesDest = new JFileDropTextField(txt -> session.curr_profile.setProperty(SettingsEnum.samples_dest_dir, txt)); //$NON-NLS-1$
		tfSamplesDest.setMode(JFileDropMode.DIRECTORY);
		tfSamplesDest.setEnabled(false);
		tfSamplesDest.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tfSamplesDest.setText(""); //$NON-NLS-1$
		final GridBagConstraints gbc_tfSamplesDest = new GridBagConstraints();
		gbc_tfSamplesDest.insets = new Insets(0, 0, 5, 0);
		gbc_tfSamplesDest.fill = GridBagConstraints.BOTH;
		gbc_tfSamplesDest.gridx = 1;
		gbc_tfSamplesDest.gridy = 4;
		this.add(tfSamplesDest, gbc_tfSamplesDest);
		tfSamplesDest.setColumns(10);

		btSamplesDest = new JButton(""); //$NON-NLS-1$
		btSamplesDest.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Boolean>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.curr_profile.getProperty("MainFrame.ChooseSamplesDestination", workdir.getAbsolutePath())), new File(tfSamplesDest.getText()), null, Messages.getString("MainFrame.ChooseSamplesDestination"), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				session.curr_profile.setProperty("MainFrame.ChooseSamplesDestination", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfSamplesDest.setText(chooser.getSelectedFile().getAbsolutePath());
				session.curr_profile.setProperty(SettingsEnum.samples_dest_dir, tfSamplesDest.getText()); //$NON-NLS-1$
				return true;
			});
		});
		btSamplesDest.setEnabled(false);
		btSamplesDest.setIcon(MainFrame.getIcon("/jrm/resicons/icons/disk.png")); //$NON-NLS-1$
		final GridBagConstraints gbc_btSamplesDest = new GridBagConstraints();
		gbc_btSamplesDest.insets = new Insets(0, 0, 5, 5);
		gbc_btSamplesDest.gridx = 2;
		gbc_btSamplesDest.gridy = 4;
		this.add(btSamplesDest, gbc_btSamplesDest);

		JLabel lblSrcDir = new JLabel(Messages.getString("MainFrame.lblSrcDir.text")); //$NON-NLS-1$
		lblSrcDir.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbc_lblSrcDir = new GridBagConstraints();
		gbc_lblSrcDir.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSrcDir.anchor = GridBagConstraints.NORTH;
		gbc_lblSrcDir.insets = new Insets(0, 0, 0, 5);
		gbc_lblSrcDir.gridx = 0;
		gbc_lblSrcDir.gridy = 5;
		this.add(lblSrcDir, gbc_lblSrcDir);

		listSrcDir = new JFileDropList(files -> session.curr_profile.setProperty(SettingsEnum.src_dir, String.join("|", files.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList())))); // $NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-2$
																																															// $NON-NLS-2$
		listSrcDir.setMode(JFileDropMode.DIRECTORY);
		listSrcDir.setUI(new JListHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		final GridBagConstraints gbc_listSrcDir = new GridBagConstraints();
		gbc_listSrcDir.insets = new Insets(0, 0, 5, 5);
		gbc_listSrcDir.gridwidth = 2;
		gbc_listSrcDir.fill = GridBagConstraints.BOTH;
		gbc_listSrcDir.gridx = 1;
		gbc_listSrcDir.gridy = 5;
		this.add(listSrcDir, gbc_listSrcDir);
		listSrcDir.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		JPopupMenu popupMenu = new JPopupMenu();
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
				mntmDeleteSelected.setEnabled(listSrcDir.getSelectedValuesList().size() > 0);
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
			new JRMFileChooser<Boolean>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.curr_profile.getProperty("MainFrame.ChooseRomsSource", workdir.getAbsolutePath())), null, null, null, true).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$
				session.curr_profile.setProperty("MainFrame.ChooseRomsSource", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				listSrcDir.add(chooser.getSelectedFiles());
				return true;
			});
		});
		mntmAddDirectory.setIcon(MainFrame.getIcon("/jrm/resicons/icons/folder_add.png")); //$NON-NLS-1$
		popupMenu.add(mntmAddDirectory);


	}
	
	public void initProfileSettings(final Session session)
	{
		txtRomsDest.setText(session.curr_profile.getProperty(SettingsEnum.roms_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		lblDisksDest.setSelected(session.curr_profile.getProperty(SettingsEnum.disks_dest_dir_enabled, false)); //$NON-NLS-1$
		tfDisksDest.setText(session.curr_profile.getProperty(SettingsEnum.disks_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		lblSWDest.setSelected(session.curr_profile.getProperty(SettingsEnum.swroms_dest_dir_enabled, false)); //$NON-NLS-1$
		tfSWDest.setText(session.curr_profile.getProperty(SettingsEnum.swroms_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		lblSWDisksDest.setSelected(session.curr_profile.getProperty(SettingsEnum.swdisks_dest_dir_enabled, false)); //$NON-NLS-1$
		tfSWDisksDest.setText(session.curr_profile.getProperty(SettingsEnum.swdisks_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		lblSamplesDest.setSelected(session.curr_profile.getProperty(SettingsEnum.samples_dest_dir_enabled, false)); //$NON-NLS-1$
		tfSamplesDest.setText(session.curr_profile.getProperty(SettingsEnum.samples_dest_dir, "")); //$NON-NLS-1$ //$NON-NLS-2$
		listSrcDir.getModel().removeAllElements();
		for (final String s : StringUtils.split(session.curr_profile.getProperty(SettingsEnum.src_dir, ""),'|')) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (!s.isEmpty())
				listSrcDir.getModel().addElement(new File(s));
		
	}

}
