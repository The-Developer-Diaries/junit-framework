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
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * {@code AssertContains} is a collection of utility methods that support asserting
 * that a string contains a substring, that a collection or array contains an
 * element, and that a collection or array contains all elements of another
 * collection or array (subset/superset checks).
 *
 * @since 6.1
 */
class AssertContains {

	private AssertContains() {
		/* no-op */
	}

	// --- String contains ---

	static void assertContains(String text, String substring) {
		assertStringContains(text, substring, (Object) null);
	}

	static void assertContains(String text, String substring, @Nullable String message) {
		assertStringContains(text, substring, (Object) message);
	}

	static void assertContains(String text, String substring, Supplier<@Nullable String> messageSupplier) {
		assertStringContains(text, substring, (Object) messageSupplier);
	}

	private static void assertStringContains(String text, String substring, @Nullable Object messageOrSupplier) {
		if (!text.contains(substring)) {
			assertionFailure() //
					.message(messageOrSupplier) //
					.reason("String does not contain expected substring") //
					.expected("a string containing \"" + substring + "\"") //
					.actual(text) //
					.trimStacktrace(Assertions.class) //
					.buildAndThrow();
		}
	}

	// --- Collection contains element ---

	static void assertContains(Collection<?> collection, @Nullable Object element) {
		assertCollectionContains(collection, element, (Object) null);
	}

	static void assertContains(Collection<?> collection, @Nullable Object element, @Nullable String message) {
		assertCollectionContains(collection, element, (Object) message);
	}

	static void assertContains(Collection<?> collection, @Nullable Object element,
			Supplier<@Nullable String> messageSupplier) {
		assertCollectionContains(collection, element, (Object) messageSupplier);
	}

	private static void assertCollectionContains(Collection<?> collection, @Nullable Object element,
			@Nullable Object messageOrSupplier) {
		if (!collection.contains(element)) {
			assertionFailure() //
					.message(messageOrSupplier) //
					.reason("Collection does not contain expected element") //
					.expected(element) //
					.actual(collection) //
					.trimStacktrace(Assertions.class) //
					.buildAndThrow();
		}
	}

	// --- Array contains element ---

	static void assertContains(Object[] array, @Nullable Object element) {
		assertArrayContainsElement(array, element, (Object) null);
	}

	static void assertContains(Object[] array, @Nullable Object element, @Nullable String message) {
		assertArrayContainsElement(array, element, (Object) message);
	}

	static void assertContains(Object[] array, @Nullable Object element, Supplier<@Nullable String> messageSupplier) {
		assertArrayContainsElement(array, element, (Object) messageSupplier);
	}

	private static void assertArrayContainsElement(Object[] array, @Nullable Object element,
			@Nullable Object messageOrSupplier) {
		for (Object item : array) {
			if (java.util.Objects.equals(item, element)) {
				return;
			}
		}
		assertionFailure() //
				.message(messageOrSupplier) //
				.reason("Array does not contain expected element") //
				.expected(element) //
				.actual(Arrays.asList(array)) //
				.trimStacktrace(Assertions.class) //
				.buildAndThrow();
	}

	// --- containsAll: Collection ⊇ Collection ---

	static void assertContainsAll(Collection<?> collection, Collection<?> subset) {
		assertContainsAllCore(collection::contains, subset, collection, (Object) null);
	}

	static void assertContainsAll(Collection<?> collection, Collection<?> subset, @Nullable String message) {
		assertContainsAllCore(collection::contains, subset, collection, (Object) message);
	}

	static void assertContainsAll(Collection<?> collection, Collection<?> subset,
			Supplier<@Nullable String> messageSupplier) {
		assertContainsAllCore(collection::contains, subset, collection, (Object) messageSupplier);
	}

	// --- containsAll: Collection ⊇ Array ---

	static void assertContainsAll(Collection<?> collection, Object[] subset) {
		assertContainsAllCore(collection::contains, Arrays.asList(subset), collection, (Object) null);
	}

	static void assertContainsAll(Collection<?> collection, Object[] subset, @Nullable String message) {
		assertContainsAllCore(collection::contains, Arrays.asList(subset), collection, (Object) message);
	}

	static void assertContainsAll(Collection<?> collection, Object[] subset,
			Supplier<@Nullable String> messageSupplier) {
		assertContainsAllCore(collection::contains, Arrays.asList(subset), collection, (Object) messageSupplier);
	}

	// --- containsAll: Array ⊇ Collection ---

	static void assertContainsAll(Object[] array, Collection<?> subset) {
		List<?> asList = Arrays.asList(array);
		assertContainsAllCore(asList::contains, subset, asList, (Object) null);
	}

	static void assertContainsAll(Object[] array, Collection<?> subset, @Nullable String message) {
		List<?> asList = Arrays.asList(array);
		assertContainsAllCore(asList::contains, subset, asList, (Object) message);
	}

	static void assertContainsAll(Object[] array, Collection<?> subset, Supplier<@Nullable String> messageSupplier) {
		List<?> asList = Arrays.asList(array);
		assertContainsAllCore(asList::contains, subset, asList, (Object) messageSupplier);
	}

	// --- containsAll: Array ⊇ Array ---

	static void assertContainsAll(Object[] array, Object[] subset) {
		List<?> asList = Arrays.asList(array);
		assertContainsAllCore(asList::contains, Arrays.asList(subset), asList, (Object) null);
	}

	static void assertContainsAll(Object[] array, Object[] subset, @Nullable String message) {
		List<?> asList = Arrays.asList(array);
		assertContainsAllCore(asList::contains, Arrays.asList(subset), asList, (Object) message);
	}

	static void assertContainsAll(Object[] array, Object[] subset, Supplier<@Nullable String> messageSupplier) {
		List<?> asList = Arrays.asList(array);
		assertContainsAllCore(asList::contains, Arrays.asList(subset), asList, (Object) messageSupplier);
	}

	// --- Shared core ---

	private static void assertContainsAllCore(Predicate<Object> containsCheck, Iterable<?> subset,
			Object actualContainer, @Nullable Object messageOrSupplier) {
		List<Object> missing = new ArrayList<>();
		for (Object element : subset) {
			if (!containsCheck.test(element)) {
				missing.add(element);
			}
		}
		if (!missing.isEmpty()) {
			assertionFailure() //
					.message(messageOrSupplier) //
					.reason("Does not contain all expected elements. Missing: " + missing) //
					.expected(subset) //
					.actual(actualContainer) //
					.trimStacktrace(Assertions.class) //
					.buildAndThrow();
		}
	}

}
