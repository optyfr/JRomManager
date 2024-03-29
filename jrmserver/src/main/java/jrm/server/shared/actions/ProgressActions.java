package jrm.server.shared.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jrm.aui.progress.ProgressHandler;
import jrm.aui.progress.ProgressInputStream;
import jrm.misc.Log;
import jrm.misc.OffsetProvider;
import jrm.server.shared.actions.ProgressActions.SetFullProgress.Data.PB;

public class ProgressActions implements ProgressHandler
{
	private static final String HH_MM_SS = "HH:mm:ss";
	private static final String S_OF_S = "%s / %s";
	private static final String HH_MM_SS_OF_HH_MM_SS_NONE = "--:--:-- / --:--:--";
	private ActionsMgr ws;
	private final List<String> errors = new ArrayList<>();

	/** The cancel. */
	private boolean cancel = false;

	private boolean canCancel = true;
	
	private Gson gson;

	static final class SetFullProgress
	{
		static final class Data
		{
			static final class PB
			{
				boolean visibility = false;
				boolean stringPainted = false;
				boolean indeterminate = false;
				int max = 100;
				int val = 0;
				double perc = 0;
				String msg = null;
				String timeleft;

				transient long startTime = 0;	//NOSONAR
			}

			/** Current thread cnt */
			int threadCnt = 1;

			Boolean multipleSubInfos = false;

			String[] infos = { null };
			String[] subinfos = { null };

			final PB pb1 = new PB();
			final PB pb2 = new PB();
			final PB pb3 = new PB();
		}

		static final String cmd = "Progress.setFullProgress";	//NOSONAR
		final Data params;

		SetFullProgress(Data data)
		{
			this.params = data;
		}
	}

	static final class SetInfos
	{
		static final String cmd = "Progress.setInfos";	//NOSONAR
		final Data params;

		static final class Data
		{
			/** Current thread cnt */
			int threadCnt = 1;

			Boolean multipleSubInfos = false;

			Data(int threadCnt, Boolean multipleSubInfos)
			{
				this.threadCnt = threadCnt;
				this.multipleSubInfos = multipleSubInfos;
			}
		}

		SetInfos(Data data)
		{
			this.params = data;
		}

	}

	static final class ExtendInfos
	{
		static final String cmd = "Progress.extendInfos";	//NOSONAR
		final Data params;

		static final class Data
		{
			/** Current thread cnt */
			int threadCnt = 1;

			Boolean multipleSubInfos = false;

			Data(int threadCnt, Boolean multipleSubInfos)
			{
				this.threadCnt = threadCnt;
				this.multipleSubInfos = multipleSubInfos;
			}
		}

		ExtendInfos(Data data)
		{
			this.params = data;
		}

	}

	static final class CanCancel
	{
		static final String cmd = "Progress.canCancel";	//NOSONAR
		final Data params;

		static final class Data
		{
			final boolean canCancel;

			Data(boolean canCancel)
			{
				this.canCancel = canCancel;
			}
		}

		CanCancel(boolean canCancel)
		{
			this.params = new Data(canCancel);
		}

	}

	static final class ClearInfos
	{
		private ClearInfos()
		{
			
		}

		static final String cmd = "Progress.clearInfos";	//NOSONAR
	}

	static final class Open
	{
		private Open()
		{
			
		}
		
		static final String cmd = "Progress";	//NOSONAR
	}

	static final class Close
	{
		static final String cmd = "Progress.close";	//NOSONAR
		final Data params;

		static final class Data
		{
			String[] errors = null;
			
			public Data(List<String> errors)
			{
				this.errors = errors.toArray(String[]::new); 
			}
		}
		
		public Close(List<String> errors)
		{
			this.params = new Data(errors);
		}
	}

	private final SetFullProgress.Data data = new SetFullProgress.Data();

