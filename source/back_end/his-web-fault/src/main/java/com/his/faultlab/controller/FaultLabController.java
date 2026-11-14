package com.his.faultlab.controller;

import com.his.faultlab.service.FaultLabService;
import com.his.faultlab.vo.FaultResultVO;
import com.his.faultlab.vo.FaultStatusVO;
import com.his.faultlab.vo.ScenarioVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 演练触发器。演练进程只绑 127.0.0.1，不对外；路径统一 /fault/{场景}/{动作}。
 * 这里的接口故意「一点就应」，参数全部带上限，由 service 层夹取。
 */
@RestController
@RequestMapping("/fault")
public class FaultLabController {

    private final FaultLabService faultLabService;

    public FaultLabController(FaultLabService faultLabService) {
        this.faultLabService = faultLabService;
    }

    @GetMapping("/catalog")
    public List<ScenarioVO> catalog() {
        return faultLabService.catalog();
    }

    @GetMapping("/status")
    public FaultStatusVO status() {
        return faultLabService.status();
    }

    @PostMapping("/cpu/regex")
    public FaultResultVO cpuRegex(@RequestParam(defaultValue = "20") int seconds,
                                  @RequestParam(defaultValue = "25") int chars) {
        return faultLabService.cpuRegex(seconds, chars);
    }

    @PostMapping("/cpu/spin")
    public FaultResultVO cpuSpin(@RequestParam(defaultValue = "4") int threads,
                                 @RequestParam(defaultValue = "20") int seconds) {
        return faultLabService.cpuSpin(threads, seconds);
    }

    @PostMapping("/cpu/stop")
    public FaultResultVO cpuStop() {
        return faultLabService.cpuStop();
    }

    @PostMapping("/heap/leak")
    public FaultResultVO heapLeak(@RequestParam(defaultValue = "128") int mb) {
        return faultLabService.heapLeak(mb);
    }

    @PostMapping("/heap/oom")
    public FaultResultVO heapOom(@RequestParam(defaultValue = "256") int mb,
                                 @RequestParam(defaultValue = "true") boolean releaseAfter) {
        return faultLabService.heapOom(mb, releaseAfter);
    }

    @PostMapping("/heap/release")
    public FaultResultVO heapRelease() {
        return faultLabService.heapRelease();
    }

    @PostMapping("/offheap/leak")
    public FaultResultVO offheapLeak(@RequestParam(defaultValue = "128") int mb) {
        return faultLabService.directLeak(mb);
    }

    @PostMapping("/offheap/release")
    public FaultResultVO offheapRelease() {
        return faultLabService.directRelease();
    }

    @PostMapping("/thread/slow")
    public FaultResultVO threadSlow(@RequestParam(defaultValue = "3000") int delayMs) {
        return faultLabService.threadSlow(delayMs);
    }

    @PostMapping("/thread/load")
    public FaultResultVO threadLoad(@RequestParam(defaultValue = "300") int requests,
                                    @RequestParam(defaultValue = "3000") int delayMs) {
        return faultLabService.threadLoad(requests, delayMs);
    }

    @PostMapping("/thread/downstream")
    public FaultResultVO threadDownstream(@RequestParam(defaultValue = "3000") int delayMs) {
        return faultLabService.threadDownstream(delayMs);
    }

    @PostMapping("/db/init")
    public FaultResultVO dbInit() {
        return faultLabService.dbInit();
    }

    @PostMapping("/db/slowQuery")
    public FaultResultVO dbSlowQuery(@RequestParam(defaultValue = "张") String keyword) {
        return faultLabService.dbSlowQuery(keyword);
    }

    @PostMapping("/db/hold")
    public FaultResultVO dbHold(@RequestParam(defaultValue = "20") int requests,
                                @RequestParam(defaultValue = "4000") int holdMs) {
        return faultLabService.dbHold(requests, holdMs);
    }

    @PostMapping("/db/poolStatus")
    public FaultResultVO dbPoolStatus() {
        return faultLabService.dbPoolStatus();
    }

    @PostMapping("/deadlock")
    public FaultResultVO deadlock() {
        return faultLabService.deadlock();
    }

    @PostMapping("/cache/stampede")
    public FaultResultVO cacheStampede(@RequestParam(defaultValue = "40") int concurrency) {
        return faultLabService.cacheStampede(concurrency);
    }

    @PostMapping("/cache/singleflight")
    public FaultResultVO cacheSingleFlight(@RequestParam(defaultValue = "40") int concurrency) {
        return faultLabService.cacheSingleFlight(concurrency);
    }

    @PostMapping("/gc/pressure")
    public FaultResultVO gcPressure(@RequestParam(defaultValue = "20") int seconds,
                                    @RequestParam(defaultValue = "32") int mbPerSecond) {
        return faultLabService.gcPressure(seconds, mbPerSecond);
    }
}
