package jrm.cli;

import java.io.InputStream;

import jrm.ui.progress.ProgressHandler;

public class Progress implements ProgressHandler
{
	private Integer max = null;
	
	public Progress()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setInfos(int threadCnt, boolean multipleSubInfos)
	{
	}

	@Override
	public void clearInfos()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setProgress(String msg)
	{
		setProgress(msg, null, null, null);
	}

	@Override
	public void setProgress(String msg, Integer val)
	{
		setProgress(msg, val, null, null);
	}

	@Override
	public void setProgress(String msg, Integer val, Integer max)
	{
		setProgress(msg, val, max, null);
	}

	@Override
	public void setProgress(String msg, Integer val, Integer max, String submsg)
	{
		if (max != null)
			this.max = max;
		if (msg != null)
		{
			if (val != null && val > 0)
				System.out.format("%s (%d/%d)\n", msg, val, this.max);
			else
				System.out.format("%s\n", msg);
		}
	}

	@Override
	public void setProgress2(String msg, Integer val)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void setProgress2(String msg, Integer val, Integer max)
	{
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

	}

}
