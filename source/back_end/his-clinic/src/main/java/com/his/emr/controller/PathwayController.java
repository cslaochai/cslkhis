package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.*;
import com.his.emr.service.PathwayService;
import com.his.emr.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 临床路径（模板→发布→入径→变异登记→完成/退径→变异分析）。
 */
@Tag(name = "临床路径")
@RestController
@RequestMapping("/emr/pathway")
@RequiredArgsConstructor
public class PathwayController {

    private final PathwayService pathwayService;

    /**
     * 路径模板分页：草稿、使用中、已停用三态一张表。
     * 用于「临床路径」页「路径模板」页签的查询卡与表格。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "模板分页")
    @PostMapping("/listPage")
    public Result<PageResult<PathwayVO>> listPage(@Valid @RequestBody PathwayPageQueryDTO dto) {
        return Result.success(pathwayService.listPage(dto));
    }

    /**
     * 单张路径模板详情：连阶段步骤一起返回，步骤顺序即入径快照的顺序。
     * 用于路径模板详情弹框展示，以及编辑弹框打开时的表单回显。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "模板详情（含步骤）")
    @GetMapping("/getDetailById")
    public Result<PathwayVO> getDetailById(@RequestParam Long id) {
        return Result.success(pathwayService.getDetailById(id));
    }

    /**
     * 使用中模板下拉：可按科室收窄，只给已发布生效的模板。
     * 用于「在径管理」页签筛选用模板，以及入径登记表单选模板。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "使用中模板下拉（入径选择用）")
    @GetMapping("/activeSelectList")
    public Result<List<PathwayVO>> activeSelectList(@RequestParam(required = false) Long deptId) {
        return Result.success(pathwayService.activeSelectList(deptId));
    }

    /**
     * 路径模板新增/修改：步骤整组替换，已发布模板不许再动内容。
     * 用于路径模板新增/编辑弹框的保存动作。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:add')")
    @Operation(summary = "模板新增/修改（仅草稿可编辑，步骤整组替换）")
    @PostMapping("/pathwayUpsert")
    public Result<PathwayVO> pathwayUpsert(@Valid @RequestBody PathwayUpsertDTO dto) {
        return Result.success("保存成功", pathwayService.pathwayUpsert(dto));
    }

    /**
     * 模板发布：草稿转使用中，同一路径编码同时只保留一张生效模板。
     * 用于路径模板列表行的发布操作，发布后该模板才能被入径。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:add')")
    @Operation(summary = "发布（草稿→使用中，同码仅一张）")
    @PostMapping("/publishPathway")
    public Result<PathwayVO> publishPathway(@Valid @RequestBody PathwayActionDTO dto) {
        return Result.success("已发布", pathwayService.publishPathway(dto));
    }

    /**
     * 模板停用：使用中转已停用，不再出现在入径下拉。
     * 用于路径模板列表行的停用操作；已在径的患者照原快照走完，不受影响。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:add')")
    @Operation(summary = "停用（使用中→已停用，存量入径不受影响）")
    @PostMapping("/deprecatePathway")
    public Result<PathwayVO> deprecatePathway(@Valid @RequestBody PathwayActionDTO dto) {
        return Result.success("已停用", pathwayService.deprecatePathway(dto));
    }

    // 入径 / 变异 / 终态

    /**
     * 入径台账分页：按模板、患者、状态与入径日期收口。
     * 用于「临床路径」页「在径管理」页签的表格取数。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "入径台账分页")
    @PostMapping("/enrollListPage")
    public Result<PageResult<PathwayEnrollVO>> enrollListPage(@Valid @RequestBody EnrollPageQueryDTO dto) {
        return Result.success(pathwayService.enrollListPage(dto));
    }

    /**
     * 入径详情：模板快照、当前路径日、步骤清单与名下变异台账一次给全。
     * 用于「在径管理」行点开的入径详情，完成与退径都在这一屏发起。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "入径详情（快照+当前路径日+模板步骤+变异台账）")
    @GetMapping("/enrollGetDetailById")
    public Result<PathwayEnrollVO> enrollGetDetailById(@RequestParam Long id) {
        return Result.success(pathwayService.enrollGetDetailById(id));
    }

    /**
     * 可入径候选：在院且名下没有进行中入径记录的住院就诊，按关键词限量返回。
     * 用于入径登记弹框挑选患者。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "可入径候选（在院且无在径记录）")
    @GetMapping("/admissionsForEnroll")
    public Result<List<PathwayAdmissionVO>> admissionsForEnroll(@RequestParam(required = false) String keyword,
                                                                @RequestParam(required = false) Integer limit) {
        return Result.success(pathwayService.admissionsForEnroll(keyword, limit));
    }

    /**
     * 入径登记：一次住院只允许一条在径记录，快照字段由服务端按模板重查生成。
     * 用于「在径管理」页签的入径登记弹框保存。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:edit')")
    @Operation(summary = "入径登记（一次住院仅一条在径；快照服务端重查）")
    @PostMapping("/enrollUpsert")
    public Result<PathwayEnrollVO> enrollUpsert(@Valid @RequestBody EnrollUpsertDTO dto) {
        return Result.success("入径成功", pathwayService.enrollUpsert(dto));
    }

    // 医生站开单入口也走这里（软约束），故放开给住院医嘱新增权限；科室越权在 service 收口
    /**
     * 登记变异：往名下变异台账追加一条，仅限在径记录且原因必填。
     * 用于入径详情的变异登记，也用于住院医嘱工作台开单偏离后的补登记。
     */
    @PreAuthorize("hasAnyAuthority('qc:clinicalPath:edit', 'ipd:order:add')")
    @Operation(summary = "登记变异（追加台账，仅在径，原因必填）")
    @PostMapping("/varianceUpsert")
    public Result<PathwayEnrollVO> varianceUpsert(@Valid @RequestBody VarianceUpsertDTO dto) {
        return Result.success("变异已登记", pathwayService.varianceUpsert(dto));
    }

