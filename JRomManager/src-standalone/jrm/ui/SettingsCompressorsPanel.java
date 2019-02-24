package jrm.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import jrm.compressors.SevenZipOptions;
import jrm.compressors.ZipOptions;
import jrm.compressors.zipfs.ZipLevel;
import jrm.compressors.zipfs.ZipTempThreshold;
import jrm.locale.Messages;
import jrm.misc.FindCmd;
import jrm.security.Session;
import jrm.ui.basic.JFileDropMode;
import jrm.ui.basic.JFileDropTextField;
import jrm.ui.basic.JTextFieldHintUI;

@SuppressWarnings("serial")
public class SettingsCompressorsPanel extends JPanel
{
	/** The cb 7 z args. */
	private JComboBox<SevenZipOptions> cb7zArgs;

	/** The cb zip E args. */
	private JComboBox<ZipOptions> cbZipEArgs;

	/** The ckbx 7 z solid. */
	private JCheckBox ckbx7zSolid;

	/** The tf 7 z cmd. */
	private JFileDropTextField tf7zCmd;

	/** The tf 7 z threads. */
	private JTextField tf7zThreads;

	/** The tf zip E cmd. */
	private JFileDropTextField tfZipECmd;

	/** The tf zip E threads. */
	private JTextField tfZipEThreads;

	/** The cbbx zip level. */
	private JComboBox<ZipLevel> cbbxZipLevel;

	/** The cbbx zip temp threshold. */
	private JComboBox<ZipTempThreshold> cbbxZipTempThreshold;

