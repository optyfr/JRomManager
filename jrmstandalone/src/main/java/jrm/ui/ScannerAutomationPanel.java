package jrm.ui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import jrm.locale.Messages;
import jrm.misc.ProfileSettings;
import jrm.misc.ProfileSettingsEnum;
import jrm.profile.scan.options.ScanAutomation;

@SuppressWarnings("serial")
public final class ScannerAutomationPanel extends JPanel
{
	@SuppressWarnings("exports")
	public transient ProfileSettings settings; 
	private JComboBox<ScanAutomation> comboBox;
	

	public ScannerAutomationPanel()
	{
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblOnScanAction = new JLabel(Messages.getString("ScannerAutomationPanel.OnScanAction")); //$NON-NLS-1$
		GridBagConstraints gbcLblOnScanAction = new GridBagConstraints();
		gbcLblOnScanAction.insets = new Insets(0, 0, 5, 5);
		gbcLblOnScanAction.anchor = GridBagConstraints.EAST;
		gbcLblOnScanAction.gridx = 1;
		gbcLblOnScanAction.gridy = 1;
		add(lblOnScanAction, gbcLblOnScanAction);
		
		comboBox = new JComboBox<>();
		comboBox.setModel(new DefaultComboBoxModel<>(ScanAutomation.values()));
		comboBox.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				setText(((ScanAutomation) value).getDesc());
				return this;
			}
		});
		comboBox.addActionListener(e -> settings.setProperty(ProfileSettingsEnum.automation_scan, comboBox.getSelectedItem().toString())); //$NON-NLS-1$
		GridBagConstraints gbcComboBox = new GridBagConstraints();
		gbcComboBox.insets = new Insets(0, 0, 5, 5);
		gbcComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbcComboBox.gridx = 2;
		gbcComboBox.gridy = 1;
		add(comboBox, gbcComboBox);
	}
	
	public void initProfileSettings(@SuppressWarnings("exports") final ProfileSettings settings)
	{
		this.settings = settings;
		comboBox.setSelectedItem(ScanAutomation.valueOf(settings.getProperty(ProfileSettingsEnum.automation_scan))); //$NON-NLS-1$
	}
}
