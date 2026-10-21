package com.his.spi.service;

import com.his.spi.vo.DutyOfficerBriefVO;
import com.his.spi.vo.DutyStaffBriefVO;
import com.his.spi.vo.StaffScheduleBriefVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 值班表端口：把「现在该谁值班」这个事实交给需要落执行人的业务域。
 *
 * <p>值班排班、代班、审核的编排仍归排班域自己；本端口只承诺按当前登录岗位取当班责任人。
 */
public interface DutyRosterFacadeService {

    /**
     * 取当前时刻的当班责任人。
     *
     * <p>用于急诊分诊记录落当班护士、床位中心与转科单落当班负责人——
     * 单据上没有「谁负责」就取不到归属人，交接责任断链。
     *
     * @return 当班责任人；当日该岗位没排人时返回的 {@code employeeId} 为 null
     */
    DutyOfficerBriefVO current();

    /**
     * 按单元 + 岗位取指定时刻的在岗人员（计划命中 + 已签到，过滤听班 ON_CALL）。
     *
     * <p>用于急诊派单、在岗下拉、交班候选、超时催办；跨零点班归开始日，
     * 凌晨判定会同时命中「昨天夜班」与「今天白班」。
     *
     * @param at        判定时刻；null 由提供方取当前时刻
     * @param orgType   单元类型（1-科室 2-病区 3-全院；空=全部）
     * @param orgId     单元ID（空=该类型全部单元）
     * @param staffType 岗位类别（StaffTypeEnum 码值；空=全部岗位）
     */
    List<StaffScheduleBriefVO> listOnDutyStaff(LocalDateTime at, Integer orgType, Long orgId, Integer staffType);

    /**
     * 按科室 + 岗位选当班责任人：优先本人，否则此刻在岗，再退当日最近一班。
     *
     * <p>用于分诊记录落当班护士；该科室此刻没人排班时返回的 {@code staffId} 为 null。
     *
     * @param deptId      科室ID
     * @param staffType   岗位类别（StaffTypeEnum 码值）
     * @param preferEmpId 优先取此人（当前操作人），可为 null
     */
    DutyStaffBriefVO pickDutyStaff(Long deptId, Integer staffType, Long preferEmpId);
}
