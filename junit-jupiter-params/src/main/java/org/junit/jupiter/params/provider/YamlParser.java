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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.junit.platform.commons.JUnitException;

/**
 * Lightweight, dependency-free YAML parser for use in {@link YamlFileArgumentsProvider}.
 *
 * <p>This parser supports a practical subset of YAML suitable for test data:
 * <ul>
 * <li>Block sequences (lists) using {@code -} indicators</li>
 * <li>Block mappings (key-value pairs) using {@code key: value} syntax</li>
 * <li>Flow sequences using {@code [value1, value2]} syntax</li>
 * <li>Scalar types: strings (quoted and unquoted), integers, floating-point
 * numbers, booleans ({@code true}/{@code false}), and null ({@code null}/{@code ~})</li>
 * <li>Comments starting with {@code #}</li>
 * </ul>
 *
 * <p>Unsupported features: anchors/aliases, multi-line block scalars ({@code |}
 * and {@code >}), tags, multi-document streams, complex mapping keys, and flow
 * mappings.
 *
 * @since 6.1
 */
final class YamlParser {

	private final String[] lines;
	private int lineIndex;

	private YamlParser(String input) {
		this.lines = input.split("\r?\n", -1);
		this.lineIndex = 0;
	}

	/**
	 * Parse the given YAML string and return the parsed top-level structure.
	 *
	 * <p>The result is expected to be a {@code List} (YAML sequence) for use
	 * with {@link YamlFileArgumentsProvider}.
	 *
	 * @param yaml the YAML string to parse; must not be {@code null}
	 * @return the parsed value (a List, Map, or scalar)
	 * @throws JUnitException if the YAML is malformed or uses unsupported features
	 */
	static @Nullable Object parse(String yaml) {
		YamlParser parser = new YamlParser(yaml);
		return parser.parseDocument();
	}

	private @Nullable Object parseDocument() {
		skipEmptyAndCommentLines();
		if (lineIndex >= lines.length) {
			return new ArrayList<>();
		}
		String line = lines[lineIndex];
		String trimmed = line.stripLeading();
		if (trimmed.startsWith("- ") || trimmed.equals("-")) {
			return parseBlockSequence(indentOf(line));
		}
		if (trimmed.contains(":")) {
			return parseBlockMapping(indentOf(line));
		}
		return parseScalar(trimmed);
	}

	private List<@Nullable Object> parseBlockSequence(int baseIndent) {
		List<@Nullable Object> list = new ArrayList<>();
		while (lineIndex < lines.length) {
			skipEmptyAndCommentLines();
			if (lineIndex >= lines.length) {
				break;
			}
			String line = lines[lineIndex];
			int indent = indentOf(line);
			if (indent < baseIndent) {
				break;
			}
			String trimmed = line.stripLeading();
			if (!trimmed.startsWith("-")) {
				break;
			}
			// Remove the "- " prefix and process the value
			String afterDash = trimmed.substring(1).stripLeading();
			if (afterDash.isEmpty()) {
				// "- " on its own line: the value is on subsequent indented lines
				lineIndex++;
				skipEmptyAndCommentLines();
				if (lineIndex < lines.length) {
					String nextLine = lines[lineIndex];
					int nextIndent = indentOf(nextLine);
					if (nextIndent > indent) {
						String nextTrimmed = nextLine.stripLeading();
						if (nextTrimmed.startsWith("- ") || nextTrimmed.equals("-")) {
							list.add(parseBlockSequence(nextIndent));
						}
						else if (nextTrimmed.contains(":")) {
							list.add(parseBlockMapping(nextIndent));
						}
						else {
							list.add(parseScalar(nextTrimmed));
							lineIndex++;
						}
					}
					else {
						list.add(null);
					}
				}
				else {
					list.add(null);
				}
			}
			else if (afterDash.startsWith("[")) {
				// Flow sequence: - [value1, value2]
				list.add(parseFlowSequence(afterDash));
				lineIndex++;
			}
			else if (afterDash.contains(":") && !isQuotedString(afterDash)) {
				// Inline mapping start: - key: value
				// This is a mapping that starts on the dash line
				Map<String, @Nullable Object> mapping = new LinkedHashMap<>();
				parseKeyValueInto(mapping, afterDash);
				lineIndex++;
				// Parse additional mapping entries at deeper indentation
				int mappingIndent = indent + 2;
				while (lineIndex < lines.length) {
					skipEmptyAndCommentLines();
					if (lineIndex >= lines.length) {
						break;
					}
					String nextLine = lines[lineIndex];
					int nextIndent = indentOf(nextLine);
					if (nextIndent < mappingIndent) {
						break;
					}
					String nextTrimmed = nextLine.stripLeading();
					if (nextTrimmed.startsWith("- ")) {
						break;
					}
					if (nextTrimmed.contains(":") && !isQuotedString(nextTrimmed)) {
						parseKeyValueInto(mapping, nextTrimmed);
						lineIndex++;
					}
					else {
						break;
					}
				}
				list.add(mapping);
			}
			else {
				// Simple scalar value: - value
				list.add(parseScalar(afterDash));
				lineIndex++;
			}
		}
		return list;
	}

