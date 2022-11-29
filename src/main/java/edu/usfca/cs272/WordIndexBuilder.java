package edu.usfca.cs272;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * A class that builds a WordIndex given an ArrayList of files
 */
public class WordIndexBuilder {

    /**
     * Builds a provided InvertedWordIndex from a path to a file or directory
     *
     * @param start file or directory containing the words
     * @param index a {@link InvertedWordIndex} to store the words.
     * @throws IOException if listStems throws an IOException while parsing
     */
    public static void build(Path start, InvertedWordIndex index) throws IOException {
        ArrayList<Path> files = TextFileTraverser.scanDirectory(start);
        for (Path file : files) {
            scanFile(file, index);
        }
    }

    public static void build(Path start, ThreadSafeInvertedWordIndex index, WorkQueue workQueue) throws IOException {
        ArrayList<Path> files = TextFileTraverser.scanDirectory(start);
        for (Path file : files) {
            workQueue.execute(new ScannerTask(file, index));
        }
        workQueue.finish();

    }

    /**
     * Scans a single text file and puts the words into an InvertedWordIndex
     *
     * @param file  path to a single text file
     * @param index a {@link InvertedWordIndex} to store the words.
     * @throws IOException if the buffered reader throws an IOException
     */
    public static void scanFile(Path file, InvertedWordIndex index) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, UTF_8)) {
            SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);
            String fileName = file.toString();
            int position = 1;
            String[] parsedLine;
            String line;
            while ((line = reader.readLine()) != null) {
                parsedLine = WordCleaner.parse(line);
                for (String word : parsedLine) {
                    index.add(stemmer.stem(word).toString(), fileName, position++);
                }
            }
        }
    }

    /**
     * Task to scan a single text file and puts the words into the wordIndex
     */
    private static class ScannerTask implements Runnable {

        /**
         * The Path to scan
         */
        private final Path file;

        /**
         * The index to put words into
         */
        private final ThreadSafeInvertedWordIndex index;

        /**
         * Constructs a new instance of this class
         *
         * @param file  The Path to scan
         * @param index The index to put words into
         */
        private ScannerTask(Path file, ThreadSafeInvertedWordIndex index) {
            this.file = file;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                InvertedWordIndex localIndex = new InvertedWordIndex();
                scanFile(file, localIndex);
                index.addAll(localIndex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
