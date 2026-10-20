package com.his.patient.service;

import com.his.common.base.PageResult;
import com.his.patient.dto.*;
import com.his.patient.vo.IcuVO;

import java.time.LocalDate;
import java.util.List;

/**
 * ICU 专科监护服务：入出科登记 + 床边监护记录单 + 床位看板与科室指标。
 */
public interface IcuService {

    /**
     * 入出科台账分页：按记录单号、患者姓名、病区、监护等级、在科状态与入科日期区间筛。
     * 用于「ICU 专科监护」页入出科台账查询卡。
     */
    PageResult<IcuVO.StayVO> stayListPage(IcuStayPageQueryDTO query);

    /**
     * 单条入科记录详情：患者与床位快照、监护等级与入出科时间。
     * 用于「ICU 专科监护」页入科详情弹框回显。
     */
    IcuVO.StayVO stayGetById(Long id);

    /**
     * 可入科候选（在院且无在科记录）
     */
    List<IcuVO.AdmissionVO> admissionsForIcu(String keyword, Integer limit);

    /**
     * 入科登记 / 在科期间修改（床位、监护等级、入科时间）
     */
    IcuVO.StayVO stayUpsert(IcuStayUpsertDTO dto);

    /**
     * 出科（终态，同时封住监护记录）
     */
    IcuVO.StayVO stayOut(IcuStayOutDTO dto);

    /**
     * ICU 床位看板（含在科患者与最近一次监护读数）
     */
    List<IcuVO.BedVO> bedBoard(Long wardId);

    /**
     * 床边监护记录分页：按入科记录与监护日期区间筛。
     * 用于「ICU 专科监护」页监护记录页签的查询卡。
     */
    PageResult<IcuVO.MonitorVO> monitorListPage(IcuMonitorPageQueryDTO query);

    /**
     * 单患者监护趋势（hours 为空=全部，按时间正序）
     */
    List<IcuVO.MonitorVO> monitorTrend(Long stayId, Integer hours);

    /**
     * 新增/修改一条监护记录（出科后禁写）
     */
    IcuVO.MonitorVO monitorUpsert(IcuMonitorUpsertDTO dto);

    /**
     * ICU 科室指标：统计窗口内的床位使用率、人均监护记录、带管与呼吸机分布及漏记预警。
     * 用于「ICU 专科监护」页科室指标页签。
     *
     * @param lagHours 漏记判定的超时阈值（小时）
     */
    IcuVO.StatsVO stats(LocalDate startDate, LocalDate endDate, Integer lagHours);
}
