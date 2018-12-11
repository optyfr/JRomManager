/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.ui.profile.report;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.SerializationUtils;

import jrm.locale.Messages;
import jrm.profile.report.FilterOptions;
import jrm.security.Session;
import jrm.ui.progress.StatusHandler;

// TODO: Auto-generated Javadoc
/**
 * The Class ReportFrame.
 *
 * @author optyfr
 */
@SuppressWarnings("serial")
public class ReportFrame extends JDialog implements StatusHandler
{
	
	/** The lbl status. */
	private final JLabel lblStatus = new JLabel(""); //$NON-NLS-1$

	/**
	 * Instantiates a new report frame.
	 *
	 * @param owner the owner
	 * @throws HeadlessException the headless exception
	 */
	public ReportFrame(final Session session, final Window owner) throws HeadlessException
	{
		super(); //$NON-NLS-1$
		setTitle(Messages.getString("ReportFrame.Title")); //$NON-NLS-1$
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				session.getUser().settings.setProperty("ReportFrame.Bounds", Hex.encodeHexString(SerializationUtils.serialize(getBounds()))); //$NON-NLS-1$
			}
			@Override
			public void windowOpened(WindowEvent e)
			{
				session.report.getModel().filter(session.report.getModel().getFilterOptions().toArray(new FilterOptions[0]));
			}
		});
		setTitle(Messages.getString("ReportFrame.Title")); //$NON-NLS-1$
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(400, 300));
		setIconImage(Toolkit.getDefaultToolkit().getImage(ReportFrame.class.getResource("/jrm/resources/rom.png"))); //$NON-NLS-1$
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 784, 0 };
		gridBagLayout.rowHeights = new int[] { 280, 24, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		final JScrollPane scrollPane = new ReportView(session.report);
		final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);

		session.report.setStatusHandler(this);

		
		final GridBagConstraints gbc_lblStatus = new GridBagConstraints();
		gbc_lblStatus.ipadx = 2;
		gbc_lblStatus.insets = new Insets(2, 2, 2, 2);
		gbc_lblStatus.fill = GridBagConstraints.BOTH;
		gbc_lblStatus.gridx = 0;
		gbc_lblStatus.gridy = 1;
		lblStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		getContentPane().add(lblStatus, gbc_lblStatus);

		pack();


		try
		{
			setBounds(SerializationUtils.deserialize(Hex.decodeHex(session.getUser().settings.getProperty("ReportFrame.Bounds", Hex.encodeHexString(SerializationUtils.serialize(new Rectangle(10,10,800,600))))))); //$NON-NLS-1$
		}
		catch(final DecoderException e1)
		{
			e1.printStackTrace();
		}
	}

	@Override
	public void setStatus(final String text)
	{
		lblStatus.setText(text);
	}
}
