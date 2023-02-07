package dev.renette.demo.circuitbreakerdemo;

import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4jBulkheadProvider;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.cloud.circuitbreaker.bulkhead.resilience4j.enabled=true")
class BulkheadEnabledTests {

    public static final String CBID = "test";

    @Autowired
    Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    @Autowired
    Resilience4jBulkheadProvider bulkheadProvider;

    @BeforeEach
    void init() {
        circuitBreakerFactory.configure(builder -> builder.timeLimiterConfig(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(200))
                .build()), CBID);
    }

    @Test
    void bulkheadEnabled() {
        bulkheadProvider.configure(builder -> builder.bulkheadConfig(BulkheadConfig.custom()
                .maxConcurrentCalls(1).build()), CBID);
        var task = new InterruptableTask();
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create(CBID);

        var ex = assertThrows(NoFallbackAvailableException.class, () -> {
            circuitBreaker.run(() -> task.run(10_000));
        });

        assertTrue(ex.getCause() instanceof TimeoutException, "Timeout exception thrown");

        assertAll(
                () -> assertTrue(task.completed(), "Task should be completed"),
                () -> assertTrue(task.interrupted(), "Task should be interrupted"),
                () -> assertFalse(task.sucess(), "Task shouldn't successfully")
        );

    }

}