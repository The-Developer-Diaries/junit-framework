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
 * {@code @YamlFileSource} is a {@linkplain Repeatable repeatable}
 * {@link ArgumentsSource} which is used to load YAML files from one or more
 * classpath {@link #resources} or {@link #files}.
 *
 * <p>The YAML file must contain a top-level sequence (list). Each element in
 * the sequence will be provided as arguments to the annotated
 * {@link org.junit.jupiter.params.ParameterizedTest @ParameterizedTest} method.
 *
 * <p>This parser supports a practical subset of YAML suitable for test data,
 * without requiring any external YAML libraries. The following structures are
 * supported:
 *
 * <h2>Supported YAML Structures</h2>
 *
 * <ul>
 * <li><strong>Sequence of mappings</strong> &mdash; each mapping's values (in
 * declaration order) are passed as individual arguments:
 * <pre>{@code
 * - name: Alice
 *   age: 30
 * - name: Bob
 *   age: 25
 * }</pre>
 * </li>
 * <li><strong>Sequence of sequences</strong> (flow or block style) &mdash;
 * each inner sequence's elements are passed as individual arguments:
 * <pre>{@code
 * - [Alice, 30]
 * - [Bob, 25]
 * }</pre>
 * </li>
 * <li><strong>Sequence of scalars</strong> &mdash; each value is passed as
 * a single argument:
 * <pre>{@code
 * - 1
 * - 2
 * - 3
 * }</pre>
 * </li>
 * </ul>
 *
 * <h2>Supported Scalar Types</h2>
 *
 * <p>Scalars are automatically converted to the following types:
 * <ul>
 * <li>{@code null} or {@code ~} &rarr; {@code null}</li>
 * <li>{@code true} / {@code false} &rarr; {@code Boolean}</li>
 * <li>Integer values &rarr; {@code Long}</li>
 * <li>Floating-point values &rarr; {@code Double}</li>
 * <li>Quoted or unquoted text &rarr; {@code String}</li>
 * </ul>
 *
 * <h2>Limitations</h2>
 *
 * <p>This built-in parser does <em>not</em> support the full YAML
 * specification. Unsupported features include: anchors and aliases, multi-line
 * block scalars ({@code |} and {@code >}), tags, multi-document streams, and
 * complex mapping keys. For these use cases, consider using an external YAML
 * library with a custom {@link ArgumentsProvider}.
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
@Repeatable(YamlFileSources.class)
@API(status = EXPERIMENTAL, since = "6.1")
@ArgumentsSource(YamlFileArgumentsProvider.class)
@SuppressWarnings("exports")
public @interface YamlFileSource {

	/**
	 * The YAML classpath resources to use as the sources of arguments; must
	 * not be empty unless {@link #files} is non-empty.
	 */
	String[] resources() default {};

	/**
	 * The YAML files to use as the sources of arguments; must not be empty
	 * unless {@link #resources} is non-empty.
	 */
	String[] files() default {};

	/**
	 * The encoding to use when reading the YAML files; must be a valid charset.
	 *
	 * <p>Defaults to {@code "UTF-8"}.
	 *
	 * @see java.nio.charset.StandardCharsets
	 */
	String encoding() default "UTF-8";

}
