package jrm.ui.batch;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import jrm.batch.TrntChkReport;
import jrm.security.Session;

@SuppressWarnings("serial")
public class BatchTrrntChkResultsDialog extends JDialog
{
	Window parentWindow;
	
	/**
	 * Create the dialog.
	 */
	public BatchTrrntChkResultsDialog(final Session session, Window parent, TrntChkReport results)
	{
		super(parent);
		this.parentWindow = parent;
//		setAlwaysOnTop(true);
//		setModal(true);
//		setModalityType(ModalityType.MODELESS);
		parent.setEnabled(false);
		setBounds(100, 100, 455, 410);
		getContentPane().setLayout(new BorderLayout());
		JScrollPane contentPanel = new BatchTrrntChkReportView(session, results);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						BatchTrrntChkResultsDialog.this.dispose();
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	
	@Override
	public void dispose()
	{
		parentWindow.setEnabled(true);
		super.dispose();
	}
}
