package com.github.mangstadt.emc.net;

import com.google.common.base.Strings;

/**
 * Thrown when the EMC website rejects your login credentials.
 * @author Michael Angstadt
 */
public class InvalidCredentialsException extends RuntimeException {
	public InvalidCredentialsException(String username, String password) {
		super("Invalid credentials: (username = " + username + "; password = " + Strings.repeat("*", password.length()));
	}
}
