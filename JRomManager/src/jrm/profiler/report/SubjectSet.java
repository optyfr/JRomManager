package jrm.profiler.report;

import jrm.profiler.data.Machine;

public class SubjectSet extends Subject
{
	private Status status = Status.MISSING;
	
	private enum Status {
		FOUND,
		CREATE,
		CREATEFULL,
		MISSING;
	};
	
	public SubjectSet(Machine machine)
	{
		super(machine);
	}

	public void setFound()
	{
		status = Status.FOUND;
	}
	
	public void setCreate()
	{
		status = Status.CREATE;
	}
	
	public void setCreateFull()
	{
		status = Status.CREATEFULL;
	}
	
	@Override
	public String toString()
	{
		switch(status)
		{
			case MISSING:
				return "["+machine.name+"] is missing";
			case FOUND:
				if(notes.size()>0)
					return "["+machine.name+"] found, but need fixes";
			case CREATE:
				return "["+machine.name+"] is missing but can be partially created";
			case CREATEFULL:
				return "["+machine.name+"] is missing but can be totally created";
		}
		return "";
	}

}
