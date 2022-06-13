package jrm.fullserver.security;

import lombok.Data;

public @Data class UserCredential
{
	private String login;
	private String password;
	private String roles;
}