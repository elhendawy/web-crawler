package com.elhendawy.webcrawler;

import java.io.IOException;

import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.elhendawy.test.service.CrawlerService;

/**
 * @author Mohamed Elhendawy
 * 
 *         The stating point class of this spring boot application
 * 
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

	public static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	/*
	 * a renaming for the main thread. just for easing the study job in Java
	 * VisualVM tool
	 */
	public static final String MAIN_THREAD_NAME = "MAIN_THREAD";

	/* Autowiring the service class implementation to avoid coupling our layers */
	@Autowired
	private CrawlerService crawlerService;

	/* reading the url from applicatio.properties file. */
	@Value("${url}")
	private String url;

	/* reading the depth from applicatio.properties file. */
	@Value("${depth}")
	private int depth;

	public static void main(String[] args) throws IOException {

		SpringApplication app = new SpringApplication(Application.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.run(args);

	}

	@Override
	public void run(String... args) throws Exception {

		Thread.currentThread().setName(Application.MAIN_THREAD_NAME);

		/*
		 * You can either run this application by submitting the URL as an argument to
		 * the main method or by editing it in the "application.properties" file.
		 */
		if (args.length > 0) {
			url = args[0];
			try {
				int d = Integer.parseInt(args[1]);
				depth = d;
			} catch (Exception e) {
				if (depth == 0) {
					depth = 1;
				}
			}
		}

		/*
		 * Check if URL fetched successfully, continue, otherwise stop and shouw the
		 * provided message.
		 */
		Validate.isTrue(null != url, "No URL Provided");

		crawlerService.crawlURL(url, depth);

		LOGGER.info("--->>>>>>> Finished Successfully <<<<<<<---");
	}
}