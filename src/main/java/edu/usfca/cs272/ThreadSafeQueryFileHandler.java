package edu.usfca.cs272;

import edu.usfca.cs272.InvertedWordIndex.SearchResult;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;


/**
 * Thread safe version of QueryFileHandler
 * Uses a {@link WorkQueue to execute tasks}
 */
public class ThreadSafeQueryFileHandler implements QueryFileHandlerInterface {

    /**
     * Point to an associated InvertedWordIndex
     */
    private final ThreadSafeInvertedWordIndex wordIndex;

    /**
     * Search results data structure
     * String location, List SearchResult
     */
    private final Map<String, List<SearchResult>> results;


    /**
     * The associated WorkQueue of threads to execute work
     */
    private final WorkQueue workQueue;

    /**
     * Logger used for this class
     */
    private static final Logger log = LogManager.getLogger();


    /**
     * Constructs a new instance of this class
     *
     * @param wordIndex associated wordIndex
     * @param workQueue associated workQueue
     */
    public ThreadSafeQueryFileHandler(ThreadSafeInvertedWordIndex wordIndex, WorkQueue workQueue) {
        this.wordIndex = wordIndex;
        this.results = new TreeMap<>();
        this.workQueue = workQueue;
        log.debug("ThreadSafeQueryFileHandler initialized.");
    }

    @Override
    public void parseQuery(Path queryInput, boolean exactSearch) throws IOException {
        try (BufferedReader buffReader = Files.newBufferedReader(queryInput)) {
            String line;
            log.debug("parseQuery: creating tasks...");
            while ((line = buffReader.readLine()) != null) {
                workQueue.execute(new QueryTask(line, exactSearch));
            }
            log.debug("parseQuery: called .finish() with {} remaining tasks and {} tasks pending",
                    workQueue.getTaskSize(), workQueue.getPending());
            workQueue.finish();
        }
    }

    @Override
    public void parseQuery(String line, boolean exactSearch) {
        Stemmer stemmer = new SnowballStemmer(ENGLISH);
        TreeSet<String> stems = WordCleaner.uniqueStems(line, stemmer);
        if (stems.isEmpty()) {
            return;
        }
        String key = String.join(" ", stems);
        synchronized (results) {
            if (results.containsKey(key)) {
                return;
            }
        }

        List<SearchResult> searchResults = wordIndex.search(stems, exactSearch);

        synchronized (results) {
            results.put(key, searchResults);
        }
    }

    /**
     * @return an unmodifiable set of the queries in the results data structure.
     */
    @Override
    public Set<String> getAllQueries() {
        synchronized (results) {
            return Collections.unmodifiableSet(results.keySet());
        }
    }

    /**
     * Gets results of a given location
     *
     * @param queryLine the location to get SearchResults from
     * @return an unmodifiable list of SearchResults
     */
    @Override
    public List<SearchResult> getResults(String queryLine) {
        TreeSet<String> stems = WordCleaner.uniqueStems(queryLine);
        String processedQuery = String.join(" ", stems);
        synchronized (results) {
            return results.getOrDefault(processedQuery, Collections.emptyList());
        }
    }


    /**
     * Converts the current SearchResults count to JSON
     *
     * @param output where to write the JSON file to
     * @throws IOException if the writer throws an Exception
     */
    @Override
    public void resultsToJSON(Path output) throws IOException {
        PrettyJsonWriter.resultsToJSON(this.results, output);
    }

    /**
     * A runnable object that calls parseQuery
     */
    private class QueryTask implements Runnable {

        /**
         * The query line to process
         */
        private final String line;

        /**
         * True for exact search, false if not
         */
        private final boolean exactSearch;

        /**
         * Constructs a new instance of this class
         *
         * @param line        The query line to process
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