	private Map<String, @Nullable Object> parseBlockMapping(int baseIndent) {
		Map<String, @Nullable Object> map = new LinkedHashMap<>();
		while (lineIndex < lines.length) {
			skipEmptyAndCommentLines();
			if (lineIndex >= lines.length) {
				break;
			}
			String line = lines[lineIndex];
			int indent = indentOf(line);
			if (indent < baseIndent) {
				break;
			}
			String trimmed = line.stripLeading();
			if (trimmed.startsWith("- ")) {
				break;
			}
			if (trimmed.contains(":") && !isQuotedString(trimmed)) {
				parseKeyValueInto(map, trimmed);
				lineIndex++;
			}
			else {
				break;
			}
		}
		return map;
	}

	private void parseKeyValueInto(Map<String, @Nullable Object> map, String line) {
		int colonIndex = findUnquotedColon(line);
		if (colonIndex < 0) {
			throw error("Expected 'key: value' pair but got: " + line);
		}
		String key = line.substring(0, colonIndex).strip();
		// Remove surrounding quotes from key if present
		key = unquoteIfNeeded(key);
		String rawValue = line.substring(colonIndex + 1).stripLeading();
		// Strip inline comment
		rawValue = stripInlineComment(rawValue);
		map.put(key, parseScalar(rawValue));
	}

	private List<@Nullable Object> parseFlowSequence(String text) {
		String content = text.strip();
		if (!content.startsWith("[") || !content.endsWith("]")) {
			throw error("Invalid flow sequence: " + text);
		}
		content = content.substring(1, content.length() - 1).strip();
		if (content.isEmpty()) {
			return new ArrayList<>();
		}
		List<@Nullable Object> list = new ArrayList<>();
		for (String element : splitFlowElements(content)) {
			list.add(parseScalar(element.strip()));
		}
		return list;
	}

	private static List<String> splitFlowElements(String content) {
		List<String> elements = new ArrayList<>();
		int depth = 0;
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		int start = 0;
		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
			}
			else if (c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
			}
			else if (!inSingleQuote && !inDoubleQuote) {
				if (c == '[') {
					depth++;
				}
				else if (c == ']') {
					depth--;
				}
				else if (c == ',' && depth == 0) {
					elements.add(content.substring(start, i));
					start = i + 1;
				}
			}
		}
		elements.add(content.substring(start));
		return elements;
	}

	static @Nullable Object parseScalar(String value) {
		if (value.isEmpty() || "null".equals(value) || "~".equals(value)) {
			return null;
		}
		if ("true".equals(value)) {
			return Boolean.TRUE;
		}
		if ("false".equals(value)) {
			return Boolean.FALSE;
		}
		// Handle quoted strings
		if ((value.startsWith("\"") && value.endsWith("\""))
				|| (value.startsWith("'") && value.endsWith("'"))) {
			return value.substring(1, value.length() - 1);
		}
		// Try integer
		try {
			return Long.parseLong(value);
		}
		catch (NumberFormatException ignored) {
			// not an integer
		}
		// Try floating-point
		try {
			if (value.contains(".") || value.contains("e") || value.contains("E")) {
				return Double.parseDouble(value);
			}
		}
		catch (NumberFormatException ignored) {
			// not a number
		}
		// Unquoted string
		return value;
	}

	private static int findUnquotedColon(String line) {
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
			}
			else if (c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
			}
			else if (c == ':' && !inSingleQuote && !inDoubleQuote) {
				// Ensure it's a mapping separator (followed by space, end, or newline)
				if (i + 1 >= line.length() || line.charAt(i + 1) == ' ' || line.charAt(i + 1) == '\t') {
					return i;
				}
			}
		}
		return -1;
	}

	private static boolean isQuotedString(String text) {
		String stripped = text.strip();
		return (stripped.startsWith("\"") && stripped.endsWith("\""))
				|| (stripped.startsWith("'") && stripped.endsWith("'"));
	}

	private static String unquoteIfNeeded(String value) {
		if ((value.startsWith("\"") && value.endsWith("\""))
				|| (value.startsWith("'") && value.endsWith("'"))) {
			return value.substring(1, value.length() - 1);
		}
		return value;
	}

	private static String stripInlineComment(String value) {
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
			}
			else if (c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
			}
			else if (c == '#' && !inSingleQuote && !inDoubleQuote) {
				return value.substring(0, i).stripTrailing();
			}
		}
		return value;
	}

	private void skipEmptyAndCommentLines() {
		while (lineIndex < lines.length) {
			String trimmed = lines[lineIndex].stripLeading();
			if (trimmed.isEmpty() || trimmed.startsWith("#")) {
				lineIndex++;
			}
			else {
				break;
			}
		}
	}

	private static int indentOf(String line) {
		int count = 0;
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == ' ') {
				count++;
			}
			else {
				break;
			}
		}
		return count;
	}

	private JUnitException error(String message) {
		return new JUnitException("YAML parse error at line " + (lineIndex + 1) + ": " + message);
	}

}
