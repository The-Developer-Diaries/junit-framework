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

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * {@code AssertHasSize} is a collection of utility methods that support asserting
 * the size of collections, maps, arrays, and strings.
 *
 * @since 6.1
 */
class AssertHasSize {

	private AssertHasSize() {
		/* no-op */
	}

	static void assertHasSize(Collection<?> c, int expected) {
		assertSizeEquals("Collection", c.size(), expected, (Object) null);
	}

	static void assertHasSize(Collection<?> c, int expected, @Nullable String msg) {
		assertSizeEquals("Collection", c.size(), expected, (Object) msg);
	}

	static void assertHasSize(Collection<?> c, int expected, Supplier<@Nullable String> s) {
		assertSizeEquals("Collection", c.size(), expected, (Object) s);
	}

	static void assertHasSize(Map<?, ?> m, int expected) {
		assertSizeEquals("Map", m.size(), expected, (Object) null);
	}

	static void assertHasSize(Map<?, ?> m, int expected, @Nullable String msg) {
		assertSizeEquals("Map", m.size(), expected, (Object) msg);
	}

	static void assertHasSize(Map<?, ?> m, int expected, Supplier<@Nullable String> s) {
		assertSizeEquals("Map", m.size(), expected, (Object) s);
	}

	static void assertHasSize(String str, int expected) {
		assertSizeEquals("String", str.length(), expected, (Object) null);
	}

	static void assertHasSize(String str, int expected, @Nullable String msg) {
		assertSizeEquals("String", str.length(), expected, (Object) msg);
	}

	static void assertHasSize(String str, int expected, Supplier<@Nullable String> s) {
		assertSizeEquals("String", str.length(), expected, (Object) s);
	}

	static void assertHasSize(Object[] arr, int expected) {
		assertSizeEquals("Array", arr.length, expected, (Object) null);
	}

	static void assertHasSize(Object[] arr, int expected, @Nullable String msg) {
		assertSizeEquals("Array", arr.length, expected, (Object) msg);
	}

	static void assertHasSize(Object[] arr, int expected, Supplier<@Nullable String> s) {
		assertSizeEquals("Array", arr.length, expected, (Object) s);
	}

	private static void assertSizeEquals(String kind, int actualSize, int expectedSize,
			@Nullable Object messageOrSupplier) {
		if (actualSize != expectedSize) {
			assertionFailure() //
					.message(messageOrSupplier) //
					.reason(kind + " size mismatch") //
					.expected(expectedSize) //
					.actual(actualSize) //
					.trimStacktrace(Assertions.class) //
					.buildAndThrow();
		}
	}

}
