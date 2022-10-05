package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Returns an Arraylist of text files found in a directory
 */
public class TextFileTraverser {

    /**
     * Create and populate list of files if it's a directory. Or just adds the file if not
     * Could make this recursive to reduce code
     *
     * @param userPath path given to Driver by user params
     * @return an {@link ArrayList} of files found
     * @throws IOException if an IO Exception occurs while scanning
     */
    public static ArrayList<Path> scanDirectory(Path userPath) throws IOException {
        ArrayList<Path> files = new ArrayList<>();

        if (Files.isDirectory(userPath)) {
            scanSubDirs(files, userPath);
        } else {
            files.add(userPath); // ^-^
        }

        return files;

    }

    /**
     * Recursive step for scanDirectory()
     *
     * @param files  the list of files to add new ones to
     * @param subdir the subdirectory to scan
     * @throws IOException if an IO Exception occurs while scanning
     */
    private static void scanSubDirs(ArrayList<Path> files, Path subdir) throws IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(subdir);
        for (Path file : stream) {
            if (Files.isDirectory(file)) {
                scanSubDirs(files, file);
            } else if (isTextFile(file)) {
                files.add(file); // ^-^
            }
        }
    }


    /**
     * Checks whether a file ends in .txt or .text (case insensitive)
     * @param path the file to check
     * @return true if it is a text file, false if not.
     */
    public static boolean isTextFile(Path path) {
        String fileName = path.toString().toUpperCase(); /* Maybe make substring with only last 4 chars? for efficiency */
        return fileName.endsWith(".TXT") || fileName.endsWith(".TEXT");
    }

}
