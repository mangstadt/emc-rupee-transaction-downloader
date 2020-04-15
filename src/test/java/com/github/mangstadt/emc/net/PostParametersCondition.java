package com.github.mangstadt.emc.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.github.paweladamski.httpclientmock.Request;
import com.github.paweladamski.httpclientmock.condition.Condition;

/**
 * Used for unit testing the body of POST requests that contain form parameters.
 * @author Michael Angstadt
 */
public class PostParametersCondition implements Condition {
	private final Set<BasicNameValuePair> expected;

	private PostParametersCondition(Set<BasicNameValuePair> expected) {
		this.expected = expected;
	}

	/**
	 * Creates a condition that validates the parameters in a POST request body.
	 * @param expectedPairs the expected name/value pairs
	 * @return the condition
	 */
	public static PostParametersCondition postParams(String... expectedPairs) {
		if (expectedPairs.length % 2 != 0) {
			throw new IllegalArgumentException("There must be an even number of parameters.");
		}

		return new PostParametersCondition(createSet(expectedPairs, false));
	}

	@Override
	public boolean matches(Request request) {
		HttpPost post = (HttpPost) request.getHttpRequest();

		String body;
		try {
			body = EntityUtils.toString(post.getEntity());
		} catch (ParseException | IOException e) {
			throw new RuntimeException(e);
		}

		Set<BasicNameValuePair> actual = createSet(body.split("[=&]"), true);
		return expected.equals(actual);
	}

	private static Set<BasicNameValuePair> createSet(String[] data, boolean decode) {
		Set<BasicNameValuePair> pairs = new HashSet<>();

		for (int i = 0; i < data.length; i += 2) {
			String name = data[i];
			String value = data[i + 1];

			if (decode) {
				name = decode(name);
				value = decode(value);
			}

			pairs.add(new BasicNameValuePair(name, value));
		}

		return pairs;
	}

	private static String decode(String value) {
		try {
			return URLDecoder.decode(value, "UTF-8");
		} catch (UnsupportedEncodingException ignore) {
			throw new RuntimeException(ignore);
		}
	}

}