package jrm.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.profile.manager.Dir;
import jrm.profile.manager.Import;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.manager.ProfileNFOMame.MameStatus;
import jrm.security.Session;
import jrm.ui.basic.JRMFileChooser;
import jrm.ui.basic.JRMFileChooser.OneRootFileSystemView;
import jrm.ui.profile.manager.DirNode;
import jrm.ui.profile.manager.DirTreeCellEditor;
import jrm.ui.profile.manager.DirTreeCellRenderer;
import jrm.ui.profile.manager.DirTreeModel;
import jrm.ui.profile.manager.DirTreeSelectionListener;
import jrm.ui.profile.manager.FileTableCellRenderer;
import jrm.ui.profile.manager.FileTableModel;
import jrm.ui.progress.SwingWorkerProgress;
import lombok.val;

@SuppressWarnings("serial")
public class ProfilePanel extends JPanel
{
	/** The mntm create folder. */
	private JMenuItem mntmCreateFolder;

	/** The mntm delete folder. */
	private JMenuItem mntmDeleteFolder;

	/** The mntm delete profile. */
	private JMenuItem mntmDeleteProfile;

	/** The mntm drop cache. */
	private JMenuItem mntmDropCache;

	/** The mntm rename profile. */
	private JMenuItem mntmRenameProfile;

	/** The mntm update from mame. */
	private JMenuItem mntmUpdateFromMame;

	/** The profiles list. */
	private JTable profilesList;

	/** The scroll pane 1. */
	private JScrollPane scrollPane_1;

	/** The profiles tree. */
	private JTree profilesTree;

	private ProfileLoader profileLoader;

