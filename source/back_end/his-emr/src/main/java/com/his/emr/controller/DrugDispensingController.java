package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.DrugDispenseDTO;
import com.his.emr.dto.DrugReturnDTO;
import com.his.emr.service.DrugDispensingService;
import com.his.emr.vo.BizDrugDispensingVO;
import com.his.emr.vo.DrugDispensingCountVO;
import com.his.pharmacy.dto.DispensingQueryPageDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 药品发药管理控制器
 */
@Tag(name = "药品发药管理")
@RestController
@RequestMapping("/charge/dispensing")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:dispensing:list')")
public class DrugDispensingController {

    private final DrugDispensingService drugDispensingService;

    @Operation(summary = "分页查询发药明细")
    @PostMapping("/listPage")
    public Result<PageResult<BizDrugDispensingVO>> listPage(@Valid @RequestBody DispensingQueryPageDTO queryDTO) {
        PageResult<BizDrugDispensingVO> result = drugDispensingService.selectDispensingPage(
                queryDTO.getPatientId(), queryDTO.getPatientName(), queryDTO.getPrescriptionNo(),
                queryDTO.getDispensingStatus(), queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(result);
    }

    @Operation(summary = "获取发药详情")
    @GetMapping("/getById")
    public Result<BizDrugDispensingVO> getById(@RequestParam Long id) {
        return Result.success(drugDispensingService.getDispensingDetail(id));
    }

    @Operation(summary = "三态计数（待发药/已发药/已退药）")
    @GetMapping("/statusCount")
    public Result<DrugDispensingCountVO> statusCount() {
        return Result.success(drugDispensingService.getStatusCount());
    }

    @PreAuthorize("hasAuthority('pharmacy:dispensing:edit')")
    @Operation(summary = "发药（单行）")
    @PostMapping("/dispense")
    public Result<Void> dispense(@Valid @RequestBody DrugDispenseDTO actionDTO) {
        boolean success = drugDispensingService.dispense(actionDTO.getId(),
                actionDTO.getCheckerId(),
                actionDTO.getOverLimitReason());
        return success ? Result.success("发药成功", null) : Result.error("发药失败");
    }

    @PreAuthorize("hasAuthority('pharmacy:dispensing:edit')")
    @Operation(summary = "按处方整单发药")
    @PostMapping("/dispenseByPrescription")
    public Result<Void> dispenseByPrescription(@Valid @RequestBody DrugDispenseDTO actionDTO) {
        boolean success = drugDispensingService.dispenseByPrescription(actionDTO.getPrescriptionId(),
                actionDTO.getCheckerId(),
                actionDTO.getOverLimitReason());
        return success ? Result.success("整单发药成功", null) : Result.error("整单发药失败");
    }

    @PreAuthorize("hasAuthority('pharmacy:dispensing:edit')")
    @Operation(summary = "退药")
    @PostMapping("/returnDrug")
    public Result<Void> returnDrug(@Valid @RequestBody DrugReturnDTO actionDTO) {
        boolean success = drugDispensingService.returnDrug(actionDTO.getId(), actionDTO.getReason());
        return success ? Result.success("退药成功", null) : Result.error("退药失败");
    }
}