    /**
     * 完成入径：在径转已完成，名下有变异记录不挡完成。
     * 用于入径详情里路径走完时的完成操作。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:edit')")
    @Operation(summary = "完成（在径→已完成，有变异不挡完成）")
    @PostMapping("/finishEnroll")
    public Result<PathwayEnrollVO> finishEnroll(@Valid @RequestBody EnrollActionDTO dto) {
        return Result.success("已完成", pathwayService.finishEnroll(dto));
    }

    /**
     * 退径：在径转已退径，原因必填，属终态不再回到在径。
     * 用于入径详情里患者病情变化不再适用路径时的退出操作。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:edit')")
    @Operation(summary = "退径（在径→已退径，原因必填，终态）")
    @PostMapping("/abortEnroll")
    public Result<PathwayEnrollVO> abortEnroll(@Valid @RequestBody EnrollActionDTO dto) {
        return Result.success("已退径", pathwayService.abortEnroll(dto));
    }

    // 分析

    /**
     * 变异分析：按模板聚合入径与完成情况、变异类型分布与原因 TOP。
     * 用于「临床路径」页「变异分析」页签，供路径管理员评估模板是否合理。
     */
    @PreAuthorize("hasAuthority('qc:clinicalPath:list')")
    @Operation(summary = "变异分析（模板聚合+类型分布+原因TOP；pathwayId 空则全院）")
    @GetMapping("/analysis")
    public Result<PathwayAnalysisVO> analysis(@RequestParam(required = false) Long pathwayId) {
        return Result.success(pathwayService.analysis(pathwayId));
    }

    // 医生站软约束（跨岗位复用：横幅+开单预检，只读不拦截）

    /**
     * 按住院就诊取在径记录：不在径时数据为空，命中时带当前路径日与模板步骤。
     * 用于住院医嘱工作台顶部的临床路径横幅展示。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "医生站横幅：按住院号查在径记录（不在径返回 null；含当前路径日+模板步骤）")
    @GetMapping("/activeEnrollByAdmission")
    public Result<PathwayEnrollVO> activeEnrollByAdmission(@RequestParam Long admissionId) {
        return Result.success(pathwayService.activeEnrollByAdmission(admissionId));
    }

    /**
     * 开单偏离预检：判断医嘱是否落在当前路径日的步骤范围内，只报偏差不拦截。
     * 用于住院医嘱工作台开单前的软约束提示，据此引导医生登记变异。
     */
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "开单偏离预检（软约束：报偏差不拦截，前端据此提示登记变异）")
    @PostMapping("/orderCheck")
    public Result<OrderCheckVO> orderCheck(@Valid @RequestBody OrderCheckDTO dto) {
        return Result.success(pathwayService.orderCheck(dto));
    }
}
