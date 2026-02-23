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

import java.util.Random;

import org.junit.jupiter.api.ForAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * {@link ParameterResolver} that resolves {@link ForAll @ForAll}-annotated
 * parameters with randomly generated values for property-based testing.
 *
 * <p>Each instance is created per trial with its own {@link Random} seeded
 * for reproducibility.
 *
 * @since 6.1
 */
class ForAllParameterResolver implements ParameterResolver {

	private final RandomValueGenerator generator;
	private final int trialNumber;
	private final long seed;

	ForAllParameterResolver(Random random, int trialNumber, long seed) {
		this.generator = new RandomValueGenerator(random);
		this.trialNumber = trialNumber;
		this.seed = seed;
	}

	int getTrialNumber() {
		return this.trialNumber;
	}

	long getSeed() {
		return this.seed;
	}

	@Override
	public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
		return ExtensionContextScope.TEST_METHOD;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.isAnnotated(ForAll.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		ForAll forAll = parameterContext.findAnnotation(ForAll.class).orElseThrow();
		Class<?> type = parameterContext.getParameter().getType();
		return this.generator.generate(type, forAll);
	}

}
