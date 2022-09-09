package edu.usfca.cs272;

import java.time.Duration;
import java.time.Instant;
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


		if (args.length < 1) {
			args = new String[] {
					"-max", "false", "-min", "0", "-min", "-10", "hello", "-@debug", "-f",
					"output.txt", "-verbose" };
		}
		ArgumentParser argumentParser = new ArgumentParser(args);

		System.out.println(Arrays.toString(args));
		System.out.println(argumentParser.toString());




		// calculate time elapsed and output
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}
