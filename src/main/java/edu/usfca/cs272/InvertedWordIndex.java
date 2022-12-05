package edu.usfca.cs272;


import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;


/**
 * A program that indexes the UNIQUE words that
 * were found in a text file (represented by {@link Path} objects) and stores where they
 * were found.
 *
 * @author TJ de Laveaga
 * @version Fall 2022
 */

public class InvertedWordIndex {


    /**
     * * Nested data structure to store words, what file they were found in, and the line locations.
     * String word, String location, Integer position
     */
    private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> wordMap;
    /**
     * Stores the total number of words at a file location
     * String location, Integer, count
     */
    private final TreeMap<String, Integer> wordCount;

    /**
     * * Constructs a new instance of WordIndex.
     */
    public InvertedWordIndex() {
        this.wordMap = new TreeMap<>();
        this.wordCount = new TreeMap<>();
    }

    /**
     * Increments a locations word count by one
     *
     * @param location the location to increment
     */
    private void increment(String location) {
        wordCount.putIfAbsent(location, 0);
        wordCount.put(location, wordCount.get(location) + 1);
    }

    /**
     * Increments a locations word count by @param x
     * @param x the amount to increment the word count by
     * @param location the location to increment
     */
    private void increment(String location, int x) {
        wordCount.putIfAbsent(location, 0);
        wordCount.put(location, wordCount.get(location) + x);
    }

    /**
     * Adds a new word to the WordIndex. Given the word, it's Path location, and the position number it was found at.
     * Also increments the word count
     *
     * @param word     the word to add
     * @param location where the wod was found
     * @param position what position the word was found at
     */
    public void add(String word, String location, Integer position) {
        wordMap.putIfAbsent(word, new TreeMap<>());               // new location map if it doesn't exist
        wordMap.get(word).putIfAbsent(location, new TreeSet<>()); // new position set if it doesn't exist
        boolean addedSomethingNew = wordMap.get(word).get(location).add(position);            // finally add

        if (addedSomethingNew) {
            increment(location);
        }
    }

    /**
     * Adds a list of words to the WordIndex. Given the word, it's Path location, and the position number it was found at.
     *
     * @param words    the words to add
     * @param location where the word was found
     * @param position what position the word was found at
     */
    public void addAll(ArrayList<String> words, String location, Integer position) {
        for (String word : words) {
            add(word, location, position++);
        }
    }

    /**
     * Adds everything in the provided InvertedWordIndex
     *
     * @param srcIndex Adds everything in the here
     */
    public void addAll(InvertedWordIndex srcIndex) {
        // 1. copy over word entries from source index to destination index
        for (var srcWordEntry : srcIndex.wordMap.entrySet()) {
            String word = srcWordEntry.getKey();
            if (this.contains(word)) {
                TreeMap<String, TreeSet<Integer>> destLocations = this.wordMap.get(word);
                for (var srcLocationEntry : srcWordEntry.getValue().entrySet()) {
                    String srcLocation = srcLocationEntry.getKey();
                    if (destLocations.containsKey(srcLocation)) {
                        Set<Integer> srcPositions = srcLocationEntry.getValue();
                        destLocations.get(srcLocation).addAll(srcPositions);
                    }
                    else {
                        destLocations.put(srcLocation, srcLocationEntry.getValue());
                    }
                }
            }
            else {
                this.wordMap.put(word, srcWordEntry.getValue());
            }
        }
        // 2. copy over wordCounts
        for (var countEntry : srcIndex.wordCount.entrySet()) {
            String location = countEntry.getKey();
            if (this.wordCount.containsKey(location)) {
                Integer destLocationsWordCount = wordCount.get(location);
                Integer srcLocationsWordCount = countEntry.getValue();
                this.wordCount.put(location, destLocationsWordCount + srcLocationsWordCount);
            }
            else {
                this.wordCount.put(location, countEntry.getValue());
            }
        }
    }

    /**
     * Check if the wordMap contains a word
     *
     * @param word the word to check
     * @return true if the word exists, false if not.
     */
    public boolean contains(String word) {
        return wordMap.containsKey(word);
    }

