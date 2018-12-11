package jrm.profile.report;

import java.io.*;
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
	private static final long serialVersionUID = 1L;

	/**
	 * The {@link Container} in relation
	 */
	Container container;

	private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("container", Container.class)};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("container", container);
		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		container = (Container)fields.get("container", null);
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
		return String.format(Messages.getString("ContainerTZip.NeedTZip"), container.file); //$NON-NLS-1$
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
	}

}
