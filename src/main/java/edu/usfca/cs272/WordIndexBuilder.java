package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class WordIndexBuilder {
    /**
     * Scans files and puts them into a provided wordIndex
     * @param files a list of files.
     * @param invertedWordIndex a {@link InvertedWordIndex} to store the words.
     */
    public static void scan(ArrayList<Path> files, InvertedWordIndex invertedWordIndex){
        for (Path file : files) {
            try { //first we parse and stem
                ArrayList<String> stems = WordCleaner.listStems(file);
                // ^^reads and stems line by line and inserts \n for new line
                int lineNumber = 1;
                for (String stem : stems) {
                    if (!stem.equals("\\n")) {
                        invertedWordIndex.add(stem, file, lineNumber++);
                    }
                }
            } catch (IOException e) {
                System.out.println("IO Error while stemming: " + file);
                return;
            }
        }
    }

}
