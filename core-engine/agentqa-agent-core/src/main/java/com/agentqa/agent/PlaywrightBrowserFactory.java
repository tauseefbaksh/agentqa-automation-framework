package com.agentqa.agent;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.util.Objects;

/**
 * Creates and owns one headless Chromium browser session.
 *
 * <p>The factory is deliberately {@link AutoCloseable}: callers should use it
 * in a try-with-resources block so browser processes are not leaked when a
 * test fails.</p>
 */
public final class PlaywrightBrowserFactory implements AutoCloseable {
    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private volatile boolean closed;

    private PlaywrightBrowserFactory(Playwright playwright, Browser browser,
                                     BrowserContext context, Page page) {
        this.playwright = Objects.requireNonNull(playwright, "playwright");
        this.browser = Objects.requireNonNull(browser, "browser");
        this.context = Objects.requireNonNull(context, "context");
        this.page = Objects.requireNonNull(page, "page");
    }

    /**
     * Launches a headless Chromium browser and creates an isolated context and page.
     *
     * @return a closeable browser session
     */
    public static PlaywrightBrowserFactory createHeadlessChromium() {
        Playwright playwright = Playwright.create();
        Browser browser = null;
        BrowserContext context = null;
        try {
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            context = browser.newContext();
            return new PlaywrightBrowserFactory(playwright, browser, context, context.newPage());
        } catch (RuntimeException exception) {
            closeQuietly(context);
            closeQuietly(browser);
            playwright.close();
            throw exception;
        }
    }

    /** Alias used by executors that always run browser checks headlessly. */
    public static PlaywrightBrowserFactory create() {
        return createHeadlessChromium();
    }

    public BrowserContext context() {
        ensureOpen();
        return context;
    }

    public Page page() {
        ensureOpen();
        return page;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        RuntimeException failure = null;
        try {
            page.close();
        } catch (RuntimeException exception) {
            failure = exception;
        }
        try {
            context.close();
        } catch (RuntimeException exception) {
            if (failure == null) {
                failure = exception;
            }
        }
        try {
            browser.close();
        } catch (RuntimeException exception) {
            if (failure == null) {
                failure = exception;
            }
        }
        try {
            playwright.close();
        } catch (RuntimeException exception) {
            if (failure == null) {
                failure = exception;
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("The Playwright browser session is closed");
        }
    }

    private static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {
                // Preserve the original browser initialization failure.
            }
        }
    }

}
