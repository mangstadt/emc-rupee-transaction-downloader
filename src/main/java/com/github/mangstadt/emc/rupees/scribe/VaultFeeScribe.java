package com.github.mangstadt.emc.rupees.scribe;

import com.github.mangstadt.emc.rupees.dto.VaultFee.Builder;

/**
 * @author Michael Angstadt
 */
public class VaultFeeScribe extends SimpleScribe<Builder> {
	public VaultFeeScribe() {
		super("Opened cross-server vault");
	}

	@Override
	protected Builder builder() {
		return new Builder();
	}
}
