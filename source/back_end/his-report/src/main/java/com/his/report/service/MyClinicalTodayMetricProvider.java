package com.his.report.service;

import com.his.security.provider.WorkbenchMetricProvider;
import com.his.security.entity.CurrentUser;
import java.util.Map;

public interface MyClinicalTodayMetricProvider extends WorkbenchMetricProvider {

    String widgetCode();

    Map<String, Object> summary(CurrentUser user);
}
