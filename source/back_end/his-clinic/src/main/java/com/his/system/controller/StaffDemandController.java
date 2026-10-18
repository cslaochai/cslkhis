package com.his.system.controller;

import com.his.common.base.Result;
import com.his.system.service.StaffDemandService;
import com.his.system.vo.StaffDemandGapVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 人力需求与缺口（排班的驱动源与分母）。
 */
@Tag(name = "人力需求与缺口")
@RestController
@RequestMapping("/system/staffDemand")
@RequiredArgsConstructor
public class StaffDemandController {

    private final StaffDemandService staffDemandService;

    /**
     * 人力缺口清单：按日期区间给出每天的需求人数、在岗人数与缺口（负数为富余）。
     * 用于病区护理排班页「周排班矩阵」上方的本周护理需求条，是排班的分母。
     *
     * @param orgType 单元类型（1-科室 2-病区）；与 orgId 成对传，只给 orgId 后端直接拒
     */
    @Operation(summary = "缺口清单（日期区间 + 单元 + 岗位类别；需求/在岗/缺口一并给出）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/gapList")
    public Result<List<StaffDemandGapVO>> gapList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer orgType,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer staffType) {
        return Result.success(staffDemandService.gapList(startDate, endDate, orgType, orgId, staffType));
    }

    /**
     * 重算一段日期的人力需求：由在院患者与出诊计划重新派生，手工调过的行原样保留。
     * 用于排班页需求条上的「重算需求」按钮，本周没有需求数据或床位患者变动后补算。
     */
    @Operation(summary = "重算某段日期的人力需求（只覆盖派生行，护士长手工调过的不动）")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/recalc")
    public Result<StaffDemandService.DemandRecalcResult> recalc(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        StaffDemandService.DemandRecalcResult result = staffDemandService.recalc(startDate, endDate);
        return Result.success("已重算：住院护理 " + result.inpatient + " 条、门诊护理 "
                + result.clinicNurse + " 条、门诊医生 " + result.clinicDoctor + " 条", result);
    }

    /**
     * 手工调整某单元某天某岗位的需求人数：写为手工来源，之后重算不再覆盖这一行。
     * 供护士长在系统派生的需求与实际不符时人工拍板改分母。
     */
    @Operation(summary = "护士长手工调整需求人数（写为手工来源，后续重算不再覆盖）")
    @PreAuthorize("hasAuthority('staff:schedule:edit')")
    @PostMapping("/adjust")
    public Result<Void> adjust(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate demandDate,
            @RequestParam Integer orgType,
            @RequestParam Long orgId,
            @RequestParam Integer staffType,
            @RequestParam Integer requiredCount,
            @RequestParam(required = false) String remark) {
        staffDemandService.adjust(demandDate, orgType, orgId, staffType, requiredCount, remark);
        return Result.success("需求已调整，后续重算不会再覆盖这一条", null);
    }
}
