package com.teksystems.pip.midtest;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.teksystems.pip.midtest.util.RetryExecutor;

@SpringBootApplication
public class MidTestApplication {

    private static final Logger logger = LoggerFactory.getLogger(MidTestApplication.class);

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        SpringApplication.run(MidTestApplication.class, args);

        // Sample data - includes null, negative and various currencies
        List<Transaction> transactions = List.of(
            new Transaction(UUID.randomUUID().toString(), 120.0, "USD", "NEW"),
            new Transaction(UUID.randomUUID().toString(), 10.0, "EUR", "NEW"),
            new Transaction(UUID.randomUUID().toString(), 75.5, "EUR", "NEW"),
            new Transaction(UUID.randomUUID().toString(), -5.0, "USD", "NEW"),
            new Transaction(UUID.randomUUID().toString(), 200.0, "JPY", "NEW"),
            new Transaction(UUID.randomUUID().toString(), null, "USD", "NEW")
        );

        // Executor for async work - choose a bounded pool size between 2 and 8
        int size = transactions.size();
        int poolSize = size < 2 ? 2 : Math.min(size, 8);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        RetryExecutor retryExecutor = new RetryExecutor(executor);
        try {
            // 1) Clean input: filter invalid transactions (e.g., null or negative amounts) and collect warnings
            List<Transaction> cleaned = transactions.stream()
                .filter(t -> {
                    Double amt = t.getAmount();
                    if (amt == null) {
                        logger.warn("Skipping transaction with null amount: {}", t);
                        return false;
                    }
                    if (amt < 0) {
                        logger.warn("Skipping transaction with negative amount: {}", t);
                        return false;
                    }
                    return true;
                })
                .toList();

            // 2) Streams pipeline: filter amount > 50, mark as PROCESSED, and sum amounts
            List<Transaction> filtered = cleaned.stream()
                .filter(t -> t.getAmount() != null && t.getAmount() > 50.0)
                .map(t -> t.withStatus("PROCESSED"))
                .toList();

            double totalFilteredAmount = filtered.stream()
                .mapToDouble(t -> t.getAmount() == null ? 0.0 : t.getAmount())
                .sum();

            logger.info("Filtered transactions (>50) count: {}", filtered.size());
            logger.info("Total amount of filtered transactions: {}", totalFilteredAmount);

            // 3) Strategy pattern: compute and print commissions per transaction
            CommissionStrategyFactory strategyFactory = new CommissionStrategyFactory();
            filtered.forEach(tx -> {
                CommissionStrategy strategy = strategyFactory.forCurrency(tx.getCurrency());
                Double commission = strategy.calculateCommission(tx.getAmount());
                logger.info("Transaction {} currency={} amount={} commission={}", tx.getId(), tx.getCurrency(),
                    tx.getAmount() == null ? "null" : String.format(Locale.ROOT, "%.2f", tx.getAmount()),
                    commission == null ? 0.0 : String.format(Locale.ROOT, "%.2f", commission));
            });

            // 4) Asynchronous validation: simulate external API calls with retries

            List<CompletableFuture<String>> validations = filtered.stream()
                .map(tx -> retryExecutor.retryAsync(() -> validateTransaction(tx), 3, Duration.ofMillis(500)))
                .toList();

            // Combine results non-blocking and handle when all complete
            CompletableFuture<Void> all = CompletableFuture.allOf(validations.toArray(new CompletableFuture[0]));

            CompletableFuture<List<String>> allResults = all.thenApply(v ->
                validations.stream()
                    .map(CompletableFuture::join)
                    .toList()
            );

            // Block here to print results for demo; in a real app you might return the future
            List<String> results = allResults.get();
            logger.info("Validation results:");
            results.forEach(logger::info);

        } finally {
            // ensure retry executor and thread pools are shutdown cleanly
            retryExecutor.shutdown();
            executor.shutdown();
        }
    }

    // Simulate an external validation call that occasionally fails to emulate retry behaviour
    private static String validateTransaction(Transaction tx) {
        // Validate presence of amount
        if (tx.getAmount() == null) {
            logger.warn("Transaction {} has null amount", tx.getId());
            return String.format("%s - INVALID (amount is null)", tx.getId());
        }

        // Simulate network latency
        try {
            long sleepMs = ThreadLocalRandom.current().nextLong(100, 400);
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Validation interrupted for {}", tx.getId(), e);
            return String.format("%s - validation interrupted", tx.getId());
        }

        // Randomly fail to simulate transient errors
        boolean success = ThreadLocalRandom.current().nextInt(0, 10) < 8; // 80% success
        if (success) {
            logger.debug("Transaction {} validated successfully", tx.getId());
            return String.format("%s - VALID (currency=%s, amount=%.2f)", tx.getId(), tx.getCurrency(), tx.getAmount());
        } else {
            logger.warn("Transient validation error for {}", tx.getId());
            throw new RuntimeException("Transient validation error for " + tx.getId());
        }
    }

    // Transaction model as an immutable nested static class
    public static final class Transaction {
        private final String id;

        private final Double amount;
        private final String currency;
        private final String status;

        public Transaction(String id, Double amount, String currency, String status) {
            this.id = Objects.requireNonNull(id, "id must not be null");
            this.amount = amount;
            this.currency = Objects.requireNonNull(currency, "currency must not be null");
            this.status = status == null ? "NEW" : status;

            // Negative amounts are handled/logged by the pipeline filter; constructor remains lightweight
        }

        public String getId() { return id; }
        public Double getAmount() { return amount; }
        public String getCurrency() { return currency; }
        public String getStatus() { return status; }

        public Transaction withStatus(String newStatus) {
            return new Transaction(this.id, this.amount, this.currency, newStatus);
        }

        @Override
        public String toString() {
            return "Transaction{" +
                "id='" + id + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                '}';
        }
    }

    // Strategy pattern for commission calculation
    public interface CommissionStrategy {
        // prefer Object wrapper for amount
        Double calculateCommission(Double amount);
    }

    public static class USDCommissionStrategy implements CommissionStrategy {
        public Double calculateCommission(Double amount) {
            if (amount == null) return 0.0;
            return amount * 0.02; // 2%
        }
    }

    public static class EURCommissionStrategy implements CommissionStrategy {
        public Double calculateCommission(Double amount) {
            if (amount == null) return 0.0;
            return amount * 0.01; // 1%
        }
    }

    public static class DefaultCommissionStrategy implements CommissionStrategy {
        public Double calculateCommission(Double amount) {
            if (amount == null) return 0.0;
            return amount * 0.05; // 5%
        }
    }

    public static class CommissionStrategyFactory {
        public CommissionStrategy forCurrency(String currency) {
            if (currency == null) return new DefaultCommissionStrategy();
            return switch (currency.toUpperCase(Locale.ROOT)) {
                case "USD" -> new USDCommissionStrategy();
                case "EUR" -> new EURCommissionStrategy();
                default -> new DefaultCommissionStrategy();
            };
        }
    }

}
