package jrm.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import jrm.locale.Messages;

@SuppressWarnings("serial")
public class BatchToolsPanel extends JPanel
{
	/**
	 * Create the panel.
	 */
	public BatchToolsPanel()
	{
		this.setLayout(new BorderLayout(0, 0));

		JTabbedPane batchToolsTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		this.add(batchToolsTabbedPane);

		BatchToolsDirUpd8rPanel panelBatchToolsDat2Dir = new BatchToolsDirUpd8rPanel();
		batchToolsTabbedPane.addTab(Messages.getString("MainFrame.panelBatchToolsDat2Dir.title"), null, panelBatchToolsDat2Dir, null); //$NON-NLS-1$

		BatchToolsTrrntChkPanel panelBatchToolsDir2Torrent = new BatchToolsTrrntChkPanel();
		batchToolsTabbedPane.addTab(Messages.getString("MainFrame.panelBatchToolsDir2Torrent.title"), null, panelBatchToolsDir2Torrent, null); //$NON-NLS-1$
	}

}
