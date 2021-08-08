package jrm.profile.report;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.List;

import jrm.locale.Messages;
import jrm.profile.data.AnywareBase;
import jrm.profile.data.Container;
/**
 * Subject about container that need to be torrentzipped
 * @author optyfr
 *
 */
public class ContainerTZip extends Subject implements Serializable
{
	private static final String CONTAINER_STR = "container";

	private static final long serialVersionUID = 1L;

	/**
	 * The {@link Container} in relation
	 */
	Container container;

	private static final ObjectStreamField[] serialPersistentFields = {	//NOSONAR
		new ObjectStreamField(CONTAINER_STR, Container.class)
	};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put(CONTAINER_STR, container);
		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		container = (Container)fields.get(CONTAINER_STR, null);
	}


	/**
	 * Constructor with no related {@link AnywareBase} (set to <code>null</code>), but a related {@link Container}
	 * @param c the {@link Container} in relation
	 */
	public ContainerTZip(final Container c)
	{
		super(null);
		container = c;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("ContainerTZip.NeedTZip"), container.getRelFile()); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toString();
	}
	
	@Override
	public Subject clone(final List<FilterOptions> filterOptions)
	{
		return new ContainerTZip(container);
	}

	@Override
	public void updateStats()
	{
		// do nothing
	}
	
	@Override
	public boolean equals(Object o)
	{
		return super.equals(o);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

}
