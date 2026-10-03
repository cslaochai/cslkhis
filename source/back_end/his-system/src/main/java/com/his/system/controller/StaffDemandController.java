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
 *
 * <p>{@code /gapList} 只要求登录：缺口是给全院看的公共信息 —— 护理部、门诊办、
 * 科室主任都要看「今天差几个人」，按菜单权限收口等于让人排班时看不到自己差多少人。
 * 真正会改数据的是 {@code /recalc} 与 {@code /adjust}，这两个按排班权限收口。
 */
@Tag(name = "人力需求与缺口")
@RestController
@RequestMapping("/system/staffDemand")
@RequiredArgsConstructor
public class StaffDemandController {

    private final StaffDemandService staffDemandService;

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

    @Operation(summary = "重算某段日期的人力需求（只覆盖派生行，护士长手工调过的不动）")
    @PreAuthorize("hasAuthority('org:schedule:edit')")
    @PostMapping("/recalc")
    public Result<StaffDemandService.DemandRecalcResult> recalc(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        StaffDemandService.DemandRecalcResult result = staffDemandService.recalc(startDate, endDate);
        return Result.success("已重算：住院护理 " + result.inpatient + " 条、门诊护理 "
                + result.clinicNurse + " 条、门诊医生 " + result.clinicDoctor + " 条", result);
    }

    @Operation(summary = "护士长手工调整需求人数（写为手工来源，后续重算不再覆盖）")
    @PreAuthorize("hasAuthority('org:schedule:edit')")
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
