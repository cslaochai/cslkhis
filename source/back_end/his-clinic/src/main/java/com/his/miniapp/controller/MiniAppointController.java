package com.his.miniapp.controller;

import com.his.appoint.dto.AppointCancelDTO;
import com.his.appoint.dto.AppointQueryDTO;
import com.his.appoint.dto.AppointUpsertDTO;
import com.his.appoint.service.AppointFacadeService;
import com.his.appoint.vo.BizAppointInfoListVO;
import com.his.appoint.vo.RegistBriefVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.common.util.TextUtil;
import com.his.miniapp.dto.PayRefundDTO;
import com.his.miniapp.service.MiniPayService;
import com.his.patient.service.PatientFacadeService;
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

    private final AppointFacadeService appointFacadeService;

    private final PatientFacadeService patientFacadeService;

    private final MiniPayService miniPayService;

    /**
     * 患者线上挂号：新建挂号记录并从预约池扣号，不支持改号。
     * 用于小程序「预约挂号」页选好号源后确认提交。
     */
    @Operation(summary = "患者挂号（新增；预约池扣号）")
    @PostMapping("/upsert")
    public Result<BizAppointInfoListVO> upsert(@RequestBody @Valid AppointUpsertDTO upsertDTO) {
        if (patientFacadeService.patientScopeViolated(upsertDTO.getPatientId())) {
            return Result.error("无权为该就诊人挂号");
        }
        if (upsertDTO.getId() != null) {
            return Result.error("患者端不支持改号，请退号后重新预约");
        }
        if (upsertDTO.getRegistSource() == null || upsertDTO.getRegistSource() != 4) {
            upsertDTO.setRegistSource(4);
        }
        BizAppointInfoListVO result = appointFacadeService.addAppoint(upsertDTO);
        return Result.success("挂号成功", result);
    }

    /**
     * 我的预约分页：按登录态绑定关系收窄的挂号列表。
     * 用于小程序「我的预约」页列表与「首页」近期预约卡片取数。
     */
    @Operation(summary = "我的预约（分页，按绑定关系校验）")
    @GetMapping("/listPage")
    public Result<PageResult<BizAppointInfoListVO>> listPage(@Valid AppointQueryDTO queryDTO) {
        if (queryDTO.getPatientId() != null
                && !patientFacadeService.canAccessPatient(queryDTO.getPatientId())) {
            return Result.error("无权查询该就诊人的预约");
        }
        return Result.success(appointFacadeService.listRegistPage(queryDTO));
    }

    /**
     * 单条挂号详情：按就诊人取其当前挂号记录，越权即拒。
     * 供挂号成功页回显与退号、签到、预问诊等后续动作前的归属核对；小程序暂无独立详情页直接调用。
     */
    @Operation(summary = "挂号详情")
    @GetMapping("/getDetail")
    public Result<BizAppointInfoListVO> getDetail(@Valid AppointQueryDTO queryDTO) {
        RegistBriefVO regist = appointFacadeService.findRegist(queryDTO.getPatientId());
        if (regist != null && patientFacadeService.patientScopeViolated(regist.getPatientId())) {
            return Result.error("无权查看该挂号");
        }
        return Result.success(toVO(regist));
    }

    /**
     * 患者退号：取消挂号记录并把已支付的挂号费原路退回，归属从挂号记录反查。
     * 用于小程序「我的预约」页的退号操作。
     */
    @Operation(summary = "退号（联动已支付挂号费原路退回）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid AppointCancelDTO appointCancelDTO) {
        Long registId = appointCancelDTO.getRegistId();
        // 归属从挂号记录反查，不看前端传的是谁
        RegistBriefVO regist = appointFacadeService.findRegist(registId);
        if (regist == null) {
            return Result.error("挂号记录不存在");
        }
        if (patientFacadeService.patientScopeViolated(regist.getPatientId())) {
            return Result.error("无权退该挂号");
        }
        String reason = TextUtil.hasText(appointCancelDTO.getReason())
                ? appointCancelDTO.getReason() : "患者主动退号";
        boolean success = appointFacadeService.cancelRegist(registId, reason);
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
