package com.his.clinical.controller;

import com.his.common.base.Result;
import com.his.clinical.dto.LaboratoryApplyQueryDTO;
import com.his.clinical.dto.LaboratoryApplyUpsertDTO;
import com.his.clinical.service.EmrService;
import com.his.clinical.vo.BizLaboratoryApplyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检验申请控制器：门诊医生工作站检验申请的开单、按就诊取数与删除入口。
 */
@Tag(name = "检验申请")
@RestController
@RequestMapping("/laboratory")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class DoctorLabController {

    private final EmrService emrService;

    /**
     * 取本次挂号下已开的检验申请及其执行进度。
     * 用于医生工作站「检验申请」子卡进入就诊时回显历史开单。
     */
    @Operation(summary = "查询检验申请列表（按挂号，含执行进度）")
    @GetMapping("/getByPatientId")
    public Result<List<BizLaboratoryApplyVO>> getByPatientId(@Valid LaboratoryApplyQueryDTO queryDTO) {
        return Result.success(emrService.listLaboratoryApplies(queryDTO.getRegistId()));
    }

    /**
     * 开立或更新一张检验申请，保存即落库并参与收费。
     * 用于医生工作站「检验申请」子卡的开单提交，不再等病历结诊。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "检验开单（开单即落库）")
    @PostMapping("/applyUpsert")
    public Result<BizLaboratoryApplyVO> applyUpsert(@Valid @RequestBody LaboratoryApplyUpsertDTO dto) {
        return Result.success(emrService.upsertLaboratoryApply(dto));
    }

    /**
     * 作废并删除一张检验申请单。
     * 用于医生工作站「检验申请」子卡行内删除；已缴费或已有执行记录的由服务层拒绝。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "删除检验申请单（仅未缴费且无执行记录）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        emrService.deleteLaboratoryApply(id);
        return Result.success("检验申请单已删除", null);
    }
}
