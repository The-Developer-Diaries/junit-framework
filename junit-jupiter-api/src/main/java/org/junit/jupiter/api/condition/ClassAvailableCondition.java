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
 * {@link ExecutionCondition} for {@link EnabledIfClass @EnabledIfClass} and
 * {@link DisabledIfClass @DisabledIfClass}.
 *
 * @since 6.1
 */
@API(status = API.Status.INTERNAL)
public class ClassAvailableCondition implements ExecutionCondition {

	public ClassAvailableCondition() {

	}

	private static final ConditionEvaluationResult ENABLED_NO_ANNOTATION = enabled(
		"@EnabledIfClass or @DisabledIfClass is not present");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		var enabledAnnotation = findAnnotation(context.getElement(), EnabledIfClass.class);
		var disabledAnnotation = findAnnotation(context.getElement(), DisabledIfClass.class);

		if (enabledAnnotation.isEmpty() && disabledAnnotation.isEmpty()) {
			return ENABLED_NO_ANNOTATION;
		}

		if (enabledAnnotation.isPresent()) {
			String className = enabledAnnotation.get().value();
			boolean present = isClassPresent(className);
			if (present) {
				return enabled("Class [%s] is present on the classpath".formatted(className));
			}
			return disabled("Class [%s] is not present on the classpath".formatted(className),
				enabledAnnotation.get().disabledReason());
		}

		// disabledAnnotation.isPresent()
		String className = disabledAnnotation.get().value();
		boolean present = isClassPresent(className);
		if (present) {
			return disabled("Class [%s] is present on the classpath".formatted(className),
				disabledAnnotation.get().disabledReason());
		}
		return enabled("Class [%s] is not present on the classpath".formatted(className));
	}

	boolean isClassPresent(String className) {
		try {
			Class.forName(className, false, Thread.currentThread().getContextClassLoader());
			return true;
		}
		catch (ClassNotFoundException e) {
			return false;
		}
	}

}
