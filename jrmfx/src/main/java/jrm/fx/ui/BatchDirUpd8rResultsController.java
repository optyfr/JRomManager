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
import javafx.scene.control.TableColumn;
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
import lombok.Getter;

public class BatchDirUpd8rResultsController extends BaseController {
    @FXML
    private @Getter TableView<DirUpdaterResult> resultList;
    @FXML
    private TableColumn<DirUpdaterResult, String> datCol;
    @FXML
    private TableColumn<DirUpdaterResult, Integer> haveCol;
    @FXML
    private TableColumn<DirUpdaterResult, Integer> createCol;
    @FXML
    private TableColumn<DirUpdaterResult, Integer> fixCol;
    @FXML
    private TableColumn<DirUpdaterResult, Integer> missCol;
    @FXML
    private TableColumn<DirUpdaterResult, Integer> totalCol;
    @FXML
    private TableColumn<DirUpdaterResult, DirUpdaterResult> reportCol;
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
        setupIntegerColumn(missCol, Color.RED, stats -> stats.getSetCreate() + stats.getSetFound() + stats.getSetMissing()
                - (stats.getSetCreateComplete() + stats.getSetFoundFixComplete() + stats.getSetFoundOk()));
        setupIntegerColumn(totalCol, null, stats -> stats.getSetCreate() + stats.getSetFound() + stats.getSetMissing());

        reportCol.setCellFactory(_ -> new ButtonCellFactory<>("Report", cell -> {
            final var result = resultList.getItems().get(cell.getIndex());
            try {
                new ReportLite((Stage) resultList.getScene().getWindow(), Report.load(session, result.getDat()));
            } catch (IOException | URISyntaxException e1) {
                Log.err("Failed to load report for " + result.getDat(), e1);
            }
        }));
    }

    /**
     * Configures a table column to display integer values with custom color and right alignment.
     *
     * @param col the table column to configure
     * @param color the text color, or null for default color
     * @param extractor function to extract the integer value from the stats object
     */
    private void setupIntegerColumn(TableColumn<DirUpdaterResult, Integer> col, Color color, ToIntFunction<Report.Stats> extractor) {
        col.setCellFactory(_ -> new ColoredIntegerCellFactory<>(color, Pos.CENTER_RIGHT));
        col.setCellValueFactory(param -> new ObservableValueBase<>() {
            @Override
            public Integer getValue() {
                return extractor.applyAsInt(param.getValue().getStats());
            }
        });
    }

    /**
     * Configures a table column to display string values with leading ellipsis and tooltip.
     *
     * @param col the table column to configure
     * @param extractor function to extract the string value from the result object
     */
    private void setupStringColumn(TableColumn<DirUpdaterResult, String> col, Function<DirUpdaterResult, String> extractor) {
        col.setCellFactory(_ -> new EllipsisStringCellFactory<>(OverrunStyle.LEADING_ELLIPSIS));
        col.setCellValueFactory(param -> new ObservableValueBase<>() {
            @Override
            public String getValue() {
                return extractor.apply(param.getValue());
            }
        });
    }

}
