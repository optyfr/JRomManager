package jrm.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import jrm.misc.HTMLRenderer;
import jrm.profile.report.*;

@SuppressWarnings("serial")
public class ReportTreeCellRenderer extends DefaultTreeCellRenderer
{

	public ReportTreeCellRenderer()
	{
		super();
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, ((HTMLRenderer)value).getHTML(), sel, expanded, leaf, row, hasFocus);
		if(value instanceof RomSuspiciousCRC)
			setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/information.png"))); //$NON-NLS-1$
		else if(value instanceof ContainerUnknown)
			setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/error.png"))); //$NON-NLS-1$
		else if(value instanceof ContainerUnneeded)
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
				switch(((SubjectSet) value).getStatus())
				{
					case FOUND:
						if(((SubjectSet) value).hasNotes())
						{
							if(((SubjectSet) value).isFixable())
								icon += "_purple"; //$NON-NLS-1$
							else
								icon += "_orange"; //$NON-NLS-1$
						}
						else
							icon += "_green"; //$NON-NLS-1$
						break;
					case CREATE:
					case CREATEFULL:
						if(((SubjectSet) value).isFixable())
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
				switch(((SubjectSet) value).getStatus())
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
}
