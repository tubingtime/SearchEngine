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
     */
    public static ArrayList<Path> scanDirectory(Path userPath) throws IOException { // TODO throw exception, handle in Driver instead
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
     */
    private static void scanSubDirs(ArrayList<Path> files, Path subdir) throws IOException { // TODO throw the exception instead
        DirectoryStream<Path> stream = Files.newDirectoryStream(subdir);
        for (Path file : stream) {
            if (Files.isDirectory(file)) {
                scanSubDirs(files, file);
            } else if (isTextFile(file)) {
                files.add(file); // ^-^
            }
        }
    }


    public static boolean isTextFile(Path path) {
        String fileName = path.toString().toUpperCase(); /* Maybe make substring with only last 4 chars? for efficiency */
        return fileName.endsWith(".TXT") || fileName.endsWith(".TEXT");
    }

}
