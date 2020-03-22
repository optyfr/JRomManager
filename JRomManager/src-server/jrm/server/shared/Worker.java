package jrm.server.shared;

import jrm.server.shared.actions.ProgressActions;

public class Worker extends Thread
{
	public ProgressActions progress = null;
	
	public Worker(Runnable target)
	{
		super(target);
	}


}
