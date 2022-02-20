package jrm.aui.progress;

import jrm.misc.HTMLRenderer;
import net.sf.sevenzipjbinding.IProgress;
import net.sf.sevenzipjbinding.SevenZipException;

public final class ProgressNarchiveCallBack implements IProgress, HTMLRenderer
{
	ProgressHandler ph;
	long total;

	public ProgressNarchiveCallBack(ProgressHandler ph)
	{
		this.ph = ph;
	}

	@Override
	public void setTotal(long total) throws SevenZipException
	{
		this.total = total;

	}

	@Override
	public void setCompleted(long complete) throws SevenZipException
	{
		if(!isPlain())
			ph.setProgress(null, null, null, progress(200, (int)complete, (int)total, null)); //$NON-NLS-1$
	}

}
