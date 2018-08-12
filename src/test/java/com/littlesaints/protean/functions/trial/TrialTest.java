package com.littlesaints.protean.functions.trial;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class TrialTest {

    private final static int TEST_DEFAULT_DELAY_IN_MILLIS = 1;

    private final static int TEST_DEFAULT_DELAY_INCREASE_RETRIES = 100;

    @Test
    public void test_exit_after_n_retries_for_unbounded_counter() {
        final int simulatedTries = 20;
        final Strategy strategy = Strategy.builder()
            .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
            .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
            .build();

        testRetries(simulatedTries, strategy);
    }

    @Test
    public void test_unbounded_attempts_constant() {
        assertEquals(Constants.UNBOUNDED_TRIES, -1);
    }

    @Test
    public void test_exit_after_n_retries() {
        final int simulatedTries = 5;
        final Strategy strategy = Strategy.builder()
            .maxTriesWithDelay(10)
            .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
            .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS).build();

        testRetries(simulatedTries, strategy);
    }

    @Test
    public void test_exit_after_max_retries() {
        final int maxTries = 5;
        final Strategy strategy = Strategy.builder()
            .maxTriesWithDelay(maxTries)
            .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
            .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
            .build();

        testRetries(maxTries, strategy);
    }

    @Test
    public void test_no_increase_in_delay_after_threshold() {
        int delayIncreaseRetries = 2;
        int maxDelayInMillis = 4;
        final int maxTries = 10;

        final Strategy strategy = Strategy.builder()
            .maxTriesWithDelay(maxTries)
            .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
            .triesUntilDelayIncrease(delayIncreaseRetries)
            .delayThresholdInMillis(maxDelayInMillis).build();

        testRetryDelay(maxTries + 10, maxDelayInMillis, strategy);
    }

    @Test
    public void test_increase_in_delay_after_increase_threshold() {
        int delayIncreaseRetries = 2;

        final Strategy strategy = Strategy.builder()
            .maxTriesWithDelay(10)
            .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
            .triesUntilDelayIncrease(delayIncreaseRetries)
            .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS * 3)
            .build();

        testRetryDelay(delayIncreaseRetries * 2, strategy.getDelayBetweenTriesInMillis() * 2, strategy);
    }

    @Test
    public void testResetAfterMaxRetries() {
        final int maxRetries = 5;
        final Strategy strategy = Strategy.builder()
            .maxTriesWithDelay(maxRetries)
            .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
            .delayThresholdInMillis(TEST_DEFAULT_DELAY_IN_MILLIS).build();

        testRetries(maxRetries, strategy, 2);
    }

    @Test
    public void testResetAfterThresholdDelay() {
        int delayIncreaseRetries = 2;
        int maxDelayInMillis = 4;

        final Strategy strategy = Strategy.builder()
            .maxTriesWithDelay(10)
            .delayBetweenTriesInMillis(TEST_DEFAULT_DELAY_IN_MILLIS)
            .triesUntilDelayIncrease(delayIncreaseRetries)
            .delayThresholdInMillis(maxDelayInMillis)
            .build();

        testRetryDelay(delayIncreaseRetries * 2, strategy.getDelayBetweenTriesInMillis() * 2, strategy, 2);
    }

    private void testRetryDelay(int simulatedTries, long expectedDelayInMillis, Strategy strategy) {
        testRetryDelay(simulatedTries, expectedDelayInMillis, strategy, 1);
    }

    private void testRetryDelay(int simulatedInvocations, long expectedDelayInMillis, Strategy strategy, int runs) {
        final AtomicInteger counter = new AtomicInteger(0);
        Trial <Integer> trial = Trial.of(strategy, counter::incrementAndGet, i -> i == simulatedInvocations);
        for (int i = 0; i < runs; i++) {
            counter.set(0);
            trial.get();
            assertEquals(expectedDelayInMillis, trial.getCurrentDelayBetweenTriesInMillis());
        }
    }

    private void testRetries(final int simulatedTries, Strategy strategy) {
        testRetries(simulatedTries, strategy, 1);
    }

    private void testRetries(final int simulatedInvocations, Strategy strategy, int runs) {
        final AtomicInteger counter = new AtomicInteger(0);
        Trial <Integer> trial = Trial.of(strategy, counter::incrementAndGet, i -> i == simulatedInvocations);

        Optional<Integer> result;
        for (int i = 0; i < runs; i++) {
            counter.set(0);
            result = trial.get();

            long maxAttemptedTries = trial.getAttemptedTriesWithYield() + trial.getAttemptedTriesWithDelay();
            if (strategy.getMaxTriesWithDelay() == Constants.UNBOUNDED_TRIES) {
                assertEquals(maxAttemptedTries, 0);
            }
            else {
                //Unbounded attempts cause counter to NOT increment, to avoid overflow.
                assertEquals(simulatedInvocations, maxAttemptedTries + 1);
                assertEquals(counter.get(), maxAttemptedTries + 1);
            }
            assertEquals(simulatedInvocations, counter.get());
            assertEquals(result.get().intValue(), counter.get());
        }
    }

    //	creating this method because of findbugs "ICAST_INTEGER_MULTIPLY_CAST_TO_LONG"
    private long power(int num, long pow) {
        long result = num;
        for (int i = 2; i <= pow; i++) {
            result *= num;
        }
        return result;
//		return (int)(Math.pow(num, pow));
    }

}