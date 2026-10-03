package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.BedAssignUpsertDTO;
import com.his.patient.dto.BedMapQueryDTO;
import com.his.patient.dto.BedPoolQueryPageDTO;
import com.his.patient.dto.BedWaitAdmitDTO;
import com.his.patient.dto.BedWaitOperateDTO;
import com.his.patient.dto.BedWaitQueryPageDTO;
import com.his.patient.dto.BedWaitUpsertDTO;
import com.his.patient.service.BedCenterService;
import com.his.patient.vo.BedMapVO;
import com.his.patient.vo.BedMatchVO;
import com.his.patient.vo.BedOverviewVO;
import com.his.patient.vo.BedPoolVO;
import com.his.patient.vo.BedWaitStatsVO;
import com.his.patient.vo.BedWaitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 床位服务中心（等床队列 / 全院床位调配）
 * <p>约定：查询一律 GET，写操作一律 POST，路径驼峰。
 *
 * <p><b>权限码逐条挂在方法上</b>（不挂类）：类级注解会静默覆盖所有没写自己注解的方法，
 * 一旦某个只读接口继承了写权限码，护士今天就什么都看不到了。
 */
@Tag(name = "床位服务中心")
@RestController
@RequestMapping("/patient/bedCenter")
@RequiredArgsConstructor
public class BedCenterController {

    private final BedCenterService bedCenterService;

    @Operation(summary = "等床队列分页（危重优先，同级按登记先后）")
    @GetMapping("/queue/listPage")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<IPage<BedWaitVO>> queueListPage(BedWaitQueryPageDTO query) {
        return Result.success(bedCenterService.queuePage(query));
    }

    @Operation(summary = "等床队列详情")
    @GetMapping("/queue/getDetailById")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<BedWaitVO> queueDetail(@RequestParam Long waitId) {
        return Result.success(bedCenterService.queueDetail(waitId));
    }

    @Operation(summary = "登记/修改床位排队")
    @PostMapping("/queue/upsert")
    @PreAuthorize("hasAuthority('ipd:bedCenter:add')")
    public Result<String> upsertWait(@RequestBody @Valid BedWaitUpsertDTO dto) {
        return Result.success("排队登记成功", String.valueOf(bedCenterService.upsertWait(dto)));
    }

    @Operation(summary = "安排床位（含跨科调配，床位即刻锁定）")
    @PostMapping("/queue/assignBed")
    @PreAuthorize("hasAuthority('ipd:bedCenter:assign')")
    public Result<Void> assignBed(@RequestBody @Valid BedAssignUpsertDTO dto) {
        bedCenterService.assignBed(dto);
        return Result.success("床位已预留：该床已锁定并挂上患者，等待办理入院", null);
    }

    @Operation(summary = "退回队列（释放已锁定的床位）")
    @PostMapping("/queue/releaseBed")
    @PreAuthorize("hasAuthority('ipd:bedCenter:release')")
    public Result<Void> releaseBed(@RequestBody @Valid BedWaitOperateDTO dto) {
        bedCenterService.releaseBed(dto);
        return Result.success("已退回队列：床位已释放", null);
    }

    @Operation(summary = "取消排队（已安排床位的一并释放）")
    @PostMapping("/queue/cancel")
    @PreAuthorize("hasAuthority('ipd:bedCenter:cancel')")
    public Result<Void> cancelWait(@RequestBody @Valid BedWaitOperateDTO dto) {
        bedCenterService.cancelWait(dto);
        return Result.success("排队已取消", null);
    }

    @Operation(summary = "按已安排床位办理入院登记")
    @PostMapping("/queue/admit")
    @PreAuthorize("hasAuthority('ipd:bedCenter:admit')")
    public Result<String> admit(@RequestBody @Valid BedWaitAdmitDTO dto) {
        return Result.success("入院登记成功：床位已转为占用", bedCenterService.admit(dto));
    }

    @Operation(summary = "队列概览（等待分布/平均等待/床位现状）")
    @GetMapping("/queue/stats")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<BedWaitStatsVO> queueStats() {
        return Result.success(bedCenterService.queueStats());
    }

    @Operation(summary = "等待中人数（角标）")
    @GetMapping("/queue/countWaiting")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<String> countWaiting() {
        return Result.success(String.valueOf(bedCenterService.countWaiting()));
    }

    @Operation(summary = "床位智能匹配（给候选不给最优解，决定由现场做）")
    @GetMapping("/pool/match")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<List<BedMatchVO>> matchBeds(@RequestParam Long waitId) {
        return Result.success(bedCenterService.matchBeds(waitId));
    }

    @Operation(summary = "全院床位池（含占用者与预留去向）")
    @GetMapping("/pool/listPage")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<BedPoolVO> bedPool(BedPoolQueryPageDTO query) {
        return Result.success(bedCenterService.bedPool(query));
    }

    /**
     * 床位调配图：与护士站 {@code /patient/inpatient/bedMap} 同一张图、两种视角 ——
     * 护士看"床上躺着谁、几级护理"，这里看"这张床能不能用、被谁预定了、能不能直接放人"。
     * 所以走的是床位中心的读权限，不复用护士站的 {@code ipd:nurse:list}。
     */
    @Operation(summary = "床位调配图（一床一卡，含预留去向与可用动作）")
    @GetMapping("/pool/bedMap")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<BedMapVO> bedMap(BedMapQueryDTO query) {
        return Result.success(bedCenterService.bedMap(query));
    }

    @Operation(summary = "全院床位总览与科室排行")
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('ipd:bedCenter:list')")
    public Result<BedOverviewVO> overview() {
        return Result.success(bedCenterService.overview());
    }

    /**
     * 等床超时催总值班的手工补跑（定时任务 {@code BedWaitDutyEscalateTrigger} 每小时一轮）。
     * 挂 {@code ipd:bedCenter:assign}：只有管床位调配的人该主动去"喊总值班"，
     * 只读的护士站不该有这个按钮。
     */
    @Operation(summary = "等床超时催总值班（手工补跑，返回本轮发出的待办条数）")
    @PostMapping("/escalateWaitToDuty")
    @PreAuthorize("hasAuthority('ipd:bedCenter:assign')")
    public Result<String> escalateWaitToDuty() {
        return Result.success("已向当日总值班催办", String.valueOf(bedCenterService.escalateWaitToDuty()));
    }
}
