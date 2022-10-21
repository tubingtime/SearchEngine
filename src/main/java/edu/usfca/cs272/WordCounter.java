package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * Counts the words in a InvertedWordIndex
 */
public class WordCounter {

    public static class SearchResult implements Comparable<SearchResult> {

        public final long count;

        public final double score;

        public final String where;

        public SearchResult(long count, double score, String where) {
            this.count = count;
            this.score = score;
            this.where = where;
        }

        @Override
        public int compareTo(SearchResult other) {
            int result = Double.compare(other.score, this.score);
            if (result == 0){
                result = Long.compare(other.count, this.count);
                if (result == 0){
                    result = this.where.compareToIgnoreCase(other.where);
                }
            }
            return result;
        }
    }

    /**
     * TreeMap to store how many word stems are in each file
     */
    private final TreeMap<String,Integer> totalWords;
    private final InvertedWordIndex wordIndex;

    private Map<String,List<SearchResult>> results;



    public WordCounter(InvertedWordIndex wordIndex) {
        this.totalWords = new TreeMap<>();
        this.wordIndex = wordIndex;
        this.results = null;
    }

    /**
     * Increments a locations word count by one
     * @param location the location to increment
     */
    public void increment(String location){
        totalWords.putIfAbsent(location, 0);
        totalWords.put(location, totalWords.get(location)+1);
    }

    public void buildQuery(Path input) throws IOException {
        ArrayList<TreeSet<String>> queries = WordCleaner.listUniqueStems(input);
        ArrayList<TreeSet<String>> uniqueQueries = new ArrayList<>();

        for (TreeSet<String> query : queries){
            if (query.size() > 0){

                uniqueQueries.add(query);
            }
        }

        Map<String,List<SearchResult>> results =
                uniqueQueries
                .stream()  // Stream<TreeSet<String>>
                .collect(toMap((querySet) -> {
                    String key = querySet.toString();
                    return key.substring(1,key.length() - 1); // remove brackets from key
                }, this::query, (merge1, merge2) -> merge1));
        Map<String,List<SearchResult>> sortedResults = new TreeMap<>();
        var resultsEntrySet = results.entrySet();
        for (var result : resultsEntrySet){
            sortedResults.put(result.getKey().replaceAll(",",""), result.getValue());
        }
        this.results = sortedResults;
    }


    public List<SearchResult> query(TreeSet<String> queryLine){
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


    public SearchResult makeResult(TreeSet<String> query, String location){
        long totalMatches = query.stream()
                .map(word -> wordIndex.getPositions(word, location))
                .mapToLong(Set::size)
                .sum();
        double score = (totalMatches / Double.valueOf(totalWords.get(location)));
        SearchResult searchResult = new SearchResult(totalMatches, score, location);;
        return searchResult;
    }

    public void toJSON(Path output) throws IOException {
        PrettyJsonWriter.writeObject(totalWords, output);
    }

    public void resultsToJSON(Path output) throws IOException {
        PrettyJsonWriter.resultsToJSON(this.results, output);
    }
}
