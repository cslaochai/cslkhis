package com.his.medicaltech.service;

import com.his.system.entity.CurrentUser;
import com.his.system.provider.WorkbenchMetricProvider;

import java.util.Map;

public interface WardNursingTodayMetricProvider extends WorkbenchMetricProvider {

    String widgetCode();

    Map<String, Object> summary(CurrentUser user);
}
