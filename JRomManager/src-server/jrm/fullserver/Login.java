package jrm.fullserver;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import javax.security.auth.Subject;
import javax.servlet.ServletRequest;

import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.AbstractLoginService.UserPrincipal;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;

import jrm.fullserver.db.DB;
import jrm.fullserver.db.SQL;
import jrm.server.shared.WebSession;
import lombok.val;

/**
 * Service d'identification avec backend SQL
 */
class Login extends SQL implements LoginService
{
	protected IdentityService _identityService = new DefaultIdentityService();

	public Login() throws IOException, SQLException
	{
		super(true,new ServerSettings());
		// On se connecte ici dans le constructeur pour garder la connexion Ã  la base et
		// pas devoir se reconnecter Ã  chaque identification au serveur
		db = DB.getInstance(getSettings()).connectToDB("Server.sys", false, true);
		// On crÃ©e la table user et on ne la rempli que si nÃ©cessaire
		update("CREATE TABLE IF NOT EXISTS USERS(LOGIN VARCHAR_IGNORECASE(255) PRIMARY KEY, PASSWORD VARCHAR(255), ROLES VARCHAR(255))");
		if(count("SELECT * FROM USERS") == 0)
			update("INSERT INTO USERS VALUES(?, ?, ?)", "admin", CryptCredential.hash("admin"), "admin");
	}

	@Override
	public String getName()
	{
		return "Authentication";
	}

	private final static HashMap<String, UserIdentity> cache = new HashMap<>();
	private static long cachetime = System.currentTimeMillis();

	@Override
	public UserIdentity login(String username, Object credentials, ServletRequest request)
	{
		String sessionid = null;
		WebSession sess = null;
		if(request instanceof Request)
		{
			val session = ((Request)request).getSession();
			if(session != null)
			{
				sessionid = session.getId();
				if(sessionid!=null)
					sess = (WebSession)session.getAttribute("session");
			}
		}
		synchronized(cache)
		{
			if(60000 < (System.currentTimeMillis() - cachetime))
			{
				cache.clear();
				cachetime = System.currentTimeMillis();
			}
			if(sessionid != null && cache.containsKey(username + ":" + sessionid))
				return cache.get(username + ":" + sessionid);
			else
			{
				CryptCredential credential = new CryptCredential(username, this);
				if(credential.check(credentials)) // si le hash bcrypt matche
				{
					// Creation d'un UserIdentity
					UserPrincipal principal = new UserPrincipal(credential.getUser().getLogin(), credential);
					Subject subject = new Subject();
					subject.getPrincipals().add(principal);
					subject.getPublicCredentials().add(username + ":" + sessionid);
					String[] roles = credential.getUser().getRoles().split(";");
					for(String role : roles)
						subject.getPrincipals().add(new AbstractLoginService.RolePrincipal(role));

					UserIdentity identity = _identityService.newUserIdentity(subject, principal, roles);
					if(sessionid != null)
					{
						cache.put(username + ":" + sessionid, identity);
						sess.setUser(username);
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
		System.out.println("validate");
		for(val credential : user.getSubject().getPublicCredentials())
			if(cache.containsKey(credential)) return true;
		return false;
	}

	@Override
	public IdentityService getIdentityService()
	{
		return _identityService;
	}

	@Override
	public void setIdentityService(IdentityService service)
	{
	}

	@Override
	public void logout(UserIdentity user)
	{
		for(val credential : user.getSubject().getPublicCredentials())
			cache.remove(credential);
	}

	@Override
	public String getContext()
	{
		return null;
	}
}