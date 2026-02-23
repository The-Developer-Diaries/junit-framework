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
 * {@code @PropertyTest} is used to signal that the annotated method is a
 * <em>property-based test</em> that should be executed multiple times with
 * randomly generated input values, in the style of QuickCheck.
 *
 * <p>Each invocation of the property test behaves like the execution of a
 * regular {@link Test @Test} method with full support for the same lifecycle
 * callbacks and extensions. Parameters annotated with {@link ForAll @ForAll}
 * will be resolved with randomly generated values for each trial.
 *
 * <p>If any trial fails, the framework reports the exact input values and
 * the seed used, enabling deterministic reproduction of the failure.
 *
 * <p>{@code @PropertyTest} methods must not be {@code private} or {@code static}
 * and must return {@code void}.
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * @PropertyTest
 * void additionIsCommutative(@ForAll int a, @ForAll int b) {
 *     assertEquals(a + b, b + a);
 * }
 *
 * @PropertyTest(trials = 500, seed = 42)
 * void stringConcatLength(@ForAll String s1, @ForAll String s2) {
 *     assertEquals(s1.length() + s2.length(), (s1 + s2).length());
 * }
 * }</pre>
 *
 * <h2>Supported Parameter Types</h2>
 *
 * <p>The following types are supported for {@link ForAll @ForAll} parameters:
 * <ul>
 * <li>Primitive types: {@code int}, {@code long}, {@code double}, {@code float},
 *     {@code short}, {@code byte}, {@code char}, {@code boolean}</li>
 * <li>Wrapper types: {@code Integer}, {@code Long}, {@code Double}, {@code Float},
 *     {@code Short}, {@code Byte}, {@code Character}, {@code Boolean}</li>
 * <li>{@code String}</li>
 * </ul>
 *
 * <h2>Reproducibility</h2>
 *
 * <p>When a trial fails, the failure message includes the seed that was used.
 * To reproduce the exact same sequence of random values, specify that seed
 * using the {@link #seed()} attribute.
 *
 * <p><strong>WARNING</strong>: if the trials of a {@code @PropertyTest}
 * method are executed in parallel, no guarantees can be made regarding
 * reproducibility. It is therefore recommended that a {@code @PropertyTest}
 * method be annotated with
 * {@link org.junit.jupiter.api.parallel.Execution @Execution(SAME_THREAD)}
 * when parallel execution is configured.
 *
 * @since 6.1
 * @see ForAll
 * @see Test
 * @see TestTemplate
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "6.1")
@TestTemplate
public @interface PropertyTest {

	/**
	 * Placeholder for the {@linkplain TestInfo#getDisplayName display name} of
	 * a {@code @PropertyTest} method: <code>{displayName}</code>
	 */
	String DISPLAY_NAME_PLACEHOLDER = "{displayName}";

	/**
	 * Placeholder for the current trial number of a {@code @PropertyTest}
	 * method: <code>{currentTrial}</code>
	 */
	String CURRENT_TRIAL_PLACEHOLDER = "{currentTrial}";

	/**
	 * Placeholder for the total number of trials: <code>{totalTrials}</code>
	 */
	String TOTAL_TRIALS_PLACEHOLDER = "{totalTrials}";

	/**
	 * <em>Short</em> display name pattern for a property test: {@value}
	 */
	String SHORT_DISPLAY_NAME = "trial " + CURRENT_TRIAL_PLACEHOLDER + " of " + TOTAL_TRIALS_PLACEHOLDER;

	/**
	 * <em>Long</em> display name pattern for a property test: {@value}
	 */
	String LONG_DISPLAY_NAME = DISPLAY_NAME_PLACEHOLDER + " :: " + SHORT_DISPLAY_NAME;

	/**
	 * The number of trials (random invocations) to execute.
	 *
	 * <p>Defaults to {@code 100}.
	 *
	 * @return the number of trials; must be greater than zero
	 */
	int trials() default 100;

	/**
	 * The seed for the random number generator.
	 *
	 * <p>Setting a specific seed makes the random value generation
	 * deterministic, which is useful for reproducing a failed property test.
	 *
	 * <p>Defaults to {@code 0}, which means a random seed will be chosen
	 * automatically.
	 *
	 * @return the seed value; {@code 0} for a randomly chosen seed
	 */
	long seed() default 0;

	/**
	 * The display name for each trial of the property test.
	 *
	 * <h4>Supported placeholders</h4>
	 * <ul>
	 * <li>{@link #DISPLAY_NAME_PLACEHOLDER}</li>
	 * <li>{@link #CURRENT_TRIAL_PLACEHOLDER}</li>
	 * <li>{@link #TOTAL_TRIALS_PLACEHOLDER}</li>
	 * </ul>
	 *
	 * <p>Defaults to {@link #SHORT_DISPLAY_NAME}.
	 *
	 * @return a custom display name; never blank
	 */
	String name() default SHORT_DISPLAY_NAME;

}
