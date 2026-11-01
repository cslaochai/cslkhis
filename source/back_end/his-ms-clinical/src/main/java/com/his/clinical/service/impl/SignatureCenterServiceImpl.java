package com.his.clinical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.spi.dto.SignCommandDTO;
import com.his.spi.service.SysAuditLogService;
import com.his.spi.vo.SignatureVO;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.clinical.dto.*;
import com.his.common.entity.CurrentUser;
import com.his.clinical.entity.BizInspectionApply;
import com.his.clinical.entity.BizLaboratoryApply;
import com.his.clinical.entity.BizMedicalRecord;
import com.his.clinical.entity.BizPrescription;
import com.his.clinical.mapper.BizInspectionApplyMapper;
import com.his.clinical.mapper.BizLaboratoryApplyMapper;
import com.his.clinical.mapper.BizMedicalRecordMapper;
import com.his.clinical.mapper.BizPrescriptionMapper;
import com.his.clinical.service.EmrSignatureService;
import com.his.clinical.service.SignatureCenterService;
import com.his.common.utils.UserUtils;
import com.his.clinical.vo.MyPendingSignVO;
import com.his.clinical.vo.OptionItemVO;
import com.his.clinical.vo.SignatureOptionsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 签名验签出参与命令装配。
 */
@Service
@RequiredArgsConstructor
public class SignatureCenterServiceImpl implements SignatureCenterService {

    private final EmrSignatureService emrSignatureService;
    private final SysAuditLogService sysAuditLogService;
    private final BizMedicalRecordMapper bizMedicalRecordMapper;
    private final BizPrescriptionMapper bizPrescriptionMapper;
    private final BizInspectionApplyMapper bizInspectionApplyMapper;
    private final BizLaboratoryApplyMapper bizLaboratoryApplyMapper;

    private static OptionItemVO opt(Integer code, String text) {
        return new OptionItemVO(code == null ? null : code.longValue(), text);
    }

    private static void fillSigner(SignCommandDTO cmd, CurrentUser user) {
        cmd.setSignerId(employeeIdOf(user));
        cmd.setSignerName(nameOf(user));
        cmd.setSignerDeptId(user == null ? null : user.getDeptId());
        cmd.setSignerDeptName(user == null ? null : user.getDeptName());
    }

    /**
     * 员工ID 优先（签名必须落到员工，不能落成系统账号 admin 的 userId=1）
     */
    private static Long employeeIdOf(CurrentUser user) {
        if (user == null) {
            return null;
        }
        return user.getEmployeeId();
    }

    private static String nameOf(CurrentUser user) {
        if (user == null) {
            return null;
        }
        if (TextUtil.hasText(user.getEmployeeName())) {
            return user.getEmployeeName();
        }
        return user.getRealName();
    }

    @Override
    public SignatureOptionsVO queryOptions() {
        SignatureOptionsVO vo = new SignatureOptionsVO();
        List<OptionItemVO> bizTypes = new ArrayList<>();
        for (SignBizTypeEnum t : SignBizTypeEnum.values()) {
            bizTypes.add(opt(t.getCode(), t.getLabel()));
        }
        List<OptionItemVO> scenes = new ArrayList<>();
        for (SignSceneEnum s : SignSceneEnum.values()) {
            scenes.add(opt(s.getCode(), s.getLabel()));
        }
        List<OptionItemVO> signStatuses = new ArrayList<>();
        for (SignStatusEnum s : SignStatusEnum.values()) {
            signStatuses.add(opt(s.getCode(), s.getLabel()));
        }
        List<OptionItemVO> verifyStatuses = new ArrayList<>();
        for (SignVerifyStatusEnum s : SignVerifyStatusEnum.values()) {
            verifyStatuses.add(opt(s.getCode(), s.getLabel()));
        }
        vo.setBizTypes(bizTypes);
        vo.setScenes(scenes);
        vo.setSignStatuses(signStatuses);
        vo.setVerifyStatuses(verifyStatuses);

        List<OptionItemVO> certStatuses = new ArrayList<>();
        for (CertStatusEnum s : CertStatusEnum.values()) {
            certStatuses.add(opt(s.getCode(), s.getLabel()));
        }
        List<OptionItemVO> issuedModes = new ArrayList<>();
        for (CertIssuedModeEnum m : CertIssuedModeEnum.values()) {
            issuedModes.add(opt(m.getCode(), m.getLabel()));
        }
        vo.setCertStatuses(certStatuses);
        vo.setIssuedModes(issuedModes);
        return vo;
    }

