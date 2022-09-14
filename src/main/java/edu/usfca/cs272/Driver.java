package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

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
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();
		ArgumentParser argumentParser = new ArgumentParser(args);

		System.out.println("Actual args: " + Arrays.toString(args));
		System.out.println("Parsed args: " + argumentParser);
		System.out.println("Path: " + argumentParser.getPath("-text"));
		Path userPath = argumentParser.getPath("-text");


		/*			Create and populate list of files if it's a directory.		*/
		ArrayList<Path> files = new ArrayList<Path>();
		if (Files.isDirectory(userPath)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(userPath)) {
				for (Path file : stream) {
					System.out.println("Paths: " + file.getFileName());
					if (file.endsWith(".txt") || file.endsWith(".text")) {
						files.add(file); // ^-^
					}
				}
			} catch (IOException | DirectoryIteratorException x) {
				System.err.println(x);
			}
		} else { files.add(userPath); }
		WordIndex wordIndex = new WordIndex();
		for (Path file : files) {
			try { //first we parse and stem
				ArrayList<String> stems = WordCleaner.listStems(file);
				// ^^reads and stems line by line and inserts \n for new line
				int lineNumber = 0;
				for (String stem : stems) {
					if (stem.equals("\\n")) {
						lineNumber++;
					} else {
						wordIndex.add(stem,file,lineNumber);
					}

				}
			} catch (Exception e) {
				System.out.println(e);
				return;
			}
		}



		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}
