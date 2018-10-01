package jrm.ui.progress;

import JTrrntzip.LogCallback;

/**
 * The Class ProgressTZipCallBack.
 *
 * @author optyfr
 */
public final class ProgressTZipCallBack implements LogCallback
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
	public void StatusCallBack(int percent)
	{
		ph.setProgress(null, null, null, String.format("<html><table cellpadding=2 cellspacing=0><tr><td valign='middle'><table cellpadding=0 cellspacing=0 style='width:%dpx;font-size:2px;border:1px solid gray'><tr><td style='width:%dpx;background:#ff00'><td></table><td>", 208, percent*2)); //$NON-NLS-1$
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
	
}