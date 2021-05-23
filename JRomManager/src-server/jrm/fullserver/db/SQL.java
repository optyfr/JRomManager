package jrm.fullserver.db;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Closeable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import jrm.misc.Log;
import jrm.misc.SystemSettings;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public abstract class SQL implements SQLUtils, Closeable
{
	protected @Getter Connection db;
	private final @Getter boolean shouldClose;
	private final @Getter SystemSettings settings;

	protected QueryRunner qryRunner = new QueryRunner();

	protected SQL(final boolean shouldClose, final SystemSettings settings)
	{
		this.db = null;
		this.shouldClose = shouldClose;
		this.settings = settings;
	}

	protected SQL(final @NonNull Connection db, final boolean shouldClose, final SystemSettings settings)
	{
		this.db = db;
		this.shouldClose = shouldClose;
		this.settings = settings;
	}

	/**
	 * Return a column from the first result row as a scalar value
	 * 
	 * @param <T>
	 *            the type of scalar value
	 * @param select
	 *            the SQL select string
	 * @return T
	 * @throws SQLException
	 */
	public <T> T getScalarValue(String select, Class<T> cls) throws SQLException
	{
		return qryRunner.query(db, select, new ScalarHandler<T>(1));
	}

	/**
	 * Return a column from the first result row as an integer
	 * 
	 * @param select
	 *            the sql request, the request column will have to be an integer
	 * @param col
	 *            the col from which to return the value
	 * @return the integer value or null if no result
	 * @throws SQLException
	 */
	protected Integer getIntValue(String select, int col) throws SQLException
	{
		return qryRunner.query(db, select, new ScalarHandler<Integer>(col));
	}

	/**
	 * Return a column from the first result row as a long
	 * 
	 * @param select
	 *            the sql request, the request column will have to be a long or an
	 *            integer
	 * @param col
	 *            the col from which to return the value
	 * @return the long value or null if no result
	 * @throws SQLException
	 */
	protected Long getLongValue(String select, int col) throws SQLException
	{
		return qryRunner.query(db, select, new ScalarHandler<Long>(col));
	}

	@Override
	public Long getLongValue(String select, Object... args) throws SQLException
	{
		if (!supportsArrayParams() && findArrayParam(args) != -1)
		{
			val selectRef = new AtomicReference<String>(select);
			val argsRef = new AtomicReference<Object[]>(args);
			convertArrayParams(selectRef, argsRef);
			select = selectRef.get();
			args = argsRef.get();
		}
		return qryRunner.query(db, select, new ScalarHandler<Long>(1), args);
	}

	@Override
	public Long count(String select, Object... args) throws SQLException
	{
		if (!supportsArrayParams() && findArrayParam(args) != -1)
		{
			val selectRef = new AtomicReference<String>(select);
			val argsRef = new AtomicReference<Object[]>(args);
			convertArrayParams(selectRef, argsRef);
			select = selectRef.get();
			args = argsRef.get();
		}
		return getLongValue("SELECT COUNT(*) FROM (" + select + ") AS T", args);
	}

	@Override
	public Long countTbl(String table, String context) throws SQLException
	{
		return getLongValue("SELECT COUNT(*) FROM " + getSQLTable(table, context));
	}

	/**
	 * Return the first column from the first row as an integer
	 * 
	 * @param select
	 *            the sql request, the first column will have to be an integer
	 * @return the integer value or null if no result
	 * @throws SQLException
	 */
	protected Integer getIntValue(String select) throws SQLException
	{
		return getIntValue(select, 1);
	}

	/**
	 * Return the first column from the first row as a long
	 * 
	 * @param select
	 *            the sql request, the first column will have to be an integer or a
	 *            long value
	 * @return the long value or null if no result
	 * @throws SQLException
	 */
	protected Long getLongValue(String select) throws SQLException
	{
		return getLongValue(select, 1);
	}

	/**
	 * return a column list of type T from a sql SELECT request
	 * 
	 * @param select
	 *            the SELECT request
	 * @param col
	 *            the column index to return (starting from 1)
	 * @return a list of type T, with T a compatible type for the requested column
	 * @throws SQLException
	 */
	@Override
	public <T> List<T> getColumnList(String select, int col) throws SQLException
	{
		return qryRunner.query(db, select, new ColumnListHandler<T>(col));
	}

	/**
	 * Create a schema (if not exists)
	 * 
	 * @param name
	 *            the name of the schema
	 * @param drop
	 *            if true, will drop an existing schema first
	 * @return the name of the schema
	 * @throws SQLException
	 */
	public synchronized String createSchema(String name, boolean drop) throws SQLException
	{
		if (name != null && db.getMetaData().supportsSchemasInDataManipulation())
		{
			if (drop)
				dropSchema(name);
			qryRunner.execute(db, "CREATE SCHEMA IF NOT EXISTS " + backquote(name));
		}
		return name;
	}

	/**
	 * Drop a schema (if exists)
	 * 
	 * @param name
	 *            the name of the schema
	 * @return the name of the schema
	 * @throws SQLException
	 */
	public String dropSchema(String name) throws SQLException
	{
		if (db.getMetaData().supportsSchemasInDataManipulation())
			qryRunner.execute(db, "DROP SCHEMA IF EXISTS " + backquote(name) + " CASCADE");
		return name;
	}

	/**
	 * transform a bean object into a map
	 * 
	 * @param bean
	 * @return the resulted map
	 */
	protected LinkedHashMap<String, Object> convertBeanToMap(Object bean)
	{
		return convertBeanToMap(bean, null);
	}

	/**
	 * transform a bean object into a map
	 * 
	 * @param bean
	 *            the bean object source
	 * @param columns
	 *            the optional columns name to extract, if none provided all from
	 *            bean will be added
	 * @return the resulted map
	 */
	protected LinkedHashMap<String, Object> convertBeanToMap(final Object bean, Set<String> columns)
	{
		val set = new LinkedHashMap<String, Object>();
		try
		{
			if (columns != null)
				columns = columns.stream().map(Introspector::decapitalize).collect(Collectors.toSet());
			for (val prop : Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors())
			{
				if (columns != null)
				{
					if (columns.contains(prop.getName()))
						set.put(prop.getName(), prop.getReadMethod().invoke(bean, new Object[0]));
				}
				else
				{
					if (prop.getWriteMethod() != null)
						set.put(prop.getName(), prop.getReadMethod().invoke(bean, new Object[0]));
				}
			}
		}
		catch (IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			Log.warn(e.getMessage());
		}
		return set;
	}

	/**
	 * Update a bean object from a map of key/value. keys must correspond to a
	 * variable with the same name in the the bean class
	 * 
	 * @param bean
	 *            the bean to update
	 * @param set
	 *            the set of key/value pairs to inject
	 */
	protected void updateBeanFromMap(final Object bean, Map<String, Object> set)
	{
		try
		{
			Map<String, PropertyDescriptor> descriptors = Stream.of(Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors()).collect(Collectors.toMap(e -> Introspector.decapitalize(e.getName()), e -> e));
			set.forEach((n, v) -> {
				try
				{
					descriptors.get(Introspector.decapitalize(n)).getWriteMethod().invoke(bean, v);
				}
				catch (Exception e1)
				{
					Log.err(e1.getMessage(), e1);
				}
			});
		}
		catch (IntrospectionException e1)
		{
			Log.err(e1.getMessage(), e1);
		}
	}

	/**
	 * Link a schema from another database This method is currently very slow
	 * 
	 * @param otherdb
	 * @param schema
	 * @throws SQLException
	 */
	public void linkSchema(Connection otherdb, String schema) throws SQLException
	{
		qryRunner.execute(db, "CALL LINK_SCHEMA(?,?,?,?,?,?)", schema, "", otherdb.getMetaData().getURL(), otherdb.getMetaData().getUserName(), "", schema);
	}


	/**
	 * Will tell if a request will return 0 or more results without doing a count(*)
	 * or reading all the resultset
	 * 
	 * @param select
	 *            the sql select to test
	 * @param args
	 *            the optional arguments in case of a prepared statement
	 * @return true if there is at least one record, otherwise false
	 * @throws SQLException
	 */
	protected boolean hasResult(final String select, Object... args) throws SQLException
	{
		return qryRunner.query(db, select, new ScalarHandler<Object>(1), args) != null;
	}

	/**
	 * will return a list of map converted from a resultset after querying an SQL
	 * SELECT request
	 * 
	 * @param select
	 *            the select request
	 * @param args
	 *            the optional args if it's a prepared statement
	 * @return the list of maps corresponding to the resultset
	 * @throws SQLException
	 */
	@Override
	public List<Map<String, Object>> query(String select, Object... args) throws SQLException
	{
		if (!supportsArrayParams() && findArrayParam(args) != -1)
		{
			val selectRef = new AtomicReference<String>(select);
			val argsRef = new AtomicReference<Object[]>(args);
			convertArrayParams(selectRef, argsRef);
			select = selectRef.get();
			args = argsRef.get();
		}
		return qryRunner.query(db, select, new MapListHandler(), args);
	}

	/**
	 * rempli un bean ou une bean list T avec une requete de type select (si c'est
	 * un bean et pas une liste, alors ne lit que la premiere ligne du resultset)
	 * 
	 * @param <T>
	 *            le type de bean attendu
	 * @param select
	 *            la requete sql select
	 * @param beanListHandler
	 *            le gestionnaire de bean (ou du bean list) dÃ©rivÃ© d'un
	 *            {@link ResultSetHandler}
	 * @param args
	 *            les paramÃ¨tres optionnels du preparedStatement sous jacent
	 * @return le bean T resultat
	 * @throws SQLException
	 */
	public <T> T queryHandler(String select, ResultSetHandler<T> beanListHandler, Object... args) throws SQLException
	{
		if (!supportsArrayParams() && findArrayParam(args) != -1)
		{
			val selectRef = new AtomicReference<String>(select);
			val argsRef = new AtomicReference<Object[]>(args);
			convertArrayParams(selectRef, argsRef);
			select = selectRef.get();
			args = argsRef.get();
		}
		return qryRunner.query(db, select, beanListHandler, args);
	}

	@Override
	public Map<String, Object> queryFirst(String select, Object... args) throws SQLException
	{
		if (!select.contains(" LIMIT "))
			select += " LIMIT 1";
		if (!supportsArrayParams() && findArrayParam(args) != -1)
		{
			val selectRef = new AtomicReference<String>(select);
			val argsRef = new AtomicReference<Object[]>(args);
			convertArrayParams(selectRef, argsRef);
			select = selectRef.get();
			args = argsRef.get();
		}
		return qryRunner.query(db, select, new MapHandler(), args);
	}

	@Override
	public int update(String update, Object... args) throws SQLException
	{
		if (!supportsArrayParams() && findArrayParam(args) != -1)
		{
			val updateRef = new AtomicReference<String>(update);
			val argsRef = new AtomicReference<Object[]>(args);
			convertArrayParams(updateRef, argsRef);
			update = updateRef.get();
			args = argsRef.get();
		}
		return qryRunner.update(db, update, args);
	}


	public void insert(String table, String context, final LinkedHashMap<String, Object> toset) throws SQLException
	{
		qryRunner.update(db, "INSERT INTO " + getSQLTable(table, context) + " (" + makeCols(toset.keySet()) + ") VALUES(" + appendParam(toset.size()) + ")", toset.values().toArray());
	}

	private int findArrayParam(Object[] args)
	{
		int pos = -1;
		if (args != null)
			for (var i = 0; i < args.length; i++)
				if (args[i] != null && args[i].getClass().isArray())
					pos = i;
		return pos;
	}

	private void convertArrayParams(AtomicReference<String> queryRef, AtomicReference<Object[]> argsRef)
	{
		Object[] args = argsRef.get();
		String query = queryRef.get();
		int pos;
		if(args!=null) while (-1 != (pos = findArrayParam(args)))
		{
			final var arrlen = Array.getLength(args[pos]);
			final var newargs = new Object[args.length - 1 + arrlen];
			System.arraycopy(args, 0, newargs, 0, pos);
			for (var i = 0; i < arrlen; i++)
				newargs[i + pos] = Array.get(args[pos], i);
			for (var i = pos + 1; i < args.length; i++)
				newargs[i - 1 + arrlen] = args[i];
			query = query.replaceFirst("=\\s*?ANY\\(\\?\\)", " IN(" + appendParam(arrlen) + ")");
			args = newargs;
		}
		argsRef.set(args);
		queryRef.set(query);
	}

	@Override
	public void dropTable(final String table) throws SQLException
	{
		qryRunner.update(db, "DROP TABLE IF EXISTS " + table);
	}

	@Override
	public void close()
	{
		if (shouldClose)
			try
			{
				if (!db.isClosed())
				{
					Log.info("will close " + db.getMetaData().getURL());
					db.close();
				}
			}
			catch (SQLException e)
			{
				// ignore
			}
	}

	public boolean supportsNullsFirst() throws SQLException
	{
		return db.getMetaData().getDatabaseProductName().equals("H2");
	}

	public int getMaxIndexLength() throws SQLException
	{
		return db.getMetaData().getMaxIndexLength();
	}

	public boolean supportsMultipleTablesUpdate() throws SQLException
	{
		return !db.getMetaData().getDatabaseProductName().equals("H2");
	}

	public boolean supportsReplace() throws SQLException
	{
		return !db.getMetaData().getDatabaseProductName().equals("H2");
	}

	public boolean supportsDump() throws SQLException
	{
		return db.getMetaData().getDatabaseProductName().equals("H2");
	}

	public boolean supportsArrayParams() throws SQLException
	{
		return db.getMetaData().getDatabaseProductName().equals("H2");
	}

	public boolean supportsInsertIgnore() throws SQLException
	{
		return !db.getMetaData().getDatabaseProductName().equals("H2");
	}
}
