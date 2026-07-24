package jrm.fx.ui.controls;

import javafx.scene.control.ListCell;
import jrm.profile.scan.options.Descriptor;

/**
 * A list cell factory for displaying {@link Descriptor} enum values.
 * <p>
 * Shows the human-readable description from {@link Descriptor#getDesc()}.
 *
 * @since 2.5
 */
public final class DescriptorCellFactory extends ListCell<Descriptor> {
    /**
     * Updates the cell content with the human-readable descriptor text.
     * <p>
     * Shows the value of {@link Descriptor#getDesc()} for non-empty cells, or hides
     * the graphic for empty items.
     *
     * @param item  the descriptor to display, or {@code null}
     * @param empty whether this cell represents an empty row
     */
    @Override
    protected void updateItem(Descriptor item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty)
            setGraphic(null);
        else
            setText(item.getDesc());
    }
}