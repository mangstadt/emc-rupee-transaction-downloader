package com.github.mangstadt.emc.rupees;

import com.github.mangstadt.emc.rupees.dto.RupeeTransaction;

public class Main {
	public static void main(String[] args) throws Exception {
		try (RupeeTransactionReader reader = new RupeeTransactionReader.Builder("shavingfoam", "fpd1730").build()) {
			RupeeTransaction transaction;
			int max = 10;
			int count = 0;
			while (count < max && (transaction = reader.next()) != null) {
				count++;
				System.out.println(count + ". " + transaction);
			}
		}
	}
}
