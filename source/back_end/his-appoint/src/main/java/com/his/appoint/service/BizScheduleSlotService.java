package com.his.appoint.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.appoint.dto.ScheduleSlotUpsertDTO;
import com.his.appoint.entity.BizSchedule;
import com.his.appoint.entity.BizScheduleSlot;
import com.his.appoint.entity.BizScheduleSlotTemplate;
import com.his.appoint.vo.ScheduleSlotVO;

import java.util.Collection;
import java.util.List;

/**
 * 排班时间片段服务。
 *
 * <p>号源与占用的事实都在段上（半小时一档），本服务负责段的生命周期：
 * 生成（自动切分/按模板）、重算（改号源/改时间窗）、摊加号、Σ段写回主表。
 * 所有方法都假定调用方已有事务（挂号/排班写路径均在 @Transactional 内），
 * 段与主表的双写靠同一事务保证一致。
 */
public interface BizScheduleSlotService extends IService<BizScheduleSlot> {

    /**
     * 把班次时间窗切成半小时段并均分号源落库（余数给前面的段），Σ段写回主表。
     *
     * <p>预约池同样均分到段；段内预约预留超过段号源时截断顺延（兜底，数学上均分+余数给前面通常不会超）。
     *
     * @return 生成的段列表（seq 升序）
     */
    List<BizScheduleSlot> generateSlots(Long scheduleId, String startTime, String endTime,
                                        Integer totalSource, Integer appointmentSource);

    /**
     * 按模板片段配置生成段（Σ必须与主表号源一致，由模板保存侧校验保证），Σ段写回主表。
     */
    List<BizScheduleSlot> generateFromTemplate(Long scheduleId, List<BizScheduleSlotTemplate> tplSlots);

    /**
     * 时间窗或号源变化后的段重算：
     * <ul>
     *   <li>时间窗变化且该排班已有挂号（Σused>0）→ 拒绝（段边界是挂号快照的语义，改窗等于篡改事实）；</li>
     *   <li>时间窗变化且无挂号 → 物理删旧段按新窗重建；</li>
     *   <li>仅号源变化 → 重摊号源（已用的段保底压到 used，差额向有空余的段顺延）。</li>
     * </ul>
     * 重算后 Σ段写回主表（主表 total = Σ段，可能与入参略有出入——used 保底挤压所致，注释见实现）。
     */
    void regenerateForSchedule(BizSchedule schedule, Integer newTotal, Integer newAppointment);

    /**
     * 加号均摊到段（余数给前面的段）：段 total/available/added 同加，不动预约池；Σ段写回主表。
     */
    void spreadAddSource(Long scheduleId, int addNum);

    /**
     * 段级号源编辑（号源密度按需调整的唯一写入口）：
     * 逐段改号源总数/预约预留/停用状态，段的时间窗不接受编辑。
     * 整批校验整批生效（任何一段不合法都不动库），成功后 Σ段写回主表并留痕备注。
     */
    void updateSlotSources(ScheduleSlotUpsertDTO dto);

    /**
     * Σ段写回主表 6 个号源字段（available = Σtotal - Σused）。
     * 双写收口点：生成/重算/摊加号后调用，主表读路径（周面板/列表/号源选择）不变。
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
    List<BizScheduleSlot> listByScheduleId(Long scheduleId);

    /**
     * 批量查多条排班的段列表（按 scheduleId 升序、段 seq 升序）。
     *
     * <p>日视图看板要把「医生 × 半小时段」每个格子的余号画出来，一天几十条排班
     * 逐条调 {@code /schedule/slotList} 会打出一串请求（且慢的那一格才决定整屏什么时候出）。
     * 这里一次查全，调用方按 scheduleId 归位即可。
     *
     * @param scheduleIds 排班ID；为空或空集合返回空列表（不查全表）
     */
    List<BizScheduleSlot> listByScheduleIds(Collection<Long> scheduleIds);

    /**
     * 单条排班的段列表出参（挂号选段用）。
     */
    List<ScheduleSlotVO> listVOByScheduleId(Long scheduleId);

    /**
     * 批量段列表出参（日视图看板一次拉全），口径同 {@link #listByScheduleIds}。
     */
    List<ScheduleSlotVO> listVOByScheduleIds(Collection<Long> scheduleIds);
}
