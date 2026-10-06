package com.his.report.service;

import com.his.system.provider.WorkbenchMetricProvider;
import com.his.system.entity.CurrentUser;
import java.util.Map;

public interface DeptVisitRankMetricProvider extends WorkbenchMetricProvider {

    String widgetCode();

    Map<String, Object> summary(CurrentUser user);
}
