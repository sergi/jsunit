package net.jsunit.model;

import net.jsunit.XmlRenderable;
import net.jsunit.utility.SystemUtility;
import org.jdom.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TestRunResult extends AbstractResult implements XmlRenderable, Comparable<TestRunResult> {

    private List<BrowserResult> browserResults = new ArrayList<BrowserResult>();
    private URL url;
    private String osString;
    private String ipAddress;
    private String hostname;
    private boolean unresponsive = false;

    public TestRunResult() {
        this(null);
    }

    public TestRunResult(URL url) {
        this.url = url;
    }

    public void addBrowserResult(BrowserResult browserResult) {
        browserResults.add(browserResult);
    }

    public Element asXml() {
        Element root = new Element("testRunResult");
        root.setAttribute("type", getResultType().name());
        if (url != null)
            root.setAttribute("url", url.toString());
        if (hasProperties()) {
            Element properties = new Element("properties");
            addProperties(properties);
            root.addContent(properties);
        }
        for (BrowserResult browserResult : browserResults)
            root.addContent(browserResult.asXml());
        return root;
    }

    private boolean hasProperties() {
        return osString != null || ipAddress != null || hostname != null;
    }

    private void addProperties(Element element) {
        if (osString != null)
            addProperty(element, "os", osString);
        if (ipAddress != null)
            addProperty(element, "ipAddress", ipAddress);
        if (hostname != null)
            addProperty(element, "hostname", hostname);
    }

    private void addProperty(Element element, String name, String value) {
        Element property = new Element("property");
        property.setAttribute("name", name);
        property.setAttribute("value", value);
        element.addContent(property);
    }

    protected List<? extends Result> getChildren() {
        return browserResults;
    }

    public void setUnresponsive() {
        unresponsive = true;
    }

    public boolean wasUnresponsive() {
        return unresponsive;
    }

    public URL getUrl() {
        return url;
    }

    public ResultType getResultType() {
        if (unresponsive)
            return ResultType.UNRESPONSIVE;
        else
            return super.getResultType();
    }

    public void setOsString(String osString) {
        this.osString = osString;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setURL(URL url) {
        this.url = url;
    }

    public String getOsString() {
        return osString;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public int compareTo(TestRunResult other) {
        if (url == null | other.getUrl() == null)
            return 0;
        return url.toString().compareTo(other.getUrl().toString());
    }

    public void initializeProperties() {
        setOsString(SystemUtility.osString());
        setHostname(SystemUtility.hostname());
        setIpAddress(SystemUtility.ipAddress());
    }

    protected void addMyErrorStringTo(StringBuffer buffer) {
        buffer.append(getDisplayString());
        buffer.append("\n");
    }

    protected void addChildErrorStringTo(Result child, StringBuffer buffer) {
        if (!child.wasSuccessful()) {
            child.addErrorStringTo(buffer);
            if (url != null) {
                buffer.append("\n");
                buffer.append("The full result log is at ");
                BrowserResult childBrowserResult = (BrowserResult) child;
                buffer.append(logUrlFor(childBrowserResult));
            }
        }
    }

    private URL logUrlFor(BrowserResult browserResult) {
        String path = "/jsunit/displayer?id=" + browserResult.getId() + "&browserId=" + browserResult.getBrowser().getId();
        try {
            return new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDisplayString() {
        boolean hasURL = url != null;
        StringBuffer buffer = new StringBuffer();
        if (hasURL)
            buffer.append(url.toString());
        else
            buffer.append("localhost");
        buffer.append(" (IP address: ");
        if (ipAddress != null)
            buffer.append(ipAddress);
        else
            buffer.append("unknown");
        buffer.append(", host name: ");
        if (hostname != null)
            buffer.append(hostname);
        else buffer.append("unknown");
        buffer.append(", OS: ");
        if (osString != null)
            buffer.append(osString);
        else buffer.append("unknown");
        buffer.append(")");
        return buffer.toString();
    }

    public List<BrowserResult> getBrowserResults() {
        return browserResults;
    }

}
