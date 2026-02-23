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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * {@code @DisabledIfGitBranchMatches} is used to signal that the annotated test
 * class or test method is <em>disabled</em> if the current Git branch name
 * matches the specified pattern.
 *
 * @since 6.1
 * @see EnabledIfGitBranchMatches
 * @see DisabledIfGitBranch
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(GitBranchCondition.class)
@API(status = EXPERIMENTAL, since = "6.1")
public @interface DisabledIfGitBranchMatches {

	/**
	 * A regular expression pattern to match against the current Git branch name.
	 */
	String value();

	/**
	 * Custom reason to provide if the test is disabled.
	 */
	String disabledReason() default "";

}
