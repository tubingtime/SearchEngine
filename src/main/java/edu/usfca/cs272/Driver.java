package edu.usfca.cs272;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

	// TODO Remove the constructor
    /**
     * Makes  new instance of driver so it's functions can be used
     */
    public Driver() {
    }

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
        InvertedWordIndex invertedWordIndex = new InvertedWordIndex();

        System.out.println("Actual args: " + Arrays.toString(args));
        System.out.println("Parsed args: " + argumentParser);
        System.out.println("Path: " + argumentParser.getPath("-text")); // TODO Move inside of the -text block

        Path inputPath = argumentParser.getPath("-text"); // TODO Move inside of -text block
        Path outputPath = argumentParser.getPath("-index", Path.of("index.json")); // TODO Move inside of -index block
        
        if (inputPath != null) {
        	// TODO There should be exceptions being caught here!
            try {
                ArrayList<Path> files = TextFileTraverser.scanDirectory(inputPath); /* scan directory */
                WordIndexBuilder.scan(files, invertedWordIndex); /* populate wordIndex*/
            } catch (IOException e){
                System.out.println("IO Error while scanning directory: " + inputPath );
            }

        }
        
        if (argumentParser.hasFlag("-index")) {
            try (BufferedWriter bufWriter = Files.newBufferedWriter(outputPath, UTF_8)) {
                invertedWordIndex.toJSON(bufWriter, 0);
            } catch (IOException e) {
                System.out.println("IO Error occurred while attempting to output JSON to " + outputPath);
            }
        }
        System.out.println("Output:" + outputPath.toAbsolutePath()); // TODO Move this inside of the -index if block

        // calculate time elapsed and output
        long elapsed = Duration.between(start, Instant.now()).toMillis();
        double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
        System.out.printf("Elapsed: %f seconds%n", seconds);
    }
}
