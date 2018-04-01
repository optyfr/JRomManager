package jrm.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class Progress extends JDialog implements ProgressHandler
{
	private static final long serialVersionUID = -1378025750737673375L;
	private JTextField textField;
	private JProgressBar progressBar;
	private boolean cancel = false;

	public Progress(Window owner)
	{
		super(owner, "Progression", Dialog.ModalityType.APPLICATION_MODAL);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cancel = true;
			}
		});
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 525, 129);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
		gbc_horizontalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalStrut.gridx = 1;
		gbc_horizontalStrut.gridy = 0;
		getContentPane().add(horizontalStrut, gbc_horizontalStrut);

		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 1;
		getContentPane().add(verticalStrut, gbc_verticalStrut);

		textField = new JTextField();
		textField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 2;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 1;
		getContentPane().add(textField, gbc_textField);
		textField.setColumns(10);

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_1.gridx = 3;
		gbc_verticalStrut_1.gridy = 1;
		getContentPane().add(verticalStrut_1, gbc_verticalStrut_1);

		progressBar = new JProgressBar();
		progressBar.setMinimumSize(new Dimension(300, 20));
		progressBar.setPreferredSize(new Dimension(450, 20));
		progressBar.setStringPainted(true);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.insets = new Insets(0, 0, 5, 5);
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 2;
		getContentPane().add(progressBar, gbc_progressBar);
		
		lblTimeleft = new JLabel("--:--:--");
		GridBagConstraints gbc_lblTimeleft = new GridBagConstraints();
		gbc_lblTimeleft.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeleft.gridx = 2;
		gbc_lblTimeleft.gridy = 2;
		getContentPane().add(lblTimeleft, gbc_lblTimeleft);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_1 = new GridBagConstraints();
		gbc_horizontalStrut_1.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalStrut_1.gridx = 1;
		gbc_horizontalStrut_1.gridy = 3;
		getContentPane().add(horizontalStrut_1, gbc_horizontalStrut_1);

		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridwidth = 2;
		gbc_btnCancel.anchor = GridBagConstraints.NORTH;
		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 4;
		getContentPane().add(btnCancel, gbc_btnCancel);

		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_2 = new GridBagConstraints();
		gbc_horizontalStrut_2.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalStrut_2.gridx = 1;
		gbc_horizontalStrut_2.gridy = 5;
		getContentPane().add(horizontalStrut_2, gbc_horizontalStrut_2);

		pack();
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
		if (msg != null)
			textField.setText(msg);
		if (val != null)
		{
			progressBar.setIndeterminate(val < 0);
			progressBar.setStringPainted(val >= 0);
			if (max != null)
				progressBar.setMaximum(max);
			if (val >= 0)
			{
				progressBar.setValue(val);
			}
			if(val==0)
				startTime = System.currentTimeMillis();
			if(val>0)
				lblTimeleft.setText(DurationFormatUtils.formatDuration((long)((System.currentTimeMillis() - startTime) * (progressBar.getMaximum() - val) / val), "HH:mm:ss")+" / "+DurationFormatUtils.formatDuration((long)((System.currentTimeMillis() - startTime) * progressBar.getMaximum() / val), "HH:mm:ss"));
			else
				lblTimeleft.setText("--:--:-- / --:--:--");
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
		btnCancel.setText("Canceling...");
	}

}
