package jrm.fx.ui.progress;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import jrm.fx.ui.MainFrame;
import jrm.fx.ui.progress.ProgressTask.PData;
import jrm.fx.ui.status.NeutralToNodeFormatter;
import jrm.locale.Messages;
import lombok.Setter;

public class ProgressController implements Initializable
{
	@FXML private VBox panel;
	@FXML private ProgressBar progressBar;
	@FXML private Label progressBarLbl;
	@FXML private Label lblTimeleft;
	@FXML private ProgressBar progressBar2;
	@FXML private Label progressBarLbl2;
	@FXML private Label lblTimeleft2;
	@FXML private ProgressBar progressBar3;
	@FXML private Label progressBarLbl3;
	@FXML private Label lblTimeleft3;
	@FXML private Button cancelBtn;
	
	private @Setter ProgressTask<?> task;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		cancelBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/stop.png")));
		setInfos(1, false);
		progressBar2.setVisible(false);
		progressBarLbl2.setVisible(false);
		lblTimeleft2.setVisible(false);
		((GridPane)lblTimeleft2.getParent()).getRowConstraints().get(2).setPrefHeight(0);
		progressBar3.setVisible(false);
		progressBarLbl3.setVisible(false);
		lblTimeleft3.setVisible(false);
		((GridPane)lblTimeleft3.getParent()).getRowConstraints().get(3).setPrefHeight(0);
	}

	private static final String HH_MM_SS_OF_HH_MM_SS_NONE = "--:--:-- / --:--:--";

	/** The lbl info. */
	private Pane[] lblInfo = new Pane[0];

	/** The lbl sub info. */
	private Pane[] lblSubInfo = new Pane[0];

	private static final Color colorNormal = new Color(0.7, 0.7, 0.7, 1.0);
	private static final Color colorLight = new Color(0.8, 0.8, 0.8, 1.0);
	private static final Color colorLighter = new Color(0.9, 0.9, 0.9, 1.0);

	void setInfos(int threadCnt, Boolean multipleSubInfos)
	{
		final var lblSubInfoCnt = Optional.ofNullable(multipleSubInfos).map(multSubNfo -> multSubNfo.booleanValue() ? threadCnt : 1).orElse(0);

		if (lblInfo != null && lblInfo.length == threadCnt && lblSubInfo != null && lblSubInfo.length == lblSubInfoCnt)
			return;

		panel.getChildren().forEach(n -> {
			if (n instanceof HBox w)
			{
				w.getChildren().clear();
				viewCache.add(w);
			}
		});
		panel.getChildren().clear();

		lblInfo = new Pane[threadCnt];
		lblSubInfo = new Pane[lblSubInfoCnt];

		for (int i = 0; i < threadCnt; i++)
		{
			lblInfo[i] = buildView(isOdd(i) ? colorNormal : colorLight);
			panel.getChildren().add(lblInfo[i]);

			if (Boolean.TRUE.equals(multipleSubInfos))
			{
				lblSubInfo[i] = buildView(isOdd(i) ? colorNormal : colorLight);
				panel.getChildren().add(lblSubInfo[i]);
			}
		}
		if (Boolean.FALSE.equals(multipleSubInfos))
		{
			lblSubInfo[0] = buildView(colorLighter);
			panel.getChildren().add(lblSubInfo[0]);
		}
	}
	
	void extendInfos(int threadCnt, Boolean multipleSubInfos)
	{
		if(lblInfo == null || lblInfo.length == threadCnt)
			return;

		if(Boolean.TRUE.equals(multipleSubInfos) && lblSubInfo == null)
			return;

		final var oldThreadCnt = lblInfo.length;

		lblInfo = Arrays.copyOf(lblInfo, threadCnt);
		if (Boolean.TRUE.equals(multipleSubInfos))
			lblSubInfo = Arrays.copyOf(lblSubInfo, threadCnt);

		for (int i = oldThreadCnt; i < threadCnt; i++)
		{
			lblInfo[i] = buildView(isOdd(i) ? colorNormal : colorLight);
			panel.getChildren().add(lblInfo[i]);

			if (Boolean.TRUE.equals(multipleSubInfos))
			{
				lblSubInfo[i] = buildView(isOdd(i) ? colorNormal : colorLight);
				panel.getChildren().add(lblSubInfo[i]);
			}
		}
	}
	
	private boolean isOdd(int i)
	{
		return (i % 2) != 0;
	}

	private Deque<HBox> viewCache = new ArrayDeque<>(); 
	
	private HBox buildView(Color color)
	{
		final HBox view;
		if(!viewCache.isEmpty())
			view = viewCache.poll();
		else
		{
			view = new HBox();
			view.setPrefHeight(20);
			view.setMaxWidth(Integer.MAX_VALUE);
			view.setAlignment(Pos.CENTER_LEFT);
		}
		view.setBackground(new Background(new BackgroundFill(color, null, null)));
		view.setBorder(new Border(new BorderStroke(color.darker(), color.brighter(), color.brighter(), color.darker(), BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, BorderStrokeStyle.SOLID, null, null, null)));
		return view;
	}

	void clearInfos()
	{
		for (final var label : lblInfo)
			label.getChildren().clear();
		for (final var label : lblSubInfo)
			label.getChildren().clear();
	}


	public void setFullProgress(PData pd)
	{
		for (int i = 0; i < lblInfo.length; i++)
			lblInfo[i].getChildren().setAll(NeutralToNodeFormatter.toNodes(i < pd.getInfos().length ? pd.getInfos()[i]:""));
		for (int i = 0; i < lblSubInfo.length; i++)
			lblSubInfo[i].getChildren().setAll(NeutralToNodeFormatter.toNodes(i < pd.getSubinfos().length ? pd.getSubinfos()[i] : ""));
		if (progressBar.isVisible() != pd.getPb1().isVisibility())
		{
			progressBar.setVisible(pd.getPb1().isVisibility());
			progressBarLbl.setVisible(pd.getPb1().isVisibility());
			lblTimeleft.setVisible(pd.getPb1().isVisibility());
			((GridPane)lblTimeleft.getParent()).getRowConstraints().get(1).setPrefHeight(pd.getPb1().isVisibility()?Region.USE_COMPUTED_SIZE:0);
		}
		if (pd.getPb1().isVisibility())
		{
			if(pd.getPb1().isIndeterminate())
			{
				progressBar.setProgress(-1);
				progressBarLbl.setVisible(false);
			}
			else if (pd.getPb1().getVal() > 0)
			{
				if ((int)(progressBar.getProgress()*100) != (int) pd.getPb1().getPerc())
					progressBar.setProgress(pd.getPb1().getPerc()/100);
				if (pd.getPb1().isStringPainted())
				{
					progressBarLbl.setVisible(true);
					progressBarLbl.setText(Optional.ofNullable(pd.getPb1().getMsg()).orElse(""));
				}
				else
					progressBarLbl.setVisible(false);
				lblTimeleft.setText(pd.getPb1().getTimeleft());
			}
			else
				lblTimeleft.setText(HH_MM_SS_OF_HH_MM_SS_NONE);
		}
		if (progressBar2.isVisible() != pd.getPb2().isVisibility())
		{
			progressBar2.setVisible(pd.getPb2().isVisibility());
			progressBarLbl2.setVisible(pd.getPb2().isVisibility());
			lblTimeleft2.setVisible(pd.getPb2().isVisibility());
			((GridPane)lblTimeleft2.getParent()).getRowConstraints().get(2).setPrefHeight(pd.getPb2().isVisibility()?Region.USE_COMPUTED_SIZE:0);
		}
		if (pd.getPb2().isVisibility())
		{
			if(pd.getPb2().isIndeterminate())
			{
				progressBar2.setProgress(-1);
				progressBarLbl2.setVisible(false);
			}
			else if (pd.getPb2().getPerc() >= 0)
			{
				if ((int)(progressBar2.getProgress()*100) != (int) pd.getPb2().getPerc())
					progressBar2.setProgress(pd.getPb2().getPerc()/100);
				if (pd.getPb2().isStringPainted())
				{
					progressBarLbl2.setVisible(true);
					progressBarLbl2.setText(Optional.ofNullable(pd.getPb2().getMsg()).orElse(""));
				}
				else
					progressBarLbl2.setVisible(false);
				lblTimeleft2.setText(pd.getPb2().getTimeleft());
			}
			else
				lblTimeleft2.setText(HH_MM_SS_OF_HH_MM_SS_NONE);
		}
		if (progressBar3.isVisible() != pd.getPb3().isVisibility())
		{
			progressBar3.setVisible(pd.getPb3().isVisibility());
			progressBarLbl3.setVisible(pd.getPb3().isVisibility());
			lblTimeleft3.setVisible(pd.getPb3().isVisibility());
			((GridPane)lblTimeleft3.getParent()).getRowConstraints().get(3).setPrefHeight(pd.getPb3().isVisibility()?Region.USE_COMPUTED_SIZE:0);
		}
		if (pd.getPb3().isVisibility())
		{
			if(pd.getPb3().isIndeterminate())
			{
				progressBar3.setProgress(-1);
				progressBarLbl3.setVisible(false);
			}
			else if (pd.getPb3().getPerc() >= 0)
			{
				if ((int)(progressBar3.getProgress()*100) != (int) pd.getPb3().getPerc())
					progressBar3.setProgress(pd.getPb3().getPerc()/100);
				if (pd.getPb3().isStringPainted())
				{
					progressBarLbl3.setVisible(true);
					progressBarLbl3.setText(Optional.ofNullable(pd.getPb3().getMsg()).orElse(""));
				}
				else
					progressBarLbl3.setVisible(false);
				lblTimeleft3.setText(pd.getPb3().getTimeleft());
			}
			else
				lblTimeleft3.setText(HH_MM_SS_OF_HH_MM_SS_NONE);
		}
	}

	void close()
	{
		panel.getScene().getWindow().hide();
	}
	
	void canCancel(boolean canCancel)
	{
		cancelBtn.setDisable(!canCancel);
	}
	
	@FXML void doCancel()
	{
		task.doCancel();
		cancelBtn.setDisable(true);
		cancelBtn.setText(Messages.getString("Progress.Canceling")); //$NON-NLS-1$
	}
}
