package com.github.mangstadt.emc.rupees;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.github.mangstadt.emc.rupees.dto.RupeeTransaction;
import com.github.mangstadt.emc.rupees.dto.RupeeTransactionPage;
import com.github.mangstadt.emc.rupees.scribe.DailySigninBonusScribe;
import com.github.mangstadt.emc.rupees.scribe.EggifyFeeScribe;
import com.github.mangstadt.emc.rupees.scribe.HorseSummonFeeScribe;
import com.github.mangstadt.emc.rupees.scribe.LockTransactionScribe;
import com.github.mangstadt.emc.rupees.scribe.MailFeeScribe;
import com.github.mangstadt.emc.rupees.scribe.PaymentTransactionScribe;
import com.github.mangstadt.emc.rupees.scribe.RupeeTransactionScribe;
import com.github.mangstadt.emc.rupees.scribe.ShopTransactionScribe;
import com.github.mangstadt.emc.rupees.scribe.VaultFeeScribe;
import com.github.mangstadt.emc.rupees.scribe.VoteBonusScribe;

/**
 * Scrapes rupee transaction history HTML pages. This class is thread-safe.
 * @author Michael Angstadt
 */
public class RupeeTransactionPageScraper {
	private static final Logger logger = Logger.getLogger(RupeeTransactionPageScraper.class.getName());

	private final Pattern balanceRegex = Pattern.compile("^Your balance: ([\\d,]+)$", Pattern.CASE_INSENSITIVE);
	private final Pattern amountRegex = Pattern.compile("^([-+])\\s*([\\d,]+)$", Pattern.CASE_INSENSITIVE);

	/*
	 * English month names are always used, no matter where the player lives.
	 */
	private final DateTimeFormatter transactionTsFormatter12Hour = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a", Locale.US);
	private final DateTimeFormatter transactionTsFormatter24Hour = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' H:mm", Locale.US);

	private final List<RupeeTransactionScribe<?>> scribes = new ArrayList<>();
	{
		scribes.add(new ShopTransactionScribe());
		scribes.add(new PaymentTransactionScribe());
		scribes.add(new DailySigninBonusScribe());
		scribes.add(new HorseSummonFeeScribe());
		scribes.add(new MailFeeScribe());
		scribes.add(new EggifyFeeScribe());
		scribes.add(new LockTransactionScribe());
		scribes.add(new VoteBonusScribe());
		scribes.add(new VaultFeeScribe());
	}

	public RupeeTransactionPageScraper() {
		//empty
	}

	/**
	 * @param customScribes any additional, custom scribes to use to parse the
	 * transactions
	 */
	public RupeeTransactionPageScraper(Collection<RupeeTransactionScribe<?>> customScribes) {
		this.scribes.addAll(customScribes);
	}

	/**
	 * Scrapes a transaction page.
	 * @param document the HTML page to scrape
	 * @return the scraped page or null if the given HTML page is not a rupee
	 * transaction page
	 */
	public RupeeTransactionPage scrape(Document document) {
		List<RupeeTransaction> transactions = parseTransactions(document);
		if (transactions == null) {
			return null;
		}

		//@formatter:off
		return new RupeeTransactionPage(
			parseRupeeBalance(document),
			parseCurrentPage(document),
			parseTotalPages(document),
			transactions
		);
		//@formatter:on
	}

	/**
	 * Parses the transactions from a transaction page.
	 * @param document the transaction HTML page
	 * @return the transactions or null if the given HTML page is not a rupee
	 * transaction page
	 */
	private List<RupeeTransaction> parseTransactions(Document document) {
		Element containerElement = document.select("ol.sectionItems").first();
		if (containerElement == null) {
			return null;
		}

		/*
		 * Set initial capacity to 30 because each rupee transaction page
		 * contains that many transactions.
		 */
		List<RupeeTransaction> transactions = new ArrayList<>(30);

		for (Element element : containerElement.select("li.sectionItem")) {
			try {
				String description = parseDescription(element);
				RupeeTransaction.Builder<?> builder = null;
				for (RupeeTransactionScribe<?> scribe : scribes) {
					try {
						builder = scribe.parse(description);
						if (builder != null) {
							break;
						}
					} catch (Exception e) {
						logger.log(Level.WARNING, scribe.getClass().getSimpleName() + " scribe threw an exception. Skipping it.", e);
					}
				}

				/*
				 * No scribes where found for this transaction, so just parse it
				 * as a plain RupeeTransaction object.
				 */
				if (builder == null) {
					builder = new RupeeTransaction.Builder<>();
				}

				builder.ts(parseTs(element));
				builder.description(description);
				builder.amount(parseAmount(element));
				builder.balance(parseBalance(element));

				transactions.add(builder.build());
			} catch (Exception e) {
				/*
				 * Skip the transaction if any of the fields cannot be properly
				 * parsed.
				 */
				logger.log(Level.WARNING, "Problem parsing rupee transaction, skipping.", e);
			}
		}

		return transactions;
	}

