package com.github.mangstadt.emc.rupees.scribe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.mangstadt.emc.rupees.dto.BonusFeeTransaction.BonusFeeType;
import com.github.mangstadt.emc.rupees.dto.BonusFeeTransaction.Builder;
import com.github.mangstadt.emc.util.Pair;

/**
 * @author Michael Angstadt
 */
public class BonusFeeTransactionScribeTest {
	private final BonusFeeTransactionScribe scribe = new BonusFeeTransactionScribe();

	@Test
	public void parse() {
		//@formatter:off
		List<Pair<BonusFeeType, String>> data = Arrays.asList(
			new Pair<>(BonusFeeType.SIGN_IN_BONUS, "Daily sign-in bonus"),
			new Pair<>(BonusFeeType.VOTE_BONUS, "Voted for Empire Minecraft on foobar.com"),
			new Pair<>(BonusFeeType.LOCK_CHEST, "Locked an item wilderness."),
			new Pair<>(BonusFeeType.LOCK_CHEST, "Full refund for unlocking item wilderness."),
			new Pair<>(BonusFeeType.LOCK_CHEST, "Partial refund for unlocking item wilderness."),
			new Pair<>(BonusFeeType.HORSE_SUMMON, "Summoned stabled horse in the wild"),
			new Pair<>(BonusFeeType.EGGIFY, "Eggified a pig"),
			new Pair<>(BonusFeeType.VAULT_OPEN, "Opened cross-server vault"),
			new Pair<>(BonusFeeType.MAIL_SEND, "Sent mail to Notch")
		);
		//@formatter:on

		for (Pair<BonusFeeType, String> pair : data) {
			BonusFeeType type = pair.getValue1();
			String description = pair.getValue2();

			Builder builder = scribe.parse(description);
			assertEquals("Description: " + description, type, builder.type());
		}
	}

	@Test
	public void parse_not_a_bonus_fee_transaction() {
		Builder builder = scribe.parse("Foo Bar");
		assertNull(builder);
	}
}
