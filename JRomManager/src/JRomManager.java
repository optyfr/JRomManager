import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.FileUtils;

import jrm.compressors.SevenZipOptions;
import jrm.compressors.ZipOptions;
import jrm.misc.FindCmd;
import jrm.misc.Log;
import jrm.misc.Settings;
import jrm.profiler.Import;
import jrm.profiler.Profile;
import jrm.profiler.fix.Fix;
import jrm.profiler.scan.Scan;
import jrm.profiler.scan.options.FormatOptions;
import jrm.profiler.scan.options.HashCollisionOptions;
import jrm.profiler.scan.options.MergeOptions;
import jrm.ui.DirNode;
import jrm.ui.DirTreeCellEditor;
import jrm.ui.DirTreeCellRenderer;
import jrm.ui.DirTreeModel;
import jrm.ui.DirTreeSelectionListener;
import jrm.ui.FileTableModel;
import jrm.ui.Progress;

public class JRomManager
{

	private JFrame mainFrame;

	private Profile curr_profile;
	private Scan curr_scan;

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
			// UIManager.setLookAndFeel(getProperty("LookAndFeel", UIManager.getSystemLookAndFeelClassName()/* UIManager.getCrossPlatformLookAndFeelClassName()*/));
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
	@SuppressWarnings("serial")
	private void initialize()
	{
		mainFrame = new JFrame();
		mainFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(JRomManager.class.getResource("/jrm/resources/rom.png")));
		mainFrame.setTitle("JRomManager");
		mainFrame.setBounds(100, 100, 458, 313);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.getContentPane().setLayout(new BorderLayout(0, 0));

		mainPane = new JTabbedPane(JTabbedPane.TOP);
		mainFrame.getContentPane().add(mainPane);

