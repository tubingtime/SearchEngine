package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

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

    void parseQuery(Path queryInput, boolean exactSearch) throws IOException;
    // how to do default implementation?

    void parseQuery(String line, boolean exactSearch);

    /**
     * Converts the current SearchResults count to JSON
     *
     * @param output where to write the JSON file to
     * @throws IOException if the writer throws an Exception
     */
    void resultsToJSON(Path output) throws IOException;
}
