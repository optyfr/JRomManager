package jrm.profile.fix.actions;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.ui.ProgressHandler;

/**
 * Delete a container (will all its entries)
 * @author optyfr
 *
 */
public class DeleteContainer extends ContainerAction
{

	/**
	 * constructor
	 * @param container to delete
	 * @param format format of the container
	 */
	public DeleteContainer(final Container container, final FormatOptions format)
	{
		super(container, format);
	}

	/**
	 * shortcut static method to get an instance of {@link DeleteContainer}
	 * @param action the potentially already existing {@link DeleteContainer} 
	 * @param container the container to backup
	 * @param format the format of the container
	 * @return a {@link DeleteContainer}
	 */
	public static DeleteContainer getInstance(DeleteContainer action, final Container container, final FormatOptions format)
	{
		if(action == null)
			action = new DeleteContainer(container, format);
		return action;
	}

	@Override
	public boolean doAction(final ProgressHandler handler)
	{
		handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("DeleteContainer.Deleting")), toBlue(container.file.getName()))))); //$NON-NLS-1$
		if(container.getType() == Container.Type.ZIP)
			return container.file.delete();
		else if(container.getType() == Container.Type.SEVENZIP)
			return container.file.delete();
		else if(container.getType() == Container.Type.DIR)
		{
			try
			{
				FileUtils.deleteDirectory(container.file);
				return true;
			}
			catch(final IOException e)
			{
				System.err.println("failed to delete " + container.file.getName()); //$NON-NLS-1$
				return false;
			}
		}
		else if(container.getType() == Container.Type.UNK)
			return container.file.delete();
		return false;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("DeleteContainer.Delete"), container); //$NON-NLS-1$
	}
}
