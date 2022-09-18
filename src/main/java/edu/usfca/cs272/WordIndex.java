package edu.usfca.cs272;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;

import static edu.usfca.cs272.PrettyJsonWriter.newline;
import static edu.usfca.cs272.PrettyJsonWriter.*;

/**
 * An program that indexes the UNIQUE words that
 * were found in a text file (represented by {@link Path} objects) and stores where they
 * were found.
 *
 * @author TJ de Laveaga
 * @version Fall 2022
 */

public class WordIndex {

	/**
	 ** Nested HashMap and HashSet data structure to store the locations and words
	 */
	private final TreeMap<String,WordObj> wIndex; /* Maybe i could use a hashset if I could do a custom hash func for just the word*/

	/**
	 * A data structure that holds a word along with where it was found
	 */
	public class WordObj implements Comparable<String>{
		/**
		 * Variable that stores the word this WordObj represents. It is the same as the Key in wIndex.
		 * So this String probably just points to that and doesn't create a new instance?
		 */
		private final String word;

		/**
		 * Alphabetically sorted list of locations this word was found in.
		 */
		private final TreeMap<Path,LocationsObj> locations;

		/**
		 * Constructs a new instance of this class.
		 */
		private WordObj() {
			this.word = null;
			this.locations = new TreeMap<>();
		}

		/**
		 * Constucts a new instance of this class with a word and adds the first location into the TreeMap
		 * @param word the word this WordObj represents
		 * @param location the location the word was found in
		 */
		private WordObj(String word, Path location) {
			this.word = word;
			this.locations = new TreeMap<>();
			this.locations.put(location,new LocationsObj(location));
		}

		@Override
		public int compareTo(String o) {
			return this.word.compareTo(o);
		}

		/**
		 * Uses the help of PrettyJSONWriter to convert a wordObj to JSON
		 * @param writer the {@link Writer} to use
		 * @param indent the level of indentation to use
		 * @throws IOException if the Writer throws an IOException
		 */
		public void toJSON(Writer writer, int indent) throws IOException {
			writer.write("{");
			var iterator = locations.entrySet().iterator();
			while (iterator.hasNext()){
				Map.Entry<Path,LocationsObj> locObjEntry = iterator.next();
				writer.write(newline);
				writeIndent(writer,indent+1);
				writeQuote(locObjEntry.getKey().toString(),writer,0);writer.write(": ");
				writeArray(locObjEntry.getValue().lineLocations,writer, indent+1); //locationsObj.toJSON
				if (iterator.hasNext()){writer.write(",");}
			}
			writer.write(newline);
			writeIndent(writer,indent);
			writer.write("}");
		}
	}

	/**
	 * Data structure to hold the file locations and line locations of a word.
	 */
	private class LocationsObj {
		/**
		 * the Path that this LocationsObj represents. It is the same as the Key of the TreeMap this obj is stored in.
		 */
		private final Path path;

		/**
		 * All the lines that the word was found at.
		 */
		public final ArrayList<Integer> lineLocations;

		/**
		 * Constructs a new LocationsObj and sets the path
		 * @param path the path that this LocationsObj represents
		 */
		public LocationsObj(Path path) {
			this.path = path;
			this.lineLocations = new ArrayList<>();
		}
	}

	/**
	 ** Constructs a new instance of WordIndex.
	 */
	public WordIndex(){
		this.wIndex = new TreeMap<>();
	}

	/**
	 * Adds a new word to the WordIndex. Given the word, it's Path location, and the line number it was found at.
	 * @param word the word to add
	 * @param location where the wod was found
	 * @param line what line the word was found at
	 */
	public void add(String word, Path location, Integer line) {
		wIndex.putIfAbsent(word,new WordObj(word,location));
		wIndex.get(word).locations.putIfAbsent(location,new LocationsObj(location));
		wIndex.get(word).locations.get(location).lineLocations.add(line); //so many dots o_o
	}

	/**
	 * Uses {@link WordObj} toJSON method to convert a wordIndex to JSON.
	 * @param writer the {@link Writer} to use
	 * @param indent the level of indentation.
	 * @throws IOException if the writer throws and IOException
	 */
	public void toJSON(Writer writer, int indent) throws IOException {
		writer.write("{");
		var iterator = wIndex.entrySet().iterator();
		while (iterator.hasNext()){
			Map.Entry<String, WordObj> wordObjEntry = iterator.next();
			writer.write(newline);
			writeIndent(writer,indent+1);
			writeQuote(wordObjEntry.getKey().toString(),writer,0);writer.write(": ");
			wordObjEntry.getValue().toJSON(writer, indent+1); //locationsObj.toJSON
			if (iterator.hasNext()){writer.write(",");}
		}
		writer.write(newline);
		writeIndent(writer,indent);
		writer.write("}");
	}

	/**
	 * Demonstrates this class.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
	}
}