    @Override
    public SignatureVO sign(SignatureSignDTO dto, String pin) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(dto.getBizType());
        cmd.setBizId(dto.getBizId());
        cmd.setSignScene(dto.getSignScene() == null ? SignSceneEnum.MAKEUP.getCode() : dto.getSignScene());
        fillSigner(cmd, user);
        cmd.setPin(pin);
        cmd.setRemark("补签：" + (dto.getRemark() == null ? "" : dto.getRemark()));
        return emrSignatureService.sign(cmd);
    }

    @Override
    public List<MyPendingSignVO> listMyPendingSigns() {
        CurrentUser user = requireCurrentUser();
        Long employeeId = user.getEmployeeId();
        if (employeeId == null) {
            throw new BusinessException("未取到当前登录用户的员工ID，无法查询待补签");
        }
        List<MyPendingSignVO> list = new ArrayList<>();

        // 门诊病历：已结诊提交但签名留白（无签结诊），只有本人书写的才进「我的待补签」
        List<BizMedicalRecord> records = bizMedicalRecordMapper.selectList(new LambdaQueryWrapper<BizMedicalRecord>()
                .eq(BizMedicalRecord::getDoctorId, employeeId)
                .eq(BizMedicalRecord::getRecordStatus, RecordStatusEnum.SUBMITTED.getCode())
                .eq(BizMedicalRecord::getSignStatus, ObjectSignStatusEnum.UNSIGNED.getCode()));
        for (BizMedicalRecord r : records) {
            MyPendingSignVO vo = pendingVo(SignBizTypeEnum.OUTPATIENT_RECORD, r.getId(), r.getRecordNo(),
                    r.getPatientName(), r.getDeptName(), r.getRecordStatus(),
                    RecordStatusEnum.getLabelOrUnknown(r.getRecordStatus()), r.getVisitDate(), r.getSubmitTime());
            list.add(vo);
        }

        // 处方：开方签名还没落下（无签结诊后处方照常流转，但审方/发药侧按未签名拦截提示）
        List<BizPrescription> prescriptions = bizPrescriptionMapper.selectList(new LambdaQueryWrapper<BizPrescription>()
                .eq(BizPrescription::getDoctorId, employeeId)
                .isNull(BizPrescription::getDoctorSignId)
                .notIn(BizPrescription::getPrescriptionStatus, PrescriptionStatusEnum.CANCELLED.getCode(),
                        PrescriptionStatusEnum.RETURNED.getCode(), PrescriptionStatusEnum.RETURNED_AUDIT.getCode()));
        for (BizPrescription p : prescriptions) {
            MyPendingSignVO vo = pendingVo(SignBizTypeEnum.PRESCRIPTION, p.getId(), p.getPrescriptionNo(),
                    p.getPatientName(), p.getDeptName(), p.getPrescriptionStatus(),
                    PrescriptionStatusEnum.getLabelOrUnknown(p.getPrescriptionStatus()), p.getVisitDate(),
                    p.getCreateTime());
            list.add(vo);
        }

        // 检查申请单
        List<BizInspectionApply> inspections = bizInspectionApplyMapper.selectList(
                new LambdaQueryWrapper<BizInspectionApply>()
                        .eq(BizInspectionApply::getDoctorId, employeeId)
                        .eq(BizInspectionApply::getSignStatus, ObjectSignStatusEnum.UNSIGNED.getCode())
                        .ne(BizInspectionApply::getApplyStatus, ApplyStatusEnum.CANCELLED.getCode()));
        for (BizInspectionApply a : inspections) {
            MyPendingSignVO vo = pendingVo(SignBizTypeEnum.INSPECTION_APPLY, a.getId(), a.getApplyNo(),
                    a.getPatientName(), a.getDeptName(), a.getApplyStatus(),
                    ApplyStatusEnum.getLabelOrUnknown(a.getApplyStatus()), a.getVisitDate(), a.getSubmitTime());
            list.add(vo);
        }

        // 检验申请单
        List<BizLaboratoryApply> laboratories = bizLaboratoryApplyMapper.selectList(
                new LambdaQueryWrapper<BizLaboratoryApply>()
                        .eq(BizLaboratoryApply::getDoctorId, employeeId)
                        .eq(BizLaboratoryApply::getSignStatus, ObjectSignStatusEnum.UNSIGNED.getCode())
                        .ne(BizLaboratoryApply::getApplyStatus, ApplyStatusEnum.CANCELLED.getCode()));
        for (BizLaboratoryApply a : laboratories) {
            MyPendingSignVO vo = pendingVo(SignBizTypeEnum.LAB_APPLY, a.getId(), a.getApplyNo(),
                    a.getPatientName(), a.getDeptName(), a.getApplyStatus(),
                    ApplyStatusEnum.getLabelOrUnknown(a.getApplyStatus()), a.getVisitDate(), a.getSubmitTime());
            list.add(vo);
        }

        // 新的在前：医生先处理最近一次就诊遗留的待补签
        list.sort(Comparator.comparing(MyPendingSignVO::getBizTime,
                Comparator.nullsLast(Comparator.<java.time.LocalDateTime>reverseOrder())));
        return list;
    }

    @Override
    public SignatureVO makeupSign(MyMakeupSignDTO dto) {
        CurrentUser user = requireCurrentUser();
        Long employeeId = user.getEmployeeId();
        if (employeeId == null) {
            throw new BusinessException("未取到当前登录用户的员工ID，无法补签");
        }
        SignBizTypeEnum bizType = assertOwnedByCurrentDoctor(dto.getBizType(), dto.getBizId(), employeeId);

        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(dto.getBizType());
        cmd.setBizId(dto.getBizId());
        // 自助补签的场景固定为「补签」，不接受前端指定；签名人固定为当前登录医生本人
        cmd.setSignScene(SignSceneEnum.MAKEUP.getCode());
        fillSigner(cmd, user);
        cmd.setPin(dto.getPin());
        cmd.setRemark("医生自助补签：" + dto.getReason());

        String content = String.format("对象=%s（%s）；补签原因=%s",
                bizType.getLabel(), dto.getBizId(), dto.getReason());
        try {
            SignatureVO vo = emrSignatureService.sign(cmd);
            sysAuditLogService.record(employeeId, nameOf(user), "电子签名", "医生自助补签",
                    "biz_emr_signature", vo.getId(),
                    content + "；签名编号=" + vo.getSignNo(), true, null);
            return vo;
        } catch (RuntimeException ex) {
            // 失败同样留痕：谁在什么时间试图给哪份文书补签、为什么没成，必须查得到
            sysAuditLogService.record(employeeId, nameOf(user), "电子签名", "医生自助补签",
                    "biz_emr_signature", null, content, false, ex.getMessage());
            throw ex;
        }
    }

    private static MyPendingSignVO pendingVo(SignBizTypeEnum type, Long bizId, String bizNo, String patientName,
                                             String deptName, Integer bizStatus, String bizStatusText,
                                             java.time.LocalDate visitDate, java.time.LocalDateTime bizTime) {
        MyPendingSignVO vo = new MyPendingSignVO();
        vo.setBizType(type.getCode());
        vo.setBizTypeText(type.getLabel());
        vo.setBizId(bizId);
        vo.setBizNo(bizNo);
        vo.setPatientName(patientName);
        vo.setDeptName(deptName);
        vo.setBizStatus(bizStatus);
        vo.setBizStatusText(bizStatusText);
        vo.setVisitDate(visitDate);
        vo.setBizTime(bizTime);
        return vo;
    }

    /**
     * 校验补签对象属于当前登录医生，且仍处于「未签名」可补签状态。
     * <p>医生自助补签与管理员补签的区别就在这条：签名人只能是自己，不能让病案室/核查岗替医生代签。
     *
     * @return 对象类型枚举（供日志文案使用）
     */
    private SignBizTypeEnum assertOwnedByCurrentDoctor(Integer bizTypeCode, Long bizId, Long employeeId) {
        SignBizTypeEnum type = SignBizTypeEnum.fromCode(bizTypeCode);
        if (type == null) {
            throw new BusinessException("未知的签名对象类型：" + bizTypeCode);
        }
        Long ownerId;
        switch (type) {
            case OUTPATIENT_RECORD -> {
                BizMedicalRecord r = bizMedicalRecordMapper.selectById(bizId);
                if (r == null) {
                    throw new BusinessException("门诊病历不存在或已被删除，无法补签");
                }
                if (!Objects.equals(RecordStatusEnum.SUBMITTED.getCode(), r.getRecordStatus())) {
                    throw new BusinessException("门诊病历当前不是「已提交」状态，不能补签");
                }
                if (!Objects.equals(ObjectSignStatusEnum.UNSIGNED.getCode(), r.getSignStatus())) {
                    throw new BusinessException("门诊病历当前不是「未签名」状态（可能已签名或签名已失效），不能自助补签");
                }
                ownerId = r.getDoctorId();
            }
            case PRESCRIPTION -> {
                BizPrescription p = bizPrescriptionMapper.selectById(bizId);
                if (p == null) {
                    throw new BusinessException("处方不存在或已被删除，无法补签");
                }
                if (p.getDoctorSignId() != null) {
                    throw new BusinessException("处方已有开方签名，不能重复补签");
                }
                Integer st = p.getPrescriptionStatus();
                if (Objects.equals(PrescriptionStatusEnum.CANCELLED.getCode(), st)
                        || Objects.equals(PrescriptionStatusEnum.RETURNED.getCode(), st)
                        || Objects.equals(PrescriptionStatusEnum.RETURNED_AUDIT.getCode(), st)) {
                    throw new BusinessException("处方当前状态（"
                            + PrescriptionStatusEnum.getLabelOrUnknown(st) + "）不能补签");
                }
                ownerId = p.getDoctorId();
            }
            case INSPECTION_APPLY -> {
                BizInspectionApply a = bizInspectionApplyMapper.selectById(bizId);
                if (a == null) {
                    throw new BusinessException("检查申请单不存在或已被删除，无法补签");
                }
                if (!Objects.equals(ObjectSignStatusEnum.UNSIGNED.getCode(), a.getSignStatus())) {
                    throw new BusinessException("检查申请单当前不是「未签名」状态，不能自助补签");
                }
                if (Objects.equals(ApplyStatusEnum.CANCELLED.getCode(), a.getApplyStatus())) {
                    throw new BusinessException("检查申请单已取消，不能补签");
                }
                ownerId = a.getDoctorId();
            }
            case LAB_APPLY -> {
                BizLaboratoryApply a = bizLaboratoryApplyMapper.selectById(bizId);
                if (a == null) {
                    throw new BusinessException("检验申请单不存在或已被删除，无法补签");
                }
                if (!Objects.equals(ObjectSignStatusEnum.UNSIGNED.getCode(), a.getSignStatus())) {
                    throw new BusinessException("检验申请单当前不是「未签名」状态，不能自助补签");
                }
                if (Objects.equals(ApplyStatusEnum.CANCELLED.getCode(), a.getApplyStatus())) {
                    throw new BusinessException("检验申请单已取消，不能补签");
                }
                ownerId = a.getDoctorId();
            }
            default -> throw new BusinessException("「" + type.getLabel() + "」不支持医生自助补签");
        }
        if (!Objects.equals(ownerId, employeeId)) {
            throw new BusinessException("只能补签本人书写的" + type.getLabel() + "，不能代替其他医师签名");
        }
        return type;
    }

    private static CurrentUser requireCurrentUser() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return user;
    }

    @Override
    public SignatureVO invalidate(SignatureInvalidateDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return emrSignatureService.invalidate(dto.getSignId(), dto.getReason(), employeeIdOf(user), nameOf(user));
    }
}
