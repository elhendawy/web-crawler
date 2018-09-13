package com.elhendawy.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.elhendawy.test.service.CrawlerService;

@RunWith(SpringRunner.class)
@SpringBootTest({ "url=https://en.wikipedia.org/wiki/Europe", "depth=2" })
public class ApplicationTest {

	@Autowired
	private CrawlerService crawlerService;

	@Test
	public void testApp() throws Exception {

	}

	// @Test
	// public void testWithWiki() throws Exception {
	/* Already tested on application startup */
	// crawlerService.crawlURL("https://en.wikipedia.org/wiki/Europe", 2);
	// }

	// @Test
	// public void testWithGoogle() throws Exception {
	/*
	 * commented because you will see wrong numbers due to the shared storage map.
	 * you can run each one separately
	 */
	// crawlerService.crawlURL("https://www.google.com", 1);
	// }

	// @Test
	// public void testWithMicrosoft() throws Exception {
	/*
	 * commented because it will take longer time due to the deeper depth. you can
	 * uncomment it and try.
	 */
	// crawlerService.crawlURL("https://www.microsoft.com/de-de/", 3);
	// }
}