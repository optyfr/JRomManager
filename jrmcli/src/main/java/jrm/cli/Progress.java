package jrm.cli;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jrm.aui.progress.ProgressHandler;

public class Progress implements ProgressHandler
{
	private List<String> errors = new ArrayList<>();
	private Integer max = null;
	private boolean quiet = false;
	
	@Override
	public void setInfos(int threadCnt, Boolean multipleSubInfos)
	{
		// not implemented
	}

	@Override
	public void clearInfos()
	{
		// not implemented
	}

	@Override
	public void setProgress(String msg, Integer val, Integer max, String submsg)
	{
		if (max != null)
			this.max = max;
		if (msg != null && !msg.isEmpty() && !quiet)
		{
			if (val != null && val > 0)
				System.out.format("%s (%d/%d)%n", msg, val, this.max);	// NOSONAR
			else
				System.out.format("%s%n", msg);	// NOSONAR
		}
	}

	@Override
	public void setProgress2(String msg, Integer val, Integer max)
	{
		// not implemented
	}

	@Override
	public void setProgress3(String msg, Integer val, Integer max)
	{
		// not implemented
	}

	@Override
	public int getCurrent()
	{
		return 0;
	}

	@Override
	public int getCurrent2()
	{
		return 0;
	}

	@Override
	public int getCurrent3()
	{
		return 0;
	}

	@Override
	public boolean isCancel()
	{
		return false;
	}

	@Override
	public void doCancel()
	{
		// not implemented
	}

	@Override
	public void canCancel(boolean canCancel)
	{
		// not implemented
	}

	@Override
	public boolean canCancel()
	{
		return false;
	}

	@Override
	public InputStream getInputStream(InputStream in, Integer len)
	{
		return in;
	}

	@Override
	public void close()
	{
		errors.forEach(System.err::println);	// NOSONAR
	}
	
	public void quiet(boolean quiet)
	{
		this.quiet  = quiet;
	}
	
	public void quiet()
	{
		this.quiet = !this.quiet;
	}

	@Override
	public void addError(String error)
	{
		errors.add(error);
	}

	@Override
	public void setOptions(Option first, Option... rest)
	{
		// Do nothing
	}
	
}
