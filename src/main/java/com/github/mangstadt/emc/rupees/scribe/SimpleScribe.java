package com.github.mangstadt.emc.rupees.scribe;

import com.github.mangstadt.emc.rupees.dto.RupeeTransaction;

/**
 * A scribe that uses a simple String comparison to parse the transaction.
 * @author Michael Angstadt
 * @param <T> the builder class of the rupee transaction class
 */
public abstract class SimpleScribe<T extends RupeeTransaction.Builder> extends RupeeTransactionScribe<T> {
	private final String message;

	public SimpleScribe(String message) {
		this.message = message;
	}

	@Override
	public T parse(String description) {
		return message.equalsIgnoreCase(description) ? builder() : null;
	}

	protected abstract T builder();
}
