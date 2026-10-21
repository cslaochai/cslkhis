package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.DrugDispenseDTO;
import com.his.emr.dto.DrugReturnDTO;
import com.his.emr.service.DrugDispensingService;
import com.his.emr.vo.BizDrugDispensingVO;
import com.his.emr.vo.DrugDispensingCountVO;
import com.his.emr.dto.DispensingPageQueryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 药品发药控制器：药房发药窗口的待发药查询、复核发药与患者退药入口。
 */
@Tag(name = "药品发药管理")
@RestController
@RequestMapping("/charge/dispensing")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:dispensing:list')")
public class DrugDispensingController {

    private final DrugDispensingService drugDispensingService;

    /**
     * 按患者、处方号与发药状态分页查发药明细。
     * 用于药房发药窗口的主列表，也用于药品追溯页发药核销时挑选已发药记录。
     */
    @Operation(summary = "分页查询发药明细")
    @PostMapping("/listPage")
    public Result<PageResult<BizDrugDispensingVO>> listPage(@Valid @RequestBody DispensingPageQueryDTO queryDTO) {
        PageResult<BizDrugDispensingVO> result = drugDispensingService.selectDispensingPage(
                queryDTO.getPatientId(), queryDTO.getPatientName(), queryDTO.getPrescriptionNo(),
                queryDTO.getDispensingStatus(), queryDTO.getPageNum(), queryDTO.getPageSize());
        return Result.success(result);
    }

    /**
     * 按发药记录主键取单条发药明细。
     * 用于药房发药窗口的「发药明细详情」弹框回显。
     */
    @Operation(summary = "获取发药详情")
    @GetMapping("/getById")
    public Result<BizDrugDispensingVO> getById(@RequestParam Long id) {
        return Result.success(drugDispensingService.getDispensingDetail(id));
    }

    /**
     * 统计待发药 / 已发药 / 已退药三个状态的总数。
     * 用于药房发药窗口顶部的三张状态计数卡，计数不受当前页筛选影响。
     */
    @Operation(summary = "三态计数（待发药/已发药/已退药）")
    @GetMapping("/statusCount")
    public Result<DrugDispensingCountVO> statusCount() {
        return Result.success(drugDispensingService.getStatusCount());
    }

    /**
     * 对单条发药明细执行发药并扣减药房侧库存。
     * 用于发药窗口的「发药复核」弹框确认；麻精药品需带复核药师，第二类精神药品超量需带医师理由。
     */
    @PreAuthorize("hasAuthority('pharmacy:dispensing:edit')")
    @Operation(summary = "发药（单行）")
    @PostMapping("/dispense")
    public Result<Void> dispense(@Valid @RequestBody DrugDispenseDTO actionDTO) {
        boolean success = drugDispensingService.dispense(actionDTO.getId(),
                actionDTO.getCheckerId(),
                actionDTO.getOverLimitReason());
        return success ? Result.success("发药成功", null) : Result.error("发药失败");
    }

    /**
     * 按处方整单发药，任一药品库存不足则整单不执行。
     * 用于发药窗口「整单发药」按钮进入的复核弹框确认。
     */
    @PreAuthorize("hasAuthority('pharmacy:dispensing:edit')")
    @Operation(summary = "按处方整单发药")
    @PostMapping("/dispenseByPrescription")
    public Result<Void> dispenseByPrescription(@Valid @RequestBody DrugDispenseDTO actionDTO) {
        boolean success = drugDispensingService.dispenseByPrescription(actionDTO.getPrescriptionId(),
                actionDTO.getCheckerId(),
                actionDTO.getOverLimitReason());
        return success ? Result.success("整单发药成功", null) : Result.error("整单发药失败");
    }

    /**
     * 登记患者退药并把药回加到药房侧库存。
     * 用于发药窗口列表行的「退药」，需填退药原因；退费前的退药闸门以此为准。
     */
    @PreAuthorize("hasAuthority('pharmacy:dispensing:edit')")
    @Operation(summary = "退药")
    @PostMapping("/returnDrug")
    public Result<Void> returnDrug(@Valid @RequestBody DrugReturnDTO actionDTO) {
        boolean success = drugDispensingService.returnDrug(actionDTO.getId(), actionDTO.getReason());
        return success ? Result.success("退药成功", null) : Result.error("退药失败");
    }
}
