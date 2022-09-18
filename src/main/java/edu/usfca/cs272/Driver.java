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

	/**
	 * Makes  new instance of driver so it's functions can be used
	 */
	public Driver() {
	}

	/**
	 * Scans files and puts them into a provided wordIndex
	 * @param files a list of files.
	 * @param wordIndex a {@link WordIndex} to store the words.
	 */
	private void scan(ArrayList<Path> files, WordIndex wordIndex){
		for (Path file : files) {
			try { //first we parse and stem
				ArrayList<String> stems = WordCleaner.listStems(file);
				// ^^reads and stems line by line and inserts \n for new line
				int lineNumber = 1;
				for (String stem : stems) {
					if (!stem.equals("\\n")) {
						wordIndex.add(stem, file, lineNumber++);
					}
				}
			} catch (IOException e) {
				System.out.println("IO Error while stemming: " + file);
				return;
			}
		}
	}

	/**
	 * Create and populate list of files if it's a directory. Or just adds the file if not
	 * Could make this recursive to reduce code
	 * @param userPath path given to Driver by user params
	 * @return an {@link ArrayList} of files found
	 */
	private ArrayList<Path> scanDirectory(Path userPath){

		ArrayList<Path> files = new ArrayList<>();
		if (Files.isDirectory(userPath)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(userPath)) {
				for (Path file : stream) {
					String fileName = file.toString().toUpperCase(); /* Maybe make substring with only last 4 chars? for efficiency */
					if (Files.isDirectory(file)){
						scanSubDirs(files,file);
					}
					else if (fileName.endsWith(".TXT") || fileName.endsWith(".TEXT")) {
						files.add(file); // ^-^
					}
				}
			} catch (IOException | DirectoryIteratorException x) {
				System.out.println("IO Error while scanning directory: " + userPath);
			}
		} else { files.add(userPath); } /* need to check if ends in .txt ? */
		return files;
	}

	/**
	 * Recursive step for scanDirectory()
	 * @param files the list of files to add new ones to
	 * @param subdir the subdirectory to scan
	 */
	private void scanSubDirs(ArrayList<Path> files, Path subdir){
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(subdir)) {
			for (Path file : stream) {
				System.out.println("Paths: " + file.getFileName());
				String fileName = file.toString().toUpperCase(); /* Maybe make substring with only last 4 chars? for efficiency */
				if (Files.isDirectory(file)){
					scanSubDirs(files,file);
				}
				else if (fileName.endsWith(".TXT") || fileName.endsWith(".TEXT")) {
					files.add(file); // ^-^
				}
			}
		} catch (IOException x) {
			System.out.println("IO Error while scanning directory: " + subdir);
		}
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
		if (argumentParser.hasFlag("-index")) {
			try (BufferedWriter bufWriter = Files.newBufferedWriter(outPath, UTF_8)) {
				wordIndex.toJSON(bufWriter, 0);
			} catch (IOException e) {
				System.out.println("IO Error occurred while attempting to output JSON to " + outPath);
			}
		}
		System.out.println("Output:" + outPath.toAbsolutePath());

		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}
