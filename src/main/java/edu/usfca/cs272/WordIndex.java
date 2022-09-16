package edu.usfca.cs272;

import java.io.IOException;
import java.io.StringWriter;
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
		private final String word;
		private final HashMap<Path,LocationsObj> locations;

		private WordObj() {
			this.word = null;
			this.locations = new HashMap<>();
		}

		private WordObj(String word, Path location) {
			this.word = word;
			this.locations = new HashMap<>();
			this.locations.put(location,new LocationsObj(location));
		}

		@Override
		public int compareTo(String o) {
			return this.word.compareTo(o);
		}
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
		private final Path path;
		public final ArrayList<Integer> lineLocations;

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

	public void add(String word, Path location, Integer line) {
		wIndex.putIfAbsent(word,new WordObj(word,location));
		wIndex.get(word).locations.get(location).lineLocations.add(line); //so many dots o_o TODO: simplify
	}


	public int size() {
		return wIndex.size();
	}


	public int size(Path location) {
		return wIndex.getOrDefault(location,new WordObj()).locations.size();
	}

	public boolean has(String word) {
		return wIndex.containsKey(word);
	}



	public Collection<WordObj> view() {
		return Collections.unmodifiableCollection(wIndex.values());
	}

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
