package com.github.mangstadt.emc.rupees.scribe;

import com.github.mangstadt.emc.rupees.dto.RupeeTransaction;


/**
 * Parses a rupee transaction into a specific class, based on the transaction's
 * description.
 * @author Michael Angstadt
 * @param <T> the builder class of the rupee transaction class
 */
public abstract class RupeeTransactionScribe<T extends RupeeTransaction.Builder> {
	/**
	 * Parses a transaction's description, returning a builder object for that
	 * transaction type.
	 * @param description the transaction description
	 * @return the builder or null if the description doesn't match the
	 * transaction type
	 */
	public abstract T parse(String description);

	protected static int parseNumber(String value) {
		value = value.replace(",", "");
		return Integer.parseInt(value);
	}
}
