package edu.usfca.cs272;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class ThreadSafeInvertedWordIndex extends InvertedWordIndex {

    private final ReadWriteLock lock;

    public ThreadSafeInvertedWordIndex() {
        super();
        this.lock = new ReadWriteLock();
    }


    @Override
    protected void increment(String location) {
        lock.write().lock();
        super.increment(location);
        lock.write().unlock();
    }

    /**
     * Adds a new word to the WordIndex. Given the word, it's Path location, and the position number it was found at.
     * Also increments the word count
     *
     * @param word     the word to add
     * @param location where the wod was found
     * @param position what position the word was found at
     */
    @Override
    public void add(String word, String location, Integer position) {
        lock.write().lock();
        super.add(word, location, position);
        lock.write().unlock();
    }

    /**
     * @param word the word whose associated locations to return
     * @return an unmodifiable view of the locations or an empty set
     * if the word doesn't exist
     */
    @Override
    public Set<String> getLocations(String word) {
        lock.read().lock();
        Set<String> locations = super.getLocations(word);
        lock.read().unlock();
        return locations;
    }

    /**
     * @param word     the word whose associated positions to return
     * @param location the locations whose associated positions to return
     * @return an unmodifiable view of the positions or an empty set if the
     * positions couldn't be found.
     */
    @Override
    public Set<Integer> getPositions(String word, String location) {
        lock.read().lock();
        Set<Integer> positions = super.getPositions(word, location);
        lock.read().unlock();
        return positions;
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
        List<SearchResult> searchResults = super.exactSearch(queries);
        lock.read().unlock();
        return searchResults;
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
        List<SearchResult> searchResults = super.partialSearch(queries);
        lock.read().unlock();
        return searchResults;
    }


    /**
     * Converts this Word Index to JSON and returns as a string
     *
     * @return a string of the word index in json format
     */
    @Override
    public String toString() {
        lock.read().lock();
        String str = super.toString();
        lock.read().unlock();
        return str;
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
        super.toJSON(writer, indent);
        lock.read().unlock();
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
        super.toJSON(path);
        lock.read().unlock();
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
        super.wordCountToJSON(output);
        lock.read().unlock();
    }
}
