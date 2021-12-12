package jrm.fx.ui.progress;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.commons.lang3.time.DurationFormatUtils;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import jrm.fx.ui.MainFrame;

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

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		cancelBtn.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/stop.png")));
	}

	private static final String S_OF_S = "%s / %s";

	private static final String HH_MM_SS_FMT = "HH:mm:ss";

	private static final String HH_MM_SS_OF_HH_MM_SS_NONE = "--:--:-- / --:--:--";

	private static final String HH_MM_SS_NONE = "--:--:--";

	/** The lbl info. */
	private Label[] lblInfo;

	/** The lbl sub info. */
	private Label[] lblSubInfo;

	void setInfos(int threadCnt, Boolean multipleSubInfos)
	{
		final var lblSubInfoCnt = Optional.ofNullable(multipleSubInfos).map(multSubNfo -> multSubNfo.booleanValue() ? threadCnt : 1).orElse(0);

		if (lblInfo != null && lblInfo.length == threadCnt && lblSubInfo != null && lblSubInfo.length == lblSubInfoCnt)
			return;

		panel.getChildren().clear();

		lblInfo = new Label[threadCnt];
		lblSubInfo = new Label[lblSubInfoCnt];

		final Color normal = Color.GRAY;
		final Color light = Color.DARKGRAY;
		final Color lighter = Color.LIGHTGRAY;

		for (int i = 0; i < threadCnt; i++)
		{
			lblInfo[i] = buildLabel((i % 2) != 0 ? normal : light);
			panel.getChildren().add(lblInfo[i]);

			if (Boolean.TRUE.equals(multipleSubInfos))
			{
				lblSubInfo[i] = buildLabel((i % 2) != 0 ? normal : light);
				panel.getChildren().add(lblSubInfo[i]);
			}
		}
		if (Boolean.FALSE.equals(multipleSubInfos))
		{
			lblSubInfo[0] = buildLabel(lighter);
			panel.getChildren().add(lblSubInfo[0]);
		}

		// packHeight();
	}

	private Label buildLabel(Color color)
	{
		final var label = new Label();
		label.setBackground(new Background(new BackgroundFill(color, null, null)));
		return label;
	}

	public void clearInfos()
	{
		for (final var label : lblInfo)
			label.setText(null);
		for (final var label : lblSubInfo)
			label.setText(null);
	}

	/** The start time. */
	private long startTime = System.currentTimeMillis();
	private static final Integer MOINS_UN = Integer.valueOf(-1);
	private Integer max = null;
	private Integer val = null;

	public synchronized void setProgress(final int offset, final String msg, final Integer val, final Integer max, final String submsg)
	{
		if (msg != null)
			lblInfo[offset].setText(msg);
		if (val != null)
		{
			if (val < 0 && progressBar.isVisible())
			{
				progressBar.setVisible(false);
				progressBarLbl.setVisible(false);
				lblTimeleft.setVisible(false);
				// packHeight();
			}
			else if (val >= 0 && !progressBar.isVisible())
			{
				progressBar.setVisible(true);
				progressBarLbl.setVisible(val != 0);
				lblTimeleft.setVisible(true);
				// packHeight();
			}
			if (max != null)
				this.max = max;
			if (val >= 0)
			{
				this.val = val;
				if (this.max != null && this.max > 0)
				{
					progressBar.setProgress(val == 0 ? -1 : (this.val.doubleValue() / this.max.doubleValue()));
					if (val > 0)
						progressBarLbl.setText(String.format("%.02f%%", (this.val.doubleValue() / this.max.doubleValue())*100));
				}
			}
			if (val == 0)
				startTime = System.currentTimeMillis();
			showTimeLeft(startTime, val, progressBar, lblTimeleft);
		}
		subMsg(offset, val, submsg);
	}

	/**
	 * @param offset
	 * @param val
	 * @param submsg
	 */
	protected void subMsg(final int offset, final Integer val, final String submsg)
	{
		if (submsg != null || MOINS_UN.equals(val))
		{
			if (lblSubInfo.length == 1)
				lblSubInfo[0].setText(submsg);
			else if (lblSubInfo.length > 1)
				lblSubInfo[offset].setText(submsg);
		}
	}

	private void showTimeLeft(long start, int val, ProgressBar pb, Label lab)
	{
		if (val > 0)
		{
			final String left = DurationFormatUtils.formatDuration((System.currentTimeMillis() - start) * (this.max - val) / val, HH_MM_SS_FMT); // $NON-NLS-1$
			final String total = DurationFormatUtils.formatDuration((System.currentTimeMillis() - start) * this.max / val, HH_MM_SS_FMT); // $NON-NLS-1$
			lab.setText(String.format(S_OF_S, left, total)); // $NON-NLS-1$
		}
		else
			lab.setText(HH_MM_SS_OF_HH_MM_SS_NONE); // $NON-NLS-1$
	}

}
