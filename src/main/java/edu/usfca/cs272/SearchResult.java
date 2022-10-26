package edu.usfca.cs272;

public class SearchResult implements Comparable<SearchResult> {

    public final long count;

    public final double score;

    public final String where;

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
