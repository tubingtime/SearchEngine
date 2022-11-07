package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Counts the words in a InvertedWordIndex
 */
public class QueryFileHandler {

    /**
     * Point to an associated InvertedWordIndex
     */
    private final InvertedWordIndex wordIndex;

    /**
     * Search results data structure
     */
    private Map<String, List<SearchResult>> results; // TODO private


    /**
     * Constructs a new instance of this class
     *
     * @param wordIndex associated InvertedWordIndex
     */
    public QueryFileHandler(InvertedWordIndex wordIndex) {
        this.wordIndex = wordIndex;
        this.results = new TreeMap<>();
    }

    public void exactSearch (Path queryPath) throws IOException {
        this.results = wordIndex.exactSearch(queryPath);
    }

    public void partialSearch (Path queryPath) throws IOException {
        this.results = wordIndex.partialSearch(queryPath);
    }

    /**
     * Converts the current SearchResults count to JSON
     *
     * @param output where to write the JSON file to
     * @throws IOException if the writer throws an Exception
     */
    public void resultsToJSON(Path output) throws IOException {
        PrettyJsonWriter.resultsToJSON(this.results, output);
    }
}
