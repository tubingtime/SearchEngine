package edu.usfca.cs272;

import edu.usfca.cs272.InvertedWordIndex.SearchResult;

import javax.management.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
     * String location, List SearchResult
     */
    private final Map<String, List<InvertedWordIndex.SearchResult>> results;


    /**
     * Constructs a new instance of this class
     *
     * @param wordIndex associated InvertedWordIndex
     */
    public QueryFileHandler(InvertedWordIndex wordIndex) {
        this.wordIndex = wordIndex;
        this.results = new TreeMap<>();
    }

    /**
     * @return an unmodifiable version of the results data structure.
     */
    public Map<String, List<SearchResult>> getAllResults() { // TODO Set<String> getAllQueries() --> the unmodifiable keyset of results
        return Collections.unmodifiableMap(results);
    }

    /**
     * Gets results of a given location
     *
     * @param location the location to get SearchResults from
     * @return an unmodifiable list of SearchResults
     */
    public List<SearchResult> getResults(String location) { // TODO location-->queryLine
    	/*
    	 * TODO
    	 * getResults("hello hello world") --> results.getOrDefault("hello world", ...)
    	 */

        List<SearchResult> resultList = results.getOrDefault(location, Collections.emptyList());
        return Collections.unmodifiableList(resultList);
    }

    /**
     * Reads queries from a given {@link Path} line by line and then calls helper method to
     * stem, search, and add to the results data structure.
     *
     * @param queryInput  the location of the query file
     * @param exactSearch true for exact search, false to allow partial matches
     * @throws IOException if an IO error occurs while attemping to read from the file
     */
    public void parseQuery(Path queryInput, boolean exactSearch, int threads) throws IOException {
        try (BufferedReader buffReader = Files.newBufferedReader(queryInput)) {
            String line;
            WorkQueue workQueue = new WorkQueue(threads);
            if (threads > 0) {
                while ((line = buffReader.readLine()) != null) {
                    workQueue.execute(new QueryTask(line, exactSearch));
                }
                workQueue.finish();
                workQueue.shutdown();
            } else {
                while ((line = buffReader.readLine()) != null) {
                    parseQuery(line, exactSearch);
                }
            }
        }
    }

    /**
     * Cleans and stems a single query line. Then calls either exact or partial search and
     * puts the results into the results data structure.
     *
     * @param line        a String of query words separated by spaces
     * @param exactSearch true for exact search, false to allow partial matches
     */
    public void parseQuery(String line, boolean exactSearch) {
        TreeSet<String> stems = WordCleaner.uniqueStems(line);
        if (stems.isEmpty()) {
            return;
        }
        String key = String.join(" ", stems);
        // TODO Make sure key isn't already in the results map!

        // TODO List<SearchResult> searchResults = wordIndex.search(stems, exactSearch);
        List<SearchResult> searchResults;
        if (exactSearch) {
            searchResults = wordIndex.exactSearch(stems);
        } else {
            searchResults = wordIndex.partialSearch(stems);
        }

        synchronized (results) {
            results.put(key, searchResults);
        }
    }

    // TODO Remove
    /**
     * Parses queries into unique, sorted, cleaned, and stemmed words
     *
     * @param queryInput location of queries, each query separated by newline
     * @return A nested ArrayList data structure containing a Set for each query line
     * @throws IOException if an IO error occurs while stemming
     */
    public static ArrayList<Set<String>> parseQuerySet(Path queryInput) throws IOException {
        ArrayList<TreeSet<String>> queries = WordCleaner.listUniqueStems(queryInput);
        //queries.removeIf(TreeSet::isEmpty);
        ArrayList<Set<String>> nonBlankQueries = new ArrayList<>();

        for (TreeSet<String> querySet : queries) {
            if (querySet.size() > 0) {
                nonBlankQueries.add(querySet);
            }
        }
        return nonBlankQueries;
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

    public class QueryTask implements Runnable {

        String line;

        boolean exactSearch;

        public QueryTask(String line, boolean exactSearch) {
            this.line = line;
            this.exactSearch = exactSearch;
        }
        @Override
        public void run() {
            parseQuery(line, exactSearch);
        }
    }
}
