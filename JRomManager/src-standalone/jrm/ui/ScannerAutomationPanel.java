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
import jrm.profile.scan.options.ScanAutomation;

@SuppressWarnings("serial")
public final class ScannerAutomationPanel extends JPanel
{

	public ScannerAutomationPanel()
	{
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblOnScanAction = new JLabel(Messages.getString("ScannerAutomationPanel.OnScanAction")); //$NON-NLS-1$
		GridBagConstraints gbc_lblOnScanAction = new GridBagConstraints();
		gbc_lblOnScanAction.insets = new Insets(0, 0, 5, 5);
		gbc_lblOnScanAction.anchor = GridBagConstraints.EAST;
		gbc_lblOnScanAction.gridx = 1;
		gbc_lblOnScanAction.gridy = 1;
		add(lblOnScanAction, gbc_lblOnScanAction);
		
		comboBox = new JComboBox<ScanAutomation>();
		comboBox.setModel(new DefaultComboBoxModel<ScanAutomation>(ScanAutomation.values()));
		comboBox.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus)
			{
				setText(((ScanAutomation) value).getDesc());
				return this;
			}
		});
		comboBox.addActionListener(e -> settings.setProperty("automation.scan", comboBox.getSelectedItem().toString())); //$NON-NLS-1$
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 2;
		gbc_comboBox.gridy = 1;
		add(comboBox, gbc_comboBox);
	}
	
	public ProfileSettings settings; 
	private JComboBox<ScanAutomation> comboBox;
	
	public void initProfileSettings(final ProfileSettings settings)
	{
		this.settings = settings;
		comboBox.setSelectedItem(ScanAutomation.valueOf(settings.getProperty("automation.scan", ScanAutomation.SCAN.toString()))); //$NON-NLS-1$
	}
}
