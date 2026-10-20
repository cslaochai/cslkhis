package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.AdmissionOrderCancelDTO;
import com.his.patient.dto.AdmissionOrderPageQueryDTO;
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

    /**
     * 为门诊患者开具住院证，进入待收治状态。
     * 用于门诊医生站接诊后的「开住院证」弹框提交。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:add')")
    @Operation(summary = "开住院证（门诊医生站调用）")
    @PostMapping("/create")
    public Result<String> create(@RequestBody @Valid AdmissionOrderUpsertDTO dto) {
        // 与入院登记同理：雪花ID 超过 JS 的 2^53，出参必须是字符串
        return Result.success("住院证已开具", String.valueOf(admissionOrderService.create(dto)));
    }

    /**
     * 住院证分页查询，可按患者、住院证号或挂号号反查。
     * 用于住院管理页「待收治（住院证）」看板。
     */
    @Operation(summary = "住院证分页（住院处待收治看板）")
    @GetMapping("/listPage")
    public Result<IPage<AdmissionOrderVO>> listPage(@Valid AdmissionOrderPageQueryDTO query) {
        return Result.success(admissionOrderService.listPage(query));
    }

    /**
     * 取单张住院证的拟收治科室、拟诊与有效期等内容。
     * 供入院登记前核对住院证信息时取数。
     */
    @Operation(summary = "住院证详情")
    @GetMapping("/detail")
    public Result<AdmissionOrderVO> detail(@RequestParam Long id) {
        return Result.success(admissionOrderService.detail(id));
    }

    /**
     * 作废一张住院证并留写作废原因，只有待收治的证可作废。
     * 用于住院管理页待收治看板操作列的作废动作。
     */
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:delete')")
    @Operation(summary = "作废住院证（仅待收治可作废）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid AdmissionOrderCancelDTO dto) {
        admissionOrderService.cancel(dto);
        return Result.success("住院证已作废", null);
    }

    /**
     * 统计待收治且仍在有效期内的住院证张数。
     * 供住院处待收治提醒与看板入口计数。
     */
    @Operation(summary = "待收治且未过期的证数量")
    @GetMapping("/pendingCount")
    public Result<Long> pendingCount() {
        return Result.success(admissionOrderService.countPending());
    }
}
