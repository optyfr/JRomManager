package jrm.fx.ui.controls;

import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

/**
 * An editable text field table cell with tooltip support.
 * <p>
 * Displays the item as text with a tooltip showing the full value, and allows
 * inline editing when the table is editable.
 *
 * @param <T> the type of the cell item
 * @since 2.5
 */
public class NameCellFactory<T> extends TextFieldTableCell<T, T> {
    /**
     * Constructs a name cell factory with a custom converter.
     *
     * @param converter the string converter
     */
    public NameCellFactory(StringConverter<T> converter) {
        super(converter);
        setEditable(true);
    }

    /**
     * Constructs a name cell factory with the default converter.
     */
    public NameCellFactory() {
        super();
        setEditable(true);
    }

    /**
     * Updates the cell content with the item's string representation.
     * <p>
     * Shows the text and a tooltip for non-empty cells, or clears the cell for empty rows.
     *
     * @param item  the item to display
     * @param empty whether this cell represents an empty row
     */
    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty)
            setText("");
        else {
            setText(item.toString());
            setTooltip(new Tooltip(item.toString()));
        }
        setGraphic(null);
    }
}