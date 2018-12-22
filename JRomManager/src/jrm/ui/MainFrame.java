/*
 * Copyright (C) 2018 optyfr
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ResourceBundle;

import javax.swing.*;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.SerializationUtils;

import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.security.Session;
import jrm.ui.batch.BatchPanel;
import jrm.ui.profile.ProfileViewer;
import jrm.ui.profile.report.ReportFrame;

// TODO: Auto-generated Javadoc
/**
 * The Class MainFrame.
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame
{

	/** The profile viewer. */
	public static ProfileViewer profile_viewer = null;

	/** The report frame. */
	public static ReportFrame report_frame = null;

	/** The main pane. */
	private JTabbedPane mainPane;

	private SettingsPanel settingsPanel;

	private ProfilePanel profilesPanel;

	
	private Session session;
	/**
	 * Instantiates a new main frame.
	 */
	public MainFrame(Session session)
	{
		super();
		this.session = session;
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(final WindowEvent e)
			{
				session.getUser().settings.setProperty("MainFrame.Bounds", Hex.encodeHexString(SerializationUtils.serialize(getBounds()))); //$NON-NLS-1$
			}
		});
		try
		{
			UIManager.setLookAndFeel(session.getUser().settings.getProperty("LookAndFeel", UIManager.getSystemLookAndFeelClassName()/* UIManager.getCrossPlatformLookAndFeelClassName() */)); //$NON-NLS-1$
			final File workdir = session.getUser().settings.getWorkPath().toFile(); // $NON-NLS-1$
			final File xmldir = new File(workdir, "xmlfiles"); //$NON-NLS-1$
			xmldir.mkdir();
			ResourceBundle.getBundle("jrm.resources.Messages"); //$NON-NLS-1$
		}
		catch (final Exception e)
		{
			JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			Log.err(e.getMessage(),e);
		}
		build();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (session.curr_profile != null)
				session.curr_profile.saveSettings();
			session.getUser().settings.saveSettings();
		}));
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	private String getVersion()
	{
		String version = ""; //$NON-NLS-1$
		final Package pkg = this.getClass().getPackage();
		if (pkg.getSpecificationVersion() != null)
			version += " " + pkg.getSpecificationVersion(); //$NON-NLS-1$
		if (pkg.getImplementationVersion() != null)
			version += " " + pkg.getImplementationVersion(); //$NON-NLS-1$
		return version;
	}

	/**
	 * Initialize Main GUI.
	 */
	private void build()
	{
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/jrm/resources/rom.png"))); //$NON-NLS-1$
		setTitle(Messages.getString("MainFrame.Title")
				+ getVersion()	// $hide$
		); //$NON-NLS-1$
		setBounds(50, 50, 1007, 601);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		mainPane = new JTabbedPane(SwingConstants.TOP);
		getContentPane().add(mainPane);

		MainFrame.report_frame = new ReportFrame(session, MainFrame.this);

		buildProfileTab();

		buildScannerTab();

		buildDir2DatTab();

		buildBatchToolsTab();

		buildSettingsTab();

		pack();

		try
		{
			setBounds(SerializationUtils.deserialize(Hex.decodeHex(session.getUser().settings.getProperty("MainFrame.Bounds", Hex.encodeHexString(SerializationUtils.serialize(new Rectangle(50, 50, 720, 300))))))); //$NON-NLS-1$
		}
		catch (final DecoderException e1)
		{
			Log.err(e1.getMessage(),e1);
		}

	}

	private void buildProfileTab()
	{
		profilesPanel = new ProfilePanel(session);
		mainPane.addTab(Messages.getString("MainFrame.Profiles"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/script.png")), profilesPanel, null); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void buildScannerTab()
	{
		ScannerPanel scannerPanel = new ScannerPanel(session);
		profilesPanel.setProfileLoader(scannerPanel);
		scannerPanel.setMainPane(mainPane);
		mainPane.addTab(Messages.getString("MainFrame.Scanner"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/drive_magnify.png")), scannerPanel, null); //$NON-NLS-1$ //$NON-NLS-2$
		mainPane.setEnabledAt(1, false);
	}

	private void buildDir2DatTab()
	{
		Dir2DatPanel dir2datPanel = new Dir2DatPanel(session);
		mainPane.addTab(Messages.getString("MainFrame.Dir2Dat"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/drive_go.png")), dir2datPanel, null); //$NON-NLS-1$ //$NON-NLS-2$

	}

	private void buildBatchToolsTab()
	{
		BatchPanel batchToolsPanel = new BatchPanel(session);
		mainPane.addTab(Messages.getString("MainFrame.BatchTools"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/application_osx_terminal.png")), batchToolsPanel, null); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void buildSettingsTab()
	{
		settingsPanel = new SettingsPanel(session);
		mainPane.addTab(Messages.getString("MainFrame.Settings"), new ImageIcon(MainFrame.class.getResource("/jrm/resources/icons/cog.png")), settingsPanel, null); //$NON-NLS-1$ //$NON-NLS-2$
	}


	/**
	 * Adds and show the popup menu.
	 *
	 * @param component
	 *            the component to add a popup menu
	 * @param popup
	 *            the popup menu to add
	 */
	public static void addPopup(final Component component, final JPopupMenu popup)
	{
		component.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			private void showMenu(final MouseEvent e)
			{
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

}
