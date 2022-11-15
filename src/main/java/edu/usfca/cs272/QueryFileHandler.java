package edu.usfca.cs272;

import edu.usfca.cs272.InvertedWordIndex.SearchResult;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

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
     * @return an unmodifiable set of the queries in the results data structure.
     */
    public Set<String> getAllQueries(){
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
     * Reads queries from a given {@link Path} line by line and then calls helper method to
     * stem, search, and add to the results data structure.
     *
     * @param queryInput  the location of the query file
     * @param exactSearch true for exact search, false to allow partial matches
     * @param threads how many threads to use
     * @throws IOException if an IO error occurs while attemping to read from the file
     */
    public void parseQuery(Path queryInput, boolean exactSearch, int threads) throws IOException {
        try (BufferedReader buffReader = Files.newBufferedReader(queryInput)) {
            String line;
            WorkQueue workQueue = Driver.workQueue;
            if (threads > 0) {
                while ((line = buffReader.readLine()) != null) {
                    workQueue.execute(new QueryTask(line, exactSearch));
                }
                workQueue.finish();
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
        Stemmer stemmer = new SnowballStemmer(ENGLISH);
        TreeSet<String> stems = WordCleaner.uniqueStems(line, stemmer);
        if (stems.isEmpty()) {
            return;
        }
        String key = String.join(" ", stems);

        synchronized (results) {
            if (results.containsKey(key)){
                return;
            }
        }

        List<SearchResult> searchResults = wordIndex.search(stems, exactSearch);

        synchronized (results) {
            results.put(key, searchResults);
        }
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

    /**
     * A runnable object that calls parseQuery
     */
    public class QueryTask implements Runnable {

        /**
         * The query line to process
         */
        String line;

        /**
         * True for exact search, false if not
         */
        boolean exactSearch;

        /**
         * Constructs a new instance of this class
         * @param line The query line to process
         * @param exactSearch True for exact search, false if not
         */
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
