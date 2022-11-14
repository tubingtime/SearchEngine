package edu.usfca.cs272;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        ArgumentParser argumentParser = new ArgumentParser(args); /* parse args */
        log.debug("Actual args: {}", Arrays.toString(args));
        log.debug("Parsed args: {}", argumentParser);

        boolean multiThreaded = true;

        InvertedWordIndex invertedWordIndex = new InvertedWordIndex();
        if (multiThreaded){
            invertedWordIndex = new ThreadSafeInvertedWordIndex();
        }
        else {
            invertedWordIndex = new InvertedWordIndex();
        }

        QueryFileHandler queryFileHandler = new QueryFileHandler(invertedWordIndex);


        if (argumentParser.hasValue("-text")) {
            Path inputPath = argumentParser.getPath("-text");
            log.debug("Input: " + inputPath);
            try {
                WordIndexBuilder.build(inputPath, invertedWordIndex); /* populate wordIndex*/
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

        // calculate time elapsed and output
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
        log.debug("Elapsed: {} seconds", seconds);
    }
}
