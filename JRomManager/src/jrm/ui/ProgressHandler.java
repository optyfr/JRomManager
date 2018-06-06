package jrm.ui;

import java.io.InputStream;

public interface ProgressHandler
{
	public void setInfos(int threadCnt, boolean multipleSubInfos);
	public void clearInfos();
		
	public void setProgress(String msg);

	public void setProgress(String msg, Integer val);

	public void setProgress(String msg, Integer val, Integer max);

	public void setProgress(String msg, Integer val, Integer max, String submsg);

	public void setProgress2(String msg, Integer val);

	public void setProgress2(String msg, Integer val, Integer max);

	public int getValue();

	public int getValue2();

	public boolean isCancel();

	public void cancel();
	
	public InputStream getInputStream(InputStream in, Integer len);
}
