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

import org.apiguardian.api.API;

/**
 * {@code SnapshotAssert} provides snapshot testing capabilities for JUnit
 * Jupiter tests, similar to Jest's snapshot testing.
 *
 * <p>Snapshot testing works by serializing complex objects and comparing them
 * against stored "golden" files. On the first run, the snapshot file is created
 * automatically. On subsequent runs, the output is compared against the stored
 * snapshot. If they differ, the test fails with a clear diff showing the
 * expected and actual values.
 *
 * <p>Parameters of type {@code SnapshotAssert} can be injected into methods
 * annotated with {@link SnapshotTest @SnapshotTest}, {@link Test @Test},
 * {@link RepeatedTest @RepeatedTest},
 * {@link org.junit.jupiter.params.ParameterizedTest @ParameterizedTest},
 * {@link TestFactory @TestFactory}, etc.
 *
 * <h2>Snapshot Storage</h2>
 *
 * <p>Snapshots are stored in a {@code __snapshots__} directory relative to
 * the project's working directory. The directory can be customized via the
 * {@value #SNAPSHOT_DIR_PROPERTY_NAME} configuration parameter.
 *
 * <p>Snapshot files are named using the pattern
 * {@code {TestClassName}/{testMethodName}.snap}. When using named snapshots
 * via {@link #assertMatches(String, Object)}, the file name becomes
 * {@code {TestClassName}/{testMethodName}.{snapshotName}.snap}.
 *
 * <h2>Updating Snapshots</h2>
 *
 * <p>To update all snapshots (for example, after intentional changes to
 * output), set the {@value #UPDATE_SNAPSHOTS_PROPERTY_NAME} configuration
 * parameter or system property to {@code true}. When update mode is active,
 * snapshot files are overwritten with the current output, and the tests pass.
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
 * void testMultipleSnapshots(SnapshotAssert snapshot) {
 *     snapshot.assertMatches("header", generateHeader());
 *     snapshot.assertMatches("body", generateBody());
 * }
 * }</pre>
 *
 * @since 6.1
 * @see SnapshotTest
 * @see Test
 * @see TestInfo
 */
@API(status = EXPERIMENTAL, since = "6.1")
public interface SnapshotAssert {

	/**
	 * Property name used to configure the snapshot storage directory: {@value}
	 *
	 * <p>Defaults to {@code __snapshots__} relative to the working directory.
	 */
	String SNAPSHOT_DIR_PROPERTY_NAME = "junit.jupiter.snapshot.dir";

	/**
	 * Property name used to enable snapshot update mode: {@value}
	 *
	 * <p>When set to {@code true}, all snapshot files will be overwritten with
	 * the current actual values, and assertions will pass regardless of
	 * differences. This is useful after intentional changes to test output.
	 *
	 * <p>Defaults to {@code false}.
	 */
	String UPDATE_SNAPSHOTS_PROPERTY_NAME = "junit.jupiter.snapshot.update";

	/**
	 * Assert that the string representation of the supplied {@code actual}
	 * value matches the stored snapshot.
	 *
	 * <p>If no snapshot file exists yet, it will be created with the current
	 * value, and the test will pass.
	 *
	 * <p>The actual value is converted to a string using {@link Object#toString()}.
	 *
	 * @param actual the actual value to compare against the snapshot; must not
	 * be {@code null}
	 * @throws org.opentest4j.AssertionFailedError if the actual value does not
	 * match the stored snapshot
	 */
	void assertMatches(Object actual);

	/**
	 * Assert that the supplied string value matches the stored snapshot.
	 *
	 * <p>If no snapshot file exists yet, it will be created with the current
	 * value, and the test will pass.
	 *
	 * @param actual the actual string to compare against the snapshot; must
	 * not be {@code null}
	 * @throws org.opentest4j.AssertionFailedError if the actual value does not
	 * match the stored snapshot
	 */
	void assertMatches(String actual);

	/**
	 * Assert that the string representation of the supplied {@code actual}
	 * value matches the named snapshot.
	 *
	 * <p>Named snapshots allow multiple snapshot assertions within a single
	 * test method. Each name produces a separate snapshot file.
	 *
	 * <p>If no snapshot file exists yet for the given name, it will be created
	 * with the current value, and the test will pass.
	 *
	 * <p>The actual value is converted to a string using {@link Object#toString()}.
	 *
	 * @param name the snapshot name; must not be {@code null} or blank
	 * @param actual the actual value to compare against the snapshot; must not
	 * be {@code null}
	 * @throws org.opentest4j.AssertionFailedError if the actual value does not
	 * match the stored snapshot
	 */
	void assertMatches(String name, Object actual);

}
