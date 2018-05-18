package jrm.profile;

import java.io.*;
import java.util.Date;

public final class ProfileNFOStats implements Serializable
{
	private static final long serialVersionUID = 1L;

	public String version = null;
	public Long haveSets = null;
	public Long totalSets = null;
	public Long haveRoms = null;
	public Long totalRoms = null;
	public Long haveDisks = null;
	public Long totalDisks = null;
	public Date created = null;
	public Date scanned = null;
	public Date fixed = null;

	private static final ObjectStreamField[] serialPersistentFields = {
			new ObjectStreamField("version", String.class), //$NON-NLS-1$
			new ObjectStreamField("haveSets", Long.class), //$NON-NLS-1$
			new ObjectStreamField("totalSets", Long.class), //$NON-NLS-1$
			new ObjectStreamField("haveRoms", Long.class), //$NON-NLS-1$
			new ObjectStreamField("totalRoms", Long.class), //$NON-NLS-1$
			new ObjectStreamField("haveDisks", Long.class), //$NON-NLS-1$
			new ObjectStreamField("totalDisks", Long.class), //$NON-NLS-1$
			new ObjectStreamField("created", Date.class), //$NON-NLS-1$
			new ObjectStreamField("scanned", Date.class), //$NON-NLS-1$
			new ObjectStreamField("fixed", Date.class), //$NON-NLS-1$
	};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("version", version); //$NON-NLS-1$
		fields.put("haveSets", haveSets); //$NON-NLS-1$
		fields.put("totalSets", totalSets); //$NON-NLS-1$
		fields.put("haveRoms", haveRoms); //$NON-NLS-1$
		fields.put("totalRoms", totalRoms); //$NON-NLS-1$
		fields.put("haveDisks", haveDisks); //$NON-NLS-1$
		fields.put("totalDisks", totalDisks); //$NON-NLS-1$
		fields.put("created", created); //$NON-NLS-1$
		fields.put("scanned", scanned); //$NON-NLS-1$
		fields.put("fixed", fixed); //$NON-NLS-1$
		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		version = (String) fields.get("version", null); //$NON-NLS-1$
		haveSets = (Long) fields.get("haveSets", null); //$NON-NLS-1$
		totalSets = (Long) fields.get("totalSets", null); //$NON-NLS-1$
		haveRoms = (Long) fields.get("haveRoms", null); //$NON-NLS-1$
		totalRoms = (Long) fields.get("totalRoms", null); //$NON-NLS-1$
		haveDisks = (Long) fields.get("haveDisks", null); //$NON-NLS-1$
		totalDisks = (Long) fields.get("totalDisks", null); //$NON-NLS-1$
		created = (Date) fields.get("created", null); //$NON-NLS-1$
		scanned = (Date) fields.get("scanned", null); //$NON-NLS-1$
		fixed = (Date) fields.get("fixed", null); //$NON-NLS-1$
	}

	public void reset()
	{
		version = null;
		haveSets = null;
		totalSets = null;
		haveRoms = null;
		totalRoms = null;
		haveDisks = null;
		totalDisks = null;
		created = new Date();
		scanned = null;
		fixed = null;
	}
}