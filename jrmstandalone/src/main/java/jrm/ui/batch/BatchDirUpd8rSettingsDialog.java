package jrm.ui.batch;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import jrm.ui.ScannerSettingsPanel;
import lombok.Getter;

@SuppressWarnings("serial")
public class BatchDirUpd8rSettingsDialog extends JDialog
{

	final ScannerSettingsPanel settingsPanel = new ScannerSettingsPanel();

	private @Getter boolean success = false;

	/**
	 * Create the dialog.
	 */
	@SuppressWarnings("exports")
	public BatchDirUpd8rSettingsDialog(Window parent)
	{
		super(parent);
		setAlwaysOnTop(true);
		setModal(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 455, 410);
		getContentPane().setLayout(new BorderLayout());
		settingsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(settingsPanel, BorderLayout.CENTER);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this::actionPerformed);
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this::actionPerformed);
		buttonPane.add(cancelButton);
		pack();
		setLocationRelativeTo(parent);
	}

	public void actionPerformed(@SuppressWarnings("exports") ActionEvent e)
	{
		success = e.getActionCommand().equals("OK");
		dispose();
	}

}
