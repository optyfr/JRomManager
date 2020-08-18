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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
	
	/** The panel. */
	private JPanel panel;
	
	/** The lbl info. */
	private JLabel[] lblInfo;
	
	/** The lbl sub info. */
	private JLabel[] lblSubInfo;
	
	/** The progress bar. */
	private final JProgressBar progressBar;
	
	/** The cancel. */
	private boolean cancel = false;
	
	/** Can we cancel. */
	private boolean canCancel = false;
	

	/**
	 * Instantiates a new progress.
	 *
	 * @param owner the owner
	 */
	public Progress(final Window owner)
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
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(5, 5, 5, 5);
		gbc_panel.gridwidth = 2;
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		getContentPane().add(panel, gbc_panel);
		panel.setLayout(new GridLayout(0, 1, 5, 5));

		setInfos(1,false);

		progressBar = new JProgressBar();
		progressBar.setMaximumSize(new Dimension(32767, 20));
		progressBar.setMinimumSize(new Dimension(300, 20));
		progressBar.setPreferredSize(new Dimension(450, 20));
		final GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.insets = new Insets(0, 5, 5, 5);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 1;
		getContentPane().add(progressBar, gbc_progressBar);

		lblTimeleft = new JLabel("--:--:--"); //$NON-NLS-1$
		final GridBagConstraints gbc_lblTimeleft = new GridBagConstraints();
		gbc_lblTimeleft.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeleft.gridx = 1;
		gbc_lblTimeleft.gridy = 1;
		getContentPane().add(lblTimeleft, gbc_lblTimeleft);

		btnCancel = new JButton(Messages.getString("Progress.btnCancel.text")); //$NON-NLS-1$
		btnCancel.setIcon(MainFrame.getIcon("/jrm/resicons/icons/stop.png")); //$NON-NLS-1$
		btnCancel.addActionListener(e -> cancel());

		progressBar2 = new JProgressBar();
		progressBar2.setMaximumSize(new Dimension(32767, 20));
		progressBar2.setVisible(false);
		progressBar2.setPreferredSize(new Dimension(450, 20));
		progressBar2.setMinimumSize(new Dimension(300, 20));
		final GridBagConstraints gbc_progressBar2 = new GridBagConstraints();
		gbc_progressBar2.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar2.insets = new Insets(0, 5, 0, 5);
		gbc_progressBar2.gridx = 0;
		gbc_progressBar2.gridy = 2;
		getContentPane().add(progressBar2, gbc_progressBar2);

		lblTimeLeft2 = new JLabel("--:--:--"); //$NON-NLS-1$
		lblTimeLeft2.setVisible(false);
		final GridBagConstraints gbc_lblTimeLeft2 = new GridBagConstraints();
		gbc_lblTimeLeft2.fill = GridBagConstraints.VERTICAL;
		gbc_lblTimeLeft2.insets = new Insets(0, 0, 0, 5);
		gbc_lblTimeLeft2.gridx = 1;
		gbc_lblTimeLeft2.gridy = 2;
		getContentPane().add(lblTimeLeft2, gbc_lblTimeLeft2);
		final GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.weighty = 200.0;
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.gridwidth = 2;
		gbc_btnCancel.anchor = GridBagConstraints.SOUTH;
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 3;
		getContentPane().add(btnCancel, gbc_btnCancel);

		pack();
		setLocationRelativeTo(owner);
	}

	public void setInfos(int threadCnt, boolean multipleSubInfos)
	{
		if(lblInfo==null || lblInfo.length!=threadCnt || lblSubInfo==null || lblSubInfo.length!=(multipleSubInfos?threadCnt:1))
		{
			panel.removeAll();
			
			lblInfo = new JLabel[threadCnt];
			lblSubInfo = new JLabel[multipleSubInfos?threadCnt:1];
			
			for(int i = 0; i < threadCnt; i++)
			{
				panel.add(lblInfo[i] = new JLabel());
				lblInfo[i].setPreferredSize(new Dimension(0, 20));
				lblInfo[i].setMinimumSize(new Dimension(0, 20));
				lblInfo[i].setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		
				if(multipleSubInfos)
				{
					panel.add(lblSubInfo[i] = new JLabel());
					lblSubInfo[i].setPreferredSize(new Dimension(0, 20));
					lblSubInfo[i].setMinimumSize(new Dimension(0, 20));
					lblSubInfo[i].setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
				}
			}
			if(!multipleSubInfos)
			{
				panel.add(lblSubInfo[0] = new JLabel());
				lblSubInfo[0].setPreferredSize(new Dimension(0, 20));
				lblSubInfo[0].setMinimumSize(new Dimension(0, 20));
				lblSubInfo[0].setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			}
			
			packHeight();
		}
	}
	
	public void clearInfos()
	{
		for(JLabel label : lblInfo)
			label.setText(null);
		for(JLabel label : lblSubInfo)
			label.setText(null);
	}
	
	/** The lbl timeleft. */
	private final JLabel lblTimeleft;
	
	/** The btn cancel. */
	private final JButton btnCancel;

	/** The start time. */
	private long startTime = System.currentTimeMillis();
	
	/** The progress bar 2. */
	private final JProgressBar progressBar2;
	
	/** The lbl time left 2. */
	private final JLabel lblTimeLeft2;

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
			if (val > 0)
			{
				final String left = DurationFormatUtils.formatDuration((System.currentTimeMillis() - startTime) * (progressBar.getMaximum() - val) / val, "HH:mm:ss"); //$NON-NLS-1$
				final String total = DurationFormatUtils.formatDuration((System.currentTimeMillis() - startTime) * progressBar.getMaximum() / val, "HH:mm:ss"); //$NON-NLS-1$
				lblTimeleft.setText(String.format("%s / %s", left, total)); //$NON-NLS-1$
			}
			else
				lblTimeleft.setText("--:--:-- / --:--:--"); //$NON-NLS-1$
		}
		if(lblSubInfo.length==1)
			lblSubInfo[0].setText(submsg);
		else
			lblSubInfo[offset].setText(submsg);
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
			progressBar2.setStringPainted(msg != null || val > 0);
			progressBar2.setString(msg);
			progressBar2.setIndeterminate(val==0);
			if (max != null)
				progressBar2.setMaximum(max);
			if (val >= 0)
				progressBar2.setValue(val);
			if (val == 0)
				startTime2 = System.currentTimeMillis();
			if (val > 0)
			{
				final String left = DurationFormatUtils.formatDuration((System.currentTimeMillis() - startTime2) * (progressBar2.getMaximum() - val) / val, "HH:mm:ss"); //$NON-NLS-1$
				final String total = DurationFormatUtils.formatDuration((System.currentTimeMillis() - startTime2) * progressBar2.getMaximum() / val, "HH:mm:ss"); //$NON-NLS-1$
				lblTimeLeft2.setText(String.format("%s / %s", left, total)); //$NON-NLS-1$
			}
			else
				lblTimeLeft2.setText("--:--:-- / --:--:--"); //$NON-NLS-1$
		}
		else if (progressBar2.isVisible())
		{
			progressBar2.setVisible(false);
			lblTimeLeft2.setVisible(false);
			packHeight();
		}
	}

	/**
	 * Pack height.
	 */
	public void packHeight()
	{
		invalidate();
		final Dimension newSize = getPreferredSize();
		final Rectangle rect = getBounds();
		rect.height = Math.max(rect.height, newSize.height);
		setBounds(rect);
		validate();
	}

	public int getValue()
	{
		return progressBar.getValue();
	}

	public int getValue2()
	{
		return progressBar2.getValue();
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
