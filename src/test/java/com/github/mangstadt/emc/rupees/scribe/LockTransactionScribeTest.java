package com.github.mangstadt.emc.rupees.scribe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.mangstadt.emc.rupees.dto.LockTransaction;

/**
 * @author Michael Angstadt
 */
public class LockTransactionScribeTest {
	private final LockTransactionScribe scribe = new LockTransactionScribe();

	@Test
	public void parse() {
		LockTransaction transaction = scribe.parse("Locked an item wilderness:1,2,3").build();
		assertEquals("wilderness", transaction.getWorld());
		assertEquals(1, transaction.getX(), 0);
		assertEquals(2, transaction.getY(), 0);
		assertEquals(3, transaction.getZ(), 0);

		transaction = scribe.parse("Full refund for unlocking item wilderness:1,2,3").build();
		assertEquals("wilderness", transaction.getWorld());
		assertEquals(1, transaction.getX(), 0);
		assertEquals(2, transaction.getY(), 0);
		assertEquals(3, transaction.getZ(), 0);

		transaction = scribe.parse("Partial refund for unlocking item wilderness:1,2,3").build();
		assertEquals("wilderness", transaction.getWorld());
		assertEquals(1, transaction.getX(), 0);
		assertEquals(2, transaction.getY(), 0);
		assertEquals(3, transaction.getZ(), 0);
	}
}
