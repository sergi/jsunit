package net.jsunit.action;

import junit.framework.TestCase;
import net.jsunit.MockBrowserTestRunner;
import net.jsunit.Utility;
import net.jsunit.model.ResultType;

public class TestRunnerActionTest extends TestCase {

    private TestRunnerAction action;
	private MockBrowserTestRunner mockRunner;

    public void setUp() throws Exception {
        super.setUp();
        action = new TestRunnerAction();
        mockRunner = new MockBrowserTestRunner();
        mockRunner.hasReceivedResult = true;
        action.setBrowserTestRunner(mockRunner);
    }

    public void testSuccess() throws Exception {
    	mockRunner.shouldSucceed = true;
        assertEquals(TestRunnerAction.SUCCESS, action.execute());
        String xmlString = Utility.asString(action.getXmlRenderable().asXml());
		assertTrue(xmlString.startsWith("<testRunResult type=\""+ResultType.SUCCESS.name()));
    }

    public void testFailure() throws Exception {
    	mockRunner.shouldSucceed = false;
        assertEquals(TestRunnerAction.SUCCESS, action.execute());
        String xmlString = Utility.asString(action.getXmlRenderable().asXml());
		assertTrue(xmlString.startsWith("<testRunResult type=\""+ResultType.FAILURE.name()));
    }

}