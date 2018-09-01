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

@SuppressWarnings("serial")
public class BatchToolsTrrntChkPanel extends JPanel
{
	private JSDRDropTable tableBatchToolsTrntChk;
	private JComboBox<TrntChkMode> cbBatchToolsTrntChk;

	/**
	 * Create the panel.
	 */
	public BatchToolsTrrntChkPanel()
	{
		final GridBagLayout gbl_panelBatchToolsDir2Torrent = new GridBagLayout();
		gbl_panelBatchToolsDir2Torrent.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelBatchToolsDir2Torrent.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelBatchToolsDir2Torrent.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelBatchToolsDir2Torrent.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		setLayout(gbl_panelBatchToolsDir2Torrent);

		final JScrollPane scrollPane = new JScrollPane();
		final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		this.add(scrollPane, gbc_scrollPane);

		tableBatchToolsTrntChk = new JSDRDropTable(new BatchTableModel(new String[] { Messages.getString("MainFrame.TorrentFiles"), Messages.getString("MainFrame.DstDirs"), Messages.getString("MainFrame.Result"), "Selected" }), files -> Settings.setProperty("trntchk.sdr", SrcDstResult.toJSON(files)));
		((BatchTableModel) tableBatchToolsTrntChk.getModel()).applyColumnsWidths(tableBatchToolsTrntChk);
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
		tableBatchToolsTrntChk.getSDRModel().setData(sdrl2);
		tableBatchToolsTrntChk.setCellSelectionEnabled(false);
		tableBatchToolsTrntChk.setRowSelectionAllowed(true);
		tableBatchToolsTrntChk.getSDRModel().setSrcFilter(file -> {
			final List<String> exts = Arrays.asList("torrent"); //$NON-NLS-1$
			if (file.isFile())
				return exts.contains(FilenameUtils.getExtension(file.getName()));
			return false;
		});
		tableBatchToolsTrntChk.getSDRModel().setDstFilter(file -> {
			return file.isDirectory();
		});
		tableBatchToolsTrntChk.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tableBatchToolsTrntChk.setFillsViewportHeight(true);
		scrollPane.setViewportView(tableBatchToolsTrntChk);

		final JPopupMenu pmTrntChk = new JPopupMenu();
		MainFrame.addPopup(tableBatchToolsTrntChk, pmTrntChk);

		final JMenuItem mntmAddTorrent = new JMenuItem(Messages.getString("MainFrame.mntmAddTorrent.text")); //$NON-NLS-1$
		mntmAddTorrent.setEnabled(false);
		pmTrntChk.add(mntmAddTorrent);

		final JMenuItem mntmDelTorrent = new JMenuItem(Messages.getString("MainFrame.mntmDelTorrent.text")); //$NON-NLS-1$
		mntmDelTorrent.addActionListener(e -> tableBatchToolsTrntChk.del(tableBatchToolsTrntChk.getSelectedValuesList()));
		pmTrntChk.add(mntmDelTorrent);

		final JLabel lblCheckMode = new JLabel(Messages.getString("MainFrame.lblCheckMode.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblCheckMode = new GridBagConstraints();
		gbc_lblCheckMode.insets = new Insets(0, 0, 0, 5);
		gbc_lblCheckMode.anchor = GridBagConstraints.EAST;
		gbc_lblCheckMode.gridx = 0;
		gbc_lblCheckMode.gridy = 1;
		this.add(lblCheckMode, gbc_lblCheckMode);

		cbBatchToolsTrntChk = new JComboBox<>();
		cbBatchToolsTrntChk.addActionListener(e -> Settings.setProperty("trntchk.mode", cbBatchToolsTrntChk.getSelectedItem().toString()));
		cbBatchToolsTrntChk.setModel(new DefaultComboBoxModel<>(TrntChkMode.values()));
		cbBatchToolsTrntChk.setSelectedItem(TrntChkMode.valueOf(Settings.getProperty("trntchk.mode", TrntChkMode.FILENAME.toString()))); //$NON-NLS-1$
		final GridBagConstraints gbc_cbBatchToolsTrntChk = new GridBagConstraints();
		gbc_cbBatchToolsTrntChk.anchor = GridBagConstraints.EAST;
		gbc_cbBatchToolsTrntChk.insets = new Insets(0, 0, 0, 5);
		gbc_cbBatchToolsTrntChk.gridx = 1;
		gbc_cbBatchToolsTrntChk.gridy = 1;
		this.add(cbBatchToolsTrntChk, gbc_cbBatchToolsTrntChk);

		final JButton btnBatchToolsTrntChkStart = new JButton(Messages.getString("MainFrame.btnStart_1.text")); //$NON-NLS-1$
		btnBatchToolsTrntChkStart.addActionListener((e) -> trrntChk());
		final GridBagConstraints gbc_btnBatchToolsTrntChkStart = new GridBagConstraints();
		gbc_btnBatchToolsTrntChkStart.anchor = GridBagConstraints.EAST;
		gbc_btnBatchToolsTrntChkStart.gridx = 2;
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
				final List<SrcDstResult> sdrl = ((SDRTableModel) tableBatchToolsTrntChk.getModel()).getData();
				final TrntChkMode mode = (TrntChkMode) cbBatchToolsTrntChk.getSelectedItem();
				final ResultColUpdater updater = tableBatchToolsTrntChk;
				new TorrentChecker(progress, sdrl, mode, updater);
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
