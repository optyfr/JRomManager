package jrm.ui.profile.report;

import javax.swing.tree.TreeNode;

import jrm.profile.report.ReportIntf;
import lombok.Getter;

/**
 * Generic base class for report tree nodes.
 * <p>
 * Provides common functionality for tree nodes representing report data.
 */
public abstract class ReportNodeGeneric<T extends ReportIntf<T>> implements TreeNode {
    /**
     * The report data.
     * @return the report
     */
    protected final @Getter T report;

    protected ReportNodeGeneric(final T report) {
        this.report = report;
    }

    @Override
    public TreeNode getParent() {
        return null;
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }
}
