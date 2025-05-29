package org.baioret.core.schedulable;

import org.baioret.core.logging.LogService;

import java.util.Map;
import java.util.Random;

public class Task2 implements Schedulable {
    private final Random random = new Random();

    @Override
    public boolean execute(Map<String, String> params) {

        int ex_time = random.nextInt(3000) + 1;

        if (params.containsKey("ID") && ex_time % 2 != 0) {
            try {
                Thread.sleep(ex_time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Task 2. Message for User with ID " + params.get("ID") + ": " + params.get("message"));
            LogService.logger.info("Task 2. Message for User with ID " + params.get("ID") + ": " + params.get("message"));
            return true;
        }
        return false;
    }
}
