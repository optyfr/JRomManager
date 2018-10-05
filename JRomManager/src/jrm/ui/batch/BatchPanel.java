package jrm.ui.batch;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import jrm.locale.Messages;
import jrm.security.Session;

@SuppressWarnings("serial")
public class BatchPanel extends JPanel
{
	/**
	 * Create the panel.
	 */
	public BatchPanel(final Session session)
	{
		this.setLayout(new BorderLayout(0, 0));

		JTabbedPane batchToolsTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		this.add(batchToolsTabbedPane);

		BatchDirUpd8rPanel panelBatchToolsDat2Dir = new BatchDirUpd8rPanel(session);
		batchToolsTabbedPane.addTab(Messages.getString("MainFrame.panelBatchToolsDat2Dir.title"), null, panelBatchToolsDat2Dir, null); //$NON-NLS-1$

		BatchTrrntChkPanel panelBatchToolsDir2Torrent = new BatchTrrntChkPanel(session);
		batchToolsTabbedPane.addTab(Messages.getString("MainFrame.panelBatchToolsDir2Torrent.title"), null, panelBatchToolsDir2Torrent, null); //$NON-NLS-1$
	}

}
