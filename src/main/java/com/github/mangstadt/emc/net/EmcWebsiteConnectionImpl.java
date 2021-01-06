package com.github.mangstadt.emc.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.UrlEscapers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Represents a connection to the EmpireMinecraft website.
 * @author Michael Angstadt
 */
public class EmcWebsiteConnectionImpl implements EmcWebsiteConnection {
	private static final Map<EmcServer, Integer> serverNumbers;
	static {
		ImmutableMap.Builder<EmcServer, Integer> map = ImmutableMap.builder();
		map.put(EmcServer.SMP1, 1);
		map.put(EmcServer.SMP2, 2);
		map.put(EmcServer.SMP3, 4);
		map.put(EmcServer.SMP4, 5);
		map.put(EmcServer.SMP5, 6);
		map.put(EmcServer.SMP6, 7);
		map.put(EmcServer.SMP7, 8);
		map.put(EmcServer.SMP8, 9);
		map.put(EmcServer.SMP9, 10);
		map.put(EmcServer.UTOPIA, 3);
		serverNumbers = map.build();
	}

	private final CloseableHttpClient client;
	private final CookieStore cookieStore;

	/**
	 * Creates an unauthenticated connection.
	 */
	public EmcWebsiteConnectionImpl() {
		this.cookieStore = new BasicCookieStore();
		this.client = createClient();
	}

	/**
	 * Creates a new HTTP connection using an existing cookie store.
	 * @param cookieStore the cookie store
	 */
	public EmcWebsiteConnectionImpl(CookieStore cookieStore) {
		this.cookieStore = copyOf(cookieStore);
		this.client = createClient();
	}

	/**
	 * Creates an authenticated connection.
	 * @param username the user's username
	 * @param password the user's password
	 * @throws InvalidCredentialsException if the username/password is incorrect
	 * @throws TwoFactorAuthException if a two-factor authentication code is
	 * required
	 * @throws IOException if there's a problem contacting the EMC website
	 */
	public EmcWebsiteConnectionImpl(String username, String password) throws IOException {
		this(username, password, null);
	}

	/**
	 * Creates an authenticated connection.
	 * @param username the user's username
	 * @param password the user's password
	 * @param twoFactorAuthCode the two factor authentication code (required if
	 * the user has two factor authentication enabled)
	 * @throws InvalidCredentialsException if the username/password is incorrect
	 * @throws TwoFactorAuthException if the two-factor authentication code is
	 * invalid
	 * @throws IOException if there's a problem contacting the EMC website
	 */
	public EmcWebsiteConnectionImpl(String username, String password, String twoFactorAuthCode) throws IOException {
		this();

		//log the user in
		login(username, password, twoFactorAuthCode);
	}

	/**
	 * This method is package-private so it can be overridden in unit-tests.
	 */
	CloseableHttpClient createClient() {
		int connectionTimeout = (int) TimeUnit.MINUTES.toMillis(10);

		//@formatter:off
		return HttpClientBuilder.create()
			.disableRedirectHandling()
			.setDefaultCookieStore(cookieStore)
			.setDefaultRequestConfig(RequestConfig.custom()
				.setConnectTimeout(connectionTimeout)
				.build())
			.setDefaultSocketConfig(SocketConfig.custom()
				.setSoTimeout(connectionTimeout)
				.build())
			.setUserAgent("EMC Rupee Transaction Downloader")
		.build();
		//@formatter:on
	}

	private static CookieStore copyOf(CookieStore original) {
		CookieStore copy = new BasicCookieStore();
		original.getCookies().forEach(copy::addCookie);
		return copy;
	}

	@Override
	public CookieStore getCookieStore() {
		return cookieStore;
	}

	@Override
	public HttpClient getHttpClient() {
		return client;
	}

	@Override
	public Document getRupeeTransactionPage(int pageNumber) throws IOException {
		/*
		 * Note: The HttpClient library is used here because using
		 * "Jsoup.connect()" doesn't always work when the application is run as
		 * a Web Start app.
		 * 
		 * The login dialog was repeatedly appearing because, even though the
		 * login was successful (a valid session cookie was generated), the
		 * TransactionPuller would fail when it tried to get the first
		 * transaction from the first page (i.e. when calling "isLoggedIn()").
		 * It was failing because it was getting back the unauthenticated
		 * version of the rupee page. It was as if jsoup wasn't sending the
		 * session cookie with the request.
		 * 
		 * The issue appeared to only occur when running under Web Start. It
		 * could not be reproduced when running via Eclipse.
		 */

		String base = "https://empireminecraft.com/rupees/transactions/";
		String url = base + "?page=" + pageNumber;

		HttpGet request = new HttpGet(url);
		try (CloseableHttpResponse response = client.execute(request)) {
			HttpEntity entity = response.getEntity();
			try (InputStream in = entity.getContent()) {
				return Jsoup.parse(in, "UTF-8", base);
			}
		}
	}

