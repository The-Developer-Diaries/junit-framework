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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * {@code AssertSorted} is a collection of utility methods that support asserting
 * that a list or array is sorted according to a comparator or natural ordering.
 *
 * @since 6.1
 */
class AssertSorted {

	private AssertSorted() {
		/* no-op */
	}

	// --- List with Comparator ---
	static <T> void assertSorted(List<T> list, Comparator<? super T> c) {
		assertListSorted(list, c, (Object) null);
	}

	static <T> void assertSorted(List<T> list, Comparator<? super T> c, @Nullable String msg) {
		assertListSorted(list, c, (Object) msg);
	}

	static <T> void assertSorted(List<T> list, Comparator<? super T> c, Supplier<@Nullable String> s) {
		assertListSorted(list, c, (Object) s);
	}

	// --- List natural ordering ---
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T extends Comparable<T>> void assertSorted(List<T> list) {
		assertListSorted(list, (Comparator) Comparator.naturalOrder(), (Object) null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T extends Comparable<T>> void assertSorted(List<T> list, @Nullable String msg) {
		assertListSorted(list, (Comparator) Comparator.naturalOrder(), (Object) msg);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T extends Comparable<T>> void assertSorted(List<T> list, Supplier<@Nullable String> s) {
		assertListSorted(list, (Comparator) Comparator.naturalOrder(), (Object) s);
	}

	// --- Array with Comparator ---
	static <T> void assertSorted(T[] arr, Comparator<? super T> c) {
		assertListSorted(Arrays.asList(arr), c, (Object) null);
	}

	static <T> void assertSorted(T[] arr, Comparator<? super T> c, @Nullable String msg) {
		assertListSorted(Arrays.asList(arr), c, (Object) msg);
	}

	static <T> void assertSorted(T[] arr, Comparator<? super T> c, Supplier<@Nullable String> s) {
		assertListSorted(Arrays.asList(arr), c, (Object) s);
	}

	// --- Array natural ordering ---
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T extends Comparable<T>> void assertSorted(T[] arr) {
		assertListSorted(Arrays.asList(arr), (Comparator) Comparator.naturalOrder(), (Object) null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T extends Comparable<T>> void assertSorted(T[] arr, @Nullable String msg) {
		assertListSorted(Arrays.asList(arr), (Comparator) Comparator.naturalOrder(), (Object) msg);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T extends Comparable<T>> void assertSorted(T[] arr, Supplier<@Nullable String> s) {
		assertListSorted(Arrays.asList(arr), (Comparator) Comparator.naturalOrder(), (Object) s);
	}

	// --- Shared ---
	private static <T> void assertListSorted(List<T> list, Comparator<? super T> comparator,
			@Nullable Object messageOrSupplier) {
		if (list.size() <= 1) {
			return;
		}
		Iterator<T> it = list.iterator();
		T prev = it.next();
		int idx = 0;
		while (it.hasNext()) {
			T cur = it.next();
			idx++;
			if (comparator.compare(prev, cur) > 0) {
				assertionFailure() //
						.message(messageOrSupplier) //
						.reason("Not sorted: element at index " + (idx - 1) + " (" + prev
								+ ") is greater than element at index " + idx + " (" + cur + ")") //
						.actual(list) //
						.trimStacktrace(Assertions.class) //
						.buildAndThrow();
			}
			prev = cur;
		}
	}

}
