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
 * {@code @EnabledOnCI} is used to signal that the annotated test class or
 * test method is only <em>enabled</em> when running in a Continuous
 * Integration (CI) environment.
 *
 * <p>CI detection is based on the presence of common CI environment variables
 * such as {@code CI}, {@code CONTINUOUS_INTEGRATION}, {@code BUILD_NUMBER},
 * {@code JENKINS_URL}, {@code GITHUB_ACTIONS}, {@code GITLAB_CI},
 * {@code TRAVIS}, {@code CIRCLECI}, and {@code BITBUCKET_BUILD_NUMBER}.
 *
 * <p>This is useful for tests that should only run in CI (e.g., expensive
 * integration tests, deployment smoke tests) and be skipped during local
 * development.
 *
 * @since 6.1
 * @see DisabledOnCI
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(CICondition.class)
@API(status = EXPERIMENTAL, since = "6.1")
public @interface EnabledOnCI {

	/**
	 * Custom reason to provide if the test is disabled.
	 */
	String disabledReason() default "";

}
