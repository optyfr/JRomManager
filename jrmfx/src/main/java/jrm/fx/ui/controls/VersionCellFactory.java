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
    /**
     * Updates the cell content with the version string.
     * <p>
     * Null values are rendered as {@code ???} in gray; valid values are shown with a tooltip.
     *
     * @param item  the version string to display, or {@code null}
     * @param empty whether this cell represents an empty row
     */
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