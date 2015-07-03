package com.github.mangstadt.emc.rupees.scribe;

import com.github.mangstadt.emc.rupees.dto.DailySigninBonus.Builder;

/**
 * @author Michael Angstadt
 */
public class DailySigninBonusScribe extends SimpleScribe<Builder> {
	public DailySigninBonusScribe() {
		super("Daily sign-in bonus");
	}

	@Override
	protected Builder builder() {
		return new Builder();
	}
}
