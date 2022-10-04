package edu.usfca.cs272;

import java.io.IOException;
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
    
    /*
     * TODO 
     * add(List<String> words, String location) or addAll or addWords
     * 
     * 3x has/contains, num/size, view/get methods
     * toString
     * 
     * getWords() --> safely return an unmodifiable view of the outer keyset
     * getLocations(String word) --> the inner keyset
     * getPosition(String word, String location) --> safely return the inner set of positions
     */

    /*
     * TODO Move more JSON writing logic into PrettyJsonWriter
     * 
     * Keep... toJSON(Path path) { calls 1 line from your json writer }
     */
    
    /**
     * Uses wordToJSON toJSON method to convert a wordIndex to JSON.
     *
     * @param writer the {@link Writer} to use
     * @param indent the level of indentation.
     * @throws IOException if the writer throws and IOException
     */
    public void toJSON(Writer writer, int indent) throws IOException {
        writer.write("{");
        var iterator = wordMap.entrySet().iterator();
        while (iterator.hasNext()) {
            var wordEntry = iterator.next();
            writer.write(newline);
            writeIndent(writer, indent + 1);
            writeQuote(wordEntry.getKey(), writer, 0);
            writer.write(": ");
            locationsToJSON(writer, indent + 1, wordEntry.getValue()); //locationsObj.toJSON
            if (iterator.hasNext()) {
                writer.write(",");
            }
        }
        writer.write(newline);
        writeIndent(writer, indent);
        writer.write("}");
    }

    /**
     * Uses the help of PrettyJSONWriter to convert a wordObj to JSON
     *
     * @param writer the {@link Writer} to use
     * @param indent the level of indentation to use
     * @param locations a TreeMap containing all locations the word was found in.
     * @throws IOException if the Writer throws an IOException
     */
    public void locationsToJSON(Writer writer, int indent, TreeMap<String, TreeSet<Integer>> locations) throws IOException {
        writer.write("{");
        var iterator = locations.entrySet().iterator();
        while (iterator.hasNext()) {
            var locationEntry = iterator.next();
            writer.write(newline);
            writeIndent(writer, indent + 1);
            writeQuote(locationEntry.getKey(), writer, 0);
            writer.write(": ");
            writeArray(locationEntry.getValue(), writer, indent + 1);
            if (iterator.hasNext()) {
                writer.write(",");
            }
        }
        writer.write(newline);
        writeIndent(writer, indent);
        writer.write("}");
    }
}

