package jrm.ui.progress;

import java.awt.HeadlessException;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import jrm.aui.progress.ProgressHandler;
import jrm.aui.progress.ProgressInputStream;
import jrm.misc.OffsetProvider;
import lombok.RequiredArgsConstructor;

public abstract class SwingWorkerProgress<T, V> extends SwingWorker<T, V> implements ProgressHandler
{
	private static final String SET_PROGRESS_3 = "setProgress3";

	private static final String SET_PROGRESS_2 = "setProgress2";

	private static final String SET_PROGRESS = "setProgress";

	private final Progress progress;

	private final List<String> errors = new ArrayList<>();
	
	private int threadCnt;

	protected SwingWorkerProgress(final Window owner)
	{
		super();
		progress = new Progress(owner);
		addPropertyChangeListener(e -> propertyChange(owner, e));
		progress.setVisible(true);
	}

	/**
	 * @param owner
	 * @param e
	 * @throws HeadlessException
	 */
	private void propertyChange(final Window owner, PropertyChangeEvent e) throws HeadlessException
	{
		switch (e.getPropertyName())
		{
			case SET_PROGRESS:
				if (e.getNewValue() instanceof SetProgress props)
					progress.setProgress(props.offset, props.msg, props.val, props.max, props.submsg);
				break;
			case SET_PROGRESS_2:
				if (e.getNewValue() instanceof SetProgress2 props)
					progress.setProgress2(props.msg, props.val, props.max);
				break;
			case SET_PROGRESS_3:
				if (e.getNewValue() instanceof SetProgress3 props)
					progress.setProgress3(props.msg, props.val, props.max);
				break;
			case "setInfos":
				if (e.getNewValue() instanceof SetInfos props)
					progress.setInfos(props.threadCnt, props.multipleSubInfos);
				break;
			case "extendInfos":
				if (e.getNewValue() instanceof ExtendInfos props)
					progress.extendInfos(props.threadCnt, props.multipleSubInfos);
				break;
			case "clearInfos":
				progress.clearInfos();
				break;
			case "canCancel":
				progress.canCancel((Boolean) e.getNewValue());
				break;
			case "cancel":
				progress.cancel();
				break;
			case "close":
				progress.close();
				if (!errors.isEmpty())
					JOptionPane.showMessageDialog(owner, errors.stream().collect(Collectors.joining("\n")), "Error", JOptionPane.ERROR_MESSAGE);
				break;
			default:
				break;
		}
	}

	@RequiredArgsConstructor
	private static class SetInfos
	{
		private final int threadCnt;
		private final Boolean multipleSubInfos;
	}

	@RequiredArgsConstructor
	private static class ExtendInfos
	{
		private final int threadCnt;
		private final Boolean multipleSubInfos;
	}

	private Boolean multipleSubInfos = null;

	@Override
	public void setInfos(int threadCnt, Boolean multipleSubInfos)
	{
		this.threadCnt = threadCnt <= 0 ? Runtime.getRuntime().availableProcessors() : threadCnt;
		this.multipleSubInfos = multipleSubInfos;
		firePropertyChange("setInfos", null, new SetInfos(this.threadCnt, this.multipleSubInfos));
	}

	public void extendInfos(int threadCnt)
	{
		this.threadCnt = threadCnt;
		firePropertyChange("extendInfos", null, new ExtendInfos(this.threadCnt, this.multipleSubInfos));
	}

	@Override
	public void clearInfos()
	{
		firePropertyChange("clearInfos", false, true);
	}

	@RequiredArgsConstructor
	private static class SetProgress
	{
		private final int offset;
		private final String msg;
		private final Integer val;
		private final Integer max;
		private final String submsg;
	}

	private void cleanup()
	{
		if (offsetProvider != null)
		{
			for(final var offset : offsetProvider.freeOffsets())
			{
				if(offset < threadCnt)
				{
					firePropertyChange(SET_PROGRESS, null, new SetProgress(offset, "", null, null, this.multipleSubInfos != null && this.multipleSubInfos ? "" : null));
				}
			}
		}
	}

	@Override
	public void setProgress(String msg, Integer val, Integer max, String submsg)
	{
		final int offset = getOffset();
		cleanup();
		firePropertyChange(SET_PROGRESS, null, new SetProgress(offset, msg, val, max, submsg));
	}

	/**
	 * @return
	 */
	private int getOffset()
	{
		if(offsetProvider != null)
		{
			int offset = offsetProvider.getOffset();
			if (offset < 0)
				return 0;
			if (offset >= threadCnt)
				extendInfos(offset + 1);
			return offset;
		}
		return 0;

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
		firePropertyChange(SET_PROGRESS_2, null, new SetProgress2(msg, val, max));
	}

	@RequiredArgsConstructor
	private static class SetProgress3
	{
		private final String msg;
		private final Integer val;
		private final Integer max;
	}

	@Override
	public void setProgress3(String msg, Integer val, Integer max)
	{
		firePropertyChange(SET_PROGRESS_3, null, new SetProgress3(msg, val, max));
	}

	@Override
	public int getCurrent()
	{
		return progress.getValue();
	}

	@Override
	public int getCurrent2()
	{
		return progress.getValue2();
	}

	@Override
	public int getCurrent3()
	{
		return progress.getValue3();
	}

	@Override
	public boolean isCancel()
	{
		return progress.isCancel();
	}

	@Override
	public void doCancel()
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
	
	@Override
	public void addError(String error)
	{
		errors.add(error);
	}
	
	@Override
	public void setOptions(Option first, Option... rest)
	{
	}
	
	private OffsetProvider offsetProvider = null;
	
	 @Override
	public void setOffsetProvider(OffsetProvider offsetProvider)
	{
		this.offsetProvider = offsetProvider;
	}
}
