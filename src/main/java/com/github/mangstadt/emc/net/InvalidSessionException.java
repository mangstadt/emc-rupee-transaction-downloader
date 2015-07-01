package com.github.mangstadt.emc.net;

/**
 * Thrown when the EMC website unexpectedly rejects your login session.
 * @author Michael Angstadt
 */
@SuppressWarnings("serial")
public class InvalidSessionException extends RuntimeException {
	public InvalidSessionException() {
		super("The session is no longer valid.");
	}
}
