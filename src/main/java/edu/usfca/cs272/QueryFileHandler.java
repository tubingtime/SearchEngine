package edu.usfca.cs272;

import edu.usfca.cs272.InvertedWordIndex.SearchResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Counts the words in a InvertedWordIndex
 */
public class QueryFileHandler implements QueryFileHandlerInterface { // TODO make the members private

    /**
     * Point to an associated InvertedWordIndex
     */
    private final InvertedWordIndex wordIndex;

    /**
     * Search results data structure
     * String location, List SearchResult
     */
    protected final Map<String, List<InvertedWordIndex.SearchResult>> results;


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
     * Reads queries from a given {@link Path} line by line and then calls helper method to
     * stem, search, and add to the results data structure.
     *
     * @param queryInput  the location of the query file
     * @param exactSearch true for exact search, false to allow partial matches
     * @throws IOException if an IO error occurs while attemping to read from the file
     */
    @Override
    public void parseQuery(Path queryInput, boolean exactSearch) throws IOException {
        try (BufferedReader buffReader = Files.newBufferedReader(queryInput)) {
            String line;
            while ((line = buffReader.readLine()) != null) {
                parseQuery(line, exactSearch);
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
    @Override
    public void parseQuery(String line, boolean exactSearch) {
        TreeSet<String> stems = WordCleaner.uniqueStems(line);
        if (stems.isEmpty()) {
            return;
        }
        String key = String.join(" ", stems);

        if (results.containsKey(key)) {
            return;
        }

        List<SearchResult> searchResults = wordIndex.search(stems, exactSearch);
        results.put(key, searchResults);

    }

    /**
     * @return an unmodifiable set of the queries in the results data structure.
     */
    public Set<String> getAllQueries() {
        return Collections.unmodifiableSet(results.keySet());
    }

    /**
     * Gets results of a given location
     *
     * @param queryLine the location to get SearchResults from
     * @return an unmodifiable list of SearchResults
     */
    public List<SearchResult> getResults(String queryLine) {

        TreeSet<String> stems = WordCleaner.uniqueStems(queryLine);
        String processedQuery = String.join(" ", stems);
        results.getOrDefault(processedQuery, Collections.emptyList());

        return results.getOrDefault(processedQuery, Collections.emptyList());
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
