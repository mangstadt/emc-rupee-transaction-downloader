package com.github.mangstadt.emc.rupees.scribe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.mangstadt.emc.rupees.dto.HorseSummonFee;

/**
 * @author Michael Angstadt
 */
public class HorseSummonFeeScribeTest {
	private final HorseSummonFeeScribe scribe = new HorseSummonFeeScribe();

	@Test
	public void parse() {
		HorseSummonFee fee = scribe.parse("Summoned stabled horse in the wild @ wilderness:1:2:3").build();
		assertEquals("wilderness", fee.getWorld());
		assertEquals(1.0, fee.getX(), 0);
		assertEquals(2.0, fee.getY(), 0);
		assertEquals(3.0, fee.getZ(), 0);
	}

	@Test
	public void parse_double() {
		HorseSummonFee fee = scribe.parse("Summoned stabled horse in the wild @ wilderness:1.1:2.1:3.1").build();
		assertEquals("wilderness", fee.getWorld());
		assertEquals(1.1, fee.getX(), 0);
		assertEquals(2.1, fee.getY(), 0);
		assertEquals(3.1, fee.getZ(), 0);
	}
}