	/**
	 * Create the panel.
	 */
	public ProfilePanel(final Session session)
	{
		final var gbl_profilesTab = new GridBagLayout();
		gbl_profilesTab.columnWidths = new int[] { 0, 0 };
		gbl_profilesTab.rowHeights = new int[] { 0, 0, 0 };
		gbl_profilesTab.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_profilesTab.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		this.setLayout(gbl_profilesTab);

		final var profilesPanel = new JSplitPane();
		profilesPanel.setContinuousLayout(true);
		profilesPanel.setResizeWeight(0.2);
		profilesPanel.setOneTouchExpandable(true);
		final var gbc_profilesPanel = new GridBagConstraints();
		gbc_profilesPanel.insets = new Insets(0, 0, 5, 0);
		gbc_profilesPanel.fill = GridBagConstraints.BOTH;
		gbc_profilesPanel.gridx = 0;
		gbc_profilesPanel.gridy = 0;
		this.add(profilesPanel, gbc_profilesPanel);

		final var scrollPane = new JScrollPane();
		profilesPanel.setRightComponent(scrollPane);

		profilesList = new JTable();
		final DefaultCellEditor editor = (DefaultCellEditor) profilesList.getDefaultEditor(Object.class);
		editor.setClickCountToStart(3);
		profilesList.setShowVerticalLines(false);
		profilesList.setShowHorizontalLines(false);
		profilesList.setShowGrid(false);
		profilesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		profilesList.setPreferredScrollableViewportSize(new Dimension(400, 300));
		profilesList.setFillsViewportHeight(true);
		scrollPane.setViewportView(profilesList);
		final var filemodel = new FileTableModel();
		profilesList.setModel(filemodel);
		for (var i = 0; i < profilesList.getColumnCount(); i++)
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
						getProfileLoader().loadProfile(session, filemodel.getNfoAt(row));
					}
				}
			}
		});

		scrollPane_1 = new JScrollPane();
		scrollPane_1.setMinimumSize(new Dimension(80, 22));
		profilesPanel.setLeftComponent(scrollPane_1);

		profilesTree = new JTree();
		scrollPane_1.setViewportView(profilesTree);

		final var profilesTreeModel = new DirTreeModel(new DirNode(session.getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile())); //$NON-NLS-1$
		profilesTree.setModel(profilesTreeModel);
		profilesTree.setRootVisible(true);
		profilesTree.setShowsRootHandles(true);
		profilesTree.setEditable(true);
		final var profilesTreeRenderer = new DirTreeCellRenderer();
		profilesTree.setCellRenderer(profilesTreeRenderer);
		profilesTree.setCellEditor(new DirTreeCellEditor(profilesTree, profilesTreeRenderer));
		profilesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		profilesTree.addTreeSelectionListener(new DirTreeSelectionListener(session, profilesList));

		final var popupMenu_1 = new JPopupMenu();
		popupMenu_1.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuCanceled(final PopupMenuEvent e)
			{
				// not used
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e)
			{
				// not used
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
		MainFrame.addPopup(profilesList, popupMenu_1);

		mntmDeleteProfile = new JMenuItem(Messages.getString("MainFrame.mntmDeleteProfile.text")); //$NON-NLS-1$
		mntmDeleteProfile.addActionListener(e -> {
			final int row = profilesList.getSelectedRow();
			if (row >= 0)
			{
				final ProfileNFO nfo = filemodel.getNfoAt(row);
				if ((session.curr_profile == null || !session.curr_profile.nfo.equals(nfo)) && nfo.delete())
					filemodel.populate(session);
			}
		});
		mntmDeleteProfile.setIcon(MainFrame.getIcon("/jrm/resicons/icons/script_delete.png")); //$NON-NLS-1$
		popupMenu_1.add(mntmDeleteProfile);

		mntmRenameProfile = new JMenuItem(Messages.getString("MainFrame.mntmRenameProfile.text")); //$NON-NLS-1$
		mntmRenameProfile.addActionListener(e -> {
			final int row = profilesList.getSelectedRow();
			if (row >= 0)
			{
				profilesList.editCellAt(row, 0);
			}
		});
		mntmRenameProfile.setIcon(MainFrame.getIcon("/jrm/resicons/icons/script_edit.png")); //$NON-NLS-1$
		popupMenu_1.add(mntmRenameProfile);

		mntmDropCache = new JMenuItem(Messages.getString("MainFrame.mntmDropCache.text")); //$NON-NLS-1$
		mntmDropCache.addActionListener(e -> {
			final int row = profilesList.getSelectedRow();
			if (row >= 0)
				try
				{
					Files.deleteIfExists(Paths.get(filemodel.getFileAt(row).getAbsolutePath() + ".cache"));
				}
				catch (IOException e1)
				{
					Log.err(e1.getMessage(), e1);
				}
		});
		mntmDropCache.setIcon(MainFrame.getIcon("/jrm/resicons/icons/bin.png")); //$NON-NLS-1$
		popupMenu_1.add(mntmDropCache);

		final var separator = new JSeparator();
		popupMenu_1.add(separator);

		mntmUpdateFromMame = new JMenuItem(Messages.getString("MainFrame.mntmUpdateFromMame.text")); //$NON-NLS-1$
		mntmUpdateFromMame.addActionListener(e -> {
			final int row = profilesList.getSelectedRow();
			if (row >= 0)
			{
				try
				{
					final ProfileNFO nfo = filemodel.getNfoAt(row);
					if (nfo.mame.getStatus() == MameStatus.NEEDUPDATE || (nfo.mame.getStatus() == MameStatus.NOTFOUND && new JRMFileChooser<MameStatus>(JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY, null, nfo.mame.getFile(), null, Messages.getString("MainFrame.ChooseMameNewLocation"), false).show(SwingUtilities.getWindowAncestor(this), chooser -> { //$NON-NLS-1$
						if (chooser.getSelectedFile().exists())
							return nfo.mame.relocate(chooser.getSelectedFile());
						return MameStatus.NOTFOUND;
					}) == MameStatus.NEEDUPDATE))
					{
						new SwingWorkerProgress<Import, Void>(SwingUtilities.getWindowAncestor(this))
						{

							@Override
							protected Import doInBackground() throws Exception
							{
								return new Import(session, nfo.mame.getFile(), nfo.mame.isSL(), this);
							}

							@Override
							protected void done()
							{
								try
								{
									Import imprt = get();
									nfo.mame.delete();
									nfo.mame.setFileroms(new File(nfo.file.getParentFile(), imprt.roms_file.getName()));
									Files.copy(imprt.roms_file.toPath(), nfo.mame.getFileroms().toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
									if (nfo.mame.isSL())
									{
										nfo.mame.setFilesl(new File(nfo.file.getParentFile(), imprt.sl_file.getName()));
										Files.copy(imprt.sl_file.toPath(), nfo.mame.getFilesl().toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
									}
									nfo.mame.setUpdated();
									nfo.stats.reset();
									nfo.save(session);
									close();
								}
								catch (InterruptedException | ExecutionException | IOException e)
								{
									Log.err(e.getMessage(), e);
								}
							}
						}.execute();
					}
				}
				catch (final Exception e1)
				{
					Log.err(e1.getMessage(), e1);
				}
			}
		});
		popupMenu_1.add(mntmUpdateFromMame);
		profilesTree.setSelectionRow(0);

		final var popupMenu = new JPopupMenu();
		popupMenu.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuCanceled(final PopupMenuEvent e)
			{
				// not used
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e)
			{
				// not used
			}

			@Override
			public void popupMenuWillBecomeVisible(final PopupMenuEvent e)
			{
				mntmCreateFolder.setEnabled(profilesTree.getSelectionCount() > 0);
				mntmDeleteFolder.setEnabled(profilesTree.getSelectionCount() > 0 && !((DirNode) profilesTree.getLastSelectedPathComponent()).isRoot());
			}
		});
		MainFrame.addPopup(profilesTree, popupMenu);

		mntmCreateFolder = new JMenuItem(Messages.getString("MainFrame.mntmCreateFolder.text")); //$NON-NLS-1$
		mntmCreateFolder.addActionListener(e -> {
			final DirNode selectedNode = (DirNode) profilesTree.getLastSelectedPathComponent();
			if (selectedNode != null)
			{
				final var newnode = new DirNode(new Dir(new File(selectedNode.getDir().getFile(), Messages.getString("MainFrame.NewFolder")))); //$NON-NLS-1$
				selectedNode.add(newnode);
				profilesTreeModel.reload(selectedNode);
				final var path = new TreePath(newnode.getPath());
				profilesTree.setSelectionPath(path);
				profilesTree.startEditingAtPath(path);
			}
		});
		mntmCreateFolder.setIcon(MainFrame.getIcon("/jrm/resicons/icons/folder_add.png")); //$NON-NLS-1$
		popupMenu.add(mntmCreateFolder);

		mntmDeleteFolder = new JMenuItem(Messages.getString("MainFrame.mntmDeleteFolder.text")); //$NON-NLS-1$
		mntmDeleteFolder.addActionListener(e -> {
			final DirNode selectedNode = (DirNode) profilesTree.getLastSelectedPathComponent();
			if (selectedNode != null)
			{
				final DirNode parent = (DirNode) selectedNode.getParent();
				profilesTreeModel.removeNodeFromParent(selectedNode);
				final var path = new TreePath(parent.getPath());
				profilesTree.setSelectionPath(path);
			}
		});
		mntmDeleteFolder.setIcon(MainFrame.getIcon("/jrm/resicons/icons/folder_delete.png")); //$NON-NLS-1$
		popupMenu.add(mntmDeleteFolder);

		final var profilesBtnPanel = new JPanel();
		final var gbc_profilesBtnPanel = new GridBagConstraints();
		gbc_profilesBtnPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_profilesBtnPanel.gridx = 0;
		gbc_profilesBtnPanel.gridy = 1;
		this.add(profilesBtnPanel, gbc_profilesBtnPanel);

		final var btnLoadProfile = new JButton(Messages.getString("MainFrame.btnLoadProfile.text")); //$NON-NLS-1$
		btnLoadProfile.setIcon(MainFrame.getIcon("/jrm/resicons/icons/add.png")); //$NON-NLS-1$
		btnLoadProfile.setEnabled(false);
		btnLoadProfile.addActionListener(e -> {
			// chooseProfile();
		});
		profilesBtnPanel.add(btnLoadProfile);

		final var btnImportDat = new JButton(Messages.getString("MainFrame.btnImportDat.text")); //$NON-NLS-1$
		btnImportDat.setIcon(MainFrame.getIcon("/jrm/resicons/icons/script_go.png")); //$NON-NLS-1$
		btnImportDat.addActionListener(e -> importDat(session, false));
		profilesBtnPanel.add(btnImportDat);

		final var btnImportSL = new JButton(Messages.getString("MainFrame.btnImportSL.text")); //$NON-NLS-1$
		btnImportSL.addActionListener(e -> importDat(session, true));
		btnImportSL.setIcon(MainFrame.getIcon("/jrm/resicons/icons/application_go.png")); //$NON-NLS-1$
		profilesBtnPanel.add(btnImportSL);

	}

	/**
	 * Import dat.
	 *
	 * @param sl
	 *            the sl
	 */
	private void importDat(final Session session, final boolean sl)
	{
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
		new JRMFileChooser<Void>(JFileChooser.OPEN_DIALOG, JFileChooser.FILES_ONLY, Optional.ofNullable(session.getUser().getSettings().getProperty("MainFrame.ChooseExeOrDatToImport", (String) null)).map(File::new).orElse(null), null, filters, Messages.getString("MainFrame.ChooseExeOrDatToImport"), true).show(SwingUtilities.getWindowAncestor(this), chooser -> {
			new SwingWorkerProgress<Void, Import>(SwingUtilities.getWindowAncestor(this))
			{
				@Override
				protected Void doInBackground() throws Exception
				{
					canCancel(false);
					session.getUser().getSettings().setProperty("MainFrame.ChooseExeOrDatToImport", chooser.getCurrentDirectory().getAbsolutePath()); //$NON-NLS-1$
					for (final File selectedfile : chooser.getSelectedFiles())
					{
						setProgress(Messages.getString("MainFrame.ImportingFromMame"), -1); //$NON-NLS-1$
						publish(new Import(session, selectedfile, sl, this));
					}
					return null;
				}

				@Override
				protected void process(List<Import> imprts)
				{
					for (val imprt : imprts)
					{
						if (!imprt.is_mame)
						{
							final var curr_dir = ((FileTableModel) profilesList.getModel()).curr_dir.getFile();
							var file = new File(curr_dir, imprt.file.getName());
							int mode = -1;
							if (file.exists())
							{
								final var options = new String[] { "Overwrite", "Auto Rename", "File Chooser", "Cancel" };
								mode = JOptionPane.showOptionDialog(ProfilePanel.this, "File already exists, choose what to do", "File already exists", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
								if (mode == 1)
								{
									for (var i = 1;; i++)
									{
										final var test_file = new File(file.getParentFile(), FilenameUtils.getBaseName(file.getName()) + '_' + i + '.' + FilenameUtils.getExtension(file.getName()));
										if (!test_file.exists())
										{
											file = test_file;
											break;
										}
									}
								}
								else if (mode == 3)
									continue;
							}
							if (!file.exists() || mode == 0)
							{
								try
								{
									FileUtils.copyFile(imprt.file, file);
									((FileTableModel) profilesList.getModel()).populate(session);
									continue;
								}
								catch (IOException e)
								{
									Log.err(e.getMessage(), e);
								}
							}
						}
						final var workdir = session.getUser().getSettings().getWorkPath().toFile(); // $NON-NLS-1$
						final var xmldir = new File(workdir, "xmlfiles"); //$NON-NLS-1$
						new JRMFileChooser<Void>(new OneRootFileSystemView(xmldir)).setup(JFileChooser.SAVE_DIALOG, JFileChooser.FILES_ONLY, null, new File(xmldir, imprt.file.getName()), Collections.singletonList(new FileNameExtensionFilter(Messages.getString("MainFrame.DatFile"), "dat", "xml", "jrm")), Messages.getString("MainFrame.ChooseFileName"), false).show(SwingUtilities.getWindowAncestor(ProfilePanel.this), chooser1 -> {
							try
							{
								final var file = chooser1.getSelectedFile();
								final var parent = file.getParentFile();
								FileUtils.copyFile(imprt.file, file);
								if (imprt.is_mame)
								{
									final var pnfo = ProfileNFO.load(session, file);
									pnfo.mame.set(imprt.org_file, sl);
									if (imprt.roms_file != null)
									{
										FileUtils.copyFileToDirectory(imprt.roms_file, parent);
										pnfo.mame.setFileroms(new File(parent, imprt.roms_file.getName()));
										if (imprt.sl_file != null)
										{
											FileUtils.copyFileToDirectory(imprt.sl_file, parent);
											pnfo.mame.setFilesl(new File(parent, imprt.sl_file.getName()));
										}
									}
									pnfo.save(session);
								}
								final var model = (DirTreeModel) profilesTree.getModel();
								final var root = (DirNode) model.getRoot();
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
								Log.err(e.getMessage(), e);
							}
							return null;
						});
					}
				}

				@Override
				protected void done()
				{
					close();
				}
			}.execute();
			return null;
		});
	}

	/**
	 * @return the profileLoader
	 */
	public ProfileLoader getProfileLoader()
	{
		return profileLoader;
	}

	/**
	 * @param profileLoader
	 *            the profileLoader to set
	 */
	public void setProfileLoader(ProfileLoader profileLoader)
	{
		this.profileLoader = profileLoader;
	}

}
