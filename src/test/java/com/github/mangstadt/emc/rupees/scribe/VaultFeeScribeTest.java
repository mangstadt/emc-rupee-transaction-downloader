package com.github.mangstadt.emc.rupees.scribe;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class VaultFeeScribeTest {
	private final VaultFeeScribe scribe = new VaultFeeScribe();

	@Test
	public void parse() {
		assertNotNull(scribe.parse("Opened cross-server vault"));
	}
}
