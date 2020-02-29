package jrm.ui.profile.report;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.EnumSet;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeSelectionModel;

import jrm.locale.Messages;
import jrm.profile.report.FilterOptions;
import jrm.profile.report.Report;
import jrm.ui.profile.report.ReportNode.SubjectNode;
import jrm.ui.profile.report.ReportNode.SubjectNode.NoteNode;
import lombok.val;

@SuppressWarnings("serial")
public class ReportView extends JScrollPane
{
	public ReportView(Report report) {
		final JTree tree = new JTree();
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		TreeSelectionModel selmodel = new DefaultTreeSelectionModel();
		selmodel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setSelectionModel(selmodel);
		tree.setModel(new ReportTreeModel(report.getHandler()));
		tree.setCellRenderer(new ReportTreeCellRenderer());
		this.setViewportView(tree);

		final JPopupMenu popupMenu = new JPopupMenu();
		ReportView.addPopup(tree, popupMenu);

		final JMenuItem mntmOpenAllNodes = new JMenuItem(Messages.getString("ReportFrame.mntmOpenAllNodes.text")); //$NON-NLS-1$
		mntmOpenAllNodes.setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resicons/folder_open.png"))); //$NON-NLS-1$
		mntmOpenAllNodes.addActionListener(e -> {
			tree.invalidate();
			int j = tree.getRowCount();
			int i = 0;
			while(i < j)
			{
				tree.expandRow(i);
				i += 1;
				j = tree.getRowCount();
			}
			tree.validate();
		});
		popupMenu.add(mntmOpenAllNodes);

		final JCheckBoxMenuItem chckbxmntmShowOkEntries = new JCheckBoxMenuItem(Messages.getString("ReportFrame.chckbxmntmShowOkEntries.text")); //$NON-NLS-1$
		chckbxmntmShowOkEntries.setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resicons/folder_closed_green.png"))); //$NON-NLS-1$
		chckbxmntmShowOkEntries.addItemListener(e -> {
			final EnumSet<FilterOptions> options = report.getHandler().getFilterOptions();
			if(e.getStateChange() == ItemEvent.SELECTED)
				options.add(FilterOptions.SHOWOK);
			else
				options.remove(FilterOptions.SHOWOK);
			report.getHandler().filter(options.toArray(new FilterOptions[0]));
		});

		final JMenuItem mntmCloseAllNodes = new JMenuItem(Messages.getString("ReportFrame.mntmCloseAllNodes.text")); //$NON-NLS-1$
		mntmCloseAllNodes.addActionListener(e -> {
			tree.invalidate();
			int j = tree.getRowCount();
			int i = 0;
			while(i < j)
			{
				tree.collapseRow(i);
				i += 1;
				j = tree.getRowCount();
			}
			tree.validate();
		});
		mntmCloseAllNodes.setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resicons/folder_closed.png"))); //$NON-NLS-1$
		popupMenu.add(mntmCloseAllNodes);
		popupMenu.add(chckbxmntmShowOkEntries);

		final JCheckBoxMenuItem chckbxmntmHideFullyMissing = new JCheckBoxMenuItem(Messages.getString("ReportFrame.chckbxmntmHideFullyMissing.text")); //$NON-NLS-1$
		chckbxmntmHideFullyMissing.setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resicons/folder_closed_red.png"))); //$NON-NLS-1$
		chckbxmntmHideFullyMissing.addItemListener(e -> {
			final EnumSet<FilterOptions> options = report.getHandler().getFilterOptions();
			if(e.getStateChange() == ItemEvent.SELECTED)
				options.add(FilterOptions.HIDEMISSING);
			else
				options.remove(FilterOptions.HIDEMISSING);
			report.getHandler().filter(options.toArray(new FilterOptions[0]));
		});
		popupMenu.add(chckbxmntmHideFullyMissing);
		
		popupMenu.addSeparator();
		
		JMenuItem mntmDetail = new JMenuItem(Messages.getString("ReportView.mntmDetail.text")); //$NON-NLS-1$
		mntmDetail.addActionListener(e->{
			val path = tree.getSelectionPath();
			if(path!=null)
			{
				Object node = path.getLastPathComponent();
				if(node instanceof NoteNode)
				{
					val msg = ((NoteNode)node).getNote().getDetail();
					JOptionPane.showMessageDialog(this, new JTextArea(msg), "Details", JOptionPane.INFORMATION_MESSAGE);
				}
				else if(node instanceof SubjectNode)
				{
					val subjectnode = (SubjectNode)node;
					System.out.println(subjectnode.getSubject().getClass().getSimpleName());
				}
			}
		});
		mntmDetail.setEnabled(false);
		popupMenu.add(mntmDetail);
		
		JMenuItem mntmCopyCRC = new JMenuItem("Copy CRC");
		mntmCopyCRC.setEnabled(false);
		mntmCopyCRC.addActionListener(e->{
			val path = tree.getSelectionPath();
			if(path!=null)
			{
				Object node = path.getLastPathComponent();
				if(node instanceof NoteNode)
				{
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(((NoteNode)node).getNote().getCrc()), null);
				}
			}
		});
		popupMenu.add(mntmCopyCRC);
		
		JMenuItem mntmCopyName = new JMenuItem("Copy Name");
		mntmCopyName.setEnabled(false);
		mntmCopyName.addActionListener(e->{
			val path = tree.getSelectionPath();
			if(path!=null)
			{
				Object node = path.getLastPathComponent();
				if(node instanceof NoteNode)
				{
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(((NoteNode)node).getNote().getName()), null);
				}
			}
		});
		popupMenu.add(mntmCopyName);
		
		JMenuItem mntmSearchWeb = new JMenuItem("Search on the Web");
		mntmSearchWeb.setEnabled(false);
		mntmSearchWeb.addActionListener(e->{
			val path = tree.getSelectionPath();
			if(path!=null)
			{
				Object node = path.getLastPathComponent();
				if(node instanceof NoteNode)
				{
					String name = ((NoteNode)node).getNote().getName();
					String crc = ((NoteNode)node).getNote().getCrc();
					if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
					{
						try
						{
							Desktop.getDesktop().browse(new URI("https://www.google.com/search?q=" + URLEncoder.encode('"' + name + '"', "UTF-8") + '+' + crc));
						}
						catch (IOException | URISyntaxException e1)
						{
							e1.printStackTrace();
						}
					}
				}
			}
		});
		popupMenu.add(mntmSearchWeb);

		tree.addTreeSelectionListener(e->{
			val path  = e.getNewLeadSelectionPath();
			if(path!=null)
			{
				val node = path.getLastPathComponent();
				mntmDetail.setEnabled(node instanceof NoteNode);
				mntmCopyCRC.setEnabled(node instanceof NoteNode);
				mntmCopyName.setEnabled(node instanceof NoteNode);
				mntmSearchWeb.setEnabled(node instanceof NoteNode);
			}
			else
			{
				mntmDetail.setEnabled(false);
				mntmCopyCRC.setEnabled(false);
				mntmCopyName.setEnabled(false);
				mntmSearchWeb.setEnabled(false);
			}
		});
	}

	/**
	 * Adds the popup.
	 *
	 * @param component the component
	 * @param popup the popup
	 */
	private static void addPopup(final Component component, final JPopupMenu popup)
	{
		component.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(final MouseEvent e)
			{
				if(e.isPopupTrigger())
				{
					showMenu(e);
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e)
			{
				if(e.isPopupTrigger())
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
