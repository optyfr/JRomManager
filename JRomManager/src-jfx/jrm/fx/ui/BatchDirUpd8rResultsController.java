package jrm.fx.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ObservableValueBase;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import jrm.batch.DirUpdaterResults.DirUpdaterResult;
import jrm.fx.ui.controls.ButtonCellFactory;
import jrm.fx.ui.profile.report.ReportLite;
import jrm.profile.report.Report;
import lombok.Getter;

public class BatchDirUpd8rResultsController extends BaseController
{
	@FXML private @Getter TableView<DirUpdaterResult> resultList;
	@FXML private TableColumn<DirUpdaterResult, String> datCol;
	@FXML private TableColumn<DirUpdaterResult, Integer> haveCol;
	@FXML private TableColumn<DirUpdaterResult, Integer> createCol;
	@FXML private TableColumn<DirUpdaterResult, Integer> fixCol;
	@FXML private TableColumn<DirUpdaterResult, Integer> missCol;
	@FXML private TableColumn<DirUpdaterResult, Integer> totalCol;
	@FXML private TableColumn<DirUpdaterResult, DirUpdaterResult> reportCol;
	@FXML private Button ok;

	private Font font = new Font(10);
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		ok.setOnAction(e -> ok.getScene().getWindow().hide());
		resultList.setFixedCellSize(18);
		resultList.setSelectionModel(null);
		datCol.setCellFactory(param -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty)
					setText("");
				else
				{
					setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
					setText(item);
					setTooltip(new Tooltip(item));
				}
				setMinHeight(15);
				setFont(font);
				setGraphic(null);
			}
		});
		datCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public String getValue()
			{
				return param.getValue().getDat().toString();
			}
		});
		haveCol.setCellFactory(param -> new TableCell<>() {
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty)
					setText("");
				else
				{
					setTextFill(Color.GREEN);
					setAlignment(Pos.CENTER_RIGHT);
					setText(item.toString());
					setTooltip(new Tooltip(item.toString()));
				}
				setMinHeight(15);
				setFont(font);
				setGraphic(null);
			}
		});
		haveCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public Integer getValue()
			{
				return param.getValue().getStats().getSetFoundOk();
			}
		});
		createCol.setCellFactory(param -> new TableCell<>() {
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty)
					setText("");
				else
				{
					setTextFill(Color.BLUE);
					setAlignment(Pos.CENTER_RIGHT);
					setText(item.toString());
					setTooltip(new Tooltip(item.toString()));
				}
				setMinHeight(15);
				setFont(font);
				setGraphic(null);
			}
		});
		createCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public Integer getValue()
			{
				return param.getValue().getStats().getSetCreateComplete();
			}
		});
		fixCol.setCellFactory(param -> new TableCell<>() {
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty)
					setText("");
				else
				{
					setTextFill(Color.DARKVIOLET);
					setAlignment(Pos.CENTER_RIGHT);
					setText(item.toString());
					setTooltip(new Tooltip(item.toString()));
				}
				setMinHeight(15);
				setFont(font);
				setGraphic(null);
			}
		});
		fixCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public Integer getValue()
			{
				return param.getValue().getStats().getSetFoundFixComplete();
			}
		});
		missCol.setCellFactory(param -> new TableCell<>() {
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty)
					setText("");
				else
				{
					setTextFill(Color.RED);
					setAlignment(Pos.CENTER_RIGHT);
					setText(item.toString());
					setTooltip(new Tooltip(item.toString()));
				}
				setMinHeight(15);
				setFont(font);
				setGraphic(null);
			}
		});
		missCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public Integer getValue()
			{
				return param.getValue().getStats().getSetCreate() + param.getValue().getStats().getSetFound() + param.getValue().getStats().getSetMissing() - (param.getValue().getStats().getSetCreateComplete() + param.getValue().getStats().getSetFoundFixComplete() + param.getValue().getStats().getSetFoundOk());
			}
		});
		totalCol.setCellFactory(param -> new TableCell<>() {
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty)
					setText("");
				else
				{
					setAlignment(Pos.CENTER_RIGHT);
					setText(item.toString());
					setTooltip(new Tooltip(item.toString()));
				}
				setMinHeight(15);
				setFont(font);
				setGraphic(null);
			}
		});
		totalCol.setCellValueFactory(param -> new ObservableValueBase<>()
		{
			@Override
			public Integer getValue()
			{
				return param.getValue().getStats().getSetCreate() + param.getValue().getStats().getSetFound() + param.getValue().getStats().getSetMissing();
			}
		});
		reportCol.setCellFactory(param -> new ButtonCellFactory<>("Report", cell -> {
			final var result = resultList.getItems().get(cell.getIndex());
			try
			{
				new ReportLite((Stage)resultList.getScene().getWindow(), Report.load(session, result.getDat()));
			}
			catch (IOException | URISyntaxException e1)
			{
				e1.printStackTrace();
			}
		}));
	}

}
