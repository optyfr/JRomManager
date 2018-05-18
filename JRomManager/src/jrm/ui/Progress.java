package jrm.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import org.apache.commons.lang3.time.DurationFormatUtils;

import jrm.Messages;

@SuppressWarnings("serial")
public class Progress extends JDialog implements ProgressHandler
{
	private final JLabel lblInfo;
	private final JProgressBar progressBar;
	private boolean cancel = false;

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
				if(owner.getOwner()!=null)
					owner.getOwner().setEnabled(false);
			}

			@Override
			public void windowClosed(final WindowEvent e)
			{
				owner.setEnabled(true);
				owner.toFront();
				if(owner.getOwner()!=null)
					owner.getOwner().setEnabled(true);
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 26, 26, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		lblInfo = new JLabel();
		lblInfo.setPreferredSize(new Dimension(0, 20));
		lblInfo.setMinimumSize(new Dimension(0, 20));
		lblInfo.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		final GridBagConstraints gbc_lblInfo = new GridBagConstraints();
		gbc_lblInfo.insets = new Insets(5, 5, 0, 5);
		gbc_lblInfo.gridwidth = 2;
		gbc_lblInfo.fill = GridBagConstraints.BOTH;
		gbc_lblInfo.gridx = 0;
		gbc_lblInfo.gridy = 0;
		getContentPane().add(lblInfo, gbc_lblInfo);

		lblSubInfo = new JLabel();
		lblSubInfo.setPreferredSize(new Dimension(0, 20));
		lblSubInfo.setMinimumSize(new Dimension(0, 20));
		lblSubInfo.setText("");  //$NON-NLS-1$
		lblSubInfo.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		final GridBagConstraints gbc_lblSubInfo = new GridBagConstraints();
		gbc_lblSubInfo.insets = new Insets(5, 5, 0, 5);
		gbc_lblSubInfo.gridwidth = 2;
		gbc_lblSubInfo.fill = GridBagConstraints.BOTH;
		gbc_lblSubInfo.gridx = 0;
		gbc_lblSubInfo.gridy = 1;
		getContentPane().add(lblSubInfo, gbc_lblSubInfo);

		progressBar = new JProgressBar();
		progressBar.setMaximumSize(new Dimension(32767, 20));
		progressBar.setMinimumSize(new Dimension(300, 20));
		progressBar.setPreferredSize(new Dimension(450, 20));
		final GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.insets = new Insets(5, 5, 0, 5);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 2;
		getContentPane().add(progressBar, gbc_progressBar);

		lblTimeleft = new JLabel("--:--:--"); //$NON-NLS-1$
		final GridBagConstraints gbc_lblTimeleft = new GridBagConstraints();
		gbc_lblTimeleft.insets = new Insets(5, 0, 5, 5);
		gbc_lblTimeleft.gridx = 1;
		gbc_lblTimeleft.gridy = 2;
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
		gbc_progressBar2.insets = new Insets(5, 5, 0, 5);
		gbc_progressBar2.gridx = 0;
		gbc_progressBar2.gridy = 3;
		getContentPane().add(progressBar2, gbc_progressBar2);

		lblTimeLeft2 = new JLabel("--:--:--"); //$NON-NLS-1$
		lblTimeLeft2.setVisible(false);
		final GridBagConstraints gbc_lblTimeLeft2 = new GridBagConstraints();
		gbc_lblTimeLeft2.fill = GridBagConstraints.VERTICAL;
		gbc_lblTimeLeft2.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeLeft2.gridx = 1;
		gbc_lblTimeLeft2.gridy = 3;
		getContentPane().add(lblTimeLeft2, gbc_lblTimeLeft2);
		final GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 5, 0);
		gbc_btnCancel.gridwidth = 2;
		gbc_btnCancel.anchor = GridBagConstraints.NORTH;
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 4;
		getContentPane().add(btnCancel, gbc_btnCancel);

		pack();
		setLocationRelativeTo(owner);
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

	private final JLabel lblTimeleft;
	private final JLabel lblSubInfo;
	private final JButton btnCancel;

	private long startTime = 0;
	private final JProgressBar progressBar2;
	private final JLabel lblTimeLeft2;

	@Override
	public synchronized void setProgress(final String msg, final Integer val, final Integer max, final String submsg)
	{
		if(msg != null)
			lblInfo.setText(msg);
		if(val != null)
		{
			if(val < 0 && progressBar.isVisible())
			{
				progressBar.setVisible(false);
				lblTimeleft.setVisible(false);
				packHeight();
			}
			else if(val > 0 && !progressBar.isVisible())
			{
				progressBar.setVisible(true);
				lblTimeleft.setVisible(true);
				packHeight();
			}
			progressBar.setStringPainted(true);
			if(max != null)
				progressBar.setMaximum(max);
			if(val > 0)
				progressBar.setValue(val);
			if(val == 0)
				startTime = System.currentTimeMillis();
			if(val > 0)
			{
				final String left = DurationFormatUtils.formatDuration((System.currentTimeMillis() - startTime) * (progressBar.getMaximum() - val) / val, "HH:mm:ss"); //$NON-NLS-1$
				final String total = DurationFormatUtils.formatDuration((System.currentTimeMillis() - startTime) * progressBar.getMaximum() / val, "HH:mm:ss"); //$NON-NLS-1$
				lblTimeleft.setText(String.format("%s / %s", left, total)); //$NON-NLS-1$
			}
			else
				lblTimeleft.setText("--:--:-- / --:--:--"); //$NON-NLS-1$
		}
		lblSubInfo.setText(submsg);
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

	private long startTime2 = 0;

	@Override
	public void setProgress2(final String msg, final Integer val, final Integer max)
	{
		if(msg != null && val != null)
		{
			if(!progressBar2.isVisible())
			{
				progressBar2.setVisible(true);
				lblTimeLeft2.setVisible(true);
				packHeight();
			}
			progressBar2.setStringPainted(true);
			progressBar2.setString(msg);
			if(max != null)
				progressBar2.setMaximum(max);
			if(val > 0)
				progressBar2.setValue(val);
			if(val == 0)
				startTime2 = System.currentTimeMillis();
			if(val > 0)
			{
				final String left = DurationFormatUtils.formatDuration((System.currentTimeMillis() - startTime2) * (progressBar2.getMaximum() - val) / val, "HH:mm:ss"); //$NON-NLS-1$
				final String total = DurationFormatUtils.formatDuration((System.currentTimeMillis() - startTime2) * progressBar2.getMaximum() / val, "HH:mm:ss"); //$NON-NLS-1$
				lblTimeLeft2.setText(String.format("%s / %s", left, total)); //$NON-NLS-1$
			}
			else
				lblTimeLeft2.setText("--:--:-- / --:--:--"); //$NON-NLS-1$
		}
		else if(progressBar2.isVisible())
		{
			progressBar2.setVisible(false);
			lblTimeLeft2.setVisible(false);
			packHeight();
		}
	}

	public void packHeight()
	{
		final Dimension newSize = getPreferredSize();
		final Rectangle rect = getBounds();
		rect.height = newSize.height;
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
}
