package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * Counts the words in a InvertedWordIndex
 */
public class WordCounter {

    public static class SearchResult implements Comparable<SearchResult> {

        private final long count;

        private final double score;

        private final String where;

        public SearchResult(long count, double score, String where) {
            this.count = count;
            this.score = score;
            this.where = where;
        }

        @Override
        public int compareTo(SearchResult other) {
            int result = Double.compare(this.score, other.score);
            if (result == 0){
                result = Long.compare(this.count, other.count);
                if (result == 0){
                    result = this.where.compareToIgnoreCase(other.where);
                }
            }
            return result;
        }


        public String toJSON() {
            //TODO: convert ot json
            return null;
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
        Map<String,List<SearchResult>> results = queries
                .stream()
                .collect(toMap( queryLine -> queryLine.toString(), this::query));
        this.results = results;
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
                .count();
        double score = (totalMatches / Double.valueOf(totalWords.get(location)));
        SearchResult searchResult = new SearchResult(totalMatches, score, location);;
        return searchResult;
    }

    public void toJSON(Path output) throws IOException {
        PrettyJsonWriter.writeObject(totalWords, output);
    }

    public void resultsToJSON(Path output) throws IOException {
        //TODO: convert to JSON
    }
}
