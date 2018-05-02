package jrm.profile.fix.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.StringEscapeUtils;

import JTrrntzip.LogCallback;
import JTrrntzip.TorrentZip;
import JTrrntzip.TorrentZipOptions;
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
						new TorrentZip(new LogCallback()
						{
							@Override
							public void StatusCallBack(int percent)
							{
							}
							
							@Override
							public boolean isVerboseLogging()
							{
								return false;
							}
							
							@Override
							public void StatusLogCallBack(String log)
							{
							}
						}, new TorrentZipOptions()
						{
							
							@Override
							public boolean isForceRezip()
							{
								return false;
							}
							
							@Override
							public boolean isCheckOnly()
							{
								return false;
							}
						}).Process(container.file);
/*						
						
						ProcessBuilder pb = new ProcessBuilder(tzip_cmd.getPath(), container.file.getAbsolutePath()).directory(tzip_cmd.getParentFile()).redirectErrorStream(true);
						Process process = pb.start();
						try(BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream())))
						{
							@SuppressWarnings("unused")
							String line;
							while((line = in.readLine()) != null)
							{
								// System.out.println(line);
							}
							for(File log : tzip_cmd.getParentFile().getParentFile().listFiles((dir, name) -> FilenameUtils.getExtension(name).equals("log")))
								log.delete();
						}
						return process.waitFor() == 0;*/
					}
					return true;
				}
				catch(/*InterruptedException |*/ IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return false;
	}

}
