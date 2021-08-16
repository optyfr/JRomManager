package jrm.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import jrm.locale.Messages;
import jrm.misc.SettingsEnum;
import jrm.profile.data.Driver;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.DisplayOrientation;
import jrm.profile.data.Software.Supported;
import jrm.profile.data.Systm;
import jrm.security.Session;
import jrm.ui.basic.JCheckBoxList;
import jrm.ui.profile.data.YearsModel;

@SuppressWarnings("serial")
public class ScannerFiltersPanel extends JSplitPane
{
	/** The cbbx driver status. */
	private JComboBox<Driver.StatusType> cbbxDriverStatus;

	/** The cbbx filter cabinet type. */
	private JComboBox<CabinetType> cbbxFilterCabinetType;

	/** The cbbx filter display orientation. */
	private JComboBox<DisplayOrientation> cbbxFilterDisplayOrientation;

	/** The cbbx SW min supported lvl. */
	private JComboBox<Supported> cbbxSWMinSupportedLvl;

	/** The cbbx year max. */
	private JComboBox<String> cbbxYearMax;

	/** The cbbx year min. */
	private JComboBox<String> cbbxYearMin;

	/** The chckbx include clones. */
	private JCheckBox chckbxIncludeClones;

	/** The chckbx include disks. */
	private JCheckBox chckbxIncludeDisks;

	/** The chckbx include samples. */
	private JCheckBox chckbxIncludeSamples;

	/** The check box list systems. */
	JCheckBoxList<Systm> checkBoxListSystems;


