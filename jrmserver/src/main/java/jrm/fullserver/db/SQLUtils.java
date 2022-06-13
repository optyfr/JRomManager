package jrm.fullserver.db;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.lang3.math.NumberUtils;

public interface SQLUtils
{
	
	/**
	 * backquote a name
	 * @param name the name to backquote
	 * @return a {@link String} containing `<i>name</i>`
	 */
	default String backquote(String name)
	{
		if(name==null)
			return name;
		return "`" + name + "`";
	}

	default void append(StringBuilder str, final String separator, CharSequence toAppend)
	{
		if(toAppend.length()>0)
		{
			if(str.length()>0)
				str.append(separator);
			str.append(toAppend);
		}
	}
	
	default void appendComma(StringBuilder str, CharSequence toAppend)
	{
		if(str.length()>0)
			str.append(", ");
		str.append(toAppend);
	}
	
	default void prependComma(StringBuilder str, CharSequence toPrepend)
	{
		if(str.length()>0)
			str.insert(0, ", ");
		str.insert(0, toPrepend);
	}
	
	default CharSequence appendParam(StringBuilder str, int count)
	{
		if(str == null)
			str = new StringBuilder();
		for(var i = 0; i < count; i++)
			appendComma(str, "?");
		return str;
	}
	
	default CharSequence appendParam(int count)
	{
		return appendParam(null, count);
	}

	default CharSequence makeCols(Collection<String> cols, boolean withParenthesis)
	{
		final var set = new StringBuilder();
		cols.forEach(col-> appendComma(set, backquote(col)));
		if(withParenthesis && cols.size()>1)
			set.insert(0, '(').append(')');
		return set;
	}
	
	default <T> Iterable<T> getIterable(Collection<T> coll) 
	{ 
		return coll::iterator; 
	} 
	
	default <T> Iterable<T> getReversedIterable(Collection<T> coll) 
	{
		return () -> new LinkedList<>(coll).descendingIterator();
	} 
	
	default CharSequence makeCols(Iterable<String> cols)
	{
		final var set = new StringBuilder();
		for(final var col : cols)
			appendComma(set, backquote(col));
		return set;
	}
	
	default CharSequence makeCols(Collection<String> cols)
	{
		return makeCols(cols, false);
	}

	default CharSequence makeSet(Collection<String> cols)
	{
		final var set = new StringBuilder();
		cols.forEach(col -> appendComma(set, backquote(col) + "=?"));
		return set;
	}
	
	default CharSequence makeSet(LinkedHashMap<String, Object> map)
	{
		final var set = new StringBuilder();
		map.forEach((col, value) -> appendComma(set, backquote(col) + "=?"));
		return set;
	}
	
	default CharSequence makeSet(Set<Entry<String,Object>> map)
	{
		final var set = new StringBuilder();
		map.forEach(entry -> appendComma(set, backquote(entry.getKey()) + "=?"));
		return set;
	}
	
	default String notNull(boolean required)
	{
		return required?"NOT NULL":"NULL";
	}
	
	default String getSQLValue(String value, boolean isStr, boolean notNull)
	{
		if(isStr)
		{
			if(value == null)
				return notNull?"''":"NULL";
			return "'" + value + "'";
		}
		if(value == null || value.length()==0)
			return notNull?"0":"NULL";
		return value;
	}
	
	public Connection getDb();
	
	String getContext();
	
	default String getSQLTable(String table) throws SQLException
	{
		return getSQLTable(table, getContext());
	}

	default String getSQLTable(String table, String context) throws SQLException
	{
		if(context!=null && getDb().getMetaData().supportsSchemasInDataManipulation())
			return backquote(context)+'.'+backquote(table);
		return backquote(table);
	}
	
	default String getDefaultValue(String value, JDBCType type, boolean notNull)
	{
		switch(type)
		{
			case VARCHAR:
			case LONGNVARCHAR:
			case LONGVARCHAR:
			case CLOB:
			case CHAR:
			case NCHAR:
			case NCLOB:
				return getCharDefaultValue(value, notNull);
			case BOOLEAN:
				return getBoolDefaultValue(value, notNull);
			case INTEGER:
			case TINYINT:
			case SMALLINT:
				return getIntDefaultValue(value, notNull);
			default:
				if(value == null || value.length()==0)
					return " DEFAULT " + (notNull?"0":"NULL");
				if(value.length()>0)
					return " DEFAULT " + value;
				return "";
		}
	}

	/**
	 * @param value
	 * @param notNull
	 * @return
	 */
	default String getIntDefaultValue(String value, boolean notNull)
	{
		if(value == null || value.length()==0)
			return " DEFAULT " + (notNull?"0":"NULL");
		if(value.length()>0)
			return " DEFAULT " + value;
		return "";
	}

	/**
	 * @param value
	 * @param notNull
	 * @return
	 */
	default String getBoolDefaultValue(String value, boolean notNull)
	{
		if(value == null || value.length()==0)
			return " DEFAULT " + (notNull?"FALSE":"NULL");
		if(value.length()>0)
			return " DEFAULT " + value;
		return "";
	}

	/**
	 * @param value
	 * @param notNull
	 * @return
	 */
	default String getCharDefaultValue(String value, boolean notNull)
	{
		if(value == null || value.length()==0)
			return " DEFAULT " + (notNull?"''":"NULL");
		return " DEFAULT '" + value + "'";
	}
	
	public default Number val(String str)
	{
		return val(str, false);
	}

	public default Number val(String str, boolean strict)
	{
		if(strict)
			return NumberUtils.createNumber(str);
		final var validStr = new StringBuilder();
		var seenDot = false; // when this is true, dots are not allowed
		var seenDigit = false; // when this is true, signs are not allowed
		for(var i = 0; i < str.length(); i++)
		{
			final var c = str.charAt(i);
			if(c == '.' && !seenDot)
			{
				seenDot = true;
				validStr.append(c);
			}
			else if((c == '-' || c == '+') && !seenDigit)
			{
				validStr.append(c);
			}
			else if(Character.isDigit(c))
			{
				seenDigit = true;
				validStr.append(c);
			}
			else if(!Character.isWhitespace(c))
				break;
		}
		return NumberUtils.createNumber(validStr.toString());
	}

	public default String str(final String v)
	{
		return "'" + v.replace("'", "''") + "'";
	}
	
	public default String str(final String v, boolean doubleBackSlashes)
	{
		if(doubleBackSlashes)
			return str(v).replace("\\", "\\\\");
		return str(v);
	}
	
	abstract Long count(String select, Object...args) throws SQLException;
	abstract Long countTbl(String table, String context) throws SQLException;
	abstract Long getLongValue(String select, Object...args) throws SQLException;
	abstract int update(final String update, Object...args) throws SQLException;
	abstract void dropTable(final String table) throws SQLException;
	abstract List<Map<String, Object>> query(final String select, Object... args) throws SQLException;
	abstract Map<String, Object> queryFirst(final String select, Object... args) throws SQLException;
	abstract <T> List<T> getColumnList(String select, int col) throws SQLException;

}