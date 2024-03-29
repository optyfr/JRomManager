package jrm.fx.ui.progress;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.DurationFormatUtils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import jrm.aui.progress.ProgressHandler;
import jrm.aui.progress.ProgressInputStream;
import jrm.misc.OffsetProvider;
import lombok.Data;

public abstract class ProgressTask<V> extends Task<V> implements ProgressHandler
{
	private static final String HH_MM_SS = "HH:mm:ss";
	private static final String S_OF_S = "%s / %s";
	private static final String HH_MM_SS_OF_HH_MM_SS_NONE = "--:--:-- / --:--:--";

	private final Progress progress;

	private final List<String> errors = new ArrayList<>();
	

	private boolean cancel = false;
	private boolean canCancel = true;

	static final @Data class PData
	{
		static final @Data class PB
		{
			boolean visibility = false;
			boolean stringPainted = false;
			boolean indeterminate = false;
			int max = 100;
			int val = 0;
			double perc = 0;
			String msg = null;
			String timeleft;

			transient long startTime = System.currentTimeMillis();	//NOSONAR
			
			PB()
			{
				
			}
			
			PB(PB pb)
			{
				visibility = pb.visibility;
				stringPainted = pb.stringPainted;
				indeterminate = pb.indeterminate;
				max = pb.max;
				val = pb.val;
				perc = pb.perc;
				msg = pb.msg;
				timeleft = pb.timeleft;
			}
		}

		/** Current thread cnt */
		int threadCnt = 1;

		Boolean multipleSubInfos = false;

		String[] infos = { null };
		String[] subinfos = { null };

		final PB pb1;
		final PB pb2;
		final PB pb3;
		
		PData()
		{
			pb1 = new PB();
			pb2 = new PB();
			pb3 = new PB();
		}
		
		PData(PData data)
		{
			threadCnt = data.threadCnt;
			multipleSubInfos = data.multipleSubInfos;
			infos = Arrays.copyOf(data.infos, data.infos.length);
			subinfos = Arrays.copyOf(data.subinfos, data.subinfos.length);
			pb1 = new PB(data.pb1);
			pb2 = new PB(data.pb2);
			pb3 = new PB(data.pb3);
		}
	}

	private final PData data = new PData();

	protected ProgressTask(Stage owner) throws IOException, URISyntaxException
	{
		super();
		progress = new Progress(owner, this);
	}

	@Override
	public synchronized void setInfos(int threadCnt, Boolean multipleSubInfos)
	{
		this.data.threadCnt = threadCnt <= 0 ? Runtime.getRuntime().availableProcessors() : threadCnt;
		this.data.multipleSubInfos = multipleSubInfos;
		this.data.infos = new String[this.data.threadCnt];
		if(multipleSubInfos == null)
			this.data.subinfos = new String[0];
		else
			this.data.subinfos = new String[multipleSubInfos.booleanValue() ? this.data.threadCnt : 1];
		Platform.runLater(() -> progress.getController().setInfos(data.threadCnt, data.multipleSubInfos));
	}

	private synchronized void extendInfos(int threadCnt)
	{
		this.data.threadCnt = threadCnt;
		this.data.infos = Arrays.copyOf(this.data.infos, this.data.threadCnt);
		if(Boolean.TRUE.equals(data.multipleSubInfos))
			this.data.subinfos = Arrays.copyOf(this.data.subinfos, this.data.threadCnt);
		Platform.runLater(() -> progress.getController().extendInfos(data.threadCnt, data.multipleSubInfos));
	}

	@Override
	public void clearInfos()
	{
		for (var i = 0; i < data.infos.length; i++)
			data.infos[i] = null;
		for (var i = 0; i < data.subinfos.length; i++)
			data.subinfos[i] = null;
		data.pb2.msg = null;
		Platform.runLater(() -> progress.getController().clearInfos());
	}

	private synchronized void cleanup()
	{
		if (offsetProvider != null)
		{
			for(final var offset : offsetProvider.freeOffsets())
			{
				if(offset < data.infos.length)
				{
					data.infos[offset] = "";
					if (data.infos.length == data.subinfos.length)
						data.subinfos[offset] = "";
				}
			}
		}
	}

	/**
	 * @return
	 */
	private synchronized int getOffset()
	{
		if(offsetProvider != null)
		{
			int offset = offsetProvider.getOffset();
			if (offset < 0)
				return 0;
			if (offset >= data.threadCnt)
				extendInfos(offset + 1);
			return offset;
		}
		return 0;
	}

	@Override
	public void setProgress(String msg, Integer val, Integer max, String submsg)
	{
		int offset = getOffset();
		if (msg != null)
			data.infos[offset] = msg;
		var force = false;
		if (val != null)
		{
			if (val < 0 && data.pb1.visibility)
			{
				data.pb1.visibility = false;
			}
			else if (val >= 0 && !data.pb1.visibility)
			{
				data.pb1.visibility = true;
			}
			data.pb1.stringPainted = val != 0;
			data.pb1.indeterminate = val == 0;
			force = computeProgress(data.pb1, val, max, force);
			showDuration(data.pb1, val);
		}
		if (submsg != null || (val != null && val == -1))
		{
			if (data.subinfos.length == 1)
				data.subinfos[0] = submsg;
			else if (data.subinfos.length > 1)
				data.subinfos[offset] = submsg;
		}
		sendSetProgress(1, force);
	}

