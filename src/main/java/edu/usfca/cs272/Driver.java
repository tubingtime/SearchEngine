package edu.usfca.cs272;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Thomas de Laveaga
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class Driver {
    /**
     * Log4J Logger used for this class
     */
    private static final Logger log = LogManager.getLogger();

    /**
     * Initializes the classes necessary based on the provided command-line
     * arguments. This includes (but is not limited to) how to build or search an
     * inverted index.
     *
     * @param args flag/value pairs used to start this program
     */
    public static void main(String[] args) {
        // store initial start time
        Instant start = Instant.now();
        args = new String[]{"-server", "8081",
                "-html", "https://usf-cs272-fall2022.github.io/project-web/input/simple/", "-max", "50"}; //debug todo: remove
        ArgumentParser argumentParser = new ArgumentParser(args); /* parse args */
        log.debug("Actual args: {}", Arrays.toString(args));
        log.debug("Parsed args: {}", argumentParser);

        InvertedWordIndex invertedWordIndex;
        QueryFileHandlerInterface queryFileHandler;

        ThreadSafeInvertedWordIndex threadSafe; // we use this to initialize queryFileHandler and avoid down-casting
        WorkQueue workQueue = null;


        if (argumentParser.hasFlag("-threads") || argumentParser.hasFlag("-html") || argumentParser.hasFlag("-server")) {
            int threads = argumentParser.getInteger("-threads", 5);
            if (threads < 1) {
                threads = 5;
            }
            log.debug("-threads or web crawling detected! Initializing a workQueue with {} threads", threads);
            workQueue = new WorkQueue(threads);
            threadSafe = new ThreadSafeInvertedWordIndex();
            invertedWordIndex = threadSafe;
            queryFileHandler =
                    new ThreadSafeQueryFileHandler(threadSafe, workQueue);
        } else {
            invertedWordIndex = new InvertedWordIndex();
            queryFileHandler = new QueryFileHandler(invertedWordIndex);
        }

        if (argumentParser.hasFlag("-html")) {
            String seed = argumentParser.getString("-html");
            if (seed == null) {
                System.out.println("No URL found. You must provide a URL for the -html flag." +
                        "\nExiting...");
                return;
            }
            int max = argumentParser.getInteger("-max", 1);
            if (max < 0) {
                max = 1;
            }
            WebCrawler webCrawler = new WebCrawler(max);
            assert invertedWordIndex instanceof ThreadSafeInvertedWordIndex;
            try {
                DatabaseConnector connector = new DatabaseConnector("database.properties");
                try (Connection db = connector.getConnection()){
                    webCrawler.startCrawl(seed, (ThreadSafeInvertedWordIndex) invertedWordIndex, workQueue, db);
                } catch (MalformedURLException e) {
                    System.out.printf("Malformed URL detected: " + seed);
                } catch (SQLException e) {
                    System.out.println("SQL ERROR, unable to connect to db");
                    throw new RuntimeException(e); //todo: do not throw
                }
            } catch (IOException e) {
                System.out.println("database.properties not found");
            }
        }


        if (argumentParser.hasValue("-text")) {
            Path inputPath = argumentParser.getPath("-text");
            log.debug("Input: " + inputPath);
            try {
                if (invertedWordIndex instanceof ThreadSafeInvertedWordIndex) {
                    WordIndexBuilder.build(inputPath, (ThreadSafeInvertedWordIndex) invertedWordIndex, workQueue);
                } else {
                    WordIndexBuilder.build(inputPath, invertedWordIndex); /* populate wordIndex*/
                }
            } catch (IOException e) {
                System.out.println("IO Error while scanning directory: " + inputPath);
            }
        }

        if (argumentParser.hasValue("-query")) {
            Path queryPath = argumentParser.getPath("-query");
            try {
                queryFileHandler.parseQuery(queryPath, argumentParser.hasFlag("-exact"));
            } catch (IOException e) {
                System.out.println("IO Error while attempting to query: " + queryPath);
            }
        }

        if (argumentParser.hasFlag("-counts")) {
            Path countOutput = argumentParser.getPath("-counts", Path.of("counts.json"));
            try {
                invertedWordIndex.wordCountToJSON(countOutput);
            } catch (IOException e) {
                System.out.println("IO Error occurred while attempting to output the word count to: " + countOutput);
            }
        }

        if (argumentParser.hasFlag("-index")) {
            Path outputPath = argumentParser.getPath("-index", Path.of("index.json"));
            try (BufferedWriter bufWriter = Files.newBufferedWriter(outputPath, UTF_8)) {
                invertedWordIndex.toJSON(bufWriter, 0);
            } catch (IOException e) {
                System.out.println("IO Error occurred while attempting to output JSON to " + outputPath);
            }
        }

        if (argumentParser.hasFlag("-results")) {
            Path queryOutput = argumentParser.getPath("-results", Path.of("results.json"));
            try {
                queryFileHandler.resultsToJSON(queryOutput);
            } catch (IOException e) {
                System.out.println("IO Error occurred while attempting to output search results to: " + queryOutput);
            }
        }

        if (argumentParser.hasFlag("-server")) {
            int port = argumentParser.getInteger("-server", 8080);
            try {
                System.out.println("Starting server on: " + port);
                assert queryFileHandler instanceof ThreadSafeQueryFileHandler;
                SearchServer searchServer = new SearchServer((ThreadSafeQueryFileHandler) queryFileHandler, port);
                searchServer.start();
            } catch (Exception e) {
                System.out.println("Error occurred while attempting to start the server on port " + port);
            }
        }

        if (workQueue != null) {
            workQueue.join();
        }
        // calculate time elapsed and output
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
        log.debug("Elapsed: {} seconds", seconds);
    }
}
