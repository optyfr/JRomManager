package jrm.ui.batch;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import jrm.ui.basic.JSDRDropTable;
import jrm.aui.basic.ResultColUpdater;
import jrm.ui.basic.SDRTableModel;
import jrm.aui.basic.SrcDstResult;
import jrm.ui.basic.JRMFileChooser.CallBack;
import jrm.ui.basic.JTableButton.TableButtonPressedHandler;
import jrm.aui.basic.SrcDstResult.SDRList;
import jrm.batch.TorrentChecker;
import jrm.batch.TrntChkReport;
import jrm.io.torrent.options.TrntChkMode;
import jrm.locale.Messages;
import jrm.misc.SettingsEnum;
import jrm.security.PathAbstractor;
import jrm.security.Session;
import jrm.ui.MainFrame;
import jrm.ui.basic.JRMFileChooser;
import jrm.ui.progress.Progress;

@SuppressWarnings("serial")
public class BatchTrrntChkPanel extends JPanel
{
	private JSDRDropTable tableTrntChk;
	private JComboBox<TrntChkMode> cbbxTrntChk;
	private JCheckBox cbRemoveUnknownFiles;
	private JCheckBox cbRemoveWrongSizedFiles;

	private Point popupPoint;
	private JCheckBox chckbxDetectArchivedFolder;

