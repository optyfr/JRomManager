package jrm.aui.profile.report;

import jrm.profile.report.Report;

/**
 * ReportTreeDefaultHandler is a concrete implementation of ReportTreeGenericHandler that handles the tree structure for Report. It provides a constructor that takes a Report object as the root of the tree and initializes the handler with it.
 */
public class ReportTreeDefaultHandler extends ReportTreeGenericHandler<Report> {
    /**
     * Constructs a new ReportTreeDefaultHandler with the specified Report as the root of the tree.
     *
     * @param root the Report object to be used as the root of the tree
     */
    public ReportTreeDefaultHandler(final Report root) {
        super(root);
    }
}
