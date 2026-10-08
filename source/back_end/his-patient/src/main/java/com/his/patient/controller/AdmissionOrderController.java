package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.AdmissionOrderCancelDTO;
import com.his.patient.dto.AdmissionOrderQueryPageDTO;
import com.his.patient.dto.AdmissionOrderUpsertDTO;
import com.his.patient.service.AdmissionOrderService;
import com.his.patient.vo.AdmissionOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 住院证（入院通知单）—— 门诊转住院闭环的入口
 */
@Tag(name = "住院证（门诊转住院）")
@RestController
@RequestMapping("/patient/admissionOrder")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'ipd:inpatient:list')")
public class AdmissionOrderController {

    private final AdmissionOrderService admissionOrderService;

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "开住院证（门诊医生站调用）")
    @PostMapping("/create")
    public Result<String> create(@RequestBody @Valid AdmissionOrderUpsertDTO dto) {
        // 与入院登记同理：雪花ID 超过 JS 的 2^53，出参必须是字符串
        return Result.success("住院证已开具", String.valueOf(admissionOrderService.create(dto)));
    }

    @Operation(summary = "住院证分页（住院处待收治看板）")
    @GetMapping("/listPage")
    public Result<IPage<AdmissionOrderVO>> listPage(@Valid AdmissionOrderQueryPageDTO query) {
        return Result.success(admissionOrderService.listPage(query));
    }

    @Operation(summary = "住院证详情")
    @GetMapping("/detail")
    public Result<AdmissionOrderVO> detail(@RequestParam Long id) {
        return Result.success(admissionOrderService.detail(id));
    }

    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "作废住院证（仅待收治可作废）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid AdmissionOrderCancelDTO dto) {
        admissionOrderService.cancel(dto);
        return Result.success("住院证已作废", null);
    }

    @Operation(summary = "待收治且未过期的证数量")
    @GetMapping("/pendingCount")
    public Result<Long> pendingCount() {
        return Result.success(admissionOrderService.countPending());
    }
}
