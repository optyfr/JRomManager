package jrm.fx.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import javafx.beans.value.ObservableValueBase;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jrm.batch.DirUpdaterResults.DirUpdaterResult;
import jrm.fx.ui.controls.ButtonCellFactory;
import jrm.fx.ui.controls.ColoredIntegerCellFactory;
import jrm.fx.ui.controls.EllipsisStringCellFactory;
import jrm.fx.ui.profile.report.ReportLite;
import jrm.misc.Log;
import jrm.profile.report.Report;
import jrm.profile.report.Report.Stats;
import lombok.Getter;

/**
 * FXML controller for the batch directory update results dialog.
 * <p>
 * Populates a table with per-DAT statistics (have, create, fix, miss, total) and provides a button to open the detailed report for
 * each entry.
 *
 * @since 2.5
 */
public class BatchDirUpd8rResultsController extends BaseController {
    /** The result table. */
    @FXML
    private @Getter TableView<DirUpdaterResult> resultList;
    /** Column displaying the DAT name. */
    @FXML
    private TableColumn<DirUpdaterResult, String> datCol;
    /** Column displaying the number of sets found. */
    @FXML
    private TableColumn<DirUpdaterResult, Integer> haveCol;
    /** Column displaying the number of sets created. */
    @FXML
    private TableColumn<DirUpdaterResult, Integer> createCol;
    /** Column displaying the number of sets fixed. */
    @FXML
    private TableColumn<DirUpdaterResult, Integer> fixCol;
    /** Column displaying the number of sets missing. */
    @FXML
    private TableColumn<DirUpdaterResult, Integer> missCol;
    /** Column displaying the total number of sets. */
    @FXML
    private TableColumn<DirUpdaterResult, Integer> totalCol;
    /** Column with a button to open the detailed report. */
    @FXML
    private TableColumn<DirUpdaterResult, DirUpdaterResult> reportCol;
    /** The OK button. */
    @FXML
    private Button ok;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ok.setOnAction(_ -> ok.getScene().getWindow().hide());
        resultList.setSelectionModel(null);

        setupStringColumn(datCol, result -> result.getDat().toString());
        setupIntegerColumn(haveCol, Color.GREEN, Report.Stats::getSetFoundOk);
        setupIntegerColumn(createCol, Color.BLUE, Report.Stats::getSetCreateComplete);
        setupIntegerColumn(fixCol, Color.DARKVIOLET, Report.Stats::getSetFoundFixComplete);
        setupIntegerColumn(missCol, Color.RED, this::getSetMissed);
        setupIntegerColumn(totalCol, null, this::getSetTotal);

        reportCol.setCellFactory(_ -> createReportCellFactory());
    }

    private int getSetTotal(Stats stats) {
        return stats.getSetCreate() + stats.getSetFound() + stats.getSetMissing();
    }

    private int getSetMissed(Stats stats) {
        return getSetTotal(stats) - (stats.getSetCreateComplete() + stats.getSetFoundFixComplete() + stats.getSetFoundOk());
    }

    private ButtonCellFactory<DirUpdaterResult, DirUpdaterResult> createReportCellFactory() {
        return new ButtonCellFactory<>("Report", this::showReport);
    }

    private void showReport(TableCell<DirUpdaterResult, DirUpdaterResult> cell) {
        final var result = resultList.getItems().get(cell.getIndex());
        try {
            new ReportLite((Stage) resultList.getScene().getWindow(), Report.load(session, result.getDat()));
        } catch (IOException | URISyntaxException e1) /* NOSONAR */ {
            Log.err("Failed to load report for " + result.getDat(), e1);
        }
    }

    /**
     * Configures a table column to display integer values with custom color and right alignment.
     *
     * @param col the table column to configure
     * @param color the text color, or {@code null} for default color
     * @param extractor function to extract the integer value from the stats object
     */
    private void setupIntegerColumn(TableColumn<DirUpdaterResult, Integer> col, Color color, ToIntFunction<Report.Stats> extractor) {
        col.setCellFactory(_ -> new ColoredIntegerCellFactory<>(color, Pos.CENTER_RIGHT));
        col.setCellValueFactory(param -> observableValueFromStats(extractor, param));
    }

    private ObservableValueBase<Integer> observableValueFromStats(ToIntFunction<Report.Stats> extractor, CellDataFeatures<DirUpdaterResult, Integer> param) {
        return new ObservableValueBase<>() {
            @Override
            public Integer getValue() {
                return extractor.applyAsInt(param.getValue().getStats());
            }
        };
    }

    /**
     * Configures a table column to display string values with leading ellipsis and tooltip.
     *
     * @param col the table column to configure
     * @param extractor function to extract the string value from the result object
     */
    private void setupStringColumn(TableColumn<DirUpdaterResult, String> col, Function<DirUpdaterResult, String> extractor) {
        col.setCellFactory(_ -> new EllipsisStringCellFactory<>(OverrunStyle.LEADING_ELLIPSIS));
        col.setCellValueFactory(param -> createObservableValue(extractor, param));
    }

    private ObservableValueBase<String> createObservableValue(Function<DirUpdaterResult, String> extractor, CellDataFeatures<DirUpdaterResult, String> param) {
        return new ObservableValueBase<>() {
            @Override
            public String getValue() {
                return extractor.apply(param.getValue());
            }
        };
    }

}
