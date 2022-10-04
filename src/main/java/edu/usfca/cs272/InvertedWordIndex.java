package edu.usfca.cs272;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;

import static edu.usfca.cs272.PrettyJsonWriter.*;


/**
 * A program that indexes the UNIQUE words that
 * were found in a text file (represented by {@link Path} objects) and stores where they
 * were found.
 *
 * @author TJ de Laveaga
 * @version Fall 2022
 */

public class InvertedWordIndex {


    /**
     * * Nested data structure to store words, what file they were found in, and the line locations.
     */
    private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> wordMap;

    /**
     * * Constructs a new instance of WordIndex.
     */
    public InvertedWordIndex() {
        this.wordMap = new TreeMap<>();
    }

    /**
     * Constructs a new nested TreeMap that stores file locations and puts the first location in.
     *
     * @param location the first location to put into the map
     * @return a nested TreeMap and TreeSet containing the location provided.
     */
    public TreeMap<String, TreeSet<Integer>> newLocation(String location) {
        TreeMap<String, TreeSet<Integer>> locationMap = new TreeMap<>();
        TreeSet<Integer> lineLocations = new TreeSet<>();
        locationMap.put(location, lineLocations);
        return locationMap;
    }


    /**
     * Adds a new word to the WordIndex. Given the word, it's Path location, and the line number it was found at.
     *
     * @param word     the word to add
     * @param location where the wod was found
     * @param line     what line the word was found at
     */
    public void add(String word, String location, Integer line) {
        wordMap.putIfAbsent(word, newLocation(location));
        wordMap.get(word).putIfAbsent(location, new TreeSet<>());
        wordMap.get(word).get(location).add(line);
    }

    /**
     * Adds a list of words to the WordIndex. Given the word, it's Path location, and the line number it was found at.
     *
     * @param words     the words to add
     * @param location where the wod was found
     * @param line     what line the word was found at
     */
    public void addAll(ArrayList<String> words, String location, Integer line){
        for (String word : words){
            add(word, location, line);
        }
    }

    /**
     * Check if the wordMap contains a word
     * @param word the word to check
     * @return true if the word exists, false if not.
     */
    public boolean contains(String word){
        return wordMap.containsKey(word);
    }

    /**
     * Check if a word exists in a particular location
     * @param word the word to check
     * @param location the location to check
     * @return true if exists, false if not.
     */
    public boolean contains(String word, String location){
        TreeMap<String,TreeSet<Integer>> locationMap = wordMap.get(word);
        return (locationMap != null && locationMap.containsKey(location));
    }

    /**
     * Check if a position exists in a particular location and word.
     * @param word the word to check
     * @param location the location to check
     * @param position the position to check
     * @return true if the position exists or false if not
     */
    public boolean contains(String word, String location, Integer position){
        Set<Integer> positions = getPositions(word, location);
        return positions.contains(position);
    }

    /**
     * @return an unmodifiable view of the outer keySet, aka the words.
     */
    public Set<String> getWords(){
        return Collections.unmodifiableSet(wordMap.keySet());
    }

    /**
     *
     * @param word the word whose associated locations to return
     * @return an unmodifiable view of the locations or an empty set
     *         if the word doesn't exist
     */
    public Set<String> getLocations(String word){
        TreeMap<String,TreeSet<Integer>> locationMap = wordMap.get(word);
        if (locationMap == null)
            return Collections.emptySet();
        return Collections.unmodifiableSet(locationMap.keySet());
    }

    /**
     *
     * @param word the word whose associated positions to return
     * @param location the locations whose associated positions to return
     * @return an unmodifiable view of the positions or an empty set if the
     *         positions couldn't be found.
     */
    public Set<Integer> getPositions(String word, String location){
        TreeMap<String,TreeSet<Integer>> locationMap = wordMap.get(word);
        TreeSet<Integer> positions;
        if (locationMap == null || (positions = locationMap.get(location)) == null)
            return Collections.emptySet();
        return Collections.unmodifiableSet(positions);
    }

    /**
     * @return the number of words in the index
     */
    public Integer size(){
        return wordMap.size();
    }

    /**
     * Checks the number of locations a word has
     * @param word The word whose size to check
     * @return the number of locations a word has
     */
    public Integer size(String word){
        return getLocations(word).size();
    }

    /**
     * Returns the number of times a word appears in a particular location
     * @param word the word to check
     * @param location the location to check
     * @return the number of times a word appears in a particular location
     */
    public Integer size(String word, String location){
        return getPositions(word,location).size();
    }

    /**
     * Converts this Word Index to JSON and returns as a string
     * @return a string of the word index in json format
     */
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            toJSON(writer, 0);
        } catch (IOException e) {
            System.out.println("IO Error occurred while converting JSON to String");
        }
        return writer.toString();
    }
    
    /**
     * Uses wordToJSON toJSON method to convert a wordIndex to JSON.
     *
     * @param writer the {@link Writer} to use
     * @param indent the level of indentation.
     * @throws IOException if the writer throws and IOException
     */
    public void toJSON(Writer writer, int indent) throws IOException {
        PrettyJsonWriter.invertedWordIndextoJSON(writer, indent, wordMap);
    }
}

