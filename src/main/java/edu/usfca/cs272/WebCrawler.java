package edu.usfca.cs272;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class WebCrawler {

    private final Object maxLock;

    private final int max;

    public WebCrawler(Object maxLock, int max) {
        this.maxLock = maxLock;
        this.max = max;
    }

    public void crawl (URL url, ThreadSafeInvertedWordIndex index, WorkQueue workQueue, int max)
            throws MalformedURLException {
        String html = HtmlFetcher.fetch(url);
        if (html == null){
            return;
        }
        html = HtmlCleaner.stripBlockElements(html);
        ArrayList<URL> urls = LinkFinder.listUrls(url, html);
        ArrayList<CrawlTask> crawlTasks = new ArrayList<>();
        synchronized (maxLock) {
            for (URL foundURL : urls) {
                if (max >= 1) {
                    break;
                }
                crawlTasks.add(new CrawlTask(foundURL, index, workQueue));
            }
        }

    }




    private static class CrawlTask implements Runnable {

        private final URL url;

        private final ThreadSafeInvertedWordIndex index;

        private final WorkQueue workQueue;


        public CrawlTask (URL url, ThreadSafeInvertedWordIndex index, WorkQueue workQueue) {
            this.url = url;
            this.index = index;
            this.workQueue = workQueue;
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

                crawl(url, index, workQueue);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
