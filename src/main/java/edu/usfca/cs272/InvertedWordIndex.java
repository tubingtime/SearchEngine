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
        boolean didNotContain = wordMap.get(word).get(location).add(position);            // finally add
        if (didNotContain){
            increment(location);
        }
    }

    /**
     * Adds a list of words to the WordIndex. Given the word, it's Path location, and the position number it was found at.
     *
     * @param words    the words to add
     * @param location where the wod was found
     * @param position what position the word was found at
     */
    public void addAll(ArrayList<String> words, String location, Integer position) {
        for (String word : words) {
            add(word, location, position++);
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
    

    public Map<String, Integer> getWordCount() {
        return Collections.unmodifiableMap(wordCount);
    }

    public Integer getCount(String location) {
        Integer count;
        if (!wordCount.containsKey(location)) {
            count = -1;
        } else {
            count = wordCount.get(location);
        }
        return count;
    }

    /**
     * Preforms an exact search on a Set of queries
     *
     * @param queries the queries to use
     * @return a List of SearchResult containing the results
     */
    public List<SearchResult> exactSearch(Set<String> queries) {
        List<SearchResult> results = new ArrayList<>();
        Map<String, Integer> matchCounts = new HashMap<>();  /* <Location, matchCount> */
        
        // TODO Map<String, SearchResult> matchCounts = new HashMap<>();
        for (String queryWord : queries) {
            if (contains(queryWord)) { // TODO Directly access the wordMap everywhere you can inside this method
                Set<String> wordLocations = getLocations(queryWord);
                for (String location : wordLocations) {
                			/* TODO 
                			check if you need to create a new search result
                				create a new result
                				add the result to the list
                				add the result to the map
                			
                			get the search result and update its count and score
                			mathcCounts.get(location).update(queryWord);
                			*/
                	
                	
                    int matchCount = matchCounts.getOrDefault(location, 0);
                    matchCount += getPositions(queryWord, location).size();
                    matchCounts.put(location, matchCount);
                }
            }
        }
        for (var match : matchCounts.entrySet()) { // TODO Remove
            String location = match.getKey();
            Integer count = match.getValue();
            double score = (count / Double.valueOf(wordCount.get(location)));
            results.add(new SearchResult(count, score, location));
        }
        Collections.sort(results);
        return results;
    }

    /**
     * Preforms an exact search on a Set of queries
     *
     * @param queries the queries to use
     * @return a List of SearchResult containing the results
     */
    public List<SearchResult> exactSearch(ArrayList<String> queries) { // TODO Remove
        List<SearchResult> results = new ArrayList<>();
        Map<String, Integer> matchCounts = new HashMap<>();  /* <Location, matchCount> */
        for (String queryWord : queries) {
            if (contains(queryWord)) {
                Set<String> wordLocations = getLocations(queryWord);
                for (String location : wordLocations) {
                    int matchCount = matchCounts.getOrDefault(location, 0);
                    matchCount += getPositions(queryWord, location).size();
                    matchCounts.put(location, matchCount);
                }
            }
        }
        for (var match : matchCounts.entrySet()) {
            String location = match.getKey();
            Integer count = match.getValue();
            double score = (count / Double.valueOf(wordCount.get(location)));
            results.add(new SearchResult(count, score, location));
        }
        Collections.sort(results);
        return results;
    }

    // TODO Remove
    /**
     * Calls exactSearch for every query found in the given path
     *
     * @param queryInput the file containing queries
     * @return a Map data structure containing the search results
     * @throws IOException if the WordCleaner throws an IOException
     */
    public Map<String, List<SearchResult>> exactSearch2(Path queryInput) throws IOException {
        ArrayList<Set<String>> parsedQueries = QueryFileHandler.parseQuerySet(queryInput);
        Map<String, List<SearchResult>> results = new TreeMap<>();

        for (Set<String> queries : parsedQueries) {
            results.put(String.join(" ", queries), exactSearch(queries));
        }
        return results;
    }

    /**
     * Preforms an partial search on a Set of queries
     *
     * @param queries the queries to use
     * @return a List of SearchResult containing the results
     */
    public List<SearchResult> partialSearch(Set<String> queries) {
        // build partial matches
        Set<String> wordList = this.getWords();
        ArrayList<String> partialQueries = new ArrayList<>();
        for (String word : wordList) {
            for (String queryWord : queries) {
                if (word.startsWith(queryWord)) {
                    partialQueries.add(word);
                }
            }
        }
        // search partial matches
        return exactSearch(partialQueries);
        
        /* TODO 
        create the list of results
        create the map of location to search result
        
        loop through every query word
        	loop through the appropriate keys in the wordMap --> use tailMap and break to avoid looping throuhg every key
        		if the key is a partial match...
        			then the same loop through locations as exact search
        */
        
    }

    // TODO Remove
    /**
     * Calls partialSearch for every query found in the given path
     *
     * @param queryInput the file containing queries
     * @return a Map data structure containing the search results
     * @throws IOException if the WordCleaner throws an IOException
     */
    public Map<String, List<SearchResult>> partialSearch2(Path queryInput) throws IOException {
        ArrayList<Set<String>> parsedQueries = QueryFileHandler.parseQuerySet(queryInput);
        Map<String, List<SearchResult>> results = new TreeMap<>();

        for (Set<String> queries : parsedQueries) {
            results.put(String.join(" ", queries), partialSearch(queries));
        }
        return results;
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
    public static class SearchResult implements Comparable<SearchResult> { // TODO non-static
        /**
         * How many times the word stem was found
         */
        public long count; // TODO private

        /**
         * score = total matches / total words in file
         */
        public double score; // TODO private

        /**
         * What file the search was preformed on
         */
        public String where; // TODO private and final

        /**
         * Constructs a new instance of this class
         *
         * @param count How many times the word stem was found
         * @param score Total matches / Total words in file
         * @param where What file the search was preformed on
         */
        // TODO public SearchResult(String where) {
        public SearchResult(long count, double score, String where) {
            this.count = count; // TODO 0
            this.score = score; // TODO 0
            this.where = where;
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
    }
}

