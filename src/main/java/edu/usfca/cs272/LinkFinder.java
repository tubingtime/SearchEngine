package edu.usfca.cs272;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Finds HTTP(S) URLs from the anchor tags within HTML code.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class LinkFinder {
    /**
     * Returns a list of all the valid HTTP(S) URLs found in the HREF attribute
     * of the anchor tags in the provided HTML. The URLs will be converted to
     * absolute using the base URL and normalized (removing fragments and encoding
     * special characters as necessary).
     * <p>
     * Any URLs that are unable to be properly parsed (throwing an
     * {@link MalformedURLException}) or that do not have the HTTP/S protocol will
     * not be included.
     *
     * @param base the base URL used to convert relative URLs to absolute3
     * @param html the raw HTML associated with the base URL
     * @param urls the data structure to store found HTTP(S) URLs
     * @see Pattern#compile(String)
     * @see Matcher#find()
     * @see Matcher#group(int)
     * @see #normalize(URL)
     * @see #isHttp(URL)
     */
    public static void findUrls(URL base, String html, Collection<URL> urls) {
        // regex... bleh! see: https://regex101.com/r/JjF7J1/3
        final String regex = "(?i)<a[^>]+?href\\s*=\\s*\"([^\"]+)";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            try {
                URL url = new URL(base, matcher.group(1));
                if (isHttp(url)) {
                    url = normalize(url);
                    urls.add(url);
                }
            } catch (MalformedURLException e) {
                System.out.println("Invalid URL:" + matcher.group(1));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Returns a list of all the valid HTTP(S) URLs found in the HREF attribute
     * of the anchor tags in the provided HTML.
     *
     * @param base the base URL used to convert relative URLs to absolute3
     * @param html the raw HTML associated with the base URL
     * @return list of all valid HTTP(S) URLs in the order they were found
     * @see #findUrls(URL, String, Collection)
     */
    public static ArrayList<URL> listUrls(URL base, String html) {
        ArrayList<URL> urls = new ArrayList<URL>();
        findUrls(base, html, urls);
        return urls;
    }

    /**
     * Returns a set of all the unique valid HTTP(S) URLs found in the HREF
     * attribute of the anchor tags in the provided HTML.
     *
     * @param base the base URL used to convert relative URLs to absolute3
     * @param html the raw HTML associated with the base URL
     * @return list of all valid HTTP(S) URLs in the order they were found
     * @see #findUrls(URL, String, Collection)
     */
    public static HashSet<URL> uniqueUrls(URL base, String html) {
        HashSet<URL> urls = new HashSet<URL>();
        findUrls(base, html, urls);
        return urls;
    }

    /**
     * Removes the fragment component of a URL (if present), and properly encodes
     * the query string (if necessary).
     *
     * @param url the URL to normalize
     * @return normalized URL
     * @throws URISyntaxException    if unable to craft new URI
     * @throws MalformedURLException if unable to craft new URL
     */
    public static URL normalize(URL url) throws MalformedURLException, URISyntaxException {
        return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
                url.getPort(), url.getPath(), url.getQuery(), null).toURL();
    }

    /**
     * Adds "index.html" to the end of a URL if it ends with /
     * For example google.com/ will turn into google.com/index.html
     * @param url the url to add "index.html" to
     * @return a url with "index.html" appended if it ends with /
     */
    public static URL addIndex(URL url) {
        if (url.getPath().endsWith("/")){
            try {
                url = new URL(String.join("",url.toString(), "index.html"));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return url;
    }

    /**
     * Determines whether the URL provided uses the HTTP or HTTPS protocol.
     *
     * @param url the URL to check
     * @return true if the URL uses the HTTP or HTTPS protocol
     */
    public static boolean isHttp(URL url) {
        return url.getProtocol().matches("(?i)https?");
    }
}
