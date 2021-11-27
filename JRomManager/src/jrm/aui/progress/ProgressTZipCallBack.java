package jrm.aui.progress;

import jrm.misc.HTMLRenderer;
import jtrrntzip.LogCallback;

/**
 * The Class ProgressTZipCallBack.
 *
 * @author optyfr
 */
public final class ProgressTZipCallBack implements LogCallback, HTMLRenderer
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
		if(!isPlain())
			ph.setProgress(null, null, null, String.format("<html>"
				+ "<table cellpadding=2 cellspacing=0>"
				+ "<tr><td valign='middle'>%s</td></tr>"
				+ "</table>", progress(200, percent, 100))); //$NON-NLS-1$
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