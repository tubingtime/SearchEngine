package edu.usfca.cs272;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
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

	public Driver() {
	}

	/**
	 * Scans files and puts them into a provided wordIndex
	 */
	private void scan(ArrayList<Path> files, WordIndex wordIndex){
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
	}

	/**
	 * Create and populate list of files if it's a directory.
	 */
	private ArrayList<Path> scanDirectory(Path userPath){

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
		return files;
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
		ArgumentParser argumentParser = new ArgumentParser(args);
		WordIndex wordIndex = new WordIndex();

		System.out.println("Actual args: " + Arrays.toString(args));
		System.out.println("Parsed args: " + argumentParser);
		System.out.println("Path: " + argumentParser.getPath("-text"));
		Path userPath = argumentParser.getPath("-text");
		Path outPath = argumentParser.getPath("-index",Path.of("index.json"));
		if (userPath != null) {
			Driver driver = new Driver();
			ArrayList<Path> files = driver.scanDirectory(userPath);

			driver.scan(files, wordIndex); /* populate wordIndex*/
		}
		try (BufferedWriter bufWriter = Files.newBufferedWriter(outPath, UTF_8)) {
			wordIndex.toJSON(bufWriter,0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Output:" + outPath.toAbsolutePath().toString());


		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}
