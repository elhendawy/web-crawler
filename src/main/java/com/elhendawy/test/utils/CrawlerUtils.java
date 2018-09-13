package com.elhendawy.test.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.elhendawy.test.dto.Link;

/**
 * 
 * @author Mohamed Elhendawy
 *
 */
public class CrawlerUtils {

	private static final Lock mainLock = new ReentrantLock();

	public static List<String> getLinksFromUrl(String url) throws IOException {

		Document doc = Jsoup.connect(url).get();

		return doc.select("a[href]").stream().map(link -> link.attr("abs:href"))
				.filter(link -> !link.contains(url + "#") && !link.equals(url + "/") && link.startsWith("http"))
				.collect(Collectors.toList());
	}

	public static <K> void writeLinksMapToFile(final Map<K, Link> map) throws IOException {

		mainLock.lock();

		try (BufferedWriter bw = new BufferedWriter(new FileWriter("result.txt", false))) {

			for (K key : map.keySet()) {
				bw.append(map.get(key).getCount() + "	" + key);
				bw.newLine();
				bw.flush();
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		mainLock.unlock();
	}
}
