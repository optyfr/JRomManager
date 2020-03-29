package jrm.fullserver.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.io.FilenameUtils;

import jrm.misc.Log;
import jrm.misc.SystemSettings;
import lombok.NonNull;
import lombok.val;

class H2 extends DB
{
	public H2(SystemSettings settings)
	{
		super(settings);
	}

	final static boolean MV_STORE = false; 
	final static boolean MVCC = false; 

	/**
	 * Open connection to the H2 database
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
	@Override
	public Connection connectToDB(final String name, final boolean drop, final boolean safe, final boolean ifexists) throws IOException, SQLException
	{
		try
		{
			// STEP 1: Register JDBC driver
			Class.forName("org.h2.Driver");

			// STEP 2: Open a connection
			if(drop)
				dropDB(name);
			StringBuffer url = new StringBuffer("jdbc:h2:");
			url.append(getDBPath(name));
			if(ifexists)
				url.append(";IFEXISTS=TRUE");
/*			if(settings.isMultithreaded() && safe)
				url.append(";MULTI_THREADED=TRUE");*/
			if(MV_STORE)
				url.append(";MV_STORE=TRUE;MVCC="+(MVCC?"TRUE":"FALSE"));
			else
				url.append(";MV_STORE=FALSE");
	//		url.append(";IGNORECASE=true");
	//		url.append(";INIT=SET COLLATION ENGLISH STRENGTH PRIMARY");
			if(!safe)
			{
	//			url.append(";LOG=0;LOCK_MODE=0;UNDO_LOG=0;OPTIMIZE_DISTINCT=FALSE;OPTIMIZE_EVALUATABLE_SUBQUERIES=FALSE;OPTIMIZE_IN_LIST=FALSE;OPTIMIZE_IN_SELECT=FALSE;OPTIMIZE_INSERT_FROM_SELECT=FALSE;OPTIMIZE_OR=FALSE;OPTIMIZE_TWO_EQUALS=FALSE;OPTIMIZE_UPDATE=FALSE");
				url.append(";LOG=0;LOCK_MODE=0;UNDO_LOG=0");
//				url.append(";REUSE_SPACE=FALSE");
			}
			url.append(";MODE=MYSQL");
			Log.debug("Opening " + url);
			System.out.println("Opening " + url);
			return DriverManager.getConnection(url.toString(), "sa", "");
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Drop H2 Database by delete its files
	 * 
	 * @param name
	 *            name of the database to drop
	 * @throws IOException
	 *             as it may fails if Db is openened elsewhere
	 */
	@Override
	public void dropDB(String name) throws IOException
	{
		for(val f : getDBPath(name).getParent().toFile().listFiles(f -> f.getName().toLowerCase().startsWith(name.toLowerCase() + '.') && Files.isRegularFile(f.toPath())))
			f.delete();
	}

	@Override
	public boolean shouldDropDB(final @NonNull Path cpsPath, final Path capturePath) throws IOException
	{
		final String name = cpsPath.getFileName().toString();
		val dbpath =  getDBPath(name,true);
		if(!Files.exists(dbpath))	// pas de bd h2 => drop
			return true;
		if(!Files.exists(cpsPath))	// pas de source access mais une bd h2, pas d'import possible => pas de drop
			return false;
		val created = Files.exists(dbpath)?Files.getFileAttributeView(dbpath, BasicFileAttributeView.class).readAttributes().creationTime().toMillis():0L;
		if(cpsPath.toFile().lastModified() > created)	// drop si la source access est plus rÃ©cente
			return true;
		if(capturePath!=null && capturePath.toFile().lastModified() > created)	// drop si le capture access est plus rÃ©cente
			return true;
		return false;	// pas de drop dans les autres cas
	}
	
	private Path getDBPath(String name, final boolean full)
	{
		if(name.contains("%w"))
			name = name.replace("%w", getSettings().getWorkPath().toString());
		if(!Paths.get(name).isAbsolute())
		{
			Path basepath = settings.getWorkPath();
			String ext = FilenameUtils.getExtension(name).toLowerCase();
			if(ext.equalsIgnoreCase("db"))
			{
				ext = FilenameUtils.getExtension(FilenameUtils.getBaseName(name)).toLowerCase();
				if(ext.equalsIgnoreCase("mv") || ext.equalsIgnoreCase("h2"))
					ext = FilenameUtils.getExtension(FilenameUtils.getBaseName(FilenameUtils.getBaseName(name))).toLowerCase();
			}
			switch(ext)
			{
				case "sys":
					basepath = settings.getBasePath();
					break;
			}
			if(full && !name.endsWith(".db"))
				return basepath.resolve(name + (MV_STORE?".mv.db":".h2.db"));
			else if(!full && name.endsWith(".db"))
				return basepath.resolve(name.substring(0, name.length()-6));
			else
				return basepath.resolve(name);
		}
		else
		{
			if(full && !name.endsWith(".db"))
				return Paths.get(name + (MV_STORE?".mv.db":".h2.db"));
			else if(!full && name.endsWith(".db"))
				return Paths.get(name.substring(0, name.length()-6));
			else
				return Paths.get(name);
		}
	}

	private Path getDBPath(final String name)
	{
		return getDBPath(name, false);
	}

}