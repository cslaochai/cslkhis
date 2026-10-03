package com.his.system.service;

import com.his.security.WorkbenchMetricProvider;
import com.his.security.CurrentUser;
import java.util.Map;

public interface MyTodoMetricProvider extends WorkbenchMetricProvider {

    String widgetCode();

    Map<String, Object> summary(CurrentUser user);
}
