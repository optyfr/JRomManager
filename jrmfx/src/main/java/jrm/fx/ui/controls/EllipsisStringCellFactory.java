package jrm.fx.ui.controls;

import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;

/**
 * A reusable table cell factory for displaying string values with text overrun handling.
 * Supports configurable overrun style (e.g., leading ellipsis) and optional tooltips.
 *
 * @param <S> The type of the TableView items
 */
public final class EllipsisStringCellFactory<S> extends TableCell<S, String> {
    /** The text overrun style to use when text is too long. */
    private final OverrunStyle overrunStyle;
    /** Whether to show a tooltip with the full string value. */
    private final boolean showTooltip;

    /**
     * Creates a new ellipsis string cell factory.
     *
     * @param overrunStyle the text overrun style to use when text is too long
     * @param showTooltip  whether to show a tooltip with the full string value
     */
    public EllipsisStringCellFactory(OverrunStyle overrunStyle, boolean showTooltip) {
        super();
        this.overrunStyle = overrunStyle;
        this.showTooltip = showTooltip;
    }

    /**
     * Creates a new ellipsis string cell factory with tooltips enabled.
     *
     * @param overrunStyle the text overrun style to use when text is too long
     */
    public EllipsisStringCellFactory(OverrunStyle overrunStyle) {
        this(overrunStyle, true);
    }

    /**
     * Updates the cell content with the string value.
     * <p>
     * Applies the configured overrun style and shows a tooltip for non-empty cells.
     *
     * @param item  the string to display, or {@code null} for absent values
     * @param empty whether this cell represents an empty row
     */
    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText("");
            setTooltip(null);
        } else {
            setTextOverrun(overrunStyle);
            setText(item);
            if (showTooltip) {
                setTooltip(new Tooltip(item));
            }
        }
        setGraphic(null);
    }
}
