package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Counts the words in a InvertedWordIndex
 */
public class QueryFileHandler {

    /**
     * Point to an associated InvertedWordIndex
     */
    private final InvertedWordIndex wordIndex;

    /**
     * Search results data structure
     */
    private Map<String, List<InvertedWordIndex.SearchResult>> results; // TODO final


    /**
     * Constructs a new instance of this class
     *
     * @param wordIndex associated InvertedWordIndex
     */
    public QueryFileHandler(InvertedWordIndex wordIndex) {
        this.wordIndex = wordIndex;
        this.results = new TreeMap<>();
    }
    

    public void parseQuery(Path queryInput, boolean exactSearch) throws IOException {
        try (BufferedReader buffReader = Files.newBufferedReader(queryInput)) {
            String line;
            while ((line = buffReader.readLine()) != null){
                parseQuery(line, exactSearch);
            }
        }
    }
    
    public void parseQuery(String line, boolean exactSerch) {
        ArrayList<String> stems = WordCleaner.listStems(line);
        if (stems.isEmpty()){
            return;
        }
        line = String.join(" ", stems);
        if (exactSerch){
            exact
        }
    	collect the search results for that line
    	
    	(figure out if we should skip this line... if its empty etc.)
    }
    
    ...and maybe a nice get methods too


    /**
     * Parses queries into unique, sorted, cleaned, and stemmed words
     *
     * @param queryInput location of queries, each query separated by newline
     * @return An nested ArrayList data structure containing an ArrayList for each query line
     * @throws IOException if an IO error occurs while stemming
     */
    public static ArrayList<ArrayList<String>> parseQuery(Path queryInput) throws IOException {
        ArrayList<TreeSet<String>> queries = WordCleaner.listUniqueStems(queryInput);
        ArrayList<ArrayList<String>> nonBlankQueries = new ArrayList<>();

        for (TreeSet<String> querySet : queries) {
            if (querySet.size() > 0) {
                nonBlankQueries.add(new ArrayList<>(querySet));
            }
        }
        return nonBlankQueries;
    }

    /**
     * Parses queries into unique, sorted, cleaned, and stemmed words
     *
     * @param queryInput location of queries, each query separated by newline
     * @return A nested ArrayList data structure containing a Set for each query line
     * @throws IOException if an IO error occurs while stemming
     */
    public static ArrayList<Set<String>> parseQuerySet(Path queryInput) throws IOException {
        ArrayList<TreeSet<String>> queries = WordCleaner.listUniqueStems(queryInput);
        //queries.removeIf(TreeSet::isEmpty);
        ArrayList<Set<String>> nonBlankQueries = new ArrayList<>();

        for (TreeSet<String> querySet : queries) {
            if (querySet.size() > 0) {
                nonBlankQueries.add(querySet);
            }
        }
        return nonBlankQueries;
    }

    /**
     * Preforms an exact search
     *
     * @param queryPath the location of the queries
     * @throws IOException if an IO error occurs while reading and stemming from file
     */
    public void exactSearch(Path queryPath) throws IOException {
        this.results = wordIndex.exactSearch2(queryPath);
    }

    public void exactSearch(String query) {
        return wordIndex.exactSearch(query);
    }

    /**
     * Preforms a partial search
     *
     * @param queryPath the location of the queries
     * @throws IOException if an IO error occurs while reading and stemming from file
     */
    public void partialSearch(Path queryPath) throws IOException {
        this.results = wordIndex.partialSearch2(queryPath);
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
