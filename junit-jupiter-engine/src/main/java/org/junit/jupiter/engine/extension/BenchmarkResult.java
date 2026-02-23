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

import java.util.Arrays;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.ExpectedPerformance;

/**
 * Holds statistical results from a benchmark execution.
 *
 * @since 6.1
 */
class BenchmarkResult {

	private final long[] durationsNanos;
	private final int warmupIterations;
	private final int measuredIterations;

	BenchmarkResult(long[] durationsNanos, int warmupIterations, int measuredIterations) {
		this.durationsNanos = durationsNanos.clone();
		Arrays.sort(this.durationsNanos);
		this.warmupIterations = warmupIterations;
		this.measuredIterations = measuredIterations;
	}

	double avgMs() {
		long sum = 0;
		for (long d : durationsNanos) {
			sum += d;
		}
		return nanosToMs(sum / durationsNanos.length);
	}

	double minMs() {
		return nanosToMs(durationsNanos[0]);
	}

	double maxMs() {
		return nanosToMs(durationsNanos[durationsNanos.length - 1]);
	}

	double p50Ms() {
		return percentileMs(50);
	}

	double p95Ms() {
		return percentileMs(95);
	}

	double p99Ms() {
		return percentileMs(99);
	}

	private double percentileMs(int percentile) {
		int index = (int) Math.ceil(percentile / 100.0 * durationsNanos.length) - 1;
		return nanosToMs(durationsNanos[Math.max(0, Math.min(index, durationsNanos.length - 1))]);
	}

	private static double nanosToMs(long nanos) {
		return nanos / 1_000_000.0;
	}

	/**
	 * Create a report entry map with all benchmark statistics.
	 */
	Map<String, String> toReportEntries() {
		return Map.of("benchmark.warmup", String.valueOf(warmupIterations), "benchmark.iterations",
			String.valueOf(measuredIterations), "benchmark.avg_ms", "%.3f".formatted(avgMs()), "benchmark.min_ms",
			"%.3f".formatted(minMs()), "benchmark.max_ms", "%.3f".formatted(maxMs()), "benchmark.p50_ms",
			"%.3f".formatted(p50Ms()), "benchmark.p95_ms", "%.3f".formatted(p95Ms()), "benchmark.p99_ms",
			"%.3f".formatted(p99Ms()));
	}

	/**
	 * Validate this result against the expected performance constraints.
	 *
	 * @return {@code null} if all constraints pass, or a failure message
	 */
	@Nullable
	String validate(ExpectedPerformance expected) {
		StringBuilder failures = new StringBuilder();

		checkConstraint(failures, "avg", expected.maxAvgMs(), avgMs());
		checkConstraint(failures, "min", expected.maxMinMs(), minMs());
		checkConstraint(failures, "max", expected.maxMaxMs(), maxMs());
		checkConstraint(failures, "p50", expected.maxP50Ms(), p50Ms());
		checkConstraint(failures, "p95", expected.maxP95Ms(), p95Ms());
		checkConstraint(failures, "p99", expected.maxP99Ms(), p99Ms());

		return failures.isEmpty() ? null : failures.toString();
	}

	private static void checkConstraint(StringBuilder failures, String name, long maxMs, double actualMs) {
		if (maxMs >= 0 && actualMs > maxMs) {
			if (!failures.isEmpty()) {
				failures.append("\n");
			}
			failures.append("  %s: %.3f ms exceeded max %d ms".formatted(name, actualMs, maxMs));
		}
	}

	/**
	 * Format a human-readable summary string.
	 */
	String toSummary() {
		return """
				Benchmark results (%d iterations, %d warmup):
				  avg=%.3f ms, min=%.3f ms, max=%.3f ms
				  p50=%.3f ms, p95=%.3f ms, p99=%.3f ms""".formatted(measuredIterations, warmupIterations, avgMs(),
			minMs(), maxMs(), p50Ms(), p95Ms(), p99Ms());
	}

}
