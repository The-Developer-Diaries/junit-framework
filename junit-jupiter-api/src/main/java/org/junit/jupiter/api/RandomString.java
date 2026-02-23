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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @RandomString} is used to annotate a {@code String} parameter in a
 * test method to indicate that the framework should inject a randomly
 * generated string.
 *
 * <p>The generated string consists of characters from the specified
 * {@link #charset()} and has a length within [{@link #minLength()},
 * {@link #maxLength()}].
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * @Test
 * void testWithRandomString(@RandomString(length = 10) String name) {
 *     assertEquals(10, name.length());
 * }
 *
 * @Test
 * void testVariableLength(@RandomString(minLength = 5, maxLength = 20) String value) {
 *     assertTrue(value.length() >= 5 && value.length() <= 20);
 * }
 *
 * @Test
 * void testAlphabetic(@RandomString(length = 8, charset = "abcdefghijklmnopqrstuvwxyz") String id) {
 *     assertTrue(id.matches("[a-z]{8}"));
 * }
 * }</pre>
 *
 * @since 6.1
 * @see Random
 * @see RandomDate
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "6.1")
public @interface RandomString {

	/**
	 * Default character set: alphanumeric characters.
	 */
	String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	/**
	 * Convenience shortcut: sets both {@link #minLength()} and
	 * {@link #maxLength()} to the same value.
	 *
	 * <p>Defaults to {@code -1}, meaning {@link #minLength()} and
	 * {@link #maxLength()} are used instead.
	 */
	int length() default -1;

	/**
	 * The minimum length (inclusive) of the generated string.
	 *
	 * <p>Ignored if {@link #length()} is set. Defaults to {@code 1}.
	 */
	int minLength() default 1;

	/**
	 * The maximum length (inclusive) of the generated string.
	 *
	 * <p>Ignored if {@link #length()} is set. Defaults to {@code 20}.
	 */
	int maxLength() default 20;

	/**
	 * The character set to draw from when generating the random string.
	 *
	 * <p>Defaults to {@link #ALPHANUMERIC}.
	 */
	String charset() default ALPHANUMERIC;

}
