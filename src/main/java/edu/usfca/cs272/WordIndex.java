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
	 * A data structure that holds a word along with where it was found
	 */
	private class WordObj {
		private final String word;
		private final HashMap<Path,LocationsObj> locations;

		private WordObj(String word) {
			this.word = word;
			this.locations = new HashMap<>();
		}

		private WordObj(String word, Path location) {
			this.word = word;
			this.locations = new HashMap<>();
			this.locations.put(location,new LocationsObj(location));
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
	 ** Nested HashMap and HashSet data structure to store the locations and words
	 */
	private final HashMap<String,WordObj> wIndex; /* Maybe i could use a hashset if I could do a custom hash func for just the word*/

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

	@Override
	public int size() {
		return wIndex.size();
	}

	@Override
	public int size(Path location) {
		return wIndex.getOrDefault(location,new HashSet<>()).size();
	}

	@Override
	public boolean has(Path location) {
		return fIndex.containsKey(location);
	}

	@Override
	public boolean has(Path location, String word) {
		if (this.has(location)){
			return fIndex.get(location).contains(word);
		}
		return false;
	}

	@Override
	public Collection<Path> view() {
		return Collections.unmodifiableCollection(fIndex.keySet());
	}

	@Override
	public Collection<String> view(Path location) {
		return Collections.unmodifiableCollection(fIndex.getOrDefault(location,new HashSet<>()));
	}

	/**
	 ** Basic toString generated by IntelliJ
	 * @return A string containing the content of the WordIndex.
	 */
	@Override
	public String toString() {
		return "WordIndex{" +
				"fIndex=" + fIndex +
				'}';
	}

	/**
	 * Demonstrates this class.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		/*
		 * IF YOU ARE SEEING COMPILE ERRORS... it is likely you have not yet
		 * properly implemented the interface!
		 */


		Path hello = Path.of("hello.txt");
		Path world = Path.of("world.txt");
		ForwardIndex<Path> index = new WordIndex();

		index.add(hello, List.of("hello", "hola", "aloha", "ciao"));
		index.add(world, List.of("earth", "mars", "venus", "pluto"));

		System.out.println(index.view());
		System.out.println(index.view(hello));
		System.out.println(index.view(world));
		System.out.println(index);
	}
}