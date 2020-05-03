package jrm.fullserver.security;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jetty.util.ssl.SslContextFactory;

import jrm.misc.Log;

public class SSLReload extends TimerTask
{
	private Timer timer = new Timer(true);
	private SslContextFactory sslcontext;
	
	private SSLReload(SslContextFactory sslcontext)
	{
		this.sslcontext = sslcontext;
	}
	
	public static SSLReload getInstance(SslContextFactory sslcontext)
	{
		return new SSLReload(sslcontext);
	}
	
	@Override
	public void run()
	{
		try
		{
			/*
			 * This tells the SSLContextFactory to reload its certificate
			 */
			sslcontext.reload(scc -> Log.info("SSL certificate reloaded"));
		}
		catch(Exception e)
		{
			Log.err("Error while reloading SSL certificate", e);
		}
		cancel();
		schedule();
	}
	
	private Date getTomorrowMidNight()
	{
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		return Date.from(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	
	private void schedule()
	{
		timer.schedule(this, getTomorrowMidNight());
	}
	
	public void start()
	{
		schedule();
	}
	
}
