package com.github.mangstadt.emc.net;

import static com.github.mangstadt.emc.net.PostParametersCondition.postParams;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.http.impl.client.CloseableHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.github.paweladamski.httpclientmock.HttpClientMock;

/**
 * @author Michael Angstadt
 */
@SuppressWarnings("resource")
public class EmcWebsiteConnectionImplTest {
	@Test
	public void login_without_2fa() throws Exception {
		HttpClientMock client = new HttpClientMock("https://empireminecraft.com");
		client.onPost("/login/login").doReturnStatus(303);

		new EmcWebsiteConnectionImpl("user", "pass") {
			@Override
			CloseableHttpClient createClient() {
				return client;
			}
		};

		//@formatter:off
		client.verify().post("/login/login").with(postParams(
			"login", "user",
			"password", "pass"))
			.called();
		//@formatter:on
	}

	@Test
	public void login_with_2fa() throws Exception {
		HttpClientMock client = new HttpClientMock("https://empireminecraft.com");
		client.onPost("/login/login").doReturnStatus(303);

		new EmcWebsiteConnectionImpl("user", "pass", "code") {
			@Override
			CloseableHttpClient createClient() {
				return client;
			}
		};

		//@formatter:off
		client.verify().post("/login/login").with(postParams(
			"fh2fa-login", "user",
			"fh2fa-password", "pass",
			"key[GoogleAuthenticator]", "code"))
			.called();
		//@formatter:on
	}

	@Test
	public void login_bad_credentials() throws Exception {
		HttpClientMock client = new HttpClientMock("https://empireminecraft.com");
		client.onPost("/login/login").doReturn(200, readFileContents("bad-credentials.html"));

		try {
			new EmcWebsiteConnectionImpl("user", "pass") {
				@Override
				CloseableHttpClient createClient() {
					return client;
				}
			};
			fail("Expected InvalidCredentialsException to be thrown.");
		} catch (InvalidCredentialsException expected) {
		}

		//@formatter:off
		client.verify().post("/login/login").with(postParams(
			"login", "user",
			"password", "pass"))
			.called();
		//@formatter:on
	}

	@Test
	public void login_2fa_code_required() throws Exception {
		HttpClientMock client = new HttpClientMock("https://empireminecraft.com");
		client.onPost("/login/login").doReturn(200, readFileContents("2fa-code-required.html"));

		try {
			new EmcWebsiteConnectionImpl("user", "pass") {
				@Override
				CloseableHttpClient createClient() {
					return client;
				}
			};
			fail("Expected TwoFactorAuthException to be thrown.");
		} catch (TwoFactorAuthException expected) {
		}

		//@formatter:off
		client.verify().post("/login/login").with(postParams(
			"login", "user",
			"password", "pass"))
			.called();
		//@formatter:on
	}

	@Test
	public void login_bad_2fa_code() throws Exception {
		HttpClientMock client = new HttpClientMock("https://empireminecraft.com");
		client.onPost("/login/login").doReturn(200, readFileContents("bad-2fa-code.html"));

		try {
			new EmcWebsiteConnectionImpl("user", "pass", "code") {
				@Override
				CloseableHttpClient createClient() {
					return client;
				}
			};
			fail("Expected TwoFactorAuthException to be thrown.");
		} catch (TwoFactorAuthException expected) {
		}

		//@formatter:off
		client.verify().post("/login/login").with(postParams(
			"fh2fa-login", "user",
			"fh2fa-password", "pass",
			"key[GoogleAuthenticator]", "code"))
			.called();
		//@formatter:on
	}

	@Test
	public void login_bad_credentials_with_2fa_code() throws Exception {
		HttpClientMock client = new HttpClientMock("https://empireminecraft.com");
		client.onPost("/login/login").doReturn(200, readFileContents("bad-credentials.html"));

		try {
			new EmcWebsiteConnectionImpl("user", "pass", "code") {
				@Override
				CloseableHttpClient createClient() {
					return client;
				}
			};
			fail("Expected InvalidCredentialsException to be thrown.");
		} catch (InvalidCredentialsException expected) {
		}

		//@formatter:off
		client.verify().post("/login/login").with(postParams(
			"fh2fa-login", "user",
			"fh2fa-password", "pass",
			"key[GoogleAuthenticator]", "code"))
			.called();
		//@formatter:on
	}

	@Test
	public void getRupeeTransactionPage() throws Exception {
		HttpClientMock client = new HttpClientMock("https://empireminecraft.com");
		client.onPost("/login/login").doReturnStatus(303);
		client.onGet("/rupees/transactions/").withParameter("page", "1").doReturn("<html>page 1</html>");

		EmcWebsiteConnection connection = new EmcWebsiteConnectionImpl("user", "pass") {
			@Override
			CloseableHttpClient createClient() {
				return client;
			}
		};

		Document expected = Jsoup.parse("<html>page 1</html>");
		Document actual = connection.getRupeeTransactionPage(1);
		assertEquals(expected, actual);

		//@formatter:off
		client.verify().post("/login/login").with(postParams(
			"login", "user",
			"password", "pass"))
			.called();
		client.verify().get("/rupees/transactions/")
			.withParameter("page", "1")
			.called();
		//@formatter:on
	}