	/**
	 * Parses the player's total rupee balance from a transaction page.
	 * @param document the transaction HTML page
	 * @return the rupee balance or null if not found
	 */
	private Integer parseRupeeBalance(Document document) {
		Element element = document.getElementById("rupeesBalance");
		if (element == null) {
			return null;
		}

		Matcher m = balanceRegex.matcher(element.text());
		if (!m.find()) {
			return null;
		}

		try {
			return parseNumber(m.group(1));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Parses the page number of a transaction page
	 * @param document the transaction HTML page
	 * @return the page number or null if not found
	 */
	private Integer parseCurrentPage(Document document) {
		Element element = document.select(".PageNav").first();
		if (element == null) {
			return null;
		}

		try {
			return parseNumber(element.attr("data-page"));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Parse the total number of transaction pages from a transaction page.
	 * @param document the transaction HTML page
	 * @return the total number of pages or null if not found
	 */
	private Integer parseTotalPages(Document document) {
		Element element = document.select(".PageNav").first();
		if (element == null) {
			return null;
		}

		try {
			return parseNumber(element.attr("data-last"));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Parses a transaction's description.
	 * @param transactionElement the transaction HTML element
	 * @return the description
	 */
	private String parseDescription(Element transactionElement) {
		Element descriptionElement = transactionElement.select("div.description").first();
		return descriptionElement.text();
	}

	/**
	 * Parses a transaction's timestamp.
	 * @param transactionElement the transaction HTML element
	 * @return the timestamp
	 * @throws DateTimeParseException if the timestamp can't be parsed
	 */
	private LocalDateTime parseTs(Element transactionElement) throws DateTimeParseException {
		Element tsElement = transactionElement.select("div.time abbr[data-time]").first();
		if (tsElement != null) {
			String dataTime = tsElement.attr("data-time");
			long epochSeconds = Long.parseLong(dataTime);
			return Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
		}

		tsElement = transactionElement.select("div.time span[title]").first();
		String tsText = tsElement.attr("title");

		/*
		 * Timestamp may be in 12-hour or 24-hour time, depending on the
		 * date/time settings in the user's EMC account.
		 */
		try {
			return LocalDateTime.from(transactionTsFormatter12Hour.parse(tsText));
		} catch (DateTimeParseException e) {
			return LocalDateTime.from(transactionTsFormatter24Hour.parse(tsText));
		}
	}

	/**
	 * Parses the amount of rupees that were processed in a transaction.
	 * @param transactionElement the transaction HTML element
	 * @return the amount
	 */
	private int parseAmount(Element transactionElement) {
		Element amountElement = transactionElement.select("div.amount").first();
		String amountText = amountElement.text();

		Matcher m = amountRegex.matcher(amountText);
		m.find();

		int amount = parseNumber(m.group(2));
		if ("-".equals(m.group(1))) {
			amount *= -1;
		}
		return amount;
	}

	/**
	 * Parses the player's balance after the transaction was applied.
	 * @param transactionElement the transaction HTML element
	 * @return the balance
	 */
	private int parseBalance(Element transactionElement) {
		Element balanceElement = transactionElement.select("div.balance").first();
		String balanceText = balanceElement.text();
		return parseNumber(balanceText);
	}

	/**
	 * Parses a number that may or may not have commas in it.
	 * @param value the string value (e.g. "12,560")
	 * @return the parsed number
	 */
	private static int parseNumber(String value) {
		value = value.replace(",", "");
		return Integer.parseInt(value);
	}
}
