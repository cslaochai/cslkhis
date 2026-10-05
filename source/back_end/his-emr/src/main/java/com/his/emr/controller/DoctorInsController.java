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
 * 医生工作站 - 检查申请控制器
 *
 * <p><b>批次E 改造要点</b>：
 * <ul>
 *   <li>开单不再等「保存病历」，{@link #applyUpsert} 一调即落库（E1）；</li>
 *   <li>删除走 {@link #deleteById}，带缴费/执行/收费引用三重保护（E2）；</li>
 *   <li>列表按 <b>挂号</b> 取本次就诊，并带上执行进度与危急值（E5）。</li>
 * </ul>
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

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "检查开单（开单即落库）")
    @PostMapping("/applyUpsert")
    public Result<BizInspectionApplyVO> applyUpsert(@Valid @RequestBody InspectionApplyUpsertDTO dto) {
        return Result.success(emrService.upsertInspectionApply(dto));
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "删除检查申请单（仅未缴费且无执行记录）")
    @DeleteMapping("/deleteById")
    public Result<Void> deleteById(@RequestParam Long id) {
        emrService.deleteInspectionApply(id);
        return Result.success("检查申请单已删除", null);
    }
}
