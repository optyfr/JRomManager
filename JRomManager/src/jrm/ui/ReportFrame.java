package jrm.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import jrm.profiler.report.ContainerUnknown;
import jrm.profiler.report.EntryAdd;
import jrm.profiler.report.EntryMissing;
import jrm.profiler.report.EntryMissingDuplicate;
import jrm.profiler.report.EntryOK;
import jrm.profiler.report.EntryUnneeded;
import jrm.profiler.report.EntryWrongHash;
import jrm.profiler.report.EntryWrongName;
import jrm.profiler.report.RomSuspiciousCRC;
import jrm.profiler.report.SubjectSet;
import jrm.profiler.scan.Scan;
import jrm.ui.ReportTreeModel.FilterOptions;

@SuppressWarnings("serial")
public class ReportFrame extends JDialog
{

	public ReportFrame(Window owner) throws HeadlessException
	{
		super(owner, "Report", ModalityType.MODELESS);
		setBounds(new Rectangle(100, 100, 0, 0));
		setVisible(true);
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(400, 300));
		setIconImage(Toolkit.getDefaultToolkit().getImage(ReportFrame.class.getResource("/jrm/resources/rom.png")));
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JTree tree = new JTree();
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
			{
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				if(value instanceof RomSuspiciousCRC)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/information.png")));
				else if(value instanceof ContainerUnknown)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/error.png")));
				else if(value instanceof EntryOK)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_green.png")));
				else if(value instanceof EntryAdd)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_blue.png")));
				else if(value instanceof EntryMissingDuplicate)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_purple.png")));
				else if(value instanceof EntryMissing)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_red.png")));
				else if(value instanceof EntryUnneeded)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_black.png")));
				else if(value instanceof EntryWrongHash)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_orange.png")));
				else if(value instanceof EntryWrongName)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_pink.png")));
				else if(!leaf)
				{
					String icon = "/jrm/resources/folder";
					if(expanded)
						icon += "_open";
					else
						icon += "_closed";
					if(value instanceof SubjectSet)
					{
						switch(((SubjectSet)value).getStatus())
						{
							case FOUND:
								if(((SubjectSet)value).hasNotes())
								{
									if(((SubjectSet)value).isFixable())
										icon += "_purple";
									else
										icon += "_orange";
								}
								else
									icon += "_green";
								break;
							case CREATE:
							case CREATEFULL:
								if(((SubjectSet)value).isFixable())
									icon += "_blue";
								else
									icon += "_orange";
								break;
							case MISSING:
								icon += "_red";
								break;
							case UNNEEDED:
								icon += "_gray";
								break;
							default:
								break;
						}
					}
					icon += ".png";
					setIcon(new ImageIcon(ReportFrame.class.getResource(icon)));
				}
				else
				{
					if(value instanceof SubjectSet)
					{
						switch(((SubjectSet)value).getStatus())
						{
							case FOUND:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_green.png")));
								break;
							default:
								break;
						}
					}
					
				}
				return this;
			}
		});
		tree.setModel(Scan.report.getModel());
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		scrollPane.setViewportView(tree);
		
		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(tree, popupMenu);
		
		JMenuItem mntmOpenAllNodes = new JMenuItem("Open all nodes");
		mntmOpenAllNodes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    int j = tree.getRowCount();
			    int i = 0;
			    while(i < j) {
			        tree.expandRow(i);
			        i += 1;
			        j = tree.getRowCount();
			    }
			}
		});
		popupMenu.add(mntmOpenAllNodes);
		
		JCheckBoxMenuItem chckbxmntmShowOkEntries = new JCheckBoxMenuItem("Show OK Entries");
		chckbxmntmShowOkEntries.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				EnumSet<FilterOptions> options = Scan.report.getModel().getFilterOptions();
				if(e.getStateChange()==ItemEvent.SELECTED)
					options.add(FilterOptions.SHOWOK);
				else
					options.remove(FilterOptions.SHOWOK);
				Scan.report.getModel().filter(options.toArray(new FilterOptions[0]));
			}
		});
		popupMenu.add(chckbxmntmShowOkEntries);
		
		JCheckBoxMenuItem chckbxmntmHideFullyMissing = new JCheckBoxMenuItem("Hide Fully Missing");
		chckbxmntmHideFullyMissing.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				EnumSet<FilterOptions> options = Scan.report.getModel().getFilterOptions();
				if(e.getStateChange()==ItemEvent.SELECTED)
					options.add(FilterOptions.HIDEMISSING);
				else
					options.remove(FilterOptions.HIDEMISSING);
				Scan.report.getModel().filter(options.toArray(new FilterOptions[0]));
			}
		});
		popupMenu.add(chckbxmntmHideFullyMissing);
		
		pack();
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
