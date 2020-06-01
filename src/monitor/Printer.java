package monitor;

import java.util.*;

/**
 * Website performance monitor program designed for Datadog assignment
 * 
 * @author Jiayuan Hu
 *
 */
public class Printer {

	// Pre-defined interval for displaying and tracing
	static final double SHORT_DISPLAY_INTERVAL = 1000 * 10;
	static final double LONG_DISPLAY_INTERVAL = 1000 * 60;
	static final double SHORT_TRACE_INTERVAL = 1000 * 60 * 10;
	static final double LONG_TRACE_INTERVAL = 1000 * 60 * 60;
	
	public Printer() {}

	/**
	 * Start two thread to print SHORT version and LONG version of output separately
	 * @param webRequestDataSet Dataset containing all information
	 */
	public void printRequestInfo(Set<WebRequestData> webRequestDataSet) {
		// Thread for SHORT version
		Runnable runnableShort = new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep((long) SHORT_DISPLAY_INTERVAL); 
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
					
					printShortInfo(webRequestDataSet);
				}
			}
		};
		
		Thread threadShort = new Thread(runnableShort);
		threadShort.start();

		// Thread for LONG version
		Runnable runnableLong = new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep((long) LONG_DISPLAY_INTERVAL + 100); 
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
					
					printLongInfo(webRequestDataSet);
				}
			}
		};
		
		Thread threadLong = new Thread(runnableLong);
		threadLong.start();
	}
	
	/**
	 * Print SHORT version output
	 * @param webRequestDataSet
	 */
	private void printShortInfo(Set<WebRequestData> webRequestDataSet) {
		System.out.println("\n\n\n" + "############################################################################");
		System.out.println("Metrics for last " + SHORT_TRACE_INTERVAL / 1000 / 60 + " minutes. ");
		for (WebRequestData data : webRequestDataSet) {
			PrintInfo printInfo = data.getShortPrintInfo();
			printMetrics(printInfo);
		}
	}

	/**
	 * Print LONG version output
	 * @param webRequestDataSet
	 */
	private void printLongInfo(Set<WebRequestData> webRequestDataSet) {
		System.out.println("\n" + "############################################################################");
		System.out.println("Metrics for last " + LONG_TRACE_INTERVAL / 1000 / 60 + " minutes. ");
		for (WebRequestData data : webRequestDataSet) {
			PrintInfo printInfo = data.getPrintInfo();
			printMetrics(printInfo);
		}
	}
	
	/**
	 * Methoed used for printing each website request information
	 * @param printInfo
	 */
	private void printMetrics(PrintInfo printInfo) {
		System.out.println("----------------------------------------------------------------------------");
		System.out.println("url: " + printInfo.url + ". Check interval: " + printInfo.interval);
		System.out.println("Availability rate: " + printInfo.availabilityRate + "%");
		System.out.println("Avg response time: " + printInfo.avgResponseTime);
		System.out.println("Max response time: " + printInfo.maxResponseTime);
		System.out.println("Min response time: " + printInfo.minResponseTime);
		
		for (Map.Entry<Integer, Integer> responseCode : printInfo.responseCodeCount.entrySet()) {
			System.out.println("Response code " + responseCode.getKey() + " count: " + responseCode.getValue());
		}
		
		// Print down and resume history if any
		System.out.println("Down and resume history: " + (printInfo.alertingMessages.size() == 0 ? "None. " : ""));
		for (String message : printInfo.alertingMessages) {
			System.out.println(message);
		}
	}
}