	/**
	 * Create the panel.
	 */
	public BatchTrrntChkPanel(final Session session)
	{
		final GridBagLayout gbl_panelBatchToolsDir2Torrent = new GridBagLayout();
		gbl_panelBatchToolsDir2Torrent.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelBatchToolsDir2Torrent.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelBatchToolsDir2Torrent.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panelBatchToolsDir2Torrent.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		setLayout(gbl_panelBatchToolsDir2Torrent);

		final JScrollPane scrollPane = new JScrollPane();
		final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 6;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		this.add(scrollPane, gbc_scrollPane);

		BatchTableModel model = new BatchTableModel(new String[] { Messages.getString("MainFrame.TorrentFiles"), Messages.getString("MainFrame.DstDirs"), Messages.getString("MainFrame.Result"), "Details", "Selected" });
		tableTrntChk = new JSDRDropTable(model, files -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr, SrcDstResult.toJSON(files)));
		model.setButtonHandler(new TableButtonPressedHandler()
		{
			
			@Override
			public void onButtonPress(int row, int column)
			{
				final SrcDstResult sdr = model.getData().get(row);
				new BatchTrrntChkResultsDialog(session, SwingUtilities.getWindowAncestor(BatchTrrntChkPanel.this), TrntChkReport.load(session, PathAbstractor.getAbsolutePath(session, sdr.src).toFile()));
			}
		});
		tableTrntChk.addMouseListener(new MouseAdapter() {
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
		((BatchTableModel) tableTrntChk.getModel()).applyColumnsWidths(tableTrntChk);
		final SDRList sdrl2 = new SDRList();
		if(session!=null)
		{
			for (final JsonValue arrv : Json.parse(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr, "[]")).asArray()) //$NON-NLS-1$ //$NON-NLS-2$
			{
				final SrcDstResult sdr = new SrcDstResult();
				final JsonObject jso = arrv.asObject();
				final JsonValue src = jso.get("src"); //$NON-NLS-1$
				if (src != Json.NULL)
					sdr.src = src.asString();
				final JsonValue dst = jso.get("dst"); //$NON-NLS-1$
				if (dst != Json.NULL)
					sdr.dst = dst.asString();
				final JsonValue result = jso.get("result"); //$NON-NLS-1$
				sdr.result = result.asString();
				sdr.selected = jso.getBoolean("selected", true); //$NON-NLS-1$
				sdrl2.add(sdr);
			}
		}
		tableTrntChk.getSDRModel().setData(sdrl2);
		tableTrntChk.setCellSelectionEnabled(false);
		tableTrntChk.setRowSelectionAllowed(true);
		tableTrntChk.getSDRModel().setSrcFilter(file -> {
			final List<String> exts = Arrays.asList("torrent"); //$NON-NLS-1$
			if (file.isFile())
				return exts.contains(FilenameUtils.getExtension(file.getName()));
			return false;
		});
		tableTrntChk.getSDRModel().setDstFilter(file -> {
			return file.isDirectory();
		});
		tableTrntChk.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tableTrntChk.setFillsViewportHeight(true);
		scrollPane.setViewportView(tableTrntChk);

		final JPopupMenu pmTrntChk = new JPopupMenu();
		MainFrame.addPopup(tableTrntChk, pmTrntChk);

		final JMenuItem mntmAddTorrent = new JMenuItem(Messages.getString("BatchToolsTrrntChkPanel.mntmAddTorrent.text"));
		mntmAddTorrent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final int col = tableTrntChk.columnAtPoint(popupPoint);
				final int row  = tableTrntChk.rowAtPoint(popupPoint);
				List<SrcDstResult> list = tableTrntChk.getSelectedValuesList();
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
									filter = tableTrntChk.getSDRModel().getDstFilter();
								else if (col == 0)
									filter = new java.io.FileFilter() {

										@Override
										public boolean accept(File file)
										{
											final List<String> exts = Arrays.asList("torrent"); //$NON-NLS-1$ //$NON-NLS-2$
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
								return col==0?"Torrent files":"Destination directories";
							}
						}),
						col == 0 ? "Choose torrent files" : "Choose destination directories",
						true).show(SwingUtilities.windowForComponent(BatchTrrntChkPanel.this), new CallBack<Void>()
				{
					@Override
					public Void call(JRMFileChooser<Void> chooser)
					{
						File[] files = chooser.getSelectedFiles();
						SDRTableModel model = tableTrntChk.getSDRModel();
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
							tableTrntChk.call();
						}
						return null;
					}
				});
			}
		});
		pmTrntChk.add(mntmAddTorrent);

		pmTrntChk.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				mntmAddTorrent.setEnabled(tableTrntChk.columnAtPoint(popupPoint) <= 1);
			}
		});

		final JMenuItem mntmDelTorrent = new JMenuItem(Messages.getString("BatchToolsTrrntChkPanel.mntmDelTorrent.text")); //$NON-NLS-1$
		mntmDelTorrent.addActionListener(e -> tableTrntChk.del(tableTrntChk.getSelectedValuesList()));
		pmTrntChk.add(mntmDelTorrent);

		final JLabel lblCheckMode = new JLabel(Messages.getString("BatchToolsTrrntChkPanel.lblCheckMode.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblCheckMode = new GridBagConstraints();
		gbc_lblCheckMode.insets = new Insets(0, 0, 0, 5);
		gbc_lblCheckMode.anchor = GridBagConstraints.EAST;
		gbc_lblCheckMode.gridx = 0;
		gbc_lblCheckMode.gridy = 1;
		this.add(lblCheckMode, gbc_lblCheckMode);

		cbbxTrntChk = new JComboBox<>();
		cbbxTrntChk.setModel(new DefaultComboBoxModel<>(TrntChkMode.values()));
		if(session!=null)
			cbbxTrntChk.setSelectedItem(TrntChkMode.valueOf(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_mode, TrntChkMode.FILENAME.toString()))); //$NON-NLS-1$
		cbbxTrntChk.addActionListener(e -> {
			session.getUser().getSettings().setProperty(SettingsEnum.trntchk_mode, cbbxTrntChk.getSelectedItem().toString());
			cbRemoveWrongSizedFiles.setEnabled(cbbxTrntChk.getSelectedItem()!=TrntChkMode.FILENAME);
		});
		final GridBagConstraints gbc_cbbxTrntChk = new GridBagConstraints();
		gbc_cbbxTrntChk.anchor = GridBagConstraints.EAST;
		gbc_cbbxTrntChk.insets = new Insets(0, 0, 0, 5);
		gbc_cbbxTrntChk.gridx = 1;
		gbc_cbbxTrntChk.gridy = 1;
		this.add(cbbxTrntChk, gbc_cbbxTrntChk);

		final JButton btnBatchToolsTrntChkStart = new JButton(Messages.getString("BatchToolsTrrntChkPanel.TrntCheckStart.text")); //$NON-NLS-1$
		btnBatchToolsTrntChkStart.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resicons/icons/bullet_go.png")));
		btnBatchToolsTrntChkStart.addActionListener((e) -> trrntChk(session));

		chckbxDetectArchivedFolder = new JCheckBox(Messages.getString("BatchTrrntChkPanel.chckbxDetectArchivedFolder.text")); //$NON-NLS-1$
		chckbxDetectArchivedFolder.addActionListener(e -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_detect_archived_folders, chckbxDetectArchivedFolder.isSelected()));
		if(session!=null)
			chckbxDetectArchivedFolder.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_detect_archived_folders, true)); //$NON-NLS-1$
		GridBagConstraints gbc_chckbxDetectArchivedFolder = new GridBagConstraints();
		gbc_chckbxDetectArchivedFolder.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxDetectArchivedFolder.gridx = 2;
		gbc_chckbxDetectArchivedFolder.gridy = 1;
		add(chckbxDetectArchivedFolder, gbc_chckbxDetectArchivedFolder);

		cbRemoveUnknownFiles = new JCheckBox(Messages.getString("BatchToolsTrrntChkPanel.chckbxRemoveUnknownFiles.text")); //$NON-NLS-1$
		cbRemoveUnknownFiles.addActionListener(e -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_remove_unknown_files, cbRemoveUnknownFiles.isSelected()));
		if(session!=null)
			cbRemoveUnknownFiles.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_remove_unknown_files, false)); //$NON-NLS-1$
		GridBagConstraints gbc_cbRemoveUnknownFiles = new GridBagConstraints();
		gbc_cbRemoveUnknownFiles.insets = new Insets(0, 0, 0, 5);
		gbc_cbRemoveUnknownFiles.gridx = 3;
		gbc_cbRemoveUnknownFiles.gridy = 1;
		add(cbRemoveUnknownFiles, gbc_cbRemoveUnknownFiles);

		cbRemoveWrongSizedFiles = new JCheckBox(Messages.getString("BatchToolsTrrntChkPanel.chckbxRemoveWrongSized.text")); //$NON-NLS-1$
		cbRemoveWrongSizedFiles.addActionListener(e -> session.getUser().getSettings().setProperty(SettingsEnum.trntchk_remove_wrong_sized_files, cbRemoveWrongSizedFiles.isSelected()));
		if(session!=null)
			cbRemoveWrongSizedFiles.setSelected(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_remove_wrong_sized_files, false)); //$NON-NLS-1$
		cbRemoveWrongSizedFiles.setEnabled(cbbxTrntChk.getSelectedItem()!=TrntChkMode.FILENAME);
		GridBagConstraints gbc_cbRemoveWrongSizedFiles = new GridBagConstraints();
		gbc_cbRemoveWrongSizedFiles.anchor = GridBagConstraints.WEST;
		gbc_cbRemoveWrongSizedFiles.insets = new Insets(0, 0, 0, 5);
		gbc_cbRemoveWrongSizedFiles.gridx = 4;
		gbc_cbRemoveWrongSizedFiles.gridy = 1;
		add(cbRemoveWrongSizedFiles, gbc_cbRemoveWrongSizedFiles);
		final GridBagConstraints gbc_btnBatchToolsTrntChkStart = new GridBagConstraints();
		gbc_btnBatchToolsTrntChkStart.anchor = GridBagConstraints.EAST;
		gbc_btnBatchToolsTrntChkStart.gridx = 5;
		gbc_btnBatchToolsTrntChkStart.gridy = 1;
		this.add(btnBatchToolsTrntChkStart, gbc_btnBatchToolsTrntChkStart);

	}

	private void trrntChk(final Session session)
	{
		final Progress progress = new Progress(SwingUtilities.getWindowAncestor(this));
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				final List<SrcDstResult> sdrl = ((SDRTableModel) tableTrntChk.getModel()).getData();
				final TrntChkMode mode = (TrntChkMode) cbbxTrntChk.getSelectedItem();
				final ResultColUpdater updater = tableTrntChk;
				final boolean detectArchivedFolders = chckbxDetectArchivedFolder.isSelected();
				final boolean removeUnknownFiles = cbRemoveUnknownFiles.isSelected();
				final boolean removeWrongSizedFiles = cbRemoveWrongSizedFiles.isSelected();
				new TorrentChecker(session, progress, sdrl, mode, updater, removeUnknownFiles, removeWrongSizedFiles, detectArchivedFolders);
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
