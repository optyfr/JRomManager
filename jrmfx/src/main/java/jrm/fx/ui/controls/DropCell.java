package jrm.fx.ui.controls;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import jrm.fx.ui.misc.DragNDrop;
import jrm.fx.ui.misc.DragNDrop.SetFilesCallBack;
import jrm.fx.ui.misc.SrcDstResult;

/**
 * A table cell that accepts drag-and-drop file operations.
 * <p>
 * When files are dropped onto the cell, the supplied callback is invoked with the
 * list of affected {@link SrcDstResult} rows and the dropped files.
 *
 * @since 2.5
 */
public class DropCell extends TableCell<SrcDstResult, String> {
    /**
     * Callback interface for drop operations.
     */
    public interface DropCellCallback {
        /**
         * Invoked when files are dropped.
         *
         * @param sdrlist the affected source/destination result rows
         * @param files   the dropped files
         */
        void call(List<SrcDstResult> sdrlist, List<File> files);
    }

    /**
     * Constructs a drop cell.
     *
     * @param view   the table view containing this cell
     * @param cb     the callback to invoke on drop
     * @param filter the predicate for accepting files
     */
    public DropCell(TableView<SrcDstResult> view, DropCell.DropCellCallback cb, Predicate<File> filter) {
        final SetFilesCallBack drop = files -> process(view, getIndex(), files, cb);
        new DragNDrop(this).addFiltered(filter, drop);
    }

    /**
     * Processes a drop operation by mapping files to table rows and invoking the callback.
     *
     * @param view       the table view
     * @param startIndex the starting row index
     * @param files      the dropped files
     * @param cb         the callback to invoke
     */
    public static void process(TableView<SrcDstResult> view, int startIndex, List<File> files, DropCell.DropCellCallback cb) {
        int count = view.getItems().size();
        if (startIndex > count || startIndex < 0)
            startIndex = count;
        final var sdrlist = new ArrayList<SrcDstResult>();
        for (int i = 0; i < files.size(); i++) {
            if (startIndex + i >= count)
                view.getItems().add(new SrcDstResult());
            sdrlist.add(view.getItems().get(startIndex + i));
        }
        cb.call(sdrlist, files);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty)
            setText("");
        else {
            setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
            setText(item);
            setTooltip(new Tooltip(item));
        }
        setGraphic(null);
    }
}