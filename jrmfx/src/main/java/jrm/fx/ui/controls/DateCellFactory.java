package jrm.fx.ui.controls;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import jrm.profile.manager.ProfileNFO;

/**
 * A table cell factory for displaying {@link Instant} timestamps in profile tables.
 * <p>
 * Formats dates as "yyyy-MM-dd HH:mm:ss" in the system time zone and shows a tooltip
 * with the full formatted date. Null values are rendered as "????-??-?? ??:??:??".
 *
 * @since 2.5
 */
public final class DateCellFactory extends TableCell<ProfileNFO, Instant> {
    /** The date formatter. */
    private final DateTimeFormatter datefmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @Override
    protected void updateItem(Instant item, boolean empty) {
        if (empty)
            setText("");
        else if (item == null) {
            setTextFill(getTableRow().isSelected() ? Color.LIGHTGRAY : Color.GRAY);
            setText("????-??-?? ??:??:??");
        } else {
            final var date = datefmt.format(item);
            setText(date);
            setTooltip(new Tooltip(date));
        }
        setGraphic(null);
    }
}