		profilesTab = new JPanel();
		mainPane.addTab("Profiles", new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/script.png")), profilesTab, null);
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
						loadProfile((File) target.getModel().getValueAt(row, 0));
				}
				super.mouseClicked(e);
			}
		});

		scrollPane_1 = new JScrollPane();
		scrollPane_1.setMinimumSize(new Dimension(100, 22));
		profilesPanel.setLeftComponent(scrollPane_1);

		profilesTree = new JTree();
		scrollPane_1.setViewportView(profilesTree);
		DirTreeModel profilesTreeModel = new DirTreeModel(new DirNode(Paths.get("./xmlfiles").toAbsolutePath().normalize().toFile()));
		profilesTree.setModel(profilesTreeModel);
		profilesTree.setRootVisible(true);
		profilesTree.setShowsRootHandles(true);
		profilesTree.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		profilesTree.setEditable(true);
		DirTreeCellRenderer profilesTreeRenderer = new DirTreeCellRenderer();
		profilesTree.setCellRenderer(profilesTreeRenderer);
		profilesTree.setCellEditor(new DirTreeCellEditor(profilesTree, profilesTreeRenderer));

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

		mntmCreateFolder = new JMenuItem("Create folder");
		mntmCreateFolder.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				DirNode selectedNode = (DirNode) profilesTree.getLastSelectedPathComponent();
				if(selectedNode != null)
				{
					DirNode newnode = new DirNode(new DirNode.Dir(new File(selectedNode.dir.getFile(), "new folder")));
					selectedNode.add(newnode);
					profilesTreeModel.reload(selectedNode);
					TreePath path = new TreePath(newnode.getPath());
					profilesTree.setSelectionPath(path);
					profilesTree.startEditingAtPath(path);
				}
			}
		});
		mntmCreateFolder.setIcon(new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/folder_add.png")));
		popupMenu_1.add(mntmCreateFolder);

		mntmDeleteFolder = new JMenuItem("Delete folder");
		mntmDeleteFolder.setIcon(new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/folder_delete.png")));
		popupMenu_1.add(mntmDeleteFolder);
		profilesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		profilesTree.addTreeSelectionListener(new DirTreeSelectionListener(profilesList));

		profilesBtnPanel = new JPanel();
		GridBagConstraints gbc_profilesBtnPanel = new GridBagConstraints();
		gbc_profilesBtnPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_profilesBtnPanel.gridx = 0;
		gbc_profilesBtnPanel.gridy = 1;
		profilesTab.add(profilesBtnPanel, gbc_profilesBtnPanel);

		btnLoadProfile = new JButton("Load Profile");
		btnLoadProfile.setIcon(new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/add.png")));
		btnLoadProfile.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				chooseProfile();
			}
		});
		profilesBtnPanel.add(btnLoadProfile);

		btnImportDat = new JButton("Import Dat");
		btnImportDat.setIcon(new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/script_go.png")));
		btnImportDat.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				importDat();
			}
		});
		profilesBtnPanel.add(btnImportDat);

		scannerTab = new JPanel();
		mainPane.addTab("Scanner", new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/drive_magnify.png")), scannerTab, null);
		mainPane.setEnabledAt(1, false);
		GridBagLayout gbl_scannerTab = new GridBagLayout();
		gbl_scannerTab.columnWidths = new int[] { 104, 0 };
		gbl_scannerTab.rowHeights = new int[] { 0, 33, 0 };
		gbl_scannerTab.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_scannerTab.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		scannerTab.setLayout(gbl_scannerTab);

		scannerBtnPanel = new JPanel();
		GridBagConstraints gbc_scannerBtnPanel = new GridBagConstraints();
		gbc_scannerBtnPanel.insets = new Insets(0, 0, 5, 0);
		gbc_scannerBtnPanel.fill = GridBagConstraints.BOTH;
		gbc_scannerBtnPanel.gridx = 0;
		gbc_scannerBtnPanel.gridy = 0;
		scannerTab.add(scannerBtnPanel, gbc_scannerBtnPanel);

		btnScan = new JButton("Scan");
		btnScan.setIcon(new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/magnifier.png")));
		scannerBtnPanel.add(btnScan);
		btnScan.setEnabled(false);

		btnFix = new JButton("Fix");
		btnFix.setIcon(new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/tick.png")));
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
		scannerSettingsPanel.setBackground(UIManager.getColor("Panel.background"));
		scannerSettingsPanel.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_scannerSettingsPanel = new GridBagConstraints();
		gbc_scannerSettingsPanel.ipady = 20;
		gbc_scannerSettingsPanel.insets = new Insets(0, 5, 5, 5);
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

		chckbxNeedSHA1 = new JCheckBox("Calculate all SHA1");
		chckbxNeedSHA1.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				curr_profile.setProperty("need_sha1_or_md5", e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		chckbxNeedSHA1.setToolTipText("Calculate SHA1 while scanning new files, even if CRC is not suspicious (Slow process)");
		GridBagConstraints gbc_chckbxNeedSHA1 = new GridBagConstraints();
		gbc_chckbxNeedSHA1.fill = GridBagConstraints.BOTH;
		gbc_chckbxNeedSHA1.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNeedSHA1.gridx = 0;
		gbc_chckbxNeedSHA1.gridy = 0;
		scannerSettingsPanel.add(chckbxNeedSHA1, gbc_chckbxNeedSHA1);

		chckbxUseParallelism = new JCheckBox("Enable MultiThreading");
		chckbxUseParallelism.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				curr_profile.setProperty("use_parallelism", e.getStateChange() == ItemEvent.SELECTED);
			}
		});

		chckbxCreateMissingSets = new JCheckBox("Create missing sets");
		chckbxCreateMissingSets.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				curr_profile.setProperty("create_mode", e.getStateChange() == ItemEvent.SELECTED);
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
		chckbxUseParallelism.setToolTipText("Use all CPU while scanning and fixing, SSD is STRONGLY recommended otherwise you may get slower results!");
		GridBagConstraints gbc_chckbxUseParallelism = new GridBagConstraints();
		gbc_chckbxUseParallelism.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxUseParallelism.fill = GridBagConstraints.BOTH;
		gbc_chckbxUseParallelism.gridx = 0;
		gbc_chckbxUseParallelism.gridy = 1;
		scannerSettingsPanel.add(chckbxUseParallelism, gbc_chckbxUseParallelism);

		chckbxCreateOnlyComplete = new JCheckBox("Create only complete sets");
		chckbxCreateOnlyComplete.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				curr_profile.setProperty("createfull_mode", e.getStateChange() == ItemEvent.SELECTED);
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

		lblCompression = new JLabel("Compression");
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
				curr_profile.settings.setProperty("format", cbCompression.getSelectedItem().toString());
			}
		});
		GridBagConstraints gbc_cbCompression = new GridBagConstraints();
		gbc_cbCompression.gridwidth = 2;
		gbc_cbCompression.insets = new Insets(0, 0, 5, 5);
		gbc_cbCompression.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbCompression.gridx = 1;
		gbc_cbCompression.gridy = 0;
		scannerSubSettingsPanel.add(cbCompression, gbc_cbCompression);

		lblMergeMode = new JLabel("Merge Mode");
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
		cbbxMergeMode.setToolTipText("Select the Merge mode");
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
				curr_profile.settings.setProperty("merge_mode", cbbxMergeMode.getSelectedItem().toString());
				cbHashCollision.setEnabled(((MergeOptions) cbbxMergeMode.getSelectedItem()).isMerge());
			}
		});

		lblHashCollision = new JLabel("Hash collision");
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
				curr_profile.settings.setProperty("hash_collision_mode", cbHashCollision.getSelectedItem().toString());
			}
		});
		GridBagConstraints gbc_cbHashCollision = new GridBagConstraints();
		gbc_cbHashCollision.gridwidth = 2;
		gbc_cbHashCollision.insets = new Insets(0, 0, 5, 5);
		gbc_cbHashCollision.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbHashCollision.gridx = 1;
		gbc_cbHashCollision.gridy = 2;
		scannerSubSettingsPanel.add(cbHashCollision, gbc_cbHashCollision);

		lblRomsDest = new JLabel("Roms Dest.");
		GridBagConstraints gbc_lblRomsDest = new GridBagConstraints();
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
				curr_profile.setProperty("roms_dest_dir", txtRomsDest.getText());
			}
		});
		GridBagConstraints gbc_txtRomsDest = new GridBagConstraints();
		gbc_txtRomsDest.insets = new Insets(0, 0, 5, 0);
		gbc_txtRomsDest.fill = GridBagConstraints.BOTH;
		gbc_txtRomsDest.gridx = 1;
		gbc_txtRomsDest.gridy = 3;
		scannerSubSettingsPanel.add(txtRomsDest, gbc_txtRomsDest);
		txtRomsDest.setColumns(10);

		btnRomsDest = new JButton("");
		btnRomsDest.setIcon(new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/disk.png")));
		btnRomsDest.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new JFileChooser()
				{
					{
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

		lblSrcDir = new JLabel("Src Dir.");
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
						curr_profile.setProperty("src_dir", String.join("|", Collections.list(modelSrcDir.elements()).stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList())));

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

		mntmDeleteSelected = new JMenuItem("Delete Selected");
		mntmDeleteSelected.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				List<File> files = listSrcDir.getSelectedValuesList();
				for(File file : files)
					modelSrcDir.removeElement(file);
				curr_profile.setProperty("src_dir", String.join("|", Collections.list(modelSrcDir.elements()).stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList())));
			}
		});
		mntmDeleteSelected.setIcon(new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/folder_delete.png")));
		popupMenu.add(mntmDeleteSelected);

		mntmAddDirectory = new JMenuItem("Add Directory");
		mntmAddDirectory.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new JFileChooser()
				{
					{
						File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
						setCurrentDirectory(workdir);
						setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						setMultiSelectionEnabled(true);
						if(showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
						{
							for(File f : getSelectedFiles())
								modelSrcDir.addElement(f);
							curr_profile.setProperty("src_dir", String.join("|", Collections.list(modelSrcDir.elements()).stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList())));
						}
					}
				};
			}
		});
		mntmAddDirectory.setIcon(new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/folder_add.png")));
		popupMenu.add(mntmAddDirectory);
		GridBagConstraints gbc_listSrcDir = new GridBagConstraints();
		gbc_listSrcDir.insets = new Insets(0, 0, 5, 5);
		gbc_listSrcDir.anchor = GridBagConstraints.NORTH;
		gbc_listSrcDir.gridwidth = 2;
		gbc_listSrcDir.fill = GridBagConstraints.BOTH;
		gbc_listSrcDir.gridx = 1;
		gbc_listSrcDir.gridy = 4;
		scannerSubSettingsPanel.add(listSrcDir, gbc_listSrcDir);

		settingsTab = new JPanel();
		mainPane.addTab("Settings", new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/cog.png")), settingsTab, null);
		settingsTab.setLayout(new BorderLayout(0, 0));

		settingsPane = new JTabbedPane(JTabbedPane.TOP);
		settingsTab.add(settingsPane);

		compressors = new JPanel();
		settingsPane.addTab("Compressors", new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/compress.png")), compressors, null);
		settingsPane.setEnabledAt(0, true);
		compressors.setLayout(new BorderLayout(0, 0));

		compressorsPane = new JTabbedPane(JTabbedPane.TOP);
		compressors.add(compressorsPane);

		panelZip = new JPanel();
		compressorsPane.addTab("Zip", null, panelZip, null);
		panelZip.setLayout(new GridLayout(0, 1, 0, 0));

		chkbxZipUseTemp = new JCheckBox("Use temporary files (recommended)");
		chkbxZipUseTemp.setHorizontalAlignment(SwingConstants.CENTER);
		chkbxZipUseTemp.setSelected(true);
		panelZip.add(chkbxZipUseTemp);

		lblZipWarn = new JLabel();
		lblZipWarn.setVerticalAlignment(SwingConstants.TOP);
		lblZipWarn.setHorizontalAlignment(SwingConstants.CENTER);
		lblZipWarn.setBackground(UIManager.getColor("Panel.background"));
		lblZipWarn.setText("<html><center>There is no compression method choice available, <br>if you need to specify one then you will have to use zip (external)</center></html>");
		lblZipWarn.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
		panelZip.add(lblZipWarn);

		panelZipE = new JPanel();
		compressorsPane.addTab("Zip (external)", null, panelZipE, null);
		GridBagLayout gbl_panelZipE = new GridBagLayout();
		gbl_panelZipE.columnWidths = new int[] { 85, 246, 40, 0 };
		gbl_panelZipE.rowHeights = new int[] { 0, 28, 28, 28, 0, 0 };
		gbl_panelZipE.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panelZipE.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelZipE.setLayout(gbl_panelZipE);

		lblZipECmd = new JLabel("Path to command");
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
				Settings.setProperty("zip_cmd", tfZipECmd.getText());
			}
		});
		tfZipECmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				Settings.setProperty("zip_cmd", tfZipECmd.getText());
			}
		});
		tfZipECmd.setText(Settings.getProperty("zip_cmd", FindCmd.find7z()));
		GridBagConstraints gbc_tfZipECmd = new GridBagConstraints();
		gbc_tfZipECmd.insets = new Insets(0, 0, 5, 0);
		gbc_tfZipECmd.fill = GridBagConstraints.BOTH;
		gbc_tfZipECmd.gridx = 1;
		gbc_tfZipECmd.gridy = 1;
		panelZipE.add(tfZipECmd, gbc_tfZipECmd);
		tfZipECmd.setColumns(30);

		btZipECmd = new JButton("");
		btZipECmd.setIcon(new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/disk.png")));
		GridBagConstraints gbc_btZipECmd = new GridBagConstraints();
		gbc_btZipECmd.fill = GridBagConstraints.BOTH;
		gbc_btZipECmd.insets = new Insets(0, 0, 5, 5);
		gbc_btZipECmd.gridx = 2;
		gbc_btZipECmd.gridy = 1;
		panelZipE.add(btZipECmd, gbc_btZipECmd);

		lblZipEArgs = new JLabel("Compression level");
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
				Settings.setProperty("zip_level", cbZipEArgs.getSelectedItem().toString());
			}
		});
		cbZipEArgs.setSelectedItem(ZipOptions.valueOf(Settings.getProperty("zip_level", ZipOptions.NORMAL.toString())));
		GridBagConstraints gbc_cbZipEArgs = new GridBagConstraints();
		gbc_cbZipEArgs.insets = new Insets(0, 0, 5, 5);
		gbc_cbZipEArgs.gridwidth = 2;
		gbc_cbZipEArgs.fill = GridBagConstraints.BOTH;
		gbc_cbZipEArgs.gridx = 1;
		gbc_cbZipEArgs.gridy = 2;
		panelZipE.add(cbZipEArgs, gbc_cbZipEArgs);

		lblZipEThreads = new JLabel("Threads");
		GridBagConstraints gbc_lblZipEThreads = new GridBagConstraints();
		gbc_lblZipEThreads.insets = new Insets(0, 0, 5, 5);
		gbc_lblZipEThreads.anchor = GridBagConstraints.EAST;
		gbc_lblZipEThreads.gridx = 0;
		gbc_lblZipEThreads.gridy = 3;
		panelZipE.add(lblZipEThreads, gbc_lblZipEThreads);

		tfZipEThreads = new JTextField();
		tfZipEThreads.setText("1");
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
		lblZipEWarning.setText("<html>\r\n<center>\r\nCommand line executable will be used only if SevenZipJBinding is not available<br>Internal methods are still used for listing and decompression...\r\n</center>\r\n</html>");
		lblZipEWarning.setHorizontalAlignment(SwingConstants.CENTER);
		lblZipEWarning.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
		lblZipEWarning.setBackground(UIManager.getColor("Button.background"));
		GridBagConstraints gbc_lblZipEWarning = new GridBagConstraints();
		gbc_lblZipEWarning.gridwidth = 3;
		gbc_lblZipEWarning.gridx = 0;
		gbc_lblZipEWarning.gridy = 4;
		panelZipE.add(lblZipEWarning, gbc_lblZipEWarning);

		panel7Zip = new JPanel();
		compressorsPane.addTab("7z (external)", null, panel7Zip, null);
		compressorsPane.setEnabledAt(2, true);
		GridBagLayout gbl_panel7Zip = new GridBagLayout();
		gbl_panel7Zip.columnWidths = new int[] { 85, 123, 0, 40, 0 };
		gbl_panel7Zip.rowHeights = new int[] { 0, 28, 28, 28, 0, 0 };
		gbl_panel7Zip.columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel7Zip.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panel7Zip.setLayout(gbl_panel7Zip);

		lbl7zCmd = new JLabel("Path to command");
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
				Settings.setProperty("7z_cmd", tf7zCmd.getText());
			}
		});
		tf7zCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Settings.setProperty("7z_cmd", tf7zCmd.getText());
			}
		});
		tf7zCmd.setText(Settings.getProperty("7z_cmd", FindCmd.find7z()));
		tf7zCmd.setColumns(30);
		GridBagConstraints gbc_tf7zCmd = new GridBagConstraints();
		gbc_tf7zCmd.gridwidth = 2;
		gbc_tf7zCmd.fill = GridBagConstraints.BOTH;
		gbc_tf7zCmd.insets = new Insets(0, 0, 5, 0);
		gbc_tf7zCmd.gridx = 1;
		gbc_tf7zCmd.gridy = 1;
		panel7Zip.add(tf7zCmd, gbc_tf7zCmd);

		btn7zCmd = new JButton("");
		btn7zCmd.setIcon(new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/disk.png")));
		GridBagConstraints gbc_btn7zCmd = new GridBagConstraints();
		gbc_btn7zCmd.fill = GridBagConstraints.BOTH;
		gbc_btn7zCmd.insets = new Insets(0, 0, 5, 5);
		gbc_btn7zCmd.gridx = 3;
		gbc_btn7zCmd.gridy = 1;
		panel7Zip.add(btn7zCmd, gbc_btn7zCmd);

		lbl7zArgs = new JLabel("Compression level");
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
				Settings.setProperty("7z_level", cb7zArgs.getSelectedItem().toString());
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
		cb7zArgs.setSelectedItem(SevenZipOptions.valueOf(Settings.getProperty("7z_level", SevenZipOptions.NORMAL.toString())));
		GridBagConstraints gbc_cb7zArgs = new GridBagConstraints();
		gbc_cb7zArgs.fill = GridBagConstraints.BOTH;
		gbc_cb7zArgs.gridwidth = 3;
		gbc_cb7zArgs.insets = new Insets(0, 0, 5, 5);
		gbc_cb7zArgs.gridx = 1;
		gbc_cb7zArgs.gridy = 2;
		panel7Zip.add(cb7zArgs, gbc_cb7zArgs);

		lbl7zThreads = new JLabel("Threads");
		GridBagConstraints gbc_lbl7zThreads = new GridBagConstraints();
		gbc_lbl7zThreads.insets = new Insets(0, 0, 5, 5);
		gbc_lbl7zThreads.anchor = GridBagConstraints.EAST;
		gbc_lbl7zThreads.gridx = 0;
		gbc_lbl7zThreads.gridy = 3;
		panel7Zip.add(lbl7zThreads, gbc_lbl7zThreads);

		tf7zThreads = new JTextField();
		tf7zThreads.setText(Integer.toString(Settings.getProperty("7z_threads", -1)));
		tf7zThreads.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				Settings.setProperty("7z_threads", tf7zThreads.getText());
			}
		});
		tf7zThreads.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				Settings.setProperty("7z_threads", tf7zThreads.getText());
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

		ckbx7zSolid = new JCheckBox("Solid archive");
		ckbx7zSolid.setSelected(Settings.getProperty("7z_solid", true));
		cb7zArgs.setEnabled(ckbx7zSolid.isSelected());
		ckbx7zSolid.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				cb7zArgs.setEnabled(ckbx7zSolid.isSelected());
				Settings.setProperty("7z_solid", ckbx7zSolid.isSelected());
			}
		});
		GridBagConstraints gbc_ckbx7zSolid = new GridBagConstraints();
		gbc_ckbx7zSolid.insets = new Insets(0, 0, 5, 5);
		gbc_ckbx7zSolid.gridx = 2;
		gbc_ckbx7zSolid.gridy = 3;
		panel7Zip.add(ckbx7zSolid, gbc_ckbx7zSolid);

		lbl7zWarning = new JLabel();
		lbl7zWarning.setVerticalAlignment(SwingConstants.TOP);
		lbl7zWarning.setText("<html>\r\n<center>\r\nCommand line executable will be used only if SevenZipJBinding is not available<br>Internal methods are still used for listing...\r\n</center>\r\n</html>");
		lbl7zWarning.setHorizontalAlignment(SwingConstants.CENTER);
		lbl7zWarning.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 11));
		lbl7zWarning.setBackground(UIManager.getColor("Button.background"));
		GridBagConstraints gbc_lbl7zWarning = new GridBagConstraints();
		gbc_lbl7zWarning.gridwidth = 4;
		gbc_lbl7zWarning.gridx = 0;
		gbc_lbl7zWarning.gridy = 4;
		panel7Zip.add(lbl7zWarning, gbc_lbl7zWarning);

		panelTZip = new JPanel();
		compressorsPane.addTab("TorrentZip (external)", null, panelTZip, null);
		GridBagLayout gbl_panelTZip = new GridBagLayout();
		gbl_panelTZip.columnWidths = new int[] { 100, 334, 34, 0 };
		gbl_panelTZip.rowHeights = new int[] { 0, 20, 0, 0 };
		gbl_panelTZip.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelTZip.rowWeights = new double[] { 1.0, 0.0, 1.0, Double.MIN_VALUE };
		panelTZip.setLayout(gbl_panelTZip);

		lblTZipCmd = new JLabel("Path to command");
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
				Settings.setProperty("tzip_cmd", tfTZipCmd.getText());
			}
		});
		tfTZipCmd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
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
		panelTZip.add(tfTZipCmd, gbc_tfTZipCmd);

		btTZipCmd = new JButton("");
		btTZipCmd.setIcon(new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/disk.png")));
		GridBagConstraints gbc_btTZipCmd = new GridBagConstraints();
		gbc_btTZipCmd.insets = new Insets(0, 0, 5, 5);
		gbc_btTZipCmd.anchor = GridBagConstraints.WEST;
		gbc_btTZipCmd.gridx = 2;
		gbc_btTZipCmd.gridy = 1;
		panelTZip.add(btTZipCmd, gbc_btTZipCmd);

		debug = new JPanel();
		settingsPane.addTab("Debug", new ImageIcon(JRomManager.class.getResource("/jrm/resources/icons/bug.png")), debug, null);
		GridBagLayout gbl_debug = new GridBagLayout();
		gbl_debug.columnWidths = new int[] { 100, 0, 0 };
		gbl_debug.rowHeights = new int[] { 0, 0 };
		gbl_debug.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_debug.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		debug.setLayout(gbl_debug);

		lblLogLevel = new JLabel("Log level");
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

		mainFrame.pack();
	}

	@SuppressWarnings("serial")
	private void importDat()
	{
		try
		{
			new JFileChooser()
			{
				{
					addChoosableFileFilter(new FileNameExtensionFilter("Dat file", "dat", "xml"));
					addChoosableFileFilter(new FileNameExtensionFilter("Mame executable", "exe"));
					if(showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
					{
						Import imprt = new Import(getSelectedFile());
						new JFileChooser()
						{
							{
								setFileSelectionMode(JFileChooser.FILES_ONLY);
								setDialogType(JFileChooser.SAVE_DIALOG);
								setSelectedFile(imprt.file);
								if(showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
								{
									FileUtils.copyFile(imprt.file, getSelectedFile());
								}
							}
						};
					}
				}
			};
		}
		catch(IOException e)
		{
			Log.err("Encountered IO Exception", e);
		}

	}

	private void loadProfile(File profile)
	{
		if(curr_profile != null)
			curr_profile.saveSettings();
		final Progress progress = new Progress(mainFrame);
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

	@SuppressWarnings("serial")
	private void chooseProfile()
	{
		new JFileChooser()
		{
			{
				File workdir = Paths.get("./xmlfiles").toAbsolutePath().normalize().toFile();
				setCurrentDirectory(workdir);
				addChoosableFileFilter(new FileNameExtensionFilter("Dat file", "dat", "xml"));
				if(showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION)
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

		final Progress progress = new Progress(mainFrame);
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
		final Progress progress = new Progress(mainFrame);
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
		chckbxNeedSHA1.setSelected(curr_profile.getProperty("need_sha1_or_md5", false));
		chckbxUseParallelism.setSelected(curr_profile.getProperty("use_parallelism", false));
		chckbxCreateMissingSets.setSelected(curr_profile.getProperty("create_mode", false));
		chckbxCreateOnlyComplete.setSelected(curr_profile.getProperty("createfull_mode", false) && chckbxCreateMissingSets.isSelected());
		chckbxCreateOnlyComplete.setEnabled(chckbxCreateMissingSets.isSelected());
		txtRomsDest.setText(curr_profile.getProperty("roms_dest_dir", ""));
		for(String s : curr_profile.getProperty("src_dir", "").split("\\|"))
			((DefaultListModel<File>) listSrcDir.getModel()).addElement(new File(s));
		cbCompression.setSelectedItem(FormatOptions.valueOf(curr_profile.settings.getProperty("format", FormatOptions.ZIP.toString())));
		cbbxMergeMode.setSelectedItem(MergeOptions.valueOf(curr_profile.settings.getProperty("merge_mode", MergeOptions.SPLIT.toString())));
		cbHashCollision.setEnabled(((MergeOptions) cbbxMergeMode.getSelectedItem()).isMerge());
		cbHashCollision.setSelectedItem(HashCollisionOptions.valueOf(curr_profile.settings.getProperty("hash_collision_mode", HashCollisionOptions.SINGLEFILE.toString())));
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
