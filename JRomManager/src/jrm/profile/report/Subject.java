package jrm.profile.report;

import java.io.IOException;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import jrm.misc.HTMLRenderer;
import jrm.profile.data.AnywareBase;
import lombok.Getter;

/**
 * A Subject is generally a report node about a container
 * @author optyfr
 *
 */
public abstract class Subject extends AbstractList<Note> implements HTMLRenderer, Serializable
{
	private static final String WARE_STR = "ware";
	private static final String NOTES_STR = "notes";

	private static final long serialVersionUID = 2L;

	/**
	 * The related {@link AnywareBase}
	 */
	protected @Getter AnywareBase ware;

	/**
	 * the {@link List} of {@link Note}s
	 */
	protected @Getter List<Note> notes;

	/**
	 * The {@link Report} root as parent
	 */
	protected transient Report parent;
	
	protected transient int id = -1;

	private static final ObjectStreamField[] serialPersistentFields = {	//NOSONAR
		new ObjectStreamField(WARE_STR, AnywareBase.class),
		new ObjectStreamField(NOTES_STR, List.class)
	};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final var fields = stream.putFields();
		fields.put(WARE_STR, ware); //$NON-NLS-1$
		fields.put(NOTES_STR, notes); //$NON-NLS-1$
		stream.writeFields();
	}

	@SuppressWarnings("unchecked")
	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final var fields = stream.readFields();
		ware = (AnywareBase) fields.get(WARE_STR, null); //$NON-NLS-1$
		notes = (List<Note>) fields.get(NOTES_STR, new ArrayList<>()); //$NON-NLS-1$
		notes.forEach(n -> n.parent = this);
	}
	/**
	 * the public constructor with emptied {@link List}&lt;{@link Note}&gt;
	 * @param machine The related {@link AnywareBase}
	 */
	protected Subject(final AnywareBase machine)
	{
		ware = machine;
		notes = new ArrayList<>();
	}

	/**
	 * The internal constructor
	 * @param org the originating {@link Subject}
	 * @param notes a {@link List}&lt;{@link Note}&gt;
	 */
	protected Subject(Subject org, final List<Note> notes)
	{
		ware = org.ware;
		id = org.id;
		this.notes = notes;
	}

	/**
	 * Clone this subject according a {@link List}&lt;{@link FilterOptions}&gt;
	 * @param filterOptions {@link List}&lt;{@link FilterOptions}&gt; to apply while cloning
	 * @return the cloned {@link Subject}
	 */
	public abstract Subject clone(Set<FilterOptions> filterOptions);

	
	/**
	 * Stream this subject according a {@link List}&lt;{@link FilterOptions}&gt;
	 * @param filterOptions {@link List}&lt;{@link FilterOptions}&gt; to apply while cloning
	 * @return the streamed {@link Subject}
	 */
	public abstract Stream<Note> stream(Set<FilterOptions> filterOptions);

	/**
	 * add a {@link Note} to this {@link Subject}
	 * @param note the {@link Note} to add
	 * @return true on success
	 */
	@Override
	public boolean add(final Note note)
	{
		note.parent = this;
		return notes.add(note);
	}

	/**
	 * get the Ware Name
	 * @return the {@link AnywareBase#getFullName()} of {@link #ware} if not null, otherwise an empty {@link String}
	 */
	public String getWareName()
	{
		if(ware!=null)
			return ware.getFullName();
		return ""; //$NON-NLS-1$
	}

	/**
	 * update Report Statistics
	 */
	public abstract void updateStats();

	@Override
	public abstract String toString();

	public int getId()
	{
		return id;
	}

	@Override
	public int size()
	{
		return notes.size();
	}
	
	@Override
	public Note get(int index)
	{
		return notes.get(index);
	}
	
	@Override
	public int hashCode()
	{
		return System.identityHashCode(this);
	}
	
	@Override
	public boolean equals(Object o)
	{
		return super.equals(o);
	}

}
