package com.his.report.service;

import com.his.security.WorkbenchMetricProvider;
import com.his.security.CurrentUser;
import java.util.Map;

public interface MyClinicalTodayMetricProvider extends WorkbenchMetricProvider {

    String widgetCode();

    Map<String, Object> summary(CurrentUser user);
}
