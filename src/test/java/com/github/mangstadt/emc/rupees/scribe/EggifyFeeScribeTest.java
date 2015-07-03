package com.github.mangstadt.emc.rupees.scribe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.mangstadt.emc.rupees.dto.EggifyFee;

/**
 * @author Michael Angstadt
 */
public class EggifyFeeScribeTest {
	private final EggifyFeeScribe scribe = new EggifyFeeScribe();

	@Test
	public void parse() {
		EggifyFee fee = scribe.parse("Eggified a Wolf").build();
		assertEquals("Wolf", fee.getMob());
	}
}
