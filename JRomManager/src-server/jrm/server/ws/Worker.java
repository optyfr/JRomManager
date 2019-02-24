package jrm.server.ws;

public class Worker extends Thread
{
	public ProgressWS progress = null;
	
	public Worker(Runnable target)
	{
		super(target);
	}


}
