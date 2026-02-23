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

import java.lang.reflect.Method;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * {@link ExecutionCondition} for {@link EnabledIfProperty @EnabledIfProperty}
 * and {@link DisabledIfProperty @DisabledIfProperty}.
 *
 * <p>Evaluates conditions based on JUnit configuration parameters and/or
 * method references returning boolean values.
 *
 * @since 6.1
 */
@API(status = API.Status.INTERNAL)
public class PropertyCondition implements ExecutionCondition {

	public PropertyCondition() {

	}

	private static final ConditionEvaluationResult ENABLED_NO_ANNOTATION = enabled(
		"@EnabledIfProperty or @DisabledIfProperty is not present");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		var enabledAnnotation = findAnnotation(context.getElement(), EnabledIfProperty.class);
		if (enabledAnnotation.isPresent()) {
			EnabledIfProperty ann = enabledAnnotation.get();
			return evaluate(ann.named(), ann.matches(), ann.method(), ann.disabledReason(), context, true);
		}

		var disabledAnnotation = findAnnotation(context.getElement(), DisabledIfProperty.class);
		if (disabledAnnotation.isPresent()) {
			DisabledIfProperty ann = disabledAnnotation.get();
			return evaluate(ann.named(), ann.matches(), ann.method(), ann.disabledReason(), context, false);
		}

		return ENABLED_NO_ANNOTATION;
	}

	private ConditionEvaluationResult evaluate(String named, String matches, String method, String customReason,
			ExtensionContext context, boolean enableOnMatch) {

		boolean hasNamedCondition = !named.isBlank();
		boolean hasMethodCondition = !method.isBlank();

		Preconditions.condition(hasNamedCondition || hasMethodCondition,
			"@%s must specify at least 'named' or 'method'".formatted(
				enableOnMatch ? "EnabledIfProperty" : "DisabledIfProperty"));

		// Evaluate named property condition
		boolean namedResult = true;
		String namedReason = "";
		if (hasNamedCondition) {
			Optional<String> paramValue = context.getConfigurationParameter(named);
			if (paramValue.isEmpty()) {
				// Also check system property as fallback
				String sysProp = System.getProperty(named);
				paramValue = Optional.ofNullable(sysProp);
			}
			if (paramValue.isPresent() && paramValue.get().matches(matches)) {
				namedReason = "Property [%s] with value [%s] matches [%s]".formatted(named, paramValue.get(), matches);
			}
			else {
				namedResult = false;
				namedReason = paramValue.map(
					v -> "Property [%s] with value [%s] does not match [%s]".formatted(named, v, matches)).orElse(
						"Property [%s] is not set".formatted(named));
			}
		}

		// Evaluate method condition
		boolean methodResult = true;
		String methodReason = "";
		if (hasMethodCondition) {
			boolean methodReturnValue = invokeConditionMethod(method, context);
			if (methodReturnValue) {
				methodReason = "Method [%s] returned true".formatted(method);
			}
			else {
				methodResult = false;
				methodReason = "Method [%s] returned false".formatted(method);
			}
		}

		// Both conditions must pass
		boolean allConditionsMet = namedResult && methodResult;
		String combinedReason = buildCombinedReason(namedReason, methodReason);

		if (enableOnMatch) {
			return allConditionsMet ? enabled(combinedReason) : disabled(combinedReason, customReason);
		}
		else {
			return allConditionsMet ? disabled(combinedReason, customReason) : enabled(combinedReason);
		}
	}

	private boolean invokeConditionMethod(String methodRef, ExtensionContext context) {
		Class<?> testClass = context.getRequiredTestClass();
		Method method;

		if (methodRef.contains("#")) {
			String[] parts = ReflectionUtils.parseFullyQualifiedMethodName(methodRef);
			String className = parts[0];
			String methodName = parts[1];
			ClassLoader classLoader = ClassLoaderUtils.getClassLoader(testClass);
			Class<?> clazz = ReflectionSupport.tryToLoadClass(className, classLoader).getNonNullOrThrow(
				cause -> new JUnitException("Could not load class [%s]".formatted(className), cause));
			method = findConditionMethod(clazz, methodName);
		}
		else {
			method = findConditionMethod(testClass, methodRef);
		}

		Preconditions.condition(method.getReturnType() == boolean.class,
			() -> "Condition method [%s] must return a boolean".formatted(method));

		Object testInstance = context.getTestInstance().orElse(null);

		if (method.getParameterCount() == 0) {
			Object result = ReflectionSupport.invokeMethod(method, testInstance);
			if (!(result instanceof Boolean b)) {
				throw new JUnitException("Condition method [%s] must return a boolean".formatted(method));
			}
			return b;
		}
		if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == ExtensionContext.class) {
			Object result = ReflectionSupport.invokeMethod(method, testInstance, context);
			if (!(result instanceof Boolean b)) {
				throw new JUnitException("Condition method [%s] must return a boolean".formatted(method));
			}
			return b;
		}
		throw new JUnitException(
			"Condition method [%s] must accept either no arguments or a single ExtensionContext".formatted(method));
	}

	private static Method findConditionMethod(Class<?> clazz, String methodName) {
		return ReflectionSupport.findMethod(clazz, methodName) //
				.orElseGet(() -> ReflectionUtils.getRequiredMethod(clazz, methodName, ExtensionContext.class));
	}

	private static String buildCombinedReason(String reason1, String reason2) {
		if (reason1.isEmpty()) {
			return reason2;
		}
		if (reason2.isEmpty()) {
			return reason1;
		}
		return reason1 + "; " + reason2;
	}

}
