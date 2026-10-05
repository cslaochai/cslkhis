package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.emr.service.ApplyExecStatusGateway;
import com.his.medicaltech.entity.BizCriticalValue;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.enums.InsRecordStatusEnum;
import com.his.medicaltech.enums.LabRecordStatusEnum;
import com.his.medicaltech.mapper.BizCriticalValueMapper;
import com.his.medicaltech.mapper.BizInspectionRecordMapper;
import com.his.medicaltech.mapper.BizLaboratoryRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link ApplyExecStatusGateway} 的实现（批次E/E5：结果回显到医生站）。
 *
 * <p>放在 his-medicaltech 是因为「执行进度」是本模块的事实；
 * 接口定义在 his-emr 是为了让依赖方向保持 his-medicaltech -&gt; his-emr 单向。
 */
@Service
@RequiredArgsConstructor
public class ApplyExecStatusGatewayImpl implements ApplyExecStatusGateway {

    /**
     * 检查记录「已签到」起算已开始执行
     */
    private static final int INS_STARTED_FROM = InsRecordStatusEnum.SIGNED_IN.getCode();

    /**
     * 检验记录「已采样」起算已开始执行（标本都抽了，不能说没开始）
     */
    private static final int LAB_STARTED_FROM = LabRecordStatusEnum.SAMPLED.getCode();

    private final BizInspectionRecordMapper inspectionRecordMapper;
    private final BizLaboratoryRecordMapper laboratoryRecordMapper;
    private final BizCriticalValueMapper criticalValueMapper;

    @Override
    public List<ExecStatus> listInspectionExecStatus(List<Long> applyIds) {
        if (CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyList();
        }
        List<BizInspectionRecord> records = inspectionRecordMapper.selectList(
                new LambdaQueryWrapper<BizInspectionRecord>()
                        .in(BizInspectionRecord::getApplyId, applyIds)
                        .orderByAsc(BizInspectionRecord::getId));
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Set<Long> criticalRecordIds = criticalRecordIds(
                records.stream().map(BizInspectionRecord::getId).collect(Collectors.toList()));

        List<ExecStatus> result = new ArrayList<>(records.size());
        Set<Long> seenApply = new HashSet<>();
        for (BizInspectionRecord r : records) {
            // 存量数据里同一申请单可能有多条执行记录（幂等缺失留下的），
            // 这里只取最早那条作为「主线」，其余不参与展示 —— 但也不假装它们不存在，
            // 由 verification 脚本去报「同申请单多条记录」这个数据问题。
            if (!seenApply.add(r.getApplyId())) {
                continue;
            }
            ExecStatus s = new ExecStatus();
            s.setApplyId(r.getApplyId());
            s.setExecRecordId(r.getId());
            s.setExecStatus(r.getRecordStatus());
            s.setExecStatusText(inspectionText(r.getRecordStatus()));
            s.setStarted(r.getRecordStatus() != null && r.getRecordStatus() >= INS_STARTED_FROM);
            s.setCritical(criticalRecordIds.contains(r.getId()));
            result.add(s);
        }
        return result;
    }

    @Override
    public List<ExecStatus> listLaboratoryExecStatus(List<Long> applyIds) {
        if (CollectionUtils.isEmpty(applyIds)) {
            return Collections.emptyList();
        }
        List<BizLaboratoryRecord> records = laboratoryRecordMapper.selectList(
                new LambdaQueryWrapper<BizLaboratoryRecord>()
                        .in(BizLaboratoryRecord::getApplyId, applyIds)
                        .orderByAsc(BizLaboratoryRecord::getId));
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Set<Long> criticalRecordIds = criticalRecordIds(
                records.stream().map(BizLaboratoryRecord::getId).collect(Collectors.toList()));

        List<ExecStatus> result = new ArrayList<>(records.size());
        Set<Long> seenApply = new HashSet<>();
        for (BizLaboratoryRecord r : records) {
            if (!seenApply.add(r.getApplyId())) {
                continue;
            }
            ExecStatus s = new ExecStatus();
            s.setApplyId(r.getApplyId());
            s.setExecRecordId(r.getId());
            s.setExecStatus(r.getRecordStatus());
            s.setExecStatusText(laboratoryText(r.getRecordStatus()));
            s.setStarted(r.getRecordStatus() != null && r.getRecordStatus() >= LAB_STARTED_FROM);
            s.setCritical(criticalRecordIds.contains(r.getId()));
            result.add(s);
        }
        return result;
    }

    /**
     * 查这批执行记录里有哪些挂了未作废的危急值（status != 4 已作废）。
     */
    private Set<Long> criticalRecordIds(List<Long> recordIds) {
        if (CollectionUtils.isEmpty(recordIds)) {
            return Collections.emptySet();
        }
        List<BizCriticalValue> list = criticalValueMapper.selectList(
                new LambdaQueryWrapper<BizCriticalValue>()
                        .in(BizCriticalValue::getRecordId, recordIds)
                        .ne(BizCriticalValue::getStatus, 4));
        return list.stream().map(BizCriticalValue::getRecordId).collect(Collectors.toSet());
    }

    /**
     * 检查记录状态 → 医生站文案。
     *
     * <p>注意 1「已登记」对医生来说不是"已登记"，而是"钱交了、还没去检查科"。
     */
    private String inspectionText(Integer status) {
        if (status == null) {
            return "已缴费待执行";
        }
        InsRecordStatusEnum e = InsRecordStatusEnum.getByCode(status);
        if (e == null) {
            return InsRecordStatusEnum.labelOrUnknown(status);
        }
        return switch (e) {
            case REGISTERED -> "已缴费待执行";
            case SIGNED_IN -> "已到检";
            case CHECKING -> "检查中";
            case RESULTED -> "已出结果";
            case REVIEWED -> "已审核";
            case PUBLISHED -> "已发布";
            case CANCELLED -> "已取消";
        };
    }

    /**
     * 检验记录状态 → 医生站文案。
     *
     * <p>检验的码表与检查**不是同一套**：这里 2 = 已采样、3 = 已接收，
     * 而检查的 2 = 已签到 —— 所以文案必须在医技模块算，不能由调用方按码值猜。
     */
    private String laboratoryText(Integer status) {
        if (status == null) {
            return "已缴费待执行";
        }
        LabRecordStatusEnum e = LabRecordStatusEnum.getByCode(status);
        if (e == null) {
            return LabRecordStatusEnum.labelOrUnknown(status);
        }
        return switch (e) {
            case REGISTERED -> "已缴费待执行";
            case SAMPLED -> "已采样";
            case RECEIVED -> "已接收";
            case TESTING -> "检测中";
            case RESULTED -> "已出结果";
            case REVIEWED -> "已审核";
            case RELEASED -> "已发布";
            case CANCELLED -> "已取消";
        };
    }
}
