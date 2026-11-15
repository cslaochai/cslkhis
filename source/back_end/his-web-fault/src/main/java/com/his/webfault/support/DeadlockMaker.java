package com.his.webfault.support;

import com.his.webfault.config.FaultLabProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Java 级死锁：两个线程按相反顺序拿同一对锁。
 * <p>
 * 真实来源通常是「A 先锁库存再锁账单、B 先锁账单再锁库存」，
 * 现象是接口不报错也不返回（线程永久 BLOCKED），QPS 归零但 CPU 不高。
 * <p>
 * 注意：死锁不可自愈，这两个线程会一直停在那里——线上只能重启。
 * 所以这里只允许触发一次，重启 lab 进程即可复位。
 */
@Component
public class DeadlockMaker {

    private final FaultLabProperties props;
    private final FaultExecutor executor;
    private final ReentrantLock inventoryLock = new ReentrantLock();
    private final ReentrantLock billLock = new ReentrantLock();
    private final AtomicBoolean triggered = new AtomicBoolean(false);

    public DeadlockMaker(FaultLabProperties props, FaultExecutor executor) {
        this.props = props;
        this.executor = executor;
    }

    public String trigger() {
        if (!props.isEnabled()) {
            return "REJECTED: fault.enabled=false";
        }
        if (!triggered.compareAndSet(false, true)) {
            return "REJECTED: 已触发过一次；死锁线程不可回收，需重启 lab 才能再次演练";
        }
        executor.run("fault-deadlock-A", () -> {
            inventoryLock.lock();
            try {
                sleep(150);
                billLock.lock();
                try {
                    sleep(60_000);
                } finally {
                    billLock.unlock();
                }
            } finally {
                inventoryLock.unlock();
            }
        });
        executor.run("fault-deadlock-B", () -> {
            billLock.lock();
            try {
                sleep(150);
                inventoryLock.lock();
                try {
                    sleep(60_000);
                } finally {
                    inventoryLock.unlock();
                }
            } finally {
                billLock.unlock();
            }
        });
        return "STARTED: 2 threads, 相反加锁顺序；10 秒内执行 jcmd <pid> Thread.print 可直接看到 Found one Java-level deadlock";
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
