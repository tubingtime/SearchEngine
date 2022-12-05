package edu.usfca.cs272;

import edu.usfca.cs272.InvertedWordIndex.SearchResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Counts the words in a InvertedWordIndex
 */
public class QueryFileHandler implements QueryFileHandlerInterface {

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
