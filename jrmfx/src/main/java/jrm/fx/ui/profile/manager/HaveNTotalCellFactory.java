package jrm.fx.ui.profile.manager;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import jrm.profile.manager.ProfileNFOStats.HaveNTotal;

/**
 * A table cell factory for displaying have/total statistics with color coding.
 * <p>
 * Shows the have count in a color based on the percentage (red for low, green for high)
 * and the total count in black or white depending on row selection. Null values are
 * displayed as "?" or "?/?".
 *
 * @param <T> the type of the TableView items
 * @since 2.5
 */
public final class HaveNTotalCellFactory<T> extends TableCell<T, HaveNTotal> {

    @Override
    protected void updateItem(HaveNTotal hnt, boolean empty) {
        setGraphic(null);
        setStyle("-fx-text-fill: #000000;");
        setAlignment(Pos.CENTER);
        setMinHeight(USE_PREF_SIZE);
        setPrefHeight(USE_COMPUTED_SIZE);
        if (hnt == null || empty) {
            setText("");
            return;
        }
        if (hnt.getHave() == null)
            updateHaveNull(hnt);
        else
            updateHaveNotNull(hnt);
    }

    /**
     * Updates the cell when have is null.
     *
     * @param hnt the have/total statistics
     */
    private void updateHaveNull(HaveNTotal hnt) {
        if (hnt.getTotal() == null) {
            setTextFill(getTableRow().isSelected() ? Color.LIGHTGRAY : Color.GRAY);
            setText("?/?");
            return;
        }
        final var have = new Text("?");
        final var total = new Text(String.format("/%d", hnt.getTotal()));
        have.setFill(getTableRow().isSelected() ? Color.LIGHTGRAY : Color.GRAY);
        total.setFill(getTableRow().isSelected() ? Color.WHITE : Color.BLACK);
        setGraphic(buildHBox(have, total));
    }

    /**
     * Updates the cell when have is not null.
     *
     * @param hnt the have/total statistics
     */
    private void updateHaveNotNull(HaveNTotal hnt) {
        final var have = new Text(hnt.getHave().toString());
        final var total = new Text(String.format("/%d", hnt.getTotal()));
        total.setFill(getTableRow().isSelected() ? Color.WHITE : Color.BLACK);
        have.setFill(getHaveColor(hnt));
        setGraphic(buildHBox(have, total));
    }

    /**
     * Returns the color for the have count based on percentage.
     *
     * @param hnt the have/total statistics
     * @return the color
     */
    private Color getHaveColor(HaveNTotal hnt) {
        final var selected = getTableRow().isSelected();
        if (hnt.getHave() == 0 && hnt.getTotal() > 0)
            return selected ? Color.web("#ffaaaa") : Color.web("#cc0000");
        if (hnt.getHave().equals(hnt.getTotal()))
            return selected ? Color.web("#aaffaa") : Color.web("#00aa00");
        return selected ? Color.web("#ffaa88") : Color.web("#cc8800");
    }

    private HBox buildHBox(Text have, Text total) {
        final var tf = new HBox();
        tf.getChildren().addAll(have, total);
        tf.setMinWidth(USE_PREF_SIZE);
        tf.setPrefWidth(USE_COMPUTED_SIZE);
        tf.setPrefHeight(USE_COMPUTED_SIZE);
        tf.setAlignment(Pos.CENTER);
        return tf;
    }
}