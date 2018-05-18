package jrm.profile.fix.actions;

import java.io.IOException;
import java.util.EnumSet;

import org.apache.commons.text.StringEscapeUtils;

import JTrrntzip.DummyLogCallback;
import JTrrntzip.SimpleTorrentZipOptions;
import JTrrntzip.TorrentZip;
import JTrrntzip.TrrntZipStatus;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.ui.ProgressHandler;

public class TZipContainer extends ContainerAction
{

	public TZipContainer(final Container container, final FormatOptions format)
	{
		super(container, format);
	}

	@Override
	public boolean doAction(final ProgressHandler handler)
	{
		handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4("TorrentZipping %s [%s]"), toBlue(container.m.getFullName(container.file.getName())), toPurple(container.m.getDescription()))))); //$NON-NLS-1$
		if(container.getType() == Container.Type.ZIP)
		{
			if(format == FormatOptions.TZIP)
			{
				try
				{
					if(container.file.exists())
					{
						final EnumSet<TrrntZipStatus> status = new TorrentZip(new DummyLogCallback(), new SimpleTorrentZipOptions()).Process(container.file);
						if(!status.contains(TrrntZipStatus.ValidTrrntzip))
							System.out.format("%-64s => %s\n", container.file, status.toString()); //$NON-NLS-1$
					}
					return true;
				}
				catch(/*InterruptedException |*/ final IOException e)
				{
					System.err.println(container.file);
					e.printStackTrace();
				}
			}
		}
		return false;
	}

}
