package com.his.emr.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.PrescriptionPayStatusEnum;
import com.his.common.enums.PrescriptionStatusEnum;
import com.his.common.enums.PrescriptionTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.common.dto.SignCommandDTO;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;
import com.his.common.service.EmrSignatureService;
import com.his.common.vo.SignatureVO;
import com.his.emr.entity.BizInspectionApply;
import com.his.emr.entity.BizLaboratoryApply;
import com.his.emr.entity.BizPrescription;
import com.his.emr.entity.BizPrescriptionAuditLog;
import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.entity.BizMedicalRecordLog;
import com.his.emr.mapper.BizInspectionApplyMapper;
import com.his.emr.mapper.BizLaboratoryApplyMapper;
import com.his.emr.mapper.BizPrescriptionAuditLogMapper;
import com.his.emr.mapper.BizPrescriptionDetailMapper;
import com.his.emr.mapper.BizPrescriptionMapper;
import com.his.emr.mapper.BizMedicalRecordLogMapper;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.system.enums.BizTypeEnum;
import com.his.system.service.SysMessageService;
import com.his.system.dto.DrugRationalGroupDTO;
import com.his.system.dto.DrugRationalItemDTO;
import com.his.system.service.DrugRationalCheckService;
import com.his.system.vo.DrugRationalGroupVO;
import com.his.emr.dto.InspectionApplyQueryDTO;
import com.his.emr.dto.LaboratoryApplyQueryDTO;
import com.his.emr.dto.PrescriptionAuditDTO;
import com.his.emr.dto.PrescriptionQueryDTO;
import com.his.emr.dto.PrescriptionQueryPageDTO;
import com.his.emr.service.PrescriptionService;
import com.his.emr.vo.BizPrescriptionDetailVO;
import com.his.emr.vo.BizPrescriptionVO;
import com.his.emr.vo.MyPrescriptionDetailVO;
import com.his.emr.vo.MyPrescriptionVO;
import com.his.emr.vo.PrescriptionRationalVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.his.emr.enums.PrescriptionDetailStatusEnum;
import com.his.emr.enums.RxAuditActionEnum;

