/*
 * Copyright 2015-2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

/**
 * {@code AssertMatchesRegex} is a collection of utility methods that support
 * asserting that a string matches or does not match a regular expression.
 *
 * @since 6.1
 */
class AssertMatchesRegex {

	private AssertMatchesRegex() {
		/* no-op */
	}

	static void assertMatchesRegex(String text, String regex) {
		assertMatchesRegex(text, regex, (Object) null);
	}

	static void assertMatchesRegex(String text, String regex, @Nullable String message) {
		assertMatchesRegex(text, regex, (Object) message);
	}

	static void assertMatchesRegex(String text, String regex, Supplier<@Nullable String> messageSupplier) {
		assertMatchesRegex(text, regex, (Object) messageSupplier);
	}

	private static void assertMatchesRegex(String text, String regex, @Nullable Object messageOrSupplier) {
		if (!Pattern.matches(regex, text)) {
			assertionFailure() //
					.message(messageOrSupplier) //
					.reason("String does not match regular expression") //
					.expected("a string matching /" + regex + "/") //
					.actual(text) //
					.trimStacktrace(Assertions.class) //
					.buildAndThrow();
		}
	}

	static void assertDoesNotMatchRegex(String text, String regex) {
		assertDoesNotMatchRegex(text, regex, (Object) null);
	}

	static void assertDoesNotMatchRegex(String text, String regex, @Nullable String message) {
		assertDoesNotMatchRegex(text, regex, (Object) message);
	}

	static void assertDoesNotMatchRegex(String text, String regex, Supplier<@Nullable String> messageSupplier) {
		assertDoesNotMatchRegex(text, regex, (Object) messageSupplier);
	}

	private static void assertDoesNotMatchRegex(String text, String regex, @Nullable Object messageOrSupplier) {
		if (Pattern.matches(regex, text)) {
			assertionFailure() //
					.message(messageOrSupplier) //
					.reason("String unexpectedly matches regular expression") //
					.expected("a string not matching /" + regex + "/") //
					.actual(text) //
					.trimStacktrace(Assertions.class) //
					.buildAndThrow();
		}
	}

}
