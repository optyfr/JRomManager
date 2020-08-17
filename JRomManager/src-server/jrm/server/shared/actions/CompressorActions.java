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
		(ws.getSession().setWorker(new Worker(()->{
			WebSession session = ws.getSession();
			final CompressorFormat format = CompressorFormat.valueOf(session.getUser().getSettings().getProperty(SettingsEnum.compressor_format, "TZIP"));
			final boolean force = session.getUser().getSettings().getProperty(SettingsEnum.compressor_force, false);

			final var use_parallelism = true;
			final var nThreads = use_parallelism ? session.getUser().getSettings().getProperty(SettingsEnum.thread_count, -1) : 1;

			session.getWorker().progress = new ProgressActions(ws);
			session.getWorker().progress.setInfos(Math.min(Runtime.getRuntime().availableProcessors(),ws.getSession().getCachedCompressorList().size()), true);
			try
			{
				clearResults();
				final AtomicInteger cnt = new AtomicInteger();
				final Compressor compressor = new Compressor(session, cnt, ws.getSession().getCachedCompressorList().size(), session.getWorker().progress);
				List<FileResult> values = new ArrayList<>(ws.getSession().getCachedCompressorList().values());
				
				new MultiThreading<Compressor.FileResult>(nThreads, fr -> {
					if(session.getWorker().progress.isCancel())
						return;
					try
					{
						final int i = values.indexOf(fr);
						File file = PathAbstractor.getAbsolutePath(session, fr.file.toString()).toFile();
						Compressor.UpdResultCallBack cb = txt -> updateResult(i, fr.result = txt);
						Compressor.UpdSrcCallBack scb = src -> updateFile(i, fr.file = PathAbstractor.getRelativePath(session, src.toPath()));
						switch(format)
						{
							case SEVENZIP:
							{
								switch(FilenameUtils.getExtension(file.getName()))
								{
									case "zip":
										compressor.zip2SevenZip(file, cb, scb);
										break;
									case "7z":
										if(force)
											compressor.sevenZip2SevenZip(file, cb, scb);
										else
											cb.apply("Skipped");
										break;
									default:
										compressor.sevenZip2SevenZip(file, cb, scb);
										break;
								}
								break;
							}
							case ZIP:
							{
								switch(FilenameUtils.getExtension(file.getName()))
								{
									case "zip":
										if(force)
											compressor.zip2Zip(file, cb, scb);
										else
											cb.apply("Skipped");
										break;
									default:
										compressor.sevenZip2Zip(file, false, cb, scb);
										break;
								}
								break;
							}
							case TZIP:
							{
								switch(FilenameUtils.getExtension(file.getName()))
								{
									case "zip":
										compressor.zip2TZip(file, force, cb);
										break;
									default:
										file = compressor.sevenZip2Zip(file, true, cb, scb);
										if(file!=null && file.exists())
											compressor.zip2TZip(file, force, cb);
										break;
								}
								break;
							}
						}
					}
					catch(BreakException e)
					{
						session.getWorker().progress.cancel();;
					}
					catch (final Throwable e)
					{	// oups! something unexpected happened
						Log.err(e.getMessage(), e);
					}
					finally
					{
						cnt.incrementAndGet();
					}
					return;
				}).start(ws.getSession().getCachedCompressorList().values().stream());
				
			}
			catch(BreakException e)
			{
				session.getWorker().progress.cancel();;
			}
			finally
			{
				session.getWorker().progress.close();
				CompressorActions.this.end();
			}
		}))).start();
	}
	
	@SuppressWarnings("serial")
	void updateFile(int row, Path file)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Compressor.updateFile");
					add("params", new JsonObject() {{
						add("row", row);
						add("file", file.toString());
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	@SuppressWarnings("serial")
	void updateResult(int row, String result)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Compressor.updateResult");
					add("params", new JsonObject() {{
						add("row", row);
						add("result", result);
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	@SuppressWarnings("serial")
	void clearResults()
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Compressor.clearResults");
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	@SuppressWarnings("serial")
	void end()
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Compressor.end");
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

}
