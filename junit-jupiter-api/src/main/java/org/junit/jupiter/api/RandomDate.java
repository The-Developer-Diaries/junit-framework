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
 * {@code @RandomDate} is used to annotate a date/time parameter in a test
 * method to indicate that the framework should inject a randomly generated
 * date within a specified range.
 *
 * <h2>Supported Types</h2>
 *
 * <ul>
 * <li>{@link java.time.LocalDate}</li>
 * <li>{@link java.time.LocalDateTime}</li>
 * <li>{@link java.time.Instant}</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * @Test
 * void testWithRandomDate(@RandomDate(from = "2020-01-01", to = "2025-12-31") LocalDate date) {
 *     assertFalse(date.isBefore(LocalDate.of(2020, 1, 1)));
 * }
 *
 * @Test
 * void testWithRandomDateTime(@RandomDate LocalDateTime dateTime) {
 *     // Random date/time within the default range (past year to now)
 * }
 * }</pre>
 *
 * @since 6.1
 * @see Random
 * @see RandomString
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "6.1")
public @interface RandomDate {

	/**
	 * The earliest date (inclusive) in ISO-8601 format ({@code yyyy-MM-dd}).
	 *
	 * <p>Defaults to {@code "2000-01-01"}.
	 */
	String from() default "2000-01-01";

	/**
	 * The latest date (inclusive) in ISO-8601 format ({@code yyyy-MM-dd}).
	 *
	 * <p>Defaults to {@code "2030-12-31"}.
	 */
	String to() default "2030-12-31";

}