	/**
	 * Create the panel.
	 */
	public SettingsCompressorsPanel(final Session session)
	{
		this.setLayout(new BorderLayout(0, 0));

		JTabbedPane compressorsPane = new JTabbedPane(SwingConstants.TOP);
		this.add(compressorsPane);

		JPanel panelZip = new JPanel();
		compressorsPane.addTab(Messages.getString("MainFrame.Zip"), null, panelZip, null); //$NON-NLS-1$
		GridBagLayout gbl_panelZip = new GridBagLayout();
		gbl_panelZip.columnWidths = new int[] { 1, 0, 1, 0 };
		gbl_panelZip.rowHeights = new int[] { 0, 20, 20, 0, 0 };
		gbl_panelZip.columnWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panelZip.rowWeights = new double[] { 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelZip.setLayout(gbl_panelZip);

		JLabel lblTemporaryFilesThreshold = new JLabel(Messages.getString("MainFrame.lblTemporaryFilesThreshold.text")); //$NON-NLS-1$
		lblTemporaryFilesThreshold.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblTemporaryFilesThreshold = new GridBagConstraints();
		gbc_lblTemporaryFilesThreshold.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblTemporaryFilesThreshold.insets = new Insets(0, 0, 5, 5);
		gbc_lblTemporaryFilesThreshold.gridx = 0;
		gbc_lblTemporaryFilesThreshold.gridy = 1;
		panelZip.add(lblTemporaryFilesThreshold, gbc_lblTemporaryFilesThreshold);

		cbbxZipTempThreshold = new JComboBox<>();
		cbbxZipTempThreshold.setModel(new DefaultComboBoxModel<>(ZipTempThreshold.values()));
		cbbxZipTempThreshold.setSelectedItem(ZipTempThreshold.valueOf(session.getUser().settings.getProperty("zip_temp_threshold", ZipTempThreshold._10MB.toString()))); //$NON-NLS-1$
		cbbxZipTempThreshold.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				session.getUser().settings.setProperty("zip_temp_threshold", cbbxZipTempThreshold.getSelectedItem().toString()); //$NON-NLS-1$
			}
		});
		cbbxZipTempThreshold.setRenderer(new DefaultListCellRenderer()
		{

			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				setText(((ZipTempThreshold) value).getName());
				return this;
			}
		});
		GridBagConstraints gbc_cbbxZipTempThreshold = new GridBagConstraints();
		gbc_cbbxZipTempThreshold.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxZipTempThreshold.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxZipTempThreshold.gridx = 1;
		gbc_cbbxZipTempThreshold.gridy = 1;
		panelZip.add(cbbxZipTempThreshold, gbc_cbbxZipTempThreshold);

		JLabel lblCompressionLevel = new JLabel(Messages.getString("MainFrame.lblCompressionLevel.text")); //$NON-NLS-1$
		lblCompressionLevel.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblCompressionLevel = new GridBagConstraints();
		gbc_lblCompressionLevel.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblCompressionLevel.insets = new Insets(0, 0, 5, 5);
		gbc_lblCompressionLevel.gridx = 0;
		gbc_lblCompressionLevel.gridy = 2;
		panelZip.add(lblCompressionLevel, gbc_lblCompressionLevel);

		cbbxZipLevel = new JComboBox<>();
		cbbxZipLevel.setModel(new DefaultComboBoxModel<>(ZipLevel.values()));
		cbbxZipLevel.setSelectedItem(ZipLevel.valueOf(session.getUser().settings.getProperty("zip_compression_level", ZipLevel.DEFAULT.toString()))); //$NON-NLS-1$
		cbbxZipLevel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				session.getUser().settings.setProperty("zip_compression_level", cbbxZipLevel.getSelectedItem().toString()); //$NON-NLS-1$
			}
		});
		cbbxZipLevel.setRenderer(new DefaultListCellRenderer()
		{

			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				setText(((ZipLevel) value).getName());
				return this;
			}
		});
		GridBagConstraints gbc_cbbxZipLevel = new GridBagConstraints();
		gbc_cbbxZipLevel.fill = GridBagConstraints.BOTH;
		gbc_cbbxZipLevel.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxZipLevel.gridx = 1;
		gbc_cbbxZipLevel.gridy = 2;
		panelZip.add(cbbxZipLevel, gbc_cbbxZipLevel);

		JPanel panelZipE = new JPanel();
		compressorsPane.addTab(Messages.getString("MainFrame.ZipExternal"), null, panelZipE, null); //$NON-NLS-1$
		final GridBagLayout gbl_panelZipE = new GridBagLayout();
		gbl_panelZipE.columnWidths = new int[] { 85, 246, 40, 0 };
		gbl_panelZipE.rowHeights = new int[] { 0, 28, 28, 28, 0, 0 };
		gbl_panelZipE.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panelZipE.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelZipE.setLayout(gbl_panelZipE);

		JLabel lblZipECmd = new JLabel(Messages.getString("MainFrame.lblZipECmd.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblZipECmd = new GridBagConstraints();
		gbc_lblZipECmd.anchor = GridBagConstraints.EAST;
		gbc_lblZipECmd.insets = new Insets(5, 5, 5, 5);
		gbc_lblZipECmd.gridx = 0;
		gbc_lblZipECmd.gridy = 1;
		panelZipE.add(lblZipECmd, gbc_lblZipECmd);

		tfZipECmd = new JFileDropTextField(txt -> session.getUser().settings.setProperty("zip_cmd", txt));//$NON-NLS-1$
		tfZipECmd.setMode(JFileDropMode.FILE);
		tfZipECmd.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tfZipECmd.setText(session.getUser().settings.getProperty("zip_cmd", FindCmd.find7z())); //$NON-NLS-1$
		final GridBagConstraints gbc_tfZipECmd = new GridBagConstraints();
		gbc_tfZipECmd.insets = new Insets(0, 0, 5, 0);
		gbc_tfZipECmd.fill = GridBagConstraints.BOTH;
		gbc_tfZipECmd.gridx = 1;
		gbc_tfZipECmd.gridy = 1;
		panelZipE.add(tfZipECmd, gbc_tfZipECmd);
		tfZipECmd.setColumns(30);

		JButton btZipECmd = new JButton(""); //$NON-NLS-1$
		btZipECmd.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		final GridBagConstraints gbc_btZipECmd = new GridBagConstraints();
		gbc_btZipECmd.fill = GridBagConstraints.BOTH;
		gbc_btZipECmd.insets = new Insets(0, 0, 5, 5);
		gbc_btZipECmd.gridx = 2;
		gbc_btZipECmd.gridy = 1;
		panelZipE.add(btZipECmd, gbc_btZipECmd);

		JLabel lblZipEArgs = new JLabel(Messages.getString("MainFrame.lblZipEArgs.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblZipEArgs = new GridBagConstraints();
		gbc_lblZipEArgs.anchor = GridBagConstraints.EAST;
		gbc_lblZipEArgs.insets = new Insets(0, 5, 5, 5);
		gbc_lblZipEArgs.gridx = 0;
		gbc_lblZipEArgs.gridy = 2;
		panelZipE.add(lblZipEArgs, gbc_lblZipEArgs);

		cbZipEArgs = new JComboBox<>();
		cbZipEArgs.setEditable(false);
		cbZipEArgs.setModel(new DefaultComboBoxModel<>(ZipOptions.values()));
		cbZipEArgs.setRenderer(new DefaultListCellRenderer()
		{

			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				setText(((ZipOptions) value).getDesc());
				return this;
			}
		});
		cbZipEArgs.addActionListener(arg0 -> session.getUser().settings.setProperty("zip_level", cbZipEArgs.getSelectedItem().toString())); //$NON-NLS-1$
		cbZipEArgs.setSelectedItem(ZipOptions.valueOf(session.getUser().settings.getProperty("zip_level", ZipOptions.NORMAL.toString()))); //$NON-NLS-1$
		final GridBagConstraints gbc_cbZipEArgs = new GridBagConstraints();
		gbc_cbZipEArgs.insets = new Insets(0, 0, 5, 5);
		gbc_cbZipEArgs.gridwidth = 2;
		gbc_cbZipEArgs.fill = GridBagConstraints.BOTH;
		gbc_cbZipEArgs.gridx = 1;
		gbc_cbZipEArgs.gridy = 2;
		panelZipE.add(cbZipEArgs, gbc_cbZipEArgs);

		JLabel lblZipEThreads = new JLabel(Messages.getString("MainFrame.lblZipEThreads.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblZipEThreads = new GridBagConstraints();
		gbc_lblZipEThreads.insets = new Insets(0, 0, 5, 5);
		gbc_lblZipEThreads.anchor = GridBagConstraints.EAST;
		gbc_lblZipEThreads.gridx = 0;
		gbc_lblZipEThreads.gridy = 3;
		panelZipE.add(lblZipEThreads, gbc_lblZipEThreads);

		tfZipEThreads = new JTextField();
		tfZipEThreads.setText("1"); //$NON-NLS-1$
		final GridBagConstraints gbc_tfZipEThreads = new GridBagConstraints();
		gbc_tfZipEThreads.fill = GridBagConstraints.VERTICAL;
		gbc_tfZipEThreads.anchor = GridBagConstraints.WEST;
		gbc_tfZipEThreads.insets = new Insets(0, 0, 5, 5);
		gbc_tfZipEThreads.gridx = 1;
		gbc_tfZipEThreads.gridy = 3;
		panelZipE.add(tfZipEThreads, gbc_tfZipEThreads);
		tfZipEThreads.setColumns(4);

		JLabel lblZipEWarning = new JLabel();
		lblZipEWarning.setVerticalAlignment(SwingConstants.TOP);
		lblZipEWarning.setText(Messages.getString("MainFrame.lblZipEWarning.text")); //$NON-NLS-1$
		lblZipEWarning.setHorizontalAlignment(SwingConstants.CENTER);
		lblZipEWarning.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11)); //$NON-NLS-1$
		lblZipEWarning.setBackground(UIManager.getColor("Button.background")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblZipEWarning = new GridBagConstraints();
		gbc_lblZipEWarning.gridwidth = 3;
		gbc_lblZipEWarning.gridx = 0;
		gbc_lblZipEWarning.gridy = 4;
		panelZipE.add(lblZipEWarning, gbc_lblZipEWarning);

		JPanel panel7Zip = new JPanel();
		compressorsPane.addTab(Messages.getString("MainFrame.7zExternal"), null, panel7Zip, null); //$NON-NLS-1$
		compressorsPane.setEnabledAt(2, true);
		final GridBagLayout gbl_panel7Zip = new GridBagLayout();
		gbl_panel7Zip.columnWidths = new int[] { 85, 123, 0, 40, 0 };
		gbl_panel7Zip.rowHeights = new int[] { 0, 28, 28, 28, 0, 0 };
		gbl_panel7Zip.columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel7Zip.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panel7Zip.setLayout(gbl_panel7Zip);

		JLabel lbl7zCmd = new JLabel(Messages.getString("MainFrame.lbl7zCmd.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lbl7zCmd = new GridBagConstraints();
		gbc_lbl7zCmd.anchor = GridBagConstraints.EAST;
		gbc_lbl7zCmd.insets = new Insets(5, 5, 5, 5);
		gbc_lbl7zCmd.gridx = 0;
		gbc_lbl7zCmd.gridy = 1;
		panel7Zip.add(lbl7zCmd, gbc_lbl7zCmd);

		tf7zCmd = new JFileDropTextField(txt -> session.getUser().settings.setProperty("7z_cmd", txt)); //$NON-NLS-1$
		tf7zCmd.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tf7zCmd.setMode(JFileDropMode.FILE);
		tf7zCmd.setText(session.getUser().settings.getProperty("7z_cmd", FindCmd.find7z())); //$NON-NLS-1$
		tf7zCmd.setColumns(30);
		final GridBagConstraints gbc_tf7zCmd = new GridBagConstraints();
		gbc_tf7zCmd.gridwidth = 2;
		gbc_tf7zCmd.fill = GridBagConstraints.BOTH;
		gbc_tf7zCmd.insets = new Insets(0, 0, 5, 0);
		gbc_tf7zCmd.gridx = 1;
		gbc_tf7zCmd.gridy = 1;
		panel7Zip.add(tf7zCmd, gbc_tf7zCmd);

		JButton btn7zCmd = new JButton(""); //$NON-NLS-1$
		btn7zCmd.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		final GridBagConstraints gbc_btn7zCmd = new GridBagConstraints();
		gbc_btn7zCmd.fill = GridBagConstraints.BOTH;
		gbc_btn7zCmd.insets = new Insets(0, 0, 5, 5);
		gbc_btn7zCmd.gridx = 3;
		gbc_btn7zCmd.gridy = 1;
		panel7Zip.add(btn7zCmd, gbc_btn7zCmd);

		JLabel lbl7zArgs = new JLabel(Messages.getString("MainFrame.lbl7zArgs.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lbl7zArgs = new GridBagConstraints();
		gbc_lbl7zArgs.anchor = GridBagConstraints.EAST;
		gbc_lbl7zArgs.insets = new Insets(0, 5, 5, 5);
		gbc_lbl7zArgs.gridx = 0;
		gbc_lbl7zArgs.gridy = 2;
		panel7Zip.add(lbl7zArgs, gbc_lbl7zArgs);

		cb7zArgs = new JComboBox<>();
		cb7zArgs.addActionListener(arg0 -> session.getUser().settings.setProperty("7z_level", cb7zArgs.getSelectedItem().toString())); //$NON-NLS-1$
		cb7zArgs.setEditable(false);
		cb7zArgs.setModel(new DefaultComboBoxModel<>(SevenZipOptions.values()));
		cb7zArgs.setRenderer(new DefaultListCellRenderer()
		{

			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				setText(((SevenZipOptions) value).getName());
				return this;
			}
		});
		cb7zArgs.setSelectedItem(SevenZipOptions.valueOf(session.getUser().settings.getProperty("7z_level", SevenZipOptions.NORMAL.toString()))); //$NON-NLS-1$
		final GridBagConstraints gbc_cb7zArgs = new GridBagConstraints();
		gbc_cb7zArgs.fill = GridBagConstraints.BOTH;
		gbc_cb7zArgs.gridwidth = 3;
		gbc_cb7zArgs.insets = new Insets(0, 0, 5, 5);
		gbc_cb7zArgs.gridx = 1;
		gbc_cb7zArgs.gridy = 2;
		panel7Zip.add(cb7zArgs, gbc_cb7zArgs);

		JLabel lbl7zThreads = new JLabel(Messages.getString("MainFrame.lbl7zThreads.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lbl7zThreads = new GridBagConstraints();
		gbc_lbl7zThreads.insets = new Insets(0, 0, 5, 5);
		gbc_lbl7zThreads.anchor = GridBagConstraints.EAST;
		gbc_lbl7zThreads.gridx = 0;
		gbc_lbl7zThreads.gridy = 3;
		panel7Zip.add(lbl7zThreads, gbc_lbl7zThreads);

		tf7zThreads = new JTextField();
		tf7zThreads.setText(Integer.toString(session.getUser().settings.getProperty("7z_threads", -1))); //$NON-NLS-1$
		tf7zThreads.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(final FocusEvent e)
			{
				session.getUser().settings.setProperty("7z_threads", tf7zThreads.getText()); //$NON-NLS-1$
			}
		});
		tf7zThreads.addActionListener(arg0 -> session.getUser().settings.setProperty("7z_threads", tf7zThreads.getText())); //$NON-NLS-1$
		final GridBagConstraints gbc_tf7zThreads = new GridBagConstraints();
		gbc_tf7zThreads.fill = GridBagConstraints.VERTICAL;
		gbc_tf7zThreads.anchor = GridBagConstraints.WEST;
		gbc_tf7zThreads.insets = new Insets(0, 0, 5, 5);
		gbc_tf7zThreads.gridx = 1;
		gbc_tf7zThreads.gridy = 3;
		panel7Zip.add(tf7zThreads, gbc_tf7zThreads);
		tf7zThreads.setColumns(4);

		ckbx7zSolid = new JCheckBox(Messages.getString("MainFrame.ckbx7zSolid.text")); //$NON-NLS-1$
		ckbx7zSolid.setSelected(session.getUser().settings.getProperty("7z_solid", true)); //$NON-NLS-1$
		cb7zArgs.setEnabled(ckbx7zSolid.isSelected());
		ckbx7zSolid.addActionListener(arg0 -> {
			cb7zArgs.setEnabled(ckbx7zSolid.isSelected());
			session.getUser().settings.setProperty("7z_solid", ckbx7zSolid.isSelected()); //$NON-NLS-1$
		});
		final GridBagConstraints gbc_ckbx7zSolid = new GridBagConstraints();
		gbc_ckbx7zSolid.insets = new Insets(0, 0, 5, 5);
		gbc_ckbx7zSolid.gridx = 2;
		gbc_ckbx7zSolid.gridy = 3;
		panel7Zip.add(ckbx7zSolid, gbc_ckbx7zSolid);

		JLabel lbl7zWarning = new JLabel();
		lbl7zWarning.setVerticalAlignment(SwingConstants.TOP);
		lbl7zWarning.setText(Messages.getString("MainFrame.lbl7zWarning.text")); //$NON-NLS-1$
		lbl7zWarning.setHorizontalAlignment(SwingConstants.CENTER);
		lbl7zWarning.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11)); //$NON-NLS-1$
		lbl7zWarning.setBackground(UIManager.getColor("Button.background")); //$NON-NLS-1$
		final GridBagConstraints gbc_lbl7zWarning = new GridBagConstraints();
		gbc_lbl7zWarning.gridwidth = 4;
		gbc_lbl7zWarning.gridx = 0;
		gbc_lbl7zWarning.gridy = 4;
		panel7Zip.add(lbl7zWarning, gbc_lbl7zWarning);

	}

}
