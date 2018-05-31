package jrm.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
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
import jrm.compressors.zipfs.ZipLevel;
import jrm.compressors.zipfs.ZipTempThreshold;
import jrm.misc.FindCmd;
import jrm.misc.Settings;
import jrm.profile.Import;
import jrm.profile.Profile;
import jrm.profile.ProfileNFO;
import jrm.profile.ProfileNFOMame.MameStatus;
import jrm.profile.data.Driver;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.DisplayOrientation;
import jrm.profile.data.Software.Supported;
import jrm.profile.data.Systm;
import jrm.profile.data.Years;
import jrm.profile.filter.CatVer.Category;
import jrm.profile.filter.CatVer.SubCategory;
import jrm.profile.filter.NPlayers.NPlayer;
import jrm.profile.fix.Fix;
import jrm.profile.scan.Scan;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.ui.controls.*;
import jrm.ui.controls.JRMFileChooser.OneRootFileSystemView;

@SuppressWarnings("serial")
public class MainFrame extends JFrame
{

	public static ProfileViewer profile_viewer = null;
	public static ReportFrame report_frame = null;

	private static void addPopup(final Component component, final JPopupMenu popup)
	{
		component.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			private void showMenu(final MouseEvent e)
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	private JButton btDisksDest;
	private JButton btn7zCmd;
	private JButton btnFix;
	private JButton btnGc;
	private JButton btnImportDat;
	private JButton btnImportSL;
	private JButton btnInfo;
	private JButton btnLoadProfile;
	private JButton btnReport;
	private JButton btnRomsDest;
	private JButton btnScan;
	private JButton btnSWDest;
	private JButton btSamplesDest;
	private JButton btSWDisksDest;
	private JButton btZipECmd;
	private JComboBox<SevenZipOptions> cb7zArgs;
	private JComboBox<Driver.StatusType> cbbxDriverStatus;
	private JComboBox<CabinetType> cbbxFilterCabinetType;
	private JComboBox<DisplayOrientation> cbbxFilterDisplayOrientation;
	private JComboBox<MergeOptions> cbbxMergeMode;
	private JComboBox<Supported> cbbxSWMinSupportedLvl;
	private JComboBox<String> cbbxYearMax;
	private JComboBox<String> cbbxYearMin;
	private JComboBox<FormatOptions> cbCompression;
	private JComboBox<HashCollisionOptions> cbHashCollision;
	private JComboBox<?> cbLogLevel;
	private JComboBox<ZipOptions> cbZipEArgs;
	private JCheckBox chckbxCreateMissingSets;
	private JCheckBox chckbxCreateOnlyComplete;
	private JCheckBox chckbxIgnoreUnknownContainers;
	private JCheckBox chckbxIgnoreUnneededContainers;
	private JCheckBox chckbxIgnoreUnneededEntries;
	private JCheckBox chckbxIncludeClones;
	private JCheckBox chckbxIncludeDisks;
	private JCheckBox chckbxIncludeSamples;
	private JCheckBox chckbxNeedSHA1;
	private JCheckBox chckbxUseImplicitMerge;
	private JCheckBox chckbxUseParallelism;
	private JCheckBoxList<Systm> checkBoxListSystems;
	private JCheckBox ckbx7zSolid;
	private JPanel compressors;
	private JTabbedPane compressorsPane;
	private Scan curr_scan;
	private JPanel debug;
	private JLabel lbl7zArgs;
	private JLabel lbl7zCmd;
	private JLabel lbl7zThreads;
	private JLabel lbl7zWarning;
	private JLabel lblCabinetType;
	private JLabel lblCompression;
	private JCheckBox lblDisksDest;
	private JLabel lblDisplayOrientation;
	private JLabel lblDriverStatus;
	private JLabel lblHashCollision;
	private JLabel lblLogLevel;
	private JLabel lblMemory;
	private JLabel lblMemoryUsage;
	private JLabel lblMergeMode;
	private JLabel lblProfileinfo;
	private JLabel lblRomsDest;
	private JCheckBox lblSamplesDest;
	private JLabel lblSrcDir;
	private JCheckBox lblSWDest;
	private JCheckBox lblSWDisksDest;
	private JLabel lblSwMinSupportedLvl;
	private JLabel lblYear;
	private JLabel lblZipEArgs;
	private JLabel lblZipECmd;
	private JLabel lblZipEThreads;
	private JLabel lblZipEWarning;
	private JCheckBoxList<NPlayer> listNPlayers;
	private JFileDropList listSrcDir;
	private JTabbedPane mainPane;
	private JMenu mnSelectCat;
	private JMenuItem mntmAddDirectory;
	private JMenuItem mntmCreateFolder;
	private JMenuItem mntmDeleteFolder;
	private JMenuItem mntmDeleteProfile;
	private JMenuItem mntmDeleteSelected;
	private JMenuItem mntmDropCache;
	private JMenuItem mntmInvertSelection;
	private JMenuItem mntmInvertSelectionNPlay;
	private JMenuItem mntmRenameProfile;
	private JMenuItem mntmSelectAll;
	private JMenuItem mntmSelectAllCat;
	private JMenuItem mntmSelectAllNPlay;
	private JMenuItem mntmSelectMatureCat;
	private JMenuItem mntmUnselectAll;
	private JMenuItem mntmSelectNoneNPlay;
	private JMenuItem mntmUnselectAllCat;
	private JMenuItem mntmUnselectMatureCat;
	private JMenuItem mntmUpdateFromMame;
	private JMenu mnUnselectCat;
	private JPanel panel_1;
	private JPanel panel7Zip;
	private JPanel panelZip;
	private JPanel panelZipE;
	private JPopupMenu popupMenu;
	private JPopupMenu popupMenu_1;
	private JPopupMenu popupMenu_2;
	private JPopupMenu popupMenu_3;
	private JPopupMenu popupMenuCat;
	private JPopupMenu popupMenuNPlay;
	private JPanel profilesBtnPanel;
	private JTable profilesList;
	private JSplitPane profilesPanel;
	private JPanel profilesTab;
	private JTree profilesTree;
	private JPanel scannerAdvFilters;
	private JPanel scannerBtnPanel;
	private JTabbedPane scannerCfgTab;
	private JPanel scannerDirectories;
	private JSplitPane scannerFilters;
	private JPanel scannerSettingsPanel;
	private JPanel scannerSubSettingsPanel;
	private JPanel scannerTab;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane_1;
	private JScrollPane scrollPane_2;
	private JScrollPane scrollPaneCatVer;
	private JScrollPane scrollPaneNPlayers;
	private JSeparator separator;
	private JTabbedPane settingsPane;
	private JPanel settingsTab;
	private JFileDropTextField tf7zCmd;
	private JTextField tf7zThreads;
	private JFileDropTextField tfCatVer;
	private JFileDropTextField tfDisksDest;
	private JFileDropTextField tfNPlayers;
	private JFileDropTextField tfSamplesDest;
	private JFileDropTextField tfSWDest;
	private JFileDropTextField tfSWDisksDest;
	private JFileDropTextField tfZipECmd;
	private JTextField tfZipEThreads;
	private JCheckBoxTree treeCatVer;
	private JFileDropTextField txtRomsDest;

	final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private JMenu mnSelect;
	private JMenu mnUnselect;
	private JMenuItem mntmSelectAllBios;
	private JMenuItem mntmSelectAllSoftwares;
	private JMenuItem mntmUnselectAllBios;
	private JMenuItem mntmUnselectAllSoftwares;
	private JComboBox<ZipLevel> cbbxZipLevel;
	private JLabel lblCompressionLevel;
	private JLabel lblTemporaryFilesThreshold;
	private JComboBox<ZipTempThreshold> cbbxZipTempThreshold;
	private JCheckBox chckbxIgnoreMergeNameRoms;
	private JCheckBox chckbxIgnoreMergeNameDisks;

	public MainFrame()
	{
		super();
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				Settings.setProperty("MainFrame.Bounds", Hex.encodeHexString(SerializationUtils.serialize(getBounds()))); //$NON-NLS-1$
			}
		});
		try
		{
			Settings.loadSettings();
			UIManager.setLookAndFeel(Settings.getProperty("LookAndFeel", UIManager.getSystemLookAndFeelClassName()/* UIManager.getCrossPlatformLookAndFeelClassName() */)); //$NON-NLS-1$
			final File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
			final File xmldir = new File(workdir, "xmlfiles"); //$NON-NLS-1$
			xmldir.mkdir();
			ResourceBundle.getBundle("jrm.resources.Messages"); //$NON-NLS-1$
		}
		catch (final Exception e)
		{
			JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			e.printStackTrace();
		}
		initialize();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (Profile.curr_profile != null)
				Profile.curr_profile.saveSettings();
			Settings.saveSettings();
		}));
	}

	private void fix()
	{
		final Progress progress = new Progress(MainFrame.this);
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				if (Profile.curr_profile.hasPropsChanged())
				{
					switch (JOptionPane.showConfirmDialog(MainFrame.this, "Settings did change since last scan, you should rescan before fixing", "Rescan before fix?", JOptionPane.YES_NO_CANCEL_OPTION))
					{
						case JOptionPane.YES_OPTION:
							curr_scan = new Scan(Profile.curr_profile, progress);
							btnFix.setEnabled(curr_scan.actions.stream().mapToInt(Collection::size).sum() > 0);
							if (!btnFix.isEnabled())
								return null;
							break;
						case JOptionPane.NO_OPTION:
							break;
						case JOptionPane.CANCEL_OPTION:
						default:
							return null;
					}
				}
				final Fix fix = new Fix(Profile.curr_profile, curr_scan, progress);
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

	private String getVersion()
	{
		String version = ""; //$NON-NLS-1$
		final Package pkg = this.getClass().getPackage();
		if (pkg.getSpecificationVersion() != null)
			version += " " + pkg.getSpecificationVersion(); //$NON-NLS-1$
		if (pkg.getImplementationVersion() != null)
			version += " " + pkg.getImplementationVersion(); //$NON-NLS-1$
		return version;
	}

	private void importDat(final boolean sl)
	{
		new Thread(() -> {
			final List<FileFilter> filters = Arrays.asList(new FileFilter()
			{
				@Override
				public boolean accept(final File f)
				{
					return f.isDirectory() || FilenameUtils.isExtension(f.getName(), "exe") || f.canExecute(); //$NON-NLS-1$
				}

				@Override
				public String getDescription()
				{
					return Messages.getString("MainFrame.MameExecutable"); //$NON-NLS-1$
				}
			}, new FileNameExtensionFilter(Messages.getString("MainFrame.DatFile"), "dat", "xml") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			);
			new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY, Optional.ofNullable(Settings.getProperty("MainFrame.ChooseExeOrDatToImport", (String) null)).map(File::new).orElse(null), null, filters, Messages.getString("MainFrame.ChooseExeOrDatToImport"), true) //$NON-NLS-1$ //$NON-NLS-2$
					.show(MainFrame.this, chooser -> {
						final Progress progress = new Progress(MainFrame.this);
						Settings.setProperty("MainFrame.ChooseExeOrDatToImport", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
						for (final File selectedfile : chooser.getSelectedFiles())
						{
							progress.setVisible(true);
							progress.setProgress(Messages.getString("MainFrame.ImportingFromMame"), -1); //$NON-NLS-1$
							final Import imprt = new Import(selectedfile, sl);
							progress.dispose();
							final File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
							final File xmldir = new File(workdir, "xmlfiles"); //$NON-NLS-1$
							new JRMFileChooser<Void>(new OneRootFileSystemView(xmldir)).setup(JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY, null, new File(xmldir, imprt.file.getName()), Collections.singletonList(new FileNameExtensionFilter(Messages.getString("MainFrame.DatFile"), "dat", "xml", "jrm")), Messages.getString("MainFrame.ChooseFileName"), false) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
									.show(MainFrame.this, chooser1 -> {
										try
										{
											final File file = chooser1.getSelectedFile();
											final File parent = file.getParentFile();
											FileUtils.copyFile(imprt.file, file);
											if (imprt.is_mame)
											{
												final ProfileNFO pnfo = ProfileNFO.load(file);
												pnfo.mame.set(imprt.org_file, sl);
												if (imprt.roms_file != null)
												{
													FileUtils.copyFileToDirectory(imprt.roms_file, parent);
													pnfo.mame.fileroms = new File(parent, imprt.roms_file.getName());
													if (imprt.sl_file != null)
													{
														FileUtils.copyFileToDirectory(imprt.sl_file, parent);
														pnfo.mame.filesl = new File(parent, imprt.sl_file.getName());
													}
												}
												pnfo.save();
											}
											final DirTreeModel model = (DirTreeModel) profilesTree.getModel();
											final DirNode root = (DirNode) model.getRoot();
											DirNode theNode = root.find(parent);
											if (theNode != null)
											{

												theNode.reload();
												model.reload(theNode);
												if ((theNode = root.find(parent)) != null)
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
										catch (final IOException e)
										{
											e.printStackTrace();
										}
										return null;
									});
						}
						return null;
					});
		}).start();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize()
	{
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/jrm/resources/rom.png"))); //$NON-NLS-1$
		setTitle(Messages.getString("MainFrame.Title") + getVersion()); //$NON-NLS-1$
		setBounds(100, 100, 458, 313);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		mainPane = new JTabbedPane(SwingConstants.TOP);
		getContentPane().add(mainPane);

		MainFrame.report_frame = new ReportFrame(MainFrame.this);

		profilesTab = new JPanel();
		mainPane.addTab(Messages.getString("MainFrame.Profiles"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/script.png")), profilesTab, null); //$NON-NLS-1$ //$NON-NLS-2$
		final GridBagLayout gbl_profilesTab = new GridBagLayout();
		gbl_profilesTab.columnWidths = new int[] { 0, 0 };
		gbl_profilesTab.rowHeights = new int[] { 0, 0, 0 };
		gbl_profilesTab.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_profilesTab.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		profilesTab.setLayout(gbl_profilesTab);

		profilesPanel = new JSplitPane();
		profilesPanel.setContinuousLayout(true);
		profilesPanel.setResizeWeight(0.2);
		profilesPanel.setOneTouchExpandable(true);
		final GridBagConstraints gbc_profilesPanel = new GridBagConstraints();
		gbc_profilesPanel.insets = new Insets(0, 0, 5, 0);
		gbc_profilesPanel.fill = GridBagConstraints.BOTH;
		gbc_profilesPanel.gridx = 0;
		gbc_profilesPanel.gridy = 0;
		profilesTab.add(profilesPanel, gbc_profilesPanel);

		scrollPane = new JScrollPane();
		profilesPanel.setRightComponent(scrollPane);

		profilesList = new JTable();
		final DefaultCellEditor editor = (DefaultCellEditor) profilesList.getDefaultEditor(Object.class);
		editor.setClickCountToStart(3);
		profilesList.setShowVerticalLines(false);
		profilesList.setShowHorizontalLines(false);
		profilesList.setShowGrid(false);
		profilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		profilesList.setPreferredScrollableViewportSize(new Dimension(300, 400));
		profilesList.setFillsViewportHeight(true);
		scrollPane.setViewportView(profilesList);
		final FileTableModel filemodel = new FileTableModel();
		profilesList.setModel(filemodel);
		for (int i = 0; i < profilesList.getColumnCount(); i++)
		{
			final TableColumn column = profilesList.getColumnModel().getColumn(i);
			column.setCellRenderer(new FileTableCellRenderer());
			if (filemodel.columnsWidths[i] >= 0)
			{
				column.setPreferredWidth(filemodel.columnsWidths[i]);
			}
			else
			{
				final int width = profilesList.getFontMetrics(profilesList.getFont()).stringWidth(String.format("%0" + (-filemodel.columnsWidths[i]) + "d", 0)); //$NON-NLS-1$ //$NON-NLS-2$
				column.setMinWidth(width);
				column.setMaxWidth(width);
			}
		}
		profilesList.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(final MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					final JTable target = (JTable) e.getSource();
					final int row = target.getSelectedRow();
					if (row >= 0)
					{
						loadProfile(filemodel.getNfoAt(row));
					}
				}
				// super.mouseClicked(e);
			}
		});

		scrollPane_1 = new JScrollPane();
		scrollPane_1.setMinimumSize(new Dimension(80, 22));
		profilesPanel.setLeftComponent(scrollPane_1);

		profilesTree = new JTree();
		scrollPane_1.setViewportView(profilesTree);
		final DirTreeModel profilesTreeModel = new DirTreeModel(new DirNode(Paths.get("./xmlfiles").toAbsolutePath().normalize().toFile())); //$NON-NLS-1$
		profilesTree.setModel(profilesTreeModel);
		profilesTree.setRootVisible(true);
		profilesTree.setShowsRootHandles(true);
		profilesTree.setEditable(true);
		final DirTreeCellRenderer profilesTreeRenderer = new DirTreeCellRenderer();
		profilesTree.setCellRenderer(profilesTreeRenderer);
		profilesTree.setCellEditor(new DirTreeCellEditor(profilesTree, profilesTreeRenderer));
		profilesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		profilesTree.addTreeSelectionListener(new DirTreeSelectionListener(profilesList));

		popupMenu_2 = new JPopupMenu();
		popupMenu_2.addPopupMenuListener(new PopupMenuListener()
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
				mntmDeleteProfile.setEnabled(profilesList.getSelectedRowCount() > 0);
				mntmRenameProfile.setEnabled(profilesList.getSelectedRowCount() > 0);
				mntmDropCache.setEnabled(profilesList.getSelectedRowCount() > 0);
				mntmUpdateFromMame.setEnabled(profilesList.getSelectedRowCount() > 0 && EnumSet.of(MameStatus.NEEDUPDATE, MameStatus.NOTFOUND).contains(filemodel.getNfoAt(profilesList.getSelectedRow()).mame.getStatus()));
				if (profilesList.getSelectedRowCount() > 0)
					mntmUpdateFromMame.setText(Messages.getString("MainFrame.mntmUpdateFromMame.text") + " (" + filemodel.getNfoAt(profilesList.getSelectedRow()).mame.getStatus().getMsg() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				else
					mntmUpdateFromMame.setText(Messages.getString("MainFrame.mntmUpdateFromMame.text")); //$NON-NLS-1$
			}
		});
		MainFrame.addPopup(profilesList, popupMenu_2);

		mntmDeleteProfile = new JMenuItem(Messages.getString("MainFrame.mntmDeleteProfile.text")); //$NON-NLS-1$
		mntmDeleteProfile.addActionListener(e -> {
			final int row = profilesList.getSelectedRow();
			if (row >= 0)
			{
				final ProfileNFO nfo = filemodel.getNfoAt(row);
				if (Profile.curr_profile == null || !Profile.curr_profile.nfo.equals(nfo))
				{
					if (nfo.delete())
						filemodel.populate();
				}
			}
		});
		mntmDeleteProfile.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/script_delete.png"))); //$NON-NLS-1$
		popupMenu_2.add(mntmDeleteProfile);

		mntmRenameProfile = new JMenuItem(Messages.getString("MainFrame.mntmRenameProfile.text")); //$NON-NLS-1$
		mntmRenameProfile.addActionListener(e -> {
			final int row = profilesList.getSelectedRow();
			if (row >= 0)
			{
				profilesList.editCellAt(row, 0);
			}
		});
		mntmRenameProfile.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/script_edit.png"))); //$NON-NLS-1$
		popupMenu_2.add(mntmRenameProfile);

		mntmDropCache = new JMenuItem(Messages.getString("MainFrame.mntmDropCache.text")); //$NON-NLS-1$
		mntmDropCache.addActionListener(e -> {
			final int row = profilesList.getSelectedRow();
			if (row >= 0)
				new File(filemodel.getFileAt(row).getAbsolutePath() + ".cache").delete(); //$NON-NLS-1$
		});
		mntmDropCache.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/bin.png"))); //$NON-NLS-1$
		popupMenu_2.add(mntmDropCache);

		separator = new JSeparator();
		popupMenu_2.add(separator);

		mntmUpdateFromMame = new JMenuItem(Messages.getString("MainFrame.mntmUpdateFromMame.text")); //$NON-NLS-1$
		mntmUpdateFromMame.addActionListener(e -> {
			final int row = profilesList.getSelectedRow();
			if (row >= 0)
			{
				try
				{
					final ProfileNFO nfo = filemodel.getNfoAt(row);
					if (nfo.mame.getStatus() == MameStatus.NEEDUPDATE || (nfo.mame.getStatus() == MameStatus.NOTFOUND && new JRMFileChooser<MameStatus>(JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY, null, nfo.mame.getFile(), null, Messages.getString("MainFrame.ChooseMameNewLocation"), false).show(MainFrame.this, chooser -> { //$NON-NLS-1$
						if (chooser.getSelectedFile().exists())
							return nfo.mame.relocate(chooser.getSelectedFile());
						return MameStatus.NOTFOUND;
					}) == MameStatus.NEEDUPDATE))
					{
						final Import imprt = new Import(nfo.mame.getFile(), nfo.mame.isSL());
						nfo.mame.delete();
						nfo.mame.fileroms = imprt.roms_file;
						nfo.mame.filesl = imprt.sl_file;
						nfo.mame.setUpdated();
						nfo.stats.reset();
						nfo.save();
					}
				}
				catch (final Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});
		popupMenu_2.add(mntmUpdateFromMame);
		profilesTree.setSelectionRow(0);

		popupMenu_1 = new JPopupMenu();
		popupMenu_1.addPopupMenuListener(new PopupMenuListener()
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
				mntmCreateFolder.setEnabled(profilesTree.getSelectionCount() > 0);
				mntmDeleteFolder.setEnabled(profilesTree.getSelectionCount() > 0 && !((DirNode) profilesTree.getLastSelectedPathComponent()).isRoot());
			}
		});
		MainFrame.addPopup(profilesTree, popupMenu_1);

		mntmCreateFolder = new JMenuItem(Messages.getString("MainFrame.mntmCreateFolder.text")); //$NON-NLS-1$
		mntmCreateFolder.addActionListener(e -> {
			final DirNode selectedNode = (DirNode) profilesTree.getLastSelectedPathComponent();
			if (selectedNode != null)
			{
				final DirNode newnode = new DirNode(new DirNode.Dir(new File(selectedNode.dir.getFile(), Messages.getString("MainFrame.NewFolder")))); //$NON-NLS-1$
				selectedNode.add(newnode);
				profilesTreeModel.reload(selectedNode);
				final TreePath path = new TreePath(newnode.getPath());
				profilesTree.setSelectionPath(path);
				profilesTree.startEditingAtPath(path);
			}
		});
		mntmCreateFolder.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/folder_add.png"))); //$NON-NLS-1$
		popupMenu_1.add(mntmCreateFolder);

		mntmDeleteFolder = new JMenuItem(Messages.getString("MainFrame.mntmDeleteFolder.text")); //$NON-NLS-1$
		mntmDeleteFolder.addActionListener(e -> {
			final DirNode selectedNode = (DirNode) profilesTree.getLastSelectedPathComponent();
			if (selectedNode != null)
			{
				final DirNode parent = (DirNode) selectedNode.getParent();
				profilesTreeModel.removeNodeFromParent(selectedNode);
				final TreePath path = new TreePath(parent.getPath());
				profilesTree.setSelectionPath(path);
			}
		});
		mntmDeleteFolder.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/folder_delete.png"))); //$NON-NLS-1$
		popupMenu_1.add(mntmDeleteFolder);

		profilesBtnPanel = new JPanel();
		final GridBagConstraints gbc_profilesBtnPanel = new GridBagConstraints();
		gbc_profilesBtnPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_profilesBtnPanel.gridx = 0;
		gbc_profilesBtnPanel.gridy = 1;
		profilesTab.add(profilesBtnPanel, gbc_profilesBtnPanel);

		btnLoadProfile = new JButton(Messages.getString("MainFrame.btnLoadProfile.text")); //$NON-NLS-1$
		btnLoadProfile.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/add.png"))); //$NON-NLS-1$
		btnLoadProfile.setEnabled(false);
		btnLoadProfile.addActionListener(e -> {
			// chooseProfile();
		});
		profilesBtnPanel.add(btnLoadProfile);

		btnImportDat = new JButton(Messages.getString("MainFrame.btnImportDat.text")); //$NON-NLS-1$
		btnImportDat.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/script_go.png"))); //$NON-NLS-1$
		btnImportDat.addActionListener(e -> importDat(false));
		profilesBtnPanel.add(btnImportDat);

		btnImportSL = new JButton(Messages.getString("MainFrame.btnImportSL.text")); //$NON-NLS-1$
		btnImportSL.addActionListener(e -> importDat(true));
		btnImportSL.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/application_go.png"))); //$NON-NLS-1$
		profilesBtnPanel.add(btnImportSL);

		scannerTab = new JPanel();
		mainPane.addTab(Messages.getString("MainFrame.Scanner"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/drive_magnify.png")), scannerTab, null); //$NON-NLS-1$ //$NON-NLS-2$
		mainPane.setEnabledAt(1, false);
		final GridBagLayout gbl_scannerTab = new GridBagLayout();
		gbl_scannerTab.columnWidths = new int[] { 104, 0 };
		gbl_scannerTab.rowHeights = new int[] { 0, 0, 24, 0 };
		gbl_scannerTab.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_scannerTab.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		scannerTab.setLayout(gbl_scannerTab);

		scannerBtnPanel = new JPanel();
		final GridBagConstraints gbc_scannerBtnPanel = new GridBagConstraints();
		gbc_scannerBtnPanel.insets = new Insets(0, 0, 5, 0);
		gbc_scannerBtnPanel.fill = GridBagConstraints.BOTH;
		gbc_scannerBtnPanel.gridx = 0;
		gbc_scannerBtnPanel.gridy = 0;
		scannerTab.add(scannerBtnPanel, gbc_scannerBtnPanel);

		btnInfo = new JButton(Messages.getString("MainFrame.btnInfo.text")); //$NON-NLS-1$
		btnInfo.addActionListener(e -> {
			if (MainFrame.profile_viewer == null)
				MainFrame.profile_viewer = new ProfileViewer(MainFrame.this, Profile.curr_profile);
			MainFrame.profile_viewer.setVisible(true);
		});
		btnInfo.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/information.png"))); //$NON-NLS-1$
		scannerBtnPanel.add(btnInfo);

		btnScan = new JButton(Messages.getString("MainFrame.btnScan.text")); //$NON-NLS-1$
		btnScan.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/magnifier.png"))); //$NON-NLS-1$
		scannerBtnPanel.add(btnScan);
		btnScan.setEnabled(false);

		btnReport = new JButton(Messages.getString("MainFrame.btnReport.text")); //$NON-NLS-1$
		btnReport.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/report.png"))); //$NON-NLS-1$
		btnReport.addActionListener(e -> EventQueue.invokeLater(() -> MainFrame.report_frame.setVisible(true)));
		scannerBtnPanel.add(btnReport);

		btnFix = new JButton(Messages.getString("MainFrame.btnFix.text")); //$NON-NLS-1$
		btnFix.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/tick.png"))); //$NON-NLS-1$
		scannerBtnPanel.add(btnFix);
		btnFix.addActionListener(e -> fix());
		btnFix.setEnabled(false);
		btnScan.addActionListener(e -> scan());

		scannerCfgTab = new JTabbedPane(SwingConstants.TOP);
		final GridBagConstraints gbc_scannerCfgTab = new GridBagConstraints();
		gbc_scannerCfgTab.fill = GridBagConstraints.BOTH;
		gbc_scannerCfgTab.gridx = 0;
		gbc_scannerCfgTab.gridy = 1;
		scannerTab.add(scannerCfgTab, gbc_scannerCfgTab);

		scannerDirectories = new JPanel();
		scannerCfgTab.addTab(Messages.getString("MainFrame.scannerDirectories.title"), null, scannerDirectories, null); //$NON-NLS-1$
		final GridBagLayout gbl_scannerDirectories = new GridBagLayout();
		gbl_scannerDirectories.columnWidths = new int[] { 109, 65, 0, 0 };
		gbl_scannerDirectories.rowHeights = new int[] { 26, 0, 0, 0, 0, 0, 0 };
		gbl_scannerDirectories.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_scannerDirectories.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		scannerDirectories.setLayout(gbl_scannerDirectories);

		lblRomsDest = new JLabel(Messages.getString("MainFrame.lblRomsDest.text")); //$NON-NLS-1$
		lblRomsDest.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbc_lblRomsDest = new GridBagConstraints();
		gbc_lblRomsDest.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblRomsDest.insets = new Insets(0, 0, 5, 5);
		gbc_lblRomsDest.gridx = 0;
		gbc_lblRomsDest.gridy = 0;
		scannerDirectories.add(lblRomsDest, gbc_lblRomsDest);

		txtRomsDest = new JFileDropTextField(txt -> Profile.curr_profile.setProperty("roms_dest_dir", txt)); //$NON-NLS-1$
		txtRomsDest.setMode(JFileDropMode.DIRECTORY);
		txtRomsDest.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		txtRomsDest.setColumns(10);
		final GridBagConstraints gbc_txtRomsDest = new GridBagConstraints();
		gbc_txtRomsDest.fill = GridBagConstraints.BOTH;
		gbc_txtRomsDest.insets = new Insets(0, 0, 5, 5);
		gbc_txtRomsDest.gridx = 1;
		gbc_txtRomsDest.gridy = 0;
		scannerDirectories.add(txtRomsDest, gbc_txtRomsDest);

		btnRomsDest = new JButton(""); //$NON-NLS-1$
		final GridBagConstraints gbc_btnRomsDest = new GridBagConstraints();
		gbc_btnRomsDest.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnRomsDest.insets = new Insets(0, 0, 5, 0);
		gbc_btnRomsDest.gridx = 2;
		gbc_btnRomsDest.gridy = 0;
		scannerDirectories.add(btnRomsDest, gbc_btnRomsDest);
		btnRomsDest.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		btnRomsDest.addActionListener(e -> {
			final File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
			new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(Profile.curr_profile.getProperty("MainFrame.ChooseRomsDestination", workdir.getAbsolutePath())), new File(txtRomsDest.getText()), null, Messages.getString("MainFrame.ChooseRomsDestination"), false).show(MainFrame.this, chooser -> { //$NON-NLS-1$ //$NON-NLS-2$
				Profile.curr_profile.setProperty("MainFrame.ChooseRomsDestination", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				txtRomsDest.setText(chooser.getSelectedFile().getAbsolutePath());
				Profile.curr_profile.setProperty("roms_dest_dir", txtRomsDest.getText()); //$NON-NLS-1$
				return null;
			});
		});

		lblDisksDest = new JCheckBox(Messages.getString("MainFrame.lblDisksDest.text")); //$NON-NLS-1$
		lblDisksDest.addItemListener(e -> {
			tfDisksDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btDisksDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			Profile.curr_profile.setProperty("disks_dest_dir_enabled", e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		lblDisksDest.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbc_lblDisksDest = new GridBagConstraints();
		gbc_lblDisksDest.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDisksDest.insets = new Insets(0, 0, 5, 5);
		gbc_lblDisksDest.gridx = 0;
		gbc_lblDisksDest.gridy = 1;
		scannerDirectories.add(lblDisksDest, gbc_lblDisksDest);

		tfDisksDest = new JFileDropTextField(txt -> Profile.curr_profile.setProperty("disks_dest_dir", txt)); //$NON-NLS-1$
		tfDisksDest.setMode(JFileDropMode.DIRECTORY);
		tfDisksDest.setEnabled(false);
		tfDisksDest.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tfDisksDest.setText(""); //$NON-NLS-1$
		final GridBagConstraints gbc_tfDisksDest = new GridBagConstraints();
		gbc_tfDisksDest.insets = new Insets(0, 0, 5, 5);
		gbc_tfDisksDest.fill = GridBagConstraints.BOTH;
		gbc_tfDisksDest.gridx = 1;
		gbc_tfDisksDest.gridy = 1;
		scannerDirectories.add(tfDisksDest, gbc_tfDisksDest);
		tfDisksDest.setColumns(10);

		btDisksDest = new JButton(""); //$NON-NLS-1$
		btDisksDest.setEnabled(false);
		btDisksDest.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		final GridBagConstraints gbc_btDisksDest = new GridBagConstraints();
		gbc_btDisksDest.insets = new Insets(0, 0, 5, 0);
		gbc_btDisksDest.gridx = 2;
		gbc_btDisksDest.gridy = 1;
		btDisksDest.addActionListener(e -> {
			final File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
			new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(Profile.curr_profile.getProperty("MainFrame.ChooseDisksDestination", workdir.getAbsolutePath())), new File(tfDisksDest.getText()), null, Messages.getString("MainFrame.ChooseDisksDestination"), false).show(MainFrame.this, chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				Profile.curr_profile.setProperty("MainFrame.ChooseDisksDestination", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfDisksDest.setText(chooser.getSelectedFile().getAbsolutePath());
				Profile.curr_profile.setProperty("disks_dest_dir", tfDisksDest.getText()); //$NON-NLS-1$
				return null;
			});
		});
		scannerDirectories.add(btDisksDest, gbc_btDisksDest);

		lblSWDest = new JCheckBox(Messages.getString("MainFrame.chckbxSoftwareDest.text")); //$NON-NLS-1$
		lblSWDest.addItemListener(e -> {
			tfSWDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btnSWDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			Profile.curr_profile.setProperty("swroms_dest_dir_enabled", e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		lblSWDest.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbc_lblSWDest = new GridBagConstraints();
		gbc_lblSWDest.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSWDest.insets = new Insets(0, 0, 5, 5);
		gbc_lblSWDest.gridx = 0;
		gbc_lblSWDest.gridy = 2;
		scannerDirectories.add(lblSWDest, gbc_lblSWDest);

		tfSWDest = new JFileDropTextField(txt -> Profile.curr_profile.setProperty("swroms_dest_dir", txt)); //$NON-NLS-1$
		tfSWDest.setMode(JFileDropMode.DIRECTORY);
		tfSWDest.setEnabled(false);
		tfSWDest.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tfSWDest.setText(""); //$NON-NLS-1$
		final GridBagConstraints gbc_tfSWDest = new GridBagConstraints();
		gbc_tfSWDest.insets = new Insets(0, 0, 5, 5);
		gbc_tfSWDest.fill = GridBagConstraints.BOTH;
		gbc_tfSWDest.gridx = 1;
		gbc_tfSWDest.gridy = 2;
		scannerDirectories.add(tfSWDest, gbc_tfSWDest);
		tfSWDest.setColumns(10);

		btnSWDest = new JButton(""); //$NON-NLS-1$
		btnSWDest.addActionListener(e -> {
			final File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
			new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(Profile.curr_profile.getProperty("MainFrame.ChooseSWRomsDestination", workdir.getAbsolutePath())), new File(tfSWDest.getText()), null, Messages.getString("MainFrame.ChooseSWRomsDestination"), false).show(MainFrame.this, chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				Profile.curr_profile.setProperty("MainFrame.ChooseSWRomsDestination", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfSWDest.setText(chooser.getSelectedFile().getAbsolutePath());
				Profile.curr_profile.setProperty("swroms_dest_dir", tfSWDest.getText()); //$NON-NLS-1$
				return null;
			});
		});
		btnSWDest.setEnabled(false);
		btnSWDest.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		final GridBagConstraints gbc_btnSWDest = new GridBagConstraints();
		gbc_btnSWDest.insets = new Insets(0, 0, 5, 0);
		gbc_btnSWDest.gridx = 2;
		gbc_btnSWDest.gridy = 2;
		scannerDirectories.add(btnSWDest, gbc_btnSWDest);

		lblSWDisksDest = new JCheckBox(Messages.getString("MainFrame.chckbxSwdisksdest.text")); //$NON-NLS-1$
		lblSWDisksDest.addItemListener(e -> {
			tfSWDisksDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btSWDisksDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			Profile.curr_profile.setProperty("swdisks_dest_dir_enabled", e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		final GridBagConstraints gbc_lblSWDisksDest = new GridBagConstraints();
		gbc_lblSWDisksDest.insets = new Insets(0, 0, 5, 5);
		gbc_lblSWDisksDest.gridx = 0;
		gbc_lblSWDisksDest.gridy = 3;
		scannerDirectories.add(lblSWDisksDest, gbc_lblSWDisksDest);

		tfSWDisksDest = new JFileDropTextField(txt -> Profile.curr_profile.setProperty("swdisks_dest_dir", txt)); //$NON-NLS-1$
		tfSWDisksDest.setMode(JFileDropMode.DIRECTORY);
		tfSWDisksDest.setEnabled(false);
		tfSWDisksDest.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tfSWDisksDest.setText(""); //$NON-NLS-1$
		final GridBagConstraints gbc_tfSWDisksDest = new GridBagConstraints();
		gbc_tfSWDisksDest.insets = new Insets(0, 0, 5, 5);
		gbc_tfSWDisksDest.fill = GridBagConstraints.BOTH;
		gbc_tfSWDisksDest.gridx = 1;
		gbc_tfSWDisksDest.gridy = 3;
		scannerDirectories.add(tfSWDisksDest, gbc_tfSWDisksDest);
		tfSWDisksDest.setColumns(10);

		btSWDisksDest = new JButton(""); //$NON-NLS-1$
		btSWDisksDest.addActionListener(e -> {
			final File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
			new JRMFileChooser<Boolean>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(Profile.curr_profile.getProperty("MainFrame.ChooseSWDisksDestination", workdir.getAbsolutePath())), new File(tfSWDisksDest.getText()), null, Messages.getString("MainFrame.ChooseSWDisksDestination"), false).show(MainFrame.this, chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				Profile.curr_profile.setProperty("MainFrame.ChooseSWDisksDestination", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfSWDisksDest.setText(chooser.getSelectedFile().getAbsolutePath());
				Profile.curr_profile.setProperty("swdisks_dest_dir", tfSWDisksDest.getText()); //$NON-NLS-1$
				return true;
			});
		});
		btSWDisksDest.setEnabled(false);
		btSWDisksDest.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		final GridBagConstraints gbc_btSWDisksDest = new GridBagConstraints();
		gbc_btSWDisksDest.insets = new Insets(0, 0, 5, 0);
		gbc_btSWDisksDest.gridx = 2;
		gbc_btSWDisksDest.gridy = 3;
		scannerDirectories.add(btSWDisksDest, gbc_btSWDisksDest);

		lblSamplesDest = new JCheckBox(Messages.getString("MainFrame.lblSamplesDest.text")); //$NON-NLS-1$
		lblSamplesDest.addItemListener(e -> {
			tfSamplesDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			btSamplesDest.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			Profile.curr_profile.setProperty("samples_dest_dir_enabled", e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		});
		lblSamplesDest.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbc_lblSamplesDest = new GridBagConstraints();
		gbc_lblSamplesDest.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSamplesDest.insets = new Insets(0, 0, 5, 5);
		gbc_lblSamplesDest.gridx = 0;
		gbc_lblSamplesDest.gridy = 4;
		scannerDirectories.add(lblSamplesDest, gbc_lblSamplesDest);

		tfSamplesDest = new JFileDropTextField(txt -> Profile.curr_profile.setProperty("samples_dest_dir", txt)); //$NON-NLS-1$
		tfSamplesDest.setMode(JFileDropMode.DIRECTORY);
		tfSamplesDest.setEnabled(false);
		tfSamplesDest.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tfSamplesDest.setText(""); //$NON-NLS-1$
		final GridBagConstraints gbc_tfSamplesDest = new GridBagConstraints();
		gbc_tfSamplesDest.insets = new Insets(0, 0, 5, 5);
		gbc_tfSamplesDest.fill = GridBagConstraints.BOTH;
		gbc_tfSamplesDest.gridx = 1;
		gbc_tfSamplesDest.gridy = 4;
		scannerDirectories.add(tfSamplesDest, gbc_tfSamplesDest);
		tfSamplesDest.setColumns(10);

		btSamplesDest = new JButton(""); //$NON-NLS-1$
		btSamplesDest.addActionListener(e -> {
			final File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
			new JRMFileChooser<Boolean>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(Profile.curr_profile.getProperty("MainFrame.ChooseSamplesDestination", workdir.getAbsolutePath())), new File(tfSamplesDest.getText()), null, Messages.getString("MainFrame.ChooseSamplesDestination"), false).show(MainFrame.this, chooser -> { //$NON-NLS-1$//$NON-NLS-2$
				Profile.curr_profile.setProperty("MainFrame.ChooseSamplesDestination", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				tfSamplesDest.setText(chooser.getSelectedFile().getAbsolutePath());
				Profile.curr_profile.setProperty("samples_dest_dir", tfSamplesDest.getText()); //$NON-NLS-1$
				return true;
			});
		});
		btSamplesDest.setEnabled(false);
		btSamplesDest.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		final GridBagConstraints gbc_btSamplesDest = new GridBagConstraints();
		gbc_btSamplesDest.insets = new Insets(0, 0, 5, 0);
		gbc_btSamplesDest.gridx = 2;
		gbc_btSamplesDest.gridy = 4;
		scannerDirectories.add(btSamplesDest, gbc_btSamplesDest);

		lblSrcDir = new JLabel(Messages.getString("MainFrame.lblSrcDir.text")); //$NON-NLS-1$
		lblSrcDir.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbc_lblSrcDir = new GridBagConstraints();
		gbc_lblSrcDir.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSrcDir.anchor = GridBagConstraints.NORTH;
		gbc_lblSrcDir.insets = new Insets(0, 0, 0, 5);
		gbc_lblSrcDir.gridx = 0;
		gbc_lblSrcDir.gridy = 5;
		scannerDirectories.add(lblSrcDir, gbc_lblSrcDir);

		listSrcDir = new JFileDropList(files -> Profile.curr_profile.setProperty("src_dir", String.join("|", files.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList())))); // $NON-NLS-1$ //$NON-NLS-1$ //$NON-NLS-2$
																																															// $NON-NLS-2$
		listSrcDir.setMode(JFileDropMode.DIRECTORY);
		listSrcDir.setUI(new JListHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		final GridBagConstraints gbc_listSrcDir = new GridBagConstraints();
		gbc_listSrcDir.gridwidth = 2;
		gbc_listSrcDir.fill = GridBagConstraints.BOTH;
		gbc_listSrcDir.gridx = 1;
		gbc_listSrcDir.gridy = 5;
		scannerDirectories.add(listSrcDir, gbc_listSrcDir);
		listSrcDir.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		popupMenu = new JPopupMenu();
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
		mntmDeleteSelected.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/folder_delete.png"))); //$NON-NLS-1$
		popupMenu.add(mntmDeleteSelected);

		mntmAddDirectory = new JMenuItem(Messages.getString("MainFrame.mntmAddDirectory.text")); //$NON-NLS-1$
		mntmAddDirectory.addActionListener(e -> {
			final File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
			new JRMFileChooser<Boolean>(JFileChooser.OPEN_DIALOG, JFileChooser.DIRECTORIES_ONLY, new File(Profile.curr_profile.getProperty("MainFrame.ChooseRomsSource", workdir.getAbsolutePath())), null, null, null, true).show(MainFrame.this, chooser -> { //$NON-NLS-1$
				Profile.curr_profile.setProperty("MainFrame.ChooseRomsSource", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
				listSrcDir.add(chooser.getSelectedFiles());
				return true;
			});
		});
		mntmAddDirectory.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/folder_add.png"))); //$NON-NLS-1$
		popupMenu.add(mntmAddDirectory);

		scannerSettingsPanel = new JPanel();
		scannerCfgTab.addTab(Messages.getString("MainFrame.scannerSettingsPanel.title"), null, scannerSettingsPanel, null); //$NON-NLS-1$
		scannerSettingsPanel.setBackground(UIManager.getColor("Panel.background")); //$NON-NLS-1$
		final GridBagLayout gbl_scannerSettingsPanel = new GridBagLayout();
		gbl_scannerSettingsPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_scannerSettingsPanel.rowHeights = new int[] { 20, 20, 0, 0, 0, 20, 0 };
		gbl_scannerSettingsPanel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_scannerSettingsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		scannerSettingsPanel.setLayout(gbl_scannerSettingsPanel);

		chckbxNeedSHA1 = new JCheckBox(Messages.getString("MainFrame.chckbxNeedSHA1.text")); //$NON-NLS-1$
		chckbxNeedSHA1.addItemListener(e -> Profile.curr_profile.setProperty("need_sha1_or_md5", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		chckbxNeedSHA1.setToolTipText(Messages.getString("MainFrame.chckbxNeedSHA1.toolTipText")); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxNeedSHA1 = new GridBagConstraints();
		gbc_chckbxNeedSHA1.fill = GridBagConstraints.BOTH;
		gbc_chckbxNeedSHA1.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNeedSHA1.gridx = 0;
		gbc_chckbxNeedSHA1.gridy = 0;
		scannerSettingsPanel.add(chckbxNeedSHA1, gbc_chckbxNeedSHA1);

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
		scannerSettingsPanel.add(chckbxCreateMissingSets, gbc_chckbxCreateMissingSets);
		chckbxUseParallelism.setToolTipText(Messages.getString("MainFrame.chckbxUseParallelism.toolTipText")); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxUseParallelism = new GridBagConstraints();
		gbc_chckbxUseParallelism.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxUseParallelism.fill = GridBagConstraints.BOTH;
		gbc_chckbxUseParallelism.gridx = 0;
		gbc_chckbxUseParallelism.gridy = 1;
		scannerSettingsPanel.add(chckbxUseParallelism, gbc_chckbxUseParallelism);

		chckbxCreateOnlyComplete = new JCheckBox(Messages.getString("MainFrame.chckbxCreateOnlyComplete.text")); //$NON-NLS-1$
		chckbxCreateOnlyComplete.addItemListener(e -> Profile.curr_profile.setProperty("createfull_mode", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxCreateOnlyComplete = new GridBagConstraints();
		gbc_chckbxCreateOnlyComplete.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxCreateOnlyComplete.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxCreateOnlyComplete.gridx = 1;
		gbc_chckbxCreateOnlyComplete.gridy = 1;
		scannerSettingsPanel.add(chckbxCreateOnlyComplete, gbc_chckbxCreateOnlyComplete);

		chckbxIgnoreUnneededContainers = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreUnneededContainers.text")); //$NON-NLS-1$
		chckbxIgnoreUnneededContainers.addItemListener(e -> Profile.curr_profile.setProperty("ignore_unneeded_containers", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxIgnoreUnneededContainers = new GridBagConstraints();
		gbc_chckbxIgnoreUnneededContainers.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIgnoreUnneededContainers.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIgnoreUnneededContainers.gridx = 0;
		gbc_chckbxIgnoreUnneededContainers.gridy = 2;
		scannerSettingsPanel.add(chckbxIgnoreUnneededContainers, gbc_chckbxIgnoreUnneededContainers);

		chckbxIgnoreUnneededEntries = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreUnneededEntries.text")); //$NON-NLS-1$
		chckbxIgnoreUnneededEntries.addItemListener(e -> Profile.curr_profile.setProperty("ignore_unneeded_entries", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxIgnoreUnneededEntries = new GridBagConstraints();
		gbc_chckbxIgnoreUnneededEntries.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIgnoreUnneededEntries.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxIgnoreUnneededEntries.gridx = 1;
		gbc_chckbxIgnoreUnneededEntries.gridy = 2;
		scannerSettingsPanel.add(chckbxIgnoreUnneededEntries, gbc_chckbxIgnoreUnneededEntries);

		chckbxIgnoreUnknownContainers = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreUnknownContainers.text")); //$NON-NLS-1$
		chckbxIgnoreUnknownContainers.addItemListener(e -> Profile.curr_profile.setProperty("ignore_unknown_containers", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxIgnoreUnknownContainers = new GridBagConstraints();
		gbc_chckbxIgnoreUnknownContainers.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIgnoreUnknownContainers.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIgnoreUnknownContainers.gridx = 0;
		gbc_chckbxIgnoreUnknownContainers.gridy = 3;
		scannerSettingsPanel.add(chckbxIgnoreUnknownContainers, gbc_chckbxIgnoreUnknownContainers);

		chckbxUseImplicitMerge = new JCheckBox(Messages.getString("MainFrame.chckbxUseImplicitMerge.text")); //$NON-NLS-1$
		chckbxUseImplicitMerge.addItemListener(e -> Profile.curr_profile.setProperty("implicit_merge", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxUseImplicitMerge = new GridBagConstraints();
		gbc_chckbxUseImplicitMerge.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxUseImplicitMerge.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxUseImplicitMerge.gridx = 1;
		gbc_chckbxUseImplicitMerge.gridy = 3;
		scannerSettingsPanel.add(chckbxUseImplicitMerge, gbc_chckbxUseImplicitMerge);
		
		chckbxIgnoreMergeNameRoms = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreMergeName.text")); //$NON-NLS-1$
		chckbxIgnoreMergeNameRoms.addItemListener(e -> Profile.curr_profile.setProperty("ignore_merge_name_roms", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_chckbxIgnoreMergeNameRoms = new GridBagConstraints();
		gbc_chckbxIgnoreMergeNameRoms.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIgnoreMergeNameRoms.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIgnoreMergeNameRoms.gridx = 0;
		gbc_chckbxIgnoreMergeNameRoms.gridy = 4;
		scannerSettingsPanel.add(chckbxIgnoreMergeNameRoms, gbc_chckbxIgnoreMergeNameRoms);
		
		chckbxIgnoreMergeNameDisks = new JCheckBox(Messages.getString("MainFrame.chckbxIgnoreMergeName_1.text")); //$NON-NLS-1$
		chckbxIgnoreMergeNameDisks.addItemListener(e -> Profile.curr_profile.setProperty("ignore_merge_name_disks", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$
		GridBagConstraints gbc_chckbxIgnoreMergeNameDisks = new GridBagConstraints();
		gbc_chckbxIgnoreMergeNameDisks.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIgnoreMergeNameDisks.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxIgnoreMergeNameDisks.gridx = 1;
		gbc_chckbxIgnoreMergeNameDisks.gridy = 4;
		scannerSettingsPanel.add(chckbxIgnoreMergeNameDisks, gbc_chckbxIgnoreMergeNameDisks);

		scannerSubSettingsPanel = new JPanel();
		final GridBagConstraints gbc_scannerSubSettingsPanel = new GridBagConstraints();
		gbc_scannerSubSettingsPanel.gridwidth = 2;
		gbc_scannerSubSettingsPanel.fill = GridBagConstraints.BOTH;
		gbc_scannerSubSettingsPanel.gridx = 0;
		gbc_scannerSubSettingsPanel.gridy = 5;
		scannerSettingsPanel.add(scannerSubSettingsPanel, gbc_scannerSubSettingsPanel);
		final GridBagLayout gbl_scannerSubSettingsPanel = new GridBagLayout();
		gbl_scannerSubSettingsPanel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_scannerSubSettingsPanel.rowHeights = new int[] { 0, 0, 0, 8, 100, 0 };
		gbl_scannerSubSettingsPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_scannerSubSettingsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		scannerSubSettingsPanel.setLayout(gbl_scannerSubSettingsPanel);

		lblCompression = new JLabel(Messages.getString("MainFrame.lblCompression.text")); //$NON-NLS-1$
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

		lblMergeMode = new JLabel(Messages.getString("MainFrame.lblMergeMode.text")); //$NON-NLS-1$
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

		lblHashCollision = new JLabel(Messages.getString("MainFrame.lblHashCollision.text")); //$NON-NLS-1$
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

		scannerFilters = new JSplitPane();
		scannerFilters.setResizeWeight(0.5);
		scannerFilters.setOneTouchExpandable(true);
		scannerFilters.setContinuousLayout(true);
		scannerCfgTab.addTab(Messages.getString("MainFrame.Filters"), null, scannerFilters, null); //$NON-NLS-1$

		scrollPane_2 = new JScrollPane();
		scannerFilters.setRightComponent(scrollPane_2);
		scrollPane_2.setViewportBorder(new TitledBorder(null, Messages.getString("MainFrame.scrollPane_2.viewportBorderTitle"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$

		checkBoxListSystems = new JCheckBoxList<>();
		checkBoxListSystems.setCellRenderer(checkBoxListSystems.new CellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(final JList<? extends Systm> list, final Systm value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				final JCheckBox checkbox = (JCheckBox) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				checkbox.setSelected(value.isSelected());
				return checkbox;
			}
		});
		checkBoxListSystems.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting())
			{
				if (e.getFirstIndex() != -1)
				{
					for (int index = e.getFirstIndex(); index <= e.getLastIndex(); index++)
						checkBoxListSystems.getModel().getElementAt(index).setSelected(checkBoxListSystems.isSelectedIndex(index));
					if (MainFrame.profile_viewer != null)
						MainFrame.profile_viewer.reset(Profile.curr_profile);
				}
			}
		});
		scrollPane_2.setViewportView(checkBoxListSystems);

		popupMenu_3 = new JPopupMenu();
		MainFrame.addPopup(checkBoxListSystems, popupMenu_3);

		mnSelect = new JMenu(Messages.getString("MainFrame.mnSelect.text")); //$NON-NLS-1$
		popupMenu_3.add(mnSelect);

		mntmSelectAll = new JMenuItem(Messages.getString("MainFrame.mntmSelectAll.text")); //$NON-NLS-1$
		mnSelect.add(mntmSelectAll);

		mntmSelectAllBios = new JMenuItem(Messages.getString("MainFrame.mntmAllBios.text")); //$NON-NLS-1$
		mntmSelectAllBios.addActionListener(e -> checkBoxListSystems.select(sys -> sys.getType() == Systm.Type.BIOS, true));
		mnSelect.add(mntmSelectAllBios);

		mntmSelectAllSoftwares = new JMenuItem(Messages.getString("MainFrame.mntmAllSoftwares.text")); //$NON-NLS-1$
		mntmSelectAllSoftwares.addActionListener(e -> checkBoxListSystems.select(sys -> sys.getType() == Systm.Type.SOFTWARELIST, true));
		mnSelect.add(mntmSelectAllSoftwares);

		mnUnselect = new JMenu(Messages.getString("MainFrame.mnUnselect.text")); //$NON-NLS-1$
		popupMenu_3.add(mnUnselect);

		mntmUnselectAll = new JMenuItem(Messages.getString("MainFrame.mntmSelectNone.text")); //$NON-NLS-1$
		mnUnselect.add(mntmUnselectAll);

		mntmUnselectAllBios = new JMenuItem(Messages.getString("MainFrame.mntmAllBios.text")); //$NON-NLS-1$
		mntmUnselectAllBios.addActionListener(e -> checkBoxListSystems.select(sys -> sys.getType() == Systm.Type.BIOS, false));
		mnUnselect.add(mntmUnselectAllBios);

		mntmUnselectAllSoftwares = new JMenuItem(Messages.getString("MainFrame.mntmAllSoftwares.text")); //$NON-NLS-1$
		mntmUnselectAllSoftwares.addActionListener(e -> checkBoxListSystems.select(sys -> sys.getType() == Systm.Type.SOFTWARELIST, false));
		mnUnselect.add(mntmUnselectAllSoftwares);

		mntmInvertSelection = new JMenuItem(Messages.getString("MainFrame.mntmInvertSelection.text")); //$NON-NLS-1$
		mntmInvertSelection.addActionListener(e -> checkBoxListSystems.selectInvert());
		popupMenu_3.add(mntmInvertSelection);
		mntmUnselectAll.addActionListener(e -> checkBoxListSystems.selectNone());
		mntmSelectAll.addActionListener(e -> checkBoxListSystems.selectAll());

		panel_1 = new JPanel();
		scannerFilters.setLeftComponent(panel_1);
		final GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 20, 100, 0, 100, 20, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, 1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		chckbxIncludeClones = new JCheckBox(Messages.getString("MainFrame.chckbxIncludeClones.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxIncludeClones = new GridBagConstraints();
		gbc_chckbxIncludeClones.gridwidth = 3;
		gbc_chckbxIncludeClones.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIncludeClones.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIncludeClones.anchor = GridBagConstraints.NORTH;
		gbc_chckbxIncludeClones.gridx = 1;
		gbc_chckbxIncludeClones.gridy = 1;
		panel_1.add(chckbxIncludeClones, gbc_chckbxIncludeClones);

		chckbxIncludeDisks = new JCheckBox(Messages.getString("MainFrame.chckbxIncludeDisks.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxIncludeDisks = new GridBagConstraints();
		gbc_chckbxIncludeDisks.gridwidth = 3;
		gbc_chckbxIncludeDisks.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIncludeDisks.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIncludeDisks.gridx = 1;
		gbc_chckbxIncludeDisks.gridy = 2;
		panel_1.add(chckbxIncludeDisks, gbc_chckbxIncludeDisks);

		chckbxIncludeSamples = new JCheckBox(Messages.getString("MainFrame.chckbxIncludeSamples.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxIncludeSamples = new GridBagConstraints();
		gbc_chckbxIncludeSamples.gridwidth = 3;
		gbc_chckbxIncludeSamples.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIncludeSamples.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIncludeSamples.gridx = 1;
		gbc_chckbxIncludeSamples.gridy = 3;
		panel_1.add(chckbxIncludeSamples, gbc_chckbxIncludeSamples);
		chckbxIncludeSamples.setSelected(true);

		lblCabinetType = new JLabel(Messages.getString("MainFrame.lblMachineType.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblCabinetType = new GridBagConstraints();
		gbc_lblCabinetType.gridwidth = 2;
		gbc_lblCabinetType.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblCabinetType.insets = new Insets(0, 0, 5, 5);
		gbc_lblCabinetType.gridx = 1;
		gbc_lblCabinetType.gridy = 4;
		panel_1.add(lblCabinetType, gbc_lblCabinetType);
		lblCabinetType.setHorizontalAlignment(SwingConstants.TRAILING);

		cbbxFilterCabinetType = new JComboBox<>();
		final GridBagConstraints gbc_cbbxFilterCabinetType = new GridBagConstraints();
		gbc_cbbxFilterCabinetType.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxFilterCabinetType.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxFilterCabinetType.gridx = 3;
		gbc_cbbxFilterCabinetType.gridy = 4;
		panel_1.add(cbbxFilterCabinetType, gbc_cbbxFilterCabinetType);
		cbbxFilterCabinetType.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				Profile.curr_profile.setProperty("filter.CabinetType", e.getItem().toString()); //$NON-NLS-1$
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.reset(Profile.curr_profile);
			}
		});
		cbbxFilterCabinetType.setModel(new DefaultComboBoxModel<>(CabinetType.values()));

		lblDisplayOrientation = new JLabel(Messages.getString("MainFrame.lblOrientation.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblDisplayOrientation = new GridBagConstraints();
		gbc_lblDisplayOrientation.gridwidth = 2;
		gbc_lblDisplayOrientation.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDisplayOrientation.insets = new Insets(0, 0, 5, 5);
		gbc_lblDisplayOrientation.gridx = 1;
		gbc_lblDisplayOrientation.gridy = 5;
		panel_1.add(lblDisplayOrientation, gbc_lblDisplayOrientation);
		lblDisplayOrientation.setHorizontalAlignment(SwingConstants.TRAILING);

		cbbxFilterDisplayOrientation = new JComboBox<>();
		final GridBagConstraints gbc_cbbxFilterDisplayOrientation = new GridBagConstraints();
		gbc_cbbxFilterDisplayOrientation.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxFilterDisplayOrientation.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxFilterDisplayOrientation.gridx = 3;
		gbc_cbbxFilterDisplayOrientation.gridy = 5;
		panel_1.add(cbbxFilterDisplayOrientation, gbc_cbbxFilterDisplayOrientation);
		cbbxFilterDisplayOrientation.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				Profile.curr_profile.setProperty("filter.DisplayOrientation", e.getItem().toString()); //$NON-NLS-1$
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.reset(Profile.curr_profile);
			}
		});
		cbbxFilterDisplayOrientation.setModel(new DefaultComboBoxModel<>(DisplayOrientation.values()));

		lblDriverStatus = new JLabel(Messages.getString("MainFrame.lblDriverStatus.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblDriverStatus = new GridBagConstraints();
		gbc_lblDriverStatus.gridwidth = 2;
		gbc_lblDriverStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDriverStatus.insets = new Insets(0, 0, 5, 5);
		gbc_lblDriverStatus.gridx = 1;
		gbc_lblDriverStatus.gridy = 6;
		panel_1.add(lblDriverStatus, gbc_lblDriverStatus);
		lblDriverStatus.setHorizontalAlignment(SwingConstants.TRAILING);

		cbbxDriverStatus = new JComboBox<>();
		final GridBagConstraints gbc_cbbxDriverStatus = new GridBagConstraints();
		gbc_cbbxDriverStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxDriverStatus.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxDriverStatus.gridx = 3;
		gbc_cbbxDriverStatus.gridy = 6;
		panel_1.add(cbbxDriverStatus, gbc_cbbxDriverStatus);
		cbbxDriverStatus.setModel(new DefaultComboBoxModel<>(Driver.StatusType.values()));

		lblSwMinSupportedLvl = new JLabel(Messages.getString("MainFrame.lblSwMinSupport.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblSwMinSupportedLvl = new GridBagConstraints();
		gbc_lblSwMinSupportedLvl.gridwidth = 2;
		gbc_lblSwMinSupportedLvl.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSwMinSupportedLvl.insets = new Insets(0, 0, 5, 5);
		gbc_lblSwMinSupportedLvl.gridx = 1;
		gbc_lblSwMinSupportedLvl.gridy = 7;
		panel_1.add(lblSwMinSupportedLvl, gbc_lblSwMinSupportedLvl);
		lblSwMinSupportedLvl.setHorizontalAlignment(SwingConstants.TRAILING);

		cbbxSWMinSupportedLvl = new JComboBox<>();
		final GridBagConstraints gbc_cbbxSWMinSupportedLvl = new GridBagConstraints();
		gbc_cbbxSWMinSupportedLvl.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxSWMinSupportedLvl.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxSWMinSupportedLvl.gridx = 3;
		gbc_cbbxSWMinSupportedLvl.gridy = 7;
		panel_1.add(cbbxSWMinSupportedLvl, gbc_cbbxSWMinSupportedLvl);
		cbbxSWMinSupportedLvl.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				Profile.curr_profile.setProperty("filter.MinSoftwareSupportedLevel", e.getItem().toString()); //$NON-NLS-1$
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.reset(Profile.curr_profile);
			}
		});
		cbbxSWMinSupportedLvl.setModel(new DefaultComboBoxModel<>(Supported.values()));
		cbbxSWMinSupportedLvl.setSelectedIndex(0);

		cbbxYearMin = new JComboBox<>();
		cbbxYearMin.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				Profile.curr_profile.setProperty("filter.YearMin", e.getItem().toString()); //$NON-NLS-1$
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.reset(Profile.curr_profile);
			}
		});
		final GridBagConstraints gbc_cbbxYearMin = new GridBagConstraints();
		gbc_cbbxYearMin.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxYearMin.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxYearMin.gridx = 1;
		gbc_cbbxYearMin.gridy = 8;
		panel_1.add(cbbxYearMin, gbc_cbbxYearMin);

		lblYear = new JLabel(Messages.getString("MainFrame.lblYear.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblYear = new GridBagConstraints();
		gbc_lblYear.insets = new Insets(0, 0, 5, 5);
		gbc_lblYear.gridx = 2;
		gbc_lblYear.gridy = 8;
		panel_1.add(lblYear, gbc_lblYear);
		lblYear.setHorizontalAlignment(SwingConstants.CENTER);

		cbbxYearMax = new JComboBox<>();
		cbbxYearMax.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				Profile.curr_profile.setProperty("filter.YearMax", e.getItem().toString()); //$NON-NLS-1$
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.reset(Profile.curr_profile);
			}
		});
		final GridBagConstraints gbc_cbbxYearMax = new GridBagConstraints();
		gbc_cbbxYearMax.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxYearMax.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxYearMax.gridx = 3;
		gbc_cbbxYearMax.gridy = 8;
		panel_1.add(cbbxYearMax, gbc_cbbxYearMax);
		cbbxDriverStatus.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				Profile.curr_profile.setProperty("filter.DriverStatus", e.getItem().toString()); //$NON-NLS-1$
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.reset(Profile.curr_profile);
			}
		});
		chckbxIncludeDisks.addItemListener(e -> {
			Profile.curr_profile.setProperty("filter.InclDisks", e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
			if (MainFrame.profile_viewer != null)
				MainFrame.profile_viewer.reset(Profile.curr_profile);
		});
		chckbxIncludeClones.addItemListener(e -> {
			Profile.curr_profile.setProperty("filter.InclClones", e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
			if (MainFrame.profile_viewer != null)
				MainFrame.profile_viewer.reset(Profile.curr_profile);
		});
		chckbxIncludeSamples.addItemListener(e -> {
			Profile.curr_profile.setProperty("filter.InclSamples", e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
			if (MainFrame.profile_viewer != null)
				MainFrame.profile_viewer.reset(Profile.curr_profile);
		});

		scannerAdvFilters = new JPanel();
		scannerCfgTab.addTab(Messages.getString("MainFrame.AdvFilters"), null, scannerAdvFilters, null); //$NON-NLS-1$
		final GridBagLayout gbl_scannerAdvFilters = new GridBagLayout();
		gbl_scannerAdvFilters.columnWidths = new int[] { 0, 0, 0 };
		gbl_scannerAdvFilters.rowHeights = new int[] { 0, 0, 0 };
		gbl_scannerAdvFilters.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_scannerAdvFilters.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		scannerAdvFilters.setLayout(gbl_scannerAdvFilters);

		tfNPlayers = new JFileDropTextField(txt -> {
			Profile.curr_profile.setProperty("filter.nplayers.ini", txt); //$NON-NLS-1$
			Profile.curr_profile.loadNPlayers();
			Profile.curr_profile.saveSettings();
			listNPlayers.setModel(Profile.curr_profile.nplayers != null ? Profile.curr_profile.nplayers : new DefaultListModel<>());
		});
		tfNPlayers.setMode(JFileDropMode.FILE);
		tfNPlayers.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropNPlayersIniHere"), Color.gray)); //$NON-NLS-1$
		tfNPlayers.setEditable(false);
		final GridBagConstraints gbc_tfNPlayers = new GridBagConstraints();
		gbc_tfNPlayers.insets = new Insets(0, 0, 5, 5);
		gbc_tfNPlayers.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfNPlayers.gridx = 0;
		gbc_tfNPlayers.gridy = 0;
		scannerAdvFilters.add(tfNPlayers, gbc_tfNPlayers);

		tfCatVer = new JFileDropTextField(txt -> {
			Profile.curr_profile.setProperty("filter.catver.ini", txt); //$NON-NLS-1$
			Profile.curr_profile.loadCatVer();
			Profile.curr_profile.saveSettings();
			treeCatVer.setModel(Profile.curr_profile.catver != null ? new CatVerModel(Profile.curr_profile.catver) : new CatVerModel());
		});
		tfCatVer.setMode(JFileDropMode.FILE);
		tfCatVer.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropCatVerIniHere"), Color.gray)); //$NON-NLS-1$
		tfCatVer.setEditable(false);
		final GridBagConstraints gbc_tfCatVer = new GridBagConstraints();
		gbc_tfCatVer.insets = new Insets(0, 0, 5, 0);
		gbc_tfCatVer.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfCatVer.gridx = 1;
		gbc_tfCatVer.gridy = 0;
		scannerAdvFilters.add(tfCatVer, gbc_tfCatVer);

		scrollPaneNPlayers = new JScrollPane();
		scrollPaneNPlayers.setViewportBorder(new TitledBorder(null, Messages.getString("MainFrame.NPlayers"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		final GridBagConstraints gbc_scrollPaneNPlayers = new GridBagConstraints();
		gbc_scrollPaneNPlayers.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPaneNPlayers.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneNPlayers.gridx = 0;
		gbc_scrollPaneNPlayers.gridy = 1;
		scannerAdvFilters.add(scrollPaneNPlayers, gbc_scrollPaneNPlayers);

		listNPlayers = new JCheckBoxList<>();
		listNPlayers.setCellRenderer(listNPlayers.new CellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(final JList<? extends NPlayer> list, final NPlayer value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				final JCheckBox checkbox = (JCheckBox) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				checkbox.setSelected(value.isSelected());
				return checkbox;
			}
		});
		listNPlayers.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting())
			{
				if (e.getFirstIndex() != -1)
				{
					for (int index = e.getFirstIndex(); index <= e.getLastIndex() && index < listNPlayers.getModel().getSize(); index++)
						listNPlayers.getModel().getElementAt(index).setSelected(listNPlayers.isSelectedIndex(index));
					if (MainFrame.profile_viewer != null)
						MainFrame.profile_viewer.reset(Profile.curr_profile);
				}
			}
		});
		listNPlayers.setEnabled(false);
		scrollPaneNPlayers.setViewportView(listNPlayers);

		popupMenuNPlay = new JPopupMenu();
		MainFrame.addPopup(listNPlayers, popupMenuNPlay);

		mntmSelectAllNPlay = new JMenuItem(Messages.getString("MainFrame.SelectAll")); //$NON-NLS-1$
		mntmSelectAllNPlay.addActionListener(e -> listNPlayers.selectAll());
		popupMenuNPlay.add(mntmSelectAllNPlay);

		mntmSelectNoneNPlay = new JMenuItem(Messages.getString("MainFrame.SelectNone")); //$NON-NLS-1$
		mntmSelectNoneNPlay.addActionListener(e -> listNPlayers.selectNone());
		popupMenuNPlay.add(mntmSelectNoneNPlay);

		mntmInvertSelectionNPlay = new JMenuItem(Messages.getString("MainFrame.InvertSelection")); //$NON-NLS-1$
		mntmInvertSelectionNPlay.addActionListener(e -> listNPlayers.selectInvert());
		popupMenuNPlay.add(mntmInvertSelectionNPlay);

		scrollPaneCatVer = new JScrollPane();
		scrollPaneCatVer.setViewportBorder(new TitledBorder(null, Messages.getString("MainFrame.Categories"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		final GridBagConstraints gbc_scrollPaneCatVer = new GridBagConstraints();
		gbc_scrollPaneCatVer.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneCatVer.gridx = 1;
		gbc_scrollPaneCatVer.gridy = 1;
		scannerAdvFilters.add(scrollPaneCatVer, gbc_scrollPaneCatVer);

		treeCatVer = new JCheckBoxTree(new CatVerModel());
		treeCatVer.addCheckChangeEventListener(event -> {
			Profile.curr_profile.saveSettings();
			if (MainFrame.profile_viewer != null)
				MainFrame.profile_viewer.reset(Profile.curr_profile);
		});
		treeCatVer.setEnabled(false);
		scrollPaneCatVer.setViewportView(treeCatVer);

		popupMenuCat = new JPopupMenu();
		popupMenuCat.addPopupMenuListener(new PopupMenuListener()
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
			}
		});
		MainFrame.addPopup(treeCatVer, popupMenuCat);

		mnSelectCat = new JMenu(Messages.getString("MainFrame.Select")); //$NON-NLS-1$
		popupMenuCat.add(mnSelectCat);

		mntmSelectAllCat = new JMenuItem(Messages.getString("MainFrame.All")); //$NON-NLS-1$
		mntmSelectAllCat.addActionListener(e -> treeCatVer.selectAll());
		mnSelectCat.add(mntmSelectAllCat);

		mntmSelectMatureCat = new JMenuItem(Messages.getString("MainFrame.Mature")); //$NON-NLS-1$
		mntmSelectMatureCat.addActionListener(e -> {
			final List<NGTreeNode> mature_nodes = new ArrayList<>();
			for (final Category cat : Profile.curr_profile.catver)
			{
				if (cat.name.endsWith("* Mature *")) //$NON-NLS-1$
					mature_nodes.add(cat);
				else
					for (final SubCategory subcat : cat)
						if (subcat.name.endsWith("* Mature *")) //$NON-NLS-1$
							mature_nodes.add(subcat);
			}
			treeCatVer.select(mature_nodes.toArray(new NGTreeNode[0]));
		});
		mnSelectCat.add(mntmSelectMatureCat);

		mnUnselectCat = new JMenu(Messages.getString("MainFrame.Unselect")); //$NON-NLS-1$
		popupMenuCat.add(mnUnselectCat);

		mntmUnselectAllCat = new JMenuItem(Messages.getString("MainFrame.All")); //$NON-NLS-1$
		mntmUnselectAllCat.addActionListener(e -> treeCatVer.selectNone());
		mnUnselectCat.add(mntmUnselectAllCat);

		mntmUnselectMatureCat = new JMenuItem(Messages.getString("MainFrame.Mature")); //$NON-NLS-1$
		mntmUnselectMatureCat.addActionListener(e -> {
			final List<NGTreeNode> mature_nodes = new ArrayList<>();
			for (final Category cat : Profile.curr_profile.catver)
			{
				if (cat.name.endsWith("* Mature *")) //$NON-NLS-1$
					mature_nodes.add(cat);
				else
					for (final SubCategory subcat : cat)
						if (subcat.name.endsWith("* Mature *")) //$NON-NLS-1$
							mature_nodes.add(subcat);
			}
			treeCatVer.unselect(mature_nodes.toArray(new NGTreeNode[0]));
		});
		mnUnselectCat.add(mntmUnselectMatureCat);

		lblProfileinfo = new JLabel(""); //$NON-NLS-1$
		lblProfileinfo.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		final GridBagConstraints gbc_lblProfileinfo = new GridBagConstraints();
		gbc_lblProfileinfo.insets = new Insets(0, 2, 0, 2);
		gbc_lblProfileinfo.fill = GridBagConstraints.BOTH;
		gbc_lblProfileinfo.gridx = 0;
		gbc_lblProfileinfo.gridy = 2;
		scannerTab.add(lblProfileinfo, gbc_lblProfileinfo);

		settingsTab = new JPanel();
		mainPane.addTab(Messages.getString("MainFrame.Settings"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/cog.png")), settingsTab, null); //$NON-NLS-1$ //$NON-NLS-2$
		settingsTab.setLayout(new BorderLayout(0, 0));

		settingsPane = new JTabbedPane(SwingConstants.TOP);
		settingsTab.add(settingsPane);

		compressors = new JPanel();
		settingsPane.addTab(Messages.getString("MainFrame.Compressors"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/compress.png")), compressors, null); //$NON-NLS-1$ //$NON-NLS-2$
		settingsPane.setEnabledAt(0, true);
		compressors.setLayout(new BorderLayout(0, 0));

		compressorsPane = new JTabbedPane(SwingConstants.TOP);
		compressors.add(compressorsPane);

		panelZip = new JPanel();
		compressorsPane.addTab("Zip", null, panelZip, null);
		GridBagLayout gbl_panelZip = new GridBagLayout();
		gbl_panelZip.columnWidths = new int[] { 1, 0, 1, 0 };
		gbl_panelZip.rowHeights = new int[] { 0, 20, 20, 0, 0 };
		gbl_panelZip.columnWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panelZip.rowWeights = new double[] { 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelZip.setLayout(gbl_panelZip);

		lblTemporaryFilesThreshold = new JLabel(Messages.getString("MainFrame.lblTemporaryFilesThreshold.text")); //$NON-NLS-1$
		lblTemporaryFilesThreshold.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblTemporaryFilesThreshold = new GridBagConstraints();
		gbc_lblTemporaryFilesThreshold.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblTemporaryFilesThreshold.insets = new Insets(0, 0, 5, 5);
		gbc_lblTemporaryFilesThreshold.gridx = 0;
		gbc_lblTemporaryFilesThreshold.gridy = 1;
		panelZip.add(lblTemporaryFilesThreshold, gbc_lblTemporaryFilesThreshold);

		cbbxZipTempThreshold = new JComboBox<>();
		cbbxZipTempThreshold.setModel(new DefaultComboBoxModel<>(ZipTempThreshold.values()));
		cbbxZipTempThreshold.setSelectedItem(ZipTempThreshold.valueOf(Settings.getProperty("zip_temp_threshold", ZipTempThreshold._10MB.toString())));
		cbbxZipTempThreshold.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Settings.setProperty("zip_temp_threshold", cbbxZipTempThreshold.getSelectedItem().toString());
			}
		});
		cbbxZipTempThreshold.setRenderer(new DefaultListCellRenderer()
		{

			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				setText(((ZipTempThreshold)value).getName());
				return this;
			}
		});
		GridBagConstraints gbc_cbbxZipTempThreshold = new GridBagConstraints();
		gbc_cbbxZipTempThreshold.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxZipTempThreshold.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxZipTempThreshold.gridx = 1;
		gbc_cbbxZipTempThreshold.gridy = 1;
		panelZip.add(cbbxZipTempThreshold, gbc_cbbxZipTempThreshold);

		lblCompressionLevel = new JLabel(Messages.getString("MainFrame.lblCompressionLevel.text")); //$NON-NLS-1$
		lblCompressionLevel.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblCompressionLevel = new GridBagConstraints();
		gbc_lblCompressionLevel.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblCompressionLevel.insets = new Insets(0, 0, 5, 5);
		gbc_lblCompressionLevel.gridx = 0;
		gbc_lblCompressionLevel.gridy = 2;
		panelZip.add(lblCompressionLevel, gbc_lblCompressionLevel);

		cbbxZipLevel = new JComboBox<>();
		cbbxZipLevel.setModel(new DefaultComboBoxModel<>(ZipLevel.values()));
		cbbxZipLevel.setSelectedItem(ZipLevel.valueOf(Settings.getProperty("zip_compression_level", ZipLevel.DEFAULT.toString())));
		cbbxZipLevel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Settings.setProperty("zip_compression_level", cbbxZipLevel.getSelectedItem().toString());
			}
		});
		cbbxZipLevel.setRenderer(new DefaultListCellRenderer()
		{

			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				setText(((ZipLevel)value).getName());
				return this;
			}
		});
		GridBagConstraints gbc_cbbxZipLevel = new GridBagConstraints();
		gbc_cbbxZipLevel.fill = GridBagConstraints.BOTH;
		gbc_cbbxZipLevel.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxZipLevel.gridx = 1;
		gbc_cbbxZipLevel.gridy = 2;
		panelZip.add(cbbxZipLevel, gbc_cbbxZipLevel);

		panelZipE = new JPanel();
		compressorsPane.addTab(Messages.getString("MainFrame.ZipExternal"), null, panelZipE, null); //$NON-NLS-1$
		final GridBagLayout gbl_panelZipE = new GridBagLayout();
		gbl_panelZipE.columnWidths = new int[] { 85, 246, 40, 0 };
		gbl_panelZipE.rowHeights = new int[] { 0, 28, 28, 28, 0, 0 };
		gbl_panelZipE.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panelZipE.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panelZipE.setLayout(gbl_panelZipE);

		lblZipECmd = new JLabel(Messages.getString("MainFrame.lblZipECmd.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblZipECmd = new GridBagConstraints();
		gbc_lblZipECmd.anchor = GridBagConstraints.EAST;
		gbc_lblZipECmd.insets = new Insets(5, 5, 5, 5);
		gbc_lblZipECmd.gridx = 0;
		gbc_lblZipECmd.gridy = 1;
		panelZipE.add(lblZipECmd, gbc_lblZipECmd);

		tfZipECmd = new JFileDropTextField(txt -> Settings.setProperty("zip_cmd", txt));//$NON-NLS-1$
		tfZipECmd.setMode(JFileDropMode.FILE);
		tfZipECmd.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tfZipECmd.setText(Settings.getProperty("zip_cmd", FindCmd.find7z())); //$NON-NLS-1$
		final GridBagConstraints gbc_tfZipECmd = new GridBagConstraints();
		gbc_tfZipECmd.insets = new Insets(0, 0, 5, 0);
		gbc_tfZipECmd.fill = GridBagConstraints.BOTH;
		gbc_tfZipECmd.gridx = 1;
		gbc_tfZipECmd.gridy = 1;
		panelZipE.add(tfZipECmd, gbc_tfZipECmd);
		tfZipECmd.setColumns(30);

		btZipECmd = new JButton(""); //$NON-NLS-1$
		btZipECmd.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		final GridBagConstraints gbc_btZipECmd = new GridBagConstraints();
		gbc_btZipECmd.fill = GridBagConstraints.BOTH;
		gbc_btZipECmd.insets = new Insets(0, 0, 5, 5);
		gbc_btZipECmd.gridx = 2;
		gbc_btZipECmd.gridy = 1;
		panelZipE.add(btZipECmd, gbc_btZipECmd);

		lblZipEArgs = new JLabel(Messages.getString("MainFrame.lblZipEArgs.text")); //$NON-NLS-1$
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
				setText(((ZipOptions) value).getName());
				return this;
			}
		});
		cbZipEArgs.addActionListener(arg0 -> Settings.setProperty("zip_level", cbZipEArgs.getSelectedItem().toString())); //$NON-NLS-1$
		cbZipEArgs.setSelectedItem(ZipOptions.valueOf(Settings.getProperty("zip_level", ZipOptions.NORMAL.toString()))); //$NON-NLS-1$
		final GridBagConstraints gbc_cbZipEArgs = new GridBagConstraints();
		gbc_cbZipEArgs.insets = new Insets(0, 0, 5, 5);
		gbc_cbZipEArgs.gridwidth = 2;
		gbc_cbZipEArgs.fill = GridBagConstraints.BOTH;
		gbc_cbZipEArgs.gridx = 1;
		gbc_cbZipEArgs.gridy = 2;
		panelZipE.add(cbZipEArgs, gbc_cbZipEArgs);

		lblZipEThreads = new JLabel(Messages.getString("MainFrame.lblZipEThreads.text")); //$NON-NLS-1$
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

		lblZipEWarning = new JLabel();
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

		panel7Zip = new JPanel();
		compressorsPane.addTab(Messages.getString("MainFrame.7zExternal"), null, panel7Zip, null); //$NON-NLS-1$
		compressorsPane.setEnabledAt(2, true);
		final GridBagLayout gbl_panel7Zip = new GridBagLayout();
		gbl_panel7Zip.columnWidths = new int[] { 85, 123, 0, 40, 0 };
		gbl_panel7Zip.rowHeights = new int[] { 0, 28, 28, 28, 0, 0 };
		gbl_panel7Zip.columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel7Zip.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panel7Zip.setLayout(gbl_panel7Zip);

		lbl7zCmd = new JLabel(Messages.getString("MainFrame.lbl7zCmd.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lbl7zCmd = new GridBagConstraints();
		gbc_lbl7zCmd.anchor = GridBagConstraints.EAST;
		gbc_lbl7zCmd.insets = new Insets(5, 5, 5, 5);
		gbc_lbl7zCmd.gridx = 0;
		gbc_lbl7zCmd.gridy = 1;
		panel7Zip.add(lbl7zCmd, gbc_lbl7zCmd);

		tf7zCmd = new JFileDropTextField(txt -> Settings.setProperty("7z_cmd", txt)); //$NON-NLS-1$
		tf7zCmd.setUI(new JTextFieldHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		tf7zCmd.setMode(JFileDropMode.FILE);
		tf7zCmd.setText(Settings.getProperty("7z_cmd", FindCmd.find7z())); //$NON-NLS-1$
		tf7zCmd.setColumns(30);
		final GridBagConstraints gbc_tf7zCmd = new GridBagConstraints();
		gbc_tf7zCmd.gridwidth = 2;
		gbc_tf7zCmd.fill = GridBagConstraints.BOTH;
		gbc_tf7zCmd.insets = new Insets(0, 0, 5, 0);
		gbc_tf7zCmd.gridx = 1;
		gbc_tf7zCmd.gridy = 1;
		panel7Zip.add(tf7zCmd, gbc_tf7zCmd);

		btn7zCmd = new JButton(""); //$NON-NLS-1$
		btn7zCmd.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/disk.png"))); //$NON-NLS-1$
		final GridBagConstraints gbc_btn7zCmd = new GridBagConstraints();
		gbc_btn7zCmd.fill = GridBagConstraints.BOTH;
		gbc_btn7zCmd.insets = new Insets(0, 0, 5, 5);
		gbc_btn7zCmd.gridx = 3;
		gbc_btn7zCmd.gridy = 1;
		panel7Zip.add(btn7zCmd, gbc_btn7zCmd);

		lbl7zArgs = new JLabel(Messages.getString("MainFrame.lbl7zArgs.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lbl7zArgs = new GridBagConstraints();
		gbc_lbl7zArgs.anchor = GridBagConstraints.EAST;
		gbc_lbl7zArgs.insets = new Insets(0, 5, 5, 5);
		gbc_lbl7zArgs.gridx = 0;
		gbc_lbl7zArgs.gridy = 2;
		panel7Zip.add(lbl7zArgs, gbc_lbl7zArgs);

		cb7zArgs = new JComboBox<>();
		cb7zArgs.addActionListener(arg0 -> Settings.setProperty("7z_level", cb7zArgs.getSelectedItem().toString())); //$NON-NLS-1$
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
		cb7zArgs.setSelectedItem(SevenZipOptions.valueOf(Settings.getProperty("7z_level", SevenZipOptions.NORMAL.toString()))); //$NON-NLS-1$
		final GridBagConstraints gbc_cb7zArgs = new GridBagConstraints();
		gbc_cb7zArgs.fill = GridBagConstraints.BOTH;
		gbc_cb7zArgs.gridwidth = 3;
		gbc_cb7zArgs.insets = new Insets(0, 0, 5, 5);
		gbc_cb7zArgs.gridx = 1;
		gbc_cb7zArgs.gridy = 2;
		panel7Zip.add(cb7zArgs, gbc_cb7zArgs);

		lbl7zThreads = new JLabel(Messages.getString("MainFrame.lbl7zThreads.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lbl7zThreads = new GridBagConstraints();
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
			public void focusLost(final FocusEvent e)
			{
				Settings.setProperty("7z_threads", tf7zThreads.getText()); //$NON-NLS-1$
			}
		});
		tf7zThreads.addActionListener(arg0 -> Settings.setProperty("7z_threads", tf7zThreads.getText())); //$NON-NLS-1$
		final GridBagConstraints gbc_tf7zThreads = new GridBagConstraints();
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
		ckbx7zSolid.addActionListener(arg0 -> {
			cb7zArgs.setEnabled(ckbx7zSolid.isSelected());
			Settings.setProperty("7z_solid", ckbx7zSolid.isSelected()); //$NON-NLS-1$
		});
		final GridBagConstraints gbc_ckbx7zSolid = new GridBagConstraints();
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
		final GridBagConstraints gbc_lbl7zWarning = new GridBagConstraints();
		gbc_lbl7zWarning.gridwidth = 4;
		gbc_lbl7zWarning.gridx = 0;
		gbc_lbl7zWarning.gridy = 4;
		panel7Zip.add(lbl7zWarning, gbc_lbl7zWarning);

		debug = new JPanel();
		settingsPane.addTab(Messages.getString("MainFrame.Debug"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/bug.png")), debug, null); //$NON-NLS-1$ //$NON-NLS-2$
		final GridBagLayout gbl_debug = new GridBagLayout();
		gbl_debug.columnWidths = new int[] { 100, 0, 0, 0 };
		gbl_debug.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_debug.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_debug.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		debug.setLayout(gbl_debug);

		lblLogLevel = new JLabel(Messages.getString("MainFrame.lblLogLevel.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblLogLevel = new GridBagConstraints();
		gbc_lblLogLevel.anchor = GridBagConstraints.EAST;
		gbc_lblLogLevel.fill = GridBagConstraints.VERTICAL;
		gbc_lblLogLevel.insets = new Insets(0, 0, 5, 5);
		gbc_lblLogLevel.gridx = 0;
		gbc_lblLogLevel.gridy = 1;
		debug.add(lblLogLevel, gbc_lblLogLevel);

		cbLogLevel = new JComboBox<>();
		cbLogLevel.setEnabled(false);
		final GridBagConstraints gbc_cbLogLevel = new GridBagConstraints();
		gbc_cbLogLevel.gridwidth = 2;
		gbc_cbLogLevel.insets = new Insets(0, 0, 5, 5);
		gbc_cbLogLevel.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbLogLevel.gridx = 1;
		gbc_cbLogLevel.gridy = 1;
		debug.add(cbLogLevel, gbc_cbLogLevel);

		lblMemory = new JLabel(Messages.getString("MainFrame.lblMemory.text")); //$NON-NLS-1$
		lblMemory.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbc_lblMemory = new GridBagConstraints();
		gbc_lblMemory.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblMemory.insets = new Insets(0, 0, 5, 5);
		gbc_lblMemory.gridx = 0;
		gbc_lblMemory.gridy = 2;
		debug.add(lblMemory, gbc_lblMemory);

		lblMemoryUsage = new JLabel(" "); //$NON-NLS-1$
		lblMemoryUsage.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		final GridBagConstraints gbc_lblMemoryUsage = new GridBagConstraints();
		gbc_lblMemoryUsage.fill = GridBagConstraints.BOTH;
		gbc_lblMemoryUsage.insets = new Insets(0, 0, 5, 2);
		gbc_lblMemoryUsage.gridx = 1;
		gbc_lblMemoryUsage.gridy = 2;
		debug.add(lblMemoryUsage, gbc_lblMemoryUsage);

		btnGc = new JButton(Messages.getString("MainFrame.btnGc.text")); //$NON-NLS-1$
		btnGc.addActionListener(e -> {
			System.gc();
			updateMemory();
		});
		final GridBagConstraints gbc_btnGc = new GridBagConstraints();
		gbc_btnGc.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnGc.insets = new Insets(0, 0, 5, 5);
		gbc_btnGc.gridx = 2;
		gbc_btnGc.gridy = 2;
		debug.add(btnGc, gbc_btnGc);
		mainPane.setEnabledAt(2, true);

		pack();

		try
		{
			setBounds(SerializationUtils.deserialize(Hex.decodeHex(Settings.getProperty("MainFrame.Bounds", Hex.encodeHexString(SerializationUtils.serialize(new Rectangle(50, 50, 640, 400))))))); //$NON-NLS-1$
		}
		catch (final DecoderException e1)
		{
			e1.printStackTrace();
		}

		scheduler.scheduleAtFixedRate(() -> updateMemory(), 0, 1, TimeUnit.MINUTES);
	}

	public void initScanSettings()
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
		txtRomsDest.setText(Profile.curr_profile.getProperty("roms_dest_dir", "")); //$NON-NLS-1$ //$NON-NLS-2$
		lblDisksDest.setSelected(Profile.curr_profile.getProperty("disks_dest_dir_enabled", false)); //$NON-NLS-1$
		tfDisksDest.setText(Profile.curr_profile.getProperty("disks_dest_dir", "")); //$NON-NLS-1$ //$NON-NLS-2$
		lblSWDest.setSelected(Profile.curr_profile.getProperty("swroms_dest_dir_enabled", false)); //$NON-NLS-1$
		tfSWDest.setText(Profile.curr_profile.getProperty("swroms_dest_dir", "")); //$NON-NLS-1$ //$NON-NLS-2$
		lblSWDisksDest.setSelected(Profile.curr_profile.getProperty("swdisks_dest_dir_enabled", false)); //$NON-NLS-1$
		tfSWDisksDest.setText(Profile.curr_profile.getProperty("swdisks_dest_dir", "")); //$NON-NLS-1$ //$NON-NLS-2$
		lblSamplesDest.setSelected(Profile.curr_profile.getProperty("samples_dest_dir_enabled", false)); //$NON-NLS-1$
		tfSamplesDest.setText(Profile.curr_profile.getProperty("samples_dest_dir", "")); //$NON-NLS-1$ //$NON-NLS-2$
		listSrcDir.getModel().removeAllElements();
		for (final String s : Profile.curr_profile.getProperty("src_dir", "").split("\\|")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (!s.isEmpty())
				listSrcDir.getModel().addElement(new File(s));
		cbCompression.setSelectedItem(FormatOptions.valueOf(Profile.curr_profile.settings.getProperty("format", FormatOptions.ZIP.toString()))); //$NON-NLS-1$
		cbbxMergeMode.setSelectedItem(MergeOptions.valueOf(Profile.curr_profile.settings.getProperty("merge_mode", MergeOptions.SPLIT.toString()))); //$NON-NLS-1$
		cbHashCollision.setEnabled(((MergeOptions) cbbxMergeMode.getSelectedItem()).isMerge());
		cbHashCollision.setSelectedItem(HashCollisionOptions.valueOf(Profile.curr_profile.settings.getProperty("hash_collision_mode", HashCollisionOptions.SINGLEFILE.toString()))); //$NON-NLS-1$
		chckbxIncludeClones.setSelected(Profile.curr_profile.getProperty("filter.InclClones", true)); //$NON-NLS-1$
		chckbxIncludeDisks.setSelected(Profile.curr_profile.getProperty("filter.InclDisks", true)); //$NON-NLS-1$
		chckbxIncludeSamples.setSelected(Profile.curr_profile.getProperty("filter.InclSamples", true)); //$NON-NLS-1$
		cbbxDriverStatus.setSelectedItem(Driver.StatusType.valueOf(Profile.curr_profile.getProperty("filter.DriverStatus", Driver.StatusType.preliminary.toString()))); //$NON-NLS-1$
		cbbxFilterCabinetType.setSelectedItem(CabinetType.valueOf(Profile.curr_profile.getProperty("filter.CabinetType", CabinetType.any.toString()))); //$NON-NLS-1$
		cbbxFilterDisplayOrientation.setSelectedItem(DisplayOrientation.valueOf(Profile.curr_profile.getProperty("filter.DisplayOrientation", DisplayOrientation.any.toString()))); //$NON-NLS-1$
		cbbxSWMinSupportedLvl.setSelectedItem(Supported.valueOf(Profile.curr_profile.getProperty("filter.MinSoftwareSupportedLevel", Supported.no.toString()))); //$NON-NLS-1$
		cbbxYearMin.setModel(new Years(Profile.curr_profile.years));
		cbbxYearMin.setSelectedItem(Profile.curr_profile.getProperty("filter.YearMin", cbbxYearMin.getModel().getElementAt(0))); //$NON-NLS-1$
		cbbxYearMax.setModel(new Years(Profile.curr_profile.years));
		cbbxYearMax.setSelectedItem(Profile.curr_profile.getProperty("filter.YearMax", cbbxYearMax.getModel().getElementAt(cbbxYearMax.getModel().getSize() - 1))); //$NON-NLS-1$
		tfNPlayers.setText(Profile.curr_profile.nplayers != null ? Profile.curr_profile.nplayers.file.getAbsolutePath() : null);
		listNPlayers.setModel(Profile.curr_profile.nplayers != null ? Profile.curr_profile.nplayers : new DefaultListModel<>());
		tfCatVer.setText(Profile.curr_profile.catver != null ? Profile.curr_profile.catver.file.getAbsolutePath() : null);
		treeCatVer.setModel(Profile.curr_profile.catver != null ? new CatVerModel(Profile.curr_profile.catver) : new CatVerModel());
	}

	private void loadProfile(final ProfileNFO profile)
	{
		if (Profile.curr_profile != null)
			Profile.curr_profile.saveSettings();
		final Progress progress = new Progress(MainFrame.this);
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{
			boolean success = false;

			@Override
			protected Void doInBackground() throws Exception
			{
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.clear();
				success = (null != (Profile.curr_profile = Profile.load(profile, progress)));
				Scan.report.setProfile(Profile.curr_profile);
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.reset(Profile.curr_profile);
				mainPane.setEnabledAt(1, success);
				btnScan.setEnabled(success);
				btnFix.setEnabled(false);
				lblProfileinfo.setText(Profile.curr_profile.getName());
				checkBoxListSystems.setModel(Profile.curr_profile.systems);
				return null;
			}

			@Override
			protected void done()
			{
				progress.dispose();
				if (success && Profile.curr_profile != null)
				{
					initScanSettings();
					mainPane.setSelectedIndex(1);
				}
			}

		};
		worker.execute();
		progress.setVisible(true);
	}

	/*
	 * private void chooseProfile() { new JFileChooser() { { File workdir =
	 * Paths.get("./xmlfiles").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
	 * setCurrentDirectory(workdir); addChoosableFileFilter(new
	 * FileNameExtensionFilter(Messages.getString("MainFrame.DatFile"), "dat",
	 * "xml")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	 * if(showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
	 * loadProfile(getSelectedFile()); } } }; }
	 */
	private void scan()
	{
		String txtdstdir = txtRomsDest.getText();
		if (txtdstdir.isEmpty())
		{
			btnRomsDest.doClick();
			txtdstdir = txtRomsDest.getText();
		}
		if (txtdstdir.isEmpty())
			return;

		final Progress progress = new Progress(MainFrame.this);
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				curr_scan = new Scan(Profile.curr_profile, progress);
				btnFix.setEnabled(curr_scan.actions.stream().mapToInt(Collection::size).sum() > 0);
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

	private void updateMemory()
	{
		final Runtime rt = Runtime.getRuntime();
		lblMemoryUsage.setText(String.format(Messages.getString("MainFrame.MemoryUsage"), FileUtils.byteCountToDisplaySize(rt.totalMemory()), FileUtils.byteCountToDisplaySize(rt.totalMemory() - rt.freeMemory()), FileUtils.byteCountToDisplaySize(rt.freeMemory()), FileUtils.byteCountToDisplaySize(rt.maxMemory()))); //$NON-NLS-1$
	}
}
