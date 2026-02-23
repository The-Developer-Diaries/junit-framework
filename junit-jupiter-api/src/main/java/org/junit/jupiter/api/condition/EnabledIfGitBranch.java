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
 * {@code @EnabledIfGitBranch} is used to signal that the annotated test class
 * or test method is only <em>enabled</em> if the current Git branch matches
 * one of the specified branch names exactly.
 *
 * <p>The current branch is determined by running {@code git rev-parse --abbrev-ref HEAD}.
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * @EnabledIfGitBranch("main")
 * @Test
 * void deploymentSmokeTest() { ... }
 *
 * @EnabledIfGitBranch({"main", "develop"})
 * @Test
 * void integrationTest() { ... }
 * }</pre>
 *
 * @since 6.1
 * @see DisabledIfGitBranch
 * @see EnabledIfGitBranchMatches
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(GitBranchCondition.class)
@API(status = EXPERIMENTAL, since = "6.1")
public @interface EnabledIfGitBranch {

	/**
	 * One or more exact branch names. The test is enabled if the current
	 * branch matches any of the specified names.
	 */
	String[] value();

	/**
	 * Custom reason to provide if the test is disabled.
	 */
	String disabledReason() default "";

}
