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
 * {@code @DisabledIfPort} is used to signal that the annotated test class or
 * test method is <em>disabled</em> if a TCP connection can be established to
 * the specified {@link #port()} on the specified {@link #host()}.
 *
 * @since 6.1
 * @see EnabledIfPort
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(PortAvailableCondition.class)
@API(status = EXPERIMENTAL, since = "6.1")
public @interface DisabledIfPort {

	/**
	 * The host to connect to.
	 *
	 * <p>Defaults to {@code "localhost"}.
	 */
	String host() default "localhost";

	/**
	 * The TCP port to check.
	 */
	int port();

	/**
	 * Connection timeout in milliseconds.
	 *
	 * <p>Defaults to {@code 1000} (1 second).
	 */
	int timeoutMs() default 1000;

	/**
	 * Custom reason to provide if the test is disabled.
	 */
	String disabledReason() default "";

}
