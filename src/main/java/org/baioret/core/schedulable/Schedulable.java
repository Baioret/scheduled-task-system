package org.baioret.core.schedulable;

import java.util.Map;

public interface Schedulable {

    boolean execute(Map<String, String> params);
}