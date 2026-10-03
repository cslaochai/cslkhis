package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.EnrollActionDTO;
import com.his.emr.dto.EnrollQueryPageDTO;
import com.his.emr.dto.EnrollUpsertDTO;
import com.his.emr.dto.OrderCheckDTO;
import com.his.emr.dto.PathwayActionDTO;
import com.his.emr.dto.PathwayQueryPageDTO;
import com.his.emr.dto.PathwayUpsertDTO;
import com.his.emr.dto.VarianceUpsertDTO;
import com.his.emr.service.PathwayService;
import com.his.emr.vo.OrderCheckVO;
import com.his.emr.vo.PathwayAdmissionVO;
import com.his.emr.vo.PathwayAnalysisVO;
import com.his.emr.vo.PathwayEnrollVO;
import com.his.emr.vo.PathwayVO;
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
 * 临床路径（模板→发布→入径→变异登记→完成/退径→变异分析）。
 *
 * <p>步骤是文书记录，不生成医嘱、不计费；操作人一律服务端取当前登录人。
 * 按项目规范 @PreAuthorize 全部标到方法（类级注解会罩住未标注方法）。
 */
@Tag(name = "临床路径")
@RestController
@RequestMapping("/emr/pathway")
@RequiredArgsConstructor
public class PathwayController {

    private final PathwayService pathwayService;

    // 模板

    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "模板分页")
    @PostMapping("/listPage")
    public Result<PageResult<PathwayVO>> listPage(@RequestBody PathwayQueryPageDTO dto) {
        return Result.success(pathwayService.listPage(dto));
    }

    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "模板详情（含步骤）")
    @GetMapping("/getDetailById")
    public Result<PathwayVO> getDetailById(@RequestParam Long id) {
        return Result.success(pathwayService.getDetailById(id));
    }

    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "使用中模板下拉（入径选择用）")
    @GetMapping("/activeSelectList")
    public Result<List<PathwayVO>> activeSelectList(@RequestParam(required = false) Long deptId) {
        return Result.success(pathwayService.activeSelectList(deptId));
    }

    @PreAuthorize("hasAuthority('qc:clinicalPath:add')")
    @Operation(summary = "模板新增/修改（仅草稿可编辑，步骤整组替换）")
    @PostMapping("/pathwayUpsert")
    public Result<PathwayVO> pathwayUpsert(@Valid @RequestBody PathwayUpsertDTO dto) {
        return Result.success("保存成功", pathwayService.pathwayUpsert(dto));
    }

    @PreAuthorize("hasAuthority('qc:clinicalPath:add')")
    @Operation(summary = "发布（草稿→使用中，同码仅一张）")
    @PostMapping("/publishPathway")
    public Result<PathwayVO> publishPathway(@Valid @RequestBody PathwayActionDTO dto) {
        return Result.success("已发布", pathwayService.publishPathway(dto));
    }

    @PreAuthorize("hasAuthority('qc:clinicalPath:add')")
    @Operation(summary = "停用（使用中→已停用，存量入径不受影响）")
    @PostMapping("/deprecatePathway")
    public Result<PathwayVO> deprecatePathway(@Valid @RequestBody PathwayActionDTO dto) {
        return Result.success("已停用", pathwayService.deprecatePathway(dto));
    }

    // 入径 / 变异 / 终态

    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "入径台账分页")
    @PostMapping("/enrollListPage")
    public Result<PageResult<PathwayEnrollVO>> enrollListPage(@RequestBody EnrollQueryPageDTO dto) {
        return Result.success(pathwayService.enrollListPage(dto));
    }

    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "入径详情（快照+当前路径日+模板步骤+变异台账）")
    @GetMapping("/enrollGetDetailById")
    public Result<PathwayEnrollVO> enrollGetDetailById(@RequestParam Long id) {
        return Result.success(pathwayService.enrollGetDetailById(id));
    }

    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "可入径候选（在院且无在径记录）")
    @GetMapping("/admissionsForEnroll")
    public Result<List<PathwayAdmissionVO>> admissionsForEnroll(@RequestParam(required = false) String keyword,
                                                                @RequestParam(required = false) Integer limit) {
        return Result.success(pathwayService.admissionsForEnroll(keyword, limit));
    }

    @PreAuthorize("hasAuthority('qc:clinicalPath:edit')")
    @Operation(summary = "入径登记（一次住院仅一条在径；快照服务端重查）")
    @PostMapping("/enrollUpsert")
    public Result<PathwayEnrollVO> enrollUpsert(@Valid @RequestBody EnrollUpsertDTO dto) {
        return Result.success("入径成功", pathwayService.enrollUpsert(dto));
    }

    // 医生站开单入口也走这里（软约束），故放开给住院医嘱新增权限；科室越权在 service 收口
    @PreAuthorize("hasAnyAuthority('qc:clinicalPath:edit', 'ipd:order:add')")
    @Operation(summary = "登记变异（追加台账，仅在径，原因必填）")
    @PostMapping("/varianceUpsert")
    public Result<PathwayEnrollVO> varianceUpsert(@Valid @RequestBody VarianceUpsertDTO dto) {
        return Result.success("变异已登记", pathwayService.varianceUpsert(dto));
    }

    @PreAuthorize("hasAuthority('qc:clinicalPath:edit')")
    @Operation(summary = "完成（在径→已完成，有变异不挡完成）")
    @PostMapping("/finishEnroll")
    public Result<PathwayEnrollVO> finishEnroll(@Valid @RequestBody EnrollActionDTO dto) {
        return Result.success("已完成", pathwayService.finishEnroll(dto));
    }

    @PreAuthorize("hasAuthority('qc:clinicalPath:edit')")
    @Operation(summary = "退径（在径→已退径，原因必填，终态）")
    @PostMapping("/abortEnroll")
    public Result<PathwayEnrollVO> abortEnroll(@Valid @RequestBody EnrollActionDTO dto) {
        return Result.success("已退径", pathwayService.abortEnroll(dto));
    }

    // 分析

    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "变异分析（模板聚合+类型分布+原因TOP；pathwayId 空则全院）")
    @GetMapping("/analysis")
    public Result<PathwayAnalysisVO> analysis(@RequestParam(required = false) Long pathwayId) {
        return Result.success(pathwayService.analysis(pathwayId));
    }

    // 医生站软约束（跨岗位复用：横幅+开单预检，只读不拦截）

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "医生站横幅：按住院号查在径记录（不在径返回 null；含当前路径日+模板步骤）")
    @GetMapping("/activeEnrollByAdmission")
    public Result<PathwayEnrollVO> activeEnrollByAdmission(@RequestParam Long admissionId) {
        return Result.success(pathwayService.activeEnrollByAdmission(admissionId));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "开单偏离预检（软约束：报偏差不拦截，前端据此提示登记变异）")
    @PostMapping("/orderCheck")
    public Result<OrderCheckVO> orderCheck(@Valid @RequestBody OrderCheckDTO dto) {
        return Result.success(pathwayService.orderCheck(dto));
    }
}
