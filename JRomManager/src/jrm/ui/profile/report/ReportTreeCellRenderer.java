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

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import jrm.misc.HTMLRenderer;
import jrm.profile.report.*;

// TODO: Auto-generated Javadoc
/**
 * The Class ReportTreeCellRenderer.
 *
 * @author optyfr
 */
@SuppressWarnings("serial")
public class ReportTreeCellRenderer extends DefaultTreeCellRenderer
{

	/**
	 * Instantiates a new report tree cell renderer.
	 */
	public ReportTreeCellRenderer()
	{
		super();
	}

	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus)
	{
		try
		{
			if(value instanceof Subject)
				super.getTreeCellRendererComponent(tree, ((Subject)value).getHTML(), sel, expanded, leaf, row, hasFocus);
			else if(value instanceof Note)
				super.getTreeCellRendererComponent(tree, ((Note)value).getHTML(), sel, expanded, leaf, row, hasFocus);
			else
				super.getTreeCellRendererComponent(tree, ((HTMLRenderer)value).getHTML(), sel, expanded, leaf, row, hasFocus);
			if(value instanceof RomSuspiciousCRC)
				setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/information.png"))); //$NON-NLS-1$
			else if(value instanceof ContainerUnknown)
				setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/error.png"))); //$NON-NLS-1$
			else if(value instanceof ContainerUnneeded)
				setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/error.png"))); //$NON-NLS-1$
			else if(value instanceof ContainerTZip)
				setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/compress.png"))); //$NON-NLS-1$
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
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return this;
	}
}
