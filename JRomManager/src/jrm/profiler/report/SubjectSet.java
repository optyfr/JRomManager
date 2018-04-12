package jrm.profiler.report;

import java.util.List;
import java.util.stream.Collectors;

import jrm.profiler.data.Machine;

public class SubjectSet extends Subject
{
	private Status status = Status.UNKNOWN;
	
	public enum Status {
		UNKNOWN,
		FOUND,
		CREATE,
		CREATEFULL,
		UNNEEDED,
		MISSING;
	};
	
	public SubjectSet(Machine machine)
	{
		super(machine);
	}
	
	@Override
	public Subject clone(List<FilterOptions> filterOptions)
	{
		SubjectSet clone;
		clone = new SubjectSet(machine);
		clone.status = this.status;
		clone.notes = this.filter(filterOptions);
		return clone;
	}

	public List<Note> filter(List<FilterOptions> filterOptions)
	{
		return notes.stream().filter(n->{
			if(!filterOptions.contains(FilterOptions.SHOWOK) && n instanceof EntryOK)
				return false;
			return true;
		}).collect(Collectors.toList());
	}
	
	public void setMissing()
	{
		status = Status.MISSING;
	}
	
	public void setFound()
	{
		status = Status.FOUND;
	}
	
	public void setUnneeded()
	{
		status = Status.UNNEEDED;
	}
	
	public void setCreate()
	{
		status = Status.CREATE;
	}
	
	public void setCreateFull()
	{
		status = Status.CREATEFULL;
	}
	
	public Status getStatus()
	{
		return status;
	}
	
	public boolean hasNotes()
	{
		return notes.stream().filter(n -> !(n instanceof EntryOK)).count()>0;
	}
	
	public boolean isFixable()
	{
		return notes.stream().filter(n -> {return n instanceof EntryMissing || n instanceof EntryWrongHash;}).count()==0;
	}
	
	public boolean isFound()
	{
		return status==Status.FOUND;
	}
	
	public boolean isMissing()
	{
		return status==Status.MISSING;
	}
	
	public boolean isOK()
	{
		return isFound() && !hasNotes();
	}
	
	@Override
	public String toString()
	{
		String ret = "["+machine.name+"] ["+machine.description+"]";
		switch(status)
		{
			case MISSING:
				ret += " is missing";
				break;
			case UNNEEDED:
				ret += " is unneeded";
				break;
			case FOUND:
				if(hasNotes())
				{
					if(isFixable())
						ret += " found, but need fixes";
					else
						ret += " found, but is incomplete and not fully fixable";
				}
				else
					ret += " found";
				break;
			case CREATE:
			case CREATEFULL:
				if(isFixable())
					ret += " is missing but can be totally created";
				else
					ret += " is missing but can be partially created";
				break;
			default:
				ret += " is unknown";
				break;
		}
		return ret;
	}

}
