package com.his.faultlab.service;

import com.his.faultlab.vo.FaultResultVO;
import com.his.faultlab.vo.FaultStatusVO;
import com.his.faultlab.vo.ScenarioVO;

import java.util.List;

/**
 * 故障演练入口的业务收口：Controller 只转参数，所有夹取、上限、状态拼装都在这里。
 */
public interface FaultLabService {

    FaultStatusVO status();

    List<ScenarioVO> catalog();

    FaultResultVO cpuRegex(int seconds, int chars);

    FaultResultVO cpuSpin(int threads, int seconds);

    FaultResultVO cpuStop();

    FaultResultVO heapLeak(int mb);

    FaultResultVO heapOom(int mb, boolean releaseAfter);

    FaultResultVO heapRelease();

    FaultResultVO directLeak(int mb);

    FaultResultVO directRelease();

    FaultResultVO threadSlow(int delayMs);

    FaultResultVO threadLoad(int requests, int delayMs);

    FaultResultVO threadDownstream(int delayMs);

    FaultResultVO dbInit();

    FaultResultVO dbSlowQuery(String keyword);

    FaultResultVO dbHold(int requests, int holdMs);

    FaultResultVO dbPoolStatus();

    FaultResultVO deadlock();

    FaultResultVO cacheStampede(int concurrency);

    FaultResultVO cacheSingleFlight(int concurrency);

    FaultResultVO gcPressure(int seconds, int mbPerSecond);
}
