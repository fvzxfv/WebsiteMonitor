package monitor;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


/**
 * Website performance monitor program designed for Datadog assignment
 * 
 * @author Jiayuan Hu
 *
 */
public class MainRunner {
	
	static Set<WebRequestData> webRequestDataSet;
	final static int TIME_OUT = 1000;
	
	/**
	 * Three major components: 
	 * 1. Initialize and read from input
	 * 2. Monitor each website with one thread in the background
	 * 3. Print matrics with pre-defined interval
	 */
	public static void main(String[] args) throws Exception {
		init();
		
		for (WebRequestData webData : webRequestDataSet) {
			monitor(webData);
		}
		
		new Printer().printRequestInfo(webRequestDataSet);
	}	
	
	/**
	 * Initial the program
	 * Read user-defined url and interval from input stream 
	 */
	private static void init() {
		System.out.println("Please enter the websites for monitoring along with its check interval (in millisecond).");
		System.out.println("Example: " + "\r" + "www.google.com 800" + "\r" + "github.com 1000");
		System.out.println("Press Enter once complete. ");
		
		webRequestDataSet = new HashSet<>();
		
		Scanner reader = new Scanner(System.in);
		String rawData = reader.nextLine();
		Boolean hasInputData = false;
		
		while(!rawData.equals("")) {
			WebRequestData webData = new WebRequestData();
			
			try {
				webData = parseRawData(rawData);
			} catch(Exception e) {
				System.out.println("Unexpected input. Please check the correct format again. ");
			}
			
			// Add current website to set if valid
			if (webData.url == null || webData.url.toString() != "") {
				webRequestDataSet.add(webData);
			}
			
			rawData = reader.nextLine();
			hasInputData = true;
		}
		reader.close();		
		
		if (hasInputData) {
			System.out.println("Input complete, please wait for your update. ");
		} else {
			System.out.println("Oops, seems you didn't enter website to monitor. ");
		}
	}

	/**
	 * Parse raw data into url and check interval
	 * @param rawInput User's input string
	 */
	private static WebRequestData parseRawData(String rawData) throws Exception {
		String[] rawInputSplit = rawData.split(" ");
		
		if (isValidData(rawInputSplit)) {
			WebRequestData webData = new WebRequestData();
			webData.setURL(new URL("http://" + rawInputSplit[0]));
			webData.setInterval(Integer.valueOf(rawInputSplit[1]));
			
			return webData;
		} else {
			throw new Exception("Invalid format of input. ");
		}
	}
	
	/**
	 * Check if input is valid
	 * @param rawInputSplit Array containing splited data
	 * @return Validness of input data
	 */
	private static boolean isValidData(String[] rawInputSplit) {
		try {
			String validtor = new BigDecimal(rawInputSplit[1]).toString();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Each time this method is called, a new thread will start to monitor a website
	 * @param webData Website needed to be monitored with corresponding data
	 */
	private static void monitor(WebRequestData webData) {
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					requestUrl(webData);
					
					try {
						Thread.sleep((long) webData.interval);
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		Thread thread = new Thread(runnable);
		thread.start();
	}

	/**
	 * Send a HTTP request and get response information
	 * @param webData Website needed to be monitored with corresponding data
	 */
	protected static void requestUrl(WebRequestData webData) {
		double currentTime = System.currentTimeMillis(), responseTime = 0;
		int responseCode = 0;
		boolean availability = false;
		
		try {
			HttpURLConnection connection = (HttpURLConnection) webData.url.openConnection();
			connection.setConnectTimeout(TIME_OUT);
			connection.connect();
			
			// This is for calculating the response time
			currentTime = System.currentTimeMillis();
			responseCode = connection.getResponseCode();
			responseTime = System.currentTimeMillis() - currentTime;
			availability = true;
			
			if (responseCode < 200 || responseCode > 399) {
				responseTime = 0;
				availability = false;
			}
		} catch (Exception e) {
			responseTime = 0;
			availability = false;
		} finally {
			// Update corresponding data
			webData.update(responseTime, responseCode, availability, currentTime);
		}
	}
}
