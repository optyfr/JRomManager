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
	final transient ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
		public int hashCode()
		{
			return super.hashCode();
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
			for(var i = 1; i <= Runtime.getRuntime().availableProcessors(); i++)
				list.add(new ThreadCnt(i));
			return list.toArray(ThreadCnt[]::new);
		}
	}
	
	/**
	 * Create the panel.
	 */
	public SettingsGenPanel(final Session session)
	{
		final var gblDebug = new GridBagLayout();
		gblDebug.columnWidths = new int[] { 50, 0, 10, 0 };
		gblDebug.rowHeights = new int[] { 0, 0, 0, 0 };
		gblDebug.columnWeights = new double[] { 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gblDebug.rowWeights = new double[] { 1.0, 0.0, 1.0, Double.MIN_VALUE };
		this.setLayout(gblDebug);

		final var lblThreading = new JLabel(Messages.getString("SettingsGenPanel.lblThreading.text")); //$NON-NLS-1$
		final var gbcLblThreading = new GridBagConstraints();
		gbcLblThreading.anchor = GridBagConstraints.EAST;
		gbcLblThreading.fill = GridBagConstraints.VERTICAL;
		gbcLblThreading.insets = new Insets(0, 0, 5, 5);
		gbcLblThreading.gridx = 0;
		gbcLblThreading.gridy = 1;
		this.add(lblThreading, gbcLblThreading);

		
		
		cbThreading = new JComboBox<>(new DefaultComboBoxModel<>(ThreadCnt.build()));
		final var gbcCBThreading = new GridBagConstraints();
		gbcCBThreading.insets = new Insets(0, 0, 5, 5);
		gbcCBThreading.fill = GridBagConstraints.HORIZONTAL;
		gbcCBThreading.gridx = 1;
		gbcCBThreading.gridy = 1;
		this.add(cbThreading, gbcCBThreading);
		cbThreading.addActionListener(arg0 -> 
			session.getUser().getSettings().setProperty(SettingsEnum.thread_count, ((ThreadCnt)cbThreading.getSelectedItem()).getCnt())
		); //$NON-NLS-1$
		cbThreading.setSelectedItem(new ThreadCnt(session.getUser().getSettings().getProperty(SettingsEnum.thread_count, -1)));
	}

}
