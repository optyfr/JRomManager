/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.ui.progress;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

import org.apache.commons.lang3.time.DurationFormatUtils;

import jrm.locale.Messages;
import jrm.ui.MainFrame;

/**
 * The Class Progress.
 *
 * @author optyfr
 */
@SuppressWarnings("serial")
public class Progress extends JDialog
{
	
	private static final String S_OF_S = "%s / %s";

	private static final String HH_MM_SS_FMT = "HH:mm:ss";

	private static final String HH_MM_SS_OF_HH_MM_SS_NONE = "--:--:-- / --:--:--";

	private static final String HH_MM_SS_NONE = "--:--:--";

	/** The panel. */
	private JPanel panel;
	
	/** The lbl info. */
	private JLabel[] lblInfo;
	
	/** The lbl sub info. */
	private JLabel[] lblSubInfo;
	
	/** The progress bar. */
	private final JProgressBar progressBar;
	
	/** The progress bar 2. */
	private final JProgressBar progressBar2;
	
	/** The progress bar 3. */
	private final JProgressBar progressBar3;
	
	/** The lbl timeleft. */
	private final JLabel lblTimeleft;
	
	/** The lbl time left 2. */
	private final JLabel lblTimeLeft2;

	/** The lbl time left 3. */
	private final JLabel lblTimeLeft3;

	/** The btn cancel. */
	private final JButton btnCancel;

	/** The cancel. */
	private boolean cancel = false;
	
	/** Can we cancel. */
	private boolean canCancel = false;
	

	/**
	 * Instantiates a new progress.
	 *
	 * @param owner the owner
	 */
	public Progress(@SuppressWarnings("exports") final Window owner)
	{
		super(owner, Messages.getString("Progress.Title"), ModalityType.MODELESS); //$NON-NLS-1$
		setIconImage(MainFrame.getIcon("/jrm/resicons/rom.png").getImage()); //$NON-NLS-1$
		getContentPane().setBackground(UIManager.getColor("Panel.background")); //$NON-NLS-1$
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				cancel = true;
			}

			@Override
			public void windowOpened(final WindowEvent e)
			{
				owner.setEnabled(false);
				if (owner.getOwner() != null)
					owner.getOwner().setEnabled(false);
			}

