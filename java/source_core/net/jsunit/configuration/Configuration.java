package net.jsunit.configuration;

import net.jsunit.Utility;
import net.jsunit.XmlRenderable;
import org.jdom.Element;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Edward Hieatt, edward@jsunit.net
 */

public final class Configuration implements XmlRenderable {

    private ConfigurationSource source;
    public static final String DEFAULT_RESOURCE_BASE = ".";
    public static final int DEFAULT_PORT = 8080;
    public static final int DEFAULT_TIMEOUT_SECONDS = 60;

    public Configuration(ConfigurationSource source) {
        this.source = source;
    }

    public static Configuration resolve(String[] arguments) {
        ConfigurationSource source;
        if (arguments.length > 0)
            source = new ArgumentsConfigurationSource(Arrays.asList(arguments));
        else
            source = resolveSource();
        return new Configuration(source);
    }

    private static ConfigurationSource resolveSource() {
        EnvironmentVariablesConfigurationSource evConfig = new EnvironmentVariablesConfigurationSource();
        if (evConfig.isAppropriate())
            return evConfig;
        return new PropertiesFileConfigurationSource();
    }

    public static Configuration resolve() {
        return new Configuration(resolveSource());
    }

    public URL getTestURL() throws ConfigurationException {
        String urlString = source.url();
        if (Utility.isEmpty(urlString))
            return null;
        try {
            return new URL(urlString);
        } catch (Exception e) {
            throw new ConfigurationException(ConfigurationProperty.URL.getName(), urlString, e);
        }
    }

    public List<String> getBrowserFileNames() throws ConfigurationException {
        String browserFileNamesString = source.browserFileNames();
        try {
            return Utility.listFromCommaDelimitedString(browserFileNamesString);
        } catch (Exception e) {
            throw new ConfigurationException(ConfigurationProperty.BROWSER_FILE_NAMES.getName(), browserFileNamesString, e);
        }
    }

    public File getLogsDirectory() throws ConfigurationException {
        String logsDirectoryString = source.logsDirectory();
        try {
            if (Utility.isEmpty(logsDirectoryString))
                logsDirectoryString = resourceBaseCheckForDefault() + File.separator + "logs";
            File logsDirectory = new File(logsDirectoryString);
            if (!logsDirectory.exists()) {
                logsDirectory.mkdir();
            }
            return logsDirectory;
        } catch (Exception e) {
            throw new ConfigurationException(ConfigurationProperty.LOGS_DIRECTORY.getName(), logsDirectoryString, e);
        }
    }

    public int getPort() throws ConfigurationException {
        String portString = source.port();
        try {
            int port;
            if (Utility.isEmpty(portString))
                port = DEFAULT_PORT;
            else
                port = Integer.parseInt(portString);
            return port;
        } catch (Exception e) {
            throw new ConfigurationException(ConfigurationProperty.PORT.getName(), portString, e);
        }
    }

    public boolean shouldCloseBrowsersAfterTestRuns() throws ConfigurationException {
        String string = source.closeBrowsersAfterTestRuns();
        if (Utility.isEmpty(string))
            return true;
        return Boolean.valueOf(string);
    }

    public File getResourceBase() throws ConfigurationException {
        String resourceBaseString = resourceBaseCheckForDefault();
        try {
            return new File(resourceBaseString);
        } catch (Exception e) {
            throw new ConfigurationException(ConfigurationProperty.RESOURCE_BASE.getName(), resourceBaseString, e);
        }
    }

    private String resourceBaseCheckForDefault() {
        String result = source.resourceBase();
        if (Utility.isEmpty(result))
            result = DEFAULT_RESOURCE_BASE;
        return result;
    }

    public ConfigurationSource getSource() {
        return source;
    }

    public boolean logStatus() {
        String logStatus = source.logStatus();
        if (Utility.isEmpty(logStatus))
            return true;
        return Boolean.valueOf(logStatus);
    }

