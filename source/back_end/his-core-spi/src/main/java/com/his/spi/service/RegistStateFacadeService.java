package com.his.spi.service;

import com.his.spi.vo.QueueStateRowVO;
import com.his.spi.vo.RegistStateRowVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 挂号与队列状态端口：结诊联动「读挂号行、推进挂号状态、结束队列」的写入出口，状态事实仍由挂号域持有。
 *
 * <p>状态推进一律带「期望状态集合」做乐观校验：只有当前状态落在集合内才写，返回 false 让调用方
 * 当场提示状态已变更，避免并发下把已退号或已重看的就诊洗成已完成。
 */
public interface RegistStateFacadeService {

    /**
     * 按挂号ID取状态行。
     *
     * <p>用于开检查/检验申请单时把科室与接诊医生快照到申请单上。
     *
     * @return 挂号状态行；记录不存在返回 null
     */
    RegistStateRowVO getRegistRow(Long registId);

    /**
     * 把挂号推进到目标状态，仅当当前状态命中期望集合。
     *
     * <p>用于结诊时把「已受理」的挂号记成「已完成」。
     *
     * @param registId        挂号ID
     * @param targetStatus    目标状态
     * @param expectStatuses  允许推进的当前状态集合
     * @return true=已推进；false=状态已被改动，本次未写入
     */
    boolean updateRegistStatus(Long registId, Integer targetStatus, List<Integer> expectStatuses);

    /**
     * 按队列ID取状态行。
     *
     * <p>用于结诊前确认队列入存在并拿到该队列的接诊医生。
     *
     * @return 队列状态行；记录不存在返回 null
     */
    QueueStateRowVO getQueueRow(Long queueId);

    /**
     * 结束队列：写入队列状态与结束时刻，仅当当前状态命中期望集合。
     *
     * <p>用于结诊时把「就诊中」的队列收口，结束时刻由调用方给出以保证与病历提交时刻同源。
     */
    boolean finishQueue(Long queueId, Integer targetStatus, LocalDateTime endTime, List<Integer> expectStatuses);
}
