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

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link ExecutionCondition} for {@link EnabledIfDockerAvailable @EnabledIfDockerAvailable}
 * and {@link DisabledIfDockerAvailable @DisabledIfDockerAvailable}.
 *
 * @since 6.1
 */
@API(status = API.Status.INTERNAL)
public class DockerAvailableCondition implements ExecutionCondition {

	public DockerAvailableCondition() {

	}

	private static final ConditionEvaluationResult ENABLED_NO_ANNOTATION = enabled(
		"@EnabledIfDockerAvailable or @DisabledIfDockerAvailable is not present");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		var enabledAnnotation = findAnnotation(context.getElement(), EnabledIfDockerAvailable.class);
		var disabledAnnotation = findAnnotation(context.getElement(), DisabledIfDockerAvailable.class);

		if (enabledAnnotation.isEmpty() && disabledAnnotation.isEmpty()) {
			return ENABLED_NO_ANNOTATION;
		}

		boolean dockerAvailable = isDockerAvailable();

		if (enabledAnnotation.isPresent()) {
			if (dockerAvailable) {
				return enabled("Docker is available");
			}
			return disabled("Docker is not available", enabledAnnotation.get().disabledReason());
		}

		// disabledAnnotation.isPresent()
		if (dockerAvailable) {
			return disabled("Docker is available", disabledAnnotation.get().disabledReason());
		}
		return enabled("Docker is not available");
	}

	/**
	 * Check whether Docker is available by running {@code docker info}.
	 */
	boolean isDockerAvailable() {
		try {
			Process process = new ProcessBuilder("docker", "info").redirectErrorStream(true).start();
			// Consume output to prevent blocking
			process.getInputStream().readAllBytes();
			return process.waitFor() == 0;
		}
		catch (Exception e) {
			return false;
		}
	}

}
