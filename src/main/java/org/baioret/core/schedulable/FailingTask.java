package org.baioret.core.schedulable;

import java.util.Map;

public class FailingTask implements Schedulable {

    @Override
    public boolean execute(Map<String, String> params) {
        return false;
    }
}
