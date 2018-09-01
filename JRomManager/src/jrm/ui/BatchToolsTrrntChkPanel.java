package jrm.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.io.FilenameUtils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import jrm.batch.TorrentChecker;
import jrm.io.torrent.options.TrntChkMode;
import jrm.locale.Messages;
import jrm.misc.Settings;
import jrm.ui.basic.JSDRDropTable;
import jrm.ui.basic.ResultColUpdater;
import jrm.ui.basic.SDRTableModel;
import jrm.ui.basic.SrcDstResult;
import jrm.ui.batch.BatchTableModel;
import jrm.ui.progress.Progress;
import javax.swing.JCheckBox;

@SuppressWarnings("serial")
public class BatchToolsTrrntChkPanel extends JPanel
{
	private JSDRDropTable tableTrntChk;
	private JComboBox<TrntChkMode> cbbxTrntChk;
	private JCheckBox cbRemoveUnknownFiles;
	private JCheckBox cbRemoveWrongSizedFiles;

	/**
	 * Create the panel.
	 */
	public BatchToolsTrrntChkPanel()
	{
		final GridBagLayout gbl_panelBatchToolsDir2Torrent = new GridBagLayout();
		gbl_panelBatchToolsDir2Torrent.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panelBatchToolsDir2Torrent.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelBatchToolsDir2Torrent.columnWeights = new double[] { 1.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panelBatchToolsDir2Torrent.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		setLayout(gbl_panelBatchToolsDir2Torrent);

		final JScrollPane scrollPane = new JScrollPane();
		final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 5;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		this.add(scrollPane, gbc_scrollPane);

		tableTrntChk = new JSDRDropTable(new BatchTableModel(new String[] { Messages.getString("MainFrame.TorrentFiles"), Messages.getString("MainFrame.DstDirs"), Messages.getString("MainFrame.Result"), "Selected" }), files -> Settings.setProperty("trntchk.sdr", SrcDstResult.toJSON(files)));
		((BatchTableModel) tableTrntChk.getModel()).applyColumnsWidths(tableTrntChk);
		final List<SrcDstResult> sdrl2 = new ArrayList<>();
		for (final JsonValue arrv : Json.parse(Settings.getProperty("trntchk.sdr", "[]")).asArray()) //$NON-NLS-1$ //$NON-NLS-2$
		{
			final SrcDstResult sdr = new SrcDstResult();
			final JsonObject jso = arrv.asObject();
			final JsonValue src = jso.get("src"); //$NON-NLS-1$
			if (src != Json.NULL)
				sdr.src = new File(src.asString());
			final JsonValue dst = jso.get("dst"); //$NON-NLS-1$
			if (dst != Json.NULL)
				sdr.dst = new File(dst.asString());
			final JsonValue result = jso.get("result"); //$NON-NLS-1$
			sdr.result = result.asString();
			sdr.selected = jso.getBoolean("selected", true); //$NON-NLS-1$
			sdrl2.add(sdr);
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

		final JMenuItem mntmAddTorrent = new JMenuItem(Messages.getString("BatchToolsTrrntChkPanel.mntmAddTorrent.text")); //$NON-NLS-1$
		mntmAddTorrent.setEnabled(false);
		pmTrntChk.add(mntmAddTorrent);

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
		cbbxTrntChk.setSelectedItem(TrntChkMode.valueOf(Settings.getProperty("trntchk.mode", TrntChkMode.FILENAME.toString()))); //$NON-NLS-1$
		cbbxTrntChk.addActionListener(e -> {
			Settings.setProperty("trntchk.mode", cbbxTrntChk.getSelectedItem().toString());
			cbRemoveWrongSizedFiles.setEnabled(cbbxTrntChk.getSelectedItem()!=TrntChkMode.FILENAME);
		});
		final GridBagConstraints gbc_cbbxTrntChk = new GridBagConstraints();
		gbc_cbbxTrntChk.anchor = GridBagConstraints.EAST;
		gbc_cbbxTrntChk.insets = new Insets(0, 0, 0, 5);
		gbc_cbbxTrntChk.gridx = 1;
		gbc_cbbxTrntChk.gridy = 1;
		this.add(cbbxTrntChk, gbc_cbbxTrntChk);

		final JButton btnBatchToolsTrntChkStart = new JButton(Messages.getString("BatchToolsTrrntChkPanel.TrntCheckStart.text")); //$NON-NLS-1$
		btnBatchToolsTrntChkStart.addActionListener((e) -> trrntChk());

		cbRemoveUnknownFiles = new JCheckBox(Messages.getString("BatchToolsTrrntChkPanel.chckbxRemoveUnknownFiles.text")); //$NON-NLS-1$
		cbRemoveUnknownFiles.addActionListener(e -> Settings.setProperty("trntchk.remove_unknown_files", cbRemoveUnknownFiles.isSelected()));
		cbRemoveUnknownFiles.setSelected(Settings.getProperty("trntchk.remove_unknown_files", false)); //$NON-NLS-1$
		GridBagConstraints gbc_cbRemoveUnknownFiles = new GridBagConstraints();
		gbc_cbRemoveUnknownFiles.insets = new Insets(0, 0, 0, 5);
		gbc_cbRemoveUnknownFiles.gridx = 2;
		gbc_cbRemoveUnknownFiles.gridy = 1;
		add(cbRemoveUnknownFiles, gbc_cbRemoveUnknownFiles);

		cbRemoveWrongSizedFiles = new JCheckBox(Messages.getString("BatchToolsTrrntChkPanel.chckbxRemoveWrongSized.text")); //$NON-NLS-1$
		cbRemoveWrongSizedFiles.addActionListener(e -> Settings.setProperty("trntchk.remove_wrong_sized_files", cbRemoveWrongSizedFiles.isSelected()));
		cbRemoveWrongSizedFiles.setSelected(Settings.getProperty("trntchk.remove_wrong_sized_files", false)); //$NON-NLS-1$
		cbRemoveWrongSizedFiles.setEnabled(cbbxTrntChk.getSelectedItem()!=TrntChkMode.FILENAME);
		GridBagConstraints gbc_cbRemoveWrongSizedFiles = new GridBagConstraints();
		gbc_cbRemoveWrongSizedFiles.anchor = GridBagConstraints.WEST;
		gbc_cbRemoveWrongSizedFiles.insets = new Insets(0, 0, 0, 5);
		gbc_cbRemoveWrongSizedFiles.gridx = 3;
		gbc_cbRemoveWrongSizedFiles.gridy = 1;
		add(cbRemoveWrongSizedFiles, gbc_cbRemoveWrongSizedFiles);
		final GridBagConstraints gbc_btnBatchToolsTrntChkStart = new GridBagConstraints();
		gbc_btnBatchToolsTrntChkStart.anchor = GridBagConstraints.EAST;
		gbc_btnBatchToolsTrntChkStart.gridx = 4;
		gbc_btnBatchToolsTrntChkStart.gridy = 1;
		this.add(btnBatchToolsTrntChkStart, gbc_btnBatchToolsTrntChkStart);

	}

	private void trrntChk()
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
				final boolean removeUnknownFiles = cbRemoveUnknownFiles.isSelected();
				final boolean removeWrongSizedFiles = cbRemoveWrongSizedFiles.isSelected();
				new TorrentChecker(progress, sdrl, mode, updater, removeUnknownFiles, removeWrongSizedFiles);
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
