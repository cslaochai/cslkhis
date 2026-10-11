package com.his.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.system.dto.ScheduleSlotUpsertDTO;
import com.his.system.entity.BizClinicSource;
import com.his.system.entity.BizClinicSourceSlot;
import com.his.system.entity.BizClinicSourceSlotTemplate;
import com.his.system.vo.ScheduleSlotVO;

import java.util.Collection;
import java.util.List;

/**
 * 排班时间片段服务。
 */
public interface BizClinicSourceSlotService extends IService<BizClinicSourceSlot> {

    /**
     * 把班次时间窗切成半小时段并均分号源落库（余数给前面的段），Σ段写回主表。
     */
    List<BizClinicSourceSlot> generateSlots(Long scheduleId, String startTime, String endTime,
                                            Integer totalSource, Integer appointmentSource);

    /**
     * 按模板片段配置生成段（Σ必须与主表号源一致，由模板保存侧校验保证），Σ段写回主表。
     */
    List<BizClinicSourceSlot> generateFromTemplate(Long scheduleId, List<BizClinicSourceSlotTemplate> tplSlots);

    /**
     * 时间窗或号源变化后的段重算：
     */
    void regenerateForSchedule(BizClinicSource schedule, Integer newTotal, Integer newAppointment);

    /**
     * 加号均摊到段（余数给前面的段）：段 total/available/added 同加，不动预约池；Σ段写回主表。
     */
    void spreadAddSource(Long scheduleId, int addNum);

    /**
     * 段级号源编辑（号源密度按需调整的唯一写入口）：
     */
    void updateSlotSources(ScheduleSlotUpsertDTO dto);

    /**
     * Σ段写回主表 6 个号源字段（available = Σtotal - Σused）。
     */
    void syncSumToSchedule(Long scheduleId);

    /**
     * 段状态随主表联动（停诊→段全停：扣减 SQL 带 status=1，停用段不可再挂）。
     */
    void syncStatusToSlots(Long scheduleId, Integer status);

    /**
     * 物理删除排班下的全部段（排班删除/时间窗重建用）。
     */
    void physicalDeleteByScheduleId(Long scheduleId);

    /**
     * 查某排班的段列表（seq 升序）。
     */
    List<BizClinicSourceSlot> listByScheduleId(Long scheduleId);

    /**
     * 批量查多条排班的段列表（按 scheduleId 升序、段 seq 升序）。
     *
     * @param scheduleIds 排班ID；为空或空集合返回空列表（不查全表）
     */
    List<BizClinicSourceSlot> listByScheduleIds(Collection<Long> scheduleIds);

    /**
     * 单条排班的段列表出参（挂号选段用）。
     */
    List<ScheduleSlotVO> listVOByScheduleId(Long scheduleId);

    /**
     * 批量段列表出参（日视图看板一次拉全），口径同 {@link #listByScheduleIds}。
     */
    List<ScheduleSlotVO> listVOByScheduleIds(Collection<Long> scheduleIds);
}
