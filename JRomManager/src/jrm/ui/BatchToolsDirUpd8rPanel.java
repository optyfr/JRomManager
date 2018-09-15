package jrm.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.io.FilenameUtils;

import jrm.batch.DirUpdater;
import jrm.locale.Messages;
import jrm.misc.GlobalSettings;
import jrm.misc.Settings;
import jrm.profile.Profile;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.ui.basic.JFileDropList;
import jrm.ui.basic.JFileDropMode;
import jrm.ui.basic.JListHintUI;
import jrm.ui.basic.JSDRDropTable;
import jrm.ui.basic.SDRTableModel;
import jrm.ui.basic.SrcDstResult;
import jrm.ui.batch.BatchTableModel;
import jrm.ui.progress.Progress;

@SuppressWarnings("serial")
public class BatchToolsDirUpd8rPanel extends JPanel
{
	private JFileDropList listBatchToolsDat2DirSrc;
	private JSDRDropTable tableBatchToolsDat2Dir;
	private JMenu mnDat2DirPresets;


	/**
	 * Create the panel.
	 */
	public BatchToolsDirUpd8rPanel()
	{
		GridBagLayout gbl_panelBatchToolsDat2Dir = new GridBagLayout();
		gbl_panelBatchToolsDat2Dir.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelBatchToolsDat2Dir.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelBatchToolsDat2Dir.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelBatchToolsDat2Dir.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		this.setLayout(gbl_panelBatchToolsDat2Dir);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setResizeWeight(0.3);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.gridwidth = 3;
		gbc_splitPane.insets = new Insets(0, 0, 5, 0);
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 0;
		this.add(splitPane, gbc_splitPane);

		JScrollPane scrollPane_5 = new JScrollPane();
		splitPane.setLeftComponent(scrollPane_5);
		scrollPane_5.setBorder(new TitledBorder(null, Messages.getString("MainFrame.SrcDirs"), TitledBorder.LEADING, TitledBorder.TOP, null, null));

		listBatchToolsDat2DirSrc = new JFileDropList(files -> GlobalSettings.setProperty("dat2dir.srcdirs", String.join("|", files.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList())))); //$NON-NLS-1$ //$NON-NLS-2$
		listBatchToolsDat2DirSrc.setMode(JFileDropMode.DIRECTORY);
		listBatchToolsDat2DirSrc.setUI(new JListHintUI(Messages.getString("MainFrame.DropDirHint"), Color.gray)); //$NON-NLS-1$
		listBatchToolsDat2DirSrc.setToolTipText(Messages.getString("MainFrame.listBatchToolsDat2DirSrc.toolTipText")); //$NON-NLS-1$
		scrollPane_5.setViewportView(listBatchToolsDat2DirSrc);

		JPopupMenu popupMenu_2 = new JPopupMenu();
		MainFrame.addPopup(listBatchToolsDat2DirSrc, popupMenu_2);

		JMenuItem mnDat2DirAddSrcDir = new JMenuItem(Messages.getString("MainFrame.AddSrcDir")); //$NON-NLS-1$
		mnDat2DirAddSrcDir.setEnabled(false);
		popupMenu_2.add(mnDat2DirAddSrcDir);

