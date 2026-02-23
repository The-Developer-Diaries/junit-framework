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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Random;
import org.junit.jupiter.api.RandomDate;
import org.junit.jupiter.api.RandomString;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * {@link ParameterResolver} that resolves parameters annotated with
 * {@link Random @Random}, {@link RandomString @RandomString}, and
 * {@link RandomDate @RandomDate} with randomly generated test data.
 *
 * <p>This extension is registered as a default extension and activates
 * only for parameters bearing one of the supported annotations.
 *
 * @since 6.1
 * @see Random
 * @see RandomString
 * @see RandomDate
 */
class TestDataGeneratorExtension implements ParameterResolver {

	@Override
	public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
		return ExtensionContextScope.TEST_METHOD;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.isAnnotated(Random.class) || parameterContext.isAnnotated(RandomString.class)
				|| parameterContext.isAnnotated(RandomDate.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		if (parameterContext.isAnnotated(Random.class)) {
			return resolveRandom(parameterContext);
		}
		if (parameterContext.isAnnotated(RandomString.class)) {
			return resolveRandomString(parameterContext);
		}
		if (parameterContext.isAnnotated(RandomDate.class)) {
			return resolveRandomDate(parameterContext);
		}
		throw new ParameterResolutionException("No supported annotation found on parameter");
	}

	// -------------------------------------------------------------------------
	// --- @Random ---
	// -------------------------------------------------------------------------

	private Object resolveRandom(ParameterContext parameterContext) {
		Random ann = parameterContext.findAnnotation(Random.class).orElseThrow();
		Class<?> type = parameterContext.getParameter().getType();
		ThreadLocalRandom rng = ThreadLocalRandom.current();

		if (type == int.class || type == Integer.class) {
			return rng.nextInt((int) ann.min(), (int) ann.max() + 1);
		}
		if (type == long.class || type == Long.class) {
			return rng.nextLong(ann.min(), ann.max() + 1);
		}
		if (type == double.class || type == Double.class) {
			return ann.minDouble() + (ann.maxDouble() - ann.minDouble()) * rng.nextDouble();
		}
		if (type == float.class || type == Float.class) {
			return rng.nextFloat();
		}
		if (type == boolean.class || type == Boolean.class) {
			return rng.nextBoolean();
		}
		if (type == short.class || type == Short.class) {
			return (short) rng.nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1);
		}
		if (type == byte.class || type == Byte.class) {
			byte[] bytes = new byte[1];
			rng.nextBytes(bytes);
			return bytes[0];
		}

		throw new ParameterResolutionException("@Random does not support type: " + type.getName()
				+ ". Supported: int, long, double, float, boolean, short, byte (and wrappers).");
	}

	// -------------------------------------------------------------------------
	// --- @RandomString ---
	// -------------------------------------------------------------------------

	private Object resolveRandomString(ParameterContext parameterContext) {
		RandomString ann = parameterContext.findAnnotation(RandomString.class).orElseThrow();
		Class<?> type = parameterContext.getParameter().getType();

		if (type != String.class) {
			throw new ParameterResolutionException(
				"@RandomString can only be applied to String parameters, but was: " + type.getName());
		}

		ThreadLocalRandom rng = ThreadLocalRandom.current();
		String charset = ann.charset();

		int length;
		if (ann.length() >= 0) {
			length = ann.length();
		}
		else {
			length = rng.nextInt(ann.minLength(), ann.maxLength() + 1);
		}

		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			sb.append(charset.charAt(rng.nextInt(charset.length())));
		}
		return sb.toString();
	}

	// -------------------------------------------------------------------------
	// --- @RandomDate ---
	// -------------------------------------------------------------------------

	private Object resolveRandomDate(ParameterContext parameterContext) {
		RandomDate ann = parameterContext.findAnnotation(RandomDate.class).orElseThrow();
		Class<?> type = parameterContext.getParameter().getType();
		ThreadLocalRandom rng = ThreadLocalRandom.current();

		LocalDate fromDate = LocalDate.parse(ann.from());
		LocalDate toDate = LocalDate.parse(ann.to());
		long fromEpochDay = fromDate.toEpochDay();
		long toEpochDay = toDate.toEpochDay();
		long randomDay = rng.nextLong(fromEpochDay, toEpochDay + 1);
		LocalDate randomDate = LocalDate.ofEpochDay(randomDay);

		if (type == LocalDate.class) {
			return randomDate;
		}
		if (type == LocalDateTime.class) {
			int randomSecond = rng.nextInt(86400); // seconds in a day
			return LocalDateTime.of(randomDate, LocalTime.ofSecondOfDay(randomSecond));
		}
		if (type == Instant.class) {
			int randomSecond = rng.nextInt(86400);
			return LocalDateTime.of(randomDate, LocalTime.ofSecondOfDay(randomSecond)).toInstant(ZoneOffset.UTC);
		}

		throw new ParameterResolutionException(
			"@RandomDate does not support type: " + type.getName() + ". Supported: LocalDate, LocalDateTime, Instant.");
	}

}