    /**
     * Check if a word exists in a particular location
     *
     * @param word     the word to check
     * @param location the location to check
     * @return true if exists, false if not.
     */
    public boolean contains(String word, String location) {
        Set<String> locationMap = getLocations(word);
        return (locationMap.contains(location));
    }

    /**
     * Check if a position exists in a particular location and word.
     *
     * @param word     the word to check
     * @param location the location to check
     * @param position the position to check
     * @return true if the position exists or false if not
     */
    public boolean contains(String word, String location, Integer position) {
        Set<Integer> positions = getPositions(word, location);
        return positions.contains(position);
    }

    /**
     * @return an unmodifiable view of the outer keySet, aka the words.
     */
    public Set<String> getWords() {
        return Collections.unmodifiableSet(wordMap.keySet());
    }

    /**
     * @param word the word whose associated locations to return
     * @return an unmodifiable view of the locations or an empty set
     * if the word doesn't exist
     */
    public Set<String> getLocations(String word) {
        TreeMap<String, TreeSet<Integer>> locationMap = wordMap.get(word);
        if (locationMap == null)
            return Collections.emptySet();
        return Collections.unmodifiableSet(locationMap.keySet());
    }

    /**
     * @param word     the word whose associated positions to return
     * @param location the locations whose associated positions to return
     * @return an unmodifiable view of the positions or an empty set if the
     * positions couldn't be found.
     */
    public Set<Integer> getPositions(String word, String location) {
        TreeMap<String, TreeSet<Integer>> locationMap = wordMap.get(word);
        TreeSet<Integer> positions;
        if (locationMap == null || (positions = locationMap.get(location)) == null)
            return Collections.emptySet();
        return Collections.unmodifiableSet(positions);
    }

    /**
     * Gets the wordCount
     *
     * @return an unmodifiable version of the wordCount
     */
    public Map<String, Integer> getWordCount() {
        return Collections.unmodifiableMap(wordCount);
    }

    /**
     * Gets the word count given a location
     *
     * @param location the location to find the word count of
     * @return the Integer value of the word count at a given location
     */
    public Integer getCount(String location) {
        return wordCount.getOrDefault(location, -1);
    }

    /**
     * Preforms an exact search on a Set of queries
     *
     * @param queries the queries to use
     * @return a List of SearchResult containing the results
     */
    public List<SearchResult> exactSearch(Set<String> queries) {
        List<SearchResult> results = new ArrayList<>();
        Map<String, SearchResult> resultsMap = new HashMap<>();

        for (String queryWord : queries) {
            if (wordMap.containsKey(queryWord)) {
                searchHelper(queryWord, resultsMap, results);
            }
        }
        Collections.sort(results);
        return results;
    }

    /**
     * Preforms a partial search on a Set of queries. Uses tailMap/binary search to avoid
     * looping through the whole wordMap
     *
     * @param queries the queries to use
     * @return a List of SearchResult containing the results
     */
    public List<SearchResult> partialSearch(Set<String> queries) {

        List<SearchResult> results = new ArrayList<>();
        Map<String, SearchResult> resultsMap = new HashMap<>();

        for (String queryWord : queries) {
            Set<String> tailSet = wordMap.tailMap(queryWord).keySet();

            String partialQueryWord;
            Iterator<String> tailIterator = tailSet.iterator();
            while ((tailIterator.hasNext() && (partialQueryWord = tailIterator.next()).startsWith(queryWord))) {
                searchHelper(partialQueryWord, resultsMap, results);
            }
        }
        Collections.sort(results);
        return results;
    }

    /**
     * Helper function for partial and exact search
     * Given a queryWord that was found in the wordIndex it will get the locations where that word is at and will
     * update or add all SearchResults associated with that location
     *
     * @param queryWord  the queryWord found in the wordIndex
     * @param resultsMap Helper data structure for efficiently updating SearchResults
     * @param results    the list of searchResults to add to and eventually return
     */
    private void searchHelper(String queryWord, Map<String, SearchResult> resultsMap, List<SearchResult> results) {
        Set<String> wordLocations = wordMap.get(queryWord).keySet();
        for (String location : wordLocations) {
            if (!resultsMap.containsKey(location)) {
                SearchResult searchResult = new SearchResult(location);
                resultsMap.put(location, searchResult);
                results.add(searchResult);
            }
            resultsMap.get(location).update(queryWord);
        }
    }


