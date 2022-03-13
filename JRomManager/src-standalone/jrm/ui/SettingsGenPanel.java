package jrm.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import jrm.locale.Messages;
import jrm.misc.ProfileSettingsEnum;
import jrm.misc.SettingsEnum;
import jrm.security.Session;
import jrm.ui.basic.JFileDropMode;
import jrm.ui.basic.JFileDropTextField;
import jrm.ui.basic.JRMFileChooser;
import jrm.ui.basic.JTextFieldHintUI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("serial")
public class SettingsGenPanel extends JPanel
{
	/** The cb log level. */
	private JComboBox<ThreadCnt> cbThreading;

	/** The scheduler. */
	final transient ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@RequiredArgsConstructor
	private static class ThreadCnt
	{
		final @Getter int cnt;
		final @Getter String name;
		
		public ThreadCnt(int cnt)
		{
			this.cnt = cnt;
			this.name = null;
		}
		
		@Override
		public String toString()
		{
			return name!=null?name:Integer.toString(cnt);
		}
		
		@Override
		public int hashCode()
		{
			return super.hashCode();
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if(obj == null)
				return false;
			if(obj instanceof ThreadCnt)
				return this.cnt == ((ThreadCnt) obj).cnt;
			return super.equals(obj);
		}
		
		static ThreadCnt[] build()
		{
			ArrayList<ThreadCnt> list = new ArrayList<>();
			list.add(new ThreadCnt(-1, "Adaptive"));
			list.add(new ThreadCnt(0, "Max available"));
			for(var i = 1; i <= Runtime.getRuntime().availableProcessors(); i++)
				list.add(new ThreadCnt(i));
			return list.toArray(ThreadCnt[]::new);
		}
	}
	
	/**
	 * Create the panel.
	 */
	public SettingsGenPanel(@SuppressWarnings("exports") final Session session)
	{
		final var gblDebug = new GridBagLayout();
		gblDebug.columnWidths = new int[] { 50, 0, 10, 10, 50 };
		gblDebug.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gblDebug.columnWeights = new double[] { 1.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gblDebug.rowWeights = new double[] { 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		this.setLayout(gblDebug);

		final var lblThreading = new JLabel(Messages.getString("SettingsGenPanel.lblThreading.text")); //$NON-NLS-1$
		final var gbcLblThreading = new GridBagConstraints();
		gbcLblThreading.anchor = GridBagConstraints.EAST;
		gbcLblThreading.fill = GridBagConstraints.VERTICAL;
		gbcLblThreading.insets = new Insets(0, 0, 5, 5);
		gbcLblThreading.gridx = 0;
		gbcLblThreading.gridy = 1;
		this.add(lblThreading, gbcLblThreading);

		
		
		cbThreading = new JComboBox<>(new DefaultComboBoxModel<>(ThreadCnt.build()));
		final var gbcCBThreading = new GridBagConstraints();
		gbcCBThreading.insets = new Insets(0, 0, 5, 5);
		gbcCBThreading.fill = GridBagConstraints.HORIZONTAL;
		gbcCBThreading.gridx = 1;
		gbcCBThreading.gridy = 1;
		gbcCBThreading.gridwidth = 3;
		this.add(cbThreading, gbcCBThreading);
		cbThreading.addActionListener(arg0 -> 
			session.getUser().getSettings().setProperty(SettingsEnum.thread_count, ((ThreadCnt)cbThreading.getSelectedItem()).getCnt())
		); //$NON-NLS-1$
		cbThreading.setSelectedItem(new ThreadCnt(session.getUser().getSettings().getProperty(SettingsEnum.thread_count, Integer.class)));
		
		
		
		final var tfBackupDest = new JFileDropTextField(txt -> session.getUser().getSettings().setProperty(ProfileSettingsEnum.backup_dest_dir, txt)); //$NON-NLS-1$
		tfBackupDest.setMode(JFileDropMode.DIRECTORY);
		tfBackupDest.setEnabled(session.getUser().getSettings().getProperty(ProfileSettingsEnum.backup_dest_dir_enabled, Boolean.class));
		tfBackupDest.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tfBackupDest.setText(session.getUser().getSettings().getProperty(ProfileSettingsEnum.backup_dest_dir)); //$NON-NLS-1$
		
		final var btnBackupDest = new JButton(""); //$NON-NLS-1$
		btnBackupDest.addActionListener(e -> {
			final File workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
			new JRMFileChooser<Boolean>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(session.getUser().getSettings().getProperty("MainFrame.ChooseBackupGDestination", workdir.getAbsolutePath())), new File(tfBackupDest.getText()), null, Messages.getString("MainFrame.ChooseBackupDestination"), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				session.getUser().getSettings().setProperty("MainFrame.ChooseBackupGDestination", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfBackupDest.setText(chooser.getSelectedFile().getAbsolutePath());
				session.getUser().getSettings().setProperty(ProfileSettingsEnum.backup_dest_dir, tfBackupDest.getText()); //$NON-NLS-1$
				return true;
			});
		});
		btnBackupDest.setEnabled(session.getUser().getSettings().getProperty(ProfileSettingsEnum.backup_dest_dir_enabled, Boolean.class));
		btnBackupDest.setIcon(MainFrame.getIcon("/jrm/resicons/icons/disk.png")); //$NON-NLS-1$

		final var lblBackupDest = new JCheckBox(Messages.getString("MainFrame.lblBackupDest.text")); //$NON-NLS-1$
		lblBackupDest.addItemListener(e -> {
			tfBackupDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btnBackupDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			session.getUser().getSettings().setProperty(ProfileSettingsEnum.backup_dest_dir_enabled, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		lblBackupDest.setHorizontalAlignment(SwingConstants.TRAILING);
		lblBackupDest.setSelected(session.getUser().getSettings().getProperty(ProfileSettingsEnum.backup_dest_dir_enabled, Boolean.class));
		final GridBagConstraints gbcLblBackupDest = new GridBagConstraints();
		gbcLblBackupDest.fill = GridBagConstraints.HORIZONTAL;
		gbcLblBackupDest.insets = new Insets(0, 0, 5, 5);
		gbcLblBackupDest.gridx = 0;
		gbcLblBackupDest.gridy = 2;
		this.add(lblBackupDest, gbcLblBackupDest);
	
		final GridBagConstraints gbcTFBackupDest = new GridBagConstraints();
		gbcTFBackupDest.insets = new Insets(0, 0, 5, 0);
		gbcTFBackupDest.fill = GridBagConstraints.BOTH;
		gbcTFBackupDest.gridx = 1;
		gbcTFBackupDest.gridy = 2;
		this.add(tfBackupDest, gbcTFBackupDest);
		tfBackupDest.setColumns(10);
	
		final GridBagConstraints gbcBtnBackupDest = new GridBagConstraints();
		gbcBtnBackupDest.insets = new Insets(0, 0, 5, 5);
		gbcBtnBackupDest.gridx = 2;
		gbcBtnBackupDest.gridy = 2;
		this.add(btnBackupDest, gbcBtnBackupDest);

	}

}
