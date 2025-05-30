package org.baioret.core.schedulable;

import org.baioret.core.logging.LogService;

import java.util.Map;
import java.util.Random;

public class Task1 implements Schedulable {
    private final Random random = new Random();

    @Override
    public boolean execute(Map<String, String> params) {

        int ex_time = random.nextInt(5000) + 1;

        if (params.containsKey("ID") && ex_time % 2 == 0) {
            try {
                Thread.sleep(ex_time);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("[TASK 1] Message for User with ID " + params.get("ID") + ": " + params.get("message"));
            return true;
        }
        return false;
    }
}
