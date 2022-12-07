package edu.usfca.cs272;


import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class WebCrawler {

    private final Object maxLock; //todo: rename?

    private int max;

    private HashSet<URL> crawledUrls;

    public WebCrawler(int max) {
        this.maxLock = new Object();
        this.max = max;
        this.crawledUrls = new HashSet<>();
    }

    public void startCrawl (String seed, ThreadSafeInvertedWordIndex index, WorkQueue workQueue) throws MalformedURLException {
        URL seedUrl = new URL(seed);
        try { // todo: maybe unnecessary
            LinkFinder.normalize(seedUrl);
            if (seedUrl.getPath().endsWith("/")){
                System.out.println(String.join("",seedUrl.toString(), "index.html"));
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        synchronized (maxLock) {
            crawledUrls.add(seedUrl);
        }
        crawl(seedUrl, index, workQueue);
        workQueue.finish();
    }

    public void crawl (URL url, ThreadSafeInvertedWordIndex index, WorkQueue workQueue) {
        String html = HtmlFetcher.fetch(url, 3);
        if (html == null){
            return; // unable to find resource or is not html
        }
        html = HtmlCleaner.stripBlockElements(html);

        // Find links
        ArrayList<URL> urls = new ArrayList<>();
        LinkFinder.findUrls(url, html, urls);
        ArrayList<CrawlTask> crawlTasks = new ArrayList<>();
        synchronized (maxLock) {
            for (URL foundURL : urls) {
                if (this.max <= 1) {
                    break; // todo: could make this a while loop for simplification
                }
                if (!crawledUrls.contains(foundURL) && !crawledUrls.contains(LinkFinder.addIndex(foundURL))) {
                    crawlTasks.add(new CrawlTask(foundURL, index, workQueue, this));
                    crawledUrls.add(foundURL);
                    max--;
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




    private static class CrawlTask implements Runnable {
        private final URL url;

        private final ThreadSafeInvertedWordIndex index;

        private final WorkQueue workQueue;

        private final WebCrawler crawler;

        public CrawlTask (URL url, ThreadSafeInvertedWordIndex index, WorkQueue workQueue, WebCrawler crawler) {
            this.url = url;
            this.index = index;
            this.workQueue = workQueue;
            this.crawler = crawler;
        }
        /**
         * When an object implementing interface {@code Runnable} is used
         * to create a thread, starting the thread causes the object's
         * {@code run} method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method {@code run} is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            crawler.crawl(url, index, workQueue);
        }
    }
}
