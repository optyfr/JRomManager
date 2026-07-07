package jrm.fx.ui.controls;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

/**
 * A table cell factory for displaying version strings.
 * <p>
 * Null versions are rendered as "???" in gray; non-null values are shown with a tooltip.
 *
 * @param <T> the type of the TableView items
 * @since 2.5
 */
public final class VersionCellFactory<T> extends TableCell<T, String> {
    @Override
    protected void updateItem(String item, boolean empty) {
        if (empty)
            setText("");
        else if (item == null) {
            setTextFill(getTableRow().isSelected() ? Color.LIGHTGRAY : Color.GRAY);
            setText("???");
        } else {
            setText(item);
            setTooltip(new Tooltip(item));
        }
        setGraphic(null);
        setAlignment(Pos.CENTER_LEFT);
    }
}