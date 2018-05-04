package jrm.profile.fix.actions;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.apache.commons.text.StringEscapeUtils;

import JTrrntzip.DummyLogCallback;
import JTrrntzip.SimpleTorrentZipOptions;
import JTrrntzip.TorrentZip;
import JTrrntzip.TrrntZipStatus;
import jrm.misc.FindCmd;
import jrm.misc.Settings;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.ui.ProgressHandler;

public class TZipContainer extends ContainerAction
{

	public TZipContainer(Container container, FormatOptions format)
	{
		super(container, format);
	}

	static File tzip_cmd = new File(Settings.getProperty("tzip_cmd", FindCmd.findTZip())); //$NON-NLS-1$

	@Override
	public boolean doAction(ProgressHandler handler)
	{
		handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4("Fixing %s [%s]"), toBlue(container.m.getFullName(container.file.getName())), toPurple(container.m.description))))); //$NON-NLS-1$
		if(container.getType() == Container.Type.ZIP)
		{
			if(format == FormatOptions.TZIP && tzip_cmd.exists())
			{
				try
				{
					if(container.file.exists())
					{
						EnumSet<TrrntZipStatus> status = new TorrentZip(new DummyLogCallback(), new SimpleTorrentZipOptions()).Process(container.file);
						if(!status.contains(TrrntZipStatus.ValidTrrntzip))
							System.out.format("%-64s => %s\n", container.file, status.toString());
					}
					return true;
				}
				catch(/*InterruptedException |*/ IOException e)
				{
					System.err.println(container.file);
					e.printStackTrace();
				}
			}
		}
		return false;
	}

}
