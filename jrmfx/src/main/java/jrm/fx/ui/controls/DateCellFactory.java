package jrm.fx.ui.controls;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import jrm.profile.manager.ProfileNFO;

public final class DateCellFactory extends TableCell<ProfileNFO, Instant> {
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
