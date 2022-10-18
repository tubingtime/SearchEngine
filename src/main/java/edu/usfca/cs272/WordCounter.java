package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.TreeMap;

/**
 * Counts the words in a InvertedWordIndex
 */
public class WordCounter {
    // functional appreach would likeley need a non-inverted index
    // for every word
    // putIfAbsent location, [words].append(word)
    // get(location).append(word)

    /**
     * TreeMap to store how many word stems are in each file
     */
    private final TreeMap<String,Integer> wordCount;

    public WordCounter() {
        this.wordCount = new TreeMap<>();
    }

    /**
     * Increments a locations word count by one
     * @param location the location to increment
     */
    public void increment(String location){
        wordCount.putIfAbsent(location, 0);
        wordCount.put(location, wordCount.get(location)+1);
    }

    public void toJSON(Path path) throws IOException {
        PrettyJsonWriter.writeObject(wordCount, path);
    }
}
