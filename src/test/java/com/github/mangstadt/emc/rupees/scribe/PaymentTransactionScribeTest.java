package com.github.mangstadt.emc.rupees.scribe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.github.mangstadt.emc.rupees.dto.PaymentTransaction.Builder;

/**
 * @author Michael Angstadt
 */
public class PaymentTransactionScribeTest {
	private final PaymentTransactionScribe scribe = new PaymentTransactionScribe();

	@Test
	public void parse_payment_from() {
		Builder builder = scribe.parse("Payment from Notch");
		assertEquals("Notch", builder.player());
		assertNull(builder.reason());

		builder = scribe.parse("Payment from Notch:");
		assertEquals("Notch", builder.player());
		assertNull(builder.reason());

		builder = scribe.parse("Payment from Notch: ");
		assertEquals("Notch", builder.player());
		assertNull(builder.reason());

		builder = scribe.parse("Payment from Notch: 64 Apples");
		assertEquals("Notch", builder.player());
		assertEquals("64 Apples", builder.reason());
	}

	@Test
	public void parse_payment_to() {
		Builder builder = scribe.parse("Payment to Notch");
		assertEquals("Notch", builder.player());
		assertNull(builder.reason());

		builder = scribe.parse("Payment to Notch:");
		assertEquals("Notch", builder.player());
		assertNull(builder.reason());

		builder = scribe.parse("Payment to Notch: ");
		assertEquals("Notch", builder.player());
		assertNull(builder.reason());

		builder = scribe.parse("Payment to Notch: 64 Apples");
		assertEquals("Notch", builder.player());
		assertEquals("64 Apples", builder.reason());
	}

	@Test
	public void parse_not_a_payment_transaction() {
		Builder builder = scribe.parse("Foo Bar");
		assertNull(builder);
	}
}
