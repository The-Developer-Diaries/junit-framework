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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link ExecutionCondition} for {@link EnabledIfPort @EnabledIfPort} and
 * {@link DisabledIfPort @DisabledIfPort}.
 *
 * @since 6.1
 */
@API(status = API.Status.INTERNAL)
public class PortAvailableCondition implements ExecutionCondition {

	public PortAvailableCondition() {

	}

	private static final ConditionEvaluationResult ENABLED_NO_ANNOTATION = enabled(
		"@EnabledIfPort or @DisabledIfPort is not present");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		var enabledAnnotation = findAnnotation(context.getElement(), EnabledIfPort.class);
		var disabledAnnotation = findAnnotation(context.getElement(), DisabledIfPort.class);

		if (enabledAnnotation.isEmpty() && disabledAnnotation.isEmpty()) {
			return ENABLED_NO_ANNOTATION;
		}

		if (enabledAnnotation.isPresent()) {
			EnabledIfPort ann = enabledAnnotation.get();
			boolean reachable = isPortReachable(ann.host(), ann.port(), ann.timeoutMs());
			if (reachable) {
				return enabled("Port %s:%d is reachable".formatted(ann.host(), ann.port()));
			}
			return disabled("Port %s:%d is not reachable".formatted(ann.host(), ann.port()), ann.disabledReason());
		}

		// disabledAnnotation.isPresent()
		DisabledIfPort ann = disabledAnnotation.get();
		boolean reachable = isPortReachable(ann.host(), ann.port(), ann.timeoutMs());
		if (reachable) {
			return disabled("Port %s:%d is reachable".formatted(ann.host(), ann.port()), ann.disabledReason());
		}
		return enabled("Port %s:%d is not reachable".formatted(ann.host(), ann.port()));
	}

	boolean isPortReachable(String host, int port, int timeoutMs) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), timeoutMs);
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

}
