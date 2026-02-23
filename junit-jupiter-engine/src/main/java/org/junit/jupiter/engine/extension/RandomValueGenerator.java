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

import java.util.Random;

import org.junit.jupiter.api.ForAll;
import org.junit.jupiter.api.extension.ParameterResolutionException;

/**
 * Generates random values for {@link ForAll @ForAll}-annotated parameters
 * in property-based tests.
 *
 * @since 6.1
 */
final class RandomValueGenerator {

	private static final String PRINTABLE_ASCII = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 !@#$%^&*()-_=+[]{}|;:',.<>?/";

	private final Random random;

	RandomValueGenerator(Random random) {
		this.random = random;
	}

	/**
	 * Generate a random value for the given parameter type and constraints.
	 *
	 * @param type the parameter type
	 * @param forAll the {@code @ForAll} annotation with constraints
	 * @return a randomly generated value
	 * @throws ParameterResolutionException if the type is not supported
	 */
	Object generate(Class<?> type, ForAll forAll) {
		// --- Primitives and wrappers ---
		if (type == int.class || type == Integer.class) {
			return randomInt(forAll.minInt(), forAll.maxInt());
		}
		if (type == long.class || type == Long.class) {
			return randomLong(forAll.minLong(), forAll.maxLong());
		}
		if (type == double.class || type == Double.class) {
			return randomDouble(forAll.minDouble(), forAll.maxDouble());
		}
		if (type == float.class || type == Float.class) {
			return random.nextFloat();
		}
		if (type == boolean.class || type == Boolean.class) {
			return random.nextBoolean();
		}
		if (type == short.class || type == Short.class) {
			return (short) random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1);
		}
		if (type == byte.class || type == Byte.class) {
			byte[] bytes = new byte[1];
			random.nextBytes(bytes);
			return bytes[0];
		}
		if (type == char.class || type == Character.class) {
			return PRINTABLE_ASCII.charAt(random.nextInt(PRINTABLE_ASCII.length()));
		}
		// --- String ---
		if (type == String.class) {
			return randomString(forAll.minLength(), forAll.maxLength());
		}
		throw new ParameterResolutionException("@ForAll does not support parameter type: " + type.getName()
				+ ". Supported types: int, long, double, float, short, byte, char, boolean, String"
				+ " (and their wrapper types).");
	}

	private int randomInt(int min, int max) {
		if (min == max) {
			return min;
		}
		// Use long arithmetic to avoid overflow on (max - min + 1)
		return (int) (min + random.nextLong(0, (long) max - min + 1));
	}

	private long randomLong(long min, long max) {
		if (min == max) {
			return min;
		}
		if (min == Long.MIN_VALUE && max == Long.MAX_VALUE) {
			return random.nextLong();
		}
		// For bounded ranges, handle potential overflow
		if (max - min > 0 || min == Long.MIN_VALUE) {
			return random.nextLong(min, max);
		}
		// Fallback for ranges where (max - min) overflows
		return random.nextLong();
	}

	private double randomDouble(double min, double max) {
		return min + (max - min) * random.nextDouble();
	}

	private String randomString(int minLength, int maxLength) {
		int length = (minLength == maxLength) ? minLength : random.nextInt(minLength, maxLength + 1);
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(PRINTABLE_ASCII.charAt(random.nextInt(PRINTABLE_ASCII.length())));
		}
		return sb.toString();
	}

}
