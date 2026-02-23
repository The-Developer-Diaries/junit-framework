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

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

import java.lang.reflect.Method;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.PropertyTest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code TestTemplateInvocationContextProvider} that supports the
 * {@link PropertyTest @PropertyTest} annotation.
 *
 * <p>For each trial, this extension creates a new {@link Random} instance
 * seeded from the property test's seed (or a randomly chosen seed). Each
 * trial's random values are generated from this per-trial RNG, ensuring
 * that the same seed produces the same sequence of values across all
 * trials.
 *
 * @since 6.1
 */
class PropertyTestExtension implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return isAnnotated(context.getTestMethod(), PropertyTest.class);
	}

	@Override
	public Stream<PropertyTestInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		Method testMethod = context.getRequiredTestMethod();
		String displayName = context.getDisplayName();
		PropertyTest propertyTest = findAnnotation(testMethod, PropertyTest.class).get();
		int trials = validatedTrials(propertyTest, testMethod);
		long baseSeed = resolveBaseSeed(propertyTest);
		PropertyTestDisplayNameFormatter formatter = displayNameFormatter(propertyTest, testMethod, displayName);

		// @formatter:off
		return IntStream
				.rangeClosed(1, trials)
				.mapToObj(trial -> {
					// Each trial gets a deterministic seed derived from baseSeed + trial
					long trialSeed = baseSeed + trial;
					Random trialRandom = new Random(trialSeed);
					ForAllParameterResolver resolver = new ForAllParameterResolver(trialRandom, trial, baseSeed);
					return new PropertyTestInvocationContext(trial, trials, formatter, resolver);
				});
		// @formatter:on
	}

	private int validatedTrials(PropertyTest propertyTest, Method method) {
		int trials = propertyTest.trials();
		Preconditions.condition(trials > 0,
			() -> "Configuration error: @PropertyTest on method [%s] must be declared with a positive 'trials' value.".formatted(
				method));
		return trials;
	}

	private long resolveBaseSeed(PropertyTest propertyTest) {
		long seed = propertyTest.seed();
		if (seed == 0) {
			// Generate a random seed and log it for reproducibility
			seed = new Random().nextLong();
		}
		return seed;
	}

	private PropertyTestDisplayNameFormatter displayNameFormatter(PropertyTest propertyTest, Method method,
			String displayName) {
		String pattern = Preconditions.notBlank(propertyTest.name().strip(),
			() -> "Configuration error: @PropertyTest on method [%s] must be declared with a non-empty name.".formatted(
				method));
		return new PropertyTestDisplayNameFormatter(pattern, displayName);
	}

}
