package edu.usfca.cs272;

public class SearchResult implements Comparable<SearchResult> {

    /**
     * How many times the word stem was found
     */
    public final long count;

    /**
     * score = total matches / total words in file
     */
    public final double score;

    /**
     * What file the search was preformed on
     */
    public final String where;

    /**
     * Constructs a new instance of this class
     * @param count How many times the word stem was found
     * @param score Total matches / Total words in file
     * @param where What file the search was preformed on
     */
    public SearchResult(long count, double score, String where) {
        this.count = count;
        this.score = score;
        this.where = where;
    }

    @Override
    public int compareTo(SearchResult other) {
        int result = Double.compare(other.score, this.score);
        if (result == 0) {
            result = Long.compare(other.count, this.count);
            if (result == 0) {
                result = this.where.compareToIgnoreCase(other.where);
            }
        }
        return result;
    }
}
