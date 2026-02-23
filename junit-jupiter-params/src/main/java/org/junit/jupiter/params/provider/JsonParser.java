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
 * Lightweight, dependency-free JSON parser for use in {@link JsonFileArgumentsProvider}.
 *
 * <p>This parser supports the full JSON specification (RFC 8259) including
 * objects, arrays, strings (with escape sequences), numbers (integers and
 * floating-point), booleans, and null values.
 *
 * <p>Parsed values are represented as:
 * <ul>
 * <li>JSON objects &rarr; {@code LinkedHashMap<String, Object>} (preserving insertion order)</li>
 * <li>JSON arrays &rarr; {@code List<Object>}</li>
 * <li>JSON strings &rarr; {@code String}</li>
 * <li>JSON numbers &rarr; {@code Long} or {@code Double}</li>
 * <li>JSON booleans &rarr; {@code Boolean}</li>
 * <li>JSON null &rarr; {@code null}</li>
 * </ul>
 *
 * @since 6.1
 */
final class JsonParser {

	private final String input;
	private int pos;

	private JsonParser(String input) {
		this.input = input;
		this.pos = 0;
	}

	/**
	 * Parse the given JSON string and return the parsed value.
	 *
	 * @param json the JSON string to parse; must not be {@code null}
	 * @return the parsed value
	 * @throws JUnitException if the JSON is malformed
	 */
	static @Nullable Object parse(String json) {
		JsonParser parser = new JsonParser(json);
		Object value = parser.parseValue();
		parser.skipWhitespace();
		if (parser.pos < parser.input.length()) {
			throw parser.error("Unexpected content after JSON value");
		}
		return value;
	}

	private @Nullable Object parseValue() {
		skipWhitespace();
		if (pos >= input.length()) {
			throw error("Unexpected end of input");
		}
		char c = input.charAt(pos);
		return switch (c) {
			case '"' -> parseString();
			case '{' -> parseObject();
			case '[' -> parseArray();
			case 't', 'f' -> parseBoolean();
			case 'n' -> parseNull();
			default -> {
				if (c == '-' || (c >= '0' && c <= '9')) {
					yield parseNumber();
				}
				throw error("Unexpected character: '" + c + "'");
			}
		};
	}

	private String parseString() {
		expect('"');
		StringBuilder sb = new StringBuilder();
		while (pos < input.length()) {
			char c = input.charAt(pos++);
			if (c == '"') {
				return sb.toString();
			}
			if (c == '\\') {
				if (pos >= input.length()) {
					throw error("Unexpected end of input in string escape");
				}
				char escaped = input.charAt(pos++);
				switch (escaped) {
					case '"' -> sb.append('"');
					case '\\' -> sb.append('\\');
					case '/' -> sb.append('/');
					case 'b' -> sb.append('\b');
					case 'f' -> sb.append('\f');
					case 'n' -> sb.append('\n');
					case 'r' -> sb.append('\r');
					case 't' -> sb.append('\t');
					case 'u' -> {
						if (pos + 4 > input.length()) {
							throw error("Unexpected end of input in unicode escape");
						}
						String hex = input.substring(pos, pos + 4);
						try {
							sb.append((char) Integer.parseInt(hex, 16));
						}
						catch (NumberFormatException e) {
							throw error("Invalid unicode escape: \\u" + hex);
						}
						pos += 4;
					}
					default -> throw error("Invalid escape character: '\\" + escaped + "'");
				}
			}
			else {
				sb.append(c);
			}
		}
		throw error("Unterminated string");
	}

