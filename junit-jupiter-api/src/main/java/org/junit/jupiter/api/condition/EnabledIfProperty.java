/*
 * Copyright 2015-2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @EnabledIfProperty} is used to signal that the annotated test class
 * or test method is only <em>enabled</em> if a specified condition is met.
 *
 * <p>The condition can be specified in two ways:
 *
 * <ol>
 * <li><strong>Configuration parameter</strong>: set {@link #named()} to a
 * JUnit configuration parameter name and optionally {@link #matches()} to a
 * regex pattern. The test is enabled if the parameter exists and its value
 * matches the pattern (defaults to {@code "true"}).</li>
 * <li><strong>Method reference</strong>: set {@link #method()} to reference a
 * {@code static boolean} method in the test class or an external class. The
 * method may accept no arguments or a single
 * {@link org.junit.jupiter.api.extension.ExtensionContext ExtensionContext}
 * argument. Use {@code ClassName#methodName} for external classes.</li>
 * </ol>
 *
 * <p>If both {@link #named()} and {@link #method()} are specified,
 * <em>both</em> conditions must be satisfied for the test to be enabled.
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * // Enabled if the configuration parameter "feature.x.enabled" is "true"
 * @EnabledIfProperty(named = "feature.x.enabled")
 * @Test
 * void testFeatureX() { ... }
 *
 * // Enabled if the parameter matches a regex
 * @EnabledIfProperty(named = "test.env", matches = "(staging|production)")
 * @Test
 * void testInStagingOrProd() { ... }
 *
 * // Enabled if a static method returns true
 * @EnabledIfProperty(method = "isFeatureEnabled")
 * @Test
 * void testWithMethodCondition() { ... }
 *
 * static boolean isFeatureEnabled() {
 *     return System.getenv("FEATURE_FLAG") != null;
 * }
 *
 * // Enabled if both a property AND a method return true
 * @EnabledIfProperty(named = "integration.tests", method = "isDatabaseUp")
 * @Test
 * void integrationTest() { ... }
 * }</pre>
 *
 * @since 6.1
 * @see DisabledIfProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(PropertyCondition.class)
@API(status = EXPERIMENTAL, since = "6.1")
public @interface EnabledIfProperty {

	/**
	 * The name of the JUnit configuration parameter to check.
	 *
	 * <p>The parameter is resolved from JUnit's configuration (e.g.,
	 * {@code junit-platform.properties}, system properties, or build tool
	 * configuration).
	 *
	 * <p>Defaults to {@code ""} (not used). Must be non-empty if
	 * {@link #method()} is also empty.
	 */
	String named() default "";

	/**
	 * A regular expression that the configuration parameter's value must
	 * match for the test to be enabled.
	 *
	 * <p>Only used when {@link #named()} is specified. Defaults to
	 * {@code "true"}.
	 */
	String matches() default "true";

	/**
	 * The name of a {@code static boolean} method to invoke as a condition.
	 *
	 * <p>The method must return a {@code boolean}. It may accept no arguments
	 * or a single {@link org.junit.jupiter.api.extension.ExtensionContext
	 * ExtensionContext} argument.
	 *
	 * <p>If the method is in an external class, use the fully qualified form
	 * {@code com.example.Conditions#methodName}.
	 *
	 * <p>Defaults to {@code ""} (not used). Must be non-empty if
	 * {@link #named()} is also empty.
	 */
	String method() default "";

	/**
	 * Custom reason to provide if the test is disabled.
	 */
	String disabledReason() default "";

}
