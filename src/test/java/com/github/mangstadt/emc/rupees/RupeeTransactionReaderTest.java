package com.github.mangstadt.emc.rupees;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.LogManager;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.mangstadt.emc.net.EmcWebsiteConnection;
import com.github.mangstadt.emc.net.InvalidSessionException;
import com.github.mangstadt.emc.rupees.RupeeTransactionReader.PageSource;
import com.github.mangstadt.emc.rupees.dto.RupeeTransaction;
import com.github.mangstadt.emc.rupees.dto.RupeeTransactionPage;

/**
 * @author Michael Angstadt
 */
public class RupeeTransactionReaderTest {
	@BeforeClass
	public static void beforeClass() {
		LogManager.getLogManager().reset();
	}

	@Test
	public void maintain_page_order() throws Exception {
		//@formatter:off
		TransactionGenerator gen = new TransactionGenerator();
		int pageCount = 1;
		List<RupeeTransactionPage> pages = Arrays.asList(
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3))
		);
		//@formatter:on

		PageProducerMock pageProducer = spy(new PageProducerMock(pages));
		pageProducer.sleepOnPage(2, 1000);
		pageProducer.sleepOnPage(3, 500);

		List<RupeeTransaction> expectedTransactions = new ArrayList<RupeeTransaction>();
		for (RupeeTransactionPage page : pages) {
			expectedTransactions.addAll(page.getTransactions());
		}

		//@formatter:off
		RupeeTransactionReader reader = new RupeeTransactionReader
			.Builder(pageProducer)
			.threads(2)
			.build();
		//@formatter:on

		assertTransactionOrder(expectedTransactions, reader);
		verify(pageProducer).createSession();

		//one time for each thread - 1
		verify(pageProducer, times(1)).recreateConnection(any(EmcWebsiteConnection.class));
	}

	@Test
	public void ignore_duplicate_transactions() throws IOException {
		TransactionGenerator gen = new TransactionGenerator();
		int pageCount = 1;
		List<RupeeTransactionPage> pages = new ArrayList<RupeeTransactionPage>();

		List<RupeeTransaction> transactionsPage1 = gen.next(5);
		pages.add(new RupeeTransactionPage(1, pageCount++, 2, transactionsPage1));

		List<RupeeTransaction> transactionsPage2 = new ArrayList<RupeeTransaction>();
		transactionsPage2.add(transactionsPage1.get(3));
		transactionsPage2.add(transactionsPage1.get(4));
		transactionsPage2.addAll(gen.next(3));
		pages.add(new RupeeTransactionPage(1, pageCount++, 2, transactionsPage2));

		PageProducerMock pageProducer = spy(new PageProducerMock(pages));

		List<RupeeTransaction> expectedTransactions = new ArrayList<RupeeTransaction>();
		expectedTransactions.addAll(transactionsPage1);
		expectedTransactions.addAll(transactionsPage2.subList(2, 5));

		//@formatter:off
		RupeeTransactionReader reader = new RupeeTransactionReader
			.Builder(pageProducer)
			.threads(2)
			.build();
		//@formatter:on

		assertTransactionOrder(expectedTransactions, reader);
		verify(pageProducer).createSession();

		//one time for each thread - 1
		verify(pageProducer, times(1)).recreateConnection(any(EmcWebsiteConnection.class));
	}

	@Test
	public void startPage() throws IOException {
		//@formatter:off
		TransactionGenerator gen = new TransactionGenerator();
		int pageCount = 1;
		List<RupeeTransactionPage> pages = Arrays.asList(
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3))
		);
		//@formatter:on

		PageProducerMock pageProducer = spy(new PageProducerMock(pages));

		List<RupeeTransaction> expectedTransactions = new ArrayList<RupeeTransaction>();
		for (int i = 1; i < pages.size(); i++) {
			expectedTransactions.addAll(pages.get(i).getTransactions());
		}

		//@formatter:off
		RupeeTransactionReader reader = new RupeeTransactionReader
			.Builder(pageProducer)
			.threads(2)
			.start(2)
			.build();
		//@formatter:on

		assertTransactionOrder(expectedTransactions, reader);
		verify(pageProducer).createSession();

		//one time for each thread - 1
		verify(pageProducer, times(1)).recreateConnection(any(EmcWebsiteConnection.class));
	}

	@Test
	public void startDate() throws IOException {
		Date latestTransactionDate = new Date();

		//@formatter:off
		TransactionGenerator gen = new TransactionGenerator(latestTransactionDate);
		int pageCount = 1;
		List<RupeeTransactionPage> pages = Arrays.asList(
			new RupeeTransactionPage(1000, pageCount++, 5, gen.next(5)),
			new RupeeTransactionPage(1000, pageCount++, 5, gen.next(5)),
			new RupeeTransactionPage(1000, pageCount++, 5, gen.next(5)),
			new RupeeTransactionPage(1000, pageCount++, 5, gen.next(5)),
			new RupeeTransactionPage(1000, pageCount++, 5, gen.next(5))
		);
		//@formatter:on

		PageProducerMock pageProducer = spy(new PageProducerMock(pages));

		List<RupeeTransaction> expectedTransactions = new ArrayList<RupeeTransaction>();
		expectedTransactions.addAll(pages.get(1).getTransactions().subList(2, 5));
		for (int i = 2; i < pages.size(); i++) {
			expectedTransactions.addAll(pages.get(i).getTransactions());
		}

		//@formatter:off
		Calendar cal = Calendar.getInstance();
		cal.setTime(latestTransactionDate);
		cal.add(Calendar.HOUR_OF_DAY, -7);
		RupeeTransactionReader reader = new RupeeTransactionReader
			.Builder(pageProducer)
			.threads(2)
			.start(cal.getTime())
			.build();
		//@formatter:on

		assertTransactionOrder(expectedTransactions, reader);
		verify(pageProducer).createSession();

		//one time for each thread - 1
		verify(pageProducer, times(1)).recreateConnection(any(EmcWebsiteConnection.class));
	}

	@Test
	public void startDate_not_on_any_page() throws IOException {
		Date latestTransactionDate = new Date();

		TransactionGenerator gen = new TransactionGenerator(latestTransactionDate);
		int pageCount = 1;
		List<RupeeTransactionPage> pages = new ArrayList<RupeeTransactionPage>();
		pages.add(new RupeeTransactionPage(1000, pageCount++, 2, gen.next(5)));
		gen.next(13);
		pages.add(new RupeeTransactionPage(1000, pageCount++, 2, gen.next(5)));

		PageProducerMock pageProducer = spy(new PageProducerMock(pages));

		List<RupeeTransaction> expectedTransactions = new ArrayList<RupeeTransaction>();
		expectedTransactions.addAll(pages.get(1).getTransactions());

		//@formatter:off
		Calendar cal = Calendar.getInstance();
		cal.setTime(latestTransactionDate);
		cal.add(Calendar.HOUR_OF_DAY, -7);
		RupeeTransactionReader reader = new RupeeTransactionReader
			.Builder(pageProducer)
			.threads(2)
			.start(cal.getTime())
			.build();
		//@formatter:on

		assertTransactionOrder(expectedTransactions, reader);
		verify(pageProducer).createSession();

		//one time for each thread - 1
		verify(pageProducer, times(1)).recreateConnection(any(EmcWebsiteConnection.class));
	}

	@Test
	public void recoverable_ioexceptions_thrown_once() throws Exception {
		IOException exceptions[] = { new ConnectException(), new SocketTimeoutException() };
		for (IOException exception : exceptions) {
			//@formatter:off
			TransactionGenerator gen = new TransactionGenerator();
			int pageCount = 1;
			List<RupeeTransactionPage> pages = Arrays.asList(
				new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
				new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
				new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
				new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3))
			);
			//@formatter:on

			PageProducerMock pageProducer = spy(new PageProducerMock(pages));
			pageProducer.throwOnPage(2, exception);

			List<RupeeTransaction> expectedTransactions = new ArrayList<RupeeTransaction>();
			for (RupeeTransactionPage page : pages) {
				expectedTransactions.addAll(page.getTransactions());
			}

			//@formatter:off
			RupeeTransactionReader reader = new RupeeTransactionReader
				.Builder(pageProducer)
				.threads(2)
				.build();
			//@formatter:on

			assertTransactionOrder(expectedTransactions, reader);
			verify(pageProducer).createSession();

			//one time for each thread - 1, and then once again for when the exception is thrown
			verify(pageProducer, times(2)).recreateConnection(any(EmcWebsiteConnection.class));
		}
	}

	@Test
	public void recoverable_ioexceptions_thrown_twice() throws Exception {
		@SuppressWarnings("unchecked")
		List<Class<? extends IOException>> exceptions = Arrays.asList(ConnectException.class, SocketTimeoutException.class);
		for (Class<? extends IOException> exception : exceptions) {
			//@formatter:off
			TransactionGenerator gen = new TransactionGenerator();
			int pageCount = 1;
			List<RupeeTransactionPage> pages = Arrays.asList(
				new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
				new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
				new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
				new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3))
			);
			//@formatter:on

			PageProducerMock pageProducer = spy(new PageProducerMock(pages));

			//even though the exception is thrown before page 2 is downloaded, page 2 should still be returned
			pageProducer.sleepOnPage(2, 200);
			pageProducer.throwOnPage(3, exception.newInstance());
			IOException secondException = exception.newInstance();
			pageProducer.throwOnPage(3, secondException);

			List<RupeeTransaction> expectedTransactions = new ArrayList<RupeeTransaction>();
			expectedTransactions.addAll(pages.get(0).getTransactions());
			expectedTransactions.addAll(pages.get(1).getTransactions());

			//@formatter:off
			RupeeTransactionReader reader = new RupeeTransactionReader
				.Builder(pageProducer)
				.threads(2)
				.build();
			//@formatter:on

			try {
				assertTransactionOrder(expectedTransactions, reader);
				fail("IOException expected.");
			} catch (IOException e) {
				assertSame(secondException, e.getCause());
			}

			verify(pageProducer).createSession();

			//one time for each thread - 1, and then once again for when the exception is thrown
			verify(pageProducer, times(2)).recreateConnection(any(EmcWebsiteConnection.class));
		}
	}

	@Test
	public void exception_thrown() throws Exception {
		//@formatter:off
		TransactionGenerator gen = new TransactionGenerator();
		int pageCount = 1;
		List<RupeeTransactionPage> pages = Arrays.asList(
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3))
		);
		//@formatter:on

		PageProducerMock pageProducer = spy(new PageProducerMock(pages));
		//even though the exception is thrown before page 2 is downloaded, page 2 should still be returned
		pageProducer.sleepOnPage(2, 200);
		Exception exception = new RuntimeException();
		pageProducer.throwOnPage(3, exception);

		List<RupeeTransaction> expectedTransactions = new ArrayList<RupeeTransaction>();
		expectedTransactions.addAll(pages.get(0).getTransactions());
		expectedTransactions.addAll(pages.get(1).getTransactions());

		//@formatter:off
		RupeeTransactionReader reader = new RupeeTransactionReader
			.Builder(pageProducer)
			.threads(2)
			.build();
		//@formatter:on

		try {
			assertTransactionOrder(expectedTransactions, reader);
			fail("IOException expected.");
		} catch (IOException e) {
			assertSame(exception, e.getCause());
		}
		verify(pageProducer).createSession();

		//one time for each thread - 1
		verify(pageProducer, times(1)).recreateConnection(any(EmcWebsiteConnection.class));
	}

	@Test
	public void session_expired() throws IOException {
		//@formatter:off
		TransactionGenerator gen = new TransactionGenerator();
		int pageCount = 1;
		List<RupeeTransactionPage> pages = Arrays.asList(
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3))
		);
		//@formatter:on

		PageProducerMock pageProducer = spy(new PageProducerMock(pages));
		pageProducer.expireOnPage(2);

		List<RupeeTransaction> expectedTransactions = new ArrayList<RupeeTransaction>();
		for (RupeeTransactionPage page : pages) {
			expectedTransactions.addAll(page.getTransactions());
		}

		//@formatter:off
		RupeeTransactionReader reader = new RupeeTransactionReader
			.Builder(pageProducer)
			.threads(2)
			.build();
		//@formatter:on

		assertTransactionOrder(expectedTransactions, reader);

		//a new connection is created when it sees that the session expired (a null RupeeTransactionPage is returned by the PageProducer)
		verify(pageProducer, times(2)).createSession();

		//one time for each thread - 1
		verify(pageProducer, times(1)).recreateConnection(any(EmcWebsiteConnection.class));
	}

	@Test
	public void session_expired_cannot_recreate() throws IOException {
		//@formatter:off
		TransactionGenerator gen = new TransactionGenerator();
		int pageCount = 1;
		List<RupeeTransactionPage> pages = Arrays.asList(
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3)),
			new RupeeTransactionPage(1000, pageCount++, 4, gen.next(3))
		);
		//@formatter:on

		PageProducerMock pageProducer = spy(new PageProducerMock(pages));
		pageProducer.expireOnPage(2);
		pageProducer.expireOnPage(2);

		List<RupeeTransaction> expectedTransactions = new ArrayList<RupeeTransaction>();
		expectedTransactions.addAll(pages.get(0).getTransactions());

		//@formatter:off
		RupeeTransactionReader reader = new RupeeTransactionReader
			.Builder(pageProducer)
			.threads(2)
			.build();
		//@formatter:on

		try {
			assertTransactionOrder(expectedTransactions, reader);
			fail("IOException expected.");
		} catch (IOException e) {
			assertTrue(e.getCause() instanceof InvalidSessionException);
		}

		//a new connection is created when it sees that the session expired (a null RupeeTransactionPage is returned by the PageProducer)
		verify(pageProducer, times(2)).createSession();

		//one time for each thread - 1
		verify(pageProducer, times(1)).recreateConnection(any(EmcWebsiteConnection.class));
	}

	private static void assertTransactionOrder(List<RupeeTransaction> expectedTransactions, RupeeTransactionReader reader) throws IOException {
		Iterator<RupeeTransaction> expectedOrder = expectedTransactions.iterator();

		try {
			RupeeTransaction actual;
			while ((actual = reader.next()) != null) {
				RupeeTransaction expected = expectedOrder.next();
				assertSame(expected, actual);
			}
		} finally {
			assertFalse(expectedOrder.hasNext());
		}
	}

	private static class PageProducerMock implements PageSource {
		private final List<RupeeTransactionPage> pages;
		private final List<List<Exception>> exceptions;
		private final List<Integer> sleep;
		private final List<List<Boolean>> expires;

		public PageProducerMock(List<RupeeTransactionPage> pages) {
			this.pages = pages;

			int pageCount = pages.size();
			exceptions = new ArrayList<List<Exception>>(pageCount);
			sleep = new ArrayList<Integer>(pageCount);
			expires = new ArrayList<List<Boolean>>(pageCount);
			for (int i = 0; i < pageCount; i++) {
				exceptions.add(new ArrayList<Exception>());
				sleep.add(null);
				expires.add(new ArrayList<Boolean>());
			}
		}

		@Override
		public RupeeTransactionPage getPage(int pageNumber, EmcWebsiteConnection connection) throws IOException {
			if (pageNumber > pages.size()) {
				pageNumber = 1;
			}

			int index = pageNumber - 1;
			List<Exception> list = exceptions.get(index);
			if (!list.isEmpty()) {
				//simulate a network error
				Exception e = list.remove(0);
				if (e instanceof IOException) {
					throw (IOException) e;
				}
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				fail("Unit test can only throw IOExceptions or RuntimeExceptions.");
			}

			Integer sleep = this.sleep.get(index);
			if (sleep != null) {
				//simulate network lag
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					//empty
				}
			}

			List<Boolean> expires = this.expires.get(index);
			if (!expires.isEmpty()) {
				//simulate expired session
				expires.remove(0);
				return null;
			}

			return pages.get(index);
		}

		public void sleepOnPage(int pageNumber, int sleep) {
			this.sleep.set(pageNumber - 1, sleep);
		}

		public void throwOnPage(int pageNumber, Exception e) {
			this.exceptions.get(pageNumber - 1).add(e);
		}

		public void expireOnPage(int pageNumber) {
			this.expires.get(pageNumber - 1).add(true);
		}

		@Override
		public EmcWebsiteConnection recreateConnection(EmcWebsiteConnection connection) throws IOException {
			return mock(EmcWebsiteConnection.class);
		}

		@Override
		public EmcWebsiteConnection createSession() throws IOException {
			return mock(EmcWebsiteConnection.class);
		}
	}

	private static class TransactionGenerator implements Iterator<RupeeTransaction> {
		private final Calendar calendar = Calendar.getInstance();

		public TransactionGenerator() {
			//empty
		}

		public TransactionGenerator(Date date) {
			calendar.setTime(date);
		}

		@Override
		public boolean hasNext() {
			return true;
		}

		@Override
		public RupeeTransaction next() {
			Date ts = calendar.getTime();
			calendar.add(Calendar.HOUR_OF_DAY, -1);

			//@formatter:off
			return new RupeeTransaction.Builder()
			.ts(ts)
			.amount(1)
			.balance(1)
			.description("Description")
			.build();
			//@formatter:on
		}

		public List<RupeeTransaction> next(int count) {
			List<RupeeTransaction> list = new ArrayList<RupeeTransaction>(count);
			for (int i = 0; i < count; i++) {
				list.add(next());
			}
			return list;
		}

		@Override
		public void remove() {
			//empty
		}
	}
}
