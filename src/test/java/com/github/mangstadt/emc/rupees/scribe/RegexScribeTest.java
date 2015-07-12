package com.github.mangstadt.emc.rupees.scribe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.regex.Matcher;

import org.junit.Test;

import com.github.mangstadt.emc.rupees.dto.RupeeTransaction.Builder;

/**
 * @author Michael Angstadt
 */
public class RegexScribeTest {
	@Test
	public void parse() {
		RegexScribe<Builder<Builder<?>>> scribe = new RegexScribe<Builder<Builder<?>>>("Message from: (.*)") {
			private int count = 1;

			@Override
			protected Builder<Builder<?>> builder(Matcher m) {
				assertEquals(1, count);
				assertEquals("Notch", m.group(1));
				count++;
				return new Builder<Builder<?>>();
			}
		};
		assertNotNull(scribe.parse("Message from: Notch"));
		assertNull(scribe.parse("Another message"));
	}
}