    public Element asXml() {
        Element configuration = new Element("configuration");

        Element resourceBase = new Element(ConfigurationProperty.RESOURCE_BASE.getName());
        resourceBase.setText(getResourceBase().toString());
        configuration.addContent(resourceBase);

        Element port = new Element(ConfigurationProperty.PORT.getName());
        port.setText(String.valueOf(getPort()));
        configuration.addContent(port);

        Element logsDirectory = new Element(ConfigurationProperty.LOGS_DIRECTORY.getName());
        logsDirectory.setText(getLogsDirectory().toString());
        configuration.addContent(logsDirectory);

        Element browserFileNames = new Element(ConfigurationProperty.BROWSER_FILE_NAMES.getName());
        for (String name : getBrowserFileNames()) {
            Element browserFileName = new Element("browserFileName");
            browserFileName.setText(name);
            browserFileNames.addContent(browserFileName);
        }
        configuration.addContent(browserFileNames);

        Element url = new Element(ConfigurationProperty.URL.getName());
        URL testURL = getTestURL();
        url.setText(testURL == null ? "" : testURL.toString());
        configuration.addContent(url);

        Element closeBrowsers = new Element(ConfigurationProperty.CLOSE_BROWSERS_AFTER_TEST_RUNS.getName());
        closeBrowsers.setText(String.valueOf(shouldCloseBrowsersAfterTestRuns()));
        configuration.addContent(closeBrowsers);

        Element logStatus = new Element(ConfigurationProperty.LOG_STATUS.getName());
        logStatus.setText(String.valueOf(logStatus()));
        configuration.addContent(logStatus);

        Element timeoutSeconds = new Element(ConfigurationProperty.TIMEOUT_SECONDS.getName());
        timeoutSeconds.setText(String.valueOf(getTimeoutSeconds()));
        configuration.addContent(timeoutSeconds);

        Element remoteMachineURLs = new Element(ConfigurationProperty.REMOTE_MACHINE_URLS.getName());
        for (URL machineURL : getRemoteMachineURLs()) {
            Element urlElement = new Element("remoteMachineURL");
            urlElement.setText(machineURL.toString());
            remoteMachineURLs.addContent(urlElement);
        }
        configuration.addContent(remoteMachineURLs);

        return configuration;
    }

    public String[] asArgumentsArray() {
        return new String[] {
            "-" + ConfigurationProperty.BROWSER_FILE_NAMES.getName(), commaSeparatedBrowserFileNames(),
            "-" + ConfigurationProperty.CLOSE_BROWSERS_AFTER_TEST_RUNS.getName(), String.valueOf(shouldCloseBrowsersAfterTestRuns()),
            "-" + ConfigurationProperty.LOGS_DIRECTORY.getName(), getLogsDirectory().getAbsolutePath(),
            "-" + ConfigurationProperty.LOG_STATUS.getName(), String.valueOf(logStatus()),
            "-" + ConfigurationProperty.PORT.getName(), String.valueOf(getPort()),
            "-" + ConfigurationProperty.REMOTE_MACHINE_URLS.getName(), commaSeparatedRemoteMachineURLs(),
            "-" + ConfigurationProperty.RESOURCE_BASE.getName(), getResourceBase().getAbsolutePath(),
            "-" + ConfigurationProperty.TIMEOUT_SECONDS.getName(), String.valueOf(getTimeoutSeconds()),
            "-" + ConfigurationProperty.URL.getName(), getTestURL() == null ? "" : getTestURL().toString()
        };
    }

    private String commaSeparatedBrowserFileNames() {
        return Utility.commaSeparatedString(getBrowserFileNames());
    }

    private String commaSeparatedRemoteMachineURLs() {
        return Utility.commaSeparatedString(getRemoteMachineURLs());
    }

    public int getTimeoutSeconds() {
        String timeoutSecondsString = source.timeoutSeconds();
        if (Utility.isEmpty(timeoutSecondsString))
            return DEFAULT_TIMEOUT_SECONDS;
        return Integer.parseInt(timeoutSecondsString);
    }

    public void ensureValidForServer() {
        try {
            asArgumentsArray();
        } catch (ConfigurationException e) {
            throw e;
        }
    }

    public List<URL> getRemoteMachineURLs() {
        String remoteMachineURLs = source.remoteMachineURLs();
        List<String> strings = Utility.listFromCommaDelimitedString(remoteMachineURLs);
        List<URL> result = new ArrayList<URL>(strings.size());
        for (String string : strings)
            try {
                result.add(new URL(string));
            } catch (MalformedURLException e) {
                throw new ConfigurationException(ConfigurationProperty.REMOTE_MACHINE_URLS.getName(), remoteMachineURLs, e);
            }
        return result;
    }

    public void ensureValidForFarm() {
    }
}