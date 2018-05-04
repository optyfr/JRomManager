package jrm.profile.report;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profile.data.Anyware;

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
	}
	
	public SubjectSet(Anyware machine)
	{
		super(machine);
	}
	
	@Override
	public Subject clone(List<FilterOptions> filterOptions)
	{
		SubjectSet clone;
		clone = new SubjectSet(ware);
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
	
	public boolean hasFix()
	{
		return notes.stream().filter(n -> {return !(n instanceof EntryOK || n instanceof EntryMissing || n instanceof EntryWrongHash);}).count()>0;
	}
	
	public boolean isFound()
	{
		return status==Status.FOUND;
	}
	
	public boolean isMissing()
	{
		return status==Status.MISSING;
	}
	
	public boolean isUnneeded()
	{
		return status==Status.UNNEEDED;
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
				return String.format(Messages.getString("SubjectSet.Missing"), ware.getFullName(), ware.description); //$NON-NLS-1$
			case UNNEEDED:
				return String.format(Messages.getString("SubjectSet.Unneeded"), ware.getFullName(), ware.description); //$NON-NLS-1$
			case FOUND:
				if(hasNotes())
				{
					if(isFixable())
						return String.format(Messages.getString("SubjectSet.FoundNeedFixes"), ware.getFullName(), ware.description); //$NON-NLS-1$
					return String.format(Messages.getString("SubjectSet.FoundIncomplete"), ware.getFullName(), ware.description); //$NON-NLS-1$
				}
				return String.format(Messages.getString("SubjectSet.Found"), ware.getFullName(), ware.description); //$NON-NLS-1$
			case CREATE:
			case CREATEFULL:
				if(isFixable())
					return String.format(Messages.getString("SubjectSet.MissingTotallyCreated"), ware.getFullName(), ware.description); //$NON-NLS-1$
				return String.format(Messages.getString("SubjectSet.MissingPartiallyCreated"), ware.getFullName(), ware.description); //$NON-NLS-1$
			default:
				return String.format(Messages.getString("SubjectSet.Unknown"), ware.getFullName(), ware.description); //$NON-NLS-1$
		}
	}
	
	@Override
	public String getHTML()
	{
		String machine_name = toBlue(ware.getFullName());
		String machine_description = toPurple(ware.description);
		switch(status)
		{
			case MISSING:
				return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("SubjectSet.Missing")), machine_name, machine_description)); //$NON-NLS-1$
			case UNNEEDED:
				return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("SubjectSet.Unneeded")), machine_name, machine_description)); //$NON-NLS-1$
			case FOUND:
				if(hasNotes())
				{
					if(isFixable())
						return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("SubjectSet.FoundNeedFixes")), machine_name, machine_description)); //$NON-NLS-1$
					return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("SubjectSet.FoundIncomplete")), machine_name, machine_description)); //$NON-NLS-1$
				}
				return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("SubjectSet.Found")), machine_name, machine_description)); //$NON-NLS-1$
			case CREATE:
			case CREATEFULL:
				if(isFixable())
					return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("SubjectSet.MissingTotallyCreated")), machine_name, machine_description)); //$NON-NLS-1$
				return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("SubjectSet.MissingPartiallyCreated")), machine_name, machine_description)); //$NON-NLS-1$
			default:
				return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("SubjectSet.Unknown")), machine_name, machine_description)); //$NON-NLS-1$
		}
	}

	@Override
	public void updateStats()
	{
		switch(status)
		{
			case MISSING:
				parent.stats.set_missing++;
				break;
			case UNNEEDED:
				parent.stats.set_unneeded++;
				break;
			case FOUND:
				parent.stats.set_found++;
				if(hasNotes())
				{
					if(isFixable())
						parent.stats.set_found_fixcomplete++;
					else
						parent.stats.set_found_fixpartial++;
				}
				else
					parent.stats.set_found_ok++;
				break;
			case CREATE:
			case CREATEFULL:
				parent.stats.set_create++;
				if(isFixable())
					parent.stats.set_create_complete++;
				else
					parent.stats.set_create_partial++;
				break;
			default:
				break;
		}
	}

}
