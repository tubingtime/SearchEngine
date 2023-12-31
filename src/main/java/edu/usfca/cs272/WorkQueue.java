package edu.usfca.cs272;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

/**
 * A simple work queue implementation based on the IBM developerWorks article by
 * Brian Goetz. It is up to the user of this class to keep track of whether
 * there is any pending work remaining.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 * @see <a href=
 * "https://web.archive.org/web/20210126172022/https://www.ibm.com/developerworks/library/j-jtp0730/index.html">
 * Java Theory and Practice: Thread Pools and Work Queues</a>
 */
public class WorkQueue {
    /**
     * Workers that wait until work (or tasks) are available.
     */
    private final Worker[] workers;

    /**
     * Queue of pending work (or tasks).
     */
    private final LinkedList<Runnable> tasks;

    /**
     * Used to signal the workers should terminate.
     */
    private volatile boolean shutdown;

    /**
     * The default number of worker threads to use when not specified.
     */
    public static final int DEFAULT = 5;

    /**
     * Logger used for this class.
     */
    private static final Logger log = LogManager.getLogger();

    /**
     * Tracks how much work is still being processed (aka tasks still in run method)
     */
    private int pending;

    /**
     * Lock for the pending variable
     */
    private final Object pendingLock;


    /**
     * Starts a work queue with the default number of threads.
     *
     * @see #WorkQueue(int)
     */
    public WorkQueue() {
        this(DEFAULT);
    }

    /**
     * Starts a work queue with the specified number of threads.
     *
     * @param threads number of worker threads; should be greater than 1
     */
    public WorkQueue(int threads) {
        this.tasks = new LinkedList<>();
        this.workers = new Worker[threads];
        this.shutdown = false;
        this.pending = 0;
        this.pendingLock = new Object();

        // start the threads so they are waiting in the background
        for (int i = 0; i < threads; i++) {
            workers[i] = new Worker();
            workers[i].start();
        }

        log.debug("Work queue initialized with {} worker threads.", workers.length);
    }


    /**
     * Adds a work (or task) request to the queue. A worker thread will process
     * this request when available.
     *
     * @param task work request (in the form of a {@link Runnable} object)
     */
    public void execute(Runnable task) {
        synchronized (pendingLock) {
            pending++;
        }
        synchronized (tasks) {
            tasks.addLast(task);
            tasks.notifyAll();
        }

    }

    /**
     * Waits for all pending work (or tasks) to be finished. Does not terminate
     * the worker threads so that the work queue can continue to be used.
     */
    public void finish() {
        try {
            synchronized (pendingLock) {
                while (pending != 0) {
                    log.debug("Waiting to finish. Tasks: {} | Pending: {}", tasks.size(), pending);
                    pendingLock.wait(); // question: how does anything modify pending while this thread is waiting with the lock?
                }
                log.debug("Work finished!");
            }
        } catch (InterruptedException e) {
            System.err.println("Warning: Work queue interrupted while finishing.");
            log.catching(Level.DEBUG, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Similar to {@link Thread#join()}, waits for all the work to be finished and
     * the worker threads to terminate. The work queue cannot be reused after this
     * call completes.
     */
    public void join() {
        try {
            finish();
            shutdown();
            for (Worker worker : workers) {
                worker.join();
            }

            log.debug("All worker threads terminated.");
        } catch (InterruptedException e) {
            System.err.println("Warning: Work queue interrupted while joining.");
            log.catching(Level.DEBUG, e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Asks the queue to shutdown. Any unprocessed work (or tasks) will not be
     * finished, but threads in-progress will not be interrupted.
     */
    public void shutdown() {
        // safe to do unsynchronized due to volatile keyword
        shutdown = true;

        log.debug("Work queue triggering shutdown...");
        synchronized (tasks) {
            tasks.notifyAll();
        }
    }

    /**
     * Returns the number of worker threads being used by the work queue.
     *
     * @return number of worker threads
     */
    public int size() {
        return workers.length;
    }

    /**
     * Returns the amount of pending work
     *
     * @return the amount of pending work
     */
    public int getPending() {
        int gotPending;
        synchronized (pendingLock) {
            gotPending = this.pending;
        }
        return gotPending;
    }

    /**
     * Returns the amount of remaining tasks
     *
     * @return the amount of pending tasks
     */
    public int getTaskSize() {
        int taskSize;
        synchronized (tasks) {
            taskSize = tasks.size();
        }
        return taskSize;
    }

    /**
     * Waits until work (or a task) is available in the work queue. When work is
     * found, will remove the work from the queue and run it.
     *
     * <p>
     * If a shutdown is detected, will exit instead of grabbing new work from the
     * queue. These threads will continue running in the background until a
     * shutdown is requested.
     */
    private class Worker extends Thread {
        /**
         * Initializes a worker thread with a custom name.
         */
        public Worker() {
            setName("Worker" + getName());
        }

        @Override
        public void run() {
            Runnable task;

            try {
                while (true) {
                    synchronized (tasks) {
                        while (tasks.isEmpty() && !shutdown) {
                            log.debug("Work queue worker waiting...");
                            tasks.wait();
                        }

                        // exit while for one of two reasons:
                        // (a) queue has work, or (b) shutdown has been called

                        if (shutdown) {
                            log.debug("Worker detected shutdown...");
                            break;
                        } else {
                            log.debug("Worker found {} tasks...", tasks.size());
                            task = tasks.removeFirst();
                        }
                    }

                    try {
                        log.trace("Work queue worker running work.");
                        task.run();
                    } catch (RuntimeException e) {
                        // catch runtime exceptions to avoid leaking threads
                        System.err.printf("Warning: %s encountered an exception while running.%s%n", this.getName(), e);
                        log.catching(Level.DEBUG, e);
                    } finally {
                        synchronized (pendingLock) {
                            pending--;
                            if (pending == 0) {
                                pendingLock.notifyAll();
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                // causes early termination of worker threads
                System.err.printf("Warning: %s interrupted while waiting.%n", this.getName());
                log.catching(Level.DEBUG, e);
                Thread.currentThread().interrupt();
            }
            log.debug("Worker thread terminating...");
        }
    }
}
