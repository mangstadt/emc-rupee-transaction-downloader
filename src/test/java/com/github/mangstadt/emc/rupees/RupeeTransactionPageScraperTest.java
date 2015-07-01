package com.github.mangstadt.emc.rupees;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.mangstadt.emc.rupees.dto.BonusFeeTransaction;
import com.github.mangstadt.emc.rupees.dto.BonusFeeTransaction.BonusFeeType;
import com.github.mangstadt.emc.rupees.dto.PaymentTransaction;
import com.github.mangstadt.emc.rupees.dto.RupeeTransaction;
import com.github.mangstadt.emc.rupees.dto.RupeeTransactionPage;
import com.github.mangstadt.emc.rupees.dto.ShopTransaction;
import com.github.mangstadt.emc.rupees.scribe.RupeeTransactionScribe;

/**
 * @author Michael Angstadt
 */
public class RupeeTransactionPageScraperTest {
	@BeforeClass
	public static void beforeClass() {
		LogManager.getLogManager().reset();
	}

	@Test
	public void sample_page() throws Exception {
		Document document = load("transaction-page-sample.html");
		RupeeTransactionPageScraper scraper = new RupeeTransactionPageScraper();
		RupeeTransactionPage page = scraper.scrape(document);

		assertEquals(Integer.valueOf(1), page.getPage());
		assertEquals(Integer.valueOf(23_570), page.getTotalPages());
		assertEquals(Integer.valueOf(1_284_678), page.getRupeeBalance());

		Iterator<RupeeTransaction> it = page.getTransactions().iterator();

		RupeeTransaction transaction = it.next();
		assertEquals(new Date(1435429290000L), transaction.getTs());
		assertEquals("Donation to Notch", transaction.getDescription());
		assertEquals(32, transaction.getAmount());
		assertEquals(1_284_678, transaction.getBalance());

		ShopTransaction shopTransaction = (ShopTransaction) it.next();
		assertEquals(new Date(1435428172000L), shopTransaction.getTs());
		assertEquals("Player shop sold 4 Purple Dye to AnguishedCarpet", shopTransaction.getDescription());
		assertEquals(28, shopTransaction.getAmount());
		assertEquals(1_284_614, shopTransaction.getBalance());
		assertEquals("AnguishedCarpet", shopTransaction.getShopCustomer());
		assertNull(shopTransaction.getShopOwner());
		assertEquals(-4, shopTransaction.getQuantity());
		assertEquals("Purple Dye", shopTransaction.getItem());

		PaymentTransaction paymentTransaction = (PaymentTransaction) it.next();
		assertEquals(new Date(1435428159000L), paymentTransaction.getTs());
		assertEquals("Payment to AnguishedCarpet", paymentTransaction.getDescription());
		assertEquals(-32, paymentTransaction.getAmount());
		assertEquals(1_284_586, paymentTransaction.getBalance());
		assertEquals("AnguishedCarpet", paymentTransaction.getPlayer());
		assertNull(paymentTransaction.getReason());

		BonusFeeTransaction bonusFeeTransaction = (BonusFeeTransaction) it.next();
		assertEquals(new Date(1435427901000L), bonusFeeTransaction.getTs());
		assertEquals("Daily sign-in bonus", bonusFeeTransaction.getDescription());
		assertEquals(400, bonusFeeTransaction.getAmount());
		assertEquals(1_284_554, bonusFeeTransaction.getBalance());
		assertEquals(BonusFeeType.SIGN_IN_BONUS, bonusFeeTransaction.getType());

		transaction = it.next();
		assertEquals(new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("11/22/2012 21:54"), transaction.getTs());
		assertEquals("Week-old transaction", transaction.getDescription());
		assertEquals(-100, transaction.getAmount());
		assertEquals(212_990, transaction.getBalance());

		assertFalse(it.hasNext());
	}

