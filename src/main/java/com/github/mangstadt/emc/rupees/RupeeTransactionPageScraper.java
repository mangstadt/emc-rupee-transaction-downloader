package com.github.mangstadt.emc.rupees;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
	private final Pattern amountRegex = Pattern.compile("^(-|\\+)\\s*([\\d,]+)$", Pattern.CASE_INSENSITIVE);

	private final List<RupeeTransactionScribe<?>> scribes = new ArrayList<RupeeTransactionScribe<?>>();
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
	public RupeeTransactionPageScraper(List<RupeeTransactionScribe<?>> customScribes) {
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

	private List<RupeeTransaction> parseTransactions(Document document) {
		Element containerElement = document.select("ol.sectionItems").first();
		if (containerElement == null) {
			return null;
		}

		//set initial capacity to 30, because each rupee transaction page contains 30 transactions
		List<RupeeTransaction> transactions = new ArrayList<RupeeTransaction>(30);

		for (Element element : containerElement.select("li.sectionItem")) {
			try {
				String description = parseDescription(element);
				RupeeTransaction.Builder builder = null;
				for (RupeeTransactionScribe<?> scribe : scribes) {
					try {
						builder = scribe.parse(description);
						if (builder != null) {
							break;
						}
					} catch (Exception e) {
						logger.log(Level.WARNING, scribe.getClass().getSimpleName() + " scribe threw an excception. Skipping it.", e);
					}
				}
				if (builder == null) {
					builder = new RupeeTransaction.Builder();
				}

				builder.ts(parseTs(element));
				builder.description(description);
				builder.amount(parseAmount(element));
				builder.balance(parseBalance(element));

				transactions.add(builder.build());
			} catch (Exception e) {
				//skip the transaction if any of the fields cannot be properly parsed
				logger.log(Level.WARNING, "Problem parsing rupee transaction, skipping.", e);
			}
		}

		return transactions;
	}

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

	private String parseDescription(Element transactionElement) {
		Element descriptionElement = transactionElement.select("div.description").first();
		return descriptionElement.text();
	}

	private Date parseTs(Element transactionElement) throws ParseException {
		Element tsElement = transactionElement.select("div.time abbr[data-time]").first();
		if (tsElement == null) {
			tsElement = transactionElement.select("div.time span[title]").first();
			String tsText = tsElement.attr("title");

			//instantiate new DateFormat object to keep this class thread-safe
			//English month names are used, no matter where the player lives!
			DateFormat df = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm aa", Locale.US);

			return df.parse(tsText);
		}

		String dataTime = tsElement.attr("data-time");
		long ts = Long.parseLong(dataTime) * 1000;
		return new Date(ts);
	}

	private int parseAmount(Element element) {
		Element amountElement = element.select("div.amount").first();
		String amountText = amountElement.text();

		Matcher m = amountRegex.matcher(amountText);
		m.find();

		int amount = parseNumber(m.group(2));
		if ("-".equals(m.group(1))) {
			amount *= -1;
		}
		return amount;
	}

	private int parseBalance(Element element) {
		Element balanceElement = element.select("div.balance").first();
		String balanceText = balanceElement.text();
		return parseNumber(balanceText);
	}

	private int parseNumber(String value) {
		value = value.replace(",", "");
		return Integer.parseInt(value);
	}
}
