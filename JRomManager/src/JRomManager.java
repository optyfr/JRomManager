import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.io.FileUtils;

import jrm.actions.ContainerAction;
import jrm.compressors.SevenZipOptions;
import jrm.compressors.ZipOptions;
import jrm.misc.BreakException;
import jrm.misc.FindCmd;
import jrm.misc.Log;
import jrm.misc.Settings;
import jrm.profiler.Import;
import jrm.profiler.Profile;
import jrm.profiler.Scan;
import jrm.profiler.scan.MergeOptions;
import jrm.ui.Progress;
import one.util.streamex.StreamEx;
import jrm.profiler.scan.FormatOptions;

public class JRomManager
{

	private JFrame mainFrame;
	
	private Profile curr_profile;
	private Scan curr_scan;
	
	private JButton btnScan;
	private JButton btnFix;
	private JTabbedPane tabbedPane;
	private JPanel profilesTab;
	private JPanel scannerTab;
	private JPanel scannerSettingsPanel;
	private JCheckBox chckbxNeedSHA1;
	private JCheckBox chckbxUseParallelism;
	private JComboBox<MergeOptions> cbbxMergeMode;
	private JTree profilesTree;
	private JList<Object> profilesList;
	private JPanel profilesBtnPanel;
	private JButton btnLoadProfile;
	private JButton btnImportDat;
	private JSplitPane profilesPanel;
	private JTextField txtRomsDest;
	private JButton btnRomsDest;
	private JLabel lblRomsDest;
	private JPanel scannerBtnPanel;
	private JPanel panel_4;
	private JLabel lblMergeMode;
	private JTextField textSrcDir;
	private JLabel lblSrcDir;
	private JButton btnSrcDir;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					JRomManager window = new JRomManager();
					window.mainFrame.setVisible(true);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public JRomManager()
	{
		try
		{
			Settings.loadSettings();
		//	UIManager.setLookAndFeel(getProperty("LookAndFeel",  UIManager.getSystemLookAndFeelClassName()/* UIManager.getCrossPlatformLookAndFeelClassName()*/));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		initialize();
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				if(curr_profile!=null)
					curr_profile.saveSettings();
				Settings.saveSettings();
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize()
	{
		mainFrame = new JFrame();
		mainFrame.setTitle("JRomManager");
		mainFrame.setBounds(100, 100, 458, 313);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		mainFrame.getContentPane().add(tabbedPane);
		
		profilesTab = new JPanel();
		tabbedPane.addTab("Profiles", null, profilesTab, null);
		GridBagLayout gbl_profilesTab = new GridBagLayout();
		gbl_profilesTab.columnWidths = new int[]{0, 0};
		gbl_profilesTab.rowHeights = new int[]{0, 0, 0};
		gbl_profilesTab.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_profilesTab.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		profilesTab.setLayout(gbl_profilesTab);
		
		profilesPanel = new JSplitPane();
		profilesPanel.setResizeWeight(0.25);
		profilesPanel.setOneTouchExpandable(true);
		GridBagConstraints gbc_profilesPanel = new GridBagConstraints();
		gbc_profilesPanel.insets = new Insets(0, 0, 5, 0);
		gbc_profilesPanel.fill = GridBagConstraints.BOTH;
		gbc_profilesPanel.gridx = 0;
		gbc_profilesPanel.gridy = 0;
		profilesTab.add(profilesPanel, gbc_profilesPanel);
		
		profilesTree = new JTree();
		profilesPanel.setLeftComponent(profilesTree);
		profilesTree.setSelectionRows(new int[] {5});
		profilesTree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("JTree") {
				{
				}
			}
		));
		profilesTree.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		profilesTree.setRootVisible(false);
		profilesTree.setEditable(true);
		
		profilesList = new JList<Object>();
		profilesPanel.setRightComponent(profilesList);
		profilesList.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
		profilesBtnPanel = new JPanel();
		GridBagConstraints gbc_profilesBtnPanel = new GridBagConstraints();
		gbc_profilesBtnPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_profilesBtnPanel.gridx = 0;
		gbc_profilesBtnPanel.gridy = 1;
		profilesTab.add(profilesBtnPanel, gbc_profilesBtnPanel);
		
		btnLoadProfile = new JButton("Load Profile");
		btnLoadProfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadProfile();
			}
		});
		profilesBtnPanel.add(btnLoadProfile);
		
		btnImportDat = new JButton("Import Dat");
		btnImportDat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				importDat();
			}
		});
		profilesBtnPanel.add(btnImportDat);
		
		scannerTab = new JPanel();
		tabbedPane.addTab("Scanner", null, scannerTab, null);
		tabbedPane.setEnabledAt(1, false);
		GridBagLayout gbl_scannerTab = new GridBagLayout();
		gbl_scannerTab.columnWidths = new int[]{104, 0};
		gbl_scannerTab.rowHeights = new int[]{0, 33, 0};
		gbl_scannerTab.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_scannerTab.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		scannerTab.setLayout(gbl_scannerTab);
		
		scannerBtnPanel = new JPanel();
		GridBagConstraints gbc_scannerBtnPanel = new GridBagConstraints();
		gbc_scannerBtnPanel.insets = new Insets(0, 0, 5, 0);
		gbc_scannerBtnPanel.fill = GridBagConstraints.BOTH;
		gbc_scannerBtnPanel.gridx = 0;
		gbc_scannerBtnPanel.gridy = 0;
		scannerTab.add(scannerBtnPanel, gbc_scannerBtnPanel);
		
		btnScan = new JButton("Scan");
		scannerBtnPanel.add(btnScan);
		btnScan.setEnabled(false);
		
		btnFix = new JButton("Fix");
		scannerBtnPanel.add(btnFix);
		btnFix.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fix();
			}
		});
		btnFix.setEnabled(false);
		btnScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scan();
			}
		});
		
		scannerSettingsPanel = new JPanel();
		scannerSettingsPanel.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_scannerSettingsPanel = new GridBagConstraints();
		gbc_scannerSettingsPanel.ipady = 20;
		gbc_scannerSettingsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_scannerSettingsPanel.fill = GridBagConstraints.BOTH;
		gbc_scannerSettingsPanel.gridx = 0;
		gbc_scannerSettingsPanel.gridy = 1;
		scannerTab.add(scannerSettingsPanel, gbc_scannerSettingsPanel);
		GridBagLayout gbl_scannerSettingsPanel = new GridBagLayout();
		gbl_scannerSettingsPanel.columnWidths = new int[]{301, 0};
		gbl_scannerSettingsPanel.rowHeights = new int[]{20, 20, 20, 0, 20, 0};
		gbl_scannerSettingsPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_scannerSettingsPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		scannerSettingsPanel.setLayout(gbl_scannerSettingsPanel);
		
		chckbxNeedSHA1 = new JCheckBox("Calculate all SHA1");
		chckbxNeedSHA1.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				curr_profile.setProperty("need_sha1_or_md5", e.getStateChange()==ItemEvent.SELECTED);
			}
		});
		chckbxNeedSHA1.setToolTipText("Calculate SHA1 while scanning new files, even if CRC is not suspicious (Slow process)");
		GridBagConstraints gbc_chckbxNeedSHA1 = new GridBagConstraints();
		gbc_chckbxNeedSHA1.fill = GridBagConstraints.BOTH;
		gbc_chckbxNeedSHA1.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxNeedSHA1.gridx = 0;
		gbc_chckbxNeedSHA1.gridy = 0;
		scannerSettingsPanel.add(chckbxNeedSHA1, gbc_chckbxNeedSHA1);
		
		chckbxUseParallelism = new JCheckBox("Enable MultiThreading");
		chckbxUseParallelism.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				curr_profile.setProperty("use_parallelism", e.getStateChange()==ItemEvent.SELECTED);
			}
		});
		chckbxUseParallelism.setToolTipText("Use all CPU while scanning and fixing, SSD is STRONGLY recommended otherwise you may get slower results!");
		GridBagConstraints gbc_chckbxUseParallelism = new GridBagConstraints();
		gbc_chckbxUseParallelism.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxUseParallelism.fill = GridBagConstraints.BOTH;
		gbc_chckbxUseParallelism.gridx = 0;
		gbc_chckbxUseParallelism.gridy = 1;
		scannerSettingsPanel.add(chckbxUseParallelism, gbc_chckbxUseParallelism);
		
		panel_4 = new JPanel();
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.insets = new Insets(0, 0, 5, 0);
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 2;
		scannerSettingsPanel.add(panel_4, gbc_panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_4.rowHeights = new int[]{0, 0, 8, 0, 0};
		gbl_panel_4.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_4.rowWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		panel_4.setLayout(gbl_panel_4);
		
		lblCompression = new JLabel("Compression");
		GridBagConstraints gbc_lblCompression = new GridBagConstraints();
		gbc_lblCompression.anchor = GridBagConstraints.EAST;
		gbc_lblCompression.insets = new Insets(0, 5, 5, 5);
		gbc_lblCompression.gridx = 0;
		gbc_lblCompression.gridy = 0;
		panel_4.add(lblCompression, gbc_lblCompression);
		
		cbCompression = new JComboBox<FormatOptions>();
		cbCompression.setModel(new DefaultComboBoxModel<FormatOptions>(FormatOptions.values()));
		cbCompression.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				setText(((FormatOptions)value).getDesc());
				return this;
			}
		});
		cbCompression.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				curr_profile.settings.setProperty("format", cbCompression.getSelectedItem().toString());
			}
		});
		GridBagConstraints gbc_cbCompression = new GridBagConstraints();
		gbc_cbCompression.gridwidth = 2;
		gbc_cbCompression.insets = new Insets(0, 0, 5, 5);
		gbc_cbCompression.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbCompression.gridx = 1;
		gbc_cbCompression.gridy = 0;
		panel_4.add(cbCompression, gbc_cbCompression);
		
		lblMergeMode = new JLabel("Merge Mode");
		GridBagConstraints gbc_lblMergeMode = new GridBagConstraints();
		gbc_lblMergeMode.insets = new Insets(0, 0, 5, 5);
		gbc_lblMergeMode.anchor = GridBagConstraints.EAST;
		gbc_lblMergeMode.gridx = 0;
		gbc_lblMergeMode.gridy = 1;
		panel_4.add(lblMergeMode, gbc_lblMergeMode);
		
		cbbxMergeMode = new JComboBox<>();
		GridBagConstraints gbc_cbbxMergeMode = new GridBagConstraints();
		gbc_cbbxMergeMode.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxMergeMode.gridwidth = 2;
		gbc_cbbxMergeMode.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxMergeMode.gridx = 1;
		gbc_cbbxMergeMode.gridy = 1;
		panel_4.add(cbbxMergeMode, gbc_cbbxMergeMode);
		cbbxMergeMode.setToolTipText("Select the Merge mode");
		cbbxMergeMode.setModel(new DefaultComboBoxModel<MergeOptions>(MergeOptions.values()));
		cbbxMergeMode.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				setText(((MergeOptions)value).getDesc());
				return this;
			}
		});
		cbbxMergeMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				curr_profile.settings.setProperty("merge_mode", cbbxMergeMode.getSelectedItem().toString());
			}
		});
		
		lblRomsDest = new JLabel("Roms Dest.");
		GridBagConstraints gbc_lblRomsDest = new GridBagConstraints();
		gbc_lblRomsDest.insets = new Insets(0, 0, 5, 5);
		gbc_lblRomsDest.gridx = 0;
		gbc_lblRomsDest.gridy = 2;
		panel_4.add(lblRomsDest, gbc_lblRomsDest);
		
		txtRomsDest = new JTextField();
		txtRomsDest.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				curr_profile.setProperty("roms_dest_dir", txtRomsDest.getText());
			}
		});
		GridBagConstraints gbc_txtRomsDest = new GridBagConstraints();
		gbc_txtRomsDest.insets = new Insets(0, 0, 5, 0);
		gbc_txtRomsDest.fill = GridBagConstraints.BOTH;
		gbc_txtRomsDest.gridx = 1;
		gbc_txtRomsDest.gridy = 2;
		panel_4.add(txtRomsDest, gbc_txtRomsDest);
		txtRomsDest.setColumns(10);
		
		btnRomsDest = new JButton("...");
		btnRomsDest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new JFileChooser()
				{{
					File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
					setCurrentDirectory(workdir);
					setFileSelectionMode(DIRECTORIES_ONLY);
					setSelectedFile(new File(txtRomsDest.getText()));
					setDialogTitle("Choose Roms Destination");
					if(showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
					{
						txtRomsDest.setText(getSelectedFile().getAbsolutePath());
						curr_profile.setProperty("roms_dest_dir", txtRomsDest.getText());
					}
				}};
			}
		});
		GridBagConstraints gbc_btnRomsDest = new GridBagConstraints();
		gbc_btnRomsDest.insets = new Insets(0, 0, 5, 5);
		gbc_btnRomsDest.gridx = 2;
		gbc_btnRomsDest.gridy = 2;
		panel_4.add(btnRomsDest, gbc_btnRomsDest);
		
		lblSrcDir = new JLabel("Src Dir.");
		GridBagConstraints gbc_lblSrcDir = new GridBagConstraints();
		gbc_lblSrcDir.insets = new Insets(0, 0, 0, 5);
		gbc_lblSrcDir.anchor = GridBagConstraints.EAST;
		gbc_lblSrcDir.gridx = 0;
		gbc_lblSrcDir.gridy = 3;
		panel_4.add(lblSrcDir, gbc_lblSrcDir);
		
		textSrcDir = new JTextField();
		textSrcDir.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				curr_profile.setProperty("src_dir", textSrcDir.getText());
			}
		});
		GridBagConstraints gbc_textSrcDir = new GridBagConstraints();
		gbc_textSrcDir.fill = GridBagConstraints.BOTH;
		gbc_textSrcDir.gridx = 1;
		gbc_textSrcDir.gridy = 3;
		panel_4.add(textSrcDir, gbc_textSrcDir);
		
		btnSrcDir = new JButton("...");
		btnSrcDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new JFileChooser()
				{{
					File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
					setCurrentDirectory(workdir);
					setFileSelectionMode(DIRECTORIES_ONLY);
					setSelectedFile(new File(textSrcDir.getText()));
					setDialogTitle("Choose Source Dir");
					if(showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
					{
						textSrcDir.setText(getSelectedFile().getAbsolutePath());
						curr_profile.setProperty("src_dir", textSrcDir.getText());
					}
				}};
			}
		});
		GridBagConstraints gbc_btnSrcDir = new GridBagConstraints();
		gbc_btnSrcDir.insets = new Insets(0, 0, 0, 5);
		gbc_btnSrcDir.gridx = 2;
		gbc_btnSrcDir.gridy = 3;
		panel_4.add(btnSrcDir, gbc_btnSrcDir);
		
		settingsTab = new JPanel();
		tabbedPane.addTab("Settings", null, settingsTab, null);
		settingsTab.setLayout(new BorderLayout(0, 0));
		
		tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		settingsTab.add(tabbedPane_1);
		
		panel = new JPanel();
		tabbedPane_1.addTab("Compressors", null, panel, null);
		tabbedPane_1.setEnabledAt(0, true);
		panel.setLayout(new BorderLayout(0, 0));
		
		tabbedPane_2 = new JTabbedPane(JTabbedPane.TOP);
		panel.add(tabbedPane_2);
		
		panel_1 = new JPanel();
		tabbedPane_2.addTab("Zip", null, panel_1, null);
		panel_1.setLayout(new GridLayout(0, 1, 0, 0));
		
		chckbxZipUseTemp = new JCheckBox("Use temporary files (recommended)");
		chckbxZipUseTemp.setHorizontalAlignment(SwingConstants.CENTER);
		chckbxZipUseTemp.setSelected(true);
		panel_1.add(chckbxZipUseTemp);
		
		lblZipWarn = new JLabel();
		lblZipWarn.setVerticalAlignment(SwingConstants.TOP);
		lblZipWarn.setHorizontalAlignment(SwingConstants.CENTER);
		lblZipWarn.setBackground(UIManager.getColor("Panel.background"));
		lblZipWarn.setText("<html><center>There is no compression method choice available, <br>if you need to specify one then you will have to use zip (external)</center></html>");
		lblZipWarn.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
		panel_1.add(lblZipWarn);
		
		panel_2 = new JPanel();
		tabbedPane_2.addTab("Zip (external)", null, panel_2, null);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{85, 246, 40, 0};
		gbl_panel_2.rowHeights = new int[]{0, 28, 28, 0, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		lblZipCmd = new JLabel("Path to command");
		GridBagConstraints gbc_lblZipCmd = new GridBagConstraints();
		gbc_lblZipCmd.anchor = GridBagConstraints.EAST;
		gbc_lblZipCmd.insets = new Insets(5, 5, 5, 5);
		gbc_lblZipCmd.gridx = 0;
		gbc_lblZipCmd.gridy = 1;
		panel_2.add(lblZipCmd, gbc_lblZipCmd);
		
		tfZipCmd = new JTextField();
		tfZipCmd.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				Settings.setProperty("zip_cmd", tfZipCmd.getText());
			}
		});
		tfZipCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.setProperty("zip_cmd", tfZipCmd.getText());
			}
		});
		tfZipCmd.setText(Settings.getProperty("zip_cmd", FindCmd.find7z()));
		GridBagConstraints gbc_tfZipCmd = new GridBagConstraints();
		gbc_tfZipCmd.insets = new Insets(0, 0, 5, 0);
		gbc_tfZipCmd.fill = GridBagConstraints.BOTH;
		gbc_tfZipCmd.gridx = 1;
		gbc_tfZipCmd.gridy = 1;
		panel_2.add(tfZipCmd, gbc_tfZipCmd);
		tfZipCmd.setColumns(30);
		
		btZipCmd = new JButton("");
		btZipCmd.setIcon(new ImageIcon(JRomManager.class.getResource("/javax/swing/plaf/metal/icons/ocean/floppy.gif")));
		GridBagConstraints gbc_btZipCmd = new GridBagConstraints();
		gbc_btZipCmd.fill = GridBagConstraints.BOTH;
		gbc_btZipCmd.insets = new Insets(0, 0, 5, 5);
		gbc_btZipCmd.gridx = 2;
		gbc_btZipCmd.gridy = 1;
		panel_2.add(btZipCmd, gbc_btZipCmd);
		
		lblZipArgs = new JLabel("Compression args");
		GridBagConstraints gbc_lblZipArgs = new GridBagConstraints();
		gbc_lblZipArgs.anchor = GridBagConstraints.EAST;
		gbc_lblZipArgs.insets = new Insets(0, 5, 5, 5);
		gbc_lblZipArgs.gridx = 0;
		gbc_lblZipArgs.gridy = 2;
		panel_2.add(lblZipArgs, gbc_lblZipArgs);
		
		cbZipArgs = new JComboBox<>();
		cbZipArgs.setEditable(true);
		cbZipArgs.setModel(new DefaultComboBoxModel<ZipOptions>(ZipOptions.values()));
		cbZipArgs.setRenderer(new DefaultListCellRenderer()
		{

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				setText(((ZipOptions)value).getName());
				return this;
			}
		});
		cbZipArgs.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				Settings.setProperty("zip_args", cbZipArgs.getSelectedItem().toString());
			}
		});
		cbZipArgs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.setProperty("zip_args", cbZipArgs.getSelectedItem().toString());
			}
		});
		cbZipArgs.setSelectedItem(Settings.getProperty("zip_args", ZipOptions.SEVENZIP_ULTRA.toString()));
		GridBagConstraints gbc_cbZipArgs = new GridBagConstraints();
		gbc_cbZipArgs.insets = new Insets(0, 0, 5, 5);
		gbc_cbZipArgs.gridwidth = 2;
		gbc_cbZipArgs.fill = GridBagConstraints.BOTH;
		gbc_cbZipArgs.gridx = 1;
		gbc_cbZipArgs.gridy = 2;
		panel_2.add(cbZipArgs, gbc_cbZipArgs);
		
		
		lblInternalMethodsAre = new JLabel();
		lblInternalMethodsAre.setVerticalAlignment(SwingConstants.TOP);
		lblInternalMethodsAre.setText("<html>\r\n<center>\r\nInternal methods are still used for listing and decompression...\r\n</center>\r\n</html>");
		lblInternalMethodsAre.setHorizontalAlignment(SwingConstants.CENTER);
		lblInternalMethodsAre.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
		lblInternalMethodsAre.setBackground(UIManager.getColor("Button.background"));
		GridBagConstraints gbc_lblInternalMethodsAre = new GridBagConstraints();
		gbc_lblInternalMethodsAre.gridwidth = 3;
		gbc_lblInternalMethodsAre.gridx = 0;
		gbc_lblInternalMethodsAre.gridy = 3;
		panel_2.add(lblInternalMethodsAre, gbc_lblInternalMethodsAre);
		
		panel_6 = new JPanel();
		tabbedPane_2.addTab("7z (external)", null, panel_6, null);
		tabbedPane_2.setEnabledAt(2, true);
		GridBagLayout gbl_panel_6 = new GridBagLayout();
		gbl_panel_6.columnWidths = new int[]{85, 246, 40, 0};
		gbl_panel_6.rowHeights = new int[]{0, 28, 28, 0, 0};
		gbl_panel_6.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_6.rowWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_6.setLayout(gbl_panel_6);
		
		lbl7zCmd = new JLabel("Path to command");
		GridBagConstraints gbc_lbl7zCmd = new GridBagConstraints();
		gbc_lbl7zCmd.anchor = GridBagConstraints.EAST;
		gbc_lbl7zCmd.insets = new Insets(5, 5, 5, 5);
		gbc_lbl7zCmd.gridx = 0;
		gbc_lbl7zCmd.gridy = 1;
		panel_6.add(lbl7zCmd, gbc_lbl7zCmd);
		
		tf7zCmd = new JTextField();
		tf7zCmd.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				Settings.setProperty("7z_cmd", tf7zCmd.getText());
			}
		});
		tf7zCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.setProperty("7z_cmd", tf7zCmd.getText());
			}
		});
		tf7zCmd.setText(Settings.getProperty("7z_cmd", FindCmd.find7z()));
		tf7zCmd.setColumns(30);
		GridBagConstraints gbc_tf7zCmd = new GridBagConstraints();
		gbc_tf7zCmd.fill = GridBagConstraints.BOTH;
		gbc_tf7zCmd.insets = new Insets(0, 0, 5, 0);
		gbc_tf7zCmd.gridx = 1;
		gbc_tf7zCmd.gridy = 1;
		panel_6.add(tf7zCmd, gbc_tf7zCmd);
		
		btn7zCmd = new JButton("");
		btn7zCmd.setIcon(new ImageIcon(JRomManager.class.getResource("/javax/swing/plaf/metal/icons/ocean/floppy.gif")));
		GridBagConstraints gbc_btn7zCmd = new GridBagConstraints();
		gbc_btn7zCmd.fill = GridBagConstraints.BOTH;
		gbc_btn7zCmd.insets = new Insets(0, 0, 5, 5);
		gbc_btn7zCmd.gridx = 2;
		gbc_btn7zCmd.gridy = 1;
		panel_6.add(btn7zCmd, gbc_btn7zCmd);
		
		lbl7zArgs = new JLabel("Compression args");
		GridBagConstraints gbc_lbl7zArgs = new GridBagConstraints();
		gbc_lbl7zArgs.anchor = GridBagConstraints.EAST;
		gbc_lbl7zArgs.insets = new Insets(0, 5, 5, 5);
		gbc_lbl7zArgs.gridx = 0;
		gbc_lbl7zArgs.gridy = 2;
		panel_6.add(lbl7zArgs, gbc_lbl7zArgs);
		
		cb7zArgs = new JComboBox<SevenZipOptions>();
		cb7zArgs.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				Settings.setProperty("7z_args", cb7zArgs.getSelectedItem().toString());
			}
		});
		cb7zArgs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Settings.setProperty("7z_args", cb7zArgs.getSelectedItem().toString());
			}
		});
		cb7zArgs.setEditable(true);
		cb7zArgs.setModel(new DefaultComboBoxModel<SevenZipOptions>(SevenZipOptions.values()));
		cb7zArgs.setRenderer(new DefaultListCellRenderer()
		{

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				setText(((SevenZipOptions)value).getName());
				return this;
			}
		});
		cb7zArgs.setSelectedItem(Settings.getProperty("7z_args", SevenZipOptions.SEVENZIP_ULTRA.toString()));
		GridBagConstraints gbc_cb7zArgs = new GridBagConstraints();
		gbc_cb7zArgs.fill = GridBagConstraints.BOTH;
		gbc_cb7zArgs.gridwidth = 2;
		gbc_cb7zArgs.insets = new Insets(0, 0, 5, 5);
		gbc_cb7zArgs.gridx = 1;
		gbc_cb7zArgs.gridy = 2;
		panel_6.add(cb7zArgs, gbc_cb7zArgs);
		
		lblInternalMethodsAre_1 = new JLabel();
		lblInternalMethodsAre_1.setVerticalAlignment(SwingConstants.TOP);
		lblInternalMethodsAre_1.setText("<html>\r\n<center>\r\nInternal methods are still used for listing...\r\n</center>\r\n</html>");
		lblInternalMethodsAre_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblInternalMethodsAre_1.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
		lblInternalMethodsAre_1.setBackground(UIManager.getColor("Button.background"));
		GridBagConstraints gbc_lblInternalMethodsAre_1 = new GridBagConstraints();
		gbc_lblInternalMethodsAre_1.gridwidth = 3;
		gbc_lblInternalMethodsAre_1.gridx = 0;
		gbc_lblInternalMethodsAre_1.gridy = 3;
		panel_6.add(lblInternalMethodsAre_1, gbc_lblInternalMethodsAre_1);
		
		panel_3 = new JPanel();
		tabbedPane_2.addTab("TorrentZip (external)", null, panel_3, null);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{100, 334, 34, 0};
		gbl_panel_3.rowHeights = new int[]{0, 20, 0, 0};
		gbl_panel_3.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		lblTZipCmd = new JLabel("Path to command");
		GridBagConstraints gbc_lblTZipCmd = new GridBagConstraints();
		gbc_lblTZipCmd.anchor = GridBagConstraints.WEST;
		gbc_lblTZipCmd.insets = new Insets(0, 5, 5, 5);
		gbc_lblTZipCmd.gridx = 0;
		gbc_lblTZipCmd.gridy = 1;
		panel_3.add(lblTZipCmd, gbc_lblTZipCmd);
		
		tfTZipCmd = new JTextField();
		tfTZipCmd.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				Settings.setProperty("tzip_cmd", tfTZipCmd.getText());
			}
		});
		tfTZipCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings.setProperty("tzip_cmd", tfTZipCmd.getText());
			}
		});
		tfTZipCmd.setHorizontalAlignment(SwingConstants.LEFT);
		tfTZipCmd.setText(Settings.getProperty("tzip_cmd", FindCmd.findTZip()));
		tfTZipCmd.setColumns(30);
		GridBagConstraints gbc_tfTZipCmd = new GridBagConstraints();
		gbc_tfTZipCmd.fill = GridBagConstraints.BOTH;
		gbc_tfTZipCmd.insets = new Insets(0, 0, 5, 0);
		gbc_tfTZipCmd.gridx = 1;
		gbc_tfTZipCmd.gridy = 1;
		panel_3.add(tfTZipCmd, gbc_tfTZipCmd);
		
		btTZipCmd = new JButton("");
		btTZipCmd.setIcon(new ImageIcon(JRomManager.class.getResource("/javax/swing/plaf/metal/icons/ocean/floppy.gif")));
		GridBagConstraints gbc_btTZipCmd = new GridBagConstraints();
		gbc_btTZipCmd.insets = new Insets(0, 0, 5, 5);
		gbc_btTZipCmd.anchor = GridBagConstraints.WEST;
		gbc_btTZipCmd.gridx = 2;
		gbc_btTZipCmd.gridy = 1;
		panel_3.add(btTZipCmd, gbc_btTZipCmd);
		tabbedPane.setEnabledAt(2, true);
		
		mainFrame.pack();
	}

	@SuppressWarnings("serial")
	private void importDat()
	{
		try
		{
			new JFileChooser()
			{{
				addChoosableFileFilter(new FileNameExtensionFilter("Dat file", "dat", "xml"));
				addChoosableFileFilter(new FileNameExtensionFilter("Mame executable", "exe"));
				if(showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
				{
					Import imprt = new Import(getSelectedFile());
					new JFileChooser()
					{{
						setFileSelectionMode(JFileChooser.FILES_ONLY);
						setDialogType(JFileChooser.SAVE_DIALOG);
						setSelectedFile(imprt.file);
						if(showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
						{
							FileUtils.copyFile(imprt.file, getSelectedFile());
						}
					}};
				}
			}};
		}
		catch (IOException e)
		{
			Log.err("Encountered IO Exception", e);
		}
		
	}
	
	@SuppressWarnings("serial")
	private void loadProfile()
	{
		if(curr_profile!=null)
			curr_profile.saveSettings();
		new JFileChooser()
		{{
			File workdir = Paths.get("./xmlfiles").toAbsolutePath().normalize().toFile();
			setCurrentDirectory(workdir);
			addChoosableFileFilter(new FileNameExtensionFilter("Dat file", "dat", "xml"));
			if(showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
			{
				final Progress progress = new Progress(mainFrame);
				SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){
					boolean success = false;
					
					@Override
					protected Void doInBackground() throws Exception
					{
						success = (null!=(curr_profile = Profile.load(getSelectedFile(),progress)));
						tabbedPane.setEnabledAt(1, success);
						btnScan.setEnabled(success);
						btnFix.setEnabled(false);
						return null;
					}
					
					@Override
					protected void done() {
						progress.dispose();
						if(success && curr_profile!=null)
						{
							initScanSettings();
							tabbedPane.setSelectedIndex(1);
						}
					}
					
				};
				worker.execute();
				progress.setVisible(true);
			}
		}};
	}
	
	private void scan()
	{
		String txtdstdir = txtRomsDest.getText();
		if(txtdstdir.isEmpty())
		{
			btnRomsDest.doClick();
			txtdstdir = txtRomsDest.getText();
		}
		if(txtdstdir.isEmpty())
			return;
		File dstdir = new File(txtdstdir);
		if(!dstdir.isDirectory())
			return;
		
		String txtsrcdir = textSrcDir.getText();
		List<File> srcdirs = new ArrayList<>();
		if(!txtsrcdir.isEmpty())
		{
			File srcdir = new File(txtsrcdir);
			if(!srcdir.isDirectory())
				return;
			srcdirs.add(srcdir);
		}

		final Progress progress = new Progress(mainFrame);
		SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){

			@Override
			protected Void doInBackground() throws Exception
			{
				curr_scan = new Scan(curr_profile, dstdir, srcdirs, progress);
				AtomicInteger actions_todo = new AtomicInteger(0);
				curr_scan.actions.forEach(actions -> actions_todo.addAndGet(actions.size()));
				btnFix.setEnabled(actions_todo.get()>0);
				return null;
			}
			
			@Override
			protected void done() {
				progress.dispose();
			}
			
		};
		worker.execute();
		progress.setVisible(true);
	}
	
	private void fix()
	{
		final Progress progress = new Progress(mainFrame);
		SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){

			@Override
			protected Void doInBackground() throws Exception
			{
				boolean use_parallelism = curr_profile.getProperty("use_parallelism", false);
				AtomicInteger i = new AtomicInteger(0), max = new AtomicInteger(0);
				curr_scan.actions.forEach(actions -> max.addAndGet(actions.size()));
				progress.setProgress("Fixing...", i.get(), max.get());
				curr_scan.actions.forEach(actions -> {
					List<ContainerAction> done = Collections.synchronizedList(new ArrayList<ContainerAction>());
					StreamEx.of(use_parallelism?actions.parallelStream().unordered():actions.stream()).takeWhile((action)->!progress.isCancel()).forEach(action -> {
						try
						{
							if (!action.doAction(progress))
								progress.cancel();
							done.add(action);
							progress.setProgress(null, i.incrementAndGet());
						}
						catch(BreakException be)
						{
							progress.cancel();
						}
					});
					actions.removeAll(done);
				});
				AtomicInteger actions_remain = new AtomicInteger(0);
				curr_scan.actions.forEach(actions -> actions_remain.addAndGet(actions.size()));
				btnFix.setEnabled(actions_remain.get()>0);
				return null;
			}
			
			@Override
			protected void done() {
				progress.dispose();
			}
			
		};
		worker.execute();
		progress.setVisible(true);
	}
	
	public void initScanSettings()
	{
		chckbxNeedSHA1.setSelected(curr_profile.getProperty("need_sha1_or_md5", false));
		chckbxUseParallelism.setSelected(curr_profile.getProperty("use_parallelism", false));
		txtRomsDest.setText(curr_profile.getProperty("roms_dest_dir", ""));
		textSrcDir.setText(curr_profile.getProperty("src_dir", ""));
		cbCompression.setSelectedItem(FormatOptions.valueOf(curr_profile.settings.getProperty("format", FormatOptions.ZIP.toString())));
		cbbxMergeMode.setSelectedItem(MergeOptions.valueOf(curr_profile.settings.getProperty("merge_mode", MergeOptions.SPLIT.toString())));
	}

	private JPanel settingsTab;
	private JTabbedPane tabbedPane_1;
	private JPanel panel;
	private JTabbedPane tabbedPane_2;
	private JPanel panel_1;
	private JCheckBox chckbxZipUseTemp;
	private JPanel panel_2;
	private JPanel panel_3;
	private JLabel lblZipCmd;
	private JTextField tfZipCmd;
	private JButton btZipCmd;
	private JLabel lblZipArgs;
	private JComboBox<ZipOptions> cbZipArgs;
	private JLabel lblZipWarn;
	private JPanel panel_6;
	private JLabel lbl7zCmd;
	private JTextField tf7zCmd;
	private JButton btn7zCmd;
	private JLabel lbl7zArgs;
	private JComboBox<SevenZipOptions> cb7zArgs;
	private JLabel lblTZipCmd;
	private JTextField tfTZipCmd;
	private JButton btTZipCmd;
	private JLabel lblInternalMethodsAre;
	private JLabel lblInternalMethodsAre_1;
	private JLabel lblCompression;
	private JComboBox<FormatOptions> cbCompression;
	
}
