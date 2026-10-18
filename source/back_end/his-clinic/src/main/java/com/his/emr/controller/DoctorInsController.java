package com.his.emr.controller;

import com.his.common.base.Result;
import com.his.emr.dto.InspectionApplyQueryDTO;
import com.his.emr.dto.InspectionApplyUpsertDTO;
import com.his.emr.service.EmrService;
import com.his.emr.vo.BizInspectionApplyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 检查申请控制器：医生工作站开检查单与申请单维护的接口入口。
 */
@Tag(name = "检查申请")
@RestController
@RequestMapping("/inspection")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
public class DoctorInsController {

    private final EmrService emrService;

    /**
     * 查询本次就诊的检查申请列表（不分页）。
     *
     * <p>路径沿用 {@code /getByPatientId}（前端 API 层的稳定契约），
     * 但过滤口径已从「患者」改为「挂号」：一个患者多次就诊各开各的检查，
     * 只按 patientId 查会把上一次就诊的检查单显示到这一次身上。
     */
    @Operation(summary = "查询检查申请列表（按挂号，含执行进度）")
    @GetMapping("/getByPatientId")
    public Result<List<BizInspectionApplyVO>> getByPatientId(@Valid InspectionApplyQueryDTO queryDTO) {
        return Result.success(emrService.listInspectionApplies(queryDTO.getRegistId()));
    }

    /**
     * 检查开单：挂到本次挂号上，选好项目与检查目的后直接落成申请单。
     * 用于医生工作站检查开单弹框确认时提交，不等病历保存。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "检查开单（开单即落库）")
    @PostMapping("/applyUpsert")
    public Result<BizInspectionApplyVO> applyUpsert(@Valid @RequestBody InspectionApplyUpsertDTO dto) {
        return Result.success(emrService.upsertInspectionApply(dto));
    }

    /**
     * 撤销检查申请单：钱没交、医技那边也没留下执行记录时才允许删。
     * 用于医生工作站本次就诊的检查列表行删除，开错项目时收回。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "删除检查申请单（仅未缴费且无执行记录）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        emrService.deleteInspectionApply(id);
        return Result.success("检查申请单已删除", null);
    }
}
