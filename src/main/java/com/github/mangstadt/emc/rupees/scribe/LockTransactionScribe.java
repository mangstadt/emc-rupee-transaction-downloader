package com.github.mangstadt.emc.rupees.scribe;

import java.util.regex.Matcher;

import com.github.mangstadt.emc.rupees.dto.LockTransaction.Builder;

/**
 * @author Michael Angstadt
 */
public class LockTransactionScribe extends RegexScribe<Builder> {
	public LockTransactionScribe() {
		super("^(Locked an item|(Full|Partial) refund for unlocking item) (.*?):(.*?),(.*?),(.*?)$");
	}

	@Override
	protected Builder builder(Matcher m) {
		//@formatter:off
		return new Builder()
		.world(m.group(3))
		.coords(
			Integer.parseInt(m.group(4)),
			Integer.parseInt(m.group(5)),
			Integer.parseInt(m.group(6))
		);
		//@formatter:on
	}
}
