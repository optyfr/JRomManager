package jrm.fx.ui.controls;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;

/**
 * A table cell factory that renders a button in each row.
 * <p>
 * The button invokes the supplied {@link Action} when clicked, receiving the cell
 * as a parameter so the caller can inspect the row index or item.
 *
 * @param <S> the type of the TableView items
 * @param <T> the type of the cell item
 * @since 2.5
 */
public final class ButtonCellFactory<S, T> extends TableCell<S, T> {
    /** The button displayed in the cell. */
    private final Button btn;

    /**
     * Callback interface for button actions.
     *
     * @param <S> the type of the TableView items
     * @param <T> the type of the cell item
     */
    public interface Action<S, T> {
        /**
         * Invoked when the button is clicked.
         *
         * @param cell the table cell containing the button
         */
        void doAction(TableCell<S, T> cell);
    }

    /**
     * Constructs a button cell factory.
     *
     * @param name   the button label
     * @param action the action to invoke when the button is clicked
     */
    public ButtonCellFactory(String name, Action<S, T> action) {
        super();
        btn = new Button(name);
        btn.setMinHeight(15);
        btn.setPadding(new Insets(0));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(_ -> action.doAction(this));
        setMinHeight(15);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        setGraphic(empty ? null : btn);
    }
}
