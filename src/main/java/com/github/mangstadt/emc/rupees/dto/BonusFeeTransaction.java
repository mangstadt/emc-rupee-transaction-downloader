package com.github.mangstadt.emc.rupees.dto;

/**
 * Represents a rupee transaction that records a bonus (like a sign-in bonus) or
 * a fee (like an eggify fee).
 * @author Michael Angstadt
 */
public class BonusFeeTransaction extends RupeeTransaction {
	//TODO split up into multiple classes
	public enum BonusFeeType {
		HORSE_SUMMON, LOCK_CHEST, EGGIFY, VAULT_OPEN, SIGN_IN_BONUS, VOTE_BONUS, MAIL_SEND
	}

	private final BonusFeeType type;

	public BonusFeeTransaction(Builder builder) {
		super(builder);
		type = builder.type;
	}

	public BonusFeeType getType() {
		return type;
	}

	public static class Builder extends RupeeTransaction.Builder {
		private BonusFeeType type;

		public BonusFeeType type() {
			return type;
		}

		public Builder type(BonusFeeType type) {
			this.type = type;
			return this;
		}

		@Override
		public BonusFeeTransaction build() {
			return new BonusFeeTransaction(this);
		}
	}
}
