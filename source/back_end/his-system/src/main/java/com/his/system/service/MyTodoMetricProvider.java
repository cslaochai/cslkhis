package com.his.system.service;

import com.his.system.provider.WorkbenchMetricProvider;
import com.his.system.entity.CurrentUser;
import java.util.Map;

public interface MyTodoMetricProvider extends WorkbenchMetricProvider {

    String widgetCode();

    Map<String, Object> summary(CurrentUser user);
}
