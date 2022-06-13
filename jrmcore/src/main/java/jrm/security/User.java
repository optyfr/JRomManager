package jrm.security;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jrm.misc.GlobalSettings;
import lombok.Getter;

public class User
{
	private final @Getter Session session;
	
	private final @Getter String name;
	private final Set<String> roles;
	
	private final @Getter GlobalSettings settings;
	
	public User(final Session session, final String name, final String[] roles)
	{
		this.session = session;
		this.session.user = this;
		this.name = name;
		this.roles = roles!=null?Stream.of(roles).map(String::toLowerCase).collect(Collectors.toSet()):Collections.emptySet();
		this.settings = new GlobalSettings(this);
	}
	
	public boolean hasRole(final String role)
	{
		return roles.contains(role.toLowerCase());
	}
	
	public boolean isAdmin()
	{
		return hasRole("admin");
	}
}
