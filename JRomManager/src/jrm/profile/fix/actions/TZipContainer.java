package jrm.profile.fix.actions;

import java.io.IOException;
import java.util.EnumSet;

import org.apache.commons.text.StringEscapeUtils;

import JTrrntzip.SimpleTorrentZipOptions;
import JTrrntzip.TorrentZip;
import JTrrntzip.TrrntZipStatus;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.ui.Progress;
import jrm.ui.ProgressHandler;

/**
 * The specialized container action for trrntzipping zip containers
 * @author optyfr
 *
 */
public class TZipContainer extends ContainerAction
{

	/**
	 * Constructor
	 * @param container the container to tzip
	 * @param format the desired format (should be always {@link FormatOptions#TZIP} otherwise nothing will happen)
	 */
	public TZipContainer(final Container container, final FormatOptions format)
	{
		super(container, format);
	}

	@Override
	public boolean doAction(final ProgressHandler handler)
	{
		if(container.getType() == Container.Type.ZIP)
		{
			if(format == FormatOptions.TZIP)
			{
				handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4("TorrentZipping %s [%s]"), toBlue(container.m.getFullName(container.file.getName())), toPurple(container.m.getDescription()))))); //$NON-NLS-1$
				try
				{
					if(container.file.exists())
					{
						final EnumSet<TrrntZipStatus> status = new TorrentZip(new Progress.ProgressTZipCallBack(handler), new SimpleTorrentZipOptions()).Process(container.file);
						if(!status.contains(TrrntZipStatus.ValidTrrntzip))
							System.out.format("%-64s => %s\n", container.file, status.toString()); //$NON-NLS-1$
					}
					handler.setProgress(""); //$NON-NLS-1$
					return true;
				}
				catch(/*InterruptedException |*/ final IOException e)
				{
					System.err.println(container.file);
					e.printStackTrace();
				}
				handler.setProgress(""); //$NON-NLS-1$
			}
		}
		return false;
	}

}
