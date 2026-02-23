/*
 * Copyright 2015-2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.SnapshotAssert;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.opentest4j.AssertionFailedError;

/**
 * {@link ParameterResolver} that injects a {@link SnapshotAssert} for snapshot testing.
 *
 * <p>This extension is registered as a default extension and automatically
 * resolves parameters of type {@link SnapshotAssert}. The resolved instance
 * manages reading, writing, and comparing snapshot files.
 *
 * @since 6.1
 * @see SnapshotAssert
 */
class SnapshotExtension implements ParameterResolver {

	private static final String DEFAULT_SNAPSHOT_DIR = "__snapshots__";
	private static final String SNAPSHOT_FILE_EXTENSION = ".snap";

	@Override
	public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
		return ExtensionContextScope.TEST_METHOD;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return (parameterContext.getParameter().getType() == SnapshotAssert.class);
	}

	@Override
	public SnapshotAssert resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return new DefaultSnapshotAssert(extensionContext);
	}

	/**
	 * Default implementation of {@link SnapshotAssert} that manages snapshot
	 * files on the filesystem.
	 */
	private static class DefaultSnapshotAssert implements SnapshotAssert {

		private final ExtensionContext context;
		private final Path snapshotDir;
		private final boolean updateMode;

		DefaultSnapshotAssert(ExtensionContext context) {
			this.context = context;
			this.snapshotDir = resolveSnapshotDir(context);
			this.updateMode = resolveUpdateMode(context);
		}

		@Override
		public void assertMatches(Object actual) {
			Preconditions.notNull(actual, "actual value must not be null");
			performAssert(actual.toString(), null);
		}

		@Override
		public void assertMatches(String actual) {
			Preconditions.notNull(actual, "actual string must not be null");
			performAssert(actual, null);
		}

		@Override
		public void assertMatches(String name, Object actual) {
			Preconditions.notBlank(name, "snapshot name must not be null or blank");
			Preconditions.notNull(actual, "actual value must not be null");
			performAssert(actual.toString(), name);
		}

		private void performAssert(String actual, @Nullable String name) {
			Path snapshotFile = resolveSnapshotFile(name);

			if (updateMode || !Files.exists(snapshotFile)) {
				// Create or update the snapshot file
				writeSnapshot(snapshotFile, actual);
				return;
			}

			// Compare against existing snapshot
			String expected = readSnapshot(snapshotFile);
			if (!expected.equals(actual)) {
				throw new AssertionFailedError(buildDiffMessage(snapshotFile, expected, actual), expected, actual);
			}
		}

		private Path resolveSnapshotFile(@Nullable String name) {
			String testClassName = context.getRequiredTestClass().getSimpleName();
			String testMethodName = context.getRequiredTestMethod().getName();

			String fileName;
			if (name != null) {
				fileName = testMethodName + "." + name + SNAPSHOT_FILE_EXTENSION;
			}
			else {
				fileName = testMethodName + SNAPSHOT_FILE_EXTENSION;
			}

			return snapshotDir.resolve(testClassName).resolve(fileName);
		}

		private static void writeSnapshot(Path snapshotFile, String content) {
			try {
				Files.createDirectories(snapshotFile.getParent());
				Files.writeString(snapshotFile, content, StandardCharsets.UTF_8);
			}
			catch (IOException e) {
				throw new JUnitException("Failed to write snapshot file [" + snapshotFile + "]", e);
			}
		}

		private static String readSnapshot(Path snapshotFile) {
			try {
				return Files.readString(snapshotFile, StandardCharsets.UTF_8);
			}
			catch (IOException e) {
				throw new JUnitException("Failed to read snapshot file [" + snapshotFile + "]", e);
			}
		}

		private static String buildDiffMessage(Path snapshotFile, String expected, String actual) {
			StringBuilder sb = new StringBuilder();
			sb.append("Snapshot mismatch [").append(snapshotFile.getFileName()).append("]\n\n");

			String[] expectedLines = expected.split("\n", -1);
			String[] actualLines = actual.split("\n", -1);
			int maxLines = Math.max(expectedLines.length, actualLines.length);

			boolean hasDiff = false;
			for (int i = 0; i < maxLines; i++) {
				String exp = i < expectedLines.length ? expectedLines[i] : "";
				String act = i < actualLines.length ? actualLines[i] : "";
				if (!exp.equals(act)) {
					if (!hasDiff) {
						sb.append("First difference at line ").append(i + 1).append(":\n");
						hasDiff = true;
					}
					sb.append("  expected: ").append(exp).append("\n");
					sb.append("    actual: ").append(act).append("\n");
					break;
				}
			}

			sb.append("\nTo update this snapshot, run with -D").append(UPDATE_SNAPSHOTS_PROPERTY_NAME).append("=true");

			return sb.toString();
		}

		private static Path resolveSnapshotDir(ExtensionContext context) {
			String configuredDir = context.getConfigurationParameter(SNAPSHOT_DIR_PROPERTY_NAME).orElse(null);
			if (configuredDir != null && !configuredDir.isBlank()) {
				return Path.of(configuredDir);
			}
			// Also check system property as a fallback
			String systemProp = System.getProperty(SNAPSHOT_DIR_PROPERTY_NAME);
			if (systemProp != null && !systemProp.isBlank()) {
				return Path.of(systemProp);
			}
			return Path.of(DEFAULT_SNAPSHOT_DIR);
		}

		private static boolean resolveUpdateMode(ExtensionContext context) {
			String configuredUpdate = context.getConfigurationParameter(UPDATE_SNAPSHOTS_PROPERTY_NAME).orElse(null);
			if (configuredUpdate != null) {
				return Boolean.parseBoolean(configuredUpdate);
			}
			// Also check system property as a fallback
			return Boolean.getBoolean(UPDATE_SNAPSHOTS_PROPERTY_NAME);
		}

	}

}