	@Test
	public void getProfilePage() throws Exception {
		HttpClientMock client = new HttpClientMock();
		client.onPost("https://empireminecraft.com/login/login").doReturnStatus(303);
		client.onGet("https://u.emc.gs/player").doReturn("<html>player</html>");

		EmcWebsiteConnection connection = new EmcWebsiteConnectionImpl("user", "pass") {
			@Override
			CloseableHttpClient createClient() {
				return client;
			}
		};

		Document expected = Jsoup.parse("<html>player</html>");
		Document actual = connection.getProfilePage("player");
		assertEquals(expected, actual);

		//@formatter:off
		client.verify().post("https://empireminecraft.com/login/login").with(postParams(
			"login", "user",
			"password", "pass"))
			.called();
		client.verify().get("https://u.emc.gs/player")
			.called();
		//@formatter:on
	}

	@Test
	public void getOnlinePlayers() throws Exception {
		HttpClientMock client = new HttpClientMock("https://empireminecraft.com");
		client.onPost("/login/login").doReturnStatus(303);
		client.onGet("/api/server-online-1.json").doReturnJSON( //@formatter:off
			"[" +
				"{\"group\":\"1\", \"name\":\"Player1\", \"start\":\"1586966260\", \"user_id\":\"123\"}," +
				"{\"group\":\"1\", \"name\":\"Player2\", \"start\":\"1586966260\", \"user_id\":\"456\"}," +
				"{\"group\":\"1\", \"start\":\"1586966260\", \"user_id\":\"789\"}" +
			"]"); //@formatter:on

		EmcWebsiteConnection connection = new EmcWebsiteConnectionImpl("user", "pass") {
			@Override
			CloseableHttpClient createClient() {
				return client;
			}
		};

		List<String> expected = Arrays.asList("Player1", "Player2");
		List<String> actual = connection.getOnlinePlayers(EmcServer.SMP1);
		assertEquals(expected, actual);

		//@formatter:off
		client.verify().post("/login/login").with(postParams(
			"login", "user",
			"password", "pass"))
			.called();
		client.verify().get("/api/server-online-1.json")
			.called();
		//@formatter:on
	}

	@Test
	public void getOnlinePlayers_no_players() throws Exception {
		HttpClientMock client = new HttpClientMock("https://empireminecraft.com");
		client.onPost("/login/login").doReturnStatus(303);
		client.onGet("/api/server-online-1.json").doReturnJSON( //@formatter:off
			"[" +
			"]"); //@formatter:on

		EmcWebsiteConnection connection = new EmcWebsiteConnectionImpl("user", "pass") {
			@Override
			CloseableHttpClient createClient() {
				return client;
			}
		};

		List<String> expected = Arrays.asList();
		List<String> actual = connection.getOnlinePlayers(EmcServer.SMP1);
		assertEquals(expected, actual);

		//@formatter:off
		client.verify().post("/login/login").with(postParams(
			"login", "user",
			"password", "pass"))
			.called();
		client.verify().get("/api/server-online-1.json")
			.called();
		//@formatter:on
	}

	@Test
	public void getOnlinePlayers_unexpected_JSON_structure() throws Exception {
		HttpClientMock client = new HttpClientMock("https://empireminecraft.com");
		client.onPost("/login/login").doReturnStatus(303);
		client.onGet("/api/server-online-1.json").doReturnJSON("{\"foo\":\"bar\"}");

		EmcWebsiteConnection connection = new EmcWebsiteConnectionImpl("user", "pass") {
			@Override
			CloseableHttpClient createClient() {
				return client;
			}
		};

		try {
			connection.getOnlinePlayers(EmcServer.SMP1);
			fail("Expected IOException to be thrown.");
		} catch (IOException expected) {
		}

		//@formatter:off
		client.verify().post("/login/login").with(postParams(
			"login", "user",
			"password", "pass"))
			.called();
		client.verify().get("/api/server-online-1.json")
			.called();
		//@formatter:on
	}

	@Test
	public void getOnlinePlayers_invalid_JSON() throws Exception {
		HttpClientMock client = new HttpClientMock("https://empireminecraft.com");
		client.onPost("/login/login").doReturnStatus(303);
		client.onGet("/api/server-online-1.json").doReturnJSON("not JSON");

		EmcWebsiteConnection connection = new EmcWebsiteConnectionImpl("user", "pass") {
			@Override
			CloseableHttpClient createClient() {
				return client;
			}
		};

		try {
			connection.getOnlinePlayers(EmcServer.SMP1);
			fail("Expected IOException to be thrown.");
		} catch (IOException expected) {
		}

		//@formatter:off
		client.verify().post("/login/login").with(postParams(
			"login", "user",
			"password", "pass"))
			.called();
		client.verify().get("/api/server-online-1.json")
			.called();
		//@formatter:on
	}

	@Test
	public void getOnlinePlayers_empty_response() throws Exception {
		HttpClientMock client = new HttpClientMock("https://empireminecraft.com");
		client.onPost("/login/login").doReturnStatus(303);
		client.onGet("/api/server-online-1.json").doReturnJSON("");

		EmcWebsiteConnection connection = new EmcWebsiteConnectionImpl("user", "pass") {
			@Override
			CloseableHttpClient createClient() {
				return client;
			}
		};

		try {
			connection.getOnlinePlayers(EmcServer.SMP1);
			fail("Expected IOException to be thrown.");
		} catch (IOException expected) {
		}

		//@formatter:off
		client.verify().post("/login/login").with(postParams(
			"login", "user",
			"password", "pass"))
			.called();
		client.verify().get("/api/server-online-1.json")
			.called();
		//@formatter:on
	}

	private String readFileContents(String classpath) throws IOException, URISyntaxException {
		URI uri = getClass().getResource(classpath).toURI();
		Path path = Paths.get(uri);
		return new String(Files.readAllBytes(path), "UTF-8");
	}
}
