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
 * {@code @ForAll} is used to annotate parameters in a {@link PropertyTest @PropertyTest}
 * method to indicate that the framework should generate random values for them.
 *
 * <p>The type of the annotated parameter determines what kind of random values
 * are generated. Optional constraints can be specified to narrow the range of
 * generated values.
 *
 * <h2>Supported Types</h2>
 *
 * <ul>
 * <li>{@code int} / {@code Integer} — random integers within [{@link #minInt()}, {@link #maxInt()}]</li>
 * <li>{@code long} / {@code Long} — random longs within [{@link #minLong()}, {@link #maxLong()}]</li>
 * <li>{@code double} / {@code Double} — random doubles within [{@link #minDouble()}, {@link #maxDouble()})</li>
 * <li>{@code float} / {@code Float} — random floats within [0.0, 1.0)</li>
 * <li>{@code short} / {@code Short} — random shorts</li>
 * <li>{@code byte} / {@code Byte} — random bytes</li>
 * <li>{@code char} / {@code Character} — random printable ASCII characters</li>
 * <li>{@code boolean} / {@code Boolean} — random booleans</li>
 * <li>{@code String} — random strings of printable ASCII characters with
 *     length within [{@link #minLength()}, {@link #maxLength()}]</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * @PropertyTest
 * void additionIsCommutative(@ForAll int a, @ForAll int b) {
 *     assertEquals(a + b, b + a);
 * }
 *
 * @PropertyTest
 * void boundedValues(@ForAll(minInt = 0, maxInt = 100) int age,
 *                    @ForAll(maxLength = 50) String name) {
 *     assertTrue(age >= 0 && age <= 100);
 *     assertTrue(name.length() <= 50);
 * }
 * }</pre>
 *
 * @since 6.1
 * @see PropertyTest
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "6.1")
public @interface ForAll {

	// --- Integer constraints ---

	/**
	 * The minimum value (inclusive) for {@code int}/{@code Integer} parameters.
	 *
	 * <p>Defaults to {@link Integer#MIN_VALUE}.
	 */
	int minInt() default Integer.MIN_VALUE;

	/**
	 * The maximum value (inclusive) for {@code int}/{@code Integer} parameters.
	 *
	 * <p>Defaults to {@link Integer#MAX_VALUE}.
	 */
	int maxInt() default Integer.MAX_VALUE;

	// --- Long constraints ---

	/**
	 * The minimum value (inclusive) for {@code long}/{@code Long} parameters.
	 *
	 * <p>Defaults to {@link Long#MIN_VALUE}.
	 */
	long minLong() default Long.MIN_VALUE;

	/**
	 * The maximum value (inclusive) for {@code long}/{@code Long} parameters.
	 *
	 * <p>Defaults to {@link Long#MAX_VALUE}.
	 */
	long maxLong() default Long.MAX_VALUE;

	// --- Double constraints ---

	/**
	 * The minimum value (inclusive) for {@code double}/{@code Double} parameters.
	 *
	 * <p>Defaults to {@code -1000.0}.
	 */
	double minDouble() default -1000.0;

	/**
	 * The maximum value (exclusive) for {@code double}/{@code Double} parameters.
	 *
	 * <p>Defaults to {@code 1000.0}.
	 */
	double maxDouble() default 1000.0;

	// --- String constraints ---

	/**
	 * The minimum length (inclusive) for {@code String} parameters.
	 *
	 * <p>Defaults to {@code 0}.
	 */
	int minLength() default 0;

	/**
	 * The maximum length (inclusive) for {@code String} parameters.
	 *
	 * <p>Defaults to {@code 100}.
	 */
	int maxLength() default 100;

}