	@Test
	public void custom_scribe_list() throws Exception {
		Document document = load("transaction-page-sample.html");
		RupeeTransactionPageScraper scraper = new RupeeTransactionPageScraper(Arrays.asList(new DonationTransactionScribe()));
		RupeeTransactionPage page = scraper.scrape(document);

		assertEquals(Integer.valueOf(1), page.getPage());
		assertEquals(Integer.valueOf(23_570), page.getTotalPages());
		assertEquals(Integer.valueOf(1_284_678), page.getRupeeBalance());

		Iterator<RupeeTransaction> it = page.getTransactions().iterator();

		DonationTransaction donation = (DonationTransaction) it.next();
		assertEquals(new Date(1435429290000L), donation.getTs());
		assertEquals("Donation to Notch", donation.getDescription());
		assertEquals(32, donation.getAmount());
		assertEquals(1_284_678, donation.getBalance());
		assertEquals("Notch", donation.player);

		ShopTransaction shopTransaction = (ShopTransaction) it.next();
		assertEquals(new Date(1435428172000L), shopTransaction.getTs());
		assertEquals("Player shop sold 4 Purple Dye to AnguishedCarpet", shopTransaction.getDescription());
		assertEquals(28, shopTransaction.getAmount());
		assertEquals(1_284_614, shopTransaction.getBalance());
		assertEquals("AnguishedCarpet", shopTransaction.getShopCustomer());
		assertNull(shopTransaction.getShopOwner());
		assertEquals(-4, shopTransaction.getQuantity());
		assertEquals("Purple Dye", shopTransaction.getItem());

		PaymentTransaction paymentTransaction = (PaymentTransaction) it.next();
		assertEquals(new Date(1435428159000L), paymentTransaction.getTs());
		assertEquals("Payment to AnguishedCarpet", paymentTransaction.getDescription());
		assertEquals(-32, paymentTransaction.getAmount());
		assertEquals(1_284_586, paymentTransaction.getBalance());
		assertEquals("AnguishedCarpet", paymentTransaction.getPlayer());
		assertNull(paymentTransaction.getReason());

		BonusFeeTransaction bonusFeeTransaction = (BonusFeeTransaction) it.next();
		assertEquals(new Date(1435427901000L), bonusFeeTransaction.getTs());
		assertEquals("Daily sign-in bonus", bonusFeeTransaction.getDescription());
		assertEquals(400, bonusFeeTransaction.getAmount());
		assertEquals(1_284_554, bonusFeeTransaction.getBalance());
		assertEquals(BonusFeeType.SIGN_IN_BONUS, bonusFeeTransaction.getType());

		RupeeTransaction transaction = it.next();
		assertEquals(new SimpleDateFormat("MM/dd/yyyy HH:mm").parse("11/22/2012 21:54"), transaction.getTs());
		assertEquals("Week-old transaction", transaction.getDescription());
		assertEquals(-100, transaction.getAmount());
		assertEquals(212_990, transaction.getBalance());

		assertFalse(it.hasNext());
	}

	@Test
	public void not_logged_in() throws Exception {
		Document document = load("transaction-page-not-logged-in.html");
		RupeeTransactionPageScraper scraper = new RupeeTransactionPageScraper();
		RupeeTransactionPage page = scraper.scrape(document);
		assertNull(page);
	}

	@Test
	public void invalid_rupee_balance() throws Exception {
		Document document = load("transaction-page-invalid-rupee-balance.html");
		RupeeTransactionPageScraper scraper = new RupeeTransactionPageScraper();
		RupeeTransactionPage page = scraper.scrape(document);

		assertEquals(Integer.valueOf(3), page.getPage());
		assertEquals(Integer.valueOf(2_654), page.getTotalPages());
		assertNull(page.getRupeeBalance());
	}

	@Test
	public void missing_rupee_balance() throws Exception {
		Document document = load("transaction-page-missing-rupee-balance.html");
		RupeeTransactionPageScraper scraper = new RupeeTransactionPageScraper();
		RupeeTransactionPage page = scraper.scrape(document);

		assertEquals(Integer.valueOf(3), page.getPage());
		assertEquals(Integer.valueOf(2_654), page.getTotalPages());
		assertNull(page.getRupeeBalance());
	}

	private Document load(String file) throws IOException {
		try (InputStream in = getClass().getResourceAsStream(file)) {
			return Jsoup.parse(in, "UTF-8", "");
		}
	}

	private static class DonationTransactionScribe extends RupeeTransactionScribe<DonationTransaction.Builder> {
		private final Pattern regex = Pattern.compile("^Donation to (.*)");

		@Override
		public DonationTransaction.Builder parse(String description) {
			Matcher m = regex.matcher(description);
			if (!m.find()) {
				return null;
			}

			return new DonationTransaction.Builder().player(m.group(1));
		}
	}

	public static class DonationTransaction extends RupeeTransaction {
		private final String player;

		private DonationTransaction(Builder builder) {
			super(builder);
			this.player = builder.player;
		}

		public static class Builder extends RupeeTransaction.Builder {
			private String player;

			public Builder player(String player) {
				this.player = player;
				return this;
			}

			public DonationTransaction build() {
				return new DonationTransaction(this);
			}
		}
	}
}
