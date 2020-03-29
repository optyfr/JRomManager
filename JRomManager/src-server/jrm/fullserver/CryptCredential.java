package jrm.fullserver;

import java.sql.SQLException;

import org.apache.commons.dbutils.handlers.BeanHandler;
import org.eclipse.jetty.util.security.Credential;
import org.mindrot.jbcrypt.BCrypt;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Factory.Argon2Types;
import jrm.fullserver.db.SQL;
import lombok.Getter;
import lombok.val;

@SuppressWarnings("serial") class CryptCredential extends Credential
{
	private String username;
	private SQL sql;
	private @Getter UserCredential user;
	
	public CryptCredential(String username, SQL sql)
	{
		this.username = username;
		this.sql = sql;
	}
	
	static public class UserCredential
	{
		private String login;
		private String password;
		private String roles;

		public String getLogin()
		{
			return login;
		}
		public void setLogin(String login)
		{
			this.login = login;
		}
		public String getPassword()
		{
			return password;
		}
		public void setPassword(String password)
		{
			this.password = password;
		}
		public String getRoles()
		{
			return roles;
		}
		public void setRoles(String roles)
		{
			this.roles = roles;
		}
	}
	
	@Override
	public boolean check(Object credentials)
	{
		try
		{
			// methode "magique" de la classe SQL qui rempli un bean (ou une liste de beans) avec une requete select
			user = sql.queryHandler("SELECT * FROM USERS WHERE LOGIN=?", new BeanHandler<>(UserCredential.class), username);
			if (user != null) // si il y a un bien un user avec le login correspondant
			{
				System.out.println(user.getLogin()+":"+user.getPassword());
				if(user.getPassword().startsWith("$argon2"))
				{
					Argon2 argon2;
					if(user.getPassword().startsWith("$argon2id$"))
						argon2 = Argon2Factory.create(Argon2Types.ARGON2id);
					else if(user.getPassword().startsWith("$argon2i$"))
						argon2 = Argon2Factory.create(Argon2Types.ARGON2i);
					else if(user.getPassword().startsWith("$argon2d$"))
						argon2 = Argon2Factory.create(Argon2Types.ARGON2d);
					else
						argon2 = Argon2Factory.create();
					if(argon2.verify(user.getPassword(), credentials.toString().toCharArray()))
						return true;
				}
				else if (BCrypt.checkpw(credentials.toString(), user.getPassword())) // si le hash bcrypt matche
					return true;
			}
		
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public static String hash(String password)
	{
		try
		{
			val argon2 = Argon2Factory.create(Argon2Types.ARGON2id);
			password = argon2.hash(40, 65536, 4, password.toCharArray());
		}
		catch(Throwable e)
		{
			System.err.println(e.getMessage());
			password = BCrypt.hashpw(password, BCrypt.gensalt());
		}
		return password;
	}
	
}