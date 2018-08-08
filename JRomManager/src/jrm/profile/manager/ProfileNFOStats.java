/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.manager;

import java.io.*;
import java.util.Date;

/**
 * Contains statistics data to be (manually) serialized 
 * @author optyfr
 */
public final class ProfileNFOStats implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * The Mame current version
	 */
	public String version = null;
	/**
	 * Number of Sets we own
	 */
	public Long haveSets = null;
	/**
	 * Number of Sets in the profile
	 */
	public Long totalSets = null;
	/**
	 * Number of Roms we own
	 */
	public Long haveRoms = null;
	/**
	 * Number of Roms in the profile
	 */
	public Long totalRoms = null;
	/**
	 * Number of Disks we own
	 */
	public Long haveDisks = null;
	/**
	 * Number of Disks in the profile
	 */
	public Long totalDisks = null;
	/**
	 * Profile creation date
	 */
	public Date created = null;
	/**
	 * Profile last scan date
	 */
	public Date scanned = null;
	/**
	 * Profile last fix date
	 */
	public Date fixed = null;

	/**
	 * fields declaration for manual serialization
	 * @serialField version String version returned from main dat
	 * @serialField haveSets Long number of sets we own
	 * @serialField totalsSets Long number of sets in dats
	 * @serialField haveRoms Long number of roms we own
	 * @serialField totalRoms Long number of roms in dats
	 * @serialField haveDisks Long number of disks we own
	 * @serialField totalDisks Long number of disks in dats
	 * @serialField created Date when this profile was created
	 * @serialField scanned Date when this profile was last scanned 
	 * @serialField fixed Date when this profile was last fixed
	 */
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

	/**
	 * Manually write serialization
	 * @param stream the destination {@link ObjectOutputStream}
	 * @throws IOException
	 */
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

	/**
	 * Manually read serialization
	 * @param stream the destination {@link ObjectInputStream}
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
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

	/**
	 * Resets stats data
	 */
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