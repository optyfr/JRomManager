package jrm.fx.ui.misc;

import java.io.File;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import jrm.security.PathAbstractor;
import jrm.security.Sessions;

public class DragNDrop {
    private final String styleAccept;
    private final String styleReject;

    private Control control;

    public DragNDrop(Control control) {
        if (control instanceof Cell) {
            styleAccept = "-fx-background-color: #DDFFDD;";
            styleReject = "-fx-background-color: #FFDDDD;";
        } else {
            styleAccept = "-fx-control-inner-background: #DDFFDD;";
            styleReject = "-fx-control-inner-background: #FFDDDD;";
        }
        this.control = control;
    }

    @FunctionalInterface
    public interface SetCallBack {
        public void call(String txt);
    }

    public interface SetFilesCallBack {
        public void call(List<File> files);
    }

    public void addFile(SetCallBack cb) {
        addFiltered(f -> Files.isRegularFile(f.toPath()), cb);
    }

    public void addNewFile(SetCallBack cb) {
        addFiltered(f -> (!Files.exists(f.toPath())) || Files.isRegularFile(f.toPath()), cb);
    }

    public void addAny(SetFilesCallBack cb) {
        addFiltered(_ -> true, cb);
    }

    public void addDir(SetCallBack cb) {
        addFiltered(f -> Files.isDirectory(f.toPath()), cb);
    }

    public void addDirs(SetFilesCallBack cb) {
        addFiltered(f -> Files.isDirectory(f.toPath()), cb);
    }

    /**
     * Installs drag-and-drop handlers that accept every dragged file satisfying {@code filter}
     * and forward the resulting file list to {@code cb}.
     *
     * @param filter the predicate every dropped file must satisfy
     * @param cb     the callback receiving the accepted files
     */
    public void addFiltered(Predicate<File> filter, SetFilesCallBack cb) {
        control.setOnDragOver(event -> handleDragOverFiles(event, filter));
        control.setOnDragExited(_ -> control.setStyle(""));
        control.setOnDragDropped(event -> handleDragDroppedFiles(event, filter, cb));
    }

    /**
     * Installs drag-and-drop handlers that accept a single dragged file satisfying {@code filter}.
     * When the control is a {@link TextField}, live validation of the typed text is also installed.
     *
     * @param filter the predicate the single dropped file must satisfy
     * @param cb     the callback receiving the accepted file path as a string
     */
    public void addFiltered(Predicate<File> filter, SetCallBack cb) {
        if (control instanceof TextField tf) {
            installTextValidator(tf, filter, cb);
        }
        control.setOnDragOver(event -> handleDragOverSingle(event, filter));
        control.setOnDragExited(_ -> control.setStyle(""));
        control.setOnDragDropped(event -> handleDragDroppedSingle(event, filter, cb));
    }

    /**
     * Highlights the control as an accept target when every dragged file satisfies {@code filter}.
     *
     * @param event  the drag-over event
     * @param filter the predicate every dragged file must satisfy
     */
    private void handleDragOverFiles(DragEvent event, Predicate<File> filter) {
        if (event.getGestureSource() != control) {
            final var db = event.getDragboard();
            if (db.hasFiles() && db.getFiles().stream().allMatch(filter)) {
                event.acceptTransferModes(TransferMode.COPY);
                control.setStyle(styleAccept);
                return;
            }
        }
        control.setStyle(styleReject);
        event.consume();
    }

    /**
     * Performs the drop for the multi-file variant, delegating list merging when the control is a
     * {@link ListView}.
     *
     * @param event  the drop event
     * @param filter the predicate every dropped file must satisfy
     * @param cb     the callback receiving the accepted files
     */
    @SuppressWarnings("unchecked")
    private void handleDragDroppedFiles(DragEvent event, Predicate<File> filter, SetFilesCallBack cb) {
        final var db = event.getDragboard();
        var success = false;
        if (db.hasFiles() && db.getFiles().stream().allMatch(filter)) {
            if (control instanceof ListView) {
                appendFilesToListView(db.getFiles());
                cb.call(((ListView<File>) control).getItems());
            } else {
                cb.call(db.getFiles());
            }
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Appends each file from {@code files} to the bound {@link ListView}, skipping duplicates while
     * preserving insertion order.
     *
     * @param files the files to append
     */
    @SuppressWarnings("unchecked")
    private void appendFilesToListView(List<File> files) {
        final var lv = (ListView<File>) control;
        final var set = new LinkedHashSet<>(lv.getItems());
        for (final var f : files) {
            if (!set.contains(f)) {
                lv.getItems().add(f);
            }
        }
    }

    /**
     * Highlights the control as an accept target when exactly one dragged file satisfies
     * {@code filter}.
     *
     * @param event  the drag-over event
     * @param filter the predicate the single dragged file must satisfy
     */
    private void handleDragOverSingle(DragEvent event, Predicate<File> filter) {
        if (event.getGestureSource() != control) {
            final var db = event.getDragboard();
            if (db.hasFiles() && db.getFiles().size() == 1 && filter.test(db.getFiles().get(0))) {
                event.acceptTransferModes(TransferMode.COPY);
                control.setStyle(styleAccept);
                return;
            }
        }
        control.setStyle(styleReject);
        event.consume();
    }

    /**
     * Performs the drop for the single-file variant, updating a {@link TextInputControl} when
     * applicable before invoking {@code cb}.
     *
     * @param event  the drop event
     * @param filter the predicate the single dropped file must satisfy
     * @param cb     the callback receiving the accepted file path as a string
     */
    private void handleDragDroppedSingle(DragEvent event, Predicate<File> filter, SetCallBack cb) {
        final var db = event.getDragboard();
        var success = false;
        if (db.hasFiles() && db.getFiles().size() == 1 && filter.test(db.getFiles().get(0))) {
            if (control instanceof TextInputControl tic) {
                tic.setText(db.getFiles().get(0).toString());
            }
            cb.call(db.getFiles().get(0).toString());
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Installs a listener that validates {@code tf}'s text against {@code filter} as the user types.
     *
     * @param tf     the text field to validate
     * @param filter the predicate the resolved path must satisfy
     * @param cb     the callback invoked with valid text
     */
    private void installTextValidator(TextField tf, Predicate<File> filter, SetCallBack cb) {
        tf.textProperty().addListener((_, _, newValue) -> validateText(tf, newValue, filter, cb));
    }

    /**
     * Validates {@code value} resolved through {@link PathAbstractor} against {@code filter} and
     * updates the field color accordingly, invoking {@code cb} when valid.
     *
     * @param tf     the originating text field
     * @param value  the current text value
     * @param filter the predicate the resolved path must satisfy
     * @param cb     the callback invoked with valid text
     */
    private void validateText(TextField tf, String value, Predicate<File> filter, SetCallBack cb) {
        if (value == null || value.isBlank() || tf.isDisabled()) {
            tf.setStyle(null);
        } else if (filter.test(PathAbstractor.getAbsolutePath(Sessions.getSingleSession(), value).toFile())) {
            tf.setStyle("-fx-text-inner-color: green;");
            cb.call(value);
        } else {
            tf.setStyle("-fx-text-inner-color: red;");
        }
    }
}
