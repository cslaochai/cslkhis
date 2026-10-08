package com.his.emr.controller;

import com.his.common.base.Result;
import com.his.emr.dto.LaboratoryApplyQueryDTO;
import com.his.emr.dto.LaboratoryApplyUpsertDTO;
import com.his.emr.service.EmrService;
import com.his.emr.vo.BizLaboratoryApplyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 医生工作站 - 检验申请控制器
 */
@Tag(name = "检验申请")
@RestController
@RequestMapping("/laboratory")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class DoctorLabController {

    private final EmrService emrService;

    /**
     * 查询本次就诊的检验申请列表（不分页）。
     *
     * <p>路径沿用 {@code /getByPatientId}，过滤口径为「挂号」（原因见检查申请控制器）。
     */
    @Operation(summary = "查询检验申请列表（按挂号，含执行进度）")
    @GetMapping("/getByPatientId")
    public Result<List<BizLaboratoryApplyVO>> getByPatientId(@Valid LaboratoryApplyQueryDTO queryDTO) {
        return Result.success(emrService.listLaboratoryApplies(queryDTO.getRegistId()));
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "检验开单（开单即落库）")
    @PostMapping("/applyUpsert")
    public Result<BizLaboratoryApplyVO> applyUpsert(@Valid @RequestBody LaboratoryApplyUpsertDTO dto) {
        return Result.success(emrService.upsertLaboratoryApply(dto));
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "删除检验申请单（仅未缴费且无执行记录）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        emrService.deleteLaboratoryApply(id);
        return Result.success("检验申请单已删除", null);
    }
}