			@Override
			public void windowClosed(final WindowEvent e)
			{
				owner.setEnabled(true);
				owner.toFront();
				if (owner.getOwner() != null)
					owner.getOwner().setEnabled(true);
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 30, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		panel = new JPanel();
		GridBagConstraints gbcPanel = new GridBagConstraints();
		gbcPanel.insets = new Insets(5, 5, 5, 5);
		gbcPanel.gridwidth = 2;
		gbcPanel.fill = GridBagConstraints.BOTH;
		gbcPanel.gridx = 0;
		gbcPanel.gridy = 0;
		getContentPane().add(panel, gbcPanel);
		panel.setLayout(new GridLayout(0, 1, 5, 5));

		setInfos(1,false);

		progressBar = new JProgressBar();
		progressBar.setMaximumSize(new Dimension(32767, 20));
		progressBar.setMinimumSize(new Dimension(300, 20));
		progressBar.setPreferredSize(new Dimension(450, 20));
		final GridBagConstraints gbcProgressBar = new GridBagConstraints();
		gbcProgressBar.fill = GridBagConstraints.HORIZONTAL;
		gbcProgressBar.anchor = GridBagConstraints.SOUTH;
		gbcProgressBar.insets = new Insets(0, 5, 5, 5);
		gbcProgressBar.gridx = 0;
		gbcProgressBar.gridy = 1;
		getContentPane().add(progressBar, gbcProgressBar);

		lblTimeleft = new JLabel(HH_MM_SS_NONE); //$NON-NLS-1$
		final GridBagConstraints gbcLblTimeleft = new GridBagConstraints();
		gbcLblTimeleft.fill = GridBagConstraints.NONE;
		gbcLblTimeleft.anchor = GridBagConstraints.SOUTH;
		gbcLblTimeleft.insets = new Insets(0, 0, 5, 5);
		gbcLblTimeleft.gridx = 1;
		gbcLblTimeleft.gridy = 1;
		getContentPane().add(lblTimeleft, gbcLblTimeleft);

		progressBar2 = new JProgressBar();
		progressBar2.setMaximumSize(new Dimension(32767, 20));
		progressBar2.setVisible(false);
		progressBar2.setPreferredSize(new Dimension(450, 20));
		progressBar2.setMinimumSize(new Dimension(300, 20));
		final GridBagConstraints gbcProgressBar2 = new GridBagConstraints();
		gbcProgressBar2.fill = GridBagConstraints.HORIZONTAL;
		gbcProgressBar2.insets = new Insets(0, 5, 5, 5);
		gbcProgressBar2.gridx = 0;
		gbcProgressBar2.gridy = 2;
		getContentPane().add(progressBar2, gbcProgressBar2);

		lblTimeLeft2 = new JLabel(HH_MM_SS_NONE); //$NON-NLS-1$
		lblTimeLeft2.setVisible(false);
		final GridBagConstraints gbcLblTimeLeft2 = new GridBagConstraints();
		gbcLblTimeLeft2.fill = GridBagConstraints.NONE;
		gbcLblTimeLeft2.insets = new Insets(0, 0, 5, 5);
		gbcLblTimeLeft2.gridx = 1;
		gbcLblTimeLeft2.gridy = 2;
		getContentPane().add(lblTimeLeft2, gbcLblTimeLeft2);

		progressBar3 = new JProgressBar();
		progressBar3.setMaximumSize(new Dimension(32767, 20));
		progressBar3.setVisible(false);
		progressBar3.setPreferredSize(new Dimension(450, 20));
		progressBar3.setMinimumSize(new Dimension(300, 20));
		final GridBagConstraints gbcProgressBar3 = new GridBagConstraints();
		gbcProgressBar3.fill = GridBagConstraints.HORIZONTAL;
		gbcProgressBar3.insets = new Insets(0, 5, 0, 5);
		gbcProgressBar3.gridx = 0;
		gbcProgressBar3.gridy = 3;
		getContentPane().add(progressBar3, gbcProgressBar3);

		lblTimeLeft3 = new JLabel(HH_MM_SS_NONE); //$NON-NLS-1$
		lblTimeLeft3.setVisible(false);
		final GridBagConstraints gbcLblTimeLeft3 = new GridBagConstraints();
		gbcLblTimeLeft3.fill = GridBagConstraints.NONE;
		gbcLblTimeLeft3.insets = new Insets(0, 0, 0, 5);
		gbcLblTimeLeft3.gridx = 1;
		gbcLblTimeLeft3.gridy = 3;
		getContentPane().add(lblTimeLeft3, gbcLblTimeLeft3);

		btnCancel = new JButton(Messages.getString("Progress.btnCancel.text")); //$NON-NLS-1$
		btnCancel.setIcon(MainFrame.getIcon("/jrm/resicons/icons/stop.png")); //$NON-NLS-1$
		btnCancel.addActionListener(e -> cancel());
		final GridBagConstraints gbcBtnCancel = new GridBagConstraints();
		gbcBtnCancel.insets = new Insets(5, 5, 5, 5);
		gbcBtnCancel.gridwidth = 2;
		gbcBtnCancel.anchor = GridBagConstraints.SOUTH;
		gbcBtnCancel.gridx = 0;
		gbcBtnCancel.gridy = 4;
		getContentPane().add(btnCancel, gbcBtnCancel);

		pack();
		setLocationRelativeTo(owner);
	}

	public void setInfos(int threadCnt, Boolean multipleSubInfos)
	{
		final var lblSubInfoCnt = Optional.ofNullable(multipleSubInfos).map(multSubNfo -> multSubNfo.booleanValue() ? threadCnt : 1).orElse(0);
		
		if (lblInfo != null && lblInfo.length == threadCnt && lblSubInfo != null && lblSubInfo.length == lblSubInfoCnt)
			return;
		
		panel.removeAll();
		
		lblInfo = new JLabel[threadCnt];
		lblSubInfo = new JLabel[lblSubInfoCnt];
		
		final Color normal = SystemColor.control;
		final Color light = SystemColor.controlLtHighlight;
		final Color lighter = SystemColor.controlHighlight;
		
		for(int i = 0; i < threadCnt; i++)
		{
			lblInfo[i] = buildLabel((i%2)!=0?normal:light);
			panel.add(lblInfo[i]);
	
			if(Boolean.TRUE.equals(multipleSubInfos))
			{
				lblSubInfo[i] = buildLabel((i%2)!=0?normal:light);
				panel.add(lblSubInfo[i]);
			}
		}
		if(Boolean.FALSE.equals(multipleSubInfos))
		{
			lblSubInfo[0] = buildLabel(lighter);
			panel.add(lblSubInfo[0]);
		}
		
		packHeight();
	}
	
	@SuppressWarnings("exports")
	public JLabel buildLabel(Color color)
	{
		final var label = new JLabel();
		label.setOpaque(true);
		label.setBackground(color);
		label.setPreferredSize(new Dimension(0, 20));
		label.setMinimumSize(new Dimension(0, 20));
		label.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		return label;
	}
	
	public void clearInfos()
	{
		for(JLabel label : lblInfo)
			label.setText(null);
		for(JLabel label : lblSubInfo)
			label.setText(null);
	}
	
	/** The start time. */
	private long startTime = System.currentTimeMillis();
	private static final Integer MOINS_UN = Integer.valueOf(-1);
	
