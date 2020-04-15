package com.github.mangstadt.emc.net;

import static com.github.mangstadt.emc.net.PostParametersCondition.postParams;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.impl.client.CloseableHttpClient;
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

	private String readFileContents(String classpath) throws IOException, URISyntaxException {
		URI uri = getClass().getResource(classpath).toURI();
		Path path = Paths.get(uri);
		return new String(Files.readAllBytes(path), "UTF-8");
	}
}
