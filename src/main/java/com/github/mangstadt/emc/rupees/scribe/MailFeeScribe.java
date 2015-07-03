package com.github.mangstadt.emc.rupees.scribe;

import java.util.regex.Matcher;

import com.github.mangstadt.emc.rupees.dto.MailFee.Builder;

/**
 * @author Michael Angstadt
 */
public class MailFeeScribe extends RegexScribe<Builder> {
	public MailFeeScribe() {
		super("^Sent mail to (.*?): (.*)$");
	}

	@Override
	protected Builder builder(Matcher m) {
		//@formatter:off
		return new Builder()
		.player(m.group(1))
		.subject(m.group(2));
		//@formatter:on
	}
}
