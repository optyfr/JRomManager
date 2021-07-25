package jrm.ui.batch;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import jrm.JRomManager;
import jrm.aui.basic.SrcDstResult;
import jrm.batch.DirUpdater;
import jrm.batch.DirUpdaterResults;
import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.misc.ProfileSettings;
import jrm.misc.SettingsEnum;
import jrm.security.PathAbstractor;
import jrm.security.Session;
import jrm.ui.MainFrame;
import jrm.ui.basic.JFileDropList;
import jrm.ui.basic.JFileDropMode;
import jrm.ui.basic.JListHintUI;
import jrm.ui.basic.JRMFileChooser;
import jrm.ui.basic.JRMFileChooser.CallBack;
import jrm.ui.basic.JSDRDropTable;
import jrm.ui.basic.JTableButton.TableButtonPressedHandler;
import jrm.ui.basic.SDRTableModel;
import jrm.ui.progress.SwingWorkerProgress;
import one.util.streamex.StreamEx;

@SuppressWarnings("serial")
public class BatchDirUpd8rPanel extends JPanel
{
	private JFileDropList listBatchToolsDat2DirSrc;
	private JSDRDropTable tableBatchToolsDat2Dir;
	private JMenu mnDat2DirPresets;

	private Point popupPoint;

	/**
	 * Create the panel.
	 */
	public BatchDirUpd8rPanel(final Session session)
	{
		final GridBagLayout gbl_panelBatchToolsDat2Dir = new GridBagLayout();
		gbl_panelBatchToolsDat2Dir.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelBatchToolsDat2Dir.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelBatchToolsDat2Dir.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelBatchToolsDat2Dir.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		this.setLayout(gbl_panelBatchToolsDat2Dir);

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.3);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		final GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.gridwidth = 3;
		gbc_splitPane.insets = new Insets(0, 0, 5, 0);
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 0;
		this.add(splitPane, gbc_splitPane);

		final JScrollPane scrollPane_5 = new JScrollPane();
		splitPane.setLeftComponent(scrollPane_5);
		scrollPane_5.setBorder(new TitledBorder(null, Messages.getString("MainFrame.SrcDirs"), TitledBorder.LEADING, TitledBorder.TOP, null, null));

