package com.his.report.service;

import com.his.security.WorkbenchMetricProvider;
import com.his.security.CurrentUser;
import java.util.Map;

public interface HospitalTodayMetricProvider extends WorkbenchMetricProvider {

    String widgetCode();

    Map<String, Object> summary(CurrentUser user);
}
