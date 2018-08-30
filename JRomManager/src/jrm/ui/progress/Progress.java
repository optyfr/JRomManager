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

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import org.apache.commons.lang3.time.DurationFormatUtils;

import JTrrntzip.LogCallback;
import jrm.locale.Messages;

// TODO: Auto-generated Javadoc
/**
 * The Class Progress.
 *
 * @author optyfr
 */
@SuppressWarnings("serial")
public class Progress extends JDialog implements ProgressHandler
{
	
	/** The panel. */
	private JPanel panel;
	
	/** The lbl info. */
	private JLabel[] lblInfo;
	
	/** The lbl sub info. */
	private JLabel[] lblSubInfo;
	
	/** The thread id offset. */
	private Map<Long,Integer> threadId_Offset = new HashMap<>();
	
	/** The progress bar. */
	private final JProgressBar progressBar;
	
	/** The cancel. */
	private boolean cancel = false;

	/**
	 * The Class ProgressTZipCallBack.
	 *
	 * @author optyfr
	 */
	public static final class ProgressTZipCallBack implements LogCallback
	{
		
		/** The ph. */
		ProgressHandler ph;
		
		/**
		 * Instantiates a new progress T zip call back.
		 *
		 * @param ph the ph
		 */
		public ProgressTZipCallBack(ProgressHandler ph)
		{
			this.ph = ph;
		}
		
		@Override
		public void StatusCallBack(int percent)
		{
			ph.setProgress(null, null, null, String.format("<html><table cellpadding=2 cellspacing=0><tr><td valign='middle'><table cellpadding=0 cellspacing=0 style='width:%dpx;font-size:2px;border:1px solid gray'><tr><td style='width:%dpx;background:#ff00'><td></table><td>", 208, percent*2)); //$NON-NLS-1$
		}

		@Override
		public boolean isVerboseLogging()
		{
			return false;
		}

		@Override
		public void StatusLogCallBack(String log)
		{
		}
		
	}
	
	
	/**
	 * The Class ProgressInputStream.
	 *
	 * @author optyfr
	 */
	public final class ProgressInputStream extends FilterInputStream
	{
		
		/** The value. */
		private int value;

		/**
		 * Instantiates a new progress input stream.
		 *
		 * @param in the in
		 * @param len the len
		 */
		protected ProgressInputStream(final InputStream in, final Integer len)
		{
			super(in);
			Progress.this.setProgress(null, (value = 0), len);
		}

		@Override
		public int read() throws IOException
		{
			final int ret = super.read();
			if (ret != -1)
				Progress.this.setProgress(null, ++value);
			return ret;
		}

		@Override
		public int read(final byte[] b) throws IOException
		{
			final int ret = super.read(b);
			if (ret != -1)
				Progress.this.setProgress(null, (value += ret));
			return ret;
		}

		@Override
		public int read(final byte[] b, final int off, final int len) throws IOException
		{
			final int ret = super.read(b, off, len);
			if (ret != -1)
				Progress.this.setProgress(null, (value += ret));
			return ret;
		}

		@Override
		public long skip(final long n) throws IOException
		{
			final long ret = super.skip(n);
			if (ret != -1)
				Progress.this.setProgress(null, (value += ret));
			return ret;
		}
	}

	/**
	 * Instantiates a new progress.
	 *
	 * @param owner the owner
	 */
	public Progress(final Window owner)
	{
		super(owner, Messages.getString("Progress.Title"), ModalityType.MODELESS); //$NON-NLS-1$
		setIconImage(Toolkit.getDefaultToolkit().getImage(Progress.class.getResource("/jrm/resources/rom.png"))); //$NON-NLS-1$
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
		btnCancel.setIcon(new ImageIcon(Progress.class.getResource("/jrm/resources/icons/stop.png"))); //$NON-NLS-1$
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

	@Override
	public void setInfos(int threadCnt, boolean multipleSubInfos)
	{
		panel.removeAll();
		
		lblInfo = new JLabel[threadCnt];
		lblSubInfo = new JLabel[multipleSubInfos?threadCnt:1];
		threadId_Offset.clear();
		
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
	
	@Override
	public void clearInfos()
	{
		for(JLabel label : lblInfo)
			label.setText(null);
		for(JLabel label : lblSubInfo)
			label.setText(null);
	}
	
	@Override
	public void setProgress(final String msg)
	{
		setProgress(msg, null, null, null);
	}

	@Override
	public void setProgress(final String msg, final Integer val)
	{
		setProgress(msg, val, null, null);
	}

	@Override
	public void setProgress(final String msg, final Integer val, final Integer max)
	{
		setProgress(msg, val, max, null);
	}

	/** The lbl timeleft. */
	private final JLabel lblTimeleft;
	
	/** The btn cancel. */
	private final JButton btnCancel;

	/** The start time. */
	private long startTime = 0;
	
	/** The progress bar 2. */
	private final JProgressBar progressBar2;
	
	/** The lbl time left 2. */
	private final JLabel lblTimeLeft2;

	@Override
	public synchronized void setProgress(final String msg, final Integer val, final Integer max, final String submsg)
	{
		if (!threadId_Offset.containsKey(Thread.currentThread().getId()))
		{
			if (threadId_Offset.size() < lblInfo.length)
				threadId_Offset.put(Thread.currentThread().getId(), threadId_Offset.size());
			else
			{
				ThreadGroup tg = Thread.currentThread().getThreadGroup();
				Thread[] tl = new Thread[tg.activeCount()];
				int tl_count = tg.enumerate(tl, false);
				boolean found = false;
				for (Map.Entry<Long, Integer> e : threadId_Offset.entrySet())
				{
					boolean exists = false;
					for (int i = 0; i < tl_count; i++)
					{
						if (e.getKey() == tl[i].getId())
						{
							exists = true;
							break;
						}
					}
					if (!exists)
					{
						threadId_Offset.remove(e.getKey());
						threadId_Offset.put(Thread.currentThread().getId(), e.getValue());
						found = true;
						break;
					}
				}
				if (!found)
					threadId_Offset.put(Thread.currentThread().getId(), 0);
			}
		}
		int offset = threadId_Offset.get(Thread.currentThread().getId());
		
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
			else if (val > 0 && !progressBar.isVisible())
			{
				progressBar.setVisible(true);
				lblTimeleft.setVisible(true);
				packHeight();
			}
			progressBar.setStringPainted(true);
			if (max != null)
				progressBar.setMaximum(max);
			if (val > 0)
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

	@Override
	public boolean isCancel()
	{
		return cancel;
	}

	@Override
	public void cancel()
	{
		cancel = true;
		btnCancel.setEnabled(false);
		btnCancel.setText(Messages.getString("Progress.Canceling")); //$NON-NLS-1$
	}

	@Override
	public void setProgress2(final String msg, final Integer val)
	{
		setProgress2(msg, val, null);
	}

	/** The start time 2. */
	private long startTime2 = 0;

	@Override
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
			progressBar2.setStringPainted(true);
			progressBar2.setString(msg);
			if (max != null)
				progressBar2.setMaximum(max);
			if (val > 0)
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
		final Dimension newSize = getPreferredSize();
		final Rectangle rect = getBounds();
		rect.height = Math.max(rect.height, newSize.height);
		setBounds(rect);
	}

	@Override
	public int getValue()
	{
		return progressBar.getValue();
	}

	@Override
	public int getValue2()
	{
		return progressBar2.getValue();
	}

	@Override
	public InputStream getInputStream(InputStream in, Integer len)
	{
		return new ProgressInputStream(in, len);
	}
}
