package monitor;

import java.net.HttpURLConnection;

public class Monitor {

	final static int TIME_OUT = 1000;
	
	/**
	 * Each time this method is called, a new thread will start to monitor a website
	 * @param webData Website needed to be monitored with corresponding data
	 */
	public void monitor(WebRequestData webData) {
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
	private void requestUrl(WebRequestData webData) {
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
			
			// HTTP response code only represent successful connection if it is in [200, 399]
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
