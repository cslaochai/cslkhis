package com.his.medicaltech.service;

import com.his.medicaltech.dto.ExamApptDTO;
import com.his.medicaltech.entity.BizExamDevice;
import com.his.medicaltech.entity.BizExamSlot;
import com.his.medicaltech.vo.ExamApptVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 检查号源服务：设备开放时段切成分时段格子后的生成、看板、锁放号、对账与占退号收口。
 */
public interface ExamSlotService {

    /**
     * 生成（补齐）预约设备的分时段格子，可连排多天且不改动已有格子。
     * 用于检查预约「设备与号源」页的号源投放。
     */
    ExamApptVO.SlotEnsureVO ensureSlots(ExamApptDTO.SlotEnsure dto);

    /**
     * 号源看板：某设备某日的格子计数与每格实际占号者指认。
     * 用于检查预约「设备与号源」页的号源看板展示。
     */
    ExamApptVO.SlotBoardVO board(ExamApptDTO.SlotQuery dto);

    /**
     * 锁号/放号单个时段：格子里还有人占号时拒绝锁。
     * 用于号源看板上对格子的锁号、放号操作。
     */
    void toggle(ExamApptDTO.SlotToggle dto);

    /**
     * 号源对账：以预约单为事实独立复算格子占用计数，报出漂移并修正。
     * 用于检查预约「设备与号源」页人工核对号源计数（一次跨度不超 92 天）。
     */
    ExamApptVO.SlotRecalcVO recalc(ExamApptDTO.SlotRecalc dto);

    /**
     * 锁住设备后幂等补齐某日格子，按开始时刻升序返回当前开放窗口内的格子。
     * 预约占号与取消/改约退号环节取当日格子时调用，必须运行在事务内。
     */
    List<BizExamSlot> ensureLockedDay(BizExamDevice device, LocalDate date);

    /**
     * 逐格加一占号，超出设备并行容量即失败回滚。
     * 预约确认、改约签到落单后占用号源时调用。
     */
    void claim(BizExamDevice device, LocalDate date, List<BizExamSlot> cells, int[] span);

    /**
     * 逐格减一退号。
     * 预约取消、改约或爽约判定收尾释放号源时调用。
     */
    void release(BizExamDevice device, LocalDate date, List<BizExamSlot> cells, int[] span);

    /**
     * 算出检查起止分钟覆盖的格子下标区间；跨过午休断档或超出设备开放窗口即拒绝。
     * 预约占号与空闲格子探测时用于格子定位。
     */
    int[] spanOf(List<BizExamSlot> cells, int startMin, int endMin);

    /**
     * 取预约设备台账行，不存在即业务报错。
     * 号源对账指定单台设备时用于设备定位。
     */
    BizExamDevice requireDevice(Long deviceId);
}
