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

public class ThreadSafeQueryFileHandler extends QueryFileHandler {

    private final WorkQueue workQueue;

    public ThreadSafeQueryFileHandler(InvertedWordIndex wordIndex, WorkQueue workQueue) {
        super(wordIndex);
        this.workQueue = workQueue;
    }

    @Override
    public void parseQuery(Path queryInput, boolean exactSearch) throws IOException {
        try (BufferedReader buffReader = Files.newBufferedReader(queryInput)) {
            String line;
            while ((line = buffReader.readLine()) != null) {
                workQueue.execute(new QueryTask(line, exactSearch));
            }
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
