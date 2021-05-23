package jrm.server.shared.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FilenameUtils;

import com.eclipsesource.json.JsonObject;

import jrm.batch.Compressor;
import jrm.batch.Compressor.FileResult;
import jrm.batch.CompressorFormat;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.MultiThreading;
import jrm.misc.SettingsEnum;
import jrm.security.PathAbstractor;
import jrm.server.shared.WebSession;
import jrm.server.shared.Worker;

public class CompressorActions
{
	private final ActionsMgr ws;

	public CompressorActions(ActionsMgr ws)
	{
		this.ws = ws;
	}

	public void start(JsonObject jso)
	{
		(ws.getSession().setWorker(new Worker(() -> {
			final var session = ws.getSession();
			final var format = CompressorFormat.valueOf(session.getUser().getSettings().getProperty(SettingsEnum.compressor_format, "TZIP"));
			final var force = session.getUser().getSettings().getProperty(SettingsEnum.compressor_force, false);

			final var use_parallelism = session.getUser().getSettings().getProperty(SettingsEnum.use_parallelism, true);
			final var nThreads = use_parallelism ? session.getUser().getSettings().getProperty(SettingsEnum.thread_count, -1) : 1;

			session.getWorker().progress = new ProgressActions(ws);
			session.getWorker().progress.setInfos(Math.min(Runtime.getRuntime().availableProcessors(), ws.getSession().getCachedCompressorList().size()), true);
			try
			{
				clearResults();
				final var cnt = new AtomicInteger();
				final var compressor = new Compressor(session, cnt, ws.getSession().getCachedCompressorList().size(), session.getWorker().progress);
				List<FileResult> values = new ArrayList<>(ws.getSession().getCachedCompressorList().values());

				new MultiThreading<Compressor.FileResult>(nThreads, fr -> doCompress(session, format, force, cnt, compressor, values, fr)).start(ws.getSession().getCachedCompressorList().values().stream());

			}
			catch (BreakException e)
			{
				session.getWorker().progress.cancel();
			}
			finally
			{
				session.getWorker().progress.close();
				CompressorActions.this.end();
			}
		}))).start();
	}

	/**
	 * @param session
	 * @param format
	 * @param force
	 * @param cnt
	 * @param compressor
	 * @param values
	 * @param fr
	 */
	private void doCompress(final WebSession session, final CompressorFormat format, final boolean force, final AtomicInteger cnt, final Compressor compressor, List<FileResult> values, FileResult fr)
	{
		if (session.getWorker().progress.isCancel())
			return;
		try
		{
			final int i = values.indexOf(fr);
			var file = PathAbstractor.getAbsolutePath(session, fr.getFile().toString()).toFile();
			Compressor.UpdResultCallBack cb = txt -> {
				fr.setResult(txt);
				updateResult(i, fr.getResult());
			};
			Compressor.UpdSrcCallBack scb = src -> {
				fr.setFile(PathAbstractor.getRelativePath(session, src.toPath()));
				updateFile(i, fr.getFile());
			};
			switch (format)
			{
				case SEVENZIP:
				{
					doCompress2SevenZip(force, compressor, file, cb, scb);
					break;
				}
				case ZIP:
				{
					doCompress2Zip(force, compressor, file, cb, scb);
					break;
				}
				case TZIP:
				{
					doCompress2TZip(force, compressor, file, cb, scb);
					break;
				}
			}
		}
		catch (BreakException e)
		{
			session.getWorker().progress.cancel();
		}
		catch (final Exception e)
		{ // oups! something unexpected happened
			Log.err(e.getMessage(), e);
		}
		finally
		{
			cnt.incrementAndGet();
		}
	}

	/**
	 * @param force
	 * @param compressor
	 * @param file
	 * @param cb
	 * @param scb
	 * @throws IllegalArgumentException
	 */
	private void doCompress2TZip(final boolean force, final Compressor compressor, File file, Compressor.UpdResultCallBack cb, Compressor.UpdSrcCallBack scb) throws IllegalArgumentException
	{
		switch (FilenameUtils.getExtension(file.getName()))
		{
			case "zip":
				compressor.zip2TZip(file, force, cb);
				break;
			default:
				file = compressor.sevenZip2Zip(file, true, cb, scb);
				if (file != null && file.exists())
					compressor.zip2TZip(file, force, cb);
				break;
		}
	}

	/**
	 * @param force
	 * @param compressor
	 * @param file
	 * @param cb
	 * @param scb
	 * @throws IllegalArgumentException
	 */
	private void doCompress2Zip(final boolean force, final Compressor compressor, File file, Compressor.UpdResultCallBack cb, Compressor.UpdSrcCallBack scb) throws IllegalArgumentException
	{
		switch (FilenameUtils.getExtension(file.getName()))
		{
			case "zip":
				if (force)
					compressor.zip2Zip(file, cb, scb);
				else
					cb.apply("Skipped");
				break;
			default:
				compressor.sevenZip2Zip(file, false, cb, scb);
				break;
		}
	}

	/**
	 * @param force
	 * @param compressor
	 * @param file
	 * @param cb
	 * @param scb
	 * @throws IllegalArgumentException
	 */
	private void doCompress2SevenZip(final boolean force, final Compressor compressor, File file, Compressor.UpdResultCallBack cb, Compressor.UpdSrcCallBack scb) throws IllegalArgumentException
	{
		switch (FilenameUtils.getExtension(file.getName()))
		{
			case "zip":
				compressor.zip2SevenZip(file, cb, scb);
				break;
			case "7z":
				if (force)
					compressor.sevenZip2SevenZip(file, cb, scb);
				else
					cb.apply("Skipped");
				break;
			default:
				compressor.sevenZip2SevenZip(file, cb, scb);
				break;
		}
	}

	void updateFile(int row, Path file)
	{
		try
		{
			if (ws.isOpen())
			{
				final var msg = new JsonObject();
				msg.add("cmd", "Compressor.updateFile");
				final var params = new JsonObject();
				params.add("row", row);
				params.add("file", file.toString());
				msg.add("params", params);
				ws.send(msg.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	void updateResult(int row, String result)
	{
		try
		{
			if (ws.isOpen())
			{
				final var msg = new JsonObject();
				msg.add("cmd", "Compressor.updateResult");
				final var params = new JsonObject();
				params.add("row", row);
				params.add("result", result);
				msg.add("params", params);
				ws.send(msg.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	void clearResults()
	{
		try
		{
			if (ws.isOpen())
			{
				final var msg = new JsonObject();
				msg.add("cmd", "Compressor.clearResults");
				ws.send(msg.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	void end()
	{
		try
		{
			if (ws.isOpen())
			{
				final var msg = new JsonObject();
				msg.add("cmd", "Compressor.end");
				ws.send(msg.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

}
