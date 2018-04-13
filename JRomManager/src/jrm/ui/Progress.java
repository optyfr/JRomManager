package jrm.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.border.BevelBorder;

import org.apache.commons.lang3.time.DurationFormatUtils;

import jrm.Messages;

@SuppressWarnings("serial")
public class Progress extends JDialog implements ProgressHandler
{
	private JLabel lblInfo;
	private JProgressBar progressBar;
	private boolean cancel = false;

	public Progress(Window owner)
	{
		super(owner, Messages.getString("Progress.Title"), ModalityType.MODELESS); //$NON-NLS-1$
		setIconImage(Toolkit.getDefaultToolkit().getImage(Progress.class.getResource("/jrm/resources/rom.png"))); //$NON-NLS-1$
		getContentPane().setBackground(UIManager.getColor("Panel.background")); //$NON-NLS-1$
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				cancel = true;
			}
			@Override
			public void windowOpened(WindowEvent e) {
				owner.setEnabled(false);
			}
			@Override
			public void windowClosed(WindowEvent e)
			{
				owner.setEnabled(true);
				owner.toFront();
			}
		});
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 32, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		lblInfo = new JLabel();
		lblInfo.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_lblInfo = new GridBagConstraints();
		gbc_lblInfo.insets = new Insets(5, 5, 5, 5);
		gbc_lblInfo.gridwidth = 2;
		gbc_lblInfo.fill = GridBagConstraints.BOTH;
		gbc_lblInfo.gridx = 0;
		gbc_lblInfo.gridy = 0;
		getContentPane().add(lblInfo, gbc_lblInfo);

		progressBar = new JProgressBar();
		progressBar.setMinimumSize(new Dimension(300, 20));
		progressBar.setPreferredSize(new Dimension(450, 20));
		progressBar.setStringPainted(true);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.insets = new Insets(0, 5, 5, 5);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 1;
		getContentPane().add(progressBar, gbc_progressBar);

		lblTimeleft = new JLabel("--:--:--"); //$NON-NLS-1$
		GridBagConstraints gbc_lblTimeleft = new GridBagConstraints();
		gbc_lblTimeleft.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeleft.gridx = 1;
		gbc_lblTimeleft.gridy = 1;
		getContentPane().add(lblTimeleft, gbc_lblTimeleft);

		btnCancel = new JButton(Messages.getString("Progress.btnCancel.text")); //$NON-NLS-1$
		btnCancel.setIcon(new ImageIcon(Progress.class.getResource("/jrm/resources/icons/stop.png"))); //$NON-NLS-1$
		btnCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancel();
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 5, 0);
		gbc_btnCancel.gridwidth = 2;
		gbc_btnCancel.anchor = GridBagConstraints.NORTH;
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 2;
		getContentPane().add(btnCancel, gbc_btnCancel);

		pack();
		setLocationRelativeTo(owner);
	}

	@Override
	public void setProgress(String msg)
	{
		setProgress(msg, null, null, null);
	}

	@Override
	public void setProgress(String msg, Integer val)
	{
		setProgress(msg, val, null, null);
	}

	@Override
	public void setProgress(String msg, Integer val, Integer max)
	{
		setProgress(msg, val, max, null);
	}

	long startTime = 0;
	private JLabel lblTimeleft;
	private JButton btnCancel;

	@Override
	public synchronized void setProgress(String msg, Integer val, Integer max, String submsg)
	{
		if(msg != null)
			lblInfo.setText(msg);
		if(val != null)
		{
			progressBar.setIndeterminate(val < 0);
			progressBar.setStringPainted(val >= 0);
			if(max != null)
				progressBar.setMaximum(max);
			if(val >= 0)
			{
				progressBar.setValue(val);
			}
			if(val == 0)
				startTime = System.currentTimeMillis();
			if(val > 0)
				lblTimeleft.setText(DurationFormatUtils.formatDuration((long) ((System.currentTimeMillis() - startTime) * (progressBar.getMaximum() - val) / val), "HH:mm:ss") + " / " + DurationFormatUtils.formatDuration((long) ((System.currentTimeMillis() - startTime) * progressBar.getMaximum() / val), "HH:mm:ss")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			else
				lblTimeleft.setText("--:--:-- / --:--:--"); //$NON-NLS-1$
		}
		// if(submsg!=null)
		progressBar.setString(submsg);
	}

	public boolean isCancel()
	{
		return cancel;
	}

	public void cancel()
	{
		this.cancel = true;
		btnCancel.setEnabled(false);
		btnCancel.setText(Messages.getString("Progress.Canceling")); //$NON-NLS-1$
	}
	
}
