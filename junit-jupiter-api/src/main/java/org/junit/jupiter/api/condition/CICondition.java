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

import java.util.List;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link ExecutionCondition} for {@link EnabledOnCI @EnabledOnCI} and
 * {@link DisabledOnCI @DisabledOnCI}.
 *
 * <p>Detects CI environments by checking for the presence of common CI
 * environment variables used by GitHub Actions, GitLab CI, Jenkins,
 * Travis CI, CircleCI, Bitbucket Pipelines, Azure DevOps, TeamCity,
 * and others.
 *
 * @since 6.1
 */
@API(status = API.Status.INTERNAL)
public class CICondition implements ExecutionCondition {

	public CICondition() {

	}

	/**
	 * Common CI environment variables. If any of these is set (non-null),
	 * the environment is considered a CI environment.
	 */
	private static final List<String> CI_ENV_VARS = List.of("CI", // Generic (GitHub Actions, GitLab CI, Travis, etc.)
		"CONTINUOUS_INTEGRATION", // Generic
		"BUILD_NUMBER", // Jenkins, TeamCity
		"JENKINS_URL", // Jenkins
		"GITHUB_ACTIONS", // GitHub Actions
		"GITLAB_CI", // GitLab CI
		"TRAVIS", // Travis CI
		"CIRCLECI", // CircleCI
		"BITBUCKET_BUILD_NUMBER", // Bitbucket Pipelines
		"TF_BUILD", // Azure DevOps
		"TEAMCITY_VERSION", // TeamCity
		"BUILDKITE", // Buildkite
		"CODEBUILD_BUILD_ID" // AWS CodeBuild
	);

	private static final ConditionEvaluationResult ENABLED_NO_ANNOTATION = enabled(
		"@EnabledOnCI or @DisabledOnCI is not present");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		var enabledAnnotation = findAnnotation(context.getElement(), EnabledOnCI.class);
		var disabledAnnotation = findAnnotation(context.getElement(), DisabledOnCI.class);

		if (enabledAnnotation.isEmpty() && disabledAnnotation.isEmpty()) {
			return ENABLED_NO_ANNOTATION;
		}

		String detectedVar = detectCIEnvironmentVariable();
		boolean isCI = (detectedVar != null);

		if (enabledAnnotation.isPresent()) {
			if (isCI) {
				return enabled("Running in CI environment (detected: %s)".formatted(detectedVar));
			}
			return disabled("Not running in CI environment", enabledAnnotation.get().disabledReason());
		}

		// disabledAnnotation.isPresent()
		if (isCI) {
			return disabled("Running in CI environment (detected: %s)".formatted(detectedVar),
				disabledAnnotation.get().disabledReason());
		}
		return enabled("Not running in CI environment");
	}

	/**
	 * Detect whether the current environment is a CI environment.
	 *
	 * @return the name of the first detected CI environment variable, or
	 * {@code null} if not in a CI environment
	 */
	@Nullable
	String detectCIEnvironmentVariable() {
		for (String varName : CI_ENV_VARS) {
			if (getEnvironmentVariable(varName) != null) {
				return varName;
			}
		}
		return null;
	}

	/**
	 * Get the value of the named environment variable.
	 * Can be overridden in a subclass for testing purposes.
	 */
	@Nullable
	String getEnvironmentVariable(String name) {
		return System.getenv(name);
	}

}
