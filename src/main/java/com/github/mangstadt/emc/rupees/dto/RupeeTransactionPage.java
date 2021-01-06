package com.github.mangstadt.emc.rupees.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Contains information from a scraped rupee transaction history page.
 * @author Michael Angstadt
 */
public class RupeeTransactionPage {
	private final Integer rupeeBalance, page, totalPages;
	private final List<RupeeTransaction> transactions;

	/**
	 * @param rupeeBalance the logged-in player's rupee balance
	 * @param page the page number
	 * @param totalPages the total number of pages
	 * @param transactions the rupee transactions
	 */
	public RupeeTransactionPage(Integer rupeeBalance, Integer page, Integer totalPages, List<RupeeTransaction> transactions) {
		this.rupeeBalance = rupeeBalance;
		this.page = page;
		this.totalPages = totalPages;
		this.transactions = Collections.unmodifiableList(transactions);
	}

	public Integer getRupeeBalance() {
		return rupeeBalance;
	}

	public Integer getPage() {
		return page;
	}

	public Integer getTotalPages() {
		return totalPages;
	}

	public List<RupeeTransaction> getTransactions() {
		return transactions;
	}

	/**
	 * Gets the date of the transaction that is listed first on the page.
	 * Because transactions are listed in descending order, this is the <b>most
	 * recent</b> transaction on the page.
	 * @return the date of the first transaction or null if no transactions
	 * could be found on the page
	 */
	public LocalDateTime getFirstTransactionDate() {
		return transactions.isEmpty() ? null : transactions.get(0).getTs();
	}

	/**
	 * Gets the date of the transaction that is listed first on the page.
	 * Because transactions are listed in descending order, this is the
	 * <b>oldest</b> transaction on the page.
	 * @return the date of the last transaction or null if no transactions
	 * could be found on the page
	 */
	public LocalDateTime getLastTransactionDate() {
		return transactions.isEmpty() ? null : transactions.get(transactions.size() - 1).getTs();
	}
}
