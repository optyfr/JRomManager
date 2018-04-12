package jrm.profiler.report;

import java.util.List;
import java.util.stream.Collectors;

import jrm.Messages;
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
		switch(status)
		{
			case MISSING:
				return String.format(Messages.getString("SubjectSet.Missing"), machine.name, machine.description); //$NON-NLS-1$
			case UNNEEDED:
				return String.format(Messages.getString("SubjectSet.Unneeded"), machine.name, machine.description); //$NON-NLS-1$
			case FOUND:
				if(hasNotes())
				{
					if(isFixable())
						return String.format(Messages.getString("SubjectSet.FoundNeedFixes"), machine.name, machine.description); //$NON-NLS-1$
					return String.format(Messages.getString("SubjectSet.FoundIncomplete"), machine.name, machine.description); //$NON-NLS-1$
				}
				return String.format(Messages.getString("SubjectSet.Found"), machine.name, machine.description); //$NON-NLS-1$
			case CREATE:
			case CREATEFULL:
				if(isFixable())
					return String.format(Messages.getString("SubjectSet.MissingTotallyCreated"), machine.name, machine.description); //$NON-NLS-1$
				return String.format(Messages.getString("SubjectSet.MissingPartiallyCreated"), machine.name, machine.description); //$NON-NLS-1$
			default:
				return String.format(Messages.getString("SubjectSet.Unknown"), machine.name, machine.description); //$NON-NLS-1$
		}
	}

}