	@Override
	public Document getProfilePage(String playerName) throws IOException {
		String url = "https://u.emc.gs/" + UrlEscapers.urlPathSegmentEscaper().escape(playerName);
		HttpGet request = new HttpGet(url);
		try (CloseableHttpResponse response = client.execute(request)) {
			HttpEntity entity = response.getEntity();
			try (InputStream in = entity.getContent()) {
				return Jsoup.parse(in, "UTF-8", "https://empireminecraft.com");
			}
		}
	}

	@Override
	public List<String> getOnlinePlayers(EmcServer server) throws IOException {
		Integer serverNumber = serverNumbers.get(server);
		String url = "https://empireminecraft.com/api/server-online-" + serverNumber + ".json";
		HttpGet request = new HttpGet(url);

		JsonElement root;
		try (CloseableHttpResponse response = client.execute(request)) {
			HttpEntity entity = response.getEntity();
			try (Reader reader = new InputStreamReader(entity.getContent())) {
				root = new JsonParser().parse(reader);
			} catch (JsonParseException e) {
				throw new IOException(e);
			}
		}

		try {
			JsonArray array = root.getAsJsonArray();

			return StreamSupport.stream(array.spliterator(), false) //@formatter:off
				.map(JsonElement::getAsJsonObject)
				.map(player -> player.get("name"))
				.filter(Objects::nonNull)
				.map(JsonElement::getAsString)
			.collect(Collectors.toList()); //@formatter:on
		} catch (IllegalStateException e) {
			/*
			 * Thrown if the JSON is not structured as expected (e.g. if there's
			 * an array where an object should be).
			 */
			throw new IOException(e);
		}
	}

	/**
	 * Logs the user into the website.
	 * @param username the username
	 * @param password the password
	 * @param twoFactorAuthCode the two-factor authentication code, or null if
	 * the user does not have two-factor authentication enabled
	 * @throws InvalidCredentialsException if the username/password is incorrect
	 * @throws TwoFactorAuthException if a code is required or if the provided
	 * code is invalid
	 * @throws IOException if there's a problem contacting the EMC website
	 */
	private void login(String username, String password, String twoFactorAuthCode) throws IOException {
		String url = "https://empireminecraft.com/login/login";
		HttpPost request = new HttpPost(url);

		if (twoFactorAuthCode == null) {
			setPostParameters(request, //@formatter:off
				"login", username,
				"password", password
			); //@formatter:on
		} else {
			setPostParameters(request, //@formatter:off
				"fh2fa-login", username,
				"fh2fa-password", password,
				"key[GoogleAuthenticator]", twoFactorAuthCode
			); //@formatter:on
		}

		Document responseBody;
		try (CloseableHttpResponse response = client.execute(request)) {
			//client is redirected to the homepage on successful login
			if (response.getStatusLine().getStatusCode() == 303) {
				return;
			}

			try (InputStream in = response.getEntity().getContent()) {
				responseBody = Jsoup.parse(in, "UTF-8", url);
			}
		}

		boolean twoFactorAuthCodeRequired = (responseBody.getElementById("key-GoogleAuthenticator") != null);
		if (twoFactorAuthCodeRequired) {
			throw TwoFactorAuthException.codeRequired();
		}

		boolean twoFactorAuthCodeInvalid = !responseBody.select("span[class=errors]:contains(two-factor authentication)").isEmpty();
		if (twoFactorAuthCodeInvalid) {
			throw TwoFactorAuthException.codeIsInvalid(twoFactorAuthCode);
		}

		throw new InvalidCredentialsException(username, password);

	}

	private static void setPostParameters(HttpPost request, String... params) {
		if (params.length % 2 != 0) {
			throw new IllegalArgumentException("params vararg must have an even number of elements.");
		}

		List<NameValuePair> pairs = new ArrayList<>();
		for (int i = 0; i < params.length; i += 2) {
			String name = params[i];
			String value = params[i + 1];

			NameValuePair pair = new BasicNameValuePair(name, value);
			pairs.add(pair);
		}

		request.setEntity(new UrlEncodedFormEntity(pairs, Consts.UTF_8));
	}

	@Override
	public void close() throws IOException {
		client.close();
	}
}