	public synchronized void setProgress(final int offset, final String msg, final Integer val, final Integer max, final String submsg)
	{
		
		if (msg != null)
			lblInfo[offset].setText(msg);
		if (val != null)
		{
			if (val < 0 && progressBar.isVisible())
			{
				progressBar.setVisible(false);
				lblTimeleft.setVisible(false);
				packHeight();
			}
			else if (val >= 0 && !progressBar.isVisible())
			{
				progressBar.setVisible(true);
				lblTimeleft.setVisible(true);
				packHeight();
			}
			progressBar.setStringPainted(val!=0);
			progressBar.setIndeterminate(val==0);
			if (max != null)
				progressBar.setMaximum(max);
			if (val >= 0)
				progressBar.setValue(val);
			if (val == 0)
				startTime = System.currentTimeMillis();
			showTimeLeft(startTime, val, progressBar, lblTimeleft);
		}
		if (submsg != null || MOINS_UN.equals(val))
		{
			if (lblSubInfo.length == 1)
				lblSubInfo[0].setText(submsg);
			else if (lblSubInfo.length > 1)
				lblSubInfo[offset].setText(submsg);
		}
	}
	
	private void showTimeLeft(long start, int val, JProgressBar pb, JLabel lab)
	{
		if (val > 0)
		{
			final String left = DurationFormatUtils.formatDuration((System.currentTimeMillis() - start) * (pb.getMaximum() - val) / val, HH_MM_SS_FMT); //$NON-NLS-1$
			final String total = DurationFormatUtils.formatDuration((System.currentTimeMillis() - start) * pb.getMaximum() / val, HH_MM_SS_FMT); //$NON-NLS-1$
			lab.setText(String.format(S_OF_S, left, total)); //$NON-NLS-1$
		}
		else
			lab.setText(HH_MM_SS_OF_HH_MM_SS_NONE); //$NON-NLS-1$
	}

	/** The start time 2. */
	private long startTime2 = System.currentTimeMillis();

	public void setProgress2(final String msg, final Integer val, final Integer max)
	{
		if (msg != null && val != null)
		{
			if (!progressBar2.isVisible())
			{
				progressBar2.setVisible(true);
				lblTimeLeft2.setVisible(true);
				packHeight();
			}
			progressBar2.setStringPainted(val > 0);
			progressBar2.setString(msg);
			progressBar2.setIndeterminate(val==0);
			if (max != null)
				progressBar2.setMaximum(max);
			if (val >= 0)
				progressBar2.setValue(val);
			if (val == 0)
				startTime2 = System.currentTimeMillis();
			showTimeLeft(startTime2, val, progressBar2, lblTimeLeft2);
		}
		else if (progressBar2.isVisible())
		{
			progressBar2.setVisible(false);
			lblTimeLeft2.setVisible(false);
			packHeight();
		}
	}


	/** The start time 2. */
	private long startTime3 = System.currentTimeMillis();

	public void setProgress3(final String msg, final Integer val, final Integer max)
	{
		if (msg != null && val != null)
		{
			if (!progressBar3.isVisible())
			{
				progressBar3.setVisible(true);
				lblTimeLeft3.setVisible(true);
				packHeight();
			}
			progressBar3.setStringPainted(val > 0);
			progressBar3.setString(msg);
			progressBar3.setIndeterminate(val==0);
			if (max != null)
				progressBar3.setMaximum(max);
			if (val >= 0)
				progressBar3.setValue(val);
			if (val == 0)
				startTime3 = System.currentTimeMillis();
			showTimeLeft(startTime3, val, progressBar3, lblTimeLeft3);
		}
		else if (progressBar3.isVisible())
		{
			progressBar3.setVisible(false);
			lblTimeLeft3.setVisible(false);
			packHeight();
		}
	}

	/**
	 * Pack height.
	 */
	private void packHeight()
	{
		invalidate();
		final Dimension newSize = getPreferredSize();
		final Rectangle rect = getBounds();
		rect.height = Math.max(rect.height, newSize.height);
		setBounds(rect);
		validate();
	}

	public boolean isCancel()
	{
		return cancel;
	}

	public void cancel()
	{
		cancel = true;
		btnCancel.setEnabled(false);
		btnCancel.setText(Messages.getString("Progress.Canceling")); //$NON-NLS-1$
	}

	public int getValue()
	{
		return progressBar.getValue();
	}

	public int getValue2()
	{
		return progressBar2.getValue();
	}

	public int getValue3()
	{
		return progressBar3.getValue();
	}

	public void close()
	{
		dispose();
	}

	public void canCancel(boolean canCancel)
	{
		this.canCancel = canCancel;
		btnCancel.setEnabled(canCancel);
	}

	public boolean canCancel()
	{
		return canCancel;
	}
}
