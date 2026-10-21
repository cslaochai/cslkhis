package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.*;
import com.his.pharmacy.service.AntibioticService;
import com.his.pharmacy.vo.*;
import com.his.common.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 抗菌药物分级目录 + 医师处方权授权。
 */
@Tag(name = "抗菌药物分级目录与处方权")
@RestController
@RequestMapping("/antibiotic")
@RequiredArgsConstructor
public class AntibioticController {

    private final AntibioticService antibioticService;

    /**
     * 抗菌药物分级目录分页：按关键字与级别筛出已纳入分级的药品及其 DDD 值。
     * 用于药事管理「抗菌药物分级目录」页的分级目录页签查询卡。
     */
    @Operation(summary = "分级目录分页")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalog')")
    @PostMapping("/catalogListPage")
    public Result<PageResult<AntibioticCatalogVO>> catalogListPage(@Valid @RequestBody AntibioticCatalogPageQueryDTO query) {
        return Result.success(antibioticService.catalogListPage(query));
    }

    /**
     * 维护单个药品的抗菌药物分级与 DDD 值（新增与修改同一入口，按药品ID分支）。
     * 用于分级目录页签行内「分级维护」弹框保存。
     */
    @Operation(summary = "维护药品抗菌药物分级与 DDD 值")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalogEdit')")
    @PostMapping("/catalogLevelUpsert")
    public Result<AntibioticCatalogVO> catalogLevelUpsert(@Valid @RequestBody AntibioticCatalogLevelUpsertDTO dto) {
        return Result.success(antibioticService.catalogLevelUpsert(dto));
    }

    /**
     * 已纳入分级的抗菌药物下拉候选（不带分页，一次取全）。
     * 用于分级目录页的查询条件与 I 类切口点评弹框选药。
     */
    @Operation(summary = "抗菌药物下拉（selectList）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalog')")
    @GetMapping("/antibioticDrugSelectList")
    public Result<List<AntibioticDrugSelectListVO>> antibioticDrugSelectList() {
        return Result.success(antibioticService.antibioticDrugSelectList());
    }

    /**
     * 医师下拉候选，可按姓名或工号关键字过滤。
     * 用于处方权授权弹框选择被授权医师。
     */
    @Operation(summary = "医师下拉（selectList）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalog')")
    @GetMapping("/doctorSelectList")
    public Result<List<AntibioticDoctorSelectListVO>> doctorSelectList(
            @RequestParam(required = false) String keyword) {
        return Result.success(antibioticService.doctorSelectList(keyword));
    }

    /**
     * 查抗菌药物的医嘱别名清单；药品ID为空时返回全部别名。
     * 用于分级目录页「医嘱别名」页签的维护列表。
     */
    @Operation(summary = "别名列表（drugId 为空=全部）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalog')")
    @GetMapping("/aliasList")
    public Result<List<AntibioticAliasVO>> aliasList(@RequestParam(required = false) Long drugId) {
        return Result.success(antibioticService.aliasList(drugId));
    }

    /**
     * 新增或修改一条抗菌药物医嘱别名（新增与修改同一入口，按ID分支）。
     * 用于「医嘱别名」页签的别名维护弹框保存。
     */
    @Operation(summary = "别名新增/修改")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalogEdit')")
    @PostMapping("/aliasUpsert")
    public Result<AntibioticAliasVO> aliasUpsert(@Valid @RequestBody AntibioticAliasUpsertDTO dto) {
        return Result.success(antibioticService.aliasUpsert(dto));
    }

    /**
     * 删除一条抗菌药物医嘱别名。
     * 用于「医嘱别名」页签行内删除，删后该别名不再参与医嘱文本匹配。
     */
    @Operation(summary = "别名删除（物理删）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalogEdit')")
    @DeleteMapping("/aliasDeleteById")
    public Result<Void> aliasDeleteById(@RequestParam Long id) {
        antibioticService.aliasDeleteById(id);
        return Result.success(null);
    }

    /**
     * 医师抗菌药物处方权授权记录分页。
     * 用于药事管理「抗菌药物处方权授权」页的查询卡与台账表格。
     */
    @Operation(summary = "处方权授权分页")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalog')")
    @PostMapping("/authListPage")
    public Result<PageResult<AntibioticAuthVO>> authListPage(@Valid @RequestBody AntibioticAuthPageQueryDTO query) {
        return Result.success(antibioticService.authListPage(query));
    }

    /**
     * 修改一条处方权授权的有效期与状态（不支持换医师、换授权级别）。
     * 用于处方权授权页编辑弹框保存，换人换级别需另建授权行。
     */
    @Operation(summary = "处方权授权新增/修改（改有效期/状态；不换医师不换级别）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:authEdit')")
    @PostMapping("/authUpsert")
    public Result<AntibioticAuthVO> authUpsert(@Valid @RequestBody AntibioticAuthUpsertDTO dto) {
        return Result.success(antibioticService.authUpsert(dto));
    }

    /**
     * 以当前登录医师的有效授权级别校验这批抗菌药物能否开具，返回是否放行与越权药品清单。
     * 供医生站提交抗菌药物医嘱前的开方闸环节回写提示，判定口径与后端保存闸共用同一份。
     */
    @Operation(summary = "开方前越权自检（医生站用：这药我能不能开）")
    @PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'pharmacy:antibiotic:catalog')")
    @PostMapping("/checkAuthority")
    public Result<AntibioticAuthCheckVO> checkAuthority(@Valid @RequestBody AntibioticAuthCheckQueryDTO dto) {
        return Result.success(antibioticService.checkAuthority(
                UserUtils.getCurrentUser().getEmployeeId(), dto.getDrugIds()));
    }
}
