package jrm.ui;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SerializationUtils;

import jrm.Messages;
import jrm.compressors.SevenZipOptions;
import jrm.compressors.ZipOptions;
import jrm.misc.FindCmd;
import jrm.misc.Settings;
import jrm.profiler.Import;
import jrm.profiler.Profile;
import jrm.profiler.fix.Fix;
import jrm.profiler.scan.Scan;
import jrm.profiler.scan.options.FormatOptions;
import jrm.profiler.scan.options.HashCollisionOptions;
import jrm.profiler.scan.options.MergeOptions;

@SuppressWarnings("serial")
public class MainFrame extends JFrame
{

	private Profile curr_profile;
	private Scan curr_scan;
	private ReportFrame report_frame = null;

	private JButton btnScan;
	private JButton btnFix;
	private JTabbedPane mainPane;
	private JPanel profilesTab;
	private JPanel scannerTab;
	private JPanel scannerSettingsPanel;
	private JCheckBox chckbxNeedSHA1;
	private JCheckBox chckbxUseParallelism;
	private JComboBox<MergeOptions> cbbxMergeMode;
	private JTree profilesTree;
	private JTable profilesList;
	private JPanel profilesBtnPanel;
	private JButton btnLoadProfile;
	private JButton btnImportDat;
	private JSplitPane profilesPanel;
	private JTextField txtRomsDest;
	private JButton btnRomsDest;
	private JLabel lblRomsDest;
	private JPanel scannerBtnPanel;
	private JPanel scannerSubSettingsPanel;
	private JLabel lblMergeMode;
	private JList<File> listSrcDir;
	private JLabel lblSrcDir;

