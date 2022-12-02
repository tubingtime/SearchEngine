package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Interface for Single and Multithreaded QueryFileHandlers
 */
public interface QueryFileHandlerInterface {

    /**
     * @return an unmodifiable set of the queries in the results data structure.
     */
    Set<String> getAllQueries();

    /**
     * Gets results of a given location
     *
     * @param queryLine the location to get SearchResults from
     * @return an unmodifiable list of SearchResults
     */
    List<InvertedWordIndex.SearchResult> getResults(String queryLine);

    /**
     * Reads queries from a given {@link Path} line by line and then calls helper method to
     * stem, search, and add to the results data structure.
     *
     * @param queryInput  the location of the query file
     * @param exactSearch true for exact search, false to allow partial matches
     * @throws IOException if an IO error occurs while attemping to read from the file
     */
    void parseQuery(Path queryInput, boolean exactSearch) throws IOException;
    // how to do default implementation?
    /* TODO 
    public default void parseQuery(Path queryInput, boolean exactSearch) throws IOException {
      try (BufferedReader buffReader = Files.newBufferedReader(queryInput)) {
          String line;
          while ((line = buffReader.readLine()) != null) {
              parseQuery(line, exactSearch);
          }
      }
  }
  */

    /**
     * Cleans and stems a single query line. Then calls either exact or partial search and
     * puts the results into the results data structure.
     *
     * @param line        a String of query words separated by spaces
     * @param exactSearch true for exact search, false to allow partial matches
     */
    void parseQuery(String line, boolean exactSearch);

    /**
     * Converts the current SearchResults count to JSON
     *
     * @param output where to write the JSON file to
     * @throws IOException if the writer throws an Exception
     */
    void resultsToJSON(Path output) throws IOException;
}
