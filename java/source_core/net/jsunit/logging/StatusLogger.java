package net.jsunit.logging;

public interface StatusLogger {
    public void log(String message, boolean includeDate);

    void flush();
}
