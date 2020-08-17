package jrm.ui.progress;

import java.awt.Window;
import java.io.InputStream;

import javax.swing.SwingWorker;

import jrm.aui.progress.ProgressHandler;
import jrm.aui.progress.ProgressInputStream;

import lombok.RequiredArgsConstructor;

public abstract class SwingWorkerProgress<T,V> extends SwingWorker<T,V> implements ProgressHandler
{
	private final Progress progress;
	
	public SwingWorkerProgress(final Window owner)
	{
		super();
		progress = new Progress(owner);
		addPropertyChangeListener(e -> {
			switch(e.getPropertyName())
			{
				case "setProgress":
					if(e.getNewValue() instanceof SetProgress)
					{
						SetProgress props = (SetProgress)e.getNewValue();
						progress.setProgress(props.msg, props.val, props.max, props.submsg);
					}
					break;
				case "setProgress2":
					if(e.getNewValue() instanceof SetProgress2)
					{
						SetProgress2 props = (SetProgress2)e.getNewValue();
						progress.setProgress2(props.msg, props.val, props.max);
					}
					break;
				case "setInfos":
					if(e.getNewValue() instanceof SetInfos)
					{
						SetInfos props = (SetInfos)e.getNewValue();
						progress.setInfos(props.threadCnt, props.multipleSubInfos);
					}
					break;
				case "clearInfos":
					progress.clearInfos();
					break;
				case "canCancel":
					progress.canCancel((Boolean)e.getNewValue());
					break;
				case "cancel":
					progress.cancel();
					break;
				case "close":
					progress.close();
					break;
			}
		});
		progress.setVisible(true);
	}

	@RequiredArgsConstructor
	private static class SetInfos
	{
		private final int threadCnt;
		private final boolean multipleSubInfos;
	}
	
	@Override
	public void setInfos(int threadCnt, boolean multipleSubInfos)
	{
		firePropertyChange("setInfos", null, new SetInfos(threadCnt, multipleSubInfos));
	}

	@Override
	public void clearInfos()
	{
		firePropertyChange("clearInfos", false, true);
	}

	@RequiredArgsConstructor
	private static class SetProgress
	{
		private final String msg;
		private final Integer val;
		private final Integer max;
		private final String submsg;
	}
	
	@Override
	public void setProgress(String msg, Integer val, Integer max, String submsg)
	{
		firePropertyChange("setProgress", null, new SetProgress(msg, val, max, submsg));
	}

	@RequiredArgsConstructor
	private static class SetProgress2
	{
		private final String msg;
		private final Integer val;
		private final Integer max;
	}
	
	@Override
	public void setProgress2(String msg, Integer val, Integer max)
	{
		firePropertyChange("setProgress2", null, new SetProgress2(msg, val, max));
	}

	@Override
	public int getValue()
	{
		return progress.getValue();
	}

	@Override
	public int getValue2()
	{
		return progress.getValue2();
	}

	@Override
	public boolean isCancel()
	{
		return progress.isCancel();
	}

	@Override
	public void cancel()
	{
		firePropertyChange("cancel", false, true);
	}

	@Override
	public void canCancel(boolean canCancel)
	{
		firePropertyChange("canCancel", canCancel(), canCancel);
	}

	@Override
	public boolean canCancel()
	{
		return progress.canCancel();
	}

	@Override
	public InputStream getInputStream(InputStream in, Integer len)
	{
		return new ProgressInputStream(in, len, this);
	}

	@Override
	public void close()
	{
		firePropertyChange("close", false, true);
	}
}
