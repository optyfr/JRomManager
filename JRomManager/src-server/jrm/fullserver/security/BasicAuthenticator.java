package jrm.fullserver.security;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.authentication.DeferredAuthentication;
import org.eclipse.jetty.security.authentication.LoginAuthenticator;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.security.Constraint;

/**
 * @version $Rev: 4793 $ $Date: 2009-03-19 00:00:01 +0100 (Thu, 19 Mar 2009) $
 */
public class BasicAuthenticator extends LoginAuthenticator
{
	/**
	 * @see org.eclipse.jetty.security.Authenticator#getAuthMethod()
	 */
	@Override
	public String getAuthMethod()
	{
		return Constraint.__BASIC_AUTH;
	}

	/**
	 * @see org.eclipse.jetty.security.Authenticator#validateRequest(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
	 *      boolean)
	 */
	@Override
	public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException
	{
		HttpServletRequest request = (HttpServletRequest)req;
		HttpServletResponse response = (HttpServletResponse)res;

		try
		{
			if (!mandatory)
				return new DeferredAuthentication(this);

			final var user = getIdentity(request);
			if (user != null)
				return new UserAuthentication(getAuthMethod(), user);

			if (DeferredAuthentication.isDeferred(response))
				return Authentication.UNAUTHENTICATED;

			response.setHeader(HttpHeader.WWW_AUTHENTICATE.asString(), "basic realm=\"" + _loginService.getName() + "\", charset=\"UTF-8\"");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return Authentication.SEND_CONTINUE;
		}
		catch(IOException e)
		{
			throw new ServerAuthException(e);
		}
	}
	
	private UserIdentity getIdentity(HttpServletRequest request)
	{
		final var auth = request.getHeader(HttpHeader.AUTHORIZATION.asString());
		if (auth != null)
		{
			int space = auth.indexOf(' ');
			if (space > 0)
			{
				final var method = auth.substring(0, space);
				if ("basic".equalsIgnoreCase(method))
				{
					final var credentials = auth.substring(space + 1).trim();
					final var decoded = new String(Base64.getDecoder().decode(credentials), StandardCharsets.UTF_8);
					int i = decoded.indexOf(':');
					if (i > 0)
					{
						final var username = decoded.substring(0, i);
						final var password = decoded.substring(i + 1);
						return login(username, password, request);
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, User validatedUser) throws ServerAuthException
	{
		return true;
	}
}