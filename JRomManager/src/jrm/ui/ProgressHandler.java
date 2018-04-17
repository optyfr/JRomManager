package jrm.ui;

public interface ProgressHandler
{
	public void setProgress(String msg);

	public void setProgress(String msg, Integer val);

	public void setProgress(String msg, Integer val, Integer max);

	public void setProgress(String msg, Integer val, Integer max, String submsg);

	public void setProgress2(String msg, Integer val);

	public void setProgress2(String msg, Integer val, Integer max);

	public boolean isCancel();

	public void cancel();
}
