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
 * {@code @EnabledIfClass} is used to signal that the annotated test class or
 * test method is only <em>enabled</em> if the specified class is present on
 * the classpath.
 *
 * <p>This is useful for tests that require an optional dependency to be
 * available (e.g., a specific database driver, a framework class, or a
 * testing library).
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * @EnabledIfClass("com.mysql.cj.jdbc.Driver")
 * @Test
 * void testMySqlConnection() { ... }
 *
 * @EnabledIfClass("org.testcontainers.containers.PostgreSQLContainer")
 * @Test
 * void testWithTestcontainers() { ... }
 * }</pre>
 *
 * @since 6.1
 * @see DisabledIfClass
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(ClassAvailableCondition.class)
@API(status = EXPERIMENTAL, since = "6.1")
public @interface EnabledIfClass {

	/**
	 * The fully qualified name of the class that must be present on the
	 * classpath for the test to be enabled.
	 */
	String value();

	/**
	 * Custom reason to provide if the test is disabled.
	 */
	String disabledReason() default "";

}