	/**
	 * Create the panel.
	 */
	public ScannerFiltersPanel(final Session session)
	{
		this.setResizeWeight(0.5);
		this.setOneTouchExpandable(true);
		this.setContinuousLayout(true);
		JScrollPane systemsFilterScrollPane = new JScrollPane();
		this.setRightComponent(systemsFilterScrollPane);
		systemsFilterScrollPane.setViewportBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), Messages.getString("MainFrame.systemsFilter.viewportBorderTitle"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))); //$NON-NLS-1$

		checkBoxListSystems = new JCheckBoxList<>();
		checkBoxListSystems.setCellRenderer(checkBoxListSystems.new CellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(final JList<? extends Systm> list, final Systm value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				final JCheckBox checkbox = (JCheckBox) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				checkbox.setSelected(value.isSelected(session.getCurrProfile()));
				return checkbox;
			}
		});
		checkBoxListSystems.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting())
			{
				if (e.getFirstIndex() != -1)
				{
					for (int index = e.getFirstIndex(); index <= e.getLastIndex(); index++)
						checkBoxListSystems.getModel().getElementAt(index).setSelected(session.getCurrProfile(),checkBoxListSystems.isSelectedIndex(index));
					if (MainFrame.getProfileViewer() != null)
						MainFrame.getProfileViewer().reset(session.getCurrProfile());
				}
			}
		});
		systemsFilterScrollPane.setViewportView(checkBoxListSystems);

		JPopupMenu popupMenu = new JPopupMenu();
		MainFrame.addPopup(checkBoxListSystems, popupMenu);

		JMenu mnSelect = new JMenu(Messages.getString("MainFrame.mnSelect.text")); //$NON-NLS-1$
		popupMenu.add(mnSelect);

		JMenuItem mntmSelectAll = new JMenuItem(Messages.getString("MainFrame.mntmSelectAll.text")); //$NON-NLS-1$
		mnSelect.add(mntmSelectAll);

		JMenuItem mntmSelectAllBios = new JMenuItem(Messages.getString("MainFrame.mntmAllBios.text")); //$NON-NLS-1$
		mntmSelectAllBios.addActionListener(e -> checkBoxListSystems.select(sys -> sys.getType() == Systm.Type.BIOS, true));
		mnSelect.add(mntmSelectAllBios);

		JMenuItem mntmSelectAllSoftwares = new JMenuItem(Messages.getString("MainFrame.mntmAllSoftwares.text")); //$NON-NLS-1$
		mntmSelectAllSoftwares.addActionListener(e -> checkBoxListSystems.select(sys -> sys.getType() == Systm.Type.SOFTWARELIST, true));
		mnSelect.add(mntmSelectAllSoftwares);

		JMenu mnUnselect = new JMenu(Messages.getString("MainFrame.mnUnselect.text")); //$NON-NLS-1$
		popupMenu.add(mnUnselect);

		JMenuItem mntmUnselectAll = new JMenuItem(Messages.getString("MainFrame.mntmSelectNone.text")); //$NON-NLS-1$
		mnUnselect.add(mntmUnselectAll);

		JMenuItem mntmUnselectAllBios = new JMenuItem(Messages.getString("MainFrame.mntmAllBios.text")); //$NON-NLS-1$
		mntmUnselectAllBios.addActionListener(e -> checkBoxListSystems.select(sys -> sys.getType() == Systm.Type.BIOS, false));
		mnUnselect.add(mntmUnselectAllBios);

		JMenuItem mntmUnselectAllSoftwares = new JMenuItem(Messages.getString("MainFrame.mntmAllSoftwares.text")); //$NON-NLS-1$
		mntmUnselectAllSoftwares.addActionListener(e -> checkBoxListSystems.select(sys -> sys.getType() == Systm.Type.SOFTWARELIST, false));
		mnUnselect.add(mntmUnselectAllSoftwares);

		JMenuItem mntmInvertSelection = new JMenuItem(Messages.getString("MainFrame.mntmInvertSelection.text")); //$NON-NLS-1$
		mntmInvertSelection.addActionListener(e -> checkBoxListSystems.selectInvert());
		popupMenu.add(mntmInvertSelection);
		mntmUnselectAll.addActionListener(e -> checkBoxListSystems.selectNone());
		mntmSelectAll.addActionListener(e -> checkBoxListSystems.selectAll());

		JPanel panel = new JPanel();
		this.setLeftComponent(panel);
		final GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 20, 100, 0, 100, 20, 0 };
		gbl_panel.rowHeights = new int[] { 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		chckbxIncludeClones = new JCheckBox(Messages.getString("MainFrame.chckbxIncludeClones.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxIncludeClones = new GridBagConstraints();
		gbc_chckbxIncludeClones.gridwidth = 3;
		gbc_chckbxIncludeClones.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIncludeClones.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIncludeClones.anchor = GridBagConstraints.NORTH;
		gbc_chckbxIncludeClones.gridx = 1;
		gbc_chckbxIncludeClones.gridy = 1;
		panel.add(chckbxIncludeClones, gbc_chckbxIncludeClones);

		chckbxIncludeDisks = new JCheckBox(Messages.getString("MainFrame.chckbxIncludeDisks.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxIncludeDisks = new GridBagConstraints();
		gbc_chckbxIncludeDisks.gridwidth = 3;
		gbc_chckbxIncludeDisks.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIncludeDisks.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIncludeDisks.gridx = 1;
		gbc_chckbxIncludeDisks.gridy = 2;
		panel.add(chckbxIncludeDisks, gbc_chckbxIncludeDisks);

		chckbxIncludeSamples = new JCheckBox(Messages.getString("MainFrame.chckbxIncludeSamples.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_chckbxIncludeSamples = new GridBagConstraints();
		gbc_chckbxIncludeSamples.gridwidth = 3;
		gbc_chckbxIncludeSamples.fill = GridBagConstraints.HORIZONTAL;
		gbc_chckbxIncludeSamples.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIncludeSamples.gridx = 1;
		gbc_chckbxIncludeSamples.gridy = 3;
		panel.add(chckbxIncludeSamples, gbc_chckbxIncludeSamples);
		chckbxIncludeSamples.setSelected(true);

		JLabel lblCabinetType = new JLabel(Messages.getString("MainFrame.lblMachineType.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblCabinetType = new GridBagConstraints();
		gbc_lblCabinetType.gridwidth = 2;
		gbc_lblCabinetType.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblCabinetType.insets = new Insets(0, 0, 5, 5);
		gbc_lblCabinetType.gridx = 1;
		gbc_lblCabinetType.gridy = 4;
		panel.add(lblCabinetType, gbc_lblCabinetType);
		lblCabinetType.setHorizontalAlignment(SwingConstants.TRAILING);

		cbbxFilterCabinetType = new JComboBox<>();
		final GridBagConstraints gbc_cbbxFilterCabinetType = new GridBagConstraints();
		gbc_cbbxFilterCabinetType.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxFilterCabinetType.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxFilterCabinetType.gridx = 3;
		gbc_cbbxFilterCabinetType.gridy = 4;
		panel.add(cbbxFilterCabinetType, gbc_cbbxFilterCabinetType);
		cbbxFilterCabinetType.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				session.getCurrProfile().setProperty(SettingsEnum.filter_CabinetType, e.getItem().toString()); //$NON-NLS-1$
				if (MainFrame.getProfileViewer() != null)
					MainFrame.getProfileViewer().reset(session.getCurrProfile());
			}
		});
		cbbxFilterCabinetType.setModel(new DefaultComboBoxModel<>(CabinetType.values()));

		JLabel lblDisplayOrientation = new JLabel(Messages.getString("MainFrame.lblOrientation.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblDisplayOrientation = new GridBagConstraints();
		gbc_lblDisplayOrientation.gridwidth = 2;
		gbc_lblDisplayOrientation.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDisplayOrientation.insets = new Insets(0, 0, 5, 5);
		gbc_lblDisplayOrientation.gridx = 1;
		gbc_lblDisplayOrientation.gridy = 5;
		panel.add(lblDisplayOrientation, gbc_lblDisplayOrientation);
		lblDisplayOrientation.setHorizontalAlignment(SwingConstants.TRAILING);

		cbbxFilterDisplayOrientation = new JComboBox<>();
		final GridBagConstraints gbc_cbbxFilterDisplayOrientation = new GridBagConstraints();
		gbc_cbbxFilterDisplayOrientation.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxFilterDisplayOrientation.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxFilterDisplayOrientation.gridx = 3;
		gbc_cbbxFilterDisplayOrientation.gridy = 5;
		panel.add(cbbxFilterDisplayOrientation, gbc_cbbxFilterDisplayOrientation);
		cbbxFilterDisplayOrientation.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				session.getCurrProfile().setProperty(SettingsEnum.filter_DisplayOrientation, e.getItem().toString()); //$NON-NLS-1$
				if (MainFrame.getProfileViewer() != null)
					MainFrame.getProfileViewer().reset(session.getCurrProfile());
			}
		});
		cbbxFilterDisplayOrientation.setModel(new DefaultComboBoxModel<>(DisplayOrientation.values()));

		JLabel lblDriverStatus = new JLabel(Messages.getString("MainFrame.lblDriverStatus.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblDriverStatus = new GridBagConstraints();
		gbc_lblDriverStatus.gridwidth = 2;
		gbc_lblDriverStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDriverStatus.insets = new Insets(0, 0, 5, 5);
		gbc_lblDriverStatus.gridx = 1;
		gbc_lblDriverStatus.gridy = 6;
		panel.add(lblDriverStatus, gbc_lblDriverStatus);
		lblDriverStatus.setHorizontalAlignment(SwingConstants.TRAILING);

		cbbxDriverStatus = new JComboBox<>();
		final GridBagConstraints gbc_cbbxDriverStatus = new GridBagConstraints();
		gbc_cbbxDriverStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxDriverStatus.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxDriverStatus.gridx = 3;
		gbc_cbbxDriverStatus.gridy = 6;
		panel.add(cbbxDriverStatus, gbc_cbbxDriverStatus);
		cbbxDriverStatus.setModel(new DefaultComboBoxModel<>(Driver.StatusType.values()));

		JLabel lblSwMinSupportedLvl = new JLabel(Messages.getString("MainFrame.lblSwMinSupport.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblSwMinSupportedLvl = new GridBagConstraints();
		gbc_lblSwMinSupportedLvl.gridwidth = 2;
		gbc_lblSwMinSupportedLvl.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblSwMinSupportedLvl.insets = new Insets(0, 0, 5, 5);
		gbc_lblSwMinSupportedLvl.gridx = 1;
		gbc_lblSwMinSupportedLvl.gridy = 7;
		panel.add(lblSwMinSupportedLvl, gbc_lblSwMinSupportedLvl);
		lblSwMinSupportedLvl.setHorizontalAlignment(SwingConstants.TRAILING);

		cbbxSWMinSupportedLvl = new JComboBox<>();
		final GridBagConstraints gbc_cbbxSWMinSupportedLvl = new GridBagConstraints();
		gbc_cbbxSWMinSupportedLvl.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxSWMinSupportedLvl.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxSWMinSupportedLvl.gridx = 3;
		gbc_cbbxSWMinSupportedLvl.gridy = 7;
		panel.add(cbbxSWMinSupportedLvl, gbc_cbbxSWMinSupportedLvl);
		cbbxSWMinSupportedLvl.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				session.getCurrProfile().setProperty(SettingsEnum.filter_MinSoftwareSupportedLevel, e.getItem().toString()); //$NON-NLS-1$
				if (MainFrame.getProfileViewer() != null)
					MainFrame.getProfileViewer().reset(session.getCurrProfile());
			}
		});
		cbbxSWMinSupportedLvl.setModel(new DefaultComboBoxModel<>(Supported.values()));
		cbbxSWMinSupportedLvl.setSelectedIndex(0);

		cbbxYearMin = new JComboBox<>();
		cbbxYearMin.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				session.getCurrProfile().setProperty(SettingsEnum.filter_YearMin, e.getItem().toString()); //$NON-NLS-1$
				if (MainFrame.getProfileViewer() != null)
					MainFrame.getProfileViewer().reset(session.getCurrProfile());
			}
		});
		final GridBagConstraints gbc_cbbxYearMin = new GridBagConstraints();
		gbc_cbbxYearMin.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxYearMin.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxYearMin.gridx = 1;
		gbc_cbbxYearMin.gridy = 8;
		panel.add(cbbxYearMin, gbc_cbbxYearMin);

		JLabel lblYear = new JLabel(Messages.getString("MainFrame.lblYear.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblYear = new GridBagConstraints();
		gbc_lblYear.insets = new Insets(0, 0, 5, 5);
		gbc_lblYear.gridx = 2;
		gbc_lblYear.gridy = 8;
		panel.add(lblYear, gbc_lblYear);
		lblYear.setHorizontalAlignment(SwingConstants.CENTER);

		cbbxYearMax = new JComboBox<>();
		cbbxYearMax.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				session.getCurrProfile().setProperty(SettingsEnum.filter_YearMax, e.getItem().toString()); //$NON-NLS-1$
				if (MainFrame.getProfileViewer() != null)
					MainFrame.getProfileViewer().reset(session.getCurrProfile());
			}
		});
		final GridBagConstraints gbc_cbbxYearMax = new GridBagConstraints();
		gbc_cbbxYearMax.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbbxYearMax.insets = new Insets(0, 0, 5, 5);
		gbc_cbbxYearMax.gridx = 3;
		gbc_cbbxYearMax.gridy = 8;
		panel.add(cbbxYearMax, gbc_cbbxYearMax);
		cbbxDriverStatus.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				session.getCurrProfile().setProperty(SettingsEnum.filter_DriverStatus, e.getItem().toString()); //$NON-NLS-1$
				if (MainFrame.getProfileViewer() != null)
					MainFrame.getProfileViewer().reset(session.getCurrProfile());
			}
		});
		chckbxIncludeDisks.addItemListener(e -> {
			session.getCurrProfile().setProperty(SettingsEnum.filter_InclDisks, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
			if (MainFrame.getProfileViewer() != null)
				MainFrame.getProfileViewer().reset(session.getCurrProfile());
		});
		chckbxIncludeClones.addItemListener(e -> {
			session.getCurrProfile().setProperty(SettingsEnum.filter_InclClones, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
			if (MainFrame.getProfileViewer() != null)
				MainFrame.getProfileViewer().reset(session.getCurrProfile());
		});
		chckbxIncludeSamples.addItemListener(e -> {
			session.getCurrProfile().setProperty(SettingsEnum.filter_InclSamples, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
			if (MainFrame.getProfileViewer() != null)
				MainFrame.getProfileViewer().reset(session.getCurrProfile());
		});


	}

	public void initProfileSettings(final Session session)
	{
		chckbxIncludeClones.setSelected(session.getCurrProfile().getProperty(SettingsEnum.filter_InclClones, true)); //$NON-NLS-1$
		chckbxIncludeDisks.setSelected(session.getCurrProfile().getProperty(SettingsEnum.filter_InclDisks, true)); //$NON-NLS-1$
		chckbxIncludeSamples.setSelected(session.getCurrProfile().getProperty(SettingsEnum.filter_InclSamples, true)); //$NON-NLS-1$
		cbbxDriverStatus.setSelectedItem(Driver.StatusType.valueOf(session.getCurrProfile().getProperty(SettingsEnum.filter_DriverStatus, Driver.StatusType.preliminary.toString()))); //$NON-NLS-1$
		cbbxFilterCabinetType.setSelectedItem(CabinetType.valueOf(session.getCurrProfile().getProperty(SettingsEnum.filter_CabinetType, CabinetType.any.toString()))); //$NON-NLS-1$
		cbbxFilterDisplayOrientation.setSelectedItem(DisplayOrientation.valueOf(session.getCurrProfile().getProperty(SettingsEnum.filter_DisplayOrientation, DisplayOrientation.any.toString()))); //$NON-NLS-1$
		cbbxSWMinSupportedLvl.setSelectedItem(Supported.valueOf(session.getCurrProfile().getProperty(SettingsEnum.filter_MinSoftwareSupportedLevel, Supported.no.toString()))); //$NON-NLS-1$
		cbbxYearMin.setModel(new YearsModel(session.getCurrProfile().getYears()));
		cbbxYearMin.setSelectedItem(session.getCurrProfile().getProperty(SettingsEnum.filter_YearMin, cbbxYearMin.getModel().getElementAt(0))); //$NON-NLS-1$
		cbbxYearMax.setModel(new YearsModel(session.getCurrProfile().getYears()));
		cbbxYearMax.setSelectedItem(session.getCurrProfile().getProperty(SettingsEnum.filter_YearMax, cbbxYearMax.getModel().getElementAt(cbbxYearMax.getModel().getSize() - 1))); //$NON-NLS-1$
		
	}
	
}
