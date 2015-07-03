package com.github.mangstadt.emc.rupees.scribe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.mangstadt.emc.rupees.dto.MailFee;

/**
 * @author Michael Angstadt
 */
public class MailFeeScribeTest {
	private final MailFeeScribe scribe = new MailFeeScribe();

	@Test
	public void parse() {
		MailFee fee = scribe.parse("Sent mail to Luckypat: Hello").build();
		assertEquals("Luckypat", fee.getPlayer());
		assertEquals("Hello", fee.getSubject());
	}
}
