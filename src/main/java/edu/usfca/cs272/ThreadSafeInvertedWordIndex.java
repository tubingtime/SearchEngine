package edu.usfca.cs272;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Thread safe version of InvertedWordIndex
 */
public class ThreadSafeInvertedWordIndex extends InvertedWordIndex {



		/**
     * Manages a read and write lock. Improves efficiency for multithreading since queries involve only reading
     * that can happen concurrently.
     */
    private final ReadWriteLock lock;

    /**
     * Constructs a new instance of this class
     */
    public ThreadSafeInvertedWordIndex() {
        super();
        this.lock = new ReadWriteLock();
    }

    @Override
    public void add(String word, String location, Integer position) {
        lock.write().lock();
        try {
            super.add(word, location, position);
        } finally {
            lock.write().unlock();
            // make sure we unlock, even if there's a runtime exception
        }
    }

    @Override
	public void addAll(ArrayList<String> words, String location,
			Integer position) {
        lock.write().lock();
        try {
            for (String word : words) {
                super.add(word, location, position++);
            }
        } finally {
            lock.write().unlock();
        }
	}


    @Override
    public void addAll(InvertedWordIndex index) {
        lock.write().lock();
        try {
            super.addAll(index);
        } finally {
            lock.write().unlock();
        }
    }

    @Override
    public Set<String> getLocations(String word) {
        lock.read().lock();
        try {
            return super.getLocations(word);
        } finally {
            lock.read().unlock();
        }
    }

    @Override
    public Set<Integer> getPositions(String word, String location) {
        lock.read().lock();
        try {
            return super.getPositions(word, location);
        } finally {
            lock.read().unlock();
        }
    }

    @Override
    public boolean contains(String word) {
        lock.read().lock();
        try {
            return super.contains(word);
        } finally {
            lock.read().unlock();
        }
    }

    @Override
    public Set<String> getWords() {
        lock.read().lock();
        try {
            return super.getWords();
        } finally {
            lock.read().unlock();
        }
    }

    @Override
    public Map<String, Integer> getWordCount() {
        lock.read().lock();
        try {
            return super.getWordCount();
        } finally {
            lock.read().unlock();
        }
    }

    @Override
    public Integer getCount(String location) {
        lock.read().lock();
        try {
            return super.getCount(location);
        } finally {
            lock.read().unlock();
        }
    }

    @Override
    public int size() {
        lock.read().lock();
        try {
            return super.size();
        } finally {
            lock.read().unlock();
        }
    }

    /**
     * Preforms an exact search on a Set of queries
     *
     * @param queries the queries to use
     * @return a List of SearchResult containing the results
     */
    @Override
    public List<SearchResult> exactSearch(Set<String> queries) {
        lock.read().lock();
        try {
            return super.exactSearch(queries);
        } finally {
            lock.read().unlock();
        }
    }

    /**
     * Preforms a partial search on a Set of queries. Uses tailMap/binary search to avoid
     * looping through the whole wordMap
     *
     * @param queries the queries to use
     * @return a List of SearchResult containing the results
     */
    @Override
    public List<SearchResult> partialSearch(Set<String> queries) {
        lock.read().lock();
        try {
            return super.partialSearch(queries);
        } finally {
            lock.read().unlock();
        }
    }


    /**
     * Converts this Word Index to JSON and returns as a string
     *
     * @return a string of the word index in json format
     */
    @Override
    public String toString() {
        lock.read().lock();
        try {
            return super.toString();
        } finally {
            lock.read().unlock();
        }
    }

    /**
     * Uses PrettyJsonWriter toJSON method to convert a wordIndex to JSON.
     *
     * @param writer the {@link Writer} to use
     * @param indent the level of indentation.
     * @throws IOException if the writer throws and IOException
     */
    @Override
    public void toJSON(Writer writer, int indent) throws IOException {
        lock.read().lock();
        try {
            super.toJSON(writer, indent);
        } finally {
            lock.read().unlock();
        }
    }

    /**
     * Uses PrettyJsonWriter toJSON method to convert a wordIndex to JSON.
     *
     * @param path the path to output the json file to
     * @throws IOException if the writer throws and IOException
     */
    @Override
    public void toJSON(Path path) throws IOException {
        lock.read().lock();
        try {
            super.toJSON(path);
        } finally {
            lock.read().unlock();
        }
    }

    /**
     * Converts the current word count to JSON
     *
     * @param output where to write the JSON file to
     * @throws IOException if the writer throws an Exception
     */
    @Override
    public void wordCountToJSON(Path output) throws IOException {
        lock.read().lock();
        try {
            super.wordCountToJSON(output);
        } finally {
            lock.read().unlock();
        }
    }
}
