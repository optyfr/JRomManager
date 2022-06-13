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

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Optional;

import jrm.locale.Messages;
import jrm.misc.Log;
import lombok.Getter;
import lombok.Setter;

/**
 * Contains all mame related files informations
 * @author optyfr
 */
public final class ProfileNFOMame implements Serializable
{
	private static final String FILESL_STR = "filesl";

	private static final String FILEROMS_STR = "fileroms";

	private static final String SL_STR = "sl";

	private static final String MODIFIED_STR = "modified";

	private static final String FILE_STR = "file";

	private static final long serialVersionUID = 2L;

	/**
	 * The mame executable file
	 */
	private File file = null;
	/**
	 * The last mame update date
	 */
	private Long modified = 0L;
	/**
	 * Whether it contains a software list or not
	 */
	private boolean sl = false;
	/**
	 * The ROMs Dat file
	 */
	private @Getter @Setter File fileroms = null;
	/**
	 * The Software List Dat file
	 */
	private @Getter @Setter File filesl = null;

	/**
	 * fields declaration for manual serialization
	 * @serialField file File the mame file linked to the profile
	 * @serialField modified Long the last mame import date
	 * @serialField sl Boolean software list flag
	 * @serialField fileroms File dat file for roms
	 * @serialField filesl File dat file for software lists
	 */
	private static final ObjectStreamField[] serialPersistentFields = {	//NOSONAR
			new ObjectStreamField(FILE_STR, File.class), //$NON-NLS-1$
			new ObjectStreamField(MODIFIED_STR, Long.class), //$NON-NLS-1$
			new ObjectStreamField(SL_STR, Boolean.TYPE), //$NON-NLS-1$
			new ObjectStreamField(FILEROMS_STR, File.class), //$NON-NLS-1$
			new ObjectStreamField(FILESL_STR, File.class), //$NON-NLS-1$
	};

	/**
	 * Manually write serialization
	 * @param stream the destination {@link ObjectOutputStream}
	 * @throws IOException
	 */
	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final var fields = stream.putFields();
		fields.put(FILE_STR, file); //$NON-NLS-1$
		fields.put(MODIFIED_STR, modified); //$NON-NLS-1$
		fields.put(SL_STR, sl); //$NON-NLS-1$
		fields.put(FILEROMS_STR, fileroms); //$NON-NLS-1$
		fields.put(FILESL_STR, filesl); //$NON-NLS-1$
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
		file = (File) fields.get(FILE_STR, null); //$NON-NLS-1$
		modified = Optional.ofNullable((Long) fields.get(MODIFIED_STR, (Long)0L)).orElse(0L); //$NON-NLS-1$
		sl = fields.get(SL_STR, false); //$NON-NLS-1$
		fileroms = (File)fields.get(FILEROMS_STR, null); //$NON-NLS-1$
		filesl = (File)fields.get(FILESL_STR, null); //$NON-NLS-1$
	}

	/**
	 * The Mame presence/update status (inner enum of {@link ProfileNFOMame})
	 * @author optyfr
	 */
	public enum MameStatus
	{
		/**
		 * We don't known anything about Mame status (no attached file)
		 */
		UNKNOWN(Messages.getString("ProfileNFOMame.Unknown")), //$NON-NLS-1$
		/**
		 * Mame is present and up to date
		 */
		UPTODATE(Messages.getString("ProfileNFOMame.UpToDate")), //$NON-NLS-1$
		/**
		 * Mame is present but need to be updated
		 */
		NEEDUPDATE(Messages.getString("ProfileNFOMame.NeedUpdate")), //$NON-NLS-1$
		/**
		 * Mame was not found (has been removed from it initial path) 
		 */
		NOTFOUND(Messages.getString("ProfileNFOMame.NotFound")); //$NON-NLS-1$

		/**
		 * The translated message corresponding to the current status
		 */
		private final String msg;

		/**
		 * The internal constructor for that enum type
		 * @param msg the human readable message string
		 */
		private MameStatus(final String msg)
		{
			this.msg = msg;
		}

		/**
		 * get the human readable message from this enum status
		 * @return a human readable string message
		 */
		public String getMsg()
		{
			return msg;
		}
	}

	/**
	 * Attach the mame executable file path and initialize the internal modification date
	 * @param mame the executable file
	 * @param sl true if software list is requested
	 */
	public void set(final File mame, final boolean sl)
	{
		if(mame.exists())
		{
			file = mame;
			modified = mame.lastModified();
			this.sl = sl;
		}
	}

	/**
	 * Determine the Mame status
	 * @return a {@link MameStatus} status about Mame
	 */
	public MameStatus getStatus()
	{
		if(file != null)
		{
			if(file.exists())
			{
				if(file.lastModified() > modified)
					return MameStatus.NEEDUPDATE;
				if(!fileroms.exists())
					return MameStatus.NEEDUPDATE;
				if(isSL() && !filesl.exists())
					return MameStatus.NEEDUPDATE;
				return MameStatus.UPTODATE;
			}
			return MameStatus.NOTFOUND;
		}
		return MameStatus.UNKNOWN;
	}

	/**
	 * get the Mame executable file
	 * @return return a {@link File} to Mame executable or {@code null} if not attached
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * update the internal modification date with the current Mame file modification date
	 */
	public void setUpdated()
	{
		modified = file.lastModified();
	}

	/**
	 * Relocate the Mame file executable
	 * @param newFile the new Mame file path {@link File}
	 * @return the status of the Mame file new location
	 */
	public MameStatus relocate(final File newFile)
	{
		if(/*file != null && */newFile != null)
		{
			if(newFile.exists())
			{
				file = newFile;
				if(file.lastModified() > modified)
					return MameStatus.NEEDUPDATE;
				return MameStatus.UPTODATE;
			}
			return MameStatus.NOTFOUND;
		}
		return MameStatus.UNKNOWN;
	}

	/**
	 * Is the software list is requested
	 * @return true if the software list is requested, otherwise false
	 */
	public boolean isSL()
	{
		return sl;
	}

	/**
	 * Remove the attached Dat files
	 */
	public void delete()
	{
		try
		{
			if(fileroms!=null)
				Files.deleteIfExists(fileroms.toPath());
			if(filesl!=null)
				Files.deleteIfExists(filesl.toPath());
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}
}
