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
import org.junit.platform.commons.annotation.Testable;

/**
 * {@code @SnapshotTest} is a composed annotation that combines {@link Test @Test}
 * semantics with snapshot testing support.
 *
 * <p>Methods annotated with {@code @SnapshotTest} can declare a parameter of
 * type {@link SnapshotAssert} which will be automatically resolved and injected
 * by the framework. The injected {@code SnapshotAssert} provides methods to
 * compare test output against stored "golden" snapshot files.
 *
 * <p>{@code @SnapshotTest} methods must not be {@code private} or {@code static}
 * and must return {@code void}.
 *
 * <p>{@code @SnapshotTest} may also be used as a meta-annotation in order to
 * create a custom <em>composed annotation</em> that inherits the semantics
 * of {@code @SnapshotTest}.
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * @SnapshotTest
 * void testApiResponse(SnapshotAssert snapshot) {
 *     var response = api.getUser(42);
 *     snapshot.assertMatches(response);
 * }
 *
 * @SnapshotTest
 * void testMultipleOutputs(SnapshotAssert snapshot) {
 *     snapshot.assertMatches("header", generateHeader());
 *     snapshot.assertMatches("body", generateBody());
 * }
 * }</pre>
 *
 * <h2>Snapshot Lifecycle</h2>
 *
 * <ul>
 * <li><strong>First run</strong>: No snapshot file exists. The current output
 * is saved as the golden snapshot and the test passes.</li>
 * <li><strong>Subsequent runs</strong>: The current output is compared against
 * the stored snapshot. The test passes if they match, and fails with a clear
 * diff message if they differ.</li>
 * <li><strong>Update mode</strong>: When
 * {@code -Djunit.jupiter.snapshot.update=true} is set, all snapshot files are
 * overwritten with the current output and the tests pass.</li>
 * </ul>
 *
 * <h2>Inheritance</h2>
 *
 * <p>{@code @SnapshotTest} methods are inherited from superclasses as long as
 * they are not <em>overridden</em> according to the visibility rules of the Java
 * language. Similarly, {@code @SnapshotTest} methods declared as <em>interface
 * default methods</em> are inherited as long as they are not overridden.
 *
 * @since 6.1
 * @see SnapshotAssert
 * @see Test
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "6.1")
@Testable
@Test
public @interface SnapshotTest {
}
