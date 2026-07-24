package jrm.fx.ui.controls;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

/**
 * A reusable table cell factory for displaying integer values with custom color and alignment.
 * Supports optional tooltips and is commonly used for statistics columns in result tables.
 *
 * @param <S> The type of the TableView items
 */
public final class ColoredIntegerCellFactory<S> extends TableCell<S, Integer> {
    /** The color to use for the text, or null for default color. */
    private final Color textFill;
    /** The alignment for the cell content. */
    private final Pos alignment;
    /** Whether to show a tooltip with the integer value. */
    private final boolean showTooltip;

    /**
     * Creates a new colored integer cell factory.
     *
     * @param textFill    the color to use for the text, or null for default color
     * @param alignment   the alignment for the cell content
     * @param showTooltip whether to show a tooltip with the integer value
     */
    public ColoredIntegerCellFactory(Color textFill, Pos alignment, boolean showTooltip) {
        super();
        this.textFill = textFill;
        this.alignment = alignment;
        this.showTooltip = showTooltip;
    }

    /**
     * Creates a new colored integer cell factory with tooltips enabled.
     *
     * @param textFill  the color to use for the text, or null for default color
     * @param alignment the alignment for the cell content
     */
    public ColoredIntegerCellFactory(Color textFill, Pos alignment) {
        this(textFill, alignment, true);
    }

    /**
     * Updates the cell content with the integer value.
     * <p>
     * Applies the configured color, alignment, and tooltip for non-empty cells.
     *
     * @param item  the integer to display, or {@code null} for empty or absent values
     * @param empty whether this cell represents an empty row
     */
    @Override
    protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText("");
            setTooltip(null);
        } else {
            if (textFill != null) {
                setTextFill(textFill);
            }
            setAlignment(alignment);
            setText(item.toString());
            if (showTooltip) {
                setTooltip(new Tooltip(item.toString()));
            }
        }
        setGraphic(null);
    }
}
