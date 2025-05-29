package org.baioret.integrationtest.worker;

import org.baioret.core.schedulable.Schedulable;
import org.baioret.core.worker.TaskWorkerPool;
import org.baioret.integrationtest.EuropeanDateFormatter;

import java.time.LocalDateTime;
import java.util.*;

public class WorkerManager {
    private static final Random RANDOM = new Random();

    private final int maxWorkerThreads;
    private final int minWorkerThreads;
    private final Map<Integer, Class<? extends Schedulable>> categories;
    private final TaskWorkerPool workerPool;

    public WorkerManager(int maxWorkerThreads, int minWorkerThreads,
                         Map<Integer, Class<? extends Schedulable>> categories) {
        this.maxWorkerThreads = maxWorkerThreads;
        this.minWorkerThreads = minWorkerThreads;
        this.categories = categories;
        this.workerPool = new TaskWorkerPool();
    }

    public void initRandomWorker() {
        Class<? extends Schedulable> randomCategory =
                categories.get(RANDOM.nextInt(categories.size()));
        int randomThreadCount = RANDOM.nextInt(maxWorkerThreads) + minWorkerThreads;
        workerPool.initWorker(randomCategory, randomThreadCount);
        printInitWorkerInfo(randomCategory.getSimpleName(), randomThreadCount);
    }

    private void printInitWorkerInfo(String randomCategory, int threadCount) {
        System.out.println("Создан новый Worker:" +
                "\nКатегория - " + randomCategory +
                "\nКол-во потоков - " + threadCount +
                "\nИмя потока - " + Thread.currentThread().getName() +
                "\nДата создания - " + getStringTime(LocalDateTime.now()) + "\n");
    }

    public void stopRandomWorker() {
        String randomCategory = categories
                .get(RANDOM.nextInt(categories.size()))
                .getSimpleName();
        Optional<List<UUID>> workersIdOptional = workerPool.getWorkersIdByCategory(randomCategory);
        if (workersIdOptional.isPresent()) {
            List<UUID> workersId = workersIdOptional.get();
            if (!workersId.isEmpty()) {
                UUID randomWorkerId = workersId.get(RANDOM.nextInt(workersId.size()));
                workerPool.shutdownWorker(randomCategory, randomWorkerId);
                printStoppedWorkerInfo(randomCategory, randomWorkerId);
            }
        }
    }

    private String getStringTime(LocalDateTime time) {
        return EuropeanDateFormatter.getFromLocalDateTime(time);
    }

    private void printStoppedWorkerInfo(String randomCategory, UUID randomWorkerId) {
        System.out.println("Остановлен worker: " +
                "\nКатегория - " + randomCategory +
                "\nId - " + randomWorkerId +
                "\nДата остановки - " + getStringTime(LocalDateTime.now()) + "\n");
    }
}
