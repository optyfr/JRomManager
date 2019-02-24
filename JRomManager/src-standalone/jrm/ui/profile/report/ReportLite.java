package jrm.ui.profile.report;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import jrm.profile.report.Report;
import jrm.security.Session;

@SuppressWarnings("serial")
public class ReportLite extends JDialog
{
	private Window parent;
	/**
	 * Create the dialog.
	 */
	public ReportLite(final Session session, Window parent, File reportFile)
	{
		super(parent);
		this.parent = parent;
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
		new SwingWorker<Void, Void>()
		{
			@Override
			protected Void doInBackground() throws Exception
			{
				Report report = Report.load(session, reportFile);
				wait.setText("Building tree...");
				ReportView contentPanel = new ReportView(report);
				getContentPane().remove(wait);
				getContentPane().add(contentPanel, BorderLayout.CENTER, 0);
				validate();
				return null;
			}
		}.execute();
	}
	
	@Override
	public void dispose()
	{
		parent.setEnabled(true);
		super.dispose();
	}

}