    /**
     * Helper method that calls either exact or partial search
     *
     * @param queries     a set of queries
     * @param exactSearch true for exact, false for partial
     * @return a list of SearchResults
     */
    public List<SearchResult> search(Set<String> queries, boolean exactSearch) {
        return exactSearch ? exactSearch(queries) : partialSearch(queries);
    }


    /**
     * @return the number of words in the index
     */
    public int size() {
        return wordMap.size();
    }

    /**
     * Checks the number of locations a word has
     *
     * @param word The word whose size to check
     * @return the number of locations a word has
     */
    public int size(String word) {
        return getLocations(word).size();
    }

    /**
     * Returns the number of times a word appears in a particular location
     *
     * @param word     the word to check
     * @param location the location to check
     * @return the number of times a word appears in a particular location
     */
    public int size(String word, String location) {
        return getPositions(word, location).size();
    }

    /**
     * Converts this Word Index to JSON and returns as a string
     *
     * @return a string of the word index in json format
     */
    @Override
    public String toString() {
        return PrettyJsonWriter.invertedWordIndexToJSON(this.wordMap);
    }

    /**
     * Uses PrettyJsonWriter toJSON method to convert a wordIndex to JSON.
     *
     * @param writer the {@link Writer} to use
     * @param indent the level of indentation.
     * @throws IOException if the writer throws and IOException
     */
    public void toJSON(Writer writer, int indent) throws IOException {
        PrettyJsonWriter.invertedWordIndexToJSON(this.wordMap, writer, indent);
    }

    /**
     * Uses PrettyJsonWriter toJSON method to convert a wordIndex to JSON.
     *
     * @param path the path to output the json file to
     * @throws IOException if the writer throws and IOException
     */
    public void toJSON(Path path) throws IOException {
        PrettyJsonWriter.invertedWordIndexToJSON(wordMap, path);
    }

    /**
     * Converts the current word count to JSON
     *
     * @param output where to write the JSON file to
     * @throws IOException if the writer throws an Exception
     */
    public void wordCountToJSON(Path output) throws IOException {
        PrettyJsonWriter.writeObject(wordCount, output);
    }

    /**
     * A data structure to hold a search result.
     */
    public class SearchResult implements Comparable<SearchResult> {
        /**
         * What file the search was preformed on
         */
        private final String where;
        /**
         * How many times the word stem was found
         */
        private long count;
        /**
         * score = total matches / total words in file
         */
        private double score;

        /**
         * Constructs a new instance of this class
         *
         * @param where the associated location of this search result
         */
        public SearchResult(String where) {
            this.count = 0;
            this.score = 0;
            this.where = where;
        }

        /**
         * Updates the score and count
         *
         * @param queryWord the queryWord to get the new count of
         */
        private void update(String queryWord) {
            this.count += wordMap.get(queryWord).get(where).size();
            this.score = (this.count / Double.valueOf(wordCount.get(this.where)));
        }

        @Override
        public int compareTo(SearchResult other) {
            int result = Double.compare(other.score, this.score);
            if (result == 0) {
                result = Long.compare(other.count, this.count);
                if (result == 0) {
                    result = this.where.compareToIgnoreCase(other.where);
                }
            }
            return result;
        }

        /**
         * Gets the word count
         *
         * @return a {@link Long} value of the word count
         */
        synchronized public long getCount() {
            return count;
        }

        /**
         * Gets the results' score
         *
         * @return a {@link Double} value of the score
         */
        synchronized public double getScore() {
            return score;
        }

        /**
         * Gets the results' location
         *
         * @return a {@link String} value of the location
         */
        synchronized public String getWhere() {
            return where;
        }
    }
}

