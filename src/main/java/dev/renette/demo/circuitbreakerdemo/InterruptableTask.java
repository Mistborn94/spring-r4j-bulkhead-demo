package dev.renette.demo.circuitbreakerdemo;

import java.util.concurrent.atomic.AtomicBoolean;

public class InterruptableTask {

    private final AtomicBoolean interrupted = new AtomicBoolean(false);
    private final AtomicBoolean success = new AtomicBoolean(false);
    private final AtomicBoolean completed = new AtomicBoolean(false);

    public boolean run(int sleepMillis) {
        try {
            Thread.sleep(sleepMillis);
            success.set(true);
            return true;
        } catch (InterruptedException ignored) {
            interrupted.set(true);
            Thread.currentThread().interrupt();
            return false;
        } finally {
           completed.set(true);
        }
    }

    boolean sucess() {
        return success.get();
    }

    boolean interrupted() {
        return interrupted.get();
    }

    boolean completed() {
        return completed.get();
    }
}