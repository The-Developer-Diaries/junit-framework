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
 * {@code @DisabledIfProperty} is used to signal that the annotated test class
 * or test method is <em>disabled</em> if a specified condition is met.
 *
 * <p>Supports the same {@link #named()}, {@link #matches()}, and
 * {@link #method()} attributes as {@link EnabledIfProperty}.
 *
 * @since 6.1
 * @see EnabledIfProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(PropertyCondition.class)
@API(status = EXPERIMENTAL, since = "6.1")
public @interface DisabledIfProperty {

	/**
	 * The name of the JUnit configuration parameter to check.
	 */
	String named() default "";

	/**
	 * A regular expression that the parameter's value must match.
	 * Defaults to {@code "true"}.
	 */
	String matches() default "true";

	/**
	 * The name of a {@code static boolean} method to invoke.
	 */
	String method() default "";

	/**
	 * Custom reason to provide if the test is disabled.
	 */
	String disabledReason() default "";

}
