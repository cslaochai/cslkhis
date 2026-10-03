package com.his.ai.service;

import com.his.ai.vo.AiHealthVO;

public interface AiHealthService {

    AiHealthVO runtimeStatus();

    void refreshCodeCache();
}
