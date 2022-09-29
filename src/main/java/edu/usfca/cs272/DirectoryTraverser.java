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
public class DirectoryTraverser {


    /**
     * Create and populate list of files if it's a directory. Or just adds the file if not
     * Could make this recursive to reduce code
     *
     * @param userPath path given to Driver by user params
     * @return an {@link ArrayList} of files found
     */
    public ArrayList<Path> scanDirectory(Path userPath) {

        ArrayList<Path> files = new ArrayList<>();
        if (Files.isDirectory(userPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(userPath)) {
                for (Path file : stream) {
                    String fileName = file.toString().toUpperCase(); /* Maybe make substring with only last 4 chars? for efficiency */
                    if (Files.isDirectory(file)) {
                        scanSubDirs(files, file);
                    } else if (fileName.endsWith(".TXT") || fileName.endsWith(".TEXT")) {
                        files.add(file); // ^-^
                    }
                }
            } catch (IOException | DirectoryIteratorException x) {
                System.out.println("IO Error while scanning directory: " + userPath);
            }
        } else {
            files.add(userPath);
        } /* need to check if ends in .txt ? */
        return files;
    }

    /**
     * Recursive step for scanDirectory()
     *
     * @param files  the list of files to add new ones to
     * @param subdir the subdirectory to scan
     */
    public void scanSubDirs(ArrayList<Path> files, Path subdir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(subdir)) {
            for (Path file : stream) {
                System.out.println("Paths: " + file.getFileName());
                String fileName = file.toString().toUpperCase(); /* Maybe make substring with only last 4 chars? for efficiency */
                if (Files.isDirectory(file)) {
                    scanSubDirs(files, file);
                } else if (fileName.endsWith(".TXT") || fileName.endsWith(".TEXT")) {
                    files.add(file); // ^-^
                }
            }
        } catch (IOException x) {
            System.out.println("IO Error while scanning directory: " + subdir);
        }
    }

}
