package com.github.mangstadt.emc.rupees.scribe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.mangstadt.emc.rupees.dto.VoteBonus;

/**
 * @author Michael Angstadt
 */
public class VoteBonusScribeTest {
	private final VoteBonusScribe scribe = new VoteBonusScribe();

	@Test
	public void parse() {
		VoteBonus bonus = scribe.parse("Voted for Empire Minecraft on TopG.org - day bonus: 3").build();
		assertEquals("TopG.org", bonus.getSite());
		assertEquals(3, bonus.getDay());
	}
}
