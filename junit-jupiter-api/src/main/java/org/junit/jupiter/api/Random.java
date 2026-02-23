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
 * {@code @Random} is used to annotate a parameter in a test method to
 * indicate that the framework should inject a randomly generated numeric
 * value.
 *
 * <h2>Supported Types</h2>
 *
 * <ul>
 * <li>{@code int} / {@code Integer} — random integer within [{@link #min()}, {@link #max()}]</li>
 * <li>{@code long} / {@code Long} — random long within [{@link #min()}, {@link #max()}]</li>
 * <li>{@code double} / {@code Double} — random double within [{@link #minDouble()}, {@link #maxDouble()})</li>
 * <li>{@code float} / {@code Float} — random float within [0.0, 1.0)</li>
 * <li>{@code short} / {@code Short} — random short</li>
 * <li>{@code byte} / {@code Byte} — random byte</li>
 * <li>{@code boolean} / {@code Boolean} — random boolean</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * @Test
 * void testWithRandomData(@Random(min = 1, max = 100) int age,
 *                         @Random boolean active) {
 *     assertTrue(age >= 1 && age <= 100);
 * }
 * }</pre>
 *
 * <p>A new random value is generated for each test invocation. The values
 * are non-deterministic by default. For deterministic values in property-based
 * testing, see {@code @PropertyTest} and {@code @ForAll}.
 *
 * @since 6.1
 * @see RandomString
 * @see RandomDate
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "6.1")
public @interface Random {

	/**
	 * The minimum value (inclusive) for integer and long types.
	 *
	 * <p>Defaults to {@code 0}.
	 */
	long min() default 0;

	/**
	 * The maximum value (inclusive) for integer and long types.
	 *
	 * <p>Defaults to {@code 1000}.
	 */
	long max() default 1000;

	/**
	 * The minimum value (inclusive) for double types.
	 *
	 * <p>Defaults to {@code 0.0}.
	 */
	double minDouble() default 0.0;

	/**
	 * The maximum value (exclusive) for double types.
	 *
	 * <p>Defaults to {@code 1000.0}.
	 */
	double maxDouble() default 1000.0;

}
