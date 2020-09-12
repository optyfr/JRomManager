package jrm.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.security.Session;

@SuppressWarnings("serial")
public class SettingsDbgPanel extends JPanel
{
	/** The cb log level. */
	private JComboBox<Level> cbLogLevel;

	/** The lbl memory usage. */
	private JLabel lblMemoryUsage;

	/** The scheduler. */
	final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	final Level[] levels = new Level[] {Level.OFF,Level.SEVERE,Level.WARNING,Level.INFO,Level.CONFIG,Level.FINE,Level.FINER,Level.FINEST,Level.ALL};

	/**
	 * Create the panel.
	 */
	public SettingsDbgPanel(final Session session)
	{
		final GridBagLayout gbl_debug = new GridBagLayout();
		gbl_debug.columnWidths = new int[] { 50, 0, 0, 10, 0 };
		gbl_debug.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_debug.columnWeights = new double[] { 1.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_debug.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		this.setLayout(gbl_debug);

		JLabel lblLogLevel = new JLabel(Messages.getString("MainFrame.lblLogLevel.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblLogLevel = new GridBagConstraints();
		gbc_lblLogLevel.anchor = GridBagConstraints.EAST;
		gbc_lblLogLevel.fill = GridBagConstraints.VERTICAL;
		gbc_lblLogLevel.insets = new Insets(0, 0, 5, 5);
		gbc_lblLogLevel.gridx = 0;
		gbc_lblLogLevel.gridy = 1;
		this.add(lblLogLevel, gbc_lblLogLevel);

		cbLogLevel = new JComboBox<>(new DefaultComboBoxModel<>(levels));
		final GridBagConstraints gbc_cbLogLevel = new GridBagConstraints();
		gbc_cbLogLevel.gridwidth = 2;
		gbc_cbLogLevel.insets = new Insets(0, 0, 5, 5);
		gbc_cbLogLevel.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbLogLevel.gridx = 1;
		gbc_cbLogLevel.gridy = 1;
		this.add(cbLogLevel, gbc_cbLogLevel);
		cbLogLevel.addActionListener(arg0 -> {
			session.getUser().getSettings().setProperty(SettingsEnum.debug_level, cbLogLevel.getSelectedItem().toString());
			Log.setLevel(Level.parse(cbLogLevel.getSelectedItem().toString()));
		}); //$NON-NLS-1$
		cbLogLevel.setSelectedItem(Level.parse(session.getUser().getSettings().getProperty(SettingsEnum.debug_level, Log.getLevel().toString()))); //$NON-NLS-1$

		JLabel lblMemory = new JLabel(Messages.getString("MainFrame.lblMemory.text")); //$NON-NLS-1$
		lblMemory.setHorizontalAlignment(SwingConstants.TRAILING);
		final GridBagConstraints gbc_lblMemory = new GridBagConstraints();
		gbc_lblMemory.anchor = GridBagConstraints.EAST;
		gbc_lblMemory.insets = new Insets(0, 0, 5, 5);
		gbc_lblMemory.gridx = 0;
		gbc_lblMemory.gridy = 2;
		this.add(lblMemory, gbc_lblMemory);

		lblMemoryUsage = new JLabel(" "); //$NON-NLS-1$
		lblMemoryUsage.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		final GridBagConstraints gbc_lblMemoryUsage = new GridBagConstraints();
		gbc_lblMemoryUsage.fill = GridBagConstraints.BOTH;
		gbc_lblMemoryUsage.insets = new Insets(0, 0, 5, 5);
		gbc_lblMemoryUsage.gridx = 1;
		gbc_lblMemoryUsage.gridy = 2;
		this.add(lblMemoryUsage, gbc_lblMemoryUsage);

		JButton btnGc = new JButton(Messages.getString("MainFrame.btnGc.text")); //$NON-NLS-1$
		btnGc.addActionListener(e -> {
			System.gc();
			updateMemory();
		});
		final GridBagConstraints gbc_btnGc = new GridBagConstraints();
		gbc_btnGc.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnGc.insets = new Insets(0, 0, 5, 5);
		gbc_btnGc.gridx = 2;
		gbc_btnGc.gridy = 2;
		this.add(btnGc, gbc_btnGc);

		scheduler.scheduleAtFixedRate(() -> updateMemory(), 0, 20, TimeUnit.SECONDS);
	}


	/**
	 * Update memory.
	 */
	void updateMemory()
	{
		final Runtime rt = Runtime.getRuntime();
		lblMemoryUsage.setText(String.format(Messages.getString("MainFrame.MemoryUsage"), String.format("%.2f MiB", rt.totalMemory() / 1048576.0), String.format("%.2f MiB", (rt.totalMemory() - rt.freeMemory()) / 1048576.0), String.format("%.2f MiB", rt.freeMemory() / 1048576.0), String.format("%.2f MiB", rt.maxMemory() / 1048576.0))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

}
