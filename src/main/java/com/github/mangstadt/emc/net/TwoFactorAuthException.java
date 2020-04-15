package com.github.mangstadt.emc.net;

/**
 * Thrown when a two-factor authentication code is required and has not been
 * provided, or if the provided code is invalid.
 * @author Michael Angstadt
 */
@SuppressWarnings("serial")
public class TwoFactorAuthException extends RuntimeException {
	public TwoFactorAuthException(String message) {
		super(message);
	}

	public static TwoFactorAuthException codeRequired() {
		return new TwoFactorAuthException("This account requires a two-factor authentication code.");
	}

	public static TwoFactorAuthException codeIsInvalid(String code) {
		return new TwoFactorAuthException("The provided two-factor authentication code is invalid: " + code);
	}
}
