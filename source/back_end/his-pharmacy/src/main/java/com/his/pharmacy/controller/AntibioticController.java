package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.*;
import com.his.pharmacy.service.AntibioticService;
import com.his.pharmacy.vo.*;
import com.his.system.utils.UserUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 抗菌药物分级目录 + 医师处方权授权。
 *
 * <p>权限：页面读 {@code pharmacy:antibiotic:catalog}；目录/别名维护 {@code :catalogEdit}；
 * 授权 {@code :authEdit}。开方前的越权自检 {@code /checkAuthority} 对医生站开放
 * （医生站要能在开方前就知道"这药我能不能开"，而不是等提交才报错）。
 */
@Tag(name = "抗菌药物分级目录与处方权")
@RestController
@RequestMapping("/antibiotic")
@RequiredArgsConstructor
public class AntibioticController {

    private final AntibioticService antibioticService;

    @Operation(summary = "分级目录分页")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalog')")
    @PostMapping("/catalogListPage")
    public Result<PageResult<AntibioticCatalogVO>> catalogListPage(@Valid @RequestBody AntibioticCatalogQueryPageDTO query) {
        return Result.success(antibioticService.catalogListPage(query));
    }

    @Operation(summary = "维护药品抗菌药物分级与 DDD 值")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalogEdit')")
    @PostMapping("/catalogLevelUpsert")
    public Result<AntibioticCatalogVO> catalogLevelUpsert(@Valid @RequestBody AntibioticCatalogLevelUpsertDTO dto) {
        return Result.success(antibioticService.catalogLevelUpsert(dto));
    }

    @Operation(summary = "抗菌药物下拉（selectList）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalog')")
    @GetMapping("/antibioticDrugSelectList")
    public Result<List<AntibioticDrugSelectListVO>> antibioticDrugSelectList() {
        return Result.success(antibioticService.antibioticDrugSelectList());
    }

    @Operation(summary = "医师下拉（selectList）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalog')")
    @GetMapping("/doctorSelectList")
    public Result<List<AntibioticDoctorSelectListVO>> doctorSelectList(
            @RequestParam(required = false) String keyword) {
        return Result.success(antibioticService.doctorSelectList(keyword));
    }

    @Operation(summary = "别名列表（drugId 为空=全部）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalog')")
    @GetMapping("/aliasList")
    public Result<List<AntibioticAliasVO>> aliasList(@RequestParam(required = false) Long drugId) {
        return Result.success(antibioticService.aliasList(drugId));
    }

    @Operation(summary = "别名新增/修改")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalogEdit')")
    @PostMapping("/aliasUpsert")
    public Result<AntibioticAliasVO> aliasUpsert(@Valid @RequestBody AntibioticAliasUpsertDTO dto) {
        return Result.success(antibioticService.aliasUpsert(dto));
    }

    @Operation(summary = "别名删除（物理删）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalogEdit')")
    @DeleteMapping("/aliasDeleteById")
    public Result<Void> aliasDeleteById(@RequestParam Long id) {
        antibioticService.aliasDeleteById(id);
        return Result.success(null);
    }

    @Operation(summary = "处方权授权分页")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:catalog')")
    @PostMapping("/authListPage")
    public Result<PageResult<AntibioticAuthVO>> authListPage(@Valid @RequestBody AntibioticAuthQueryPageDTO query) {
        return Result.success(antibioticService.authListPage(query));
    }

    @Operation(summary = "处方权授权新增/修改（改有效期/状态；不换医师不换级别）")
    @PreAuthorize("hasAuthority('pharmacy:antibiotic:authEdit')")
    @PostMapping("/authUpsert")
    public Result<AntibioticAuthVO> authUpsert(@Valid @RequestBody AntibioticAuthUpsertDTO dto) {
        return Result.success(antibioticService.authUpsert(dto));
    }

    @Operation(summary = "开方前越权自检（医生站用：这药我能不能开）")
    @PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'pharmacy:antibiotic:catalog')")
    @PostMapping("/checkAuthority")
    public Result<AntibioticAuthCheckVO> checkAuthority(@Valid @RequestBody AntibioticAuthCheckQueryDTO dto) {
        return Result.success(antibioticService.checkAuthority(
                UserUtils.getCurrentEmployeeId(), dto.getDrugIds()));
    }
}
