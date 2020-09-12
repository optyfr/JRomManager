package jrm.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jrm.locale.Messages;
import jrm.misc.SettingsEnum;
import jrm.security.Session;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("serial")
public class SettingsGenPanel extends JPanel
{
	/** The cb log level. */
	private JComboBox<ThreadCnt> cbThreading;

	/** The scheduler. */
	final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@RequiredArgsConstructor
	private static class ThreadCnt
	{
		final @Getter int cnt;
		final @Getter String name;
		
		public ThreadCnt(int cnt)
		{
			this.cnt = cnt;
			this.name = null;
		}
		
		@Override
		public String toString()
		{
			return name!=null?name:Integer.toString(cnt);
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if(obj == null)
				return false;
			if(obj instanceof ThreadCnt)
				return this.cnt == ((ThreadCnt) obj).cnt;
			return super.equals(obj);
		}
		
		static ThreadCnt[] build()
		{
			ArrayList<ThreadCnt> list = new ArrayList<>();
			list.add(new ThreadCnt(-1, "Adaptive"));
			list.add(new ThreadCnt(0, "Max available"));
			for(int i = 1; i <= Runtime.getRuntime().availableProcessors(); i++)
				list.add(new ThreadCnt(i));
			return list.toArray(ThreadCnt[]::new);
		}
	}
	
	/**
	 * Create the panel.
	 */
	public SettingsGenPanel(final Session session)
	{
		final GridBagLayout gbl_debug = new GridBagLayout();
		gbl_debug.columnWidths = new int[] { 50, 0, 10, 0 };
		gbl_debug.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_debug.columnWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_debug.rowWeights = new double[] { 1.0, 0.0, 1.0, Double.MIN_VALUE };
		this.setLayout(gbl_debug);

		JLabel lblThreading = new JLabel(Messages.getString("SettingsGenPanel.lblThreading.text")); //$NON-NLS-1$
		final GridBagConstraints gbc_lblThreading = new GridBagConstraints();
		gbc_lblThreading.anchor = GridBagConstraints.EAST;
		gbc_lblThreading.fill = GridBagConstraints.VERTICAL;
		gbc_lblThreading.insets = new Insets(0, 0, 5, 5);
		gbc_lblThreading.gridx = 0;
		gbc_lblThreading.gridy = 1;
		this.add(lblThreading, gbc_lblThreading);

		
		
		cbThreading = new JComboBox<>(new DefaultComboBoxModel<>(ThreadCnt.build()));
		final GridBagConstraints gbc_cbThreading = new GridBagConstraints();
		gbc_cbThreading.insets = new Insets(0, 0, 5, 5);
		gbc_cbThreading.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbThreading.gridx = 1;
		gbc_cbThreading.gridy = 1;
		this.add(cbThreading, gbc_cbThreading);
		cbThreading.addActionListener(arg0 -> {
			session.getUser().getSettings().setProperty(SettingsEnum.thread_count, ((ThreadCnt)cbThreading.getSelectedItem()).getCnt());
		}); //$NON-NLS-1$
		cbThreading.setSelectedItem(new ThreadCnt(session.getUser().getSettings().getProperty(SettingsEnum.thread_count, -1)));
	}

}
