package jrm.server.shared;

import jrm.server.shared.actions.ProgressActions;
import lombok.Getter;

public class Worker extends Thread
{
	public @Getter ProgressActions progress = null;
	
	public Worker(Runnable target)
	{
		super(target);
	}


}
