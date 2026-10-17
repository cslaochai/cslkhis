package com.his.miniapp.controller;

import com.his.api.dto.AppointCancelDTO;
import com.his.api.dto.AppointQueryDTO;
import com.his.api.dto.AppointUpsertDTO;
import com.his.api.service.AppointProvider;
import com.his.api.vo.BizAppointInfoListVO;
import com.his.api.vo.RegistBriefVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.common.util.TextUtil;
import com.his.miniapp.dto.PayRefundDTO;
import com.his.miniapp.service.MiniPayService;
import com.his.api.service.PatientProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 患者端预约挂号（线上只放预约池普通初诊号，registSource=4 由前端固定传入）。
 */
@Tag(name = "患者端-预约挂号")
@RestController
@RequestMapping("/miniapp/appoint")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniAppointController {

    private final AppointProvider appointProvider;

    private final PatientProvider patientProvider;

    private final MiniPayService miniPayService;

    @Operation(summary = "患者挂号（新增；预约池扣号）")
    @PostMapping("/upsert")
    public Result<BizAppointInfoListVO> upsert(@RequestBody @Valid AppointUpsertDTO upsertDTO) {
        if (patientProvider.patientScopeViolated(upsertDTO.getPatientId())) {
            return Result.error("无权为该就诊人挂号");
        }
        if (upsertDTO.getId() != null) {
            return Result.error("患者端不支持改号，请退号后重新预约");
        }
        if (upsertDTO.getRegistSource() == null || upsertDTO.getRegistSource() != 4) {
            upsertDTO.setRegistSource(4);
        }
        BizAppointInfoListVO result = appointProvider.addAppoint(upsertDTO);
        return Result.success("挂号成功", result);
    }

    @Operation(summary = "我的预约（分页，按绑定关系校验）")
    @GetMapping("/listPage")
    public Result<PageResult<BizAppointInfoListVO>> listPage(@Valid AppointQueryDTO queryDTO) {
        if (queryDTO.getPatientId() != null
                && !patientProvider.canAccessPatient(queryDTO.getPatientId())) {
            return Result.error("无权查询该就诊人的预约");
        }
        return Result.success(appointProvider.listRegistPage(queryDTO));
    }

    @Operation(summary = "挂号详情")
    @GetMapping("/getDetail")
    public Result<BizAppointInfoListVO> getDetail(@Valid AppointQueryDTO queryDTO) {
        RegistBriefVO regist = appointProvider.findRegist(queryDTO.getPatientId());
        if (regist != null && patientProvider.patientScopeViolated(regist.getPatientId())) {
            return Result.error("无权查看该挂号");
        }
        return Result.success(toVO(regist));
    }

    @Operation(summary = "退号（联动已支付挂号费原路退回）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid AppointCancelDTO appointCancelDTO) {
        Long registId = appointCancelDTO.getRegistId();
        // 归属从挂号记录反查，不看前端传的是谁
        RegistBriefVO regist = appointProvider.findRegist(registId);
        if (regist == null) {
            return Result.error("挂号记录不存在");
        }
        if (patientProvider.patientScopeViolated(regist.getPatientId())) {
            return Result.error("无权退该挂号");
        }
        String reason = TextUtil.hasText(appointCancelDTO.getReason())
                ? appointCancelDTO.getReason() : "患者主动退号";
        boolean success = appointProvider.cancelRegist(registId, reason);
        if (!success) {
            return Result.error("退号失败");
        }
        // 已支付的挂号费单原路退回（无支付单则幂等跳过）
        PayRefundDTO refund = new PayRefundDTO();
        refund.setBizType(2);
        refund.setBizId(registId);
        refund.setReason(reason);
        miniPayService.refundByBiz(refund);
        return Result.success("退号成功", null);
    }

    private BizAppointInfoListVO toVO(RegistBriefVO entity) {
        if (entity == null) {
            return null;
        }
        BizAppointInfoListVO vo = new BizAppointInfoListVO();
        org.springframework.beans.BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
