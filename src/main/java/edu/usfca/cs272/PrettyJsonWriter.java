package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class PrettyJsonWriter {
	/**
	 * System independent newline.
	 */
	final static String newline = System.getProperty("line.separator");
	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "}
	 * quotation marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements,
			Writer writer, int indent) throws IOException {
		writer.write("[");
		Iterator<? extends Number> iterator = elements.iterator();
		while (iterator.hasNext()){
			Number element = iterator.next();
			writer.write(newline);
			writeIndent(writer,indent+1);
			writer.write(element.toString());
			if (iterator.hasNext()){writer.write(",");}
		}
		writer.write(newline);
		writeIndent(writer,indent);
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements,
			Writer writer, int indent) throws IOException {
		writer.write("{");
		var iterator = elements.entrySet().iterator();
		while (iterator.hasNext()){
			Map.Entry<String, ? extends Number> element = iterator.next();
			writer.write(newline);
			writeIndent(writer, indent+1);
			writeQuote(element.getKey(),writer,0);writer.write(": ");
			writer.write(element.getValue().toString()); /*writer u better be writing*/
			if (iterator.hasNext()){writer.write(",");}
		}
		writer.write(newline);
		writeIndent(writer,indent);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any
	 * type of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeNestedArrays(
			Map<String, ? extends Collection<? extends Number>> elements,
			Writer writer, int indent) throws IOException {
		writer.write("{");
		var iterator = elements.entrySet().iterator();
		while (iterator.hasNext()){ /* Iterator lifestyle 8^) */
			var collection = iterator.next();
			writer.write(newline);
			writeIndent(writer, indent+1);
			writeQuote(collection.getKey(),writer,0);writer.write(": ");
			writeArray(collection.getValue(), writer, indent+1);
			if (iterator.hasNext()){writer.write(",");}
		}
		writer.write(newline);
		writeIndent(writer,indent);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeNestedArrays(Map, Writer, int)
	 */
	public static void writeNestedArrays(
			Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeNestedArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeNestedArrays(Map, Writer, int)
	 */
	public static String writeNestedArrays(
			Map<String, ? extends Collection<? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeNestedArrays(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeNestedObjects(
			Collection<? extends Map<String, ? extends Number>> elements,
			Writer writer, int indent) throws IOException {
		writer.write("[");
		var iterator = elements.iterator();
		while (iterator.hasNext()){ /* Iterator lifestyle 8^) */
			var object = iterator.next();
			writer.write(newline);
			writeIndent(writer, indent+1);
			writeObject(object,writer,indent+1);
			if (iterator.hasNext()){writer.write(",");}
		}
		writer.write(newline);
		writeIndent(writer,indent);
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeNestedObjects(Collection)
	 */
	public static void writeNestedObjects(
			Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeNestedObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeNestedObjects(Collection)
	 */
	public static String writeNestedObjects(
			Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeNestedObjects(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}



	/**
	 * Demonstrates this class.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		Set<Integer> empty = Collections.emptySet();
		Set<Integer> single = Set.of(42);
		List<Integer> simple = List.of(65, 66, 67);

		System.out.println("\nArrays:");
		System.out.println(writeArray(empty));
		System.out.println(writeArray(single));
		System.out.println(writeArray(simple));

		System.out.println("\nObjects:");
		System.out.println(writeObject(Collections.emptyMap()));
		System.out.println(writeObject(Map.of("hello", 42)));
		System.out.println(writeObject(Map.of("hello", 42, "world", 67)));

		System.out.println("\nNested Arrays:");
		System.out.println(writeNestedArrays(Collections.emptyMap()));
		System.out.println(writeNestedArrays(Map.of("hello", single)));
		System.out.println(writeNestedArrays(Map.of("hello", single, "world", simple)));

		System.out.println("\nNested Objects:");
		System.out.println(writeNestedObjects(Collections.emptyList()));
		System.out.println(writeNestedObjects(Set.of(Map.of("hello", 3.12))));
		System.out.println(writeNestedObjects(Set.of(Map.of("hello", 3.12, "world", 2.04), Map.of("apple", 0.04))));
	}
}