		listBatchToolsDat2DirSrc = new JFileDropList(files -> session.getUser().getSettings().setProperty(SettingsEnum.dat2dir_srcdirs, String.join("|", files.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList())))); //$NON-NLS-1$ //$NON-NLS-2$
		listBatchToolsDat2DirSrc.setMode(JFileDropMode.DIRECTORY);
		listBatchToolsDat2DirSrc.setUI(new JListHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		listBatchToolsDat2DirSrc.setToolTipText(Messages.getString("MainFrame.listBatchToolsDat2DirSrc.toolTipText")); //$NON-NLS-1$
		scrollPane_5.setViewportView(listBatchToolsDat2DirSrc);

		final JPopupMenu popupMenu_2 = new JPopupMenu();
		MainFrame.addPopup(listBatchToolsDat2DirSrc, popupMenu_2);

		JMenuItem mnDat2DirAddSrcDir = new JMenuItem(Messages.getString("MainFrame.AddSrcDir"));
		mnDat2DirAddSrcDir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<File> list = listBatchToolsDat2DirSrc.getSelectedValuesList();
				new JRMFileChooser<Void>(
						JFileChooser.OPEN_DIALOG,
						JFileChooser.DIRECTORIES_ONLY,
						list.size() > 0 ? list.get(0).getParentFile() : null, // currdir
						null,	// selected
						null,	// filters
						"Choose source directories",
						true).show(SwingUtilities.windowForComponent(BatchDirUpd8rPanel.this), new CallBack<Void>()
				{
					@Override
					public Void call(JRMFileChooser<Void> chooser)
					{
						File[] files = chooser.getSelectedFiles();
						if (files.length > 0)
							listBatchToolsDat2DirSrc.add(files);
						return null;
					}
				});
			}
		});
		popupMenu_2.add(mnDat2DirAddSrcDir);

		JMenuItem mnDat2DirDelSrcDir = new JMenuItem(Messages.getString("MainFrame.DelSrcDir")); //$NON-NLS-1$
		mnDat2DirDelSrcDir.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				listBatchToolsDat2DirSrc.del(listBatchToolsDat2DirSrc.getSelectedValuesList());
			}
		});
		popupMenu_2.add(mnDat2DirDelSrcDir);

		JScrollPane scrollPane_6 = new JScrollPane();
		splitPane.setRightComponent(scrollPane_6);

		BatchTableModel model = new BatchTableModel();
		tableBatchToolsDat2Dir = new JSDRDropTable(model, files -> session.getUser().getSettings().setProperty(SettingsEnum.dat2dir_sdr, SrcDstResult.toJSON(files))); //$NON-NLS-1$
		model.setButtonHandler(new TableButtonPressedHandler()
		{
			@Override
			public void onButtonPress(int row, int column)
			{
				final SrcDstResult sdr = model.getData().get(row);
				new SwingWorkerProgress<DirUpdaterResults, Void>(SwingUtilities.getWindowAncestor(BatchDirUpd8rPanel.this))
				{

					@Override
					protected DirUpdaterResults doInBackground() throws Exception
					{
						setProgress("Loading...");
						return DirUpdaterResults.load(session, new File(sdr.src), this);
					}
					
					@Override protected void done()
					{
						close();
						try
						{
							new BatchDirUpd8rResultsDialog(session, SwingUtilities.getWindowAncestor(BatchDirUpd8rPanel.this),get());
						}
						catch (InterruptedException | ExecutionException e)
						{
							Log.err(e.getMessage(), e);
						}
					};
				}.execute();;
			}
		});
		if(session!=null)
			tableBatchToolsDat2Dir.getSDRModel().setData(SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_sdr, "[]")));
		tableBatchToolsDat2Dir.setCellSelectionEnabled(false);
		tableBatchToolsDat2Dir.setRowSelectionAllowed(true);
		tableBatchToolsDat2Dir.getSDRModel().setSrcFilter(file -> {
			final List<String> exts = Arrays.asList("xml", "dat"); //$NON-NLS-1$ //$NON-NLS-2$
			if (file.isFile())
				return exts.contains(FilenameUtils.getExtension(file.getName()));
			else if (file.isDirectory())
				return file.listFiles(f -> f.isFile() && exts.contains(FilenameUtils.getExtension(f.getName()))).length>0;
			return false;
		});
		tableBatchToolsDat2Dir.getSDRModel().setDstFilter(file -> {
			return file.isDirectory();
		});
		tableBatchToolsDat2Dir.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tableBatchToolsDat2Dir.setFillsViewportHeight(true);
		((BatchTableModel) tableBatchToolsDat2Dir.getModel()).applyColumnsWidths(tableBatchToolsDat2Dir);
		scrollPane_6.setViewportView(tableBatchToolsDat2Dir);
		
		
		tableBatchToolsDat2Dir.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(final MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					final JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					if (row >= 0)
					{
						final SDRTableModel tablemodel = tableBatchToolsDat2Dir.getSDRModel();
						//final int column = target.columnAtPoint(e.getPoint());
						final SrcDstResult sdr = tablemodel.getData().get(row);
/*						if(sdr.src.isFile())
							new ReportLite(SwingUtilities.getWindowAncestor(BatchDirUpd8rPanel.this),sdr.src);*/
						new SwingWorkerProgress<DirUpdaterResults, Void>(SwingUtilities.getWindowAncestor(BatchDirUpd8rPanel.this))
						{

							@Override
							protected DirUpdaterResults doInBackground() throws Exception
							{
								setProgress("Loading...");
								return DirUpdaterResults.load(session, new File(sdr.src), this);
							}
							
							@Override
							protected void done()
							{
								close();
								try
								{
									new BatchDirUpd8rResultsDialog(session, SwingUtilities.getWindowAncestor(BatchDirUpd8rPanel.this), get());
								}
								catch (InterruptedException | ExecutionException e)
								{
									Log.err(e.getMessage(), e);
								}
							}
						}.execute();
					}
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e)
			{
				if(e.isPopupTrigger())
					popupPoint = e.getPoint();
			}
			
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if(e.isPopupTrigger())
					popupPoint = e.getPoint();
			}
		});

		JPopupMenu popupMenu = new JPopupMenu();
		MainFrame.addPopup(tableBatchToolsDat2Dir, popupMenu);

		JMenuItem mnDat2DirAddDat = new JMenuItem(Messages.getString("MainFrame.AddDat"));
		mnDat2DirAddDat.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int col = tableBatchToolsDat2Dir.columnAtPoint(popupPoint);
				final int row  = tableBatchToolsDat2Dir.rowAtPoint(popupPoint);
				List<SrcDstResult> list = tableBatchToolsDat2Dir.getSelectedValuesList();
				new JRMFileChooser<Void>(
						col == 0 ? JFileChooser.OPEN_DIALOG : JFileChooser.SAVE_DIALOG,
						col == 0 ? JFileChooser.FILES_AND_DIRECTORIES : JFileChooser.DIRECTORIES_ONLY,
						list.size() > 0 ? Optional.ofNullable(col == 0 ? list.get(0).src : list.get(0).dst).map(f->new File(f).getParentFile()).orElse(null) : null, // currdir
						null,	// selected
						Collections.singletonList(new FileFilter()
						{
							@Override
							public boolean accept(File f)
							{
								java.io.FileFilter filter = null;
								if (col == 1)
									filter = tableBatchToolsDat2Dir.getSDRModel().getDstFilter();
								else if (col == 0)
									filter = new java.io.FileFilter() {

										@Override
										public boolean accept(File file)
										{
											final List<String> exts = Arrays.asList("xml", "dat"); //$NON-NLS-1$ //$NON-NLS-2$
											if (file.isFile())
												return exts.contains(FilenameUtils.getExtension(file.getName()));
											return true;
										}
									
								};
								if (filter != null)
									return filter.accept(f);
								return true;
							}
		
							@Override
							public String getDescription()
							{
								return col==0?"Dat/XML files or directories of Dat/XML files":"Destination directories";
							}
						}),
						col == 0 ? "Choose XML/DAT files or the parent directory in case of software lists" : "Choose destination directories",
						true).show(SwingUtilities.windowForComponent(BatchDirUpd8rPanel.this), new CallBack<Void>()
				{
					@Override
					public Void call(JRMFileChooser<Void> chooser)
					{
						File[] files = chooser.getSelectedFiles();
						SDRTableModel model = tableBatchToolsDat2Dir.getSDRModel();
						if (files.length > 0)
						{
							int start_size = model.getData().size();
							final java.io.FileFilter filter =  col == 0 ? model.getSrcFilter() : model.getDstFilter();
							for (int i = 0; i < files.length; i++)
							{
								File file = files[i];
								if(filter.accept(file))
								{
									SrcDstResult line;
									if (row == -1 || row + i >= model.getData().size())
										model.getData().add(line = new SrcDstResult());
									else
										line = model.getData().get(row + i);
									if (col == 1)
										line.dst = file.getPath();
									else
										line.src = file.getPath();
								}
							}
							if (row != -1)
								model.fireTableChanged(new TableModelEvent(model, row, start_size - 1, col));
							if (start_size != model.getData().size())
								model.fireTableChanged(new TableModelEvent(model, start_size, model.getData().size() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
							tableBatchToolsDat2Dir.call();
						}
						return null;
					}
				});
			}
		});
		popupMenu.add(mnDat2DirAddDat);
		
		popupMenu.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuCanceled(PopupMenuEvent e)
			{
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{
			}

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				mnDat2DirPresets.setEnabled(tableBatchToolsDat2Dir.getSelectedRowCount() > 0);
				mnDat2DirAddDat.setEnabled(tableBatchToolsDat2Dir.columnAtPoint(popupPoint) <= 1);
			}
		});


		JMenuItem mnDat2DirDelDat = new JMenuItem(Messages.getString("MainFrame.DelDat")); //$NON-NLS-1$
		mnDat2DirDelDat.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				tableBatchToolsDat2Dir.del(tableBatchToolsDat2Dir.getSelectedValuesList());
			}
		});
		popupMenu.add(mnDat2DirDelDat);

		mnDat2DirPresets = new JMenu(Messages.getString("MainFrame.Presets")); //$NON-NLS-1$
		popupMenu.add(mnDat2DirPresets);

		JMenu mnDat2DirD2D = new JMenu(Messages.getString("MainFrame.Dir2DatMenu")); //$NON-NLS-1$
		mnDat2DirPresets.add(mnDat2DirD2D);

		JMenuItem mntmDat2DirD2DTzip = new JMenuItem(Messages.getString("MainFrame.TZIP")); //$NON-NLS-1$
		mntmDat2DirD2DTzip.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (SrcDstResult sdr : tableBatchToolsDat2Dir.getSelectedValuesList())
				{
					ProfileSettings.TZIP(session, PathAbstractor.getAbsolutePath(session, sdr.src).toFile());
				}
			}
		});
		mnDat2DirD2D.add(mntmDat2DirD2DTzip);

		JMenuItem mntmDat2DirD2DDir = new JMenuItem(Messages.getString("MainFrame.DIR")); //$NON-NLS-1$
		mntmDat2DirD2DDir.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				for (SrcDstResult sdr : tableBatchToolsDat2Dir.getSelectedValuesList())
				{
					ProfileSettings.DIR(session, PathAbstractor.getAbsolutePath(session, sdr.src).toFile());
				}
			}
		});
		mnDat2DirD2D.add(mntmDat2DirD2DDir);
		
		JMenuItem mntmCustom = new JMenuItem(Messages.getString("BatchToolsDirUpd8rPanel.mntmCustom.text")); //$NON-NLS-1$
		mntmCustom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<SrcDstResult> list = tableBatchToolsDat2Dir.getSelectedValuesList();
				if(list.size()>0)
				{
					BatchDirUpd8rSettingsDialog dialog = new BatchDirUpd8rSettingsDialog(SwingUtilities.getWindowAncestor(BatchDirUpd8rPanel.this));
					SrcDstResult entry = list.get(0);
					dialog.settingsPanel.initProfileSettings(session.getUser().getSettings().loadProfileSettings(PathAbstractor.getAbsolutePath(session, entry.src).toFile(), null));
					dialog.setVisible(true);
					if(dialog.success)
					{
						for(SrcDstResult sdr : list)
						{
							session.getUser().getSettings().saveProfileSettings(PathAbstractor.getAbsolutePath(session, sdr.src).toFile(), dialog.settingsPanel.settings);
						}
					}
				}
			}
		});
		mnDat2DirPresets.add(mntmCustom);
		if(session!=null)
			for (final String s : StringUtils.split(session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_srcdirs, ""),'|')) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (!s.isEmpty())
					listBatchToolsDat2DirSrc.getModel().addElement(new File(s));

		JCheckBox cbBatchToolsDat2DirDryRun = new JCheckBox(Messages.getString("MainFrame.cbBatchToolsDat2DirDryRun.text")); //$NON-NLS-1$
		if(session!=null)
			cbBatchToolsDat2DirDryRun.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_dry_run, false)); //$NON-NLS-1$
		cbBatchToolsDat2DirDryRun.addItemListener(e -> session.getUser().getSettings().setProperty(SettingsEnum.dat2dir_dry_run, e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$

		JButton btnBatchToolsDir2DatStart = new JButton(Messages.getString("MainFrame.btnStart.text")); //$NON-NLS-1$
		btnBatchToolsDir2DatStart.setIcon(MainFrame.getIcon("/jrm/resicons/icons/bullet_go.png"));
		btnBatchToolsDir2DatStart.addActionListener((e) -> dat2dir(session, cbBatchToolsDat2DirDryRun.isSelected()));

		GridBagConstraints gbc_cbBatchToolsDat2DirDryRun = new GridBagConstraints();
		gbc_cbBatchToolsDat2DirDryRun.insets = new Insets(0, 0, 0, 5);
		gbc_cbBatchToolsDat2DirDryRun.gridx = 1;
		gbc_cbBatchToolsDat2DirDryRun.gridy = 1;
		this.add(cbBatchToolsDat2DirDryRun, gbc_cbBatchToolsDat2DirDryRun);
		GridBagConstraints gbc_btnBatchToolsDir2DatStart = new GridBagConstraints();
		gbc_btnBatchToolsDir2DatStart.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnBatchToolsDir2DatStart.gridx = 2;
		gbc_btnBatchToolsDir2DatStart.gridy = 1;
		this.add(btnBatchToolsDir2DatStart, gbc_btnBatchToolsDir2DatStart);

	}

	private void dat2dir(final Session session, boolean dryrun)
	{
		if (listBatchToolsDat2DirSrc.getModel().getSize() > 0)
		{
			final List<SrcDstResult> sdrl = ((SDRTableModel) tableBatchToolsDat2Dir.getModel()).getData();
			if (sdrl.stream().filter((sdr) -> !session.getUser().getSettings().getProfileSettingsFile(PathAbstractor.getAbsolutePath(session, sdr.src).toFile()).exists()).count() > 0)
				JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), Messages.getString("MainFrame.AllDatsPresetsAssigned")); //$NON-NLS-1$
			else
			{
				new SwingWorkerProgress<DirUpdater, Void>(SwingUtilities.getWindowAncestor(this))
				{
					@Override
					protected DirUpdater doInBackground() throws Exception
					{
						return new DirUpdater(session, sdrl, this, StreamEx.of(listBatchToolsDat2DirSrc.getModel().elements()).map(f->PathAbstractor.getAbsolutePath(session, f.toString()).toFile()).toList(), tableBatchToolsDat2Dir, dryrun);
					}

					@Override
					protected void done()
					{
						close();
						session.setCurrProfile(null);
						session.setCurrScan(null);
						session.getReport().setProfile(session.getCurrProfile());
						if (MainFrame.profile_viewer != null)
						{
							MainFrame.profile_viewer.dispose();
							MainFrame.profile_viewer = null;
						}
						if (MainFrame.report_frame != null)
							MainFrame.report_frame.setVisible(false);
						JRomManager.getMainFrame().getMainPane().setEnabledAt(1, false);
					}
				}.execute();;
				
			}
		}
		else
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), Messages.getString("MainFrame.AtLeastOneSrcDir")); //$NON-NLS-1$
	}

}
