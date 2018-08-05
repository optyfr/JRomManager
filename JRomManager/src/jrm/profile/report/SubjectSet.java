package jrm.profile.report;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profile.data.AnywareBase;

/**
 * Container report subject with status
 * @author optyfr
 *
 */
public class SubjectSet extends Subject
{
	/**
	 * The current {@link Status}
	 */
	private Status status = Status.UNKNOWN;

	/**
	 * All possible status
	 * @author optyfr
	 *
	 */
	public enum Status {
		/**
		 * Unknown status
		 */
		UNKNOWN,
		/**
		 * Found OK
		 */
		FOUND,
		/**
		 * Can be partially created
		 */
		CREATE,
		/**
		 * Can be fully created
		 */
		CREATEFULL,
		/**
		 * set is present bu unneeded
		 */
		UNNEEDED,
		/**
		 * set is totally missing
		 */
		MISSING;
	}

	/**
	 * the public constructor with emptied {@link List}&lt;{@link Note}&gt;
	 * @param machine The related {@link AnywareBase}
	 */
	public SubjectSet(final AnywareBase machine)
	{
		super(machine);
	}

	/**
	 * The internal constructor
	 * @param machine The related {@link AnywareBase}
	 * @param notes a {@link List}&lt;{@link Note}&gt;
	 */
	private SubjectSet(final AnywareBase machine, final List<Note> notes)
	{
		super(machine, notes);
	}

	@Override
	public Subject clone(final List<FilterOptions> filterOptions)
	{
		SubjectSet clone;
		clone = new SubjectSet(ware, filter(filterOptions));
		clone.status = status;
		return clone;
	}

	/**
	 * Filter notes according a {@link List} of {@link FilterOptions}
	 * @param filterOptions {@link List}&lt;{@link FilterOptions}&gt; to apply while filtering
	 * @return a filtered {@link List}&lt;{@link FilterOptions}&gt;
	 */
	public List<Note> filter(final List<FilterOptions> filterOptions)
	{
		return notes.stream().filter(n->{
			if(!filterOptions.contains(FilterOptions.SHOWOK) && n instanceof EntryOK)
				return false;
			return true;
		}).collect(Collectors.toList());
	}

	/**
	 * set status to {@link Status#MISSING}
	 */
	public void setMissing()
	{
		status = Status.MISSING;
	}

	/**
	 * set status to {@link Status#FOUND}
	 */
	public void setFound()
	{
		status = Status.FOUND;
	}

	/**
	 * set status to {@link Status#UNNEEDED}
	 */
	public void setUnneeded()
	{
		status = Status.UNNEEDED;
	}

	/**
	 * set status to {@link Status#CREATE}
	 */
	public void setCreate()
	{
		status = Status.CREATE;
	}

	/**
	 * set status to {@link Status#CREATEFULL}
	 */
	public void setCreateFull()
	{
		status = Status.CREATEFULL;
	}

	/**
	 * get current status
	 * @return {@link #status}
	 */
	public Status getStatus()
	{
		return status;
	}

	/**
	 * does this {@link SubjectSet} have notes (excepted {@link EntryOK})
	 * @return true if it has notes
	 */
	public boolean hasNotes()
	{
		return notes.stream().filter(n -> !(n instanceof EntryOK)).count()>0;
	}

	/**
	 * does this {@link SubjectSet} is totally fixable
	 * @return true if it is fixable, false not totally
	 */
	public boolean isFixable()
	{
		return notes.stream().filter(n -> {return n instanceof EntryMissing || n instanceof EntryWrongHash;}).count()==0;
	}

	/**
	 * does this {@link SubjectSet} is at least partially fixable
	 * @return true if it is fixable, false not at all (or does not need to be fixed)
	 */
	public boolean hasFix()
	{
		return notes.stream().filter(n -> {return !(n instanceof EntryOK || n instanceof EntryMissing || n instanceof EntryWrongHash);}).count()>0;
	}

	/**
	 * does this {@link SubjectSet} have been {@link Status#FOUND}
	 * @return true if {@link #status} is {@link Status#FOUND}
	 */
	public boolean isFound()
	{
		return status==Status.FOUND;
	}

	/**
	 * does this {@link SubjectSet} is {@link Status#MISSING}
	 * @return true if {@link #status} is {@link Status#MISSING}
	 */
	public boolean isMissing()
	{
		return status==Status.MISSING;
	}

	/**
	 * does this {@link SubjectSet} is {@link Status#UNNEEDED}
	 * @return true if {@link #status} is {@link Status#UNNEEDED}
	 */
	public boolean isUnneeded()
	{
		return status==Status.UNNEEDED;
	}

	/**
	 * does this {@link SubjectSet} is OK
	 * @return true if {@link #isFound()} and not {@link #hasNotes()}
	 */
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
				return String.format(Messages.getString("SubjectSet.Missing"), ware.getFullName(), ware.getDescription()); //$NON-NLS-1$
			case UNNEEDED:
				return String.format(Messages.getString("SubjectSet.Unneeded"), ware.getFullName(), ware.getDescription()); //$NON-NLS-1$
			case FOUND:
				if(hasNotes())
				{
					if(isFixable())
						return String.format(Messages.getString("SubjectSet.FoundNeedFixes"), ware.getFullName(), ware.getDescription()); //$NON-NLS-1$
					return String.format(Messages.getString("SubjectSet.FoundIncomplete"), ware.getFullName(), ware.getDescription()); //$NON-NLS-1$
				}
				return String.format(Messages.getString("SubjectSet.Found"), ware.getFullName(), ware.getDescription()); //$NON-NLS-1$
			case CREATE:
			case CREATEFULL:
				if(isFixable())
					return String.format(Messages.getString("SubjectSet.MissingTotallyCreated"), ware.getFullName(), ware.getDescription()); //$NON-NLS-1$
				return String.format(Messages.getString("SubjectSet.MissingPartiallyCreated"), ware.getFullName(), ware.getDescription()); //$NON-NLS-1$
			default:
				return String.format(Messages.getString("SubjectSet.Unknown"), ware.getFullName(), ware.getDescription()); //$NON-NLS-1$
		}
	}

	@Override
	public String getHTML()
	{
		final String machine_name = toBlue(ware.getFullName());
		final String machine_description = toPurple(ware.getDescription());
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
