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

/**
 * {@code @ExpectedPerformance} is used in conjunction with {@link Benchmark @Benchmark}
 * to specify performance constraints that the benchmark must meet.
 *
 * <p>If any constraint is violated, the test fails with a descriptive message
 * showing the expected vs. actual performance metrics.
 *
 * <p>All duration thresholds are specified in milliseconds. A value of
 * {@code -1} (the default) means the constraint is not enforced.
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * @Benchmark(warmup = 5, iterations = 100)
 * @ExpectedPerformance(maxAvgMs = 50, maxP99Ms = 100)
 * void sortingPerformance() {
 *     Collections.sort(largeList);
 * }
 * }</pre>
 *
 * @since 6.1
 * @see Benchmark
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = EXPERIMENTAL, since = "6.1")
public @interface ExpectedPerformance {

	/**
	 * The maximum allowed average (mean) duration in milliseconds.
	 *
	 * <p>Defaults to {@code -1} (not enforced).
	 */
	long maxAvgMs() default -1;

	/**
	 * The maximum allowed minimum duration in milliseconds.
	 *
	 * <p>Defaults to {@code -1} (not enforced).
	 */
	long maxMinMs() default -1;

	/**
	 * The maximum allowed maximum duration in milliseconds.
	 *
	 * <p>Defaults to {@code -1} (not enforced).
	 */
	long maxMaxMs() default -1;

	/**
	 * The maximum allowed P50 (median) duration in milliseconds.
	 *
	 * <p>Defaults to {@code -1} (not enforced).
	 */
	long maxP50Ms() default -1;

	/**
	 * The maximum allowed P95 duration in milliseconds.
	 *
	 * <p>Defaults to {@code -1} (not enforced).
	 */
	long maxP95Ms() default -1;

	/**
	 * The maximum allowed P99 duration in milliseconds.
	 *
	 * <p>Defaults to {@code -1} (not enforced).
	 */
	long maxP99Ms() default -1;

}
