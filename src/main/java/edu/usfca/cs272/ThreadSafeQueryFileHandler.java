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
import java.util.List;
import java.util.TreeSet;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/*
 * TODO Create a shared interface between the single thread and multi thread
 * versions of this class that both implement
 * 
 * Make public void parseQuery(Path queryInput, boolean exactSearch) throws IOException {
 * a default implementation in the interface
 */

/**
 * Thread safe version of {@link QueryFileHandler}
 * Uses a {@link WorkQueue to execute tasks}
 */
public class ThreadSafeQueryFileHandler extends QueryFileHandler {

    /**
     * The associated WorkQueue of threads to execute work
     */
    private final WorkQueue workQueue;

    /** Logger used for this class */
    private static final Logger log = LogManager.getLogger();


    /**
     * Constructs a new instance of this class
     *
     * @param wordIndex associated wordIndex
     * @param workQueue associated workQueue
     */
    // TODO Pass in a thread-safe index instead
    public ThreadSafeQueryFileHandler(InvertedWordIndex wordIndex, WorkQueue workQueue) {
        super(wordIndex);
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
     * A runnable object that calls parseQuery
     */
    public class QueryTask implements Runnable { // TODO private

    	// TODO private and final
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
