package edu.usfca.cs272;

import java.nio.file.Path;
import java.util.*;

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
	private final HashMap<String,WordObj> wIndex; /* Maybe i could use a hashset if I could do a custom hash func for just the word*/

	/**
	 * A data structure that holds a word along with where it was found
	 */
	private class WordObj implements Comparable<String>{
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
	}

	/**
	 * Data structure to hold the file locations and line locations of a word.
	 * Might not be needed. Just use HashMap<Path,ArrayList<Integer>> locations;
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
		this.wIndex = new HashMap<>();
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




	/**
	 * Demonstrates this class.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
	}
}
