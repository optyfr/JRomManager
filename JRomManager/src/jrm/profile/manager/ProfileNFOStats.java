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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * Contains statistics data to be (manually) serialized 
 * @author optyfr
 */
public final @Data class ProfileNFOStats implements Serializable
{
	private static final String FIXED_STR = "fixed";

	private static final String SCANNED_STR = "scanned";

	private static final String CREATED_STR = "created";

	private static final String TOTAL_DISKS_STR = "totalDisks";

	private static final String HAVE_DISKS_STR = "haveDisks";

	private static final String TOTAL_ROMS_STR = "totalRoms";

	private static final String HAVE_ROMS_STR = "haveRoms";

	private static final String TOTAL_SETS_STR = "totalSets";

	private static final String HAVE_SETS_STR = "haveSets";

	private static final String VERSION_STR = "version";

	private static final long serialVersionUID = 2L;

	/**
	 * The Mame current version
	 */
	private String version = null;
	/**
	 * Number of Sets we own
	 */
	private Long haveSets = null;
	/**
	 * Number of Sets in the profile
	 */
	private Long totalSets = null;
	/**
	 * Number of Roms we own
	 */
	private Long haveRoms = null;
	/**
	 * Number of Roms in the profile
	 */
	private Long totalRoms = null;
	/**
	 * Number of Disks we own
	 */
	private Long haveDisks = null;
	/**
	 * Number of Disks in the profile
	 */
	private Long totalDisks = null;
	/**
	 * Profile creation date
	 */
	private Date created = null;
	/**
	 * Profile last scan date
	 */
	private Date scanned = null;
	/**
	 * Profile last fix date
	 */
	private Date fixed = null;

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
	private static final ObjectStreamField[] serialPersistentFields = {	//NOSONAR
			new ObjectStreamField(VERSION_STR, String.class), //$NON-NLS-1$
			new ObjectStreamField(HAVE_SETS_STR, Long.class), //$NON-NLS-1$
			new ObjectStreamField(TOTAL_SETS_STR, Long.class), //$NON-NLS-1$
			new ObjectStreamField(HAVE_ROMS_STR, Long.class), //$NON-NLS-1$
			new ObjectStreamField(TOTAL_ROMS_STR, Long.class), //$NON-NLS-1$
			new ObjectStreamField(HAVE_DISKS_STR, Long.class), //$NON-NLS-1$
			new ObjectStreamField(TOTAL_DISKS_STR, Long.class), //$NON-NLS-1$
			new ObjectStreamField(CREATED_STR, Date.class), //$NON-NLS-1$
			new ObjectStreamField(SCANNED_STR, Date.class), //$NON-NLS-1$
			new ObjectStreamField(FIXED_STR, Date.class), //$NON-NLS-1$
	};

	/**
	 * Manually write serialization
	 * @param stream the destination {@link ObjectOutputStream}
	 * @throws IOException
	 */
	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put(VERSION_STR, version); //$NON-NLS-1$
		fields.put(HAVE_SETS_STR, haveSets); //$NON-NLS-1$
		fields.put(TOTAL_SETS_STR, totalSets); //$NON-NLS-1$
		fields.put(HAVE_ROMS_STR, haveRoms); //$NON-NLS-1$
		fields.put(TOTAL_ROMS_STR, totalRoms); //$NON-NLS-1$
		fields.put(HAVE_DISKS_STR, haveDisks); //$NON-NLS-1$
		fields.put(TOTAL_DISKS_STR, totalDisks); //$NON-NLS-1$
		fields.put(CREATED_STR, created); //$NON-NLS-1$
		fields.put(SCANNED_STR, scanned); //$NON-NLS-1$
		fields.put(FIXED_STR, fixed); //$NON-NLS-1$
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
		version = (String) fields.get(VERSION_STR, null); //$NON-NLS-1$
		haveSets = (Long) fields.get(HAVE_SETS_STR, null); //$NON-NLS-1$
		totalSets = (Long) fields.get(TOTAL_SETS_STR, null); //$NON-NLS-1$
		haveRoms = (Long) fields.get(HAVE_ROMS_STR, null); //$NON-NLS-1$
		totalRoms = (Long) fields.get(TOTAL_ROMS_STR, null); //$NON-NLS-1$
		haveDisks = (Long) fields.get(HAVE_DISKS_STR, null); //$NON-NLS-1$
		totalDisks = (Long) fields.get(TOTAL_DISKS_STR, null); //$NON-NLS-1$
		created = (Date) fields.get(CREATED_STR, null); //$NON-NLS-1$
		scanned = (Date) fields.get(SCANNED_STR, null); //$NON-NLS-1$
		fixed = (Date) fields.get(FIXED_STR, null); //$NON-NLS-1$
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