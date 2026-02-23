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
 * {@link ArgumentsProvider} for {@link JsonFileSource @JsonFileSource}.
 *
 * <p>Loads JSON files from classpath resources or filesystem paths, parses
 * the top-level JSON array, and converts each element to {@link Arguments}.
 *
 * @since 6.1
 */
class JsonFileArgumentsProvider extends AnnotationBasedArgumentsProvider<JsonFileSource> {

	@Override
	protected Stream<? extends Arguments> provideArguments(ParameterDeclarations parameters, ExtensionContext context,
			JsonFileSource jsonFileSource) {

		Charset charset = getCharsetFrom(jsonFileSource);

		Stream<String> resources = Arrays.stream(jsonFileSource.resources())
				.map(resource -> readClasspathResource(context.getRequiredTestClass(), resource, charset));
		Stream<String> files = Arrays.stream(jsonFileSource.files())
				.map(file -> readFile(file, charset));

		List<String> sources = Stream.concat(resources, files).toList();
		Preconditions.notEmpty(sources, "Resources or files must not be empty");

		return sources.stream().flatMap(JsonFileArgumentsProvider::parseJsonSource);
	}

	private static Charset getCharsetFrom(JsonFileSource jsonFileSource) {
		try {
			return Charset.forName(jsonFileSource.encoding());
		}
		catch (Exception ex) {
			throw new PreconditionViolationException(
				"The charset supplied in @JsonFileSource is invalid: " + jsonFileSource.encoding(), ex);
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

	private static Stream<Arguments> parseJsonSource(String jsonContent) {
		Object parsed = JsonParser.parse(jsonContent);
		if (!(parsed instanceof List<?> array)) {
			throw new JUnitException(
				"@JsonFileSource requires a top-level JSON array, but found: "
						+ (parsed == null ? "null" : parsed.getClass().getSimpleName()));
		}
		return array.stream().map(JsonFileArgumentsProvider::toArguments);
	}

	@SuppressWarnings("unchecked")
	private static Arguments toArguments(@Nullable Object element) {
		if (element instanceof Map<?, ?> map) {
			// JSON object: use the values in insertion order as arguments
			return Arguments.of(((Map<String, ?>) map).values().toArray());
		}
		if (element instanceof List<?> list) {
			// JSON array: use the elements as arguments
			return Arguments.of(list.toArray());
		}
		// JSON primitive (string, number, boolean, null): single argument
		return Arguments.of(element);
	}

}
