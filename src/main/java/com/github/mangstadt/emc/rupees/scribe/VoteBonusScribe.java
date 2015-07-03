package com.github.mangstadt.emc.rupees.scribe;

import java.util.regex.Matcher;

import com.github.mangstadt.emc.rupees.dto.VoteBonus.Builder;

/**
 * @author Michael Angstadt
 */
public class VoteBonusScribe extends RegexScribe<Builder> {
	public VoteBonusScribe() {
		super("^Voted for Empire Minecraft on (.*?) - day bonus: (\\d+)$");
	}

	@Override
	protected Builder builder(Matcher m) {
		//@formatter:off
		return new Builder()
		.site(m.group(1))
		.day(parseNumber(m.group(2)));
		//@formatter:on
	}
}
