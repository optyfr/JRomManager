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
    @Override
    protected void updateItem(Descriptor item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty)
            setGraphic(null);
        else
            setText(item.getDesc());
    }
}