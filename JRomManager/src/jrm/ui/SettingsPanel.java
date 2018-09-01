package jrm.ui;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import jrm.locale.Messages;

@SuppressWarnings("serial")
public class SettingsPanel extends JPanel
{

	/** The settings pane. */
	private JTabbedPane settingsPane;


	/**
	 * Create the panel.
	 */
	public SettingsPanel()
	{
		this.setLayout(new BorderLayout(0, 0));

		settingsPane = new JTabbedPane(SwingConstants.TOP);
		this.add(settingsPane);

		buildSettingsCompressorsTab();
		buildSettingsDebugTab();
	}

	/**
	 * 
	 */
	private void buildSettingsCompressorsTab()
	{
		SettingsCompressorsPanel compressors = new SettingsCompressorsPanel();
		settingsPane.addTab(Messages.getString("MainFrame.Compressors"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/compress.png")), compressors, null); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void buildSettingsDebugTab()
	{
		SettingsDbgPanel debug = new SettingsDbgPanel();
		settingsPane.addTab(Messages.getString("MainFrame.Debug"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/bug.png")), debug, null); //$NON-NLS-1$ //$NON-NLS-2$
	}
}

