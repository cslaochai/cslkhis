package com.his.spi.service;

import com.his.spi.dto.AppointQueryDTO;
import com.his.spi.vo.ClinicSourceBriefVO;
import com.his.spi.vo.ClinicSourceSlotBriefVO;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 门诊号源端口：把「这一班次还剩多少号、归哪个科室哪位医生、什么时段、收多少钱」
 * 以及号源的原子扣减/释放交给挂号、急诊、分诊三个域。
 *
 * <p>号源的生成、放号、停诊编排仍归排班域自己；本端口只承诺快照读、按渠道原子扣还与两类列表取数。
 */
public interface ClinicSourceFacadeService {

    /**
     * 按号源ID读取门诊号源快照（科室/医生/就诊日/起止时间/诊查费/职级档/是否专家/剩余号/诊室）。
     *
     * <p>用于挂号新增/改约填充挂号快照、改约同医生同费用核对、排队诊室快照；
     * 号源不存在返回 {@code null}，不抛错。
     */
    ClinicSourceBriefVO findClinicSource(Long sourceId);

    /**
     * 按号源段ID读取时段段快照（段ID/所属号源/起止/状态）。
     *
     * <p>用于段归属与停用校验、段快照填充；段不存在返回 {@code null}。
     */
    ClinicSourceSlotBriefVO findSlot(Long slotId);

    /**
     * 校验岗位排班是否对外放出号源（在岗状态/出勤模式过滤，封装排班域内部口径）。
     *
     * @param staffScheduleId 岗位排班（出勤事实）ID
     * @return true=该排班该放出号源；排班不存在或不放号返回 false
     */
    boolean releasesClinicSource(Long staffScheduleId);

    /**
     * 按班次ID取班次类型（早/中/晚/夜，排班域班次字典的 schedule_type）。
     * 班次没选或已被删返回 {@code null}，不猜。
     */
    Integer scheduleTypeOf(Long shiftId);

    /**
     * 按班次ID取班次名（急诊交班台账展示用）；班次不存在返回 {@code null}。
     */
    String findShiftName(Long shiftId);

    /**
     * 号源主表原子扣减（新增挂号扣、改约扣新）。
     *
     * @param sourceId      号源ID
     * @param apptChannel  true=预约渠道，从预约池扣（同时扣总池与预约池已用）；false=现场渠道
     * @param sameDayWalkin true=就诊当日现场放宽择池（只认总余号，不吃预约池限制）；false=提前日现场，
     *                      已划预约池的班次现场只能占「总余号 - 预约池剩余」
     * @return 实际扣减行数（1=成功，0=号满/预约池空，调用方按 0 判失败）
     */
    int deductScheduleSource(Long sourceId, boolean apptChannel, boolean sameDayWalkin);

    /**
     * 号源段原子扣减（口径同 {@link #deductScheduleSource}，落到半小时段）。
     *
     * @return 实际扣减行数（1=成功，0=段满/预约池空/段已停用）
     */
    int deductSlotSource(Long slotId, boolean apptChannel, boolean sameDayWalkin);

    /**
     * 号源主表原子释放（退号还、改约还旧）。
     *
     * @param sourceId    号源ID
     * @param apptChannel true=预约渠道还（总池还号 + 预约池已用回退）；false=现场渠道还
     * @return 实际释放行数（1=成功，0=无在挂可还，调用方按 0 判重复释放）
     */
    int releaseScheduleSource(Long sourceId, boolean apptChannel);

    /**
     * 号源段原子释放（口径同 {@link #releaseScheduleSource}，落到半小时段）。
     *
     * @return 实际释放行数（1=成功，0=无在挂可还）
     */
    int releaseSlotSource(Long slotId, boolean apptChannel);

    /**
     * 分诊台/看板按科室集合取指定日期的今日出诊号源医生（接诊状态仍由调用方本地缓存叠加）。
     *
     * @param deptIds 科室ID集合；为空返回空列表（不查全表）
     * @param date    就诊日；为空由提供方取当天
     */
    List<ClinicSourceBriefVO> listTodayClinicSources(Collection<Long> deptIds, LocalDate date);

    /**
     * 患者端可挂号源下拉：不走院内科室数据权限，按 deptId/doctorId/visitDate 取开放预约号源
     * （时段/挂号费/诊查费/职级档/是否专家/剩余号）。
     */
    List<ClinicSourceBriefVO> listPatientScheduleSources(AppointQueryDTO query);
}
