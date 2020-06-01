package monitor;

import static org.junit.jupiter.api.Assertions.*;
import java.net.URL;
import org.junit.jupiter.api.Test;

class AlertingTest {

	@Test
	void test() throws Exception {
		WebRequestData testGoogle = new WebRequestData(new URL("http://www.google.com"), 1000);
		WebRequestData testFailure = new WebRequestData(new URL("http://FailureWebsite"), 1000);
		
		MainRunner.monitor(testGoogle);
		MainRunner.monitor(testFailure);
		
		// Test after the first two minutes
		Thread.sleep(1000 * 60 * 3);
		
		// Google should works fine
		assertEquals(0, testGoogle.alertingMessageList.size());
		assertEquals(true, testGoogle.availabilityForAltering);
		
		// This wrong website should have alerting message
		assertEquals(true, testFailure.alertingMessageList.get(0).startsWith("Website http://FailureWebsite is down. "));
		assertEquals(false, testFailure.availabilityForAltering);
	}
}
