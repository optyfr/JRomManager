package jrm.fullserver.security;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;

import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.RolePrincipal;
import org.eclipse.jetty.security.UserPrincipal;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;

import jrm.fullserver.ServerSettings;
import jrm.fullserver.db.DB;
import jrm.fullserver.db.SQL;
import jrm.misc.Log;
import jrm.server.shared.WebSession;
import lombok.val;

/**
 * Service d'identification avec backend SQL
 */
public class Login extends SQL implements LoginService
{
	private static final String ADMIN = "admin";
	protected IdentityService identityService = new DefaultIdentityService();

	public Login() throws IOException, SQLException
	{
		super(true, new ServerSettings());
		db = DB.getInstance(getSettings()).connectToDB("Server.sys", false, true);
		update("CREATE TABLE IF NOT EXISTS USERS(LOGIN VARCHAR_IGNORECASE(255) PRIMARY KEY, PASSWORD VARCHAR(255), ROLES VARCHAR(255))");
		if (count("SELECT * FROM USERS") == 0)
			update("INSERT INTO USERS VALUES(?, ?, ?)", ADMIN, CryptCredential.hash(ADMIN), ADMIN);
	}

	@Override
	public String getName()
	{
		return "Authentication";
	}

	private static final HashMap<String, UserIdentity> cache = new HashMap<>();
	private static long cachetime = System.currentTimeMillis();

	@Override
	public UserIdentity login(String username, Object credentials, ServletRequest request)
	{
		String sessionid = null;
		WebSession sess = null;
		if (request instanceof Request r)
		{
			val session = r.getSession();
			if (session != null)
			{
				sessionid = session.getId();
				if (sessionid != null)
					sess = (WebSession) session.getAttribute("session");
			}
		}
		return login(username, credentials, sessionid, sess);
	}

	/**
	 * @param username
	 * @param credentials
	 * @param sessionid
	 * @param sess
	 * @return
	 */
	private UserIdentity login(String username, Object credentials, String sessionid, WebSession sess)
	{
		synchronized (cache)
		{
			if (60000 < (System.currentTimeMillis() - cachetime))
			{
				cache.clear();
				cachetime = System.currentTimeMillis();
			}
			if (sessionid != null && cache.containsKey(username + ":" + sessionid))
				return cache.get(username + ":" + sessionid);
			else
			{
				final var credential = new CryptCredential(username, this);
				if (credential.check(credentials))
				{
					final var principal = new UserPrincipal(credential.getUser().getLogin(), credential);
					final var subject = new Subject();
					subject.getPrincipals().add(principal);
					subject.getPublicCredentials().add(username + ":" + sessionid);
					String[] roles = credential.getUser().getRoles().split(";");
					for (String role : roles)
						subject.getPrincipals().add(new RolePrincipal(role));

					final var identity = identityService.newUserIdentity(subject, principal, roles);
					if (sessionid != null)
					{
						cache.put(username + ":" + sessionid, identity);
						sess.setUser(username, roles);
					}
					return identity;
				}
			}
		}
		return null;
	}

	@Override
	public boolean validate(UserIdentity user)
	{
		Log.debug("validate");
		for (val credential : user.getSubject().getPublicCredentials())
			if (cache.containsKey(credential))
				return true;
		return false;
	}

	@Override
	public IdentityService getIdentityService()
	{
		return identityService;
	}

	@Override
	public void setIdentityService(IdentityService service)
	{
		// disabled
	}

	@Override
	public void logout(UserIdentity user)
	{
		for (val credential : user.getSubject().getPublicCredentials())
			cache.remove(credential);
	}

	@Override
	public String getContext()
	{
		return null;
	}
}
