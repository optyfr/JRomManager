package jrm.ui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;

import jrm.locale.Messages;
import jrm.profile.Profile;
import jrm.profile.fix.Fix;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.scan.Scan;
import jrm.ui.profile.ProfileViewer;
import jrm.ui.progress.Progress;

@SuppressWarnings("serial")
public class ScannerPanel extends JPanel implements ProfileLoader
{
	/** The scanner cfg tab. */
	private JTabbedPane scannerTabbedPane;

	private ScannerSettingsPanel scannerSettingsPanel;

	private ScannerDirPanel scannerDirPanel;

	private ScannerFiltersPanel scannerFilters;

	private ScannerAdvFilterPanel scannerAdvFilters;
	
	/** The btn fix. */
	private JButton btnFix;

	/** The btn scan. */
	private JButton btnScan;

	/** The curr scan. */
	private Scan curr_scan;

	/** The lbl profileinfo. */
	private JLabel lblProfileinfo;

	private JTabbedPane mainPane;

	/**
	 * Create the panel.
	 */
	public ScannerPanel()
	{
		final GridBagLayout gbl_scannerTab = new GridBagLayout();
		gbl_scannerTab.columnWidths = new int[] { 104, 0 };
		gbl_scannerTab.rowHeights = new int[] { 0, 0, 24, 0 };
		gbl_scannerTab.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_scannerTab.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		this.setLayout(gbl_scannerTab);

		JPanel scannerBtnPanel = new JPanel();
		final GridBagConstraints gbc_scannerBtnPanel = new GridBagConstraints();
		gbc_scannerBtnPanel.insets = new Insets(0, 0, 5, 0);
		gbc_scannerBtnPanel.fill = GridBagConstraints.BOTH;
		gbc_scannerBtnPanel.gridx = 0;
		gbc_scannerBtnPanel.gridy = 0;
		this.add(scannerBtnPanel, gbc_scannerBtnPanel);

		JButton btnInfo = new JButton(Messages.getString("MainFrame.btnInfo.text")); //$NON-NLS-1$
		btnInfo.addActionListener(e -> {
			if (MainFrame.profile_viewer == null)
				MainFrame.profile_viewer = new ProfileViewer(SwingUtilities.getWindowAncestor(this), Profile.curr_profile);
			MainFrame.profile_viewer.setVisible(true);
		});
		btnInfo.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/information.png"))); //$NON-NLS-1$
		scannerBtnPanel.add(btnInfo);

		btnScan = new JButton(Messages.getString("MainFrame.btnScan.text")); //$NON-NLS-1$
		btnScan.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/magnifier.png"))); //$NON-NLS-1$
		scannerBtnPanel.add(btnScan);
		btnScan.setEnabled(false);

		JButton btnReport = new JButton(Messages.getString("MainFrame.btnReport.text")); //$NON-NLS-1$
		btnReport.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/report.png"))); //$NON-NLS-1$
		btnReport.addActionListener(e -> EventQueue.invokeLater(() -> MainFrame.report_frame.setVisible(true)));
		scannerBtnPanel.add(btnReport);

		btnFix = new JButton(Messages.getString("MainFrame.btnFix.text")); //$NON-NLS-1$
		btnFix.setIcon(new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/tick.png"))); //$NON-NLS-1$
		scannerBtnPanel.add(btnFix);
		btnFix.addActionListener(e -> fix());
		btnFix.setEnabled(false);
		btnScan.addActionListener(e -> scan());

		scannerTabbedPane = new JTabbedPane(SwingConstants.TOP);
		final GridBagConstraints gbc_scannerTabbedPane = new GridBagConstraints();
		gbc_scannerTabbedPane.fill = GridBagConstraints.BOTH;
		gbc_scannerTabbedPane.gridx = 0;
		gbc_scannerTabbedPane.gridy = 1;
		this.add(scannerTabbedPane, gbc_scannerTabbedPane);

		buildScannerDirTab();
		buildScannerSettingsTab();
		buildScannerFiltersTab();
		buildScannerAdvFiltersTab();

		lblProfileinfo = new JLabel(""); //$NON-NLS-1$
		lblProfileinfo.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		final GridBagConstraints gbc_lblProfileinfo = new GridBagConstraints();
		gbc_lblProfileinfo.insets = new Insets(0, 2, 0, 2);
		gbc_lblProfileinfo.fill = GridBagConstraints.BOTH;
		gbc_lblProfileinfo.gridx = 0;
		gbc_lblProfileinfo.gridy = 2;
		this.add(lblProfileinfo, gbc_lblProfileinfo);


	}

	private void buildScannerDirTab()
	{
		scannerDirPanel = new ScannerDirPanel();
		scannerTabbedPane.addTab(Messages.getString("MainFrame.scannerDirectories.title"), null, scannerDirPanel, null); //$NON-NLS-1$
	}

	private void buildScannerSettingsTab()
	{
		scannerSettingsPanel = new ScannerSettingsPanel();
		scannerTabbedPane.addTab(Messages.getString("MainFrame.scannerSettingsPanel.title"), null, scannerSettingsPanel, null); //$NON-NLS-1$

	}

	private void buildScannerFiltersTab()
	{
		scannerFilters = new ScannerFiltersPanel();
		scannerTabbedPane.addTab(Messages.getString("MainFrame.Filters"), null, scannerFilters, null); //$NON-NLS-1$

	}

	private void buildScannerAdvFiltersTab()
	{
		scannerAdvFilters = new ScannerAdvFilterPanel();
		scannerTabbedPane.addTab(Messages.getString("MainFrame.AdvFilters"), null, scannerAdvFilters, null); //$NON-NLS-1$

	}

	/**
	 * Scan.
	 */
	private void scan()
	{
		String txtdstdir = scannerDirPanel.txtRomsDest.getText();
		if (txtdstdir.isEmpty())
		{
			scannerDirPanel.btnRomsDest.doClick();
			txtdstdir = scannerDirPanel.txtRomsDest.getText();
		}
		if (txtdstdir.isEmpty())
			return;

		final Progress progress = new Progress(SwingUtilities.getWindowAncestor(this));
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				curr_scan = new Scan(Profile.curr_profile, progress);
				btnFix.setEnabled(curr_scan.actions.stream().mapToInt(Collection::size).sum() > 0);
				return null;
			}

			@Override
			protected void done()
			{
				progress.dispose();
			}

		};
		worker.execute();
		progress.setVisible(true);
	}

	/**
	 * Fix.
	 */
	private void fix()
	{
		final Progress progress = new Progress(SwingUtilities.getWindowAncestor(this));
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				if (Profile.curr_profile.hasPropsChanged())
				{
					switch (JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(ScannerPanel.this), Messages.getString("MainFrame.WarnSettingsChanged"), Messages.getString("MainFrame.RescanBeforeFix"), JOptionPane.YES_NO_CANCEL_OPTION)) //$NON-NLS-1$ //$NON-NLS-2$
					{
						case JOptionPane.YES_OPTION:
							curr_scan = new Scan(Profile.curr_profile, progress);
							btnFix.setEnabled(curr_scan.actions.stream().mapToInt(Collection::size).sum() > 0);
							if (!btnFix.isEnabled())
								return null;
							break;
						case JOptionPane.NO_OPTION:
							break;
						case JOptionPane.CANCEL_OPTION:
						default:
							return null;
					}
				}
				final Fix fix = new Fix(Profile.curr_profile, curr_scan, progress);
				btnFix.setEnabled(fix.getActionsRemain() > 0);
				return null;
			}

			@Override
			protected void done()
			{
				progress.dispose();
			}

		};
		worker.execute();
		progress.setVisible(true);
	}

	/**
	 * Inits the scan settings.
	 */
	public void initProfileSettings()
	{
		scannerSettingsPanel.initProfileSettings(Profile.curr_profile.settings);
		scannerDirPanel.initProfileSettings();
		scannerFilters.initProfileSettings();
		scannerAdvFilters.initProfileSettings();
	}

	/**
	 * Load profile.
	 *
	 * @param profile
	 *            the profile
	 */
	public void loadProfile(final ProfileNFO profile)
	{
		if (Profile.curr_profile != null)
			Profile.curr_profile.saveSettings();
		final Progress progress = new Progress(SwingUtilities.getWindowAncestor(this));
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{
			boolean success = false;

			@Override
			protected Void doInBackground() throws Exception
			{
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.clear();
				success = (null != (Profile.curr_profile = Profile.load(profile, progress)));
				Scan.report.setProfile(Profile.curr_profile);
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.reset(Profile.curr_profile);
				mainPane.setEnabledAt(1, success);
				btnScan.setEnabled(success);
				btnFix.setEnabled(false);
				lblProfileinfo.setText(Profile.curr_profile.getName());
				scannerFilters.checkBoxListSystems.setModel(Profile.curr_profile.systems);
				return null;
			}

			@Override
			protected void done()
			{
				progress.dispose();
				if (success && Profile.curr_profile != null)
				{
					initProfileSettings();
					mainPane.setSelectedIndex(1);
				}
			}

		};
		worker.execute();
		progress.setVisible(true);
	}

	JTabbedPane getMainPane()
	{
		return mainPane;
	}

	void setMainPane(JTabbedPane mainPane)
	{
		this.mainPane = mainPane;
	}

}
