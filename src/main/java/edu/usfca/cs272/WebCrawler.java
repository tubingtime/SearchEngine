package edu.usfca.cs272;


import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Multithreaded web crawler that will recursively crawl on links found inside html
 *
 * @author Thomas de Laveaga
 */
public class WebCrawler {

    /**
     * Lock object to allow for safe modification of maxUrls and crawledUrls.
     */
    private final Object urlLock;

    /**
     * Integer that stores the amount of URLs to crawl before stopping.
     */
    private int maxUrls;

    /**
     * Stores the already crawled URLs so that we don't crawl them again.
     */
    private final HashSet<URL> crawledUrls;

    /**
     * Creates a new instance of this class with a specified amount of URLs to crawl if
     * more are found from the seed url.
     *
     * @param maxUrls the maximum amount of URLs to crawl before stopping.
     */
    public WebCrawler(int maxUrls) {
        this.urlLock = new Object();
        this.maxUrls = maxUrls;
        this.crawledUrls = new HashSet<>();
    }

    /**
     * Starts a recursive web crawl using Sockets to download the page. Follows up to 3 redirects and up to
     * a specified amount of links found inside href tags.
     *
     * @param seed      the url to crawl, any links found inside will be crawled
     * @param index     the {@link ThreadSafeInvertedWordIndex} to add data to
     * @param workQueue A {@link WorkQueue} to distribute work to threads.
     * @throws MalformedURLException if the seed url is invalid
     */
    public void startCrawl(String seed, ThreadSafeInvertedWordIndex index, WorkQueue workQueue) throws MalformedURLException {
        URL seedUrl = new URL(seed);
        try { // todo: maybe unnecessary
            LinkFinder.normalize(seedUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        synchronized (urlLock) {
            crawledUrls.add(seedUrl);
        }
        crawl(seedUrl, index, workQueue);
        workQueue.finish();
    }

    /**
     * Starts a recursive web crawl using Sockets to download the page. Follows up to 3 redirects and up to
     * a specified amount of links found inside href tags.
     *
     * @param seed      the url to crawl, any links found inside will be crawled
     * @param index     the {@link ThreadSafeInvertedWordIndex} to add data to
     * @param workQueue A {@link WorkQueue} to distribute work to threads.
     * @throws MalformedURLException if the seed url is invalid
     */
    public void startCrawl(String seed, ThreadSafeInvertedWordIndex index, WorkQueue workQueue, Connection db)
            throws MalformedURLException {
        URL seedUrl = new URL(seed);
        try { // todo: maybe unnecessary
            LinkFinder.normalize(seedUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e); // todo:  maybe not throw
        }

        //add our seed url to already crawled urls list
        synchronized (urlLock) {
            crawledUrls.add(seedUrl);
        }

        //setup db tables if it doesn't already exist
        Path sql = Path.of("src", "main", "resources", "sql", "CREATE_TABLE.sql");
        try {
            String sqlCreateTable = Files.readString(sql, StandardCharsets.UTF_8);
            try (PreparedStatement statement = db.prepareStatement(sqlCreateTable)) {
                ResultSet resultSet = statement.executeQuery();
                System.out.println(resultSet.toString()); // todo: log?
            } catch (SQLException e) {
                System.out.println("SQL Exception!");//todo: log
            }
        } catch (IOException e) {
            throw new RuntimeException(e); //todo: log
        }

        crawl(seedUrl, index, workQueue, db);
        workQueue.finish();
    }

    /**
     * Uses Sockets to download a webpage if it's content type is text/html. After stripping block elements, it
     * looks for links inside href tags. We create a new {@link CrawlTask} if the link is: it's a valid URL,
     * it hasn't already been crawled,
     * and we haven't reached our maxUrls crawled.
     *
     * @param url       the url to crawl
     * @param index     the index to add data to
     * @param workQueue a {@link WorkQueue} to handle the execution of {@link CrawlTask}
     */
    private void crawl(URL url, ThreadSafeInvertedWordIndex index, WorkQueue workQueue) {
        String html = HtmlFetcher.fetch(url, 3);
        if (html == null) {
            return; // unable to find resource or is not html
        }
        html = HtmlCleaner.stripBlockElements(html);

        // Find links
        ArrayList<URL> urls = new ArrayList<>();
        LinkFinder.findUrls(url, html, urls); //todo: could save some time by only finding enough urls to satisfy maxURLs
        ArrayList<CrawlTask> crawlTasks = new ArrayList<>();
        synchronized (urlLock) {
            Iterator<URL> urlsIterator = urls.iterator();
            while (urlsIterator.hasNext() && this.maxUrls > 1) {
                URL foundURL = urlsIterator.next();
                if (!crawledUrls.contains(foundURL)) {
                    crawlTasks.add(new CrawlTask(foundURL, index, workQueue, this));
                    crawledUrls.add(foundURL);
                    maxUrls--;
                }
            }
        }
        for (CrawlTask crawlTask : crawlTasks) {
            workQueue.execute(crawlTask);
        }

        // Continue processing HTML from current link
        html = HtmlCleaner.stripTags(html);
        html = HtmlCleaner.stripEntities(html);

        WordIndexBuilder.scanText(html, url.toString(), index);
    }

    /**
     * Uses Sockets to download a webpage if it's content type is text/html. After stripping block elements, it
     * looks for links inside href tags. We create a new {@link CrawlTask} if the link is: it's a valid URL,
     * it hasn't already been crawled,
     * and we haven't reached our maxUrls crawled. // todo; update javadoc
     *
     * @param url       the url to crawl
     * @param index     the index to add data to
     * @param workQueue a {@link WorkQueue} to handle the execution of {@link CrawlTask}
     */
    private void crawl(URL url, ThreadSafeInvertedWordIndex index, WorkQueue workQueue, Connection db) {
        Map<String, List<String>> headers = new HashMap<>();
        String html = HtmlFetcher.fetch(url, 3, headers);
        if (html == null) {
            return; // unable to find resource or is not html
        }
        String title = HtmlCleaner.getTitle(html);
        html = HtmlCleaner.stripBlockElements(html);

        // Find links
        ArrayList<URL> urls = new ArrayList<>();
        LinkFinder.findUrls(url, html, urls); //todo: could save some time by only finding enough urls to satisfy maxURLs
        ArrayList<CrawlTask> crawlTasks = new ArrayList<>();
        synchronized (urlLock) {
            Iterator<URL> urlsIterator = urls.iterator();
            while (urlsIterator.hasNext() && this.maxUrls > 1) {
                URL foundURL = urlsIterator.next();
                if (!crawledUrls.contains(foundURL)) {
                    crawlTasks.add(new CrawlTask(foundURL, index, workQueue, this, db));
                    crawledUrls.add(foundURL);
                    maxUrls--;
                }
            }
        }
        for (CrawlTask crawlTask : crawlTasks) {
            workQueue.execute(crawlTask);
        }

        // Continue processing HTML from current link
        html = HtmlCleaner.stripTags(html);
        html = HtmlCleaner.stripEntities(html);

        WordIndexBuilder.scanText(html, url.toString(), index);

        // get metadata and store in db
        String snippet;
        if (html.length() > 200) {
            snippet = html.substring(0, 200);
        }
        snippet = String.join("",WordCleaner.splitAndGetSubString(html, 0, 15));

        snippet = StringEscapeUtils.escapeHtml4(snippet);

        if (title != null) {
            title = StringEscapeUtils.escapeHtml4(title);
            if (title.length() > 30) {
                title = String.join("", WordCleaner.splitAndGetSubString(title, 0, 6), "...");
            }
        } else {
            title = "No title";
        }

        List<String> contentLengthHeader = headers.get("Content-Length");
        Integer contentLength = (contentLengthHeader == null) ? -1 : Integer.parseInt(contentLengthHeader.get(0));
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO crawler_stats");
        sql.append(System.lineSeparator());
        sql.append("VALUES (");
        sql.append("\"").append(url).append("\", ");
        sql.append("\"").append(snippet).append("\", ");
        sql.append("\"").append(title).append("\", ");
        sql.append("\"").append(contentLength).append("\", ");
        sql.append("CURRENT_TIMESTAMP").append(")");
        sql.append(";");

        try (PreparedStatement statement = db.prepareStatement(sql.toString())) {
            ResultSet resultSet = statement.executeQuery();
            System.out.println(resultSet.toString());
        } catch (SQLException e) {
            System.out.println("SQL Exception!");//todo: log
        }


    }

    /**
     * A task that calls crawl
     */
    private static class CrawlTask implements Runnable {

        /**
         * The url to crawl
         */
        private final URL url;

        /**
         * The index to add data to
         */
        private final ThreadSafeInvertedWordIndex index;

        /**
         * The workQueue to add more CrawlTasks to
         */
        private final WorkQueue workQueue;

        /**
         * A webcrawler that holds the maxUrls and crawledUrls
         */
        private final WebCrawler crawler;

        private final Connection db;
        /**
         * Constructs a new CrawlTask
         *
         * @param url       The url to crawl
         * @param index     The workQueue to add more CrawlTasks to
         * @param workQueue The workQueue to add more CrawlTasks to
         * @param crawler   A webcrawler that holds the maxUrls and crawledUrls
         */
        public CrawlTask(URL url, ThreadSafeInvertedWordIndex index, WorkQueue workQueue, WebCrawler crawler) {
            this.url = url;
            this.index = index;
            this.workQueue = workQueue;
            this.crawler = crawler;
            this.db = null;
        }

        public CrawlTask(URL url, ThreadSafeInvertedWordIndex index,
                         WorkQueue workQueue, WebCrawler crawler, Connection db
        ) {
            this.url = url;
            this.index = index;
            this.workQueue = workQueue;
            this.crawler = crawler;
            this.db = db;
        }

        @Override
        public void run() {
            if (db == null) {
                crawler.crawl(url, index, workQueue);
            } else {
                crawler.crawl(url, index, workQueue, db);
            }
        }
    }
}
