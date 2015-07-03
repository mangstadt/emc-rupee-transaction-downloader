package com.github.mangstadt.emc.rupees.scribe;

import java.util.regex.Matcher;

import com.github.mangstadt.emc.rupees.dto.EggifyFee.Builder;

/**
 * @author Michael Angstadt
 */
public class EggifyFeeScribe extends RegexScribe<Builder> {
	public EggifyFeeScribe() {
		super("^Eggified a (.*)$");
	}

	@Override
	protected Builder builder(Matcher m) {
		return new Builder().mob(m.group(1));
	}
}
