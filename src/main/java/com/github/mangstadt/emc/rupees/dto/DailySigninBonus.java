package com.github.mangstadt.emc.rupees.dto;

/**
 * The daily bonus players get when they sign in.
 * @author Michael Angstadt
 */
public class DailySigninBonus extends RupeeTransaction {
	private DailySigninBonus(Builder builder) {
		super(builder);
	}

	public static class Builder extends RupeeTransaction.Builder {
		public DailySigninBonus build() {
			return new DailySigninBonus(this);
		}
	}
}
