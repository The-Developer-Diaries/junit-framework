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

import java.util.List;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

/**
 * {@link TestTemplateInvocationContext} for a single trial of a
 * {@link org.junit.jupiter.api.PropertyTest @PropertyTest}.
 *
 * @since 6.1
 */
class PropertyTestInvocationContext implements TestTemplateInvocationContext {

	private final int currentTrial;
	private final int totalTrials;
	private final PropertyTestDisplayNameFormatter formatter;
	private final ForAllParameterResolver parameterResolver;

	PropertyTestInvocationContext(int currentTrial, int totalTrials, PropertyTestDisplayNameFormatter formatter,
			ForAllParameterResolver parameterResolver) {
		this.currentTrial = currentTrial;
		this.totalTrials = totalTrials;
		this.formatter = formatter;
		this.parameterResolver = parameterResolver;
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return this.formatter.format(this.currentTrial, this.totalTrials);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return List.of(this.parameterResolver);
	}

}