/**
 * 医生工作站服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl extends ServiceImpl<BizMedicalRecordMapper, BizMedicalRecord> implements PrescriptionService {

    static final String[] HUIFANG_TYPE = {"糖尿病", "高血压", "冠心病"};
    private static final AtomicInteger TASK_SEQ = new AtomicInteger(0);
    private static final AtomicInteger SEQ = new AtomicInteger(0);
    private final BizPrescriptionMapper prescriptionMapper;
    private final BizPrescriptionDetailMapper prescriptionDetailMapper;
    private final BizInspectionApplyMapper inspectionApplyMapper;
    private final BizLaboratoryApplyMapper laboratoryApplyMapper;
    private final BizMedicalRecordLogMapper recordLogMapper;
    private final EmrSignatureService signatureService;
    private final SysMessageService sysMessageService;
    private final BizPrescriptionAuditLogMapper auditLogMapper;
    private final DrugRationalCheckService drugRationalCheckService;

    @Override
    public List<BizPrescriptionVO> getByPatientId(PrescriptionQueryDTO queryDTO) {
        LambdaQueryWrapper<BizPrescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPrescription::getPatientId, queryDTO.getPatientId())
                .eq(BizPrescription::getRegistId, queryDTO.getRegistId())
                .orderByDesc(BizPrescription::getCreateTime);
        List<BizPrescription> bizPrescriptions = prescriptionMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(bizPrescriptions)) {
            return Collections.emptyList();
        }

        List<Long> ids = bizPrescriptions.stream().map(BizPrescription::getId).toList();
        LambdaQueryWrapper<BizPrescriptionDetail> queryDetailWrapper = new LambdaQueryWrapper<>();
        queryDetailWrapper.in(BizPrescriptionDetail::getPrescriptionId, ids)
                .orderByDesc(BizPrescriptionDetail::getCreateTime);
        Map<Long, List<BizPrescriptionDetail>> bizPrescriptionMap = prescriptionDetailMapper
                .selectList(queryDetailWrapper).stream().collect(Collectors.groupingBy(BizPrescriptionDetail::getPrescriptionId));

        List<BizPrescriptionVO> resultList = BeanUtil.copyToList(bizPrescriptions, BizPrescriptionVO.class);
        for (BizPrescriptionVO bizPrescriptionVO : resultList) {
            List<BizPrescriptionDetail> bizPrescriptionDetails = bizPrescriptionMap.get(bizPrescriptionVO.getId());
            bizPrescriptionVO.setDetails(BeanUtil.copyToList(bizPrescriptionDetails, BizPrescriptionDetailVO.class));
        }
        return resultList;
    }

    @Override
    public List<MyPrescriptionVO> myPrescriptions(Long patientId) {
        LambdaQueryWrapper<BizPrescription> wrapper = new LambdaQueryWrapper<>();
        // 草稿是医生没写完的方子，患者侧不展示；作废/退药要展示 —— 患者需要知道这张方子怎么了
        wrapper.eq(BizPrescription::getPatientId, patientId)
                .ne(BizPrescription::getPrescriptionStatus, PrescriptionStatusEnum.DRAFT.getCode())
                .orderByDesc(BizPrescription::getCreateTime);
        List<BizPrescription> prescriptions = prescriptionMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(prescriptions)) {
            return Collections.emptyList();
        }

        List<Long> ids = prescriptions.stream().map(BizPrescription::getId).toList();
        LambdaQueryWrapper<BizPrescriptionDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.in(BizPrescriptionDetail::getPrescriptionId, ids)
                .orderByAsc(BizPrescriptionDetail::getId);
        Map<Long, List<BizPrescriptionDetail>> detailMap = prescriptionDetailMapper.selectList(detailWrapper)
                .stream().collect(Collectors.groupingBy(BizPrescriptionDetail::getPrescriptionId));

        return prescriptions.stream().map(rx -> {
            MyPrescriptionVO vo = new MyPrescriptionVO();
            BeanUtils.copyProperties(rx, vo);
            vo.setPrescriptionTypeText(PrescriptionTypeEnum.labelOf(rx.getPrescriptionType()));
            vo.setStatusText(PrescriptionStatusEnum.labelOf(rx.getPrescriptionStatus()));
            vo.setPaymentStatusText(PrescriptionPayStatusEnum.labelOf(rx.getPaymentStatus()));
            vo.setDetails(BeanUtil.copyToList(detailMap.getOrDefault(rx.getId(), Collections.emptyList()),
                    MyPrescriptionDetailVO.class));
            return vo;
        }).toList();
    }

    @Override
    public PageResult<BizPrescriptionVO> listPage(PrescriptionQueryPageDTO query) {
        LambdaQueryWrapper<BizPrescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getPrescriptionStatus() != null, BizPrescription::getPrescriptionStatus, query.getPrescriptionStatus())
                .eq(query.getDoctorId() != null, BizPrescription::getDoctorId, query.getDoctorId())
                // 「未审方」按签名锚点判空，而不是按状态码猜 —— 状态码有 7 个值、
                // 其中 1/2 都可能"待审"，靠状态码枚举迟早漏；
                // 7-审方退回的单子签名也为空，但它等的是医生改方重提，不是药师再审
                .isNull(Boolean.TRUE.equals(query.getUnauditedOnly()), BizPrescription::getAuditSignId)
                .ne(Boolean.TRUE.equals(query.getUnauditedOnly()), BizPrescription::getPrescriptionStatus,
                        PrescriptionStatusEnum.RETURNED_AUDIT.getCode())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(BizPrescription::getPrescriptionNo, query.getKeyword())
                        .or().like(BizPrescription::getPatientName, query.getKeyword())
                        .or().like(BizPrescription::getPatientNo, query.getKeyword()))
                .orderByDesc(BizPrescription::getCreateTime);

        Page<BizPrescription> page = prescriptionMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), Collections.emptyList());
        }

        // 批量查明细：药师审方要看得到具体药品，而逐条查会是 N+1
        List<Long> ids = page.getRecords().stream().map(BizPrescription::getId).toList();
        Map<Long, List<BizPrescriptionDetail>> detailMap = prescriptionDetailMapper.selectList(
                        new LambdaQueryWrapper<BizPrescriptionDetail>()
                                .in(BizPrescriptionDetail::getPrescriptionId, ids)
                                .orderByAsc(BizPrescriptionDetail::getId))
                .stream().collect(Collectors.groupingBy(BizPrescriptionDetail::getPrescriptionId));

        List<BizPrescriptionVO> vos = BeanUtil.copyToList(page.getRecords(), BizPrescriptionVO.class);
        for (BizPrescriptionVO vo : vos) {
            List<BizPrescriptionDetail> details = detailMap.get(vo.getId());
            vo.setDetails(details == null ? Collections.emptyList()
                    : BeanUtil.copyToList(details, BizPrescriptionDetailVO.class));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), vos);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BizPrescriptionVO auditPrescription(PrescriptionAuditDTO dto, Long auditorId, String auditorName,
                                               Long auditorDeptId, String auditorDeptName) {
        BizPrescription p = prescriptionMapper.selectById(dto.getPrescriptionId());
        if (p == null) {
            throw new BusinessException("处方不存在或已被删除");
        }
        // 服务端硬准入：已签发过的不能再审（重复审会重复签名）；已发药/已作废/已退药的终态也不允许
        if (p.getAuditSignId() != null) {
            throw new BusinessException("处方 " + p.getPrescriptionNo() + " 已完成审方签发，不能重复审核");
        }
        Integer cur = p.getPrescriptionStatus();
        if (cur != null && (cur == PrescriptionStatusEnum.DISPENSED.getCode()
                || cur == PrescriptionStatusEnum.CANCELLED.getCode()
                || cur == PrescriptionStatusEnum.RETURNED.getCode()
                || cur == PrescriptionStatusEnum.RETURNED_AUDIT.getCode())) {
            throw new BusinessException(cur == PrescriptionStatusEnum.RETURNED_AUDIT.getCode()
                    ? "处方 " + p.getPrescriptionNo() + " 已被审方退回，等待医生修改后重新提交，不能直接再次审核"
                    : "处方 " + p.getPrescriptionNo() + " 当前状态（"
                    + PrescriptionStatusEnum.labelOf(cur) + "）不允许审核");
        }
        boolean pass = Integer.valueOf(1).equals(dto.getAuditResult());
        String opinion = dto.getAuditOpinion();
        // B 类保留：条件必填——仅审方退回时要求退回原因，通过可不填
        if (!pass && !StringUtils.hasText(opinion)) {
            throw new BusinessException("审方退回必须填写退回原因");
        }
        // 禁忌配伍硬闸：知识表（药物相互作用知识库）命中禁忌级时拒发「通过」。
        // 与审方列表的标注走同一个 rationalCheck，界面和后端不可能给出两套结论。
        if (pass) {
            for (PrescriptionRationalVO rational : rationalCheck(List.of(p.getId()))) {
                if (Boolean.TRUE.equals(rational.getBlocked())) {
                    throw new BusinessException("处方 " + p.getPrescriptionNo()
                            + " 存在禁忌配伍，不能签发通过：" + rational.getBlockMessage());
                }
            }
        }
        // 流水轮次 = 同病历下已有动作数 + 1（重提 = 处方先删后增，id 换了靠 record_id 串轮次）
        Long roundNo = auditLogMapper.selectCount(new LambdaQueryWrapper<BizPrescriptionAuditLog>()
                .eq(BizPrescriptionAuditLog::getRecordId, p.getRecordId())) + 1;

        if (pass) {
            // 通过：先签名、后改状态（顺序不能反 —— 签名层的准入规则要求处方处于「草稿」或「已提交」，
            // 若先把状态写成 3-已审核，签名层会当场把自己拒掉） ----
            SignCommandDTO cmd = new SignCommandDTO();
            cmd.setBizType(SignBizType.PRESCRIPTION.getCode());
            cmd.setBizId(p.getId());
            cmd.setSignScene(SignScene.RX_AUDIT.getCode());
            cmd.setSignerId(auditorId);
            cmd.setSignerName(auditorName);
            cmd.setSignerDeptId(auditorDeptId);
            cmd.setSignerDeptName(auditorDeptName);
            cmd.setRemark("审方：" + (opinion == null ? "" : opinion));
            SignatureVO sig;
            try {
                sig = signatureService.sign(cmd);
            } catch (BusinessException e) {
                throw new BusinessException("处方 " + p.getPrescriptionNo() + " 审方失败：" + e.getMessage());
            }
            // 审核时间取自签名时刻，保证「审核时间」与「签名时间」是同一瞬间（否则两处会差几百毫秒）
            BizPrescription patch = new BizPrescription();
            patch.setId(p.getId());
            patch.setPrescriptionStatus(PrescriptionStatusEnum.AUDITED.getCode());
            patch.setAuditResult(RxAuditActionEnum.PASS.getCode());
            patch.setAuditBy(auditorName);
            patch.setAuditTime(sig.getSignedTime());
            prescriptionMapper.updateById(patch);
            insertAuditLog(p, roundNo.longValue(), RxAuditActionEnum.PASS.getCode(), auditorId, auditorName, opinion);
            notifyDoctor(p, auditorName, true, opinion, null);
        } else {
            // 退回（L7）：不产生药师签名（签名=签发，拒绝签发不留签名）；
            // 状态置 7-审方退回，医生在诊间看到退回原因，改方重新保存病历即重提 ----
            BizPrescription patch = new BizPrescription();
            patch.setId(p.getId());
            patch.setPrescriptionStatus(PrescriptionStatusEnum.RETURNED_AUDIT.getCode());
            patch.setAuditResult(RxAuditActionEnum.RETURN.getCode());
            patch.setReturnReason(opinion.trim());
            patch.setReturnTime(java.time.LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
            patch.setReturnCount((p.getReturnCount() == null ? 0 : p.getReturnCount()) + 1);
            prescriptionMapper.updateById(patch);
            insertAuditLog(p, roundNo.longValue(), RxAuditActionEnum.RETURN.getCode(), auditorId, auditorName, opinion.trim());
            notifyDoctor(p, auditorName, false, opinion.trim(), patch.getReturnCount());
        }

        BizPrescription fresh = prescriptionMapper.selectById(p.getId());
        if (fresh != null) {
            fresh.setDetails(prescriptionDetailMapper.selectList(
                    new LambdaQueryWrapper<BizPrescriptionDetail>()
                            .eq(BizPrescriptionDetail::getPrescriptionId, p.getId())
                            .orderByAsc(BizPrescriptionDetail::getId)));
        }
        return toVo(fresh);
    }

    @Override
    public List<PrescriptionRationalVO> rationalCheck(List<Long> prescriptionIds) {
        if (CollectionUtils.isEmpty(prescriptionIds)) {
            return Collections.emptyList();
        }
        // 入参可能带重复 id（前端整页勾选 + 单行点击同时触发），Set 保序去重，返回顺序与入参一致
        List<Long> ids = new ArrayList<>(new LinkedHashSet<>(prescriptionIds));
        Map<Long, List<BizPrescriptionDetail>> detailMap = prescriptionDetailMapper.selectList(
                        new LambdaQueryWrapper<BizPrescriptionDetail>()
                                .in(BizPrescriptionDetail::getPrescriptionId, ids)
                                .ne(BizPrescriptionDetail::getDetailStatus, PrescriptionDetailStatusEnum.RETURNED.getCode())
                                .orderByAsc(BizPrescriptionDetail::getId))
                .stream().collect(Collectors.groupingBy(BizPrescriptionDetail::getPrescriptionId,
                        LinkedHashMap::new, Collectors.toList()));

        List<DrugRationalGroupDTO> groups = new ArrayList<>();
        for (Long id : ids) {
            List<BizPrescriptionDetail> details = detailMap.get(id);
            if (CollectionUtils.isEmpty(details)) {
                continue;
            }
            DrugRationalGroupDTO group = new DrugRationalGroupDTO();
            group.setGroupId(String.valueOf(id));
            group.setItems(details.stream().map(this::toRationalItem).toList());
            groups.add(group);
        }

        Map<String, DrugRationalGroupVO> checked = drugRationalCheckService.checkGroups(groups).stream()
                .collect(Collectors.toMap(DrugRationalGroupVO::getGroupId, g -> g, (a, b) -> a));
        List<PrescriptionRationalVO> results = new ArrayList<>(ids.size());
        for (Long id : ids) {
            DrugRationalGroupVO group = checked.get(String.valueOf(id));
            PrescriptionRationalVO vo = new PrescriptionRationalVO();
            vo.setPrescriptionId(id);
            vo.setHits(group == null || group.getHits() == null ? Collections.emptyList() : group.getHits());
            vo.setBlocked(group != null && Boolean.TRUE.equals(group.getBlocked()));
            vo.setBlockMessage(group == null ? null : group.getBlockMessage());
            results.add(vo);
        }
        return results;
    }

    /**
     * 处方明细 → 审查入参：名称与规格取<b>单据快照</b>而不是回查药品字典，
     * 这样知识表改了、字典也改了，一张老处方被重新审查时结论仍然按当时开的那份药算。
     */
    private DrugRationalItemDTO toRationalItem(BizPrescriptionDetail detail) {
        DrugRationalItemDTO item = new DrugRationalItemDTO();
        item.setDrugId(detail.getDrugId());
        item.setDrugName(detail.getDrugName());
        item.setGenericName(detail.getGenericName());
        item.setSpecification(detail.getSpecification());
        item.setSingleDosage(detail.getSingleDosage());
        item.setFrequency(detail.getFrequency());
        return item;
    }

    /** 审方动作流水（只增）。 */
    private void insertAuditLog(BizPrescription p, Long roundNo, int action, Long auditorId,
                                String auditorName, String opinion) {
        BizPrescriptionAuditLog logRow = new BizPrescriptionAuditLog();
        logRow.setPrescriptionId(p.getId());
        logRow.setPrescriptionNo(p.getPrescriptionNo());
        logRow.setRecordId(p.getRecordId());
        logRow.setRegistId(p.getRegistId());
        logRow.setRoundNo(roundNo.intValue());
        logRow.setAction(action);
        logRow.setAuditorId(auditorId);
        logRow.setAuditorName(auditorName);
        // 写库文本先截到列宽，防止 Data too long 把业务失败升级成 500
        logRow.setOpinion(opinion != null && opinion.length() > 500 ? opinion.substring(0, 500) : opinion);
        auditLogMapper.insert(logRow);
    }

    /**
     * 审方结果 → 站内信通知开方医生（bizType=drugaudit，通知型：read_status 即闭环）。
     * 审方是药师在审方工作台做的，医生在诊间看不到；结果（尤其退回原因）必须推给本人。
     * 通知失败只留 warn 日志，不回滚审方。
     */
    private void notifyDoctor(BizPrescription p, String auditorName, boolean pass, String opinion, Integer returnNo) {
        if (p.getDoctorId() == null) {
            return;
        }
        try {
            String content = pass
                    ? String.format("您为患者 %s 开具的处方 %s 已由药师 %s 审核通过。%s",
                            p.getPatientName(), p.getPrescriptionNo(), auditorName,
                            StringUtils.hasText(opinion) ? "审方意见：" + opinion : "")
                    : String.format("您为患者 %s 开具的处方 %s 被药师 %s 退回（第 %s 次）。退回原因：%s。请修改处方后重新提交。",
                            p.getPatientName(), p.getPrescriptionNo(), auditorName, returnNo, opinion);
            String payload = JSONUtil.toJsonStr(new java.util.LinkedHashMap<String, Object>() {{
                put("patientName", p.getPatientName());
                put("prescriptionNo", p.getPrescriptionNo());
                put("auditBy", auditorName);
                put("auditResult", pass ? 1 : 2);
                put("opinion", opinion);
            }});
            sysMessageService.sendSystemMessage(p.getDoctorId(), p.getDoctorName(),
                    "处方审核结果：" + p.getPrescriptionNo(), content,
                    BizTypeEnum.DRUG_AUDIT.getType(), p.getId(), pass ? "warning" : "error", payload, null);
        } catch (Exception ex) {
            log.warn("[审方] 结果通知发送失败 prescriptionNo={} doctorId={}",
                    p.getPrescriptionNo(), p.getDoctorId(), ex);
        }
    }

    /**
     * 处方实体转 VO（含明细列表）
     */
    private BizPrescriptionVO toVo(BizPrescription prescription) {
        if (prescription == null) {
            return null;
        }
        BizPrescriptionVO vo = new BizPrescriptionVO();
        BeanUtils.copyProperties(prescription, vo);
        if (prescription.getDetails() != null) {
            vo.setDetails(prescription.getDetails().stream().map(detail -> {
                BizPrescriptionDetailVO detailVO = new BizPrescriptionDetailVO();
                BeanUtils.copyProperties(detail, detailVO);
                return detailVO;
            }).collect(Collectors.toList()));
        }
        return vo;
    }

    @Override
    public List<BizInspectionApply> getByPatientId(InspectionApplyQueryDTO queryDTO) {
        LambdaQueryWrapper<BizInspectionApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizInspectionApply::getPatientId, queryDTO.getPatientId())
                .orderByDesc(BizInspectionApply::getCreateTime);
        return inspectionApplyMapper.selectList(wrapper);
    }

    @Override
    public List<BizLaboratoryApply> getByPatientId(LaboratoryApplyQueryDTO queryDTO) {
        LambdaQueryWrapper<BizLaboratoryApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizLaboratoryApply::getPatientId, queryDTO.getPatientId())
                .orderByDesc(BizLaboratoryApply::getCreateTime);
        return laboratoryApplyMapper.selectList(wrapper);
    }

    private void addRecordLog(Long recordId, String recordNo, String operation,
                              String fieldName, String oldValue, String newValue) {
        BizMedicalRecordLog log = new BizMedicalRecordLog();
        log.setRecordId(recordId);
        log.setRecordNo(recordNo);
        log.setOperation(operation);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        recordLogMapper.insert(log);
    }
}
