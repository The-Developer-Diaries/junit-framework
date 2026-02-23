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

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Benchmark;
import org.junit.jupiter.api.ExpectedPerformance;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.util.Preconditions;
import org.opentest4j.AssertionFailedError;

/**
 * {@link InvocationInterceptor} that implements the {@link Benchmark @Benchmark}
 * annotation by intercepting test method invocations to perform warm-up
 * iterations, timed measurement iterations, statistical analysis, and
 * optional performance constraint validation.
 *
 * <p>This extension is registered as a default extension and only activates
 * when the test method is annotated with {@code @Benchmark}.
 *
 * @since 6.1
 * @see Benchmark
 * @see ExpectedPerformance
 */
class BenchmarkExtension implements InvocationInterceptor {

	@Override
	public void interceptTestMethod(Invocation<@Nullable Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

		Method testMethod = extensionContext.getRequiredTestMethod();

		var benchmarkOpt = findAnnotation(testMethod, Benchmark.class);
		if (benchmarkOpt.isEmpty()) {
			// Not a benchmark test — proceed normally
			invocation.proceed();
			return;
		}

		Benchmark benchmark = benchmarkOpt.get();
		int warmup = benchmark.warmup();
		int iterations = benchmark.iterations();

		Preconditions.condition(warmup >= 0,
			() -> "@Benchmark on method [%s] must have a non-negative 'warmup' value.".formatted(testMethod));
		Preconditions.condition(iterations > 0,
			() -> "@Benchmark on method [%s] must have a positive 'iterations' value.".formatted(testMethod));

		Object testInstance = extensionContext.getRequiredTestInstance();

		// The original invocation must be proceeded exactly once per the contract.
		// We proceed it as the first warm-up, then use reflection for the rest.
		invocation.proceed();

		// Remaining warm-up iterations (first one was the proceed() call above)
		for (int i = 1; i < warmup; i++) {
			invokeTestMethod(testMethod, testInstance);
		}

		// Measured iterations
		long[] durations = new long[iterations];
		for (int i = 0; i < iterations; i++) {
			long start = System.nanoTime();
			invokeTestMethod(testMethod, testInstance);
			durations[i] = System.nanoTime() - start;
		}

		BenchmarkResult result = new BenchmarkResult(durations, warmup, iterations);

		// Publish statistics as report entries
		extensionContext.publishReportEntry(result.toReportEntries());

		// Validate against @ExpectedPerformance if present
		findAnnotation(testMethod, ExpectedPerformance.class).ifPresent(expected -> {
			String failureMessage = result.validate(expected);
			if (failureMessage != null) {
				throw new AssertionFailedError(
					"Benchmark performance constraints violated:\n" + failureMessage + "\n\n" + result.toSummary());
			}
		});
	}

	private static void invokeTestMethod(Method method, Object instance) throws Throwable {
		try {
			method.setAccessible(true);
			method.invoke(instance);
		}
		catch (java.lang.reflect.InvocationTargetException e) {
			throw e.getCause();
		}
	}

}
