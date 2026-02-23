/*
 * Copyright 2015-2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.nio.charset.Charset;
import java.util.Arrays;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link ExecutionCondition} for {@link EnabledIfGitBranch @EnabledIfGitBranch},
 * {@link DisabledIfGitBranch @DisabledIfGitBranch},
 * {@link EnabledIfGitBranchMatches @EnabledIfGitBranchMatches}, and
 * {@link DisabledIfGitBranchMatches @DisabledIfGitBranchMatches}.
 *
 * @since 6.1
 */
@API(status = API.Status.INTERNAL)
public class GitBranchCondition implements ExecutionCondition {

	public GitBranchCondition() {

	}

	private static final ConditionEvaluationResult ENABLED_NO_ANNOTATION = enabled(
		"No Git branch condition annotation is present");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		// --- Exact branch match ---
		var enabledExact = findAnnotation(context.getElement(), EnabledIfGitBranch.class);
		if (enabledExact.isPresent()) {
			return evaluateExactBranch(enabledExact.get().value(), enabledExact.get().disabledReason(), true);
		}

		var disabledExact = findAnnotation(context.getElement(), DisabledIfGitBranch.class);
		if (disabledExact.isPresent()) {
			return evaluateExactBranch(disabledExact.get().value(), disabledExact.get().disabledReason(), false);
		}

		// --- Pattern branch match ---
		var enabledPattern = findAnnotation(context.getElement(), EnabledIfGitBranchMatches.class);
		if (enabledPattern.isPresent()) {
			return evaluatePatternBranch(enabledPattern.get().value(), enabledPattern.get().disabledReason(), true);
		}

		var disabledPattern = findAnnotation(context.getElement(), DisabledIfGitBranchMatches.class);
		if (disabledPattern.isPresent()) {
			return evaluatePatternBranch(disabledPattern.get().value(), disabledPattern.get().disabledReason(), false);
		}

		return ENABLED_NO_ANNOTATION;
	}

	private ConditionEvaluationResult evaluateExactBranch(String[] branches, String customReason,
			boolean enableOnMatch) {

		String currentBranch = getCurrentBranch();
		if (currentBranch == null) {
			return enableOnMatch ? disabled("Could not determine current Git branch", customReason)
					: enabled("Could not determine current Git branch");
		}

		boolean matches = Arrays.asList(branches).contains(currentBranch);

		if (enableOnMatch) {
			if (matches) {
				return enabled("Current Git branch [%s] matches".formatted(currentBranch));
			}
			return disabled(
				"Current Git branch [%s] does not match any of %s".formatted(currentBranch, Arrays.toString(branches)),
				customReason);
		}
		else {
			if (matches) {
				return disabled("Current Git branch [%s] matches".formatted(currentBranch), customReason);
			}
			return enabled(
				"Current Git branch [%s] does not match any of %s".formatted(currentBranch, Arrays.toString(branches)));
		}
	}

	private ConditionEvaluationResult evaluatePatternBranch(String pattern, String customReason,
			boolean enableOnMatch) {

		String currentBranch = getCurrentBranch();
		if (currentBranch == null) {
			return enableOnMatch ? disabled("Could not determine current Git branch", customReason)
					: enabled("Could not determine current Git branch");
		}

		boolean matches = currentBranch.matches(pattern);

		if (enableOnMatch) {
			if (matches) {
				return enabled("Current Git branch [%s] matches pattern [%s]".formatted(currentBranch, pattern));
			}
			return disabled("Current Git branch [%s] does not match pattern [%s]".formatted(currentBranch, pattern),
				customReason);
		}
		else {
			if (matches) {
				return disabled("Current Git branch [%s] matches pattern [%s]".formatted(currentBranch, pattern),
					customReason);
			}
			return enabled("Current Git branch [%s] does not match pattern [%s]".formatted(currentBranch, pattern));
		}
	}

	/**
	 * Get the current Git branch name.
	 * Can be overridden in a subclass for testing purposes.
	 */
	@Nullable
	String getCurrentBranch() {
		// First check common CI environment variables
		String branch = System.getenv("GITHUB_REF_NAME");
		if (branch != null && !branch.isBlank()) {
			return branch;
		}
		branch = System.getenv("CI_COMMIT_BRANCH");
		if (branch != null && !branch.isBlank()) {
			return branch;
		}
		branch = System.getenv("BRANCH_NAME");
		if (branch != null && !branch.isBlank()) {
			return branch;
		}

		// Fall back to git command
		try {
			Process process = new ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD").redirectErrorStream(
				true).start();
			String output = new String(process.getInputStream().readAllBytes(), Charset.defaultCharset()).strip();
			int exitCode = process.waitFor();
			if (exitCode == 0 && !output.isBlank()) {
				return output;
			}
		}
		catch (Exception e) {
			// Ignore — git not available
		}
		return null;
	}

}
