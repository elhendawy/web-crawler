package com.elhendawy.webcrawler.service;

/**
 * 
 * @author Mohamed Elhendawy
 *
 */
public interface CrawlerService {

	public void crawlURL(String url, int depth) throws Exception;

	public int getCounter();
}