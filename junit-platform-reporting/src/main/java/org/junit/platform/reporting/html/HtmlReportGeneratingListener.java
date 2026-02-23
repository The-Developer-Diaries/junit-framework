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

import static java.util.Objects.requireNonNull;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZoneId;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * {@code HtmlReportGeneratingListener} is a {@link TestExecutionListener} that
 * generates a self-contained HTML test report at the end of test execution.
 *
 * <p>The report includes:
 * <ul>
 * <li>A summary dashboard with total, passed, failed, and skipped test counts</li>
 * <li>A visual progress bar showing the pass/fail ratio</li>
 * <li>Total execution duration</li>
 * <li>A detailed table of all test results with per-test durations</li>
 * <li>Expandable stack traces for failed tests</li>
 * </ul>
 *
 * <p>The generated HTML file is fully self-contained with embedded CSS and
 * requires no external dependencies or JavaScript frameworks.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * Path reportsDir = Path.of("build/reports/tests");
 * PrintWriter out = new PrintWriter(System.out);
 * HtmlReportGeneratingListener listener =
 *     new HtmlReportGeneratingListener(reportsDir, out);
 *
 * Launcher launcher = LauncherFactory.create();
 * launcher.execute(request, listener);
 * }</pre>
 *
 * @since 6.1
 * @see org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener
 * @see org.junit.platform.launcher.listeners.SummaryGeneratingListener
 */
@API(status = EXPERIMENTAL, since = "6.1")
public class HtmlReportGeneratingListener implements TestExecutionListener {

	private final Path reportsDir;
	private final PrintWriter out;
	private final Clock clock;

	private @Nullable HtmlReportData reportData;

	/**
	 * Create a new {@code HtmlReportGeneratingListener} that writes the HTML
	 * report to the specified directory.
	 *
	 * @param reportsDir the directory in which to write the HTML report; will
	 * be created if it does not exist
	 * @param out writer for diagnostic messages (e.g., errors during report
	 * generation)
	 */
	public HtmlReportGeneratingListener(Path reportsDir, PrintWriter out) {
		this(reportsDir, out, Clock.system(ZoneId.systemDefault()));
	}

	// For tests only
	HtmlReportGeneratingListener(Path reportsDir, PrintWriter out, Clock clock) {
		this.reportsDir = reportsDir;
		this.out = out;
		this.clock = clock;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		this.reportData = new HtmlReportData(testPlan, this.clock);
		try {
			Files.createDirectories(this.reportsDir);
		}
		catch (IOException e) {
			printException("Could not create reports directory: " + this.reportsDir, e);
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		writeHtmlReport();
		this.reportData = null;
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		requiredReportData().markSkipped(testIdentifier, reason);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		requiredReportData().markStarted(testIdentifier);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
		requiredReportData().markFinished(testIdentifier, result);
	}

	private void writeHtmlReport() {
		Path htmlFile = this.reportsDir.resolve("index.html");
		try (Writer fileWriter = Files.newBufferedWriter(htmlFile)) {
			new HtmlReportWriter(requiredReportData()).writeHtmlReport(fileWriter);
			this.out.println("HTML test report generated: " + htmlFile.toAbsolutePath());
		}
		catch (IOException e) {
			printException("Could not write HTML report: " + htmlFile, e);
		}
	}

	private HtmlReportData requiredReportData() {
		return requireNonNull(this.reportData);
	}

	private void printException(String message, Exception exception) {
		out.println(message);
		exception.printStackTrace(out);
	}

}
