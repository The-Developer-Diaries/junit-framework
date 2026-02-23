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
 * {@code @EnabledIfGitTag} is used to signal that the annotated test class or
 * test method is only <em>enabled</em> if the current Git commit is tagged.
 *
 * <p>This is useful for tests that should only run during release builds
 * (e.g., deployment verification, release smoke tests).
 *
 * <p>The tag is determined by running {@code git describe --tags --exact-match HEAD}.
 *
 * <h2>Example Usage</h2>
 *
 * <pre>{@code
 * @EnabledIfGitTag
 * @Test
 * void releaseVerification() { ... }
 * }</pre>
 *
 * @since 6.1
 * @see DisabledIfGitTag
 * @see EnabledIfGitBranch
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(GitTagCondition.class)
@API(status = EXPERIMENTAL, since = "6.1")
public @interface EnabledIfGitTag {

	/**
	 * Custom reason to provide if the test is disabled.
	 */
	String disabledReason() default "";

}
