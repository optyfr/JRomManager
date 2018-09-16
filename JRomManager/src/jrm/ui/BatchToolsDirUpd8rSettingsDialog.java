package jrm.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class BatchToolsDirUpd8rSettingsDialog extends JDialog
{

	final ScannerSettingsPanel settingsPanel = new ScannerSettingsPanel();
	
	public boolean success = false;


	/**
	 * Create the dialog.
	 */
	public BatchToolsDirUpd8rSettingsDialog(Window parent)
	{
		super(parent);
		setAlwaysOnTop(true);
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 455, 410);
		getContentPane().setLayout(new BorderLayout());
		settingsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(settingsPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				okButton.addActionListener(e->actionPerformed(e));
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(e->actionPerformed(e));
				buttonPane.add(cancelButton);
			}
		}
		pack();
		setLocationRelativeTo(parent);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		success = e.getActionCommand().equals("OK");
		dispose();
	}

}
