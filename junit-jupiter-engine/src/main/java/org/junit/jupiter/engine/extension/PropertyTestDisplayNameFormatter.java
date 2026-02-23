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

import static org.junit.jupiter.api.PropertyTest.CURRENT_TRIAL_PLACEHOLDER;
import static org.junit.jupiter.api.PropertyTest.DISPLAY_NAME_PLACEHOLDER;
import static org.junit.jupiter.api.PropertyTest.TOTAL_TRIALS_PLACEHOLDER;

import org.junit.jupiter.api.PropertyTest;

/**
 * Display name formatter for a {@link PropertyTest @PropertyTest}.
 *
 * @since 6.1
 */
class PropertyTestDisplayNameFormatter {

	private final String pattern;
	private final String displayName;

	PropertyTestDisplayNameFormatter(String pattern, String displayName) {
		this.pattern = pattern;
		this.displayName = displayName;
	}

	String format(int currentTrial, int totalTrials) {
		return this.pattern//
				.replace(DISPLAY_NAME_PLACEHOLDER, this.displayName)//
				.replace(CURRENT_TRIAL_PLACEHOLDER, String.valueOf(currentTrial))//
				.replace(TOTAL_TRIALS_PLACEHOLDER, String.valueOf(totalTrials));
	}

}
