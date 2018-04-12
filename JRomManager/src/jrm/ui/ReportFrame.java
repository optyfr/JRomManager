package jrm.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.EnumSet;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;

import jrm.Messages;
import jrm.profiler.report.*;
import jrm.profiler.scan.Scan;
import java.util.ResourceBundle;

@SuppressWarnings("serial")
public class ReportFrame extends JDialog
{
	private static final ResourceBundle MSGS = ResourceBundle.getBundle("jrm.resources.Messages"); //$NON-NLS-1$

	public ReportFrame(Window owner) throws HeadlessException
	{
		super(owner, Messages.getString("ReportFrame.Title"), ModalityType.MODELESS); //$NON-NLS-1$
		setBounds(new Rectangle(100, 100, 0, 0));
		setVisible(true);
		setPreferredSize(new Dimension(800, 600));
		setMinimumSize(new Dimension(400, 300));
		setIconImage(Toolkit.getDefaultToolkit().getImage(ReportFrame.class.getResource("/jrm/resources/rom.png"))); //$NON-NLS-1$
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JTree tree = new JTree();
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
			{
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				if(value instanceof RomSuspiciousCRC)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/information.png"))); //$NON-NLS-1$
				else if(value instanceof ContainerUnknown)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/error.png"))); //$NON-NLS-1$
				else if(value instanceof EntryOK)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_green.png"))); //$NON-NLS-1$
				else if(value instanceof EntryAdd)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_blue.png"))); //$NON-NLS-1$
				else if(value instanceof EntryMissingDuplicate)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_purple.png"))); //$NON-NLS-1$
				else if(value instanceof EntryMissing)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_red.png"))); //$NON-NLS-1$
				else if(value instanceof EntryUnneeded)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_black.png"))); //$NON-NLS-1$
				else if(value instanceof EntryWrongHash)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_orange.png"))); //$NON-NLS-1$
				else if(value instanceof EntryWrongName)
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_pink.png"))); //$NON-NLS-1$
				else if(!leaf)
				{
					String icon = "/jrm/resources/folder"; //$NON-NLS-1$
					if(expanded)
						icon += "_open"; //$NON-NLS-1$
					else
						icon += "_closed"; //$NON-NLS-1$
					if(value instanceof SubjectSet)
					{
						switch(((SubjectSet)value).getStatus())
						{
							case FOUND:
								if(((SubjectSet)value).hasNotes())
								{
									if(((SubjectSet)value).isFixable())
										icon += "_purple"; //$NON-NLS-1$
									else
										icon += "_orange"; //$NON-NLS-1$
								}
								else
									icon += "_green"; //$NON-NLS-1$
								break;
							case CREATE:
							case CREATEFULL:
								if(((SubjectSet)value).isFixable())
									icon += "_blue"; //$NON-NLS-1$
								else
									icon += "_orange"; //$NON-NLS-1$
								break;
							case MISSING:
								icon += "_red"; //$NON-NLS-1$
								break;
							case UNNEEDED:
								icon += "_gray"; //$NON-NLS-1$
								break;
							default:
								break;
						}
					}
					icon += ".png"; //$NON-NLS-1$
					setIcon(new ImageIcon(ReportFrame.class.getResource(icon)));
				}
				else
				{
					if(value instanceof SubjectSet)
					{
						switch(((SubjectSet)value).getStatus())
						{
							case FOUND:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/bullet_green.png"))); //$NON-NLS-1$
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
		
		JMenuItem mntmOpenAllNodes = new JMenuItem(MSGS.getString("ReportFrame.mntmOpenAllNodes.text")); //$NON-NLS-1$
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
		
		JCheckBoxMenuItem chckbxmntmShowOkEntries = new JCheckBoxMenuItem(MSGS.getString("ReportFrame.chckbxmntmShowOkEntries.text")); //$NON-NLS-1$
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
		
		JCheckBoxMenuItem chckbxmntmHideFullyMissing = new JCheckBoxMenuItem(MSGS.getString("ReportFrame.chckbxmntmHideFullyMissing.text")); //$NON-NLS-1$
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
