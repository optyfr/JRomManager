package jrm.server.ws;

import java.io.File;
import java.io.IOException;
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
import jrm.server.WebSession;
import one.util.streamex.StreamEx;

public class CompressorWS
{
	private final WebSckt ws;

	public CompressorWS(WebSckt ws)
	{
		this.ws = ws;
	}

	void start(JsonObject jso)
	{
		(ws.session.worker = new Worker(()->{
			WebSession session = ws.session;
			final CompressorFormat format = CompressorFormat.valueOf(session.getUser().settings.getProperty("compressor.format", "TZIP"));
			final boolean force = session.getUser().settings.getProperty("compressor.force", false);

			session.worker.progress = new ProgressWS(ws);
			session.worker.progress.setInfos(Math.min(Runtime.getRuntime().availableProcessors(),ws.session.tmp_compressor_lst.size()), true);
			try
			{
				clearResults();
				AtomicInteger cnt = new AtomicInteger();
				final Compressor compressor = new Compressor(session, cnt, ws.session.tmp_compressor_lst.size(), session.worker.progress);
				List<FileResult> values = new ArrayList<>(ws.session.tmp_compressor_lst.values());
				StreamEx.of(ws.session.tmp_compressor_lst.values().parallelStream().unordered()).takeWhile(p->!session.worker.progress.isCancel()).forEach(fr->{
					final int i = values.indexOf(fr);
					File file = fr.file;
					cnt.incrementAndGet();
					Compressor.UpdResultCallBack cb = txt -> updateResult(i, fr.result = txt);
					Compressor.UpdSrcCallBack scb = src -> updateFile(i, fr.file = src);
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
				});
			}
			catch(BreakException e)
			{
				
			}
			finally
			{
				session.worker.progress.close();
				CompressorWS.this.end();
			}
		})).start();
	}
	
	@SuppressWarnings("serial")
	void updateFile(int row, File file)
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
