package monitor;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Website performance monitor program designed for Datadog assignment
 * 
 * @author Jiayuan Hu
 *
 */
@SuppressWarnings("rawtypes")
public class WebRequestData {

	// Pre-defined interval for displaying, tracing, and alerting
	static final double SHORT_DISPLAY_INTERVAL = 1000 * 10;
	static final double LONG_DISPLAY_INTERVAL = 1000 * 60;
	static final double SHORT_TRACE_INTERVAL = 1000 * 60 * 10;
	static final double LONG_TRACE_INTERVAL = 1000 * 60 * 60;
	
	static final double ALERT_CHECK_INTERVAL = 1000 * 60 * 2;
	static final double ALERT_THRESHOLD = 0.8;
	
	URL url;
	double interval;
	
	List<ResponseDataInfo> availabilityList;
	
	List<ResponseDataInfo> responseTimeList;
	
	// Hashmap is used to reduce counting time
	List<ResponseDataInfo> responseCodeList;
	Map<Integer, Integer> responseCodeCount;
	
	public List<String> alertingMessageList;
	public boolean availabilityForAltering;
	
	public WebRequestData() {
		this.availabilityList = new ArrayList<ResponseDataInfo>();
		this.responseTimeList = new ArrayList<ResponseDataInfo>();
		this.responseCodeList = new ArrayList<ResponseDataInfo>();
		this.responseCodeCount = new HashMap<Integer, Integer>();
		this.alertingMessageList = new ArrayList<String>();
		this.availabilityForAltering = true;
	}
	
	public WebRequestData(URL url, double interval) {
		this.url = url;
		this.interval = interval;
		this.availabilityList = new ArrayList<ResponseDataInfo>();
		this.responseTimeList = new ArrayList<ResponseDataInfo>();
		this.responseCodeList = new ArrayList<ResponseDataInfo>();
		this.responseCodeCount = new HashMap<Integer, Integer>();
		this.alertingMessageList = new ArrayList<String>();
		this.availabilityForAltering = true;
	}
	
	/**
	 * Method to update data in this get request
	 * @param responseTime Site response time
	 * @param responseCode Site response code
	 * @param availability If site can be reached
	 * @param currentTime Time point for this request
	 */
	public void update(double responseTime, int responseCode, boolean availability, double currentTime) {
		
		updateAvailability(availability, currentTime);
		
		updateResponseTime(responseTime, currentTime);
		
		updateResponseCode(responseCode, currentTime);
		
		// If time has passed the first alerting check interval, then it's ready for check available state
		if (this.availabilityList.get(this.availabilityList.size() - 1).timeLineInfo - this.availabilityList.get(0).timeLineInfo >= ALERT_CHECK_INTERVAL) {
			checkAlerting(currentTime);
		}
	}

	/**
	 * Method to check whether to send an alerting
	 * @param currentTime Availability updated time
	 */
	public void checkAlerting(double currentTime) {
		// Calculate availability in given interval
		int index = this.availabilityList.size() - 1, trueCount = 0, availablityWithinInterval = 0;
		for (; index >= 0; index--) {
			if (this.availabilityList.get(index).timeLineInfo > this.availabilityList.get(this.availabilityList.size() - 1).timeLineInfo - ALERT_CHECK_INTERVAL) {
				availablityWithinInterval++;
				if (this.availabilityList.get(index).data.equals(true)) {
					trueCount++;
				}
			}
		}
		double availabilityRate = availablityWithinInterval == 0 ? 0 : trueCount / availablityWithinInterval;
		
		// Update alerting messages and print them if any
		Date date = new Date();
		date.setTime((long) currentTime);
		if (availabilityRate < ALERT_THRESHOLD && this.availabilityForAltering == true) {
			this.alertingMessageList.add("Website " + this.url.toString() + " is down. Availability = " + availabilityRate + ", time = " + new SimpleDateFormat().format(date));
			
			for (String message : alertingMessageList) {
				System.out.println(message);
			}
			
			this.availabilityForAltering = false;
		} else if (availabilityRate >= ALERT_THRESHOLD && this.availabilityForAltering == false) {
			this.alertingMessageList.add("Website " + this.url.toString() + "'s availability resumes. Availability = " + availabilityRate + ", time = " + new SimpleDateFormat().format(date));
			
			for (String message : alertingMessageList) {
				System.out.println(message);
			}
			
			this.availabilityForAltering = true;
		}
	}

