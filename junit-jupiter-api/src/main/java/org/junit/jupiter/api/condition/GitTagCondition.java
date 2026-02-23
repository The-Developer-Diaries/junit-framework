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

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link ExecutionCondition} for {@link EnabledIfGitTag @EnabledIfGitTag} and
 * {@link DisabledIfGitTag @DisabledIfGitTag}.
 *
 * @since 6.1
 */
@API(status = API.Status.INTERNAL)
public class GitTagCondition implements ExecutionCondition {

	public GitTagCondition() {

	}

	private static final ConditionEvaluationResult ENABLED_NO_ANNOTATION = enabled(
		"@EnabledIfGitTag or @DisabledIfGitTag is not present");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		var enabledAnnotation = findAnnotation(context.getElement(), EnabledIfGitTag.class);
		var disabledAnnotation = findAnnotation(context.getElement(), DisabledIfGitTag.class);

		if (enabledAnnotation.isEmpty() && disabledAnnotation.isEmpty()) {
			return ENABLED_NO_ANNOTATION;
		}

		String currentTag = getCurrentTag();
		boolean isTagged = (currentTag != null);

		if (enabledAnnotation.isPresent()) {
			if (isTagged) {
				return enabled("Current commit is tagged [%s]".formatted(currentTag));
			}
			return disabled("Current commit is not tagged", enabledAnnotation.get().disabledReason());
		}

		// disabledAnnotation.isPresent()
		if (isTagged) {
			return disabled("Current commit is tagged [%s]".formatted(currentTag),
				disabledAnnotation.get().disabledReason());
		}
		return enabled("Current commit is not tagged");
	}

	/**
	 * Get the tag for the current Git commit, if any.
	 * Can be overridden in a subclass for testing purposes.
	 */
	@Nullable
	String getCurrentTag() {
		// First check common CI environment variables
		String tag = System.getenv("GITHUB_REF_TYPE");
		if ("tag".equals(tag)) {
			String tagName = System.getenv("GITHUB_REF_NAME");
			if (tagName != null && !tagName.isBlank()) {
				return tagName;
			}
		}
		tag = System.getenv("CI_COMMIT_TAG");
		if (tag != null && !tag.isBlank()) {
			return tag;
		}

		// Fall back to git command
		try {
			Process process = new ProcessBuilder("git", "describe", "--tags", "--exact-match",
				"HEAD").redirectErrorStream(true).start();
			String output = new String(process.getInputStream().readAllBytes(), Charset.defaultCharset()).strip();
			int exitCode = process.waitFor();
			if (exitCode == 0 && !output.isBlank()) {
				return output;
			}
		}
		catch (Exception e) {
			// Ignore — git not available or commit not tagged
		}
		return null;
	}

}
