package edu.usfca.cs272;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;

/**
 * Utility class for parsing, cleaning, and stemming text and text files into
 * collections of processed words.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class WordCleaner {
    /**
     * Regular expression that matches any whitespace.
     **/
    public static final Pattern SPLIT_REGEX = Pattern.compile("(?U)\\p{Space}+");

    /**
     * Regular expression that matches non-alphabetic characters.
     **/
    public static final Pattern CLEAN_REGEX = Pattern
            .compile("(?U)[^\\p{Alpha}\\p{Space}]+");

    /**
     * Default stemmer to use incase user doesn't provide one
     **/
    public static final Stemmer defaultStemmer = new SnowballStemmer(ENGLISH);

    /**
     * Cleans the text by removing any non-alphabetic characters (e.g. non-letters
     * like digits, punctuation, symbols, and diacritical marks like the umlaut)
     * and converting the remaining characters to lowercase.
     *
     * @param text the text to clean
     * @return cleaned text
     */
    public static String clean(String text) {
        String cleaned = Normalizer.normalize(text, Normalizer.Form.NFD);
        cleaned = CLEAN_REGEX.matcher(cleaned).replaceAll("");
        return cleaned.toLowerCase();
    }

    /**
     * Splits the supplied text by whitespaces.
     *
     * @param text the text to split
     * @return an array of {@link String} objects
     */
    public static String[] split(String text) {
        return text.isBlank() ? new String[0] : SPLIT_REGEX.split(text.strip());
    }

    /**
     * Parses the text into an array of clean words.
     *
     * @param text the text to clean and split
     * @return an array of {@link String} objects
     * @see #clean(String)
     * @see #parse(String)
     */
    public static String[] parse(String text) {
        return split(clean(text));
    }

    /**
     * Parses the line into cleaned and stemmed words and adds them to the
     * provided collection.
     *
     * @param line    the line of words to clean, split, and stem
     * @param stemmer the stemmer to use
     * @param stems   the collection to add stems
     * @see #parse(String)
     * @see Stemmer#stem(CharSequence)
     * @see Collection#add(Object)
     */
    public static void addStems(String line, Stemmer stemmer, Collection<String> stems) {
        String[] wordList = parse(line);
        for (String word : wordList) {
            stems.add(stemmer.stem(word).toString());
        }
    }

    /**
     * Parses the line into a list of cleaned and stemmed words.
     *
     * @param line    the line of words to clean, split, and stem
     * @param stemmer the stemmer to use
     * @return a list of cleaned and stemmed words in parsed order
     * @see #parse(String)
     * @see Stemmer#stem(CharSequence)
     * @see #addStems(String, Stemmer, Collection)
     */
    public static ArrayList<String> listStems(String line, Stemmer stemmer) {
        ArrayList<String> stems = new ArrayList<>();
        addStems(line, stemmer, stems);
        return stems;
    }

    /**
     * Parses the line into a list of cleaned and stemmed words using the default
     * stemmer for English.
     *
     * @param line the line of words to parse and stem
     * @return a list of cleaned and stemmed words in parsed order
     * @see SnowballStemmer#SnowballStemmer(ALGORITHM)
     * @see ALGORITHM#ENGLISH
     * @see #listStems(String, Stemmer)
     */
    public static ArrayList<String> listStems(String line) {
        ArrayList<String> stems = new ArrayList<>();
        addStems(line, defaultStemmer, stems);
        return stems;
    }

    /**
     * Reads a file line by line, parses each line into cleaned and stemmed words
     * using the default stemmer for English.
     *
     * @param input the input file to parse and stem
     * @return a list of stems from file in parsed order, and a "\n" for every new line
     * @throws IOException if unable to read or parse file
     * @see SnowballStemmer
     * @see ALGORITHM#ENGLISH
     * @see StandardCharsets#UTF_8
     * @see #listStems(String, Stemmer)
     */
    public static ArrayList<String> listStems(Path input) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(input, UTF_8)) {
            String line;
            SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);
            ArrayList<String> stems = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                addStems(line, stemmer, stems);
            }
            return stems;
        }
    }

    /**
     * Parses the line into a set of unique, sorted, cleaned, and stemmed words.
     * Not thread safe! need to create new stemmer for each new thread?
     *
     * @param line    the line of words to parse and stem
     * @param stemmer the stemmer to use
     * @return a sorted set of unique cleaned and stemmed words
     * @see #parse(String)
     * @see Stemmer#stem(CharSequence)
     * @see #addStems(String, Stemmer, Collection)
     */
    public static TreeSet<String> uniqueStems(String line, Stemmer stemmer) {
        TreeSet<String> stems = new TreeSet<>();
        addStems(line, stemmer, stems);
        return stems;
    }

    /**
     * Parses the line into a set of unique, sorted, cleaned, and stemmed words
     * using the default stemmer for English.
     *
     * @param line the line of words to parse and stem
     * @return a sorted set of unique cleaned and stemmed words
     * @see SnowballStemmer#SnowballStemmer(ALGORITHM)
     * @see ALGORITHM#ENGLISH
     * @see #uniqueStems(String, Stemmer)
     */
    public static TreeSet<String> uniqueStems(String line) {
        TreeSet<String> stems = new TreeSet<>();
        addStems(line, defaultStemmer, stems);
        return stems;
    }

    /**
     * Reads a file line by line, parses each line into a set of unique, sorted,
     * cleaned, and stemmed words using the default stemmer for English.
     *
     * @param input the input file to parse and stem
     * @return a sorted set of unique cleaned and stemmed words from file
     * @throws IOException if unable to read or parse file
     * @see SnowballStemmer
     * @see ALGORITHM#ENGLISH
     * @see StandardCharsets#UTF_8
     * @see #uniqueStems(String, Stemmer)
     */
    public static TreeSet<String> uniqueStems(Path input) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(input, UTF_8)) {
            String line;
            TreeSet<String> stems = new TreeSet<>();                //this line only diff between this and
            SnowballStemmer stemmer = new SnowballStemmer(ENGLISH); //uniqueStems ArrayList. can be condense
            while ((line = reader.readLine()) != null) {
                addStems(line, stemmer, stems);
            }
            return stems;
        }
    }

    /**
     * Reads a file line by line, parses each line into unique, sorted, cleaned,
     * and stemmed words using the default stemmer for English, and adds the set
     * of unique sorted stems to a list per line in the file.
     *
     * @param input the input file to parse and stem
     * @return a list where each item is the sets of unique sorted stems parsed
     * from a single line of the input file
     * @throws IOException if unable to read or parse file
     * @see SnowballStemmer
     * @see ALGORITHM#ENGLISH
     * @see StandardCharsets#UTF_8
     * @see #uniqueStems(String, Stemmer)
     */
    public static ArrayList<TreeSet<String>> listUniqueStems(Path input) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(input, UTF_8)) {
            String line;
            ArrayList<TreeSet<String>> uniqueStems = new ArrayList<>();
            SnowballStemmer stemmer = new SnowballStemmer(ENGLISH);
            while ((line = reader.readLine()) != null) {
                uniqueStems.add(uniqueStems(line, stemmer));
            }
            return uniqueStems;
        }
    }
}
