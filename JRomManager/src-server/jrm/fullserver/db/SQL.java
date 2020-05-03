package jrm.fullserver.db;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import jrm.misc.Log;
import jrm.misc.SystemSettings;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public abstract class SQL implements SQLUtils, Closeable
{
	protected @NonNull @Getter Connection db;
	private final @Getter boolean shouldClose;
	private final @Getter SystemSettings settings;
	
	protected QueryRunner qryRunner = new QueryRunner();

	public SQL(final boolean shouldClose, final SystemSettings settings)
	{
		this.shouldClose = shouldClose;
		this.settings = settings;
	}

	/**
	 * Return a column from the first result row as a scalar value
	 * @param <T> the type of scalar value
	 * @param select the SQL select string
	 * @return T
	 * @throws SQLException
	 */
	public <T> T getScalarValue(String select, Class<T> cls) throws SQLException
	{
		return qryRunner.query(db, select, new ScalarHandler<T>(1));
	}

	/**
	 * Return a column from the first result row as an integer
	 * @param select the sql request, the request column will have to be an integer
	 * @param col the col from which to return the value
	 * @return the integer value or null if no result
	 * @throws SQLException
	 */
	protected Integer getIntValue(String select, int col) throws SQLException
	{
		return qryRunner.query(db, select, new ScalarHandler<Integer>(col));
	}
	
	/**
	 * Return a column from the first result row as a long
	 * @param select the sql request, the request column will have to be a long or an integer
	 * @param col the col from which to return the value
	 * @return the long value or null if no result
	 * @throws SQLException
	 */
	protected Long getLongValue(String select, int col) throws SQLException
	{
		return qryRunner.query(db, select, new ScalarHandler<Long>(col));
	}
	
	@Override
	public Long getLongValue(String select, Object...args) throws SQLException
	{
		if(!supportsArrayParams() && findArrayParam(args) != -1)
		{
			val select_ref = new AtomicReference<String>(select);
			val args_ref = new AtomicReference<Object[]>(args);
			convertArrayParams(select_ref, args_ref);
			select = select_ref.get();
			args = args_ref.get();
		}
		return qryRunner.query(db, select, new ScalarHandler<Long>(1), args);
	}

	@Override
	public Long count(String select, Object... args) throws SQLException
	{
		if(!supportsArrayParams() && findArrayParam(args) != -1)
		{
			val select_ref = new AtomicReference<String>(select);
			val args_ref = new AtomicReference<Object[]>(args);
			convertArrayParams(select_ref, args_ref);
			select = select_ref.get();
			args = args_ref.get();
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
	 * @param select the sql request, the first column will have to be an integer
	 * @return the integer value or null if no result
	 * @throws SQLException
	 */
	protected Integer getIntValue(String select) throws SQLException
	{
		return getIntValue(select, 1);
	}
	
	/**
	 * Return the first column from the first row as a long
	 * @param select the sql request, the first column will have to be an integer or a long value
	 * @return the long value or null if no result
	 * @throws SQLException
	 */
	protected Long getLongValue(String select) throws SQLException
	{
		return getLongValue(select, 1);
	}
	
	/**
	 * return a column list of type T from a sql SELECT request
	 * @param select the SELECT request
	 * @param col the column index to return (starting from 1)
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
	 * @param name the name of the schema
	 * @param drop if true, will drop an existing schema first
	 * @return the name of the schema
	 * @throws SQLException
	 */
	public synchronized String createSchema(String name, boolean drop) throws SQLException
	{
		if(name != null)
		{
			if(db.getMetaData().supportsSchemasInDataManipulation())
			{
				if(drop) dropSchema(name);
				qryRunner.execute(db, "CREATE SCHEMA IF NOT EXISTS " + backquote(name));
			}
		}
		return name;
	}

	/**
	 * Drop a schema (if exists)
	 * @param name the name of the schema
	 * @return the name of the schema
	 * @throws SQLException
	 */
	public String dropSchema(String name) throws SQLException
	{
		if(db.getMetaData().supportsSchemasInDataManipulation())
			qryRunner.execute(db, "DROP SCHEMA IF EXISTS " + backquote(name) + " CASCADE");
		return name;
	}

	/**
	 * transform a bean object into a map
	 * @param bean
	 * @return the resulted map
	 */
	protected LinkedHashMap<String, Object> convertBeanToMap(Object bean)
	{
		return convertBeanToMap(bean, null);
	}

	/**
	 * transform a bean object into a map
	 * @param bean the bean object source
	 * @param columns the optional columns name to extract, if none provided all from bean will be added
	 * @return the resulted map
	 */
	protected LinkedHashMap<String, Object> convertBeanToMap(final Object bean, Set<String> columns)
	{
		val set = new LinkedHashMap<String, Object>();
		try
		{
			if(columns != null)
				columns = columns.stream().map(Introspector::decapitalize).collect(Collectors.toSet());
			for(val prop : Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors())
			{
				if(columns != null)
				{
					if(columns.contains(prop.getName()))
						set.put(prop.getName(), prop.getReadMethod().invoke(bean, new Object[0]));
				}
				else
				{
					if(prop.getWriteMethod() != null)
						set.put(prop.getName(), prop.getReadMethod().invoke(bean, new Object[0]));
				}
			}
		}
		catch(IntrospectionException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			Log.warn(e.getMessage());
		}
		return set;
	}
	
	/**
	 * Update a bean object from a map of key/value. keys must correspond to a variable with the same name in the the bean class
	 * @param bean the bean to update
	 * @param set the set of key/value pairs to inject
	 */
	protected void updateBeanFromMap(final Object bean, Map<String, Object> set)
	{
		try
		{
			Map<String, PropertyDescriptor> descriptors = Stream.of(Introspector.getBeanInfo(bean.getClass()).getPropertyDescriptors()).collect(Collectors.toMap(e -> Introspector.decapitalize(e.getName()), e -> e));
			set.forEach((n,v)->{
				try
				{
					descriptors.get(Introspector.decapitalize(n)).getWriteMethod().invoke(bean, v);
				}
				catch(Exception e1)
				{
					e1.printStackTrace();
				}
			});
		}
		catch(IntrospectionException e1)
		{
			e1.printStackTrace();
		}
	}
	
	/**
	 * Link a schema from another database
	 * This method is currently very slow
	 * @param otherdb
	 * @param schema
	 * @throws SQLException
	 */
	public void linkSchema(Connection otherdb, String schema) throws SQLException
	{
		qryRunner.execute(db, "CALL LINK_SCHEMA(?,?,?,?,?,?)", schema, "", otherdb.getMetaData().getURL(), otherdb.getMetaData().getUserName(), "", schema);
	}
	
	/**
	 * Link a table from another database
	 * This method is strangely slower than copying
	 * @param otherdb
	 * @param schema
	 * @param tname
	 * @throws SQLException
	 */
	public void linkTable(final Connection otherdb, final String schema, final String tname) throws SQLException
	{
		createSchema(schema, false);
		final String url = otherdb.getMetaData().getURL();
		final String user = otherdb.getMetaData().getUserName();
		final String pw = "";
		qryRunner.execute(db, String.format("CREATE LINKED TABLE " + getSQLTable(tname, schema) + " (%s,%s,%s,%s,%s,%s) READONLY",str(""),str(url),str(user),str(pw),str(schema),str(tname)));
	}

	/**
	 * Dump a table from another database, using script file generation/restoration
	 * @param otherdb
	 * @param path
	 * @param schema
	 * @param tname
	 * @throws SQLException
	 * @throws IOException
	 */
	public void dumpTable(final Connection otherdb, Path path, final String schema, final String tname, final boolean overwrite) throws IOException, SQLException
	{
		if(supportsDump())
		{
			Path dir  = settings.getWorkPath().resolve(schema);
			if(!Files.exists(dir))
				Files.createDirectories(dir);
			Path sqlfile = dir.resolve(tname+".sql");
			if(!Files.exists(sqlfile) || overwrite)
				qryRunner.execute(otherdb, String.format("SCRIPT NOSETTINGS TO %s CHARSET 'UTF-8' TABLE %s", str(sqlfile.toString()), getSQLTable(tname, schema)));
		}
	}
	
	
	/**
	 * Copy a table from another database, using script file generation/restoration
	 * @param otherdb
	 * @param path
	 * @param schema
	 * @param tname
	 * @throws SQLException
	 */
	public void copyTable(final Connection otherdb, Path path, final String schema, final String tname) throws SQLException
	{
		if(supportsDump())
		{
			Path dir  = settings.getWorkPath().resolve(schema);
			Path sqlfile = dir.resolve(tname + ".sql");
	//		if(!Files.exists(sqlfile))
	//			dumpTable(otherdb, path, schema, tname);
			qryRunner.execute(db, String.format("RUNSCRIPT FROM %s CHARSET 'UTF-8'", str(sqlfile.toString())));
		}
		else
		{
			qryRunner.update(db, "CREATE TABLE " + getSQLTable(tname, schema) + " LIKE " + backquote(otherdb.getCatalog()) + "." + getSQLTable(tname, schema));
			qryRunner.update(db, "INSERT INTO " + getSQLTable(tname, schema) + " SELECT * FROM " + backquote(otherdb.getCatalog()) + "." + getSQLTable(tname, schema));
		}
	}
	
	public void transferTable(final SQL otherdb, final String schema, final String tname) throws IOException, SQLException
	{
		if(supportsDump())
		{
			val sqlfile = Files.createTempFile(settings.getWorkPath(), null, ".sql");
			otherdb.qryRunner.execute(otherdb.db, String.format("SCRIPT NOSETTINGS TO %s CHARSET 'UTF-8' TABLE %s", str(sqlfile.toString()), getSQLTable(tname, schema)));
			if(Files.exists(sqlfile))
			{
				qryRunner.execute(db, String.format("RUNSCRIPT FROM %s CHARSET 'UTF-8'", str(sqlfile.toString())));
				Files.delete(sqlfile);
			}
		}
		else
		{
			qryRunner.update(db, "CREATE TABLE " + getSQLTable(tname, schema) + " LIKE " + backquote(otherdb.getDb().getCatalog()) + "." + getSQLTable(tname, schema));
			qryRunner.update(db, "INSERT INTO " + getSQLTable(tname, schema) + " SELECT * FROM " + backquote(otherdb.getDb().getCatalog()) + "." + getSQLTable(tname, schema));
		}
	}
	
	public void transferTable2(final SQL otherdb, final String schema, final String tname) throws IOException, SQLException
	{
		if(db.getMetaData().getDatabaseProductName().equals("H2"))
		{
			val sqlfile = Files.createTempFile(settings.getWorkPath(), null, ".csv");
			otherdb.update("CALL CSVWRITE(?,?)", sqlfile.toString(), "SELECT * FROM "+getSQLTable(tname, schema));
			if(Files.exists(sqlfile))
			{
				if(schema!=null)
					qryRunner.execute(db, "CREATE SCHEMA IF NOT EXISTS "+backquote(schema));
				qryRunner.execute(db, otherdb.getSQLCreate(schema, tname));
				qryRunner.execute(db, "INSERT INTO "+getSQLTable(tname,schema)+" SELECT * FROM CSVREAD("+str(sqlfile.toString())+")");
				Files.delete(sqlfile);
			}
		}
		else
		{
			qryRunner.update(db, "CREATE TABLE " + getSQLTable(tname, schema) + " AS SELECT * FROM " + backquote(otherdb.getDb().getCatalog()) + "." + getSQLTable(tname, schema));
		}
	}
	
	public void importCSV(final Path file, final String schema, final String tname, final List<Columns> cols) throws SQLException, IOException
	{
		if(Files.exists(file))
		{
			val header = cols.stream().map(c->str(c.getCOLUMN_NAME())).collect(Collectors.joining("||CHAR(9)||"));
			qryRunner.execute(db, "INSERT INTO "+getSQLTable(tname,schema)+" SELECT * FROM CSVREAD("+str(file.toString())+","+header+",'charset=UTF-8 escape=\\\" fieldDelimiter=\\\" null=NULL fieldSeparator='||CHAR(9))");
		}		
	}
	
	public void exportCSV(final Path file, final String schema, final String tname) throws SQLException, IOException
	{
		exportCSV(file, "*", getSQLTable(tname, schema), null);
	}
	
	public void exportCSV(final Path file, String select, final String from, String where) throws SQLException, IOException
	{
		if(select == null) select = "*";
		if(where == null) where = "";
		if(!where.isEmpty() && !where.toLowerCase().startsWith("where")) where = "where " + where;
		Files.deleteIfExists(file);
		if(db.getMetaData().getDatabaseProductName().equals("H2"))
			update("CALL CSVWRITE(?,?,STRINGDECODE('charset=UTF-8 escape=\\\" fieldDelimiter=\\\" null=NULL writeColumnHeader=false fieldSeparator=\\\\\t lineSeparator=\\\\\n'))", file.toString(), "SELECT " + select + " FROM " + from + " " + where);
		else
			update("SELECT " + select + " INTO OUTFILE " + str(file.toString(), true) + " CHARACTER SET 'utf8' FIELDS TERMINATED BY '\\t' OPTIONALLY ENCLOSED BY '\"' ESCAPED BY '' FROM " + from + " " + where);
	}
	
	public static @Data class Columns
	{
		private String TABLE_CAT;
		private String TABLE_SCHEM;
		private String TABLE_NAME;
		private String COLUMN_NAME;
		private int DATA_TYPE;
		private String TYPE_NAME;
		private int COLUMN_SIZE;
		private Integer DECIMAL_DIGITS;
		private int NULLABLE;
		private String COLUMN_DEF;
		private String IS_NULLABLE;
		private int CHAR_OCTET_LENGTH;
		private int ORDINAL_POSITION;
		private String SCOPE_CATALOG=null;
		private String SCOPE_SCHEMA=null;
		private String SCOPE_TABLE=null;
		private Short SOURCE_DATA_TYPE=null;
		private String IS_AUTOINCREMENT;
		private String IS_GENERATEDCOLUMN;
	}
	
	/**
	 * Get columns informations from a schema/table
	 * @param schema the schema (or null if none)
	 * @param table the table to return result
	 * @return
	 * @throws SQLException
	 */
	public List<Columns> getColumns(final String schema, final String table) throws SQLException
	{
		if(db.getMetaData().getDatabaseProductName().equals("H2"))
		{
			val handler = new ResultSetHandler<List<Columns>>()
			{
				@Override
				public List<Columns> handle(ResultSet rs) throws SQLException
				{
					List<Columns> list = new ArrayList<>();
					while(rs.next())
					{
						val col = new Columns();
						col.TABLE_CAT = rs.getString(1);
						col.TABLE_SCHEM = rs.getString(2);
						col.TABLE_NAME = rs.getString(3);
						col.COLUMN_NAME = rs.getString(4);
						col.ORDINAL_POSITION = rs.getInt(5);
						col.COLUMN_DEF = rs.getString(6);
						col.IS_NULLABLE = rs.getString(7);
						col.DATA_TYPE = rs.getInt(8);
						col.COLUMN_SIZE = rs.getInt(9);
						col.DECIMAL_DIGITS = rs.getInt(10);
						col.NULLABLE = rs.getInt(11);
						col.TYPE_NAME = rs.getString(12);
						col.CHAR_OCTET_LENGTH = rs.getInt(13);
						col.IS_AUTOINCREMENT = rs.getString(14) != null?"YES":"NO";
						col.IS_GENERATEDCOLUMN = rs.getBoolean(15)?"YES":"NO";
						list.add(col);
					}
					rs.close();
					return list;
				}
			};
			String cols = "TABLE_CATALOG,TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,ORDINAL_POSITION,COLUMN_DEFAULT,IS_NULLABLE,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH,NUMERIC_PRECISION,NULLABLE,TYPE_NAME,CHARACTER_OCTET_LENGTH,SEQUENCE_NAME,IS_COMPUTED";
			if(schema == null && table == null)
				return qryRunner.query(db, "SELECT " + cols + " FROM " + getSQLTable("COLUMNS", "INFORMATION_SCHEMA") + " ORDER BY ORDINAL_POSITION", 
						handler);
			else if(schema == null)
				return qryRunner.query(db, "SELECT " + cols + " FROM " + getSQLTable("COLUMNS", "INFORMATION_SCHEMA") + " WHERE `TABLE_NAME`=? ORDER BY ORDINAL_POSITION", 
						handler, table.toUpperCase());
			else if(table == null)
				return qryRunner.query(db, "SELECT " + cols + " FROM " + getSQLTable("COLUMNS", "INFORMATION_SCHEMA") + " WHERE `TABLE_SCHEMA`=? ORDER BY ORDINAL_POSITION", 
						handler, schema.toUpperCase());
			else
				return qryRunner.query(db, "SELECT " + cols + " FROM " + getSQLTable("COLUMNS", "INFORMATION_SCHEMA") + " WHERE `TABLE_SCHEMA`=? AND `TABLE_NAME`=? ORDER BY ORDINAL_POSITION", 
						handler, schema.toUpperCase(), table.toUpperCase());
		}
		else
		{
			BeanListHandler<Columns> rsh = new BeanListHandler<>(Columns.class);
			try(val rs = db.getMetaData().getColumns(null, schema!=null?schema.toUpperCase():null, table!=null?table.toUpperCase():null, "%"))
			{
				return rsh.handle(rs);
			}
		}
	}
	
	public List<String> getColumnNames(final String schema, final String table) throws SQLException
	{
		if(db.getMetaData().getDatabaseProductName().equals("H2"))
			return queryHandler("SELECT `COLUMN_NAME` FROM " + getSQLTable("COLUMNS", "INFORMATION_SCHEMA") + " WHERE `TABLE_SCHEMA`=? AND `TABLE_NAME`=?", new ColumnListHandler<String>(), schema.toUpperCase(), table.toUpperCase());
		return getColumns(schema, table).stream().map(Columns::getCOLUMN_NAME).collect(Collectors.toList());
	}	
	
	public String getSQLType(int type) throws SQLException
	{
		switch(type)
		{
			case Types.VARCHAR:
				if(db.getMetaData().getDatabaseProductName().equals("H2"))
					return "VARCHAR_IGNORECASE";
				return "VARCHAR";
			case Types.LONGVARCHAR:
				if(db.getMetaData().getDatabaseProductName().equals("MariaDB"))
					return "LONGTEXT";
				return "CLOB";
			case Types.CLOB:
				if(db.getMetaData().getDatabaseProductName().equals("MariaDB"))
					return "LONGTEXT";
				return "CLOB";
			case Types.LONGVARBINARY:
				return "BLOB";
			case Types.BLOB:
				return "BLOB";
			case Types.BIT:
			case Types.BOOLEAN:
				return "BOOLEAN";
			case Types.SMALLINT:
				return "SMALLINT";
			case Types.BIGINT:
				return "BIGINT";
			case Types.INTEGER:
				return "INTEGER";
			case Types.DOUBLE:
				return "DOUBLE";
			case Types.DATE:
				return "DATE";
			case Types.TIME:
				return "DATETIME";
			case Types.TIMESTAMP:
				return "TIMESTAMP";
		}
		return null;
	}

	public String getSQLCreate(final String schema, final String table) throws SQLException
	{
		return getSQLCreate(schema, table, getColumns(schema, table), getPrimaryKeys(schema, table));
	}
	
	public String getSQLCreate(final String schema, final String table, List<Columns> cols) throws SQLException
	{
		return getSQLCreate(schema, table, cols, null);
	}
	
	public String getSQLCreate(final String schema, final String table, List<Columns> cols, List<PrimaryKeys> pk) throws SQLException
	{
		val sql = new StringBuilder("CREATE TABLE ").append(getSQLTable(table, schema)).append('(');
		int  i = 0;
		for(val col : cols)
		{
			if(i > 0) sql.append(", ");
			sql.append(backquote(col.getCOLUMN_NAME()));
			switch(col.getDATA_TYPE())
			{
				case Types.VARCHAR:
					sql.append(' ').append(getSQLType(col.getDATA_TYPE())).append('(').append(col.getCOLUMN_SIZE()).append(')');
					break;
				case Types.LONGVARCHAR:
				case Types.CLOB:
				case Types.LONGVARBINARY:
				case Types.BLOB:
				case Types.BIT:
				case Types.BOOLEAN:
				case Types.SMALLINT:
				case Types.BIGINT:
				case Types.INTEGER:
				case Types.DOUBLE:
					sql.append(' ').append(getSQLType(col.getDATA_TYPE()));
					break;
				default:
					sql.append(' ').append(col.getTYPE_NAME());
					System.out.println(col.getTABLE_NAME()+'/'+col.getCOLUMN_NAME()+':'+col.getDATA_TYPE());
					break;
			}
			if(col.getNULLABLE()!=ResultSetMetaData.columnNullableUnknown)
				sql.append(col.getNULLABLE() == ResultSetMetaData.columnNullable?" NULL":" NOT NULL");
			if(col.getCOLUMN_DEF()!=null && !col.getIS_GENERATEDCOLUMN().equalsIgnoreCase("YES") && !col.getIS_AUTOINCREMENT().equalsIgnoreCase("YES"))
				sql.append(" DEFAULT ").append(col.getCOLUMN_DEF());
			i++;
		}
		if(pk!=null && !pk.isEmpty())
		{
			sql.append(", PRIMARY KEY (").append(pk.stream().map(p -> backquote(p.getCOLUMN_NAME())).collect(Collectors.joining(", "))).append(")");
		}
		return sql.append(')').toString();
	}
	
	public static @Data class Tables
	{
		private String TABLE_CAT;
		private String TABLE_SCHEM;
		private String TABLE_NAME;
		private String TABLE_TYPE;
	}
	
	
	public List<Tables> getTables(final String schema, final String tablepattern) throws SQLException
	{
		BeanListHandler<Tables> rsh = new BeanListHandler<>(Tables.class);
		try(val rs = db.getMetaData().getTables(null, schema, tablepattern, null))
		{
			return rsh.handle(rs);
		}
	}
	
	
	public static @Data class Indexes
	{
		private String TABLE_CAT;
		private String TABLE_SCHEM;
		private String TABLE_NAME;
		private boolean NON_UNIQUE;
		private String INDEX_QUALIFIER;
		private String INDEX_NAME;
		private short TYPE;
		private short ORDINAL_POSITION;
		private String COLUMN_NAME;
		private String ASC_OR_DESC;
		private int CARDINALITY;
		private int PAGES;
		private String FILTER_CONDITION;
	}
	
	public List<Indexes> getIndexes(final String schema, final String table) throws SQLException
	{
		BeanListHandler<Indexes> rsh = new BeanListHandler<>(Indexes.class);
		try(val rs = db.getMetaData().getIndexInfo(null, schema, table, false, false))
		{
			return rsh.handle(rs);
		}
	}
	
	public static @Data class PrimaryKeys
	{
		private String TABLE_CAT;
		private String TABLE_SCHEM;
		private String TABLE_NAME;
		private String COLUMN_NAME;
		private short KEY_SEQ;
		private String PK_NAME;
	}
	
	public List<PrimaryKeys> getPrimaryKeys(final String schema, final String table) throws SQLException
	{
		BeanListHandler<PrimaryKeys> rsh = new BeanListHandler<>(PrimaryKeys.class);
		try(val rs = db.getMetaData().getPrimaryKeys(null, schema!=null?schema.toUpperCase():null, table.toUpperCase()))
		{
			return rsh.handle(rs);
		}
	}
	
	/**
	 * Will tell if a request will return 0 or more results without doing a count(*) or reading all the resultset
	 * @param select the sql select to test
	 * @param args the optional arguments in case of a prepared statement
	 * @return true if there is at least one record, otherwise false
	 * @throws SQLException
	 */
	protected boolean hasResult(final String select, Object...args) throws SQLException
	{
		return qryRunner.query(db, select, new ScalarHandler<Object>(1), args) != null;
	}
	
	/**
	 * will return a list of map converted from a resultset after querying an SQL SELECT request
	 * @param select the select request
	 * @param args the optional args if it's a prepared statement
	 * @return the list of maps corresponding to the resultset
	 * @throws SQLException
	 */
	@Override
	public List<Map<String, Object>> query(String select, Object... args) throws SQLException
	{
		if(!supportsArrayParams() && findArrayParam(args) != -1)
		{
			val select_ref = new AtomicReference<String>(select);
			val args_ref = new AtomicReference<Object[]>(args);
			convertArrayParams(select_ref, args_ref);
			select = select_ref.get();
			args = args_ref.get();
		}
		return qryRunner.query(db, select, new MapListHandler(), args);
	}
	
	/**
	 * rempli un bean ou une bean list T avec une requete de type select (si c'est un bean et pas une liste, alors ne lit que la premiere ligne du resultset)
	 * @param <T> le type de bean attendu
	 * @param select la	 requete sql select 
	 * @param beanListHandler le gestionnaire de bean (ou du bean list) dÃ©rivÃ© d'un {@link ResultSetHandler}
	 * @param args les paramÃ¨tres optionnels du preparedStatement sous jacent
	 * @return le bean T resultat
	 * @throws SQLException
	 */
	public <T> T queryHandler(String select, ResultSetHandler<T> beanListHandler, Object... args) throws SQLException
	{
		if(!supportsArrayParams() && findArrayParam(args) != -1)
		{
			val select_ref = new AtomicReference<String>(select);
			val args_ref = new AtomicReference<Object[]>(args);
			convertArrayParams(select_ref, args_ref);
			select = select_ref.get();
			args = args_ref.get();
		}
		return qryRunner.query(db, select, beanListHandler, args);
	}
	
	@Override
	public Map<String, Object> queryFirst(String select, Object... args) throws SQLException
	{
		if(!select.contains(" LIMIT "))
			select += " LIMIT 1";
		if(!supportsArrayParams() && findArrayParam(args) != -1)
		{
			val select_ref = new AtomicReference<String>(select);
			val args_ref = new AtomicReference<Object[]>(args);
			convertArrayParams(select_ref, args_ref);
			select = select_ref.get();
			args = args_ref.get();
		}
		return qryRunner.query(db, select, new MapHandler(), args);
	}
	
	@Override
	public int update(String update, Object...args) throws SQLException
	{
		if(!supportsArrayParams() && findArrayParam(args) != -1)
		{
			val update_ref = new AtomicReference<String>(update);
			val args_ref = new AtomicReference<Object[]>(args);
			convertArrayParams(update_ref, args_ref);
			update = update_ref.get();
			args = args_ref.get();
		}
		return qryRunner.update(db, update, args);
	}
	
	public int insertBulk(final Statement stmt, final CharSequence into_table, final CharSequence into_columns, final CharSequence from_columns, final CharSequence from_tables, final CharSequence where) throws SQLException, IOException
	{
		if(db.getMetaData().getDatabaseProductName().equals("H2"))
			return stmt.executeUpdate("INSERT INTO " + into_table + " ("+into_columns+") SELECT " + from_columns + " FROM " + from_tables + where);
		else
		{
			val sqlfile = Files.createTempFile(settings.getWorkPath(), null, ".csv");
			try
			{
				Files.deleteIfExists(sqlfile);
				stmt.executeUpdate("SELECT " + from_columns + " INTO OUTFILE "+str(sqlfile.toString(),true)+" FROM " + from_tables + where);
				stmt.executeUpdate("ALTER TABLE " + into_table + " DISABLE KEYS");
				stmt.executeUpdate("LOCK TABLES " + into_table + " WRITE");
				int ret = stmt.executeUpdate("LOAD DATA INFILE " + str(sqlfile.toString(),true) + " INTO TABLE " + into_table + " (" + into_columns + ")");
				stmt.executeUpdate("UNLOCK TABLES");
				stmt.executeUpdate("ALTER TABLE " + into_table + " ENABLE KEYS");
				return ret;
			}
			finally
			{
				Files.deleteIfExists(sqlfile);
			}
		}
	}
	
	public void insert(String table, String context, final LinkedHashMap<String, Object> toset) throws SQLException
	{
		qryRunner.update(db, "INSERT INTO " + getSQLTable(table, context) + " (" + makeCols(toset.keySet()) + ") VALUES(" + appendParam(toset.size()) + ")", toset.values().toArray());
	}

	
	private int findArrayParam(Object[] args)
	{
		int pos = -1;
		if(args != null)
			for(int i = 0; i < args.length; i++)
				if(args[i]!=null)
					if(args[i].getClass().isArray())
						pos = i;
		return pos;
	}
	
	private void convertArrayParams(AtomicReference<String> query_ref, AtomicReference<Object[]> args_ref)
	{
		Object[] args = args_ref.get();
		String query = query_ref.get();
		int pos;
		while(-1 != (pos = findArrayParam(args)))
		{
			int arrlen = Array.getLength(args[pos]);
			Object[] newargs = new Object[args.length - 1 + arrlen];
			for(int i = 0; i < pos; i++)
				newargs[i] = args[i];
			for(int i = 0; i < arrlen; i++)
				newargs[i + pos] = Array.get(args[pos], i);
			for(int i = pos + 1; i < args.length; i++)
				newargs[i - 1 + arrlen] = args[i];
			query = query.replaceFirst("=\\s*?ANY\\(\\?\\)", " IN(" + appendParam(arrlen) + ")");
			args = newargs;
		}
		args_ref.set(args);
		query_ref.set(query);
	}
	
	@Override
	public void dropTable(final String table) throws SQLException
	{
		qryRunner.update(db, "DROP TABLE IF EXISTS " + table);
	}
	
	@Override
	public void close()
	{
		if(shouldClose) try
		{
			if(!db.isClosed())
			{
				System.out.println("will close "+db.getMetaData().getURL());
				db.close();
			}
		}
		catch(SQLException e)
		{
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