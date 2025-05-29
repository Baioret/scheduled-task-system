package org.baioret.integrationtest;

import java.util.Random;

public abstract class TestThreads {
    protected static final Random RANDOM = new Random();

    protected void sleep(int boundMillisToSleep) {
        try {
            Thread.sleep(RANDOM.nextInt(boundMillisToSleep));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Thread[] initThreads(int threadCount, int boundMillisToSleep) {
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = createInitThreads(boundMillisToSleep);
            threads[i].start();
        }
        return threads;
    }

    public Thread[] stoppingThreads(int threadCount, int boundMillisToSleep) {
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = createStoppingThread(boundMillisToSleep);
            threads[i].start();
        }
        return threads;
    }

    protected void setupThreadName(Thread thread, String threadName) {
        long threadId = thread.getId();
        String threadFullName = threadName + "-" + threadId;
        thread.setName(threadFullName);
    }

    protected abstract Thread createInitThreads(int boundMillisToSleep);

    protected abstract Thread createStoppingThread(int boundMillisToSleep);
}