		JMenuItem mnDat2DirDelSrcDir = new JMenuItem(Messages.getString("MainFrame.DelSrcDir")); //$NON-NLS-1$
		mnDat2DirDelSrcDir.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				listBatchToolsDat2DirSrc.del(listBatchToolsDat2DirSrc.getSelectedValuesList());
			}
		});
		popupMenu_2.add(mnDat2DirDelSrcDir);

		JScrollPane scrollPane_6 = new JScrollPane();
		splitPane.setRightComponent(scrollPane_6);

		tableBatchToolsDat2Dir = new JSDRDropTable(new BatchTableModel(), files -> GlobalSettings.setProperty("dat2dir.sdr", SrcDstResult.toJSON(files))); //$NON-NLS-1$
		tableBatchToolsDat2Dir.getSDRModel().setData(SrcDstResult.fromJSON(GlobalSettings.getProperty("dat2dir.sdr", "[]")));
		tableBatchToolsDat2Dir.setCellSelectionEnabled(false);
		tableBatchToolsDat2Dir.setRowSelectionAllowed(true);
		tableBatchToolsDat2Dir.getSDRModel().setSrcFilter(file -> {
			List<String> exts = Arrays.asList("xml", "dat"); //$NON-NLS-1$ //$NON-NLS-2$
			if (file.isFile())
				return exts.contains(FilenameUtils.getExtension(file.getName()));
			else if (file.isDirectory())
				return file.listFiles(f -> f.isFile() && exts.contains(FilenameUtils.getExtension(f.getName()))).length > 0;
			return false;
		});
		tableBatchToolsDat2Dir.getSDRModel().setDstFilter(file -> {
			return file.isDirectory();
		});
		tableBatchToolsDat2Dir.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tableBatchToolsDat2Dir.setFillsViewportHeight(true);
		((BatchTableModel) tableBatchToolsDat2Dir.getModel()).applyColumnsWidths(tableBatchToolsDat2Dir);
		scrollPane_6.setViewportView(tableBatchToolsDat2Dir);

		JPopupMenu popupMenu = new JPopupMenu();
		MainFrame.addPopup(tableBatchToolsDat2Dir, popupMenu);
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
				mnDat2DirPresets.setEnabled(tableBatchToolsDat2Dir.getSelectedRowCount() > 0);
			}
		});

		JMenuItem mnDat2DirAddDat = new JMenuItem(Messages.getString("MainFrame.AddDat")); //$NON-NLS-1$
		mnDat2DirAddDat.setEnabled(false);
		popupMenu.add(mnDat2DirAddDat);

		JMenuItem mnDat2DirDelDat = new JMenuItem(Messages.getString("MainFrame.DelDat")); //$NON-NLS-1$
		mnDat2DirDelDat.addActionListener(new ActionListener()
		{
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
			public void actionPerformed(ActionEvent e)
			{
				for (SrcDstResult sdr : tableBatchToolsDat2Dir.getSelectedValuesList())
				{
					try
					{
						Settings settings = new Settings();
						settings.setProperty("need_sha1_or_md5", false); //$NON-NLS-1$
						settings.setProperty("use_parallelism", true); //$NON-NLS-1$
						settings.setProperty("create_mode", true); //$NON-NLS-1$
						settings.setProperty("createfull_mode", false); //$NON-NLS-1$
						settings.setProperty("ignore_unneeded_containers", false); //$NON-NLS-1$
						settings.setProperty("ignore_unneeded_entries", false); //$NON-NLS-1$
						settings.setProperty("ignore_unknown_containers", true); //$NON-NLS-1$
						settings.setProperty("implicit_merge", false); //$NON-NLS-1$
						settings.setProperty("ignore_merge_name_roms", false); //$NON-NLS-1$
						settings.setProperty("ignore_merge_name_disks", false); //$NON-NLS-1$
						settings.setProperty("exclude_games", false); //$NON-NLS-1$
						settings.setProperty("exclude_machines", false); //$NON-NLS-1$
						settings.setProperty("backup", true); //$NON-NLS-1$
						settings.setProperty("format", FormatOptions.TZIP.toString()); //$NON-NLS-1$
						settings.setProperty("merge_mode", MergeOptions.NOMERGE.toString()); //$NON-NLS-1$
						settings.setProperty("archives_and_chd_as_roms", false); //$NON-NLS-1$
						Profile.saveSettings(sdr.src, settings);
					}
					catch (IOException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		mnDat2DirD2D.add(mntmDat2DirD2DTzip);

		JMenuItem mntmDat2DirD2DDir = new JMenuItem(Messages.getString("MainFrame.DIR")); //$NON-NLS-1$
		mntmDat2DirD2DDir.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				for (SrcDstResult sdr : tableBatchToolsDat2Dir.getSelectedValuesList())
				{
					try
					{
						Settings settings = new Settings();
						settings.setProperty("need_sha1_or_md5", false); //$NON-NLS-1$
						settings.setProperty("use_parallelism", true); //$NON-NLS-1$
						settings.setProperty("create_mode", true); //$NON-NLS-1$
						settings.setProperty("createfull_mode", false); //$NON-NLS-1$
						settings.setProperty("ignore_unneeded_containers", false); //$NON-NLS-1$
						settings.setProperty("ignore_unneeded_entries", false); //$NON-NLS-1$
						settings.setProperty("ignore_unknown_containers", true); //$NON-NLS-1$
						settings.setProperty("implicit_merge", false); //$NON-NLS-1$
						settings.setProperty("ignore_merge_name_roms", false); //$NON-NLS-1$
						settings.setProperty("ignore_merge_name_disks", false); //$NON-NLS-1$
						settings.setProperty("exclude_games", false); //$NON-NLS-1$
						settings.setProperty("exclude_machines", false); //$NON-NLS-1$
						settings.setProperty("backup", true); //$NON-NLS-1$
						settings.setProperty("format", FormatOptions.DIR.toString()); //$NON-NLS-1$
						settings.setProperty("merge_mode", MergeOptions.NOMERGE.toString()); //$NON-NLS-1$
						settings.setProperty("archives_and_chd_as_roms", true); //$NON-NLS-1$
						Profile.saveSettings(sdr.src, settings);
					}
					catch (IOException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		mnDat2DirD2D.add(mntmDat2DirD2DDir);
		
		JMenuItem mntmCustom = new JMenuItem(Messages.getString("BatchToolsDirUpd8rPanel.mntmCustom.text")); //$NON-NLS-1$
		mntmCustom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<SrcDstResult> list = tableBatchToolsDat2Dir.getSelectedValuesList();
				if(list.size()>0)
				{
					BatchToolsDirUpd8rSettingsDialog dialog = new BatchToolsDirUpd8rSettingsDialog(SwingUtilities.getWindowAncestor(BatchToolsDirUpd8rPanel.this));
					SrcDstResult entry = list.get(0);
					try
					{
						dialog.settingsPanel.initProfileSettings(Profile.loadSettings(entry.src, null));
						dialog.setVisible(true);
						if(dialog.success)
						{
							for(SrcDstResult sdr : list)
							{
								Profile.saveSettings(sdr.src, dialog.settingsPanel.settings);
							}
						}
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});
		mnDat2DirPresets.add(mntmCustom);
		for (final String s : GlobalSettings.getProperty("dat2dir.srcdirs", "").split("\\|")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (!s.isEmpty())
				listBatchToolsDat2DirSrc.getModel().addElement(new File(s));

		JCheckBox cbBatchToolsDat2DirDryRun = new JCheckBox(Messages.getString("MainFrame.cbBatchToolsDat2DirDryRun.text")); //$NON-NLS-1$
		cbBatchToolsDat2DirDryRun.setSelected(GlobalSettings.getProperty("dat2dir.dry_run", false)); //$NON-NLS-1$
		cbBatchToolsDat2DirDryRun.addItemListener(e -> GlobalSettings.setProperty("dat2dir.dry_run", e.getStateChange() == ItemEvent.SELECTED)); //$NON-NLS-1$

		JButton btnBatchToolsDir2DatStart = new JButton(Messages.getString("MainFrame.btnStart.text")); //$NON-NLS-1$
		btnBatchToolsDir2DatStart.addActionListener((e) -> dat2dir(cbBatchToolsDat2DirDryRun.isSelected()));

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

	private void dat2dir(boolean dryrun)
	{
		if (listBatchToolsDat2DirSrc.getModel().getSize() > 0)
		{
			List<SrcDstResult> sdrl = ((SDRTableModel) tableBatchToolsDat2Dir.getModel()).getData();
			if (sdrl.stream().filter((sdr) -> !Profile.getSettingsFile(sdr.src).exists()).count() > 0)
				JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), Messages.getString("MainFrame.AllDatsPresetsAssigned")); //$NON-NLS-1$
			else
			{
				final Progress progress = new Progress(SwingUtilities.getWindowAncestor(this));
				final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
				{

					@Override
					protected Void doInBackground() throws Exception
					{
						new DirUpdater(sdrl, progress, Collections.list(listBatchToolsDat2DirSrc.getModel().elements()), tableBatchToolsDat2Dir, dryrun);
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
		}
		else
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), Messages.getString("MainFrame.AtLeastOneSrcDir")); //$NON-NLS-1$
	}

}
