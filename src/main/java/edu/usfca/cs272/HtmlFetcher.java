package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * A specialized version of {@link HttpsFetcher} that follows redirects and
 * returns HTML content if possible.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 * @see HttpsFetcher
 */
public class HtmlFetcher {
    /**
     * Returns {@code true} if and only if there is a "Content-Type" header and
     * the first value of that header starts with the value "text/html"
     * (case-insensitive).
     *
     * @param headers the HTTP/1.1 headers to parse
     * @return {@code true} if the headers indicate the content type is HTML
     */
    public static boolean isHtml(Map<String, List<String>> headers) {
        List<String> contentType = headers.get("Content-Type");
		return contentType.get(0).startsWith("text/html");
	}

    /**
     * Parses the HTTP status code from the provided HTTP headers, assuming the
     * status line is stored under the {@code null} key.
     *
     * @param headers the HTTP/1.1 headers to parse
     * @return the HTTP status code or -1 if unable to parse for any reasons
     */
    public static int getStatusCode(Map<String, List<String>> headers) {
        List<String> statusCode = headers.get(null);
        return Integer.parseInt(statusCode.get(0).substring(9, 12));
    }

    /**
     * Returns {@code true} if and only if the HTTP status code is between 300 and
     * 399 (inclusive) and there is a "Location" header with at least one value.
     *
     * @param headers the HTTP/1.1 headers to parse
     * @return {@code true} if the headers indicate the content type is HTML
     */
    public static boolean isRedirect(Map<String, List<String>> headers) {
        int statusCode = getStatusCode(headers);
        List<String> locationHeader = headers.get("Location");
        return (statusCode >= 300 && statusCode < 400)
                && (locationHeader != null)
                && (locationHeader.size() > 0);
    }

    /**
     * Fetches the resource at the URL using HTTP/1.1 and sockets. If the status
     * code is 200 and the content type is HTML, returns the HTML as a single
     * string. If the status code is a valid redirect, will follow that redirect
     * if the number of redirects is greater than 0. Otherwise, returns
     * {@code null}.
     *
     * @param url       the url to fetch
     * @param redirects the number of times to follow redirects
     * @return the html or {@code null} if unable to fetch the resource or the
     * resource is not html
     * @see HttpsFetcher#openConnection(URL)
     * @see HttpsFetcher#printGetRequest(PrintWriter, URL)
     * @see HttpsFetcher#getHeaderFields(BufferedReader)
     * @see String#join(CharSequence, CharSequence...)
     * @see #isHtml(Map)
     * @see #isRedirect(Map)
     */
    public static String fetch(URL url, int redirects) {
        String html = null;
        try (
                Socket socket = HttpsFetcher.openConnection(url);
                PrintWriter request = new PrintWriter(socket.getOutputStream());
                InputStreamReader input = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader response = new BufferedReader(input)
		) {
            // make http GET request of the web server
            HttpsFetcher.printGetRequest(request, url);

            // the headers will be first in the response
            Map<String, List<String>> headers = HttpsFetcher.getHeaderFields(response);
            if (!isHtml(headers)) {
                return null;
            }
            if (isRedirect(headers) && redirects > 0) {
                URL redirection = new URL(headers.get("Location").get(0));
                return fetch(redirection, redirects - 1);
            }
            if (getStatusCode(headers) != 200) {
                return null;
            }
            // ELSE
            html = response.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            html = null;
        }

        return html;
    }

    /**
     * Converts the {@link String} url into a {@link URL} object and then calls
     * {@link #fetch(URL, int)}.
     *
     * @param url       the url to fetch
     * @param redirects the number of times to follow redirects
     * @return the html or {@code null} if unable to fetch the resource or the
     * resource is not html
     * @see #fetch(URL, int)
     */
    public static String fetch(String url, int redirects) {
        try {
            return fetch(new URL(url), redirects);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Converts the {@link String} url into a {@link URL} object and then calls
     * {@link #fetch(URL, int)} with 0 redirects.
     *
     * @param url the url to fetch
     * @return the html or {@code null} if unable to fetch the resource or the
     * resource is not html
     * @see #fetch(URL, int)
     */
    public static String fetch(String url) {
        return fetch(url, 0);
    }

    /**
     * Calls {@link #fetch(URL, int)} with 0 redirects.
     *
     * @param url the url to fetch
     * @return the html or {@code null} if unable to fetch the resource or the
     * resource is not html
     */
    public static String fetch(URL url) {
        return fetch(url, 0);
    }
}
