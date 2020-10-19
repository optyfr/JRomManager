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
	
	public Progress()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setInfos(int threadCnt, Boolean multipleSubInfos)
	{
	}

	@Override
	public void clearInfos()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setProgress(String msg, Integer val, Integer max, String submsg)
	{
		if (max != null)
			this.max = max;
		if (msg != null && !msg.isEmpty() && !quiet)
		{
			if (val != null && val > 0)
				System.out.format("%s (%d/%d)\n", msg, val, this.max);
			else
				System.out.format("%s\n", msg);
		}
	}

	@Override
	public void setProgress2(String msg, Integer val, Integer max)
	{
	}

	@Override
	public void setProgress3(String msg, Integer val, Integer max)
	{
	}

	@Override
	public int getValue()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getValue2()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getValue3()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isCancel()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancel()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void canCancel(boolean canCancel)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canCancel()
	{
		// TODO Auto-generated method stub
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
		errors.forEach(System.err::println);
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

}