	private Map<String, @Nullable Object> parseObject() {
		expect('{');
		Map<String, @Nullable Object> map = new LinkedHashMap<>();
		skipWhitespace();
		if (pos < input.length() && input.charAt(pos) == '}') {
			pos++;
			return map;
		}
		while (true) {
			skipWhitespace();
			String key = parseString();
			skipWhitespace();
			expect(':');
			Object value = parseValue();
			map.put(key, value);
			skipWhitespace();
			if (pos >= input.length()) {
				throw error("Unterminated object");
			}
			char c = input.charAt(pos);
			if (c == '}') {
				pos++;
				return map;
			}
			if (c == ',') {
				pos++;
			}
			else {
				throw error("Expected ',' or '}' in object, got '" + c + "'");
			}
		}
	}

	private List<@Nullable Object> parseArray() {
		expect('[');
		List<@Nullable Object> list = new ArrayList<>();
		skipWhitespace();
		if (pos < input.length() && input.charAt(pos) == ']') {
			pos++;
			return list;
		}
		while (true) {
			list.add(parseValue());
			skipWhitespace();
			if (pos >= input.length()) {
				throw error("Unterminated array");
			}
			char c = input.charAt(pos);
			if (c == ']') {
				pos++;
				return list;
			}
			if (c == ',') {
				pos++;
			}
			else {
				throw error("Expected ',' or ']' in array, got '" + c + "'");
			}
		}
	}

	private Boolean parseBoolean() {
		if (input.startsWith("true", pos)) {
			pos += 4;
			return Boolean.TRUE;
		}
		if (input.startsWith("false", pos)) {
			pos += 5;
			return Boolean.FALSE;
		}
		throw error("Expected 'true' or 'false'");
	}

	private @Nullable Object parseNull() {
		if (input.startsWith("null", pos)) {
			pos += 4;
			return null;
		}
		throw error("Expected 'null'");
	}

	private Number parseNumber() {
		int start = pos;
		if (pos < input.length() && input.charAt(pos) == '-') {
			pos++;
		}
		if (pos >= input.length()) {
			throw error("Unexpected end of input in number");
		}
		if (input.charAt(pos) == '0') {
			pos++;
		}
		else if (input.charAt(pos) >= '1' && input.charAt(pos) <= '9') {
			pos++;
			while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') {
				pos++;
			}
		}
		else {
			throw error("Invalid number");
		}
		boolean isFloatingPoint = false;
		if (pos < input.length() && input.charAt(pos) == '.') {
			isFloatingPoint = true;
			pos++;
			if (pos >= input.length() || input.charAt(pos) < '0' || input.charAt(pos) > '9') {
				throw error("Invalid number: expected digit after decimal point");
			}
			while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') {
				pos++;
			}
		}
		if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
			isFloatingPoint = true;
			pos++;
			if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-')) {
				pos++;
			}
			if (pos >= input.length() || input.charAt(pos) < '0' || input.charAt(pos) > '9') {
				throw error("Invalid number: expected digit in exponent");
			}
			while (pos < input.length() && input.charAt(pos) >= '0' && input.charAt(pos) <= '9') {
				pos++;
			}
		}
		String numberStr = input.substring(start, pos);
		try {
			if (isFloatingPoint) {
				return Double.parseDouble(numberStr);
			}
			// Try Long first, fall back to Double for very large numbers
			try {
				return Long.parseLong(numberStr);
			}
			catch (NumberFormatException e) {
				return Double.parseDouble(numberStr);
			}
		}
		catch (NumberFormatException e) {
			throw error("Invalid number: " + numberStr);
		}
	}

	private void skipWhitespace() {
		while (pos < input.length()) {
			char c = input.charAt(pos);
			if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
				pos++;
			}
			else {
				break;
			}
		}
	}

	private void expect(char expected) {
		skipWhitespace();
		if (pos >= input.length()) {
			throw error("Expected '" + expected + "' but reached end of input");
		}
		if (input.charAt(pos) != expected) {
			throw error("Expected '" + expected + "' but got '" + input.charAt(pos) + "'");
		}
		pos++;
	}

	private JUnitException error(String message) {
		return new JUnitException("JSON parse error at position " + pos + ": " + message);
	}

}
