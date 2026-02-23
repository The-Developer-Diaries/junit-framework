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

import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Writes an HTML test report from collected {@link HtmlReportData}.
 *
 * <p>The generated report is a single self-contained HTML file with embedded
 * CSS styling. No external dependencies or JavaScript frameworks are required.
 *
 * @since 6.1
 */
class HtmlReportWriter {

	private final HtmlReportData reportData;

	HtmlReportWriter(HtmlReportData reportData) {
		this.reportData = reportData;
	}

	void writeHtmlReport(Writer writer) throws IOException {
		writer.write("<!DOCTYPE html>\n");
		writer.write("<html lang=\"en\">\n");
		writeHead(writer);
		writeBody(writer);
		writer.write("</html>\n");
	}

	private void writeHead(Writer writer) throws IOException {
		writer.write("<head>\n");
		writer.write("<meta charset=\"UTF-8\">\n");
		writer.write("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
		writer.write("<title>JUnit Test Report</title>\n");
		writeStyles(writer);
		writer.write("</head>\n");
	}

	private void writeStyles(Writer writer) throws IOException {
		writer.write("<style>\n");
		writer.write("""
				* { margin: 0; padding: 0; box-sizing: border-box; }
				body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
				       background: #f5f5f5; color: #333; line-height: 1.6; }
				.container { max-width: 1200px; margin: 0 auto; padding: 24px; }
				h1 { font-size: 28px; margin-bottom: 8px; color: #1a1a1a; }
				.timestamp { color: #666; font-size: 14px; margin-bottom: 24px; }

				/* Summary cards */
				.summary { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
				           gap: 16px; margin-bottom: 32px; }
				.card { background: #fff; border-radius: 8px; padding: 20px; text-align: center;
				        box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
				.card .value { font-size: 36px; font-weight: 700; }
				.card .label { font-size: 13px; color: #666; text-transform: uppercase; letter-spacing: 0.5px; }
				.card.total .value { color: #333; }
				.card.passed .value { color: #22863a; }
				.card.failed .value { color: #cb2431; }
				.card.skipped .value { color: #b08800; }
				.card.duration .value { color: #0366d6; font-size: 24px; }

				/* Progress bar */
				.progress-bar { display: flex; height: 12px; border-radius: 6px; overflow: hidden;
				                margin-bottom: 32px; background: #e1e4e8; }
				.progress-bar .segment { transition: width 0.3s; }
				.progress-bar .passed-seg { background: #22863a; }
				.progress-bar .failed-seg { background: #cb2431; }
				.progress-bar .skipped-seg { background: #dbab09; }
				.progress-bar .aborted-seg { background: #e36209; }

				/* Test results table */
				.results { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);
				           overflow: hidden; }
				.results table { width: 100%; border-collapse: collapse; }
				.results th { background: #f6f8fa; text-align: left; padding: 12px 16px; font-size: 13px;
				              color: #666; text-transform: uppercase; letter-spacing: 0.5px;
				              border-bottom: 2px solid #e1e4e8; }
				.results td { padding: 10px 16px; border-bottom: 1px solid #e1e4e8; font-size: 14px; }
				.results tr:last-child td { border-bottom: none; }
				.results tr:hover { background: #f6f8fa; }

				/* Status badges */
				.badge { display: inline-block; padding: 2px 10px; border-radius: 12px;
				         font-size: 12px; font-weight: 600; text-transform: uppercase; }
				.badge.passed { background: #dcffe4; color: #22863a; }
				.badge.failed { background: #ffdce0; color: #cb2431; }
				.badge.skipped { background: #fff5b1; color: #735c0f; }
				.badge.aborted { background: #ffebda; color: #e36209; }

				/* Failure details */
				.failure-detail { margin-top: 8px; }
				.failure-toggle { cursor: pointer; color: #cb2431; font-size: 12px; text-decoration: underline; }
				.failure-trace { display: none; background: #fafbfc; border: 1px solid #e1e4e8;
				                 border-radius: 4px; padding: 12px; margin-top: 6px;
				                 font-family: 'SFMono-Regular', Consolas, monospace; font-size: 12px;
				                 white-space: pre-wrap; overflow-x: auto; max-height: 300px;
				                 overflow-y: auto; color: #444; }

				.duration-col { text-align: right; color: #666; font-variant-numeric: tabular-nums; }
				""");
		writer.write("</style>\n");
	}

	private void writeBody(Writer writer) throws IOException {
		writer.write("<body>\n");
		writer.write("<div class=\"container\">\n");
		writeHeader(writer);
		writeSummaryCards(writer);
		writeProgressBar(writer);
		writeTestResults(writer);
		writeScript(writer);
		writer.write("</div>\n");
		writer.write("</body>\n");
	}

	private void writeHeader(Writer writer) throws IOException {
		writer.write("<h1>JUnit Test Report</h1>\n");
		writer.write("<p class=\"timestamp\">Generated on "
				+ LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
				+ "</p>\n");
	}

	private void writeSummaryCards(Writer writer) throws IOException {
		int total = reportData.getTotalTests();
		int passed = reportData.getPassedTests();
		int failed = reportData.getFailedTests();
		int skipped = reportData.getSkippedTests();
		Duration duration = reportData.getTotalDuration();

		writer.write("<div class=\"summary\">\n");
		writeCard(writer, "total", String.valueOf(total), "Total Tests");
		writeCard(writer, "passed", String.valueOf(passed), "Passed");
		writeCard(writer, "failed", String.valueOf(failed), "Failed");
		writeCard(writer, "skipped", String.valueOf(skipped), "Skipped");
		writeCard(writer, "duration", formatDuration(duration), "Duration");
		writer.write("</div>\n");
	}

	private void writeCard(Writer writer, String cssClass, String value, String label) throws IOException {
		writer.write("<div class=\"card " + cssClass + "\">\n");
		writer.write("  <div class=\"value\">" + value + "</div>\n");
		writer.write("  <div class=\"label\">" + label + "</div>\n");
		writer.write("</div>\n");
	}

	private void writeProgressBar(Writer writer) throws IOException {
		int total = reportData.getTotalTests();
		if (total == 0) {
			return;
		}
		double passedPct = 100.0 * reportData.getPassedTests() / total;
		double failedPct = 100.0 * reportData.getFailedTests() / total;
		double skippedPct = 100.0 * reportData.getSkippedTests() / total;
		double abortedPct = 100.0 * reportData.getAbortedTests() / total;

		writer.write("<div class=\"progress-bar\">\n");
		if (passedPct > 0) {
			writer.write("  <div class=\"segment passed-seg\" style=\"width:" + passedPct + "%\"></div>\n");
		}
		if (failedPct > 0) {
			writer.write("  <div class=\"segment failed-seg\" style=\"width:" + failedPct + "%\"></div>\n");
		}
		if (abortedPct > 0) {
			writer.write("  <div class=\"segment aborted-seg\" style=\"width:" + abortedPct + "%\"></div>\n");
		}
		if (skippedPct > 0) {
			writer.write("  <div class=\"segment skipped-seg\" style=\"width:" + skippedPct + "%\"></div>\n");
		}
		writer.write("</div>\n");
	}

	private void writeTestResults(Writer writer) throws IOException {
		writer.write("<div class=\"results\">\n");
		writer.write("<table>\n");
		writer.write(
			"<thead><tr><th>Test</th><th>Status</th><th style=\"text-align:right\">Duration</th></tr></thead>\n");
		writer.write("<tbody>\n");

		TestPlan testPlan = reportData.getTestPlan();
		for (TestIdentifier root : testPlan.getRoots()) {
			writeTestEntries(writer, testPlan, root);
		}

		writer.write("</tbody>\n");
		writer.write("</table>\n");
		writer.write("</div>\n");
	}

	private void writeTestEntries(Writer writer, TestPlan testPlan, TestIdentifier identifier) throws IOException {
		if (identifier.isTest()) {
			writeTestRow(writer, identifier);
		}
		List<TestIdentifier> children = testPlan.getChildren(identifier).stream().sorted(
			(a, b) -> a.getDisplayName().compareTo(b.getDisplayName())).toList();
		for (TestIdentifier child : children) {
			writeTestEntries(writer, testPlan, child);
		}
	}

	private void writeTestRow(Writer writer, TestIdentifier testIdentifier) throws IOException {
		String statusClass = reportData.getStatusCssClass(testIdentifier);
		String statusLabel = reportData.getStatusLabel(testIdentifier);
		long durationMs = reportData.getDurationMs(testIdentifier);
		String displayName = buildFullDisplayName(testIdentifier);
		@Nullable
		String failureDetail = reportData.getFailureDetail(testIdentifier);

		writer.write("<tr>\n");
		writer.write("  <td>" + escapeHtml(displayName));
		if (failureDetail != null) {
			String id = "trace-" + testIdentifier.getUniqueId().hashCode();
			writer.write("\n    <div class=\"failure-detail\">");
			writer.write("<span class=\"failure-toggle\" onclick=\"toggleTrace('" + id + "')\">Show stacktrace</span>");
			writer.write("<pre class=\"failure-trace\" id=\"" + id + "\">" + escapeHtml(failureDetail) + "</pre>");
			writer.write("</div>");
		}
		writer.write("</td>\n");
		writer.write("  <td><span class=\"badge " + statusClass + "\">" + statusLabel + "</span></td>\n");
		writer.write("  <td class=\"duration-col\">" + durationMs + " ms</td>\n");
		writer.write("</tr>\n");
	}

	private String buildFullDisplayName(TestIdentifier testIdentifier) {
		TestPlan testPlan = reportData.getTestPlan();
		StringBuilder path = new StringBuilder();
		testPlan.getParent(testIdentifier).ifPresent(parent -> {
			if (!parent.getParentIdObject().isEmpty()) {
				path.append(parent.getDisplayName()).append(" > ");
			}
		});
		path.append(testIdentifier.getDisplayName());
		return path.toString();
	}

	private void writeScript(Writer writer) throws IOException {
		writer.write("<script>\n");
		writer.write("""
				function toggleTrace(id) {
				  var el = document.getElementById(id);
				  if (el.style.display === 'block') {
				    el.style.display = 'none';
				  } else {
				    el.style.display = 'block';
				  }
				}
				""");
		writer.write("</script>\n");
	}

	private String formatDuration(Duration duration) {
		long totalMs = duration.toMillis();
		if (totalMs < 1000) {
			return totalMs + " ms";
		}
		double seconds = totalMs / 1000.0;
		if (seconds < 60) {
			return "%.1f s".formatted(seconds);
		}
		long minutes = duration.toMinutes();
		long secs = duration.toSecondsPart();
		return minutes + "m " + secs + "s";
	}

	private static String escapeHtml(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

}
