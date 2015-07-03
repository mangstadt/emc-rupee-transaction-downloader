package com.github.mangstadt.emc.rupees.scribe;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class DailySigninBonusScribeTest {
	private final DailySigninBonusScribe scribe = new DailySigninBonusScribe();

	@Test
	public void parse() {
		assertNotNull(scribe.parse("Daily sign-in bonus"));
	}
}
