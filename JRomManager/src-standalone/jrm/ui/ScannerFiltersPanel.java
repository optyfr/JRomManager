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
import javax.swing.event.ListSelectionEvent;

import jrm.locale.Messages;
import jrm.misc.SettingsEnum;
import jrm.profile.data.Driver;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.DisplayOrientation;
import jrm.profile.data.Software.Supported;
import jrm.profile.data.Systm;
import jrm.security.Session;
import jrm.ui.basic.JCheckBoxList;
import jrm.ui.basic.Popup;
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
	public ScannerFiltersPanel(@SuppressWarnings("exports") final Session session)
	{
		this.setResizeWeight(0.5);
		this.setOneTouchExpandable(true);
		this.setContinuousLayout(true);
		JScrollPane systemsFilterScrollPane = new JScrollPane();
		this.setRightComponent(systemsFilterScrollPane);
		systemsFilterScrollPane.setViewportBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), Messages.getString("MainFrame.systemsFilter.viewportBorderTitle"), TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))); //$NON-NLS-1$

		checkBoxListSystems = new JCheckBoxList<>();
		checkBoxListSystems.setCellRenderer(getCheckBoxListSystemsCellRenderer(session));
		checkBoxListSystems.addListSelectionListener(e -> checkBoxListSystemsValueChanged(session, e));
		systemsFilterScrollPane.setViewportView(checkBoxListSystems);

		JPopupMenu popupMenu = new JPopupMenu();
		Popup.addPopup(checkBoxListSystems, popupMenu);

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
		final GridBagLayout gblPanel = new GridBagLayout();
		gblPanel.columnWidths = new int[] { 20, 100, 0, 100, 20, 0 };
		gblPanel.rowHeights = new int[] { 0, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gblPanel.columnWeights = new double[] { 1.0, 1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gblPanel.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gblPanel);

		chckbxIncludeClones = new JCheckBox(Messages.getString("MainFrame.chckbxIncludeClones.text")); //$NON-NLS-1$
		final GridBagConstraints gbcChckbxIncludeClones = new GridBagConstraints();
		gbcChckbxIncludeClones.gridwidth = 3;
		gbcChckbxIncludeClones.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxIncludeClones.insets = new Insets(0, 0, 5, 5);
		gbcChckbxIncludeClones.anchor = GridBagConstraints.NORTH;
		gbcChckbxIncludeClones.gridx = 1;
		gbcChckbxIncludeClones.gridy = 1;
		panel.add(chckbxIncludeClones, gbcChckbxIncludeClones);

		chckbxIncludeDisks = new JCheckBox(Messages.getString("MainFrame.chckbxIncludeDisks.text")); //$NON-NLS-1$
		final GridBagConstraints gbcChckbxIncludeDisks = new GridBagConstraints();
		gbcChckbxIncludeDisks.gridwidth = 3;
		gbcChckbxIncludeDisks.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxIncludeDisks.insets = new Insets(0, 0, 5, 5);
		gbcChckbxIncludeDisks.gridx = 1;
		gbcChckbxIncludeDisks.gridy = 2;
		panel.add(chckbxIncludeDisks, gbcChckbxIncludeDisks);

		chckbxIncludeSamples = new JCheckBox(Messages.getString("MainFrame.chckbxIncludeSamples.text")); //$NON-NLS-1$
		final GridBagConstraints gbcChckbxIncludeSamples = new GridBagConstraints();
		gbcChckbxIncludeSamples.gridwidth = 3;
		gbcChckbxIncludeSamples.fill = GridBagConstraints.HORIZONTAL;
		gbcChckbxIncludeSamples.insets = new Insets(0, 0, 5, 5);
		gbcChckbxIncludeSamples.gridx = 1;
		gbcChckbxIncludeSamples.gridy = 3;
		panel.add(chckbxIncludeSamples, gbcChckbxIncludeSamples);
		chckbxIncludeSamples.setSelected(true);

		JLabel lblCabinetType = new JLabel(Messages.getString("MainFrame.lblMachineType.text")); //$NON-NLS-1$
		final GridBagConstraints gbcLblCabinetType = new GridBagConstraints();
		gbcLblCabinetType.gridwidth = 2;
		gbcLblCabinetType.fill = GridBagConstraints.HORIZONTAL;
		gbcLblCabinetType.insets = new Insets(0, 0, 5, 5);
		gbcLblCabinetType.gridx = 1;
		gbcLblCabinetType.gridy = 4;
		panel.add(lblCabinetType, gbcLblCabinetType);
		lblCabinetType.setHorizontalAlignment(SwingConstants.TRAILING);

		cbbxFilterCabinetType = new JComboBox<>();
		final GridBagConstraints gbcCbbxFilterCabinetType = new GridBagConstraints();
		gbcCbbxFilterCabinetType.fill = GridBagConstraints.HORIZONTAL;
		gbcCbbxFilterCabinetType.insets = new Insets(0, 0, 5, 5);
		gbcCbbxFilterCabinetType.gridx = 3;
		gbcCbbxFilterCabinetType.gridy = 4;
		panel.add(cbbxFilterCabinetType, gbcCbbxFilterCabinetType);
		cbbxFilterCabinetType.addItemListener(e -> cbbxFilterCabinetTypeValueChanged(session, e));
		cbbxFilterCabinetType.setModel(new DefaultComboBoxModel<>(CabinetType.values()));

		JLabel lblDisplayOrientation = new JLabel(Messages.getString("MainFrame.lblOrientation.text")); //$NON-NLS-1$
		final GridBagConstraints gbcLblDisplayOrientation = new GridBagConstraints();
		gbcLblDisplayOrientation.gridwidth = 2;
		gbcLblDisplayOrientation.fill = GridBagConstraints.HORIZONTAL;
		gbcLblDisplayOrientation.insets = new Insets(0, 0, 5, 5);
		gbcLblDisplayOrientation.gridx = 1;
		gbcLblDisplayOrientation.gridy = 5;
		panel.add(lblDisplayOrientation, gbcLblDisplayOrientation);
		lblDisplayOrientation.setHorizontalAlignment(SwingConstants.TRAILING);

		cbbxFilterDisplayOrientation = new JComboBox<>();
		final GridBagConstraints gbcCbbxFilterDisplayOrientation = new GridBagConstraints();
		gbcCbbxFilterDisplayOrientation.fill = GridBagConstraints.HORIZONTAL;
		gbcCbbxFilterDisplayOrientation.insets = new Insets(0, 0, 5, 5);
		gbcCbbxFilterDisplayOrientation.gridx = 3;
		gbcCbbxFilterDisplayOrientation.gridy = 5;
		panel.add(cbbxFilterDisplayOrientation, gbcCbbxFilterDisplayOrientation);
		cbbxFilterDisplayOrientation.addItemListener(e -> cbbxFilterDisplayOrientationValueChanged(session, e));
		cbbxFilterDisplayOrientation.setModel(new DefaultComboBoxModel<>(DisplayOrientation.values()));

		JLabel lblDriverStatus = new JLabel(Messages.getString("MainFrame.lblDriverStatus.text")); //$NON-NLS-1$
		final GridBagConstraints gbcLblDriverStatus = new GridBagConstraints();
		gbcLblDriverStatus.gridwidth = 2;
		gbcLblDriverStatus.fill = GridBagConstraints.HORIZONTAL;
		gbcLblDriverStatus.insets = new Insets(0, 0, 5, 5);
		gbcLblDriverStatus.gridx = 1;
		gbcLblDriverStatus.gridy = 6;
		panel.add(lblDriverStatus, gbcLblDriverStatus);
		lblDriverStatus.setHorizontalAlignment(SwingConstants.TRAILING);

		cbbxDriverStatus = new JComboBox<>();
		final GridBagConstraints gbcCbbxDriverStatus = new GridBagConstraints();
		gbcCbbxDriverStatus.fill = GridBagConstraints.HORIZONTAL;
		gbcCbbxDriverStatus.insets = new Insets(0, 0, 5, 5);
		gbcCbbxDriverStatus.gridx = 3;
		gbcCbbxDriverStatus.gridy = 6;
		panel.add(cbbxDriverStatus, gbcCbbxDriverStatus);
		cbbxDriverStatus.setModel(new DefaultComboBoxModel<>(Driver.StatusType.values()));

		JLabel lblSwMinSupportedLvl = new JLabel(Messages.getString("MainFrame.lblSwMinSupport.text")); //$NON-NLS-1$
		final GridBagConstraints gbcLblSwMinSupportedLvl = new GridBagConstraints();
		gbcLblSwMinSupportedLvl.gridwidth = 2;
		gbcLblSwMinSupportedLvl.fill = GridBagConstraints.HORIZONTAL;
		gbcLblSwMinSupportedLvl.insets = new Insets(0, 0, 5, 5);
		gbcLblSwMinSupportedLvl.gridx = 1;
		gbcLblSwMinSupportedLvl.gridy = 7;
		panel.add(lblSwMinSupportedLvl, gbcLblSwMinSupportedLvl);
		lblSwMinSupportedLvl.setHorizontalAlignment(SwingConstants.TRAILING);

		cbbxSWMinSupportedLvl = new JComboBox<>();
		final GridBagConstraints gbcCbbxSWMinSupportedLvl = new GridBagConstraints();
		gbcCbbxSWMinSupportedLvl.fill = GridBagConstraints.HORIZONTAL;
		gbcCbbxSWMinSupportedLvl.insets = new Insets(0, 0, 5, 5);
		gbcCbbxSWMinSupportedLvl.gridx = 3;
		gbcCbbxSWMinSupportedLvl.gridy = 7;
		panel.add(cbbxSWMinSupportedLvl, gbcCbbxSWMinSupportedLvl);
		cbbxSWMinSupportedLvl.addItemListener(e -> cbbxSWMinSupportedLvlValueChanged(session, e));
		cbbxSWMinSupportedLvl.setModel(new DefaultComboBoxModel<>(Supported.values()));
		cbbxSWMinSupportedLvl.setSelectedIndex(0);

		cbbxYearMin = new JComboBox<>();
		cbbxYearMin.addItemListener(e -> cbbxYearMinValueChanged(session, e));
		final GridBagConstraints gbcCbbxYearMin = new GridBagConstraints();
		gbcCbbxYearMin.fill = GridBagConstraints.HORIZONTAL;
		gbcCbbxYearMin.insets = new Insets(0, 0, 5, 5);
		gbcCbbxYearMin.gridx = 1;
		gbcCbbxYearMin.gridy = 8;
		panel.add(cbbxYearMin, gbcCbbxYearMin);

		JLabel lblYear = new JLabel(Messages.getString("MainFrame.lblYear.text")); //$NON-NLS-1$
		final GridBagConstraints gbcLblYear = new GridBagConstraints();
		gbcLblYear.insets = new Insets(0, 0, 5, 5);
		gbcLblYear.gridx = 2;
		gbcLblYear.gridy = 8;
		panel.add(lblYear, gbcLblYear);
		lblYear.setHorizontalAlignment(SwingConstants.CENTER);

		cbbxYearMax = new JComboBox<>();
		cbbxYearMax.addItemListener(e -> cbbxYearMaxValueChanged(session, e));
		final GridBagConstraints gbcCbbxYearMax = new GridBagConstraints();
		gbcCbbxYearMax.fill = GridBagConstraints.HORIZONTAL;
		gbcCbbxYearMax.insets = new Insets(0, 0, 5, 5);
		gbcCbbxYearMax.gridx = 3;
		gbcCbbxYearMax.gridy = 8;
		panel.add(cbbxYearMax, gbcCbbxYearMax);
		cbbxDriverStatus.addItemListener(e -> cbbxDriverStatusValueChanged(session, e));
		chckbxIncludeDisks.addItemListener(e -> chckbxIncludeDisksStateChanged(session, e));
		chckbxIncludeClones.addItemListener(e -> chckbxIncludeClonesStateChanged(session, e));
		chckbxIncludeSamples.addItemListener(e -> chckbxIncludeSamplesStateChanged(session, e));
	}

	/**
	 * @param session
	 * @param e
	 */
	private void chckbxIncludeSamplesStateChanged(final Session session, ItemEvent e)
	{
		session.getCurrProfile().setProperty(SettingsEnum.filter_InclSamples, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		if (MainFrame.getProfileViewer() != null)
			MainFrame.getProfileViewer().reset(session.getCurrProfile());
	}

	/**
	 * @param session
	 * @param e
	 */
	private void chckbxIncludeClonesStateChanged(final Session session, ItemEvent e)
	{
		session.getCurrProfile().setProperty(SettingsEnum.filter_InclClones, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		if (MainFrame.getProfileViewer() != null)
			MainFrame.getProfileViewer().reset(session.getCurrProfile());
	}

	/**
	 * @param session
	 * @param e
	 */
	private void chckbxIncludeDisksStateChanged(final Session session, ItemEvent e)
	{
		session.getCurrProfile().setProperty(SettingsEnum.filter_InclDisks, e.getStateChange() == ItemEvent.SELECTED); //$NON-NLS-1$
		if (MainFrame.getProfileViewer() != null)
			MainFrame.getProfileViewer().reset(session.getCurrProfile());
	}

	/**
	 * @param session
	 * @param e
	 */
	private void cbbxDriverStatusValueChanged(final Session session, ItemEvent e)
	{
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			session.getCurrProfile().setProperty(SettingsEnum.filter_DriverStatus, e.getItem().toString()); //$NON-NLS-1$
			if (MainFrame.getProfileViewer() != null)
				MainFrame.getProfileViewer().reset(session.getCurrProfile());
		}
	}

	/**
	 * @param session
	 * @param e
	 */
	private void cbbxYearMaxValueChanged(final Session session, ItemEvent e)
	{
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			session.getCurrProfile().setProperty(SettingsEnum.filter_YearMax, e.getItem().toString()); //$NON-NLS-1$
			if (MainFrame.getProfileViewer() != null)
				MainFrame.getProfileViewer().reset(session.getCurrProfile());
		}
	}

	/**
	 * @param session
	 * @param e
	 */
	private void cbbxYearMinValueChanged(final Session session, ItemEvent e)
	{
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			session.getCurrProfile().setProperty(SettingsEnum.filter_YearMin, e.getItem().toString()); //$NON-NLS-1$
			if (MainFrame.getProfileViewer() != null)
				MainFrame.getProfileViewer().reset(session.getCurrProfile());
		}
	}

	/**
	 * @param session
	 * @param e
	 */
	private void cbbxSWMinSupportedLvlValueChanged(final Session session, ItemEvent e)
	{
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			session.getCurrProfile().setProperty(SettingsEnum.filter_MinSoftwareSupportedLevel, e.getItem().toString()); //$NON-NLS-1$
			if (MainFrame.getProfileViewer() != null)
				MainFrame.getProfileViewer().reset(session.getCurrProfile());
		}
	}

	/**
	 * @param session
	 * @param e
	 */
	private void cbbxFilterDisplayOrientationValueChanged(final Session session, ItemEvent e)
	{
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			session.getCurrProfile().setProperty(SettingsEnum.filter_DisplayOrientation, e.getItem().toString()); //$NON-NLS-1$
			if (MainFrame.getProfileViewer() != null)
				MainFrame.getProfileViewer().reset(session.getCurrProfile());
		}
	}

	/**
	 * @param session
	 * @param e
	 */
	private void cbbxFilterCabinetTypeValueChanged(final Session session, ItemEvent e)
	{
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			session.getCurrProfile().setProperty(SettingsEnum.filter_CabinetType, e.getItem().toString()); //$NON-NLS-1$
			if (MainFrame.getProfileViewer() != null)
				MainFrame.getProfileViewer().reset(session.getCurrProfile());
		}
	}

	/**
	 * @param session
	 * @param e
	 */
	private void checkBoxListSystemsValueChanged(final Session session, ListSelectionEvent e)
	{
		if (!e.getValueIsAdjusting() && e.getFirstIndex() != -1)
		{
			for (int index = e.getFirstIndex(); index <= e.getLastIndex(); index++)
				checkBoxListSystems.getModel().getElementAt(index).setSelected(session.getCurrProfile(), checkBoxListSystems.isSelectedIndex(index));
			if (MainFrame.getProfileViewer() != null)
				MainFrame.getProfileViewer().reset(session.getCurrProfile());
		}
	}

	/**
	 * @param session
	 * @return
	 */
	private JCheckBoxList<Systm>.CellRenderer getCheckBoxListSystemsCellRenderer(final Session session)
	{
		return checkBoxListSystems.new CellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(final JList<? extends Systm> list, final Systm value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				final JCheckBox checkbox = (JCheckBox) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				checkbox.setSelected(value.isSelected(session.getCurrProfile()));
				return checkbox;
			}
		};
	}

	public void initProfileSettings(@SuppressWarnings("exports") final Session session)
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
