package com.elhendawy.test.service;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.elhendawy.test.dto.Link;
import com.elhendawy.test.utils.CrawlerUtils;

/**
 * @author Mohamed Elhendawy
 * 
 *         the service layer implementation class which contains the business
 *         logic of the application
 */

@Service
public class CrawlerServiceImpl implements CrawlerService {

	public static final Logger LOGGER = LoggerFactory.getLogger(CrawlerServiceImpl.class);

	/*
	 * a Map object being used as a storage. in real time applications, we should
	 * use a different storage mechanism (ex. DB).
	 */
	public static Map<String, Link> linksMap = Collections.synchronizedMap(new HashMap<>());

	/* a counter to indicate the number of running threads. */
	private static volatile int threadCounter = 0;
	/*
	 * a StampedLock object is used to sync the modification of threadCounter
	 * variable between threads
	 */
	private final StampedLock counterLock = new StampedLock();

	/**
	 * the method which is doing the actual crawling
	 * 
	 * @return void
	 */
	@Override
	public void crawlURL(final String url, final int depth) throws Exception {

		/*
		 * it is important to increment and decrement threadsCounter for each thread, so
		 * that we will come to know that all threads finished execution and we ready to
		 * write to the file
		 */
		incrementThreadCounter();

		/*
		 * Check if the url is already exist or not. If it exist, so it was visited
		 * before and will not crawl it, otherwise I will crawl it based on the depth.
		 */
		Link l;
		if (null != (l = linksMap.get(url))) {
			l.setCount(l.getCount() + 1);

			LOGGER.debug("Thread: {} ------- Already Crawled {}..... -> Depth: {}", Thread.currentThread().getName(),
					url, depth);

			decrementThreadCounter();

			return;
		}

		/* initializing url entry to the map */
		linksMap.put(url, new Link(1));

		/* check if maximum depth reached */
		if (depth > 0) {

			/* Asynchronously running the below steps for each entry. To execute those steps
			 * in parallel for each entry, we call supplyAsync function in
			 * CompletableFuture, which will run the provided function asynchronously in the
			 * ForkJoinPool.common.
				 * 1- GetURLs by loading the document and extracting the urls (using Jsoup).
				 * 2- Re-processing the links and recursively call the crawlURL method again with the depth decreased by 1.
				 * 3- The processLink method will return a CompletableFuture for each link and we will then collect them on an array. The result of the CompletableFuture is null, but we are using it as and indicator for the execution of the tasks
				 * 4- We then call allOf method to wait for the completion of all Futures.
				 * 5- At the end we call join() to aggregate the results.
			 */
			CompletableFuture.supplyAsync(getURLs(url))
				.thenApply(processLink(depth))
				.thenApply(futures -> futures.toArray(CompletableFuture[]::new))
				.thenAccept(CompletableFuture::allOf)
				.join();

		} else {
			LOGGER.debug("Thread: {} ------- Will Not Crawl {}..... -> Depth: {}", Thread.currentThread().getName(), url,
					depth);
		}

		/* decrement the threadCounter to indicate end of execution */
		decrementThreadCounter();

		/* check if no more running tasks, then write result to the result.txt file. */
		if (getCounter() <= 0) {
			LOGGER.info("\nFinished Crawling. Number of Links: {}", linksMap.keySet().size());
			CrawlerUtils.writeLinksMapToFile(linksMap);
		}
	}

	/**
	 * examine URL and return the crawled URLs.
	 * 
	 * @return a lambda expression representing the implementation of
	 *         Supplier<List<String>> interface
	 */
	private Supplier<List<String>> getURLs(final String url) {
		return () -> {

			LOGGER.info("Thread: {} ------- Crawling {}.....", Thread.currentThread().getName(), url);

			List<String> links;
			try {
				/* The page processing happens here */
				links = CrawlerUtils.getLinksFromUrl(url);

				LOGGER.info("Thread: {} ------- Found: ({}) in URL: {}", Thread.currentThread().getName(), links.size(),
						url, " Link");
				LOGGER.debug("Thread: {} ------- Links: {}", Thread.currentThread().getName(), links);

				return links;

			} catch (IOException e) {
				e.printStackTrace();
				return new ArrayList<String>();
			}
		};
	}

	/**
	 * processing links and doing the recursive call
	 * 
	 * @return a lambda expression representing the implementation of
	 *         Function<List<String>, Stream<CompletableFuture<Void>>> interface
	 */
	private Function<List<String>, Stream<CompletableFuture<Void>>> processLink(final int depth) {
		return links -> links.parallelStream().map(link -> {
			try {
				crawlURL(link, depth - 1);
			} catch (Exception e) {
				e.printStackTrace();
			}

			/*
			 * return CompletedFuture with the result null to indicate the end of task
			 * execution.
			 */
			return completedFuture(null);
		});
	}

	/**
	 * Increment threads counter
	 * 
	 * @return threadCounter value after increment.
	 */
	private int incrementThreadCounter() {

		long lock = counterLock.writeLock();
		++threadCounter;
		counterLock.unlockWrite(lock);

		LOGGER.debug("Increment Counter: {} ------- Thread: {}", threadCounter, Thread.currentThread().getName());

		return threadCounter;
	}

	/**
	 * Decrement threads counter
	 * 
	 * @return threadCounter value after decrement.
	 */
	public int decrementThreadCounter() {

		long lock = counterLock.writeLock();
		--threadCounter;
		counterLock.unlockWrite(lock);

		LOGGER.debug("Decrement Counter: {} ------- Thread: {}", threadCounter, Thread.currentThread().getName());

		return threadCounter;
	}

	/**
	 * Reads threads counter value
	 * 
	 * @return current threadCounter value.
	 */
	@Override
	public int getCounter() {

		LOGGER.debug("Read Counter: {} ------- Thread: {}: {}", threadCounter, Thread.currentThread().getName());

		return threadCounter;
	}
}