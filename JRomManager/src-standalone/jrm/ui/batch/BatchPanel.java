package jrm.ui.batch;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import jrm.locale.Messages;
import jrm.security.Session;
import jrm.ui.MainFrame;

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
		batchToolsTabbedPane.addTab(Messages.getString("MainFrame.panelBatchToolsDat2Dir.title"), new ImageIcon(MainFrame.class.getResource("/jrm/resicons/icons/application_cascade.png")), panelBatchToolsDat2Dir, null); //$NON-NLS-1$

		BatchTrrntChkPanel panelBatchToolsDir2Torrent = new BatchTrrntChkPanel(session);
		batchToolsTabbedPane.addTab(Messages.getString("MainFrame.panelBatchToolsDir2Torrent.title"), new ImageIcon(MainFrame.class.getResource("/jrm/resicons/icons/drive_web.png")), panelBatchToolsDir2Torrent, null); //$NON-NLS-1$
		
		BatchCompressorPanel panelBatchToolsCompressor = new BatchCompressorPanel(session);
		batchToolsTabbedPane.addTab(Messages.getString("BatchPanel.Compressor"), new ImageIcon(MainFrame.class.getResource("/jrm/resicons/icons/compress.png")), panelBatchToolsCompressor, null); //$NON-NLS-1$
	}

}
