package jrm.aui.progress;

import jrm.aui.status.StatusRendererFactory;
import jtrrntzip.LogCallback;

/**
 * The Class ProgressTZipCallBack.
 *
 * @author optyfr
 */
public final class ProgressTZipCallBack implements LogCallback, StatusRendererFactory
{
	
	/** The ph. */
	ProgressHandler ph;
	
	/**
	 * Instantiates a new progress T zip call back.
	 *
	 * @param ph the ph
	 */
	public ProgressTZipCallBack(ProgressHandler ph)
	{
		this.ph = ph;
	}
	
	@Override
	public void statusCallBack(int percent)
	{
		if(hasProgress())
			ph.setProgress(null, null, null, progress(200, percent, 100, null));
	}

	@Override
	public boolean isVerboseLogging()
	{
		return false;
	}

	@Override
	public void statusLogCallBack(String log)
	{
		// do nothing
	}
	
}