	/**
	 * Update response code and remove outdated response code, both list and hashmap
	 * @param responseCode Response code for this request
	 * @param currentTime Time point for this request
	 */
	private void updateResponseCode(int responseCode, double currentTime) {
		this.responseCodeList.add(new ResponseDataInfo<Integer>(responseCode, currentTime));
		this.responseCodeCount.put(responseCode, this.responseCodeCount.getOrDefault(responseCode, 0) + 1);
		
		// Remove outdated data from list and hashmap
		List<ResponseDataInfo> polledData = pollOutdatedData(responseCodeList);
		for (ResponseDataInfo d : polledData) {
			responseCodeCount.put((Integer) d.data, responseCodeCount.get(d.data) - 1);
		}
	}

	/**
	 * Update response time and remove outdated response time
	 * @param responseTime Response time for this request
	 * @param currentTime Time point for this request
	 */
	private void updateResponseTime(double responseTime, double currentTime) {
		responseTimeList.add(new ResponseDataInfo<Double>(responseTime, currentTime));
		
		pollOutdatedData(responseTimeList);
	}

	/**
	 * Update availability and remove outdated availability
	 * @param availability Availability for this request
	 * @param currentTime Time point for this request
	 */
	private void updateAvailability(boolean availability, double currentTime) {
		availabilityList.add(new ResponseDataInfo<Boolean>(availability, currentTime));
		
		pollOutdatedData(availabilityList);
	}
	
	/**
	 * Method used to remove any data that is out of the trace range
	 * @param dataList Any list used in this class
	 * @return All data removed
	 */
	private List<ResponseDataInfo> pollOutdatedData(List<ResponseDataInfo> dataList) {
		List<ResponseDataInfo> polledData = new ArrayList<>();
		
		while (dataList.get(0).timeLineInfo < (dataList.get(dataList.size() - 1).timeLineInfo - LONG_TRACE_INTERVAL)) {
			try {
				polledData.add(dataList.get(0));
				dataList.remove(0);
			} catch (Exception e) {
				return polledData;
			}
		}
		return polledData;
	}
	
	/**
	 * Calculate all the data for printer
	 * @return Encapsulated data ready for printer
	 */
	public PrintInfo getPrintInfo() {
		PrintInfo printInfo = new PrintInfo(this.url.toString(), this.interval);
		
		printInfo.availabilityRate = getAvailabilityRate(this.availabilityList);
		printInfo.avgResponseTime = getAvgResponseTime(this.responseTimeList);
		printInfo.maxResponseTime = getMaxResponseTime(this.responseTimeList);
		printInfo.minResponseTime = getMinResponseTime(this.responseTimeList);
		printInfo.responseCodeCount = this.responseCodeCount;
		printInfo.alertingMessages = this.alertingMessageList;
		
		return printInfo;
	}

	/**
	 * Calculate all the SHORT version data for printer 
	 * @return Encapsulated data ready for printer
	 */
	public PrintInfo getShortPrintInfo() {
		PrintInfo printInfo = new PrintInfo(this.url.toString(), this.interval);
		
		printInfo.availabilityRate = getAvailabilityRate(getShortList(availabilityList));
		
		// Get the sub-list with in the SHORT interval
		List<ResponseDataInfo> shortResponseTimeList = getShortList(this.responseTimeList);
		printInfo.avgResponseTime = getAvgResponseTime(shortResponseTimeList);
		printInfo.maxResponseTime = getMaxResponseTime(shortResponseTimeList);
		printInfo.minResponseTime = getMinResponseTime(shortResponseTimeList);
		printInfo.responseCodeCount = getShortResponseCodeCount(this.responseCodeList);
		printInfo.alertingMessages = this.alertingMessageList;
		
		return printInfo;
	}
	
