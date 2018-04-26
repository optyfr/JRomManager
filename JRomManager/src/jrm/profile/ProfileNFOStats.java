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
		new ObjectStreamField("version", String.class), 
		new ObjectStreamField("haveSets", Long.class),
		new ObjectStreamField("totalSets", Long.class),
		new ObjectStreamField("haveRoms", Long.class),
		new ObjectStreamField("totalRoms", Long.class),
		new ObjectStreamField("haveDisks", Long.class),
		new ObjectStreamField("totalDisks", Long.class),
		new ObjectStreamField("created", Date.class),
		new ObjectStreamField("scanned", Date.class),
		new ObjectStreamField("fixed", Date.class),
	};

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("version", version);
		fields.put("haveSets", haveSets);
		fields.put("totalSets", totalSets);
		fields.put("haveRoms", haveRoms);
		fields.put("totalRoms", totalRoms);
		fields.put("haveDisks", haveDisks);
		fields.put("totalDisks", totalDisks);
		fields.put("created", created);
		fields.put("scanned", scanned);
		fields.put("fixed", fixed);
		stream.writeFields();
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		ObjectInputStream.GetField fields = stream.readFields();
		version = (String) fields.get("version", null);
		haveSets = (Long) fields.get("haveSets", null);
		totalSets = (Long) fields.get("totalSets", null);
		haveRoms = (Long) fields.get("haveRoms", null);
		totalRoms = (Long) fields.get("totalRoms", null);
		haveDisks = (Long) fields.get("haveDisks", null);
		totalDisks = (Long) fields.get("totalDisks", null);
		created = (Date) fields.get("created", null);
		scanned = (Date) fields.get("scanned", null);
		fixed = (Date) fields.get("fixed", null);
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