	/**
	 * Launch the application.
	 */
	/**
	 * Create the application.
	 */
	public MainFrame()
	{
		super();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Settings.setProperty("MainFrame.Bounds", Hex.encodeHexString(SerializationUtils.serialize(getBounds())));
			}
		});
		try
		{
			Settings.loadSettings();
			UIManager.setLookAndFeel(Settings.getProperty("LookAndFeel", UIManager.getSystemLookAndFeelClassName()/* UIManager.getCrossPlatformLookAndFeelClassName()*/)); //$NON-NLS-1$
			File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
			File xmldir = new File(workdir, "xmlfiles"); //$NON-NLS-1$
			xmldir.mkdir();
			ResourceBundle.getBundle("jrm.resources.Messages"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		initialize();
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				if(curr_profile != null)
					curr_profile.saveSettings();
				Settings.saveSettings();
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/jrm/resources/rom.png"))); //$NON-NLS-1$
		setTitle(Messages.getString("MainFrame.Title")+" "+this.getClass().getPackage().getSpecificationVersion()+" "+this.getClass().getPackage().getImplementationVersion()); //$NON-NLS-1$
		setBounds(100, 100, 458, 313);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		mainPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(mainPane);

		report_frame = new ReportFrame(MainFrame.this);

		profilesTab = new JPanel();
		mainPane.addTab(Messages.getString("MainFrame.Profiles"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/script.png")), profilesTab, null); //$NON-NLS-1$ //$NON-NLS-2$
		GridBagLayout gbl_profilesTab = new GridBagLayout();
		gbl_profilesTab.columnWidths = new int[] { 0, 0 };
		gbl_profilesTab.rowHeights = new int[] { 0, 0, 0 };
		gbl_profilesTab.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_profilesTab.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		profilesTab.setLayout(gbl_profilesTab);

		profilesPanel = new JSplitPane();
		profilesPanel.setContinuousLayout(true);
		profilesPanel.setResizeWeight(0.3);
		profilesPanel.setOneTouchExpandable(true);
		GridBagConstraints gbc_profilesPanel = new GridBagConstraints();
		gbc_profilesPanel.insets = new Insets(0, 0, 5, 0);
		gbc_profilesPanel.fill = GridBagConstraints.BOTH;
		gbc_profilesPanel.gridx = 0;
		gbc_profilesPanel.gridy = 0;
		profilesTab.add(profilesPanel, gbc_profilesPanel);

		scrollPane = new JScrollPane();
		profilesPanel.setRightComponent(scrollPane);

		profilesList = new JTable();
		DefaultCellEditor editor = (DefaultCellEditor) profilesList.getDefaultEditor(Object.class);
		editor.setClickCountToStart(3);
		profilesList.setShowVerticalLines(false);
		profilesList.setShowHorizontalLines(false);
		profilesList.setShowGrid(false);
		profilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		profilesList.setPreferredScrollableViewportSize(new Dimension(300, 400));
		profilesList.setFillsViewportHeight(true);
		scrollPane.setViewportView(profilesList);
		FileTableModel filemodel = new FileTableModel();
		profilesList.setModel(filemodel);
		profilesList.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		profilesList.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() == 2)
				{
					JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					if(row >= 0)
					{
						loadProfile(filemodel.getFileAt(row));
					}
				}
				//super.mouseClicked(e);
			}
		});

		scrollPane_1 = new JScrollPane();
		scrollPane_1.setMinimumSize(new Dimension(100, 22));
		profilesPanel.setLeftComponent(scrollPane_1);

		profilesTree = new JTree();
		scrollPane_1.setViewportView(profilesTree);
		DirTreeModel profilesTreeModel = new DirTreeModel(new DirNode(Paths.get("./xmlfiles").toAbsolutePath().normalize().toFile())); //$NON-NLS-1$
		profilesTree.setModel(profilesTreeModel);
		profilesTree.setRootVisible(true);
		profilesTree.setShowsRootHandles(true);
		profilesTree.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		profilesTree.setEditable(true);
		DirTreeCellRenderer profilesTreeRenderer = new DirTreeCellRenderer();
		profilesTree.setCellRenderer(profilesTreeRenderer);
		profilesTree.setCellEditor(new DirTreeCellEditor(profilesTree, profilesTreeRenderer));
		profilesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		profilesTree.addTreeSelectionListener(new DirTreeSelectionListener(profilesList));
		
		popupMenu_2 = new JPopupMenu();
		popupMenu_2.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				mntmDeleteProfile.setEnabled(profilesList.getSelectedRowCount() > 0);
			}
		});
		addPopup(profilesList, popupMenu_2);
		
		mntmDeleteProfile = new JMenuItem(Messages.getString("MainFrame.mntmDeleteProfile.text")); //$NON-NLS-1$
		mntmDeleteProfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = profilesList.getSelectedRow();
				if(row >= 0)
				{
					File to_delete = (File)filemodel.getValueAt(row, 0);
					to_delete.delete();
					new File(to_delete.getAbsolutePath()+".cache").delete(); //$NON-NLS-1$
					new File(to_delete.getAbsolutePath()+".properties").delete(); //$NON-NLS-1$
					filemodel.populate();
				}
			}
		});
		mntmDeleteProfile.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/script_delete.png"))); //$NON-NLS-1$
		popupMenu_2.add(mntmDeleteProfile);
		
		mntmRenameProfile = new JMenuItem(Messages.getString("MainFrame.mntmRenameProfile.text")); //$NON-NLS-1$
		mntmRenameProfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = profilesList.getSelectedRow();
				if(row >= 0)
				{
					profilesList.editCellAt(row, 0);
				}
			}
		});
		mntmRenameProfile.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/script_edit.png"))); //$NON-NLS-1$
		popupMenu_2.add(mntmRenameProfile);
		profilesTree.setSelectionRow(0);

		popupMenu_1 = new JPopupMenu();
		popupMenu_1.addPopupMenuListener(new PopupMenuListener()
		{
			public void popupMenuCanceled(PopupMenuEvent e)
			{
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				mntmCreateFolder.setEnabled(profilesTree.getSelectionCount() > 0);
				mntmDeleteFolder.setEnabled(profilesTree.getSelectionCount() > 0 && !((DirNode) profilesTree.getLastSelectedPathComponent()).isRoot());
			}
		});
		addPopup(profilesTree, popupMenu_1);

		mntmCreateFolder = new JMenuItem(Messages.getString("MainFrame.mntmCreateFolder.text")); //$NON-NLS-1$
		mntmCreateFolder.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DirNode selectedNode = (DirNode) profilesTree.getLastSelectedPathComponent();
				if(selectedNode != null)
				{
					DirNode newnode = new DirNode(new DirNode.Dir(new File(selectedNode.dir.getFile(), Messages.getString("MainFrame.NewFolder")))); //$NON-NLS-1$
					selectedNode.add(newnode);
					profilesTreeModel.reload(selectedNode);
					TreePath path = new TreePath(newnode.getPath());
					profilesTree.setSelectionPath(path);
					profilesTree.startEditingAtPath(path);
				}
			}
		});
		mntmCreateFolder.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/folder_add.png"))); //$NON-NLS-1$
		popupMenu_1.add(mntmCreateFolder);

		mntmDeleteFolder = new JMenuItem(Messages.getString("MainFrame.mntmDeleteFolder.text")); //$NON-NLS-1$
		mntmDeleteFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DirNode selectedNode = (DirNode) profilesTree.getLastSelectedPathComponent();
				if(selectedNode != null)
				{
					DirNode parent = (DirNode)selectedNode.getParent();
					profilesTreeModel.removeNodeFromParent(selectedNode);
					TreePath path = new TreePath(parent.getPath());
					profilesTree.setSelectionPath(path);
				}
			}
		});
		mntmDeleteFolder.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/folder_delete.png"))); //$NON-NLS-1$
		popupMenu_1.add(mntmDeleteFolder);

		profilesBtnPanel = new JPanel();
		GridBagConstraints gbc_profilesBtnPanel = new GridBagConstraints();
		gbc_profilesBtnPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_profilesBtnPanel.gridx = 0;
		gbc_profilesBtnPanel.gridy = 1;
		profilesTab.add(profilesBtnPanel, gbc_profilesBtnPanel);

		btnLoadProfile = new JButton(Messages.getString("MainFrame.btnLoadProfile.text")); //$NON-NLS-1$
		btnLoadProfile.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/add.png"))); //$NON-NLS-1$
		btnLoadProfile.setEnabled(false);
		btnLoadProfile.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				chooseProfile();
			}
		});
		profilesBtnPanel.add(btnLoadProfile);

		btnImportDat = new JButton(Messages.getString("MainFrame.btnImportDat.text")); //$NON-NLS-1$
		btnImportDat.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/script_go.png"))); //$NON-NLS-1$
		btnImportDat.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				importDat();
			}
		});
		profilesBtnPanel.add(btnImportDat);

		scannerTab = new JPanel();
		mainPane.addTab(Messages.getString("MainFrame.Scanner"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/drive_magnify.png")), scannerTab, null); //$NON-NLS-1$ //$NON-NLS-2$
		mainPane.setEnabledAt(1, false);
		GridBagLayout gbl_scannerTab = new GridBagLayout();
		gbl_scannerTab.columnWidths = new int[] { 104, 0 };
		gbl_scannerTab.rowHeights = new int[] { 0, 33, 24, 0 };
		gbl_scannerTab.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_scannerTab.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		scannerTab.setLayout(gbl_scannerTab);

		scannerBtnPanel = new JPanel();
		GridBagConstraints gbc_scannerBtnPanel = new GridBagConstraints();
		gbc_scannerBtnPanel.insets = new Insets(0, 0, 5, 0);
		gbc_scannerBtnPanel.fill = GridBagConstraints.BOTH;
		gbc_scannerBtnPanel.gridx = 0;
		gbc_scannerBtnPanel.gridy = 0;
		scannerTab.add(scannerBtnPanel, gbc_scannerBtnPanel);

		btnScan = new JButton(Messages.getString("MainFrame.btnScan.text")); //$NON-NLS-1$
		btnScan.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/magnifier.png"))); //$NON-NLS-1$
		scannerBtnPanel.add(btnScan);
		btnScan.setEnabled(false);
		
		btnReport = new JButton(Messages.getString("MainFrame.btnReport.text")); //$NON-NLS-1$
		btnReport.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/report.png")));
		btnReport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						report_frame.setVisible(true);
					}
				});
			}
		});
		scannerBtnPanel.add(btnReport);

		btnFix = new JButton(Messages.getString("MainFrame.btnFix.text")); //$NON-NLS-1$
		btnFix.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/tick.png"))); //$NON-NLS-1$
		scannerBtnPanel.add(btnFix);
		btnFix.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fix();
			}
		});
		btnFix.setEnabled(false);
		btnScan.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				scan();
			}
		});

		scannerSettingsPanel = new JPanel();
		scannerSettingsPanel.setBackground(UIManager.getColor("Panel.background")); //$NON-NLS-1$
		scannerSettingsPanel.setBorder(new TitledBorder(null, Messages.getString("MainFrame.scannerSettingsPanel.borderTitle"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		GridBagConstraints gbc_scannerSettingsPanel = new GridBagConstraints();
		gbc_scannerSettingsPanel.ipady = 20;
		gbc_scannerSettingsPanel.insets = new Insets(0, 5, 5, 0);
		gbc_scannerSettingsPanel.fill = GridBagConstraints.BOTH;
		gbc_scannerSettingsPanel.gridx = 0;
		gbc_scannerSettingsPanel.gridy = 1;
		scannerTab.add(scannerSettingsPanel, gbc_scannerSettingsPanel);
		GridBagLayout gbl_scannerSettingsPanel = new GridBagLayout();
		gbl_scannerSettingsPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_scannerSettingsPanel.rowHeights = new int[] { 20, 20, 20, 0 };
		gbl_scannerSettingsPanel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_scannerSettingsPanel.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		scannerSettingsPanel.setLayout(gbl_scannerSettingsPanel);

		chckbxNeedSHA1 = new JCheckBox(Messages.getString("MainFrame.chckbxNeedSHA1.text")); //$NON-NLS-1$
		chckbxNeedSHA1.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				curr_profile.setProperty("need_sha1_or_md5", e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
			}
		});
		chckbxNeedSHA1.setToolTipText(Messages.getString("MainFrame.chckbxNeedSHA1.toolTipText")); //$NON-NLS-1$
		GridBagConstraints gbc_chckbxNeedSHA1 = new GridBagConstraints();
		gbc_chckbxNeedSHA1.fill = GridBagConstraints.BOTH;
		gbc_chckbxNeedSHA1.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNeedSHA1.gridx = 0;
		gbc_chckbxNeedSHA1.gridy = 0;
		scannerSettingsPanel.add(chckbxNeedSHA1, gbc_chckbxNeedSHA1);

		chckbxUseParallelism = new JCheckBox(Messages.getString("MainFrame.chckbxUseParallelism.text")); //$NON-NLS-1$
		chckbxUseParallelism.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				curr_profile.setProperty("use_parallelism", e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
			}
		});

		chckbxCreateMissingSets = new JCheckBox(Messages.getString("MainFrame.chckbxCreateMissingSets.text")); //$NON-NLS-1$
		chckbxCreateMissingSets.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				curr_profile.setProperty("create_mode", e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
				if(e.getStateChange() != ItemEvent.SELECTED)
					chckbxCreateOnlyComplete.setSelected(false);
				chckbxCreateOnlyComplete.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		GridBagConstraints gbc_chckbxCreateMissingSets = new GridBagConstraints();
		gbc_chckbxCreateMissingSets.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxCreateMissingSets.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxCreateMissingSets.gridx = 1;
		gbc_chckbxCreateMissingSets.gridy = 0;
		scannerSettingsPanel.add(chckbxCreateMissingSets, gbc_chckbxCreateMissingSets);
		chckbxUseParallelism.setToolTipText(Messages.getString("MainFrame.chckbxUseParallelism.toolTipText")); //$NON-NLS-1$
		GridBagConstraints gbc_chckbxUseParallelism = new GridBagConstraints();
		gbc_chckbxUseParallelism.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxUseParallelism.fill = GridBagConstraints.BOTH;
		gbc_chckbxUseParallelism.gridx = 0;
		gbc_chckbxUseParallelism.gridy = 1;
		scannerSettingsPanel.add(chckbxUseParallelism, gbc_chckbxUseParallelism);

		chckbxCreateOnlyComplete = new JCheckBox(Messages.getString("MainFrame.chckbxCreateOnlyComplete.text")); //$NON-NLS-1$
		chckbxCreateOnlyComplete.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				curr_profile.setProperty("createfull_mode", e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
			}
		});
		GridBagConstraints gbc_chckbxCreateOnlyComplete = new GridBagConstraints();
		gbc_chckbxCreateOnlyComplete.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxCreateOnlyComplete.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxCreateOnlyComplete.gridx = 1;
		gbc_chckbxCreateOnlyComplete.gridy = 1;
		scannerSettingsPanel.add(chckbxCreateOnlyComplete, gbc_chckbxCreateOnlyComplete);

		scannerSubSettingsPanel = new JPanel();
		GridBagConstraints gbc_scannerSubSettingsPanel = new GridBagConstraints();
		gbc_scannerSubSettingsPanel.gridwidth = 2;
		gbc_scannerSubSettingsPanel.fill = GridBagConstraints.BOTH;
		gbc_scannerSubSettingsPanel.gridx = 0;
		gbc_scannerSubSettingsPanel.gridy = 2;
		scannerSettingsPanel.add(scannerSubSettingsPanel, gbc_scannerSubSettingsPanel);
		GridBagLayout gbl_scannerSubSettingsPanel = new GridBagLayout();
		gbl_scannerSubSettingsPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_scannerSubSettingsPanel.rowHeights = new int[] { 0, 0, 0, 8, 100, 0 };
		gbl_scannerSubSettingsPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_scannerSubSettingsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		scannerSubSettingsPanel.setLayout(gbl_scannerSubSettingsPanel);

		lblCompression = new JLabel(Messages.getString("MainFrame.lblCompression.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblCompression = new GridBagConstraints();
		gbc_lblCompression.anchor = GridBagConstraints.EAST;
		gbc_lblCompression.insets = new Insets(0, 5, 5, 5);
		gbc_lblCompression.gridx = 0;
		gbc_lblCompression.gridy = 0;
		scannerSubSettingsPanel.add(lblCompression, gbc_lblCompression);

		cbCompression = new JComboBox<FormatOptions>();
		cbCompression.setModel(new DefaultComboBoxModel<FormatOptions>(FormatOptions.values()));
		cbCompression.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				setText(((FormatOptions) value).getDesc());
				return this;
			}
		});
		cbCompression.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				curr_profile.settings.setProperty("format", cbCompression.getSelectedItem().toString()); //$NON-NLS-1$
			}
		});
		GridBagConstraints gbc_cbCompression = new GridBagConstraints();
		gbc_cbCompression.gridwidth = 2;
		gbc_cbCompression.insets = new Insets(0, 0, 5, 5);
		gbc_cbCompression.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbCompression.gridx = 1;
		gbc_cbCompression.gridy = 0;
		scannerSubSettingsPanel.add(cbCompression, gbc_cbCompression);

		lblMergeMode = new JLabel(Messages.getString("MainFrame.lblMergeMode.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblMergeMode = new GridBagConstraints();
		gbc_lblMergeMode.insets = new Insets(0, 0, 5, 5);
		gbc_lblMergeMode.anchor = GridBagConstraints.EAST;
		gbc_lblMergeMode.gridx = 0;
		gbc_lblMergeMode.gridy = 1;
		scannerSubSettingsPanel.add(lblMergeMode, gbc_lblMergeMode);

		cbbxMergeMode = new JComboBox<>();
		GridBagConstraints gbc_cbbxMergeMode = new GridBagConstraints();
		gbc_cbbxMergeMode.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxMergeMode.gridwidth = 2;
		gbc_cbbxMergeMode.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxMergeMode.gridx = 1;
		gbc_cbbxMergeMode.gridy = 1;
		scannerSubSettingsPanel.add(cbbxMergeMode, gbc_cbbxMergeMode);
		cbbxMergeMode.setToolTipText(Messages.getString("MainFrame.cbbxMergeMode.toolTipText")); //$NON-NLS-1$
		cbbxMergeMode.setModel(new DefaultComboBoxModel<MergeOptions>(MergeOptions.values()));
		cbbxMergeMode.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				setText(((MergeOptions) value).getDesc());
				return this;
			}
		});
		cbbxMergeMode.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				curr_profile.settings.setProperty("merge_mode", cbbxMergeMode.getSelectedItem().toString()); //$NON-NLS-1$
				cbHashCollision.setEnabled(((MergeOptions) cbbxMergeMode.getSelectedItem()).isMerge());
			}
		});

		lblHashCollision = new JLabel(Messages.getString("MainFrame.lblHashCollision.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblHashCollision = new GridBagConstraints();
		gbc_lblHashCollision.insets = new Insets(0, 0, 5, 5);
		gbc_lblHashCollision.anchor = GridBagConstraints.EAST;
		gbc_lblHashCollision.gridx = 0;
		gbc_lblHashCollision.gridy = 2;
		scannerSubSettingsPanel.add(lblHashCollision, gbc_lblHashCollision);

		cbHashCollision = new JComboBox<HashCollisionOptions>();
		cbHashCollision.setModel(new DefaultComboBoxModel<HashCollisionOptions>(HashCollisionOptions.values()));
		cbHashCollision.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				setText(((HashCollisionOptions) value).getDesc());
				return this;
			}
		});
		cbHashCollision.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				curr_profile.settings.setProperty("hash_collision_mode", cbHashCollision.getSelectedItem().toString()); //$NON-NLS-1$
			}
		});
		GridBagConstraints gbc_cbHashCollision = new GridBagConstraints();
		gbc_cbHashCollision.gridwidth = 2;
		gbc_cbHashCollision.insets = new Insets(0, 0, 5, 5);
		gbc_cbHashCollision.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbHashCollision.gridx = 1;
		gbc_cbHashCollision.gridy = 2;
		scannerSubSettingsPanel.add(cbHashCollision, gbc_cbHashCollision);

		lblRomsDest = new JLabel(Messages.getString("MainFrame.lblRomsDest.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblRomsDest = new GridBagConstraints();
		gbc_lblRomsDest.anchor = GridBagConstraints.EAST;
		gbc_lblRomsDest.fill = GridBagConstraints.VERTICAL;
		gbc_lblRomsDest.insets = new Insets(0, 0, 5, 5);
		gbc_lblRomsDest.gridx = 0;
		gbc_lblRomsDest.gridy = 3;
		scannerSubSettingsPanel.add(lblRomsDest, gbc_lblRomsDest);

		txtRomsDest = new JTextField();
		txtRomsDest.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				curr_profile.setProperty("roms_dest_dir", txtRomsDest.getText()); //$NON-NLS-1$
			}
		});
		GridBagConstraints gbc_txtRomsDest = new GridBagConstraints();
		gbc_txtRomsDest.insets = new Insets(0, 0, 5, 0);
		gbc_txtRomsDest.fill = GridBagConstraints.BOTH;
		gbc_txtRomsDest.gridx = 1;
		gbc_txtRomsDest.gridy = 3;
		scannerSubSettingsPanel.add(txtRomsDest, gbc_txtRomsDest);
		txtRomsDest.setColumns(10);

		btnRomsDest = new JButton(""); //$NON-NLS-1$
		btnRomsDest.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		btnRomsDest.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new JFileChooser()
				{
					{
						File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
						setCurrentDirectory(workdir);
						setFileSelectionMode(DIRECTORIES_ONLY);
						setSelectedFile(new File(txtRomsDest.getText()));
						setDialogTitle(Messages.getString("MainFrame.ChooseRomsDestination")); //$NON-NLS-1$
						if(showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
						{
							txtRomsDest.setText(getSelectedFile().getAbsolutePath());
							curr_profile.setProperty("roms_dest_dir", txtRomsDest.getText()); //$NON-NLS-1$
						}
					}
				};
			}
		});
		GridBagConstraints gbc_btnRomsDest = new GridBagConstraints();
		gbc_btnRomsDest.fill = GridBagConstraints.VERTICAL;
		gbc_btnRomsDest.insets = new Insets(0, 0, 5, 5);
		gbc_btnRomsDest.gridx = 2;
		gbc_btnRomsDest.gridy = 3;
		scannerSubSettingsPanel.add(btnRomsDest, gbc_btnRomsDest);

		lblSrcDir = new JLabel(Messages.getString("MainFrame.lblSrcDir.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblSrcDir = new GridBagConstraints();
		gbc_lblSrcDir.insets = new Insets(0, 0, 0, 5);
		gbc_lblSrcDir.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblSrcDir.gridx = 0;
		gbc_lblSrcDir.gridy = 4;
		scannerSubSettingsPanel.add(lblSrcDir, gbc_lblSrcDir);

		listSrcDir = new JList<>();
		DefaultListModel<File> modelSrcDir = new DefaultListModel<File>();
		listSrcDir.setModel(modelSrcDir);
		new DropTarget(listSrcDir, new DropTargetListener()
		{

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde)
			{
			}

			@SuppressWarnings("unchecked")
			@Override
			public void drop(DropTargetDropEvent dtde)
			{
				try
				{
					Transferable transferable = dtde.getTransferable();

					if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
					{
						dtde.acceptDrop(DnDConstants.ACTION_COPY);

						List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
						for(File file : files)
							modelSrcDir.addElement(file);
						String joined = String.join("|", Collections.list(modelSrcDir.elements()).stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList())); //$NON-NLS-1$
						curr_profile.setProperty("src_dir", joined); //$NON-NLS-1$

						dtde.getDropTargetContext().dropComplete(true);
					}
					else
						dtde.rejectDrop();
				}
				catch(UnsupportedFlavorException e)
				{
					dtde.rejectDrop();
				}
				catch(Exception e)
				{
					dtde.rejectDrop();
				}
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde)
			{
			}

			@Override
			public void dragExit(DropTargetEvent dte)
			{
			}

			@Override
			public void dragEnter(DropTargetDragEvent dtde)
			{
				dtde.acceptDrag(DnDConstants.ACTION_COPY);
			}
		});
		listSrcDir.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		popupMenu = new JPopupMenu();
		popupMenu.addPopupMenuListener(new PopupMenuListener()
		{
			public void popupMenuCanceled(PopupMenuEvent e)
			{
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				mntmDeleteSelected.setEnabled(listSrcDir.getSelectedValuesList().size() > 0);
			}
		});
		addPopup(listSrcDir, popupMenu);

		mntmDeleteSelected = new JMenuItem(Messages.getString("MainFrame.mntmDeleteSelected.text")); //$NON-NLS-1$
		mntmDeleteSelected.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				List<File> files = listSrcDir.getSelectedValuesList();
				for(File file : files)
					modelSrcDir.removeElement(file);
				curr_profile.setProperty("src_dir", String.join("|", Collections.list(modelSrcDir.elements()).stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList()))); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		mntmDeleteSelected.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/folder_delete.png"))); //$NON-NLS-1$
		popupMenu.add(mntmDeleteSelected);

		mntmAddDirectory = new JMenuItem(Messages.getString("MainFrame.mntmAddDirectory.text")); //$NON-NLS-1$
		mntmAddDirectory.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new JFileChooser()
				{
					{
						File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
						setCurrentDirectory(workdir);
						setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						setMultiSelectionEnabled(true);
						if(showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
						{
							for(File f : getSelectedFiles())
								modelSrcDir.addElement(f);
							curr_profile.setProperty("src_dir", String.join("|", Collections.list(modelSrcDir.elements()).stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList()))); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				};
			}
		});
		mntmAddDirectory.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/folder_add.png"))); //$NON-NLS-1$
		popupMenu.add(mntmAddDirectory);
		GridBagConstraints gbc_listSrcDir = new GridBagConstraints();
		gbc_listSrcDir.insets = new Insets(0, 0, 5, 5);
		gbc_listSrcDir.anchor = GridBagConstraints.NORTH;
		gbc_listSrcDir.gridwidth = 2;
		gbc_listSrcDir.fill = GridBagConstraints.BOTH;
		gbc_listSrcDir.gridx = 1;
		gbc_listSrcDir.gridy = 4;
		scannerSubSettingsPanel.add(listSrcDir, gbc_listSrcDir);
		
		lblProfileinfo = new JLabel(""); //$NON-NLS-1$
		lblProfileinfo.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_lblProfileinfo = new GridBagConstraints();
		gbc_lblProfileinfo.insets = new Insets(0, 2, 2, 2);
		gbc_lblProfileinfo.fill = GridBagConstraints.BOTH;
		gbc_lblProfileinfo.gridx = 0;
		gbc_lblProfileinfo.gridy = 2;
		scannerTab.add(lblProfileinfo, gbc_lblProfileinfo);

		settingsTab = new JPanel();
		mainPane.addTab(Messages.getString("MainFrame.Settings"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/cog.png")), settingsTab, null); //$NON-NLS-1$ //$NON-NLS-2$
		settingsTab.setLayout(new BorderLayout(0, 0));

		settingsPane = new JTabbedPane(JTabbedPane.TOP);
		settingsTab.add(settingsPane);

		compressors = new JPanel();
		settingsPane.addTab(Messages.getString("MainFrame.Compressors"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/compress.png")), compressors, null); //$NON-NLS-1$ //$NON-NLS-2$
		settingsPane.setEnabledAt(0, true);
		compressors.setLayout(new BorderLayout(0, 0));

		compressorsPane = new JTabbedPane(JTabbedPane.TOP);
		compressors.add(compressorsPane);

		panelZip = new JPanel();
		compressorsPane.addTab("Zip", null, panelZip, null); //$NON-NLS-1$
		panelZip.setLayout(new GridLayout(0, 1, 0, 0));

		chkbxZipUseTemp = new JCheckBox(Messages.getString("MainFrame.chkbxZipUseTemp.text")); //$NON-NLS-1$
		chkbxZipUseTemp.setHorizontalAlignment(SwingConstants.CENTER);
		chkbxZipUseTemp.setSelected(true);
		panelZip.add(chkbxZipUseTemp);

		lblZipWarn = new JLabel();
		lblZipWarn.setVerticalAlignment(SwingConstants.TOP);
		lblZipWarn.setHorizontalAlignment(SwingConstants.CENTER);
		lblZipWarn.setBackground(UIManager.getColor("Panel.background")); //$NON-NLS-1$
		lblZipWarn.setText(Messages.getString("MainFrame.lblZipWarn.text")); //$NON-NLS-1$
		lblZipWarn.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11)); //$NON-NLS-1$
		panelZip.add(lblZipWarn);

		panelZipE = new JPanel();
		compressorsPane.addTab(Messages.getString("MainFrame.ZipExternal"), null, panelZipE, null); //$NON-NLS-1$
		GridBagLayout gbl_panelZipE = new GridBagLayout();
		gbl_panelZipE.columnWidths = new int[] { 85, 246, 40, 0 };
		gbl_panelZipE.rowHeights = new int[] { 0, 28, 28, 28, 0, 0 };
		gbl_panelZipE.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panelZipE.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelZipE.setLayout(gbl_panelZipE);

		lblZipECmd = new JLabel(Messages.getString("MainFrame.lblZipECmd.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblZipECmd = new GridBagConstraints();
		gbc_lblZipECmd.anchor = GridBagConstraints.EAST;
		gbc_lblZipECmd.insets = new Insets(5, 5, 5, 5);
		gbc_lblZipECmd.gridx = 0;
		gbc_lblZipECmd.gridy = 1;
		panelZipE.add(lblZipECmd, gbc_lblZipECmd);

		tfZipECmd = new JTextField();
		tfZipECmd.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				Settings.setProperty("zip_cmd", tfZipECmd.getText()); //$NON-NLS-1$
			}
		});
		tfZipECmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				Settings.setProperty("zip_cmd", tfZipECmd.getText()); //$NON-NLS-1$
			}
		});
		tfZipECmd.setText(Settings.getProperty("zip_cmd", FindCmd.find7z())); //$NON-NLS-1$
		GridBagConstraints gbc_tfZipECmd = new GridBagConstraints();
		gbc_tfZipECmd.insets = new Insets(0, 0, 5, 0);
		gbc_tfZipECmd.fill = GridBagConstraints.BOTH;
		gbc_tfZipECmd.gridx = 1;
		gbc_tfZipECmd.gridy = 1;
		panelZipE.add(tfZipECmd, gbc_tfZipECmd);
		tfZipECmd.setColumns(30);

		btZipECmd = new JButton(""); //$NON-NLS-1$
		btZipECmd.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		GridBagConstraints gbc_btZipECmd = new GridBagConstraints();
		gbc_btZipECmd.fill = GridBagConstraints.BOTH;
		gbc_btZipECmd.insets = new Insets(0, 0, 5, 5);
		gbc_btZipECmd.gridx = 2;
		gbc_btZipECmd.gridy = 1;
		panelZipE.add(btZipECmd, gbc_btZipECmd);

		lblZipEArgs = new JLabel(Messages.getString("MainFrame.lblZipEArgs.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblZipEArgs = new GridBagConstraints();
		gbc_lblZipEArgs.anchor = GridBagConstraints.EAST;
		gbc_lblZipEArgs.insets = new Insets(0, 5, 5, 5);
		gbc_lblZipEArgs.gridx = 0;
		gbc_lblZipEArgs.gridy = 2;
		panelZipE.add(lblZipEArgs, gbc_lblZipEArgs);

		cbZipEArgs = new JComboBox<>();
		cbZipEArgs.setEditable(false);
		cbZipEArgs.setModel(new DefaultComboBoxModel<ZipOptions>(ZipOptions.values()));
		cbZipEArgs.setRenderer(new DefaultListCellRenderer()
		{

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				setText(((ZipOptions) value).getName());
				return this;
			}
		});
		cbZipEArgs.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				Settings.setProperty("zip_level", cbZipEArgs.getSelectedItem().toString()); //$NON-NLS-1$
			}
		});
		cbZipEArgs.setSelectedItem(ZipOptions.valueOf(Settings.getProperty("zip_level", ZipOptions.NORMAL.toString()))); //$NON-NLS-1$
		GridBagConstraints gbc_cbZipEArgs = new GridBagConstraints();
		gbc_cbZipEArgs.insets = new Insets(0, 0, 5, 5);
		gbc_cbZipEArgs.gridwidth = 2;
		gbc_cbZipEArgs.fill = GridBagConstraints.BOTH;
		gbc_cbZipEArgs.gridx = 1;
		gbc_cbZipEArgs.gridy = 2;
		panelZipE.add(cbZipEArgs, gbc_cbZipEArgs);

		lblZipEThreads = new JLabel(Messages.getString("MainFrame.lblZipEThreads.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblZipEThreads = new GridBagConstraints();
		gbc_lblZipEThreads.insets = new Insets(0, 0, 5, 5);
		gbc_lblZipEThreads.anchor = GridBagConstraints.EAST;
		gbc_lblZipEThreads.gridx = 0;
		gbc_lblZipEThreads.gridy = 3;
		panelZipE.add(lblZipEThreads, gbc_lblZipEThreads);

		tfZipEThreads = new JTextField();
		tfZipEThreads.setText("1"); //$NON-NLS-1$
		GridBagConstraints gbc_tfZipEThreads = new GridBagConstraints();
		gbc_tfZipEThreads.fill = GridBagConstraints.VERTICAL;
		gbc_tfZipEThreads.anchor = GridBagConstraints.WEST;
		gbc_tfZipEThreads.insets = new Insets(0, 0, 5, 5);
		gbc_tfZipEThreads.gridx = 1;
		gbc_tfZipEThreads.gridy = 3;
		panelZipE.add(tfZipEThreads, gbc_tfZipEThreads);
		tfZipEThreads.setColumns(4);

		lblZipEWarning = new JLabel();
		lblZipEWarning.setVerticalAlignment(SwingConstants.TOP);
		lblZipEWarning.setText(Messages.getString("MainFrame.lblZipEWarning.text")); //$NON-NLS-1$
		lblZipEWarning.setHorizontalAlignment(SwingConstants.CENTER);
		lblZipEWarning.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11)); //$NON-NLS-1$
		lblZipEWarning.setBackground(UIManager.getColor("Button.background")); //$NON-NLS-1$
		GridBagConstraints gbc_lblZipEWarning = new GridBagConstraints();
		gbc_lblZipEWarning.gridwidth = 3;
		gbc_lblZipEWarning.gridx = 0;
		gbc_lblZipEWarning.gridy = 4;
		panelZipE.add(lblZipEWarning, gbc_lblZipEWarning);

		panel7Zip = new JPanel();
		compressorsPane.addTab(Messages.getString("MainFrame.7zExternal"), null, panel7Zip, null); //$NON-NLS-1$
		compressorsPane.setEnabledAt(2, true);
		GridBagLayout gbl_panel7Zip = new GridBagLayout();
		gbl_panel7Zip.columnWidths = new int[] { 85, 123, 0, 40, 0 };
		gbl_panel7Zip.rowHeights = new int[] { 0, 28, 28, 28, 0, 0 };
		gbl_panel7Zip.columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel7Zip.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panel7Zip.setLayout(gbl_panel7Zip);

		lbl7zCmd = new JLabel(Messages.getString("MainFrame.lbl7zCmd.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lbl7zCmd = new GridBagConstraints();
		gbc_lbl7zCmd.anchor = GridBagConstraints.EAST;
		gbc_lbl7zCmd.insets = new Insets(5, 5, 5, 5);
		gbc_lbl7zCmd.gridx = 0;
		gbc_lbl7zCmd.gridy = 1;
		panel7Zip.add(lbl7zCmd, gbc_lbl7zCmd);

		tf7zCmd = new JTextField();
		tf7zCmd.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				Settings.setProperty("7z_cmd", tf7zCmd.getText()); //$NON-NLS-1$
			}
		});
		tf7zCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Settings.setProperty("7z_cmd", tf7zCmd.getText()); //$NON-NLS-1$
			}
		});
		tf7zCmd.setText(Settings.getProperty("7z_cmd", FindCmd.find7z())); //$NON-NLS-1$
		tf7zCmd.setColumns(30);
		GridBagConstraints gbc_tf7zCmd = new GridBagConstraints();
		gbc_tf7zCmd.gridwidth = 2;
		gbc_tf7zCmd.fill = GridBagConstraints.BOTH;
		gbc_tf7zCmd.insets = new Insets(0, 0, 5, 0);
		gbc_tf7zCmd.gridx = 1;
		gbc_tf7zCmd.gridy = 1;
		panel7Zip.add(tf7zCmd, gbc_tf7zCmd);

		btn7zCmd = new JButton(""); //$NON-NLS-1$
		btn7zCmd.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		GridBagConstraints gbc_btn7zCmd = new GridBagConstraints();
		gbc_btn7zCmd.fill = GridBagConstraints.BOTH;
		gbc_btn7zCmd.insets = new Insets(0, 0, 5, 5);
		gbc_btn7zCmd.gridx = 3;
		gbc_btn7zCmd.gridy = 1;
		panel7Zip.add(btn7zCmd, gbc_btn7zCmd);

		lbl7zArgs = new JLabel(Messages.getString("MainFrame.lbl7zArgs.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lbl7zArgs = new GridBagConstraints();
		gbc_lbl7zArgs.anchor = GridBagConstraints.EAST;
		gbc_lbl7zArgs.insets = new Insets(0, 5, 5, 5);
		gbc_lbl7zArgs.gridx = 0;
		gbc_lbl7zArgs.gridy = 2;
		panel7Zip.add(lbl7zArgs, gbc_lbl7zArgs);

		cb7zArgs = new JComboBox<SevenZipOptions>();
		cb7zArgs.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				Settings.setProperty("7z_level", cb7zArgs.getSelectedItem().toString()); //$NON-NLS-1$
			}
		});
		cb7zArgs.setEditable(false);
		cb7zArgs.setModel(new DefaultComboBoxModel<SevenZipOptions>(SevenZipOptions.values()));
		cb7zArgs.setRenderer(new DefaultListCellRenderer()
		{

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				setText(((SevenZipOptions) value).getName());
				return this;
			}
		});
		cb7zArgs.setSelectedItem(SevenZipOptions.valueOf(Settings.getProperty("7z_level", SevenZipOptions.NORMAL.toString()))); //$NON-NLS-1$
		GridBagConstraints gbc_cb7zArgs = new GridBagConstraints();
		gbc_cb7zArgs.fill = GridBagConstraints.BOTH;
		gbc_cb7zArgs.gridwidth = 3;
		gbc_cb7zArgs.insets = new Insets(0, 0, 5, 5);
		gbc_cb7zArgs.gridx = 1;
		gbc_cb7zArgs.gridy = 2;
		panel7Zip.add(cb7zArgs, gbc_cb7zArgs);

		lbl7zThreads = new JLabel(Messages.getString("MainFrame.lbl7zThreads.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lbl7zThreads = new GridBagConstraints();
		gbc_lbl7zThreads.insets = new Insets(0, 0, 5, 5);
		gbc_lbl7zThreads.anchor = GridBagConstraints.EAST;
		gbc_lbl7zThreads.gridx = 0;
		gbc_lbl7zThreads.gridy = 3;
		panel7Zip.add(lbl7zThreads, gbc_lbl7zThreads);

		tf7zThreads = new JTextField();
		tf7zThreads.setText(Integer.toString(Settings.getProperty("7z_threads", -1))); //$NON-NLS-1$
		tf7zThreads.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				Settings.setProperty("7z_threads", tf7zThreads.getText()); //$NON-NLS-1$
			}
		});
		tf7zThreads.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				Settings.setProperty("7z_threads", tf7zThreads.getText()); //$NON-NLS-1$
			}
		});
		GridBagConstraints gbc_tf7zThreads = new GridBagConstraints();
		gbc_tf7zThreads.fill = GridBagConstraints.VERTICAL;
		gbc_tf7zThreads.anchor = GridBagConstraints.WEST;
		gbc_tf7zThreads.insets = new Insets(0, 0, 5, 5);
		gbc_tf7zThreads.gridx = 1;
		gbc_tf7zThreads.gridy = 3;
		panel7Zip.add(tf7zThreads, gbc_tf7zThreads);
		tf7zThreads.setColumns(4);

		ckbx7zSolid = new JCheckBox(Messages.getString("MainFrame.ckbx7zSolid.text")); //$NON-NLS-1$
		ckbx7zSolid.setSelected(Settings.getProperty("7z_solid", true)); //$NON-NLS-1$
		cb7zArgs.setEnabled(ckbx7zSolid.isSelected());
		ckbx7zSolid.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				cb7zArgs.setEnabled(ckbx7zSolid.isSelected());
				Settings.setProperty("7z_solid", ckbx7zSolid.isSelected()); //$NON-NLS-1$
			}
		});
		GridBagConstraints gbc_ckbx7zSolid = new GridBagConstraints();
		gbc_ckbx7zSolid.insets = new Insets(0, 0, 5, 5);
		gbc_ckbx7zSolid.gridx = 2;
		gbc_ckbx7zSolid.gridy = 3;
		panel7Zip.add(ckbx7zSolid, gbc_ckbx7zSolid);

		lbl7zWarning = new JLabel();
		lbl7zWarning.setVerticalAlignment(SwingConstants.TOP);
		lbl7zWarning.setText(Messages.getString("MainFrame.lbl7zWarning.text")); //$NON-NLS-1$
		lbl7zWarning.setHorizontalAlignment(SwingConstants.CENTER);
		lbl7zWarning.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11)); //$NON-NLS-1$
		lbl7zWarning.setBackground(UIManager.getColor("Button.background")); //$NON-NLS-1$
		GridBagConstraints gbc_lbl7zWarning = new GridBagConstraints();
		gbc_lbl7zWarning.gridwidth = 4;
		gbc_lbl7zWarning.gridx = 0;
		gbc_lbl7zWarning.gridy = 4;
		panel7Zip.add(lbl7zWarning, gbc_lbl7zWarning);

		panelTZip = new JPanel();
		compressorsPane.addTab(Messages.getString("MainFrame.TorrentZipExternal"), null, panelTZip, null); //$NON-NLS-1$
		GridBagLayout gbl_panelTZip = new GridBagLayout();
		gbl_panelTZip.columnWidths = new int[] { 100, 334, 34, 0 };
		gbl_panelTZip.rowHeights = new int[] { 0, 20, 0, 0 };
		gbl_panelTZip.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelTZip.rowWeights = new double[] { 1.0, 0.0, 1.0, Double.MIN_VALUE };
		panelTZip.setLayout(gbl_panelTZip);

		lblTZipCmd = new JLabel(Messages.getString("MainFrame.lblTZipCmd.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblTZipCmd = new GridBagConstraints();
		gbc_lblTZipCmd.anchor = GridBagConstraints.WEST;
		gbc_lblTZipCmd.insets = new Insets(0, 5, 5, 5);
		gbc_lblTZipCmd.gridx = 0;
		gbc_lblTZipCmd.gridy = 1;
		panelTZip.add(lblTZipCmd, gbc_lblTZipCmd);

		tfTZipCmd = new JTextField();
		tfTZipCmd.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				Settings.setProperty("tzip_cmd", tfTZipCmd.getText()); //$NON-NLS-1$
			}
		});
		tfTZipCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Settings.setProperty("tzip_cmd", tfTZipCmd.getText()); //$NON-NLS-1$
			}
		});
		tfTZipCmd.setHorizontalAlignment(SwingConstants.LEFT);
		tfTZipCmd.setText(Settings.getProperty("tzip_cmd", FindCmd.findTZip())); //$NON-NLS-1$
		tfTZipCmd.setColumns(30);
		GridBagConstraints gbc_tfTZipCmd = new GridBagConstraints();
		gbc_tfTZipCmd.fill = GridBagConstraints.BOTH;
		gbc_tfTZipCmd.insets = new Insets(0, 0, 5, 0);
		gbc_tfTZipCmd.gridx = 1;
		gbc_tfTZipCmd.gridy = 1;
		panelTZip.add(tfTZipCmd, gbc_tfTZipCmd);

		btTZipCmd = new JButton(""); //$NON-NLS-1$
		btTZipCmd.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		GridBagConstraints gbc_btTZipCmd = new GridBagConstraints();
		gbc_btTZipCmd.insets = new Insets(0, 0, 5, 5);
		gbc_btTZipCmd.anchor = GridBagConstraints.WEST;
		gbc_btTZipCmd.gridx = 2;
		gbc_btTZipCmd.gridy = 1;
		panelTZip.add(btTZipCmd, gbc_btTZipCmd);

		debug = new JPanel();
		settingsPane.addTab(Messages.getString("MainFrame.Debug"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/bug.png")), debug, null); //$NON-NLS-1$ //$NON-NLS-2$
		GridBagLayout gbl_debug = new GridBagLayout();
		gbl_debug.columnWidths = new int[] { 100, 0, 0 };
		gbl_debug.rowHeights = new int[] { 0, 0 };
		gbl_debug.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_debug.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		debug.setLayout(gbl_debug);

		lblLogLevel = new JLabel(Messages.getString("MainFrame.lblLogLevel.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblLogLevel = new GridBagConstraints();
		gbc_lblLogLevel.anchor = GridBagConstraints.EAST;
		gbc_lblLogLevel.fill = GridBagConstraints.VERTICAL;
		gbc_lblLogLevel.insets = new Insets(0, 0, 0, 5);
		gbc_lblLogLevel.gridx = 0;
		gbc_lblLogLevel.gridy = 0;
		debug.add(lblLogLevel, gbc_lblLogLevel);

		cbLogLevel = new JComboBox<>();
		GridBagConstraints gbc_cbLogLevel = new GridBagConstraints();
		gbc_cbLogLevel.insets = new Insets(0, 0, 0, 5);
		gbc_cbLogLevel.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbLogLevel.gridx = 1;
		gbc_cbLogLevel.gridy = 0;
		debug.add(cbLogLevel, gbc_cbLogLevel);
		mainPane.setEnabledAt(2, true);

		pack();

		try
		{
			setBounds(SerializationUtils.deserialize(Hex.decodeHex(Settings.getProperty("MainFrame.Bounds", Hex.encodeHexString(SerializationUtils.serialize(new Rectangle(50,50,640,400)))))));
		}
		catch(DecoderException e1)
		{
			e1.printStackTrace();
		}
	}

	private void importDat()
	{
		List<FileFilter> filters = Arrays.asList(
			new FileNameExtensionFilter(Messages.getString("MainFrame.DatFile"), "dat", "xml"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			new FileFilter()
			{
				@Override
				public String getDescription()
				{
					return Messages.getString("MainFrame.MameExecutable"); //$NON-NLS-1$
				}
				
				@Override
				public boolean accept(File f)
				{
					return f.isDirectory() || FilenameUtils.isExtension(f.getName(), "exe") || f.canExecute();
				}
			}
		);
		new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY, null, null, filters, Messages.getString("MainFrame.ChooseExeOrDatToImport")) //$NON-NLS-1$
		.show(MainFrame.this, new JRMFileChooser.CallBack<Void>()
		{
			
			@Override
			public Void call(JRMFileChooser<Void> chooser)
			{
				final Progress progress = new Progress(MainFrame.this);
				SwingWorker<Import, Void> worker = new SwingWorker<Import, Void>()
				{
					@Override
					protected Import doInBackground() throws Exception
					{
						progress.setProgress(Messages.getString("MainFrame.ImportingFromMame"), -1); //$NON-NLS-1$
						return new Import(chooser.getSelectedFile());
					}
					
					@Override
					protected void done()
					{
						progress.dispose();
					}
				};
				worker.execute();
				progress.setVisible(true);
				try
				{
					Import imprt = worker.get();
					File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
					File xmldir = new File(workdir, "xmlfiles"); //$NON-NLS-1$
					new JRMFileChooser<>(JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY, xmldir, imprt.file, null, Messages.getString("MainFrame.ChooseFileName")) //$NON-NLS-1$
					.show(MainFrame.this, 
						new JRMFileChooser.CallBack<Object>()
						{
							@Override
							public Object call(JRMFileChooser<Object> chooser)
							{
								try
								{
									File file = chooser.getSelectedFile();
									File parent = file.getParentFile();
									FileUtils.copyFile(imprt.file, file);
									DirTreeModel model = (DirTreeModel)profilesTree.getModel();
									DirNode root = (DirNode)model.getRoot();
									DirNode theNode = root.find(parent);
									if(theNode!=null)
									{
										
										theNode.reload();
										model.reload(theNode);
										if((theNode = root.find(parent))!=null)
										{
											profilesTree.clearSelection();
											profilesTree.setSelectionPath(new TreePath(model.getPathToRoot(theNode)));
										}
										else
											System.err.println(Messages.getString("MainFrame.FinalNodeNotFound")); //$NON-NLS-1$
									}
									else
										System.err.println(Messages.getString("MainFrame.NodeNotFound")); //$NON-NLS-1$
								}
								catch(IOException e)
								{
									e.printStackTrace();
								}
								return null;
							}
						}
					);
				}
				catch(InterruptedException|ExecutionException e1)
				{
					e1.printStackTrace();
				}
				
				return null;
			}
		});

	}

	private void loadProfile(File profile)
	{
		if(curr_profile != null)
			curr_profile.saveSettings();
		final Progress progress = new Progress(MainFrame.this);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{
			boolean success = false;

			@Override
			protected Void doInBackground() throws Exception
			{
				success = (null != (curr_profile = Profile.load(profile, progress)));
				mainPane.setEnabledAt(1, success);
				btnScan.setEnabled(success);
				btnFix.setEnabled(false);
				lblProfileinfo.setText(curr_profile.getName());
				return null;
			}

			@Override
			protected void done()
			{
				progress.dispose();
				if(success && curr_profile != null)
				{
					initScanSettings();
					mainPane.setSelectedIndex(1);
				}
			}

		};
		worker.execute();
		progress.setVisible(true);
	}

	private void chooseProfile()
	{
		new JFileChooser()
		{
			{
				File workdir = Paths.get("./xmlfiles").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
				setCurrentDirectory(workdir);
				addChoosableFileFilter(new FileNameExtensionFilter(Messages.getString("MainFrame.DatFile"), "dat", "xml")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if(showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
				{
					loadProfile(getSelectedFile());
				}
			}
		};
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

		List<File> srcdirs = new ArrayList<>();
		for(int i = 0; i < listSrcDir.getModel().getSize(); i++)
		{
			File file = listSrcDir.getModel().getElementAt(i);
			if(file.isDirectory())
				srcdirs.add(file);
		}

		final Progress progress = new Progress(MainFrame.this);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				curr_scan = new Scan(curr_profile, dstdir, srcdirs, progress);
				AtomicInteger actions_todo = new AtomicInteger(0);
				curr_scan.actions.forEach(actions -> actions_todo.addAndGet(actions.size()));
				btnFix.setEnabled(actions_todo.get() > 0);
				return null;
			}

			@Override
			protected void done()
			{
				progress.dispose();
			}

		};
		worker.execute();
		progress.setVisible(true);
	}

	private void fix()
	{
		final Progress progress = new Progress(MainFrame.this);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				Fix fix = new Fix(curr_profile, curr_scan, progress);
				btnFix.setEnabled(fix.getActionsRemain() > 0);
				return null;
			}

			@Override
			protected void done()
			{
				progress.dispose();
			}

		};
		worker.execute();
		progress.setVisible(true);
	}

	public void initScanSettings()
	{
		chckbxNeedSHA1.setSelected(curr_profile.getProperty("need_sha1_or_md5", false)); //$NON-NLS-1$
		chckbxUseParallelism.setSelected(curr_profile.getProperty("use_parallelism", false)); //$NON-NLS-1$
		chckbxCreateMissingSets.setSelected(curr_profile.getProperty("create_mode", false)); //$NON-NLS-1$
		chckbxCreateOnlyComplete.setSelected(curr_profile.getProperty("createfull_mode", false) && chckbxCreateMissingSets.isSelected()); //$NON-NLS-1$
		chckbxCreateOnlyComplete.setEnabled(chckbxCreateMissingSets.isSelected());
		txtRomsDest.setText(curr_profile.getProperty("roms_dest_dir", "")); //$NON-NLS-1$ //$NON-NLS-2$
		((DefaultListModel<File>) listSrcDir.getModel()).removeAllElements();
		for(String s : curr_profile.getProperty("src_dir", "").split("\\|")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if(!s.isEmpty())
				((DefaultListModel<File>) listSrcDir.getModel()).addElement(new File(s));
		cbCompression.setSelectedItem(FormatOptions.valueOf(curr_profile.settings.getProperty("format", FormatOptions.ZIP.toString()))); //$NON-NLS-1$
		cbbxMergeMode.setSelectedItem(MergeOptions.valueOf(curr_profile.settings.getProperty("merge_mode", MergeOptions.SPLIT.toString()))); //$NON-NLS-1$
		cbHashCollision.setEnabled(((MergeOptions) cbbxMergeMode.getSelectedItem()).isMerge());
		cbHashCollision.setSelectedItem(HashCollisionOptions.valueOf(curr_profile.settings.getProperty("hash_collision_mode", HashCollisionOptions.SINGLEFILE.toString()))); //$NON-NLS-1$
	}

	private JPanel settingsTab;
	private JTabbedPane settingsPane;
	private JPanel compressors;
	private JTabbedPane compressorsPane;
	private JPanel panelZip;
	private JCheckBox chkbxZipUseTemp;
	private JPanel panelZipE;
	private JPanel panelTZip;
	private JLabel lblZipECmd;
	private JTextField tfZipECmd;
	private JButton btZipECmd;
	private JLabel lblZipEArgs;
	private JComboBox<ZipOptions> cbZipEArgs;
	private JLabel lblZipWarn;
	private JPanel panel7Zip;
	private JLabel lbl7zCmd;
	private JTextField tf7zCmd;
	private JButton btn7zCmd;
	private JLabel lbl7zArgs;
	private JComboBox<SevenZipOptions> cb7zArgs;
	private JLabel lblTZipCmd;
	private JTextField tfTZipCmd;
	private JButton btTZipCmd;
	private JLabel lblZipEWarning;
	private JLabel lbl7zWarning;
	private JLabel lblCompression;
	private JComboBox<FormatOptions> cbCompression;
	private JCheckBox chckbxCreateMissingSets;
	private JComboBox<HashCollisionOptions> cbHashCollision;
	private JLabel lblHashCollision;
	private JCheckBox ckbx7zSolid;
	private JTextField tf7zThreads;
	private JLabel lbl7zThreads;
	private JTextField tfZipEThreads;
	private JLabel lblZipEThreads;
	private JPanel debug;
	private JLabel lblLogLevel;
	private JComboBox<?> cbLogLevel;
	private JCheckBox chckbxCreateOnlyComplete;
	private JPopupMenu popupMenu;
	private JMenuItem mntmDeleteSelected;
	private JMenuItem mntmAddDirectory;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane_1;
	private JPopupMenu popupMenu_1;
	private JMenuItem mntmCreateFolder;
	private JMenuItem mntmDeleteFolder;
	private JPopupMenu popupMenu_2;
	private JMenuItem mntmDeleteProfile;
	private JLabel lblProfileinfo;
	private JMenuItem mntmRenameProfile;
	private JButton btnReport;

	private static void addPopup(Component component, final JPopupMenu popup)
	{
		component.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e)
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
