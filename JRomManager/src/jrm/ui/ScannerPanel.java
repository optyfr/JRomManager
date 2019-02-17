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
import jrm.profile.scan.options.ScanAutomation;
import jrm.security.Session;
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

	private ScannerAutomationPanel scannerAutomation;

	/** The btn fix. */
	private JButton btnFix;

	/** The btn scan. */
	private JButton btnScan;

	/** The lbl profileinfo. */
	private JLabel lblProfileinfo;

	private JTabbedPane mainPane;

	/**
	 * Create the panel.
	 */
	public ScannerPanel(final Session session)
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
				MainFrame.profile_viewer = new ProfileViewer(session, SwingUtilities.getWindowAncestor(this), session.curr_profile);
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
		btnFix.addActionListener(e -> fix(session));
		btnFix.setEnabled(false);
		btnScan.addActionListener(e -> scan(session, true));

		scannerTabbedPane = new JTabbedPane(SwingConstants.TOP);
		final GridBagConstraints gbc_scannerTabbedPane = new GridBagConstraints();
		gbc_scannerTabbedPane.fill = GridBagConstraints.BOTH;
		gbc_scannerTabbedPane.gridx = 0;
		gbc_scannerTabbedPane.gridy = 1;
		this.add(scannerTabbedPane, gbc_scannerTabbedPane);

		buildScannerDirTab(session);
		buildScannerSettingsTab(session);
		buildScannerFiltersTab(session);
		buildScannerAdvFiltersTab(session);
		buildScannerAutomationTab(session);

		lblProfileinfo = new JLabel(""); //$NON-NLS-1$
		lblProfileinfo.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		final GridBagConstraints gbc_lblProfileinfo = new GridBagConstraints();
		gbc_lblProfileinfo.insets = new Insets(0, 2, 0, 2);
		gbc_lblProfileinfo.fill = GridBagConstraints.BOTH;
		gbc_lblProfileinfo.gridx = 0;
		gbc_lblProfileinfo.gridy = 2;
		this.add(lblProfileinfo, gbc_lblProfileinfo);


	}

	private void buildScannerDirTab(final Session session)
	{
		scannerDirPanel = new ScannerDirPanel(session);
		scannerTabbedPane.addTab(Messages.getString("MainFrame.scannerDirectories.title"), null, scannerDirPanel, null); //$NON-NLS-1$
	}

	private void buildScannerSettingsTab(final Session session)
	{
		scannerSettingsPanel = new ScannerSettingsPanel();
		scannerTabbedPane.addTab(Messages.getString("MainFrame.scannerSettingsPanel.title"), null, scannerSettingsPanel, null); //$NON-NLS-1$

	}

	private void buildScannerFiltersTab(final Session session)
	{
		scannerFilters = new ScannerFiltersPanel(session);
		scannerTabbedPane.addTab(Messages.getString("MainFrame.Filters"), null, scannerFilters, null); //$NON-NLS-1$

	}

	private void buildScannerAdvFiltersTab(final Session session)
	{
		scannerAdvFilters = new ScannerAdvFilterPanel(session);
		scannerTabbedPane.addTab(Messages.getString("MainFrame.AdvFilters"), null, scannerAdvFilters, null); //$NON-NLS-1$

	}

	private void buildScannerAutomationTab(final Session session)
	{
		scannerAutomation = new ScannerAutomationPanel();
		scannerTabbedPane.addTab(Messages.getString("MainFrame.Automation"), null, scannerAutomation, null); //$NON-NLS-1$

	}

	/**
	 * Scan.
	 */
	private void scan(final Session session, final boolean automate)
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
				session.curr_scan = new Scan(session.curr_profile, progress);
				btnFix.setEnabled(session.curr_scan.actions.stream().mapToInt(Collection::size).sum() > 0);
				return null;
			}

			@Override
			protected void done()
			{
				progress.dispose();
				/* update entries in profile viewer */ 
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.reload();
				ScanAutomation automation = ScanAutomation.valueOf(session.curr_profile.settings.getProperty("automation.scan", ScanAutomation.SCAN.toString()));
				if(MainFrame.report_frame != null && automation.hasReport())
				{
					MainFrame.report_frame.setVisible(true);
					session.report.getModel().initClone();
				}
				if(automate)
				{
					if(btnFix.isEnabled() && automation.hasFix())
					{
						fix(session);
					}
				}
			}

		};
		worker.execute();
		progress.setVisible(true);
	}

	/**
	 * Fix.
	 */
	private void fix(final Session session)
	{
		final Progress progress = new Progress(SwingUtilities.getWindowAncestor(this));
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				if (session.curr_profile.hasPropsChanged())
				{
					switch (JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(ScannerPanel.this), Messages.getString("MainFrame.WarnSettingsChanged"), Messages.getString("MainFrame.RescanBeforeFix"), JOptionPane.YES_NO_CANCEL_OPTION)) //$NON-NLS-1$ //$NON-NLS-2$
					{
						case JOptionPane.YES_OPTION:
							session.curr_scan = new Scan(session.curr_profile, progress);
							btnFix.setEnabled(session.curr_scan.actions.stream().mapToInt(Collection::size).sum() > 0);
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
				final Fix fix = new Fix(session.curr_profile, session.curr_scan, progress);
				btnFix.setEnabled(fix.getActionsRemain() > 0);
				return null;
			}

			@Override
			protected void done()
			{
				progress.dispose();
				/* update entries in profile viewer */ 
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.reload();
				ScanAutomation automation = ScanAutomation.valueOf(session.curr_profile.settings.getProperty("automation.scan", ScanAutomation.SCAN.toString()));
				if(automation.hasScanAgain())
					scan(session, false);
			}

		};
		worker.execute();
		progress.setVisible(true);
	}

	/**
	 * Inits the scan settings.
	 */
	public void initProfileSettings(final Session session)
	{
		scannerSettingsPanel.initProfileSettings(session.curr_profile.settings);
		scannerDirPanel.initProfileSettings(session);
		scannerFilters.initProfileSettings(session);
		scannerAdvFilters.initProfileSettings(session);
		scannerAutomation.initProfileSettings(session.curr_profile.settings);
	}

	/**
	 * Load profile.
	 *
	 * @param profile
	 *            the profile
	 */
	@Override
	public void loadProfile(final Session session, final ProfileNFO profile)
	{
		if (session.curr_profile != null)
			session.curr_profile.saveSettings();
		final Progress progress = new Progress(SwingUtilities.getWindowAncestor(this));
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{
			boolean success = false;

			@Override
			protected Void doInBackground() throws Exception
			{
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.clear();
				success = (null != (Profile.load(session, profile, progress)));
				session.report.setProfile(session.curr_profile);
				if (MainFrame.profile_viewer != null)
					MainFrame.profile_viewer.reset(session.curr_profile);
				mainPane.setEnabledAt(1, success);
				btnScan.setEnabled(success);
				btnFix.setEnabled(false);
				lblProfileinfo.setText(session.curr_profile.getName());
				scannerFilters.checkBoxListSystems.setModel(session.curr_profile.systems);
				return null;
			}

			@Override
			protected void done()
			{
				if (success && session.curr_profile != null)
				{
					progress.close();
					initProfileSettings(session);
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
