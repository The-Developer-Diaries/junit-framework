/*
 * Copyright 2015-2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @JsonFileSource} is a {@linkplain Repeatable repeatable}
 * {@link ArgumentsSource} which is used to load JSON files from one or more
 * classpath {@link #resources} or {@link #files}.
 *
 * <p>The JSON file must contain a top-level JSON array. Each element in the
 * array will be provided as arguments to the annotated
 * {@link org.junit.jupiter.params.ParameterizedTest @ParameterizedTest} method.
 *
 * <h2>Supported JSON Structures</h2>
 *
 * <ul>
 * <li><strong>Array of objects</strong> &mdash; each object's values (in
 * declaration order) are passed as individual arguments:
 * <pre>{@code
 * [
 *   {"name": "Alice", "age": 30},
 *   {"name": "Bob", "age": 25}
 * ]
 * }</pre>
 * </li>
 * <li><strong>Array of arrays</strong> &mdash; each inner array's elements
 * are passed as individual arguments:
 * <pre>{@code
 * [
 *   ["Alice", 30],
 *   ["Bob", 25]
 * ]
 * }</pre>
 * </li>
 * <li><strong>Array of primitives</strong> &mdash; each value is passed as
 * a single argument:
 * <pre>{@code
 * [1, 2, 3, 4, 5]
 * }</pre>
 * </li>
 * </ul>
 *
 * <h2>Inheritance</h2>
 *
 * <p>This annotation is {@linkplain Inherited inherited} within class hierarchies.
 *
 * @since 6.1
 * @see org.junit.jupiter.params.provider.ArgumentsSource
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(JsonFileSources.class)
@API(status = EXPERIMENTAL, since = "6.1")
@ArgumentsSource(JsonFileArgumentsProvider.class)
@SuppressWarnings("exports")
public @interface JsonFileSource {

	/**
	 * The JSON classpath resources to use as the sources of arguments; must
	 * not be empty unless {@link #files} is non-empty.
	 */
	String[] resources() default {};

	/**
	 * The JSON files to use as the sources of arguments; must not be empty
	 * unless {@link #resources} is non-empty.
	 */
	String[] files() default {};

	/**
	 * The encoding to use when reading the JSON files; must be a valid charset.
	 *
	 * <p>Defaults to {@code "UTF-8"}.
	 *
	 * @see java.nio.charset.StandardCharsets
	 */
	String encoding() default "UTF-8";

}
