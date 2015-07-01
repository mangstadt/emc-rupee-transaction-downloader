package com.github.mangstadt.emc.rupees.scribe;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.mangstadt.emc.rupees.dto.BonusFeeTransaction.BonusFeeType;
import com.github.mangstadt.emc.rupees.dto.BonusFeeTransaction.Builder;
import com.github.mangstadt.emc.util.Pair;

/**
 * @author Michael Angstadt
 */
public class BonusFeeTransactionScribe extends RupeeTransactionScribe<Builder> {
	//@formatter:off
	private final List<Pair<BonusFeeType, Pattern>> regexes = Arrays.asList(
		pair(BonusFeeType.SIGN_IN_BONUS, "^Daily sign-in bonus$"),
		pair(BonusFeeType.VOTE_BONUS, "^Voted for Empire Minecraft on .*$"),
		pair(BonusFeeType.LOCK_CHEST, "^(Locked an item wilderness|(Full|Partial) refund for unlocking item wilderness).*"),
		pair(BonusFeeType.HORSE_SUMMON, "^Summoned stabled horse in the wild.*"),
		pair(BonusFeeType.EGGIFY, "^Eggified a .*$"),
		pair(BonusFeeType.VAULT_OPEN, "^Opened cross-server vault$"),
		pair(BonusFeeType.MAIL_SEND, "^Sent mail to .*$")
	);
	//@formatter:on

	private static Pair<BonusFeeType, Pattern> pair(BonusFeeType type, String regex) {
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		return new Pair<>(type, pattern);
	}

	@Override
	public Builder parse(String description) {
		BonusFeeType type = getType(description);
		return (type == null) ? null : new Builder().type(type);
	}

	private BonusFeeType getType(String description) {
		for (Pair<BonusFeeType, Pattern> pair : regexes) {
			Matcher m = pair.getValue2().matcher(description);
			if (m.find()) {
				return pair.getValue1();
			}
		}
		return null;
	}

}
