/*
 * Copyright 2015-2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.reporting.html;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jspecify.annotations.Nullable;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Collects test execution data for HTML report generation.
 *
 * @since 6.1
 */
class HtmlReportData {

	private final TestPlan testPlan;
	private final Clock clock;
	private final Instant startInstant;

	private final Map<TestIdentifier, TestExecutionResult> results = new ConcurrentHashMap<>();
	private final Map<TestIdentifier, String> skippedReasons = new ConcurrentHashMap<>();
	private final Map<TestIdentifier, Instant> startTimes = new ConcurrentHashMap<>();
	private final Map<TestIdentifier, Instant> endTimes = new ConcurrentHashMap<>();

	private final AtomicInteger totalTests = new AtomicInteger();
	private final AtomicInteger passedTests = new AtomicInteger();
	private final AtomicInteger failedTests = new AtomicInteger();
	private final AtomicInteger skippedTestCount = new AtomicInteger();
	private final AtomicInteger abortedTests = new AtomicInteger();

	HtmlReportData(TestPlan testPlan, Clock clock) {
		this.testPlan = testPlan;
		this.clock = clock;
		this.startInstant = clock.instant();
	}

	void markSkipped(TestIdentifier testIdentifier, @Nullable String reason) {
		if (testIdentifier.isTest()) {
			this.skippedReasons.put(testIdentifier, reason != null ? reason : "");
			this.totalTests.incrementAndGet();
			this.skippedTestCount.incrementAndGet();
		}
	}

	void markStarted(TestIdentifier testIdentifier) {
		this.startTimes.put(testIdentifier, this.clock.instant());
	}

	void markFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
		this.endTimes.put(testIdentifier, this.clock.instant());
		this.results.put(testIdentifier, result);
		if (testIdentifier.isTest()) {
			this.totalTests.incrementAndGet();
			switch (result.getStatus()) {
				case SUCCESSFUL -> this.passedTests.incrementAndGet();
				case FAILED -> this.failedTests.incrementAndGet();
				case ABORTED -> this.abortedTests.incrementAndGet();
			}
		}
	}

	TestPlan getTestPlan() {
		return this.testPlan;
	}

	int getTotalTests() {
		return this.totalTests.get();
	}

	int getPassedTests() {
		return this.passedTests.get();
	}

	int getFailedTests() {
		return this.failedTests.get();
	}

	int getSkippedTests() {
		return this.skippedTestCount.get();
	}

	int getAbortedTests() {
		return this.abortedTests.get();
	}

	Duration getTotalDuration() {
		return Duration.between(this.startInstant, this.clock.instant());
	}

	long getDurationMs(TestIdentifier testIdentifier) {
		Instant start = this.startTimes.getOrDefault(testIdentifier, Instant.EPOCH);
		Instant end = this.endTimes.getOrDefault(testIdentifier, start);
		return Duration.between(start, end).toMillis();
	}

	@Nullable
	TestExecutionResult getResult(TestIdentifier testIdentifier) {
		return this.results.get(testIdentifier);
	}

	@Nullable
	String getSkipReason(TestIdentifier testIdentifier) {
		return this.skippedReasons.get(testIdentifier);
	}

	String getStatusLabel(TestIdentifier testIdentifier) {
		String skipReason = this.skippedReasons.get(testIdentifier);
		if (skipReason != null) {
			return "SKIPPED";
		}
		TestExecutionResult result = this.results.get(testIdentifier);
		if (result == null) {
			return "UNKNOWN";
		}
		return result.getStatus().name();
	}

	String getStatusCssClass(TestIdentifier testIdentifier) {
		String label = getStatusLabel(testIdentifier);
		return switch (label) {
			case "SUCCESSFUL" -> "passed";
			case "FAILED" -> "failed";
			case "ABORTED" -> "aborted";
			case "SKIPPED" -> "skipped";
			default -> "unknown";
		};
	}

	@Nullable
	String getFailureDetail(TestIdentifier testIdentifier) {
		TestExecutionResult result = this.results.get(testIdentifier);
		if (result != null && result.getStatus() == Status.FAILED) {
			return result.getThrowable().map(ExceptionUtils::readStackTrace).orElse(null);
		}
		return null;
	}

}