	/**
	 * Get response code count within SHORT interval
	 * @param responseCodeList All response code data 
	 * @return responseCode Response code count with in given interval
	 */
	private Map<Integer, Integer> getShortResponseCodeCount(List<ResponseDataInfo> responseCodeList) {
		Map<Integer, Integer> shortResponseCodeCount = new HashMap<>();
		int index = responseCodeList.size() - 1;
		while (index >= 0 && (responseCodeList.get(index).timeLineInfo >= responseCodeList.get(responseCodeList.size() - 1).timeLineInfo - SHORT_TRACE_INTERVAL)) {
			shortResponseCodeCount.put((Integer) responseCodeList.get(index).data, shortResponseCodeCount.getOrDefault((Integer) responseCodeList.get(index).data, 0) + 1);
			index--;
		}
		
		return shortResponseCodeCount;
	}

	/**
	 * Get SHORT version of any list in this class
	 * @param dataList Any list used in this class
	 * @return List containing data within given interval
	 */
	private List<ResponseDataInfo> getShortList(List<ResponseDataInfo> dataList) {
		List<ResponseDataInfo> shortList = new ArrayList<>();
		int index = dataList.size() - 1;
		while (index >= 0 && (dataList.get(index).timeLineInfo >= dataList.get(dataList.size() - 1).timeLineInfo - SHORT_TRACE_INTERVAL)) {
			shortList.add(dataList.get(index));
			index--;
		}
		
		return shortList;
	}
	
	/**
	 * Get availability rate of input list
	 * @param availabilityList 
	 * @return Availability rate
	 */
	private double getAvailabilityRate(List<ResponseDataInfo> availabilityList) {
		if (availabilityList.size() == 0) return 0;
		
		int trueCount = 0;
		for (ResponseDataInfo dataInfo : availabilityList) {
			if (dataInfo.data.equals(true)) {
				trueCount++;
			}
		}
		return 100 * trueCount / availabilityList.size();
	}
	
	/**
	 * Get average response time of input list
	 * @param responseTimeList
	 * @return Average response time 
	 */
	private double getAvgResponseTime(List<ResponseDataInfo> responseTimeList) {
		if (responseTimeList.size() == 0) return 0;
		
		int falseCount = 0;
		double sumTime = 0;
		
		for (ResponseDataInfo dataInfo : responseTimeList) {
			if ((Double) dataInfo.data == (Double) 0.0) {
				falseCount++;
			} else {
				sumTime += (Double) dataInfo.data;
			}
		}
		
		if (falseCount == responseTimeList.size()) return 0;
		
		return sumTime / (responseTimeList.size() - falseCount);
	}
	
	/**
	 * Get maximum response time of input list
	 * @param responseTimeList
	 * @return Maximum response time
	 */
	private double getMaxResponseTime(List<ResponseDataInfo> responseTimeList) {
		double maxResponseTime = Double.MIN_VALUE;
		for (ResponseDataInfo dataInfo : responseTimeList) {
			maxResponseTime = Math.max(maxResponseTime, (Double) dataInfo.data);
		}
		
		return maxResponseTime == Double.MIN_VALUE ? 0 : maxResponseTime;
	}
	
	/**
	 * Get minimum response time of input list
	 * @param responseTimeList
	 * @return Minimum response time
	 */
	private double getMinResponseTime(List<ResponseDataInfo> responseTimeList) {
		double minResponseTime = Double.MAX_VALUE;
		for (ResponseDataInfo dataInfo : responseTimeList) {
			if ((Double) dataInfo.data == 0.0) continue;
			minResponseTime = Math.min(minResponseTime, (Double) dataInfo.data);
		}
		
		return minResponseTime == Double.MAX_VALUE ? 0 : minResponseTime;	
	}

	public void setURL(URL url) {
		this.url = url;
	}
	
	public void setInterval(double interval) {
		this.interval = interval;
	}
}

/**
 * Defined to record certain type of data and corresponding time info
 * @param <T> Availability: Boolean; Response time: double; Response code: int
 */
class ResponseDataInfo<T> {
	T data;
	double timeLineInfo;
	
	ResponseDataInfo (T data, double timeLineInfo) {
		this.data = data;
		this.timeLineInfo = timeLineInfo;
	}
}

/**
 *Defined to store data for printer
 */
class PrintInfo {
	String url;
	double interval;
	double avgResponseTime;
	double maxResponseTime;
	double minResponseTime;
	double availabilityRate;
	Map<Integer, Integer> responseCodeCount;
	List<String> alertingMessages;
	
	PrintInfo(String url, double interval) {
		this.url = url;
		this.interval = interval;
	}
}