	private long lastEvent = 0;
	private PData lastPData = null;
	
	private synchronized void sendSetProgress(final int pb, final boolean force)
	{
		boolean doit = false;
		if (pb == 1)
			cleanup();
		if (force)
			doit = true;
		else if (pb == 1 && data.pb1.visibility && !data.pb1.indeterminate && data.pb1.val > 0 && data.pb1.max == data.pb1.val)
			doit = true;
		else if (pb == 2 && data.pb2.visibility && !data.pb2.indeterminate && data.pb2.val > 0 && data.pb2.max == data.pb2.val)
			doit = true;
		else if (pb == 3 && data.pb3.visibility && !data.pb3.indeterminate && data.pb3.val > 0 && data.pb3.max == data.pb3.val)
			doit = true;
		else if (lastPData == null || (lastPData.infos.length == 1 && lastPData.infos[0] != null && !lastPData.infos[0].equals(this.data.infos[0])))
			doit = true;
		else if (System.currentTimeMillis() - lastEvent > 100)
			doit = true;
		else if (!data.pb1.visibility && !data.pb2.visibility && !data.pb3.visibility && !options.contains(Option.LAZY))
			doit = true;
		if (doit)
		{
			lastPData = new PData(this.data);
			Platform.runLater(() -> progress.getController().setFullProgress(lastPData));
			lastEvent = System.currentTimeMillis();
		}
		data.pb1.msg = null;
	}

	/**
	 * @param val
	 * @param max
	 * @param force
	 * @return
	 */
	private boolean computeProgress(final PData.PB pb, final Integer val, final Integer max, boolean force)
	{
		if (max != null)
			pb.max = max;
		if (val >= 0)
			pb.val = val;
		if (val == 0)
			pb.startTime = System.currentTimeMillis();
		if (pb.val >= 0 && pb.max > 0)
		{
			final var perc = pb.val * 100.0f / pb.max;
			force = (int) pb.perc != (int) perc;
			pb.perc = perc;
		}
		return force;
	}

	/**
	 * @param val
	 */
	private void showDuration(PData.PB pb, int val)
	{
		if (val > 0)
		{
			if (pb.msg == null)
				pb.msg = String.format("%.02f%%", pb.perc);
			final String left = DurationFormatUtils.formatDuration((System.currentTimeMillis() - pb.startTime) * (pb.max - val) / val, HH_MM_SS); //$NON-NLS-1$
			final String total = DurationFormatUtils.formatDuration((System.currentTimeMillis() - pb.startTime) * pb.max / val, HH_MM_SS); //$NON-NLS-1$
			pb.timeleft = String.format(S_OF_S, left, total); //$NON-NLS-1$
		}
		else
			pb.timeleft = HH_MM_SS_OF_HH_MM_SS_NONE; //$NON-NLS-1$
	}

	@Override
	public void setProgress2(String msg, Integer val, Integer max)
	{
		var force = false;
		if (msg != null && val != null)
		{
			if (!data.pb2.visibility)
				data.pb2.visibility = true;
			data.pb2.stringPainted = true/*msg != null || val > 0*/;
			data.pb2.msg = msg;
			data.pb2.indeterminate = val == 0 && msg == null;
			force = computeProgress(data.pb2, val, max, force);
			showDuration(data.pb2, val);
		}
		else if (data.pb2.visibility)
			data.pb2.visibility = false;
		sendSetProgress(2, force);
	}

	@Override
	public void setProgress3(String msg, Integer val, Integer max)
	{
		var force = false;
		if (msg != null && val != null)
		{
			if (!data.pb3.visibility)
				data.pb3.visibility = true;
			data.pb3.stringPainted = true/*msg != null || val > 0*/;
			data.pb3.msg = msg;
			data.pb3.indeterminate = val == 0 && msg == null;
			force = computeProgress(data.pb3, val, max, force);
			showDuration(data.pb3, val);
		}
		else if (data.pb3.visibility)
			data.pb3.visibility = false;
		sendSetProgress(3, force);
	}

	@Override
	public int getCurrent()
	{
		return data.pb1.val;
	}

	@Override
	public int getCurrent2()
	{
		return data.pb2.val;
	}

	@Override
	public int getCurrent3()
	{
		return data.pb3.val;
	}

	@Override
	public boolean isCancel()
	{
		return cancel;
	}

	@Override
	public void doCancel()
	{
		cancel = true;
	}

	public boolean canCancel()
	{
		return canCancel;
	}

	public void canCancel(boolean canCancel)
	{
		this.canCancel = canCancel;
		Platform.runLater(()->progress.getController().canCancel(canCancel));
	}

	@Override
	public InputStream getInputStream(InputStream in, Integer len)
	{
		return new ProgressInputStream(in, len, this);
	}

	@Override
	public void close()
	{
		progress.close();
	}

	@Override
	public void addError(String error)
	{
		errors.add(error);
	}
	
	private Set<Option> options = EnumSet.noneOf(Option.class);
	
	@Override
	public void setOptions(Option first, Option... rest)
	{
		options = EnumSet.of(first, rest);
		
	}

	private OffsetProvider offsetProvider = null;
	
	@Override
	public void setOffsetProvider(OffsetProvider offsetProvider)
	{
		this.offsetProvider = offsetProvider;
	}
}
