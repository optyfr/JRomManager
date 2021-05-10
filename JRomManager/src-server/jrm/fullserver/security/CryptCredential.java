package jrm.fullserver.security;

import java.sql.SQLException;

import org.apache.commons.dbutils.handlers.BeanHandler;
import org.eclipse.jetty.util.security.Credential;
import org.mindrot.jbcrypt.BCrypt;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;
import jrm.fullserver.db.SQL;
import lombok.Getter;

@SuppressWarnings("serial") public class CryptCredential extends Credential
{
	private String username;
	private SQL sql;
	private @Getter UserCredential user;
	
	public CryptCredential(String username, SQL sql)
	{
		this.username = username;
		this.sql = sql;
	}
	
	@Override
	public boolean check(Object credentials)
	{
		try
		{
			if (null != (user = sql.queryHandler("SELECT * FROM USERS WHERE LOGIN=?", new BeanHandler<>(UserCredential.class), username)))
				return check(credentials.toString(), user.getPassword());
		
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean check(String credentials, String password)
	{
		if(password.startsWith("$argon2"))
		{
			Argon2 argon2;
			if(password.startsWith("$argon2id$"))
				argon2 = Argon2Factory.create(Argon2Types.ARGON2id);
			else if(password.startsWith("$argon2i$"))
				argon2 = Argon2Factory.create(Argon2Types.ARGON2i);
			else if(password.startsWith("$argon2d$"))
				argon2 = Argon2Factory.create(Argon2Types.ARGON2d);
			else
				argon2 = Argon2Factory.create();
			if(argon2.verify(password, credentials.toCharArray()))
				return true;
		}
		else if (BCrypt.checkpw(credentials, password)) // si le hash bcrypt matche
			return true;
		return false;
	}
	
	public static String hash(String password)
	{
		if (password.startsWith("$2a$") || password.startsWith("$argon2"))
			return password;
		try
		{
			password = Argon2Factory.create(Argon2Types.ARGON2id).hash(40, 65536, 4, password.toCharArray());
		}
		catch (Exception e)
		{
			password = BCrypt.hashpw(password, BCrypt.gensalt());
		}
		return password;
	}
	
}