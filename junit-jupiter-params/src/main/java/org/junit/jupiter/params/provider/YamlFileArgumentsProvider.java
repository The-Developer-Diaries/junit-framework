/*
 * Copyright 2015-2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ArgumentsProvider} for {@link YamlFileSource @YamlFileSource}.
 *
 * <p>Loads YAML files from classpath resources or filesystem paths, parses
 * the top-level YAML sequence, and converts each element to {@link Arguments}.
 *
 * @since 6.1
 */
class YamlFileArgumentsProvider extends AnnotationBasedArgumentsProvider<YamlFileSource> {

	@Override
	protected Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context,
			YamlFileSource yamlFileSource) {

		Charset charset = getCharsetFrom(yamlFileSource);

		Stream<String> resources = Arrays.stream(yamlFileSource.resources()).map(
			resource -> readClasspathResource(context.getRequiredTestClass(), resource, charset));
		Stream<String> files = Arrays.stream(yamlFileSource.files()).map(file -> readFile(file, charset));

		List<String> sources = Stream.concat(resources, files).toList();
		Preconditions.notEmpty(sources, "Resources or files must not be empty");

		return sources.stream().flatMap(YamlFileArgumentsProvider::parseYamlSource);
	}

	private static Charset getCharsetFrom(YamlFileSource yamlFileSource) {
		try {
			return Charset.forName(yamlFileSource.encoding());
		}
		catch (Exception ex) {
			throw new PreconditionViolationException(
				"The charset supplied in @YamlFileSource is invalid: " + yamlFileSource.encoding(), ex);
		}
	}

	private static String readClasspathResource(Class<?> testClass, String resource, Charset charset) {
		Preconditions.notBlank(resource, () -> "Classpath resource [" + resource + "] must not be null or blank");
		try (InputStream inputStream = testClass.getResourceAsStream(resource)) {
			Preconditions.notNull(inputStream, () -> "Classpath resource [" + resource + "] does not exist");
			return new String(inputStream.readAllBytes(), charset);
		}
		catch (IOException e) {
			throw new JUnitException("Failed to read classpath resource [" + resource + "]", e);
		}
	}

	private static String readFile(String path, Charset charset) {
		Preconditions.notBlank(path, () -> "File [" + path + "] must not be null or blank");
		try {
			return Files.readString(Path.of(path), charset);
		}
		catch (IOException e) {
			throw new JUnitException("Failed to read file [" + path + "]", e);
		}
	}

	private static Stream<Arguments> parseYamlSource(String yamlContent) {
		Object parsed = YamlParser.parse(yamlContent);
		if (!(parsed instanceof List<?> sequence)) {
			throw new JUnitException("@YamlFileSource requires a top-level YAML sequence, but found: "
					+ (parsed == null ? "null" : parsed.getClass().getSimpleName()));
		}
		return sequence.stream().map(YamlFileArgumentsProvider::toArguments);
	}

	@SuppressWarnings("unchecked")
	private static Arguments toArguments(@Nullable Object element) {
		if (element instanceof Map<?, ?> map) {
			// YAML mapping: use the values in insertion order as arguments
			return Arguments.of(((Map<String, ?>) map).values().toArray());
		}
		if (element instanceof List<?> list) {
			// YAML sequence: use the elements as arguments
			return Arguments.of(list.toArray());
		}
		// YAML scalar (string, number, boolean, null): single argument
		return Arguments.of(element);
	}

}