	public ProgressActions(ActionsMgr ws)
	{
		this.ws = ws;
		this.gson = new GsonBuilder().excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT).create();
		sendOpen();
	}

	private void sendOpen()
	{
		try
		{
			if (ws.isOpen())
				ws.send(gson.toJson(new Open()));	//NOSONAR
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	public void reload(ActionsMgr ws)
	{
		this.ws = ws;
		sendOpen();
		sendSetInfos();
		sendSetProgress(0, true);
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

	private void sendSetProgress(final int pb, final boolean force)
	{
		try
		{
			if (pb == 1)
				cleanup();
			if (force)
				ws.send(gson.toJson(new SetFullProgress(data)));
			else if (!data.pb1.visibility && !data.pb2.visibility && !data.pb3.visibility)
				ws.send(gson.toJson(new SetFullProgress(data)));
			else if (pb == 1 && data.pb1.visibility && !data.pb1.indeterminate && data.pb1.val > 0 && data.pb1.max == data.pb1.val)
				ws.send(gson.toJson(new SetFullProgress(data)));
			else if (pb == 2 && data.pb2.visibility && !data.pb2.indeterminate && data.pb2.val > 0 && data.pb2.max == data.pb2.val)
				ws.send(gson.toJson(new SetFullProgress(data)));
			else if (pb == 3 && data.pb3.visibility && !data.pb3.indeterminate && data.pb3.val > 0 && data.pb3.max == data.pb3.val)
				ws.send(gson.toJson(new SetFullProgress(data)));
			else
				ws.sendOptional(gson.toJson(new SetFullProgress(data)));
			data.pb1.msg = null;
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
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
		sendSetInfos();
	}

	private void sendSetInfos()
	{
		try
		{
			if (ws.isOpen())
			{
				ws.send(gson.toJson(new SetInfos(new SetInfos.Data(data.threadCnt, data.multipleSubInfos))));
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	private synchronized void extendInfos(int threadCnt)
	{
		this.data.threadCnt = threadCnt;
		this.data.infos = Arrays.copyOf(this.data.infos, threadCnt);
		if (Boolean.TRUE.equals(this.data.multipleSubInfos))
			this.data.subinfos = Arrays.copyOf(this.data.subinfos, threadCnt);
		sendExtendInfos();
	}

	private void sendExtendInfos()
	{
		try
		{
			if (ws.isOpen())
			{
				ws.send(gson.toJson(new ExtendInfos(new ExtendInfos.Data(data.threadCnt, data.multipleSubInfos))));
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	@Override
	public void clearInfos()
	{
		for (var i = 0; i < data.infos.length; i++)
			data.infos[i] = null;
		for (var i = 0; i < data.subinfos.length; i++)
			data.subinfos[i] = null;
		data.pb2.msg = null;
		sendClearInfos();
	}

	private void sendClearInfos()
	{
		try
		{
			if (ws.isOpen())
				ws.send(gson.toJson(new ClearInfos()));	//NOSONAR
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
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

	/**
	 * @param val
	 * @param max
	 * @param force
	 * @return
	 */
	private boolean computeProgress(final PB pb, final Integer val, final Integer max, boolean force)
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


	/**
	 * @param val
	 */
	private void showDuration(PB pb, int val)
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

	@Override
	public InputStream getInputStream(InputStream in, Integer len)
	{
		return new ProgressInputStream(in, len, this);
	}

	@Override
	public void close()
	{
		try
		{
			if (ws.isOpen())
				ws.send(gson.toJson(new Close(errors)));
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	public boolean canCancel()
	{
		return canCancel;
	}

	public void canCancel(boolean canCancel)
	{
		this.canCancel = canCancel;
		sendCanCancel();
	}

	private void sendCanCancel()
	{
		try
		{
			if (ws.isOpen())
				ws.send(gson.toJson(new CanCancel(canCancel)));
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	@Override
	public void addError(String error)
	{
		errors.add(error);
	}

	@Override
	public void setOptions(Option first, Option... rest)
	{
		// do nothing
		
	}

	private OffsetProvider offsetProvider = null;
	
	@Override
	public void setOffsetProvider(OffsetProvider offsetProvider)
	{
		this.offsetProvider = offsetProvider;
		
	}
	
}
