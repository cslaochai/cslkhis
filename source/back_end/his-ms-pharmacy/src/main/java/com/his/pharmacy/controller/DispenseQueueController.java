package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.pharmacy.dto.DispenseCallDTO;
import com.his.pharmacy.dto.DispenseQueuePageQueryDTO;
import com.his.pharmacy.dto.DispenseSignInDTO;
import com.his.pharmacy.service.DispenseQueueService;
import com.his.pharmacy.vo.DispenseQueueVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门诊取药队列控制器：签到取号、窗口叫号、过号恢复与队列查询。
 *
 * <p>与 {@link DrugDispensingController} 的分工：那边是「发了什么药、扣了多少库存」，
 * 这边是「患者到没到、什么时候到、被哪个窗口叫走」。发药动作仍走那边，
 * 这边只负责在整单发完后回写队列状态。
 */
@Tag(name = "门诊取药队列")
@RestController
@RequestMapping("/charge/dispenseQueue")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('pharmacy:dispensing:list')")
public class DispenseQueueController {

    private final DispenseQueueService dispenseQueueService;

    /**
     * 签到取号：传处方ID按处方取号，传处方号按号反查处方，传患者ID按该患者所有待发药处方各取一个号。
     * 同一个处方当天重复签到返回原号，不重复发号。
     */
    @PreAuthorize("hasAuthority('pharmacy:dispensing:edit')")
    @Operation(summary = "签到取号")
    @PostMapping("/signIn")
    public Result<List<DispenseQueueVO>> signIn(@RequestBody DispenseSignInDTO signInDTO) {
        if (signInDTO.getPrescriptionId() == null && signInDTO.getPatientId() == null
                && !TextUtil.hasText(signInDTO.getPrescriptionNo())) {
            throw new BusinessException("处方ID、处方号与患者ID不能同时为空");
        }
        if (signInDTO.getPrescriptionId() != null) {
            DispenseQueueVO vo = dispenseQueueService.signIn(signInDTO.getPrescriptionId(), signInDTO.getSignInType());
            return Result.success(List.of(vo));
        }
        if (TextUtil.hasText(signInDTO.getPrescriptionNo())) {
            DispenseQueueVO vo = dispenseQueueService.signInByPrescriptionNo(
                    signInDTO.getPrescriptionNo(), signInDTO.getSignInType());
            return Result.success(List.of(vo));
        }
        return Result.success(dispenseQueueService.signInByPatient(signInDTO.getPatientId(), signInDTO.getSignInType()));
    }

    /**
     * 该患者名下所有待取药处方（含是否已签到、已发的号），刷身份证后先列出来
     */
    @Operation(summary = "患者待取药处方")
    @GetMapping("/listWaitingByPatient")
    public Result<List<DispenseQueueVO>> listWaitingByPatient(@RequestParam Long patientId) {
        return Result.success(dispenseQueueService.listWaitingByPatient(patientId));
    }

    /**
     * 该处方当天的取药号（未签到返回 null）
     */
    @Operation(summary = "按处方查取药号")
    @GetMapping("/getByPrescription")
    public Result<DispenseQueueVO> getByPrescription(@RequestParam Long prescriptionId) {
        return Result.success(dispenseQueueService.getByPrescription(prescriptionId));
    }

    /**
     * 取药队列分页（不传日期按当天）
     */
    @Operation(summary = "取药队列分页")
    @PostMapping("/listPage")
    public Result<PageResult<DispenseQueueVO>> listPage(@RequestBody DispenseQueuePageQueryDTO queryDTO) {
        return Result.success(dispenseQueueService.selectQueuePage(queryDTO));
    }

    /**
     * 窗口叫下一位：取最早签到且未分配窗口的号，绑到该窗口
     */
    @PreAuthorize("hasAuthority('pharmacy:dispensing:edit')")
    @Operation(summary = "叫下一位")
    @PostMapping("/callNext")
    public Result<DispenseQueueVO> callNext(@RequestBody DispenseCallDTO callDTO) {
        return Result.success(dispenseQueueService.callNext(callDTO.getWindowId()));
    }

    /**
     * 重叫：把该窗口当前正在叫的号再叫一次
     */
    @PreAuthorize("hasAuthority('pharmacy:dispensing:edit')")
    @Operation(summary = "重叫")
    @PostMapping("/recall")
    public Result<DispenseQueueVO> recall(@RequestBody DispenseCallDTO callDTO) {
        return Result.success(dispenseQueueService.recall(callDTO.getWindowId()));
    }

    /**
     * 该窗口当前正在叫的号（叫号屏轮询，无人在叫返回 null）
     */
    @Operation(summary = "当前叫号")
    @GetMapping("/currentCall")
    public Result<DispenseQueueVO> currentCall(@RequestParam Long windowId) {
        return Result.success(dispenseQueueService.currentCall(windowId));
    }

    /**
     * 过号
     */
    @PreAuthorize("hasAuthority('pharmacy:dispensing:edit')")
    @Operation(summary = "过号")
    @PostMapping("/skip")
    public Result<Void> skip(@RequestBody DispenseCallDTO callDTO) {
        boolean success = dispenseQueueService.skip(callDTO.getQueueId(), callDTO.getReason());
        return success ? Result.success("已过号", null) : Result.error("过号失败");
    }

    /**
     * 取消取药号
     */
    @PreAuthorize("hasAuthority('pharmacy:dispensing:edit')")
    @Operation(summary = "取消取药号")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody DispenseCallDTO callDTO) {
        boolean success = dispenseQueueService.cancel(callDTO.getQueueId(), callDTO.getReason());
        return success ? Result.success("已取消", null) : Result.error("取消失败");
    }

    /**
     * 过号后恢复排队（号不变，排到队尾）
     */
    @PreAuthorize("hasAuthority('pharmacy:dispensing:edit')")
    @Operation(summary = "恢复排队")
    @PostMapping("/revive")
    public Result<Void> revive(@RequestBody DispenseCallDTO callDTO) {
        boolean success = dispenseQueueService.revive(callDTO.getQueueId());
        return success ? Result.success("已恢复排队", null) : Result.error("恢复失败");
    }
}
