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

    PageResult<IcuVO.StayVO> stayListPage(IcuStayQueryPageDTO query);

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

    PageResult<IcuVO.MonitorVO> monitorListPage(IcuMonitorQueryPageDTO query);

    /**
     * 单患者监护趋势（hours 为空=全部，按时间正序）
     */
    List<IcuVO.MonitorVO> monitorTrend(Long stayId, Integer hours);

    /**
     * 新增/修改一条监护记录（出科后禁写）
     */
    IcuVO.MonitorVO monitorUpsert(IcuMonitorUpsertDTO dto);

    IcuVO.StatsVO stats(LocalDate startDate, LocalDate endDate, Integer lagHours);
}
