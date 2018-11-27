package jrm.ui.batch;

import javax.swing.tree.DefaultTreeModel;

import jrm.batch.TrntChkReport;

@SuppressWarnings("serial")
public class BatchTrrntChkReportTreeModel extends DefaultTreeModel
{

	public BatchTrrntChkReportTreeModel(TrntChkReport report)
	{
		super(report);
	}

}
