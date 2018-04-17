package jrm.profiler.fix.actions;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profiler.data.Container;
import jrm.profiler.scan.options.FormatOptions;
import jrm.ui.ProgressHandler;

public class DeleteContainer extends ContainerAction
{

	public DeleteContainer(Container container, FormatOptions format)
	{
		super(container, format);
	}

	public static DeleteContainer getInstance(DeleteContainer action, Container container, FormatOptions format)
	{
		if(action == null)
			action = new DeleteContainer(container, format);
		return action;
	}

	@Override
	public boolean doAction(ProgressHandler handler)
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
			catch(IOException e)
			{
				System.err.println("failed to delete " + container.file.getName());
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
