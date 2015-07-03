package com.github.mangstadt.emc.rupees.scribe;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.github.mangstadt.emc.rupees.dto.RupeeTransaction.Builder;

/**
 * @author Michael Angstadt
 */
public class SimpleScribeTest {
	private final SimpleScribe<Builder> scribe = new SimpleScribe<Builder>("The message") {
		@Override
		protected Builder builder() {
			return new Builder();
		}
	};

	@Test
	public void parse() {
		assertNotNull(scribe.parse("The message"));
		assertNull(scribe.parse("Another message"));
	}
}
