package com.teksystems.pip.midtest.util;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple retry executor to run blocking Suppliers asynchronously with retries and backoff.
 */
public class RetryExecutor {
    private static final Logger logger = LoggerFactory.getLogger(RetryExecutor.class);

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    public RetryExecutor(ExecutorService executor) {
        this.executor = executor;
        this.scheduler = new ScheduledThreadPoolExecutor(1);
    }

    public <T> CompletableFuture<T> retryAsync(Supplier<T> supplier, int maxAttempts, Duration backoff) {
        CompletableFuture<T> result = new CompletableFuture<>();
        attempt(supplier, maxAttempts, backoff.toMillis(), result, maxAttempts);
        return result;
    }

    private <T> void attempt(Supplier<T> supplier, int remainingAttempts, long backoffMs, CompletableFuture<T> result, int originalAttempts) {
        int attemptNumber = originalAttempts - remainingAttempts + 1;
        logger.debug("RetryExecutor: attempting #{} (remaining={})", attemptNumber, remainingAttempts);

        if (remainingAttempts <= 0) {
            RuntimeException ex = new RuntimeException("Exhausted retries");
            logger.error("RetryExecutor: exhausted retries after {} attempts", originalAttempts, ex);
            result.completeExceptionally(ex);
            return;
        }

        CompletableFuture.supplyAsync(supplier, executor)
            .whenComplete((r, ex) -> {
                if (ex == null) {
                    logger.debug("RetryExecutor: attempt #{} succeeded", attemptNumber);
                    result.complete(r);
                } else {
                    logger.warn("RetryExecutor: attempt #{} failed: {}", attemptNumber, ex.toString());
                    // schedule retry after backoff
                    scheduler.schedule(() -> attempt(supplier, remainingAttempts - 1, backoffMs, result, originalAttempts), backoffMs, TimeUnit.MILLISECONDS);
                }
            });
    }

    /**
     * Shutdown scheduler used for retries. Does not shutdown the provided executor.
     */
    public void shutdown() {
        try {
            logger.debug("Shutting down RetryExecutor scheduler");
            scheduler.shutdown();
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Interrupted while shutting down retry scheduler", e);
        }
    }
}
