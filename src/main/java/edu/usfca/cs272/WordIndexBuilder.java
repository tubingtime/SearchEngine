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
     * Scans files and puts them into a provided wordIndex
     *
     * @param files a list of files.
     * @param index a {@link InvertedWordIndex} to store the words.
     * @throws IOException if listStems throws an IOException while parsing
     */
    public static void scan(ArrayList<Path> files, InvertedWordIndex index) throws IOException {
        for (Path file : files) {
            //first we parse and stem
            ArrayList<String> stems = WordCleaner.listStems(file);
            // ^^reads and stems line by line and inserts \n for new line
            int lineNumber = 1;
            for (String stem : stems) {
                if (!stem.equals("\\n")) {
                    index.add(stem, file.toString(), lineNumber++);
                }
            }
        }
    }


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
            String fileString = file.toString();
            int lineNumber = 1;
            String[] parsedLine;
            String line;
            while ((line = reader.readLine()) != null) {
                parsedLine = WordCleaner.parse(line);
                for (String word : parsedLine) {
                    index.add(stemmer.stem(word).toString(), fileString, lineNumber++);
                }
            }
        }
/*    	adding to the index
    	
    	copy/paste logic from WordCleaner
    	
    	open a buffered reader, ready line by line, parse, stem, then add directly to the index*/
    }

}
