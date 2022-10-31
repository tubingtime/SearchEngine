package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * Counts the words in a InvertedWordIndex
 */
public class WordCounter { // TODO QueryFileHandler

    /**
     * TreeMap to store how many word stems are in each file
     */
    public final TreeMap<String, Integer> totalWords; // TODO Remove

    /**
     * Point to an associated InvertedWordIndex
     */
    private final InvertedWordIndex wordIndex;

    /**
     * Search results data structure
     */
    public Map<String, List<SearchResult>> results; // TODO private


    /**
     * Constructs a new instance of this class
     *
     * @param wordIndex associated InvertedWordIndex
     */
    public WordCounter(InvertedWordIndex wordIndex) {
        this.totalWords = new TreeMap<>();
        this.wordIndex = wordIndex;
        this.results = new TreeMap<>();
    }

    /**
     * Increments a locations word count by one
     *
     * @param location the location to increment
     */
    public void increment(String location) {
        totalWords.putIfAbsent(location, 0);
        totalWords.put(location, totalWords.get(location) + 1);
    }

    /**
     * Builds an exact search using streams.
     *
     * @param input         Path of queries to use
     * @param partialSearch true for partial (broken), false for exact
     * @return a map of the results
     * @throws IOException if the WordCleaner throws an IOException while cleaning.
     */
    public Map<String, List<SearchResult>> buildQuery(Path input, boolean partialSearch) throws IOException {
        ArrayList<TreeSet<String>> queries = WordCleaner.listUniqueStems(input);
        TreeSet<String> uniqueQuerySet = WordCleaner.uniqueStems(input);
        ArrayList<TreeSet<String>> uniqueQueries = new ArrayList<>(); //rename

        for (TreeSet<String> querySet : queries) {
            if (querySet.size() > 0) {
                uniqueQueries.add(querySet);
            }
        }

        if (partialSearch) {
            Set<String> wordList = wordIndex.getWords();
            ArrayList<String> wordBuffer;
            // this little maneuver is gonna cost us O(n^3) years
            // scan for partial matches
            for (String word : wordList) {
                if (uniqueQuerySet.contains(word)) {
                    continue;
                }
                for (TreeSet<String> querySet : uniqueQueries) {
                    wordBuffer = new ArrayList<>();
                    for (String queryWord : querySet) {
                        if (word.startsWith(queryWord)) {
                            wordBuffer.add(word);
                        }
                    }
                    querySet.addAll(wordBuffer);
                }
            }
        }

        Map<String, List<SearchResult>> results =
                uniqueQueries
                        .stream()  // Stream<TreeSet<String>>
                        .collect(toMap((querySet) -> {
                            String key = querySet.toString();
                            return key.substring(1, key.length() - 1); // remove brackets from key
                        }, this::query, (merge1, merge2) -> merge1));
        Map<String, List<SearchResult>> sortedResults = new TreeMap<>();
        var resultsEntrySet = results.entrySet();
        for (var result : resultsEntrySet) {
            sortedResults.put(result.getKey().replaceAll(",", ""), result.getValue());
        }
        return sortedResults;
    }

    /**
     * Processes a single line from a query file
     *
     * @param queryLine a TreeSet containing a parsed query
     * @return a list of SearchResult
     */
    public List<SearchResult> query(TreeSet<String> queryLine) {
        return queryLine
                .stream()
                .map(wordIndex::getLocations)
                .flatMap(Set::stream)
                .collect(toSet())
                .stream()
                .unordered()
                .distinct() // filter any duplicates
                .map(location -> makeResult(queryLine, location))
                .sorted()
                .toList();
    }


    /**
     * Creates a result given a list of word stems and their associated location.
     *
     * @param query    a list of word stems
     * @param location locations the word stems were found
     * @return a populated SearchResult
     */
    public SearchResult makeResult(TreeSet<String> query, String location) {
        long totalMatches = query.stream()
                .map(word -> wordIndex.getPositions(word, location))
                .mapToLong(Set::size)
                .sum();
        double score = (totalMatches / Double.valueOf(totalWords.get(location)));
        return new SearchResult(totalMatches, score, location);
    }

    /**
     * Converts the current word count to JSON
     *
     * @param output where to write the JSON file to
     * @throws IOException if the writer throws an Exception
     */
    public void wordCountToJSON(Path output) throws IOException {
        PrettyJsonWriter.writeObject(totalWords, output);
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
