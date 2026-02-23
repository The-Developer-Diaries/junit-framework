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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * {@code AssertContainsExactlyInAnyOrder} is a collection of utility methods that
 * support asserting that a collection or array contains exactly the expected
 * elements, regardless of order.
 *
 * @since 6.1
 */
class AssertContainsExactlyInAnyOrder {

	private AssertContainsExactlyInAnyOrder() {
		/* no-op */
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	static <T> void assertContainsExactlyInAnyOrder(Collection<T> actual, T... expected) {
		assertExactMatch(new ArrayList<>(actual), Arrays.asList(expected), null);
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	static <T> void assertContainsExactlyInAnyOrder(Collection<T> actual, @Nullable String message, T... expected) {
		assertExactMatch(new ArrayList<>(actual), Arrays.asList(expected), message);
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	static <T> void assertContainsExactlyInAnyOrder(Collection<T> actual, Supplier<@Nullable String> messageSupplier,
			T... expected) {
		assertExactMatch(new ArrayList<>(actual), Arrays.asList(expected), messageSupplier);
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	static void assertContainsExactlyInAnyOrder(Object[] actual, Object... expected) {
		assertExactMatch(new ArrayList<>(Arrays.asList(actual)), Arrays.asList(expected), null);
	}

	private static void assertExactMatch(List<?> remaining, List<?> expected, @Nullable Object messageOrSupplier) {
		List<Object> missing = new ArrayList<>();
		for (Object element : expected) {
			if (!remaining.remove(element)) {
				missing.add(element);
			}
		}
		if (!missing.isEmpty() || !remaining.isEmpty()) {
			StringBuilder reason = new StringBuilder();
			if (!missing.isEmpty()) {
				reason.append("Missing elements: ").append(missing);
			}
			if (!remaining.isEmpty()) {
				if (!reason.isEmpty()) {
					reason.append("; ");
				}
				reason.append("Unexpected elements: ").append(remaining);
			}
			assertionFailure() //
					.message(messageOrSupplier) //
					.reason(reason.toString()) //
					.expected(expected) //
					.trimStacktrace(Assertions.class) //
					.buildAndThrow();
		}
	}

}
