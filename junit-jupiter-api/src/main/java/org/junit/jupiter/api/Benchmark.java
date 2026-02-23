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
 * {@code @Benchmark} is used to signal that the annotated method is a
 * <em>performance benchmark test</em>. The method will be executed multiple
 * times: first as warm-up iterations (not measured), then as measured
 * iterations whose timing statistics are collected and optionally asserted
 * against performance constraints specified via {@link ExpectedPerformance}.
 *
 * <p>{@code @Benchmark} is a composed annotation that includes {@link Test @Test}
 * semantics, so the annotated method is discovered and executed as a regular
 * test. No additional annotations are needed.
 *
 * <p>{@code @Benchmark} methods must not be {@code private} or {@code static}
 * and must return {@code void}.
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * @Benchmark(warmup = 5, iterations = 100)
 * @ExpectedPerformance(maxAvgMs = 50)
 * void sortingPerformance() {
 *     Collections.sort(largeList);
 * }
 *
 * @Benchmark(warmup = 3, iterations = 50)
 * @ExpectedPerformance(maxAvgMs = 10, maxP99Ms = 25)
 * void hashMapLookup() {
 *     map.get("key");
 * }
 * }</pre>
 *
 * <h2>Reported Statistics</h2>
 *
 * <p>After execution, the following statistics are published as report entries
 * via the {@link TestReporter}:
 * <ul>
 * <li>Average (mean) duration</li>
 * <li>Minimum duration</li>
 * <li>Maximum duration</li>
 * <li>P50 (median) duration</li>
 * <li>P95 duration</li>
 * <li>P99 duration</li>
 * <li>Total iterations and warmup count</li>
 * </ul>
 *
 * @since 6.1
 * @see ExpectedPerformance
 * @see Test
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "6.1")
@Testable
@Test
public @interface Benchmark {

	/**
	 * The number of warm-up iterations to execute before measurement begins.
	 *
	 * <p>Warm-up iterations allow the JVM to perform JIT compilation and
	 * other optimizations before timing data is collected.
	 *
	 * <p>Defaults to {@code 5}.
	 *
	 * @return the number of warm-up iterations; must not be negative
	 */
	int warmup() default 5;

	/**
	 * The number of measured iterations to execute.
	 *
	 * <p>Each iteration is timed individually and contributes to the
	 * statistical analysis.
	 *
	 * <p>Defaults to {@code 100}.
	 *
	 * @return the number of measured iterations; must be greater than zero
	 */
	int iterations() default 100;

}
