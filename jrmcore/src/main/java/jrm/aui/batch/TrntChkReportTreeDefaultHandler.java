package jrm.aui.batch;

import jrm.aui.profile.report.ReportTreeGenericHandler;
import jrm.batch.TrntChkReport;

/**
 * TrntChkReportTreeDefaultHandler is a concrete implementation of ReportTreeGenericHandler that handles the tree structure for TrntChkReport. It provides a constructor that takes a TrntChkReport object as the root of the tree and initializes the handler with it.
 */
public class TrntChkReportTreeDefaultHandler extends ReportTreeGenericHandler<TrntChkReport> {
    /**
     * Constructs a new TrntChkReportTreeDefaultHandler with the specified TrntChkReport as the root of the tree.
     *
     * @param root the TrntChkReport object to be used as the root of the tree
     */
    public TrntChkReportTreeDefaultHandler(final TrntChkReport root) {
        super(root);
    }
}
