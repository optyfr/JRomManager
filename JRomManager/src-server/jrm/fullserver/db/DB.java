package jrm.fullserver.db;


import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import jrm.misc.SystemSettings;
import lombok.Getter;

public abstract class DB
{
//	private static final HashMap<String, DB> instances = new HashMap<>();
	
	protected @Getter SystemSettings settings; 
	
	protected DB(SystemSettings settings)
	{
		this.settings = settings;	
	}
	
	public static DB getInstance(SystemSettings settings)
	{
		try
		{
			return getInstance(settings.getDBClass(), settings);
		}
		catch(ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static DB getInstance(String cls, SystemSettings settings) throws ClassNotFoundException
	{
		return getInstance(Class.forName(cls).asSubclass(DB.class), settings);
	}
	
//	@SuppressWarnings("unchecked")
	public static <T extends DB> T getInstance(Class<T> cls, SystemSettings settings)
	{
		try
		{
		//	if(!instances.containsKey(cls.getCanonicalName()))
		//		instances.put(cls.getCanonicalName(), cls.getConstructor(Settings.class).newInstance(settings));
		//	return (T)instances.get(cls.getCanonicalName());
			return cls.getConstructor(SystemSettings.class).newInstance(settings);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Open connection to the database
	 * 
	 * @param name
	 *            the name the database to open
	 * @param drop
	 *            if true, delete the database before
	 * @return a {@link Connection} object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public abstract Connection connectToDB(final String name, final boolean drop, final boolean safe, boolean ifexists) throws IOException, SQLException;

	public Connection connectToDB(final String name, final boolean drop, final boolean safe) throws IOException, SQLException
	{
		return connectToDB(name, drop, safe, false);
	}

	
	/**
	 * Should we need to rebuild (or create) database because access database has changed?
	 * 
	 * @param cpsPath
	 *            the CPS access file (can't be null, but can be non-existent)
	 * @param capturePath
	 *            the Capture access file (may be null or non-existent)
	 * @return a boolean true if yes, false if no. if cpsPath does not exists but database exists, then it should be false
	 * @throws IOException
	 *             on error while testing
	 */
	public abstract boolean shouldDropDB(final Path cpsPath, final Path capturePath) throws IOException;

	/**
	 * Drop Database
	 * 
	 * @param name
	 *            name of the database to drop
	 * @throws IOException
	 *             as it may fails if Db is openened elsewhere
	 */
	public abstract void dropDB(String name) throws IOException;
	
}