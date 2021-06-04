package jrm.ui.profile.report;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import jrm.misc.Log;
import jrm.profile.report.Report;
import jrm.security.Session;

@SuppressWarnings("serial")
public class ReportLite extends JDialog
{
	private Window parentWindow;
	/**
	 * Create the dialog.
	 */
	public ReportLite(final Session session, Window parent, File reportFile)
	{
		super(parent);
		this.parentWindow = parent;
		//	setModal(true);
		//	setModalityType(ModalityType.APPLICATION_MODAL);
		parent.setEnabled(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//		setAlwaysOnTop(true);
		setSize(600, 600);
		setLocationRelativeTo(parent);
		setVisible(true);
		getContentPane().setLayout(new BorderLayout());
		JLabel wait = new JLabel("Loading...");
		wait.setFont(getFont().deriveFont(14.0f));
		wait.setHorizontalAlignment(SwingConstants.CENTER);
		wait.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(wait, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				buttonPane.add(okButton);
				okButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						ReportLite.this.dispose();
					}
				});
				getRootPane().setDefaultButton(okButton);
			}
		}
		new SwingWorker<Report, Void>()
		{
			@Override
			protected Report doInBackground() throws Exception
			{
				try
				{
					return Report.load(session, reportFile);
				}
				catch (Exception e)
				{
					Log.err(e.getMessage(), e);
				}
				return null;
			}
			
			@Override
			protected void done()
			{
				try
				{
					getContentPane().remove(wait);
					getContentPane().add(new ReportView(get()), BorderLayout.CENTER, 0);
					validate();
				}
				catch (InterruptedException | ExecutionException e)
				{
					Log.err(e.getMessage(), e);
				}
			}
		}.execute();
	}
	
	@Override
	public void dispose()
	{
		parentWindow.setEnabled(true);
		super.dispose();
	}

}
