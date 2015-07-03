package com.github.mangstadt.emc.rupees.scribe;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.mangstadt.emc.rupees.dto.RupeeTransaction;

/**
 * A scribe that uses a regular expression to parse the transaction.
 * @author Michael Angstadt
 * @param <T> the builder class of the rupee transaction class
 */
public abstract class RegexScribe<T extends RupeeTransaction.Builder> extends RupeeTransactionScribe<T> {
	private final Pattern regex;

	public RegexScribe(String regex) {
		this.regex = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public T parse(String description) {
		Matcher m = regex.matcher(description);
		return m.find() ? builder(m) : null;
	}

	protected abstract T builder(Matcher m);
}
