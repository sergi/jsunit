package net.jsunit.action;

import net.jsunit.TestRunManager;
import net.jsunit.XmlRenderable;
import net.jsunit.utility.StringUtility;

public class TestRunnerAction extends JsUnitBrowserTestRunnerAction implements RequestSourceAware {

    private TestRunManager manager;
    private String url;
    private String remoteAddress;
    private String remoteHost;

    public String execute() throws Exception {
        runner.logStatus(requestReceivedMessage());
        synchronized (runner) {
            manager = new TestRunManager(runner, url);
            manager.runTests();
        }
        runner.logStatus("Done running tests");
        return SUCCESS;
    }

    private String requestReceivedMessage() {
        String message = "Received request to run tests";
        if (!StringUtility.isEmpty(remoteAddress) || !StringUtility.isEmpty(remoteHost)) {
            message += " from ";
            if (!StringUtility.isEmpty(remoteHost)) {
                message += remoteHost;
                if (!StringUtility.isEmpty(remoteAddress))
                    message += " (" + remoteAddress+")";
            } else {
                message += remoteAddress;
            }
        }
        return message;
    }

    public XmlRenderable getXmlRenderable() {
        return manager.getTestRunResult();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRequestIPAddress(String ipAddress) {
        remoteAddress = ipAddress;
    }

    public void setRequestHost(String host) {
        remoteHost = host;
    }
}
