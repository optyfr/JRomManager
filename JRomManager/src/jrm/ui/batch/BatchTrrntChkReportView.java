package jrm.ui.batch;

import javax.swing.JScrollPane;
import javax.swing.JTree;

import jrm.batch.TrntChkReport;
import jrm.security.Session;
import jrm.ui.profile.report.ReportTreeCellRenderer;

@SuppressWarnings("serial")
public class BatchTrrntChkReportView extends JScrollPane
{
	public BatchTrrntChkReportView(Session session, TrntChkReport report)
	{
		final JTree tree = new JTree();
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setModel(new BatchTrrntChkReportTreeModel(report));
		tree.setCellRenderer(new ReportTreeCellRenderer());
		this.setViewportView(tree);
	}

}
