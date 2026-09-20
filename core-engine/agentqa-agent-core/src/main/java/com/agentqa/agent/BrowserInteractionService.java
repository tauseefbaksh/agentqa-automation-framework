package com.agentqa.agent;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Objects;

/**
 * Foundational browser interaction facade used by higher-level agents.
 */
public final class BrowserInteractionService implements AutoCloseable {
    private final PlaywrightBrowserFactory browserFactory;
    private final Page page;
    private boolean closed;

    public BrowserInteractionService(PlaywrightBrowserFactory browserFactory) {
        this.browserFactory = Objects.requireNonNull(browserFactory, "browserFactory");
        this.page = browserFactory.page();
    }

    /** Navigates the managed page to an absolute or relative URL. */
    public void navigateTo(String url) {
        ensureOpen();
        page.navigate(Objects.requireNonNull(url, "url"));
    }

    /** Returns a live Playwright locator for the supplied selector. */
    public Locator locate(String selector) {
        ensureOpen();
        return page.locator(Objects.requireNonNull(selector, "selector"));
    }

    /** Extracts the visible text represented by the supplied selector. */
    public String extractText(String selector) {
        return locate(selector).innerText();
    }

    /**
     * Closes the entire browser session, including the page, context, browser,
     * and Playwright driver. Safe to call more than once.
     */
    @Override
    public synchronized void close() {
        if (!closed) {
            closed = true;
            browserFactory.close();
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("The browser interaction service is closed");
        }
    }
}
