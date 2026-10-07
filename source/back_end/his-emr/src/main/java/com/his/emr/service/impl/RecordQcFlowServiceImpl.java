package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TimeUtil;
import com.his.emr.dto.*;
import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.entity.BizRecordQcFlow;
import com.his.emr.entity.BizRecordQcFlowAction;
import com.his.emr.enums.QcRecordSourceEnum;
import com.his.emr.enums.RecordQcActionEnum;
import com.his.emr.enums.RecordQcFlowStatusEnum;
import com.his.emr.enums.RecordQcLevelEnum;
import com.his.emr.mapper.BizMedicalRecordMapper;
import com.his.emr.mapper.BizRecordQcFlowActionMapper;
import com.his.emr.mapper.BizRecordQcFlowMapper;
import com.his.emr.service.RecordQcFlowService;
import com.his.emr.vo.RecordQcFlowActionVO;
import com.his.emr.vo.RecordQcFlowVO;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 病历三级质控流转服务实现
 * <p>
 * 状态机（收口在本类，Controller 不碰状态）：
 * 1 科级待审 →（approve）→ 2 病案室待审 →（approve）→ 3 医务处待审 →（finalApprove）→ 4 终审通过；
 * 1/2/3 任一级（returnForRework）→ 5 整改中，current_level 保持为退回发生级；
 * 5（resubmit）→ 回到 current_level 对应待审状态。
 * 所有动作写质控流转动作时间线时间线，只增不改。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecordQcFlowServiceImpl extends ServiceImpl<BizRecordQcFlowMapper, BizRecordQcFlow> implements RecordQcFlowService {
    private final BizRecordQcFlowMapper bizRecordQcFlowMapper;
    private final BizRecordQcFlowActionMapper bizRecordQcFlowActionMapper;
    private final BizMedicalRecordMapper bizMedicalRecordMapper;
    private final RedisSequenceService redisSequenceService;
    private DictCacheService dictCacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecordQcFlowVO start(RecordQcFlowStartDTO dto) {
        BizMedicalRecord record = bizMedicalRecordMapper.selectById(dto.getRecordId());
        if (record == null || (record.getDelFlag() != null && record.getDelFlag() == 1)) {
            throw new BusinessException("病历不存在或已删除");
        }
        if (record.getRecordStatus() != null && record.getRecordStatus() == 4) {
            throw new BusinessException("病历已作废，不能发起质控流转");
        }
        if (bizRecordQcFlowMapper.countActiveByRecordId(dto.getRecordId()) > 0) {
            throw new BusinessException("该病历已有在途质控流转（未终审通过），不能重复发起");
        }

        BizRecordQcFlow flow = new BizRecordQcFlow();
        flow.setFlowNo(nextFlowNo());
        flow.setRecordId(record.getId());
        flow.setRecordSource(record.getRegistId() == null ? "INPATIENT" : "OUTPATIENT");
        flow.setPatientId(record.getPatientId());
        flow.setPatientName(record.getPatientName());
        flow.setDeptId(record.getDeptId());
        flow.setDeptName(record.getDeptName());
        flow.setFlowStatus(RecordQcFlowStatusEnum.DEPT_PENDING.getCode());
        flow.setCurrentLevel(RecordQcLevelEnum.DEPT.getCode());
        flow.setCreateBy(UserUtils.getCurrentUser().getRealName());
        flow.setRemark(dto.getRemark());
        bizRecordQcFlowMapper.insert(flow);

        insertAction(flow.getId(), RecordQcLevelEnum.DEPT.getCode(), RecordQcActionEnum.START.getCode(), "发起三级质控流转",
                null, null);
        return decorate(bizRecordQcFlowMapper.selectFlowById(flow.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(RecordQcFlowOpinionDTO dto) {
        BizRecordQcFlow flow = lockAndCheck(dto.getFlowId());
        requireReviewStage(flow);

        int nextStatus;
        int nextLevel;
        if (flow.getCurrentLevel() == RecordQcLevelEnum.DEPT.getCode()) {
            nextStatus = RecordQcFlowStatusEnum.ARCHIVE_PENDING.getCode();
            nextLevel = RecordQcLevelEnum.ARCHIVE.getCode();
        } else if (flow.getCurrentLevel() == RecordQcLevelEnum.ARCHIVE.getCode()) {
            nextStatus = RecordQcFlowStatusEnum.MEDAFFAIRS_PENDING.getCode();
            nextLevel = RecordQcLevelEnum.MEDAFFAIRS.getCode();
        } else {
            throw new BusinessException("医务处级请走终审（定级），不能普通通过");
        }
        flow.setFlowStatus(nextStatus);
        flow.setCurrentLevel(nextLevel);
        flow.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        bizRecordQcFlowMapper.updateById(flow);

        insertAction(flow.getId(), levelOfStatus(flow.getFlowStatus()), RecordQcActionEnum.APPROVE.getCode(),
                dto.getOpinion(), null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnForRework(RecordQcFlowReturnDTO dto) {
        BizRecordQcFlow flow = lockAndCheck(dto.getFlowId());
        requireReviewStage(flow);

        flow.setFlowStatus(RecordQcFlowStatusEnum.REWORKING.getCode());
        // current_level 保持为退回发生级 = 整改后需回到的级
        flow.setReturnLevel(flow.getCurrentLevel());
        flow.setReturnReason(dto.getDefectDetail());
        flow.setReturnRequirement(dto.getRequirement());
        flow.setReturnDeadline(dto.getReturnDeadline());
        flow.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        bizRecordQcFlowMapper.updateById(flow);

        insertAction(flow.getId(), flow.getReturnLevel(), RecordQcActionEnum.RETURN.getCode(),
                dto.getOpinion(), dto.getDefectDetail(), dto.getRequirement());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resubmit(RecordQcFlowOpinionDTO dto) {
        BizRecordQcFlow flow = lockAndCheck(dto.getFlowId());
        if (flow.getFlowStatus() != RecordQcFlowStatusEnum.REWORKING.getCode()) {
            throw new BusinessException("当前状态（" + RecordQcFlowStatusEnum.labelOrUnknown(flow.getFlowStatus())
                    + "）不是整改中，无需整改提交");
        }

        flow.setFlowStatus(statusOfLevel(flow.getReturnLevel() == null ? RecordQcLevelEnum.DEPT.getCode() : flow.getReturnLevel()));
        flow.setCurrentLevel(flow.getReturnLevel() == null ? RecordQcLevelEnum.DEPT.getCode() : flow.getReturnLevel());
        flow.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        bizRecordQcFlowMapper.updateById(flow);

        insertAction(flow.getId(), RecordQcLevelEnum.DEPT.getCode(), RecordQcActionEnum.RESUBMIT.getCode(), dto.getOpinion(), null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finalApprove(RecordQcFlowFinalDTO dto) {
        BizRecordQcFlow flow = lockAndCheck(dto.getFlowId());
        requireReviewStage(flow);
        if (flow.getCurrentLevel() != RecordQcLevelEnum.MEDAFFAIRS.getCode()) {
            throw new BusinessException("当前停留级（" + RecordQcLevelEnum.labelOrUnknown(flow.getCurrentLevel())
                    + "）不是医务处，不能终审");
        }

        flow.setFlowStatus(RecordQcFlowStatusEnum.FINAL_APPROVED.getCode());
        flow.setGrade(dto.getGrade());
        flow.setFinalScore(dto.getFinalScore());
        flow.setFinalOpinion(dto.getFinalOpinion());
        flow.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        bizRecordQcFlowMapper.updateById(flow);

        insertAction(flow.getId(), RecordQcLevelEnum.MEDAFFAIRS.getCode(), RecordQcActionEnum.FINAL.getCode(),
                dto.getFinalOpinion(), null, null);
    }

    @Override
    public PageResult<RecordQcFlowVO> page(RecordQcFlowQueryPageDTO q) {
        Page<RecordQcFlowVO> page = bizRecordQcFlowMapper.selectFlowPage(
                new Page<>(q.getPageNum(), q.getPageSize()),
                q.getFlowNo(), q.getFlowStatus(), q.getCurrentLevel(), q.getRecordSource(), q.getKeyword());
        page.getRecords().forEach(this::decorate);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public RecordQcFlowVO getDetailById(Long id) {
        RecordQcFlowVO vo = bizRecordQcFlowMapper.selectFlowById(id);
        if (vo == null) {
            throw new BusinessException("流转单不存在或已删除");
        }
        return decorate(vo);
    }

    @Override
    public List<RecordQcFlowActionVO> listActions(Long flowId) {
        List<BizRecordQcFlowAction> list = bizRecordQcFlowActionMapper.selectList(
                new LambdaQueryWrapper<BizRecordQcFlowAction>()
                        .eq(BizRecordQcFlowAction::getFlowId, flowId)
                        .eq(BizRecordQcFlowAction::getDelFlag, 0)
                        .orderByAsc(BizRecordQcFlowAction::getActionTime)
                        .orderByAsc(BizRecordQcFlowAction::getId));
        return list.stream().map(this::toActionVO).toList();
    }

    // 私有

    private String nextFlowNo() {
        return redisSequenceService.generateRecordQcFlowNo();
    }

    private void insertAction(Long flowId, int level, int action,
                              String opinion, String defectDetail, String requirement) {
        BizRecordQcFlowAction a = new BizRecordQcFlowAction();
        a.setFlowId(flowId);
        a.setLevel(level);
        a.setAction(action);
        a.setOpinion(opinion);
        a.setDefectDetail(defectDetail);
        a.setRequirement(requirement);
        a.setOperatorId(UserUtils.getCurrentUser().getEmployeeId());
        a.setOperatorName(UserUtils.getCurrentUser().getRealName());
        a.setActionTime(TimeUtil.nowSeconds());
        bizRecordQcFlowActionMapper.insert(a);
    }

    /**
     * 取流转单（行锁）并校验存在
     */
    private BizRecordQcFlow lockAndCheck(Long flowId) {
        BizRecordQcFlow flow = bizRecordQcFlowMapper.selectByIdForUpdate(flowId);
        if (flow == null) {
            throw new BusinessException("流转单不存在或已删除");
        }
        if (flow.getFlowStatus() == RecordQcFlowStatusEnum.FINAL_APPROVED.getCode()) {
            throw new BusinessException("流转已终审通过（终态），不能再操作");
        }
        return flow;
    }

    /**
     * 校验处于三个待审级之一
     */
    private void requireReviewStage(BizRecordQcFlow flow) {
        int s = flow.getFlowStatus();
        if (s != RecordQcFlowStatusEnum.DEPT_PENDING.getCode() && s != RecordQcFlowStatusEnum.ARCHIVE_PENDING.getCode() && s != RecordQcFlowStatusEnum.MEDAFFAIRS_PENDING.getCode()) {
            throw new BusinessException("当前状态（" + RecordQcFlowStatusEnum.labelOrUnknown(s) + "）不允许该操作");
        }
    }

    private int statusOfLevel(int level) {
        if (level == RecordQcLevelEnum.DEPT.getCode()) {
            return RecordQcFlowStatusEnum.DEPT_PENDING.getCode();
        }
        if (level == RecordQcLevelEnum.ARCHIVE.getCode()) {
            return RecordQcFlowStatusEnum.ARCHIVE_PENDING.getCode();
        }
        if (level == RecordQcLevelEnum.MEDAFFAIRS.getCode()) {
            return RecordQcFlowStatusEnum.MEDAFFAIRS_PENDING.getCode();
        }
        throw new BusinessException("未知级别 " + level);
    }

    private int levelOfStatus(int status) {
        if (status == RecordQcFlowStatusEnum.DEPT_PENDING.getCode()) {
            return RecordQcLevelEnum.DEPT.getCode();
        }
        if (status == RecordQcFlowStatusEnum.ARCHIVE_PENDING.getCode()) {
            return RecordQcLevelEnum.ARCHIVE.getCode();
        }
        if (status == RecordQcFlowStatusEnum.MEDAFFAIRS_PENDING.getCode()) {
            return RecordQcLevelEnum.MEDAFFAIRS.getCode();
        }
        throw new BusinessException("未知状态 " + status);
    }

    private RecordQcFlowActionVO toActionVO(BizRecordQcFlowAction a) {
        RecordQcFlowActionVO vo = new RecordQcFlowActionVO();
        vo.setId(a.getId());
        vo.setFlowId(a.getFlowId());
        vo.setLevel(a.getLevel());
        vo.setAction(a.getAction());
        vo.setOpinion(a.getOpinion());
        vo.setDefectDetail(a.getDefectDetail());
        vo.setRequirement(a.getRequirement());
        vo.setOperatorId(a.getOperatorId());
        vo.setOperatorName(a.getOperatorName());
        vo.setActionTime(a.getActionTime());
        vo.setLevelText(RecordQcLevelEnum.getText(a.getLevel()));
        vo.setActionText(RecordQcActionEnum.getText(a.getAction()));
        return vo;
    }

    private RecordQcFlowVO decorate(RecordQcFlowVO vo) {
        if (vo == null) {
            return null;
        }
        vo.setFlowStatusText(RecordQcFlowStatusEnum.getText(vo.getFlowStatus()));
        vo.setCurrentLevelText(RecordQcLevelEnum.getText(vo.getCurrentLevel()));
        vo.setReturnLevelText(RecordQcLevelEnum.getText(vo.getReturnLevel()));
        vo.setGradeText(dictCacheService.getDicDataLabel("biz_emr_qcGradeEnum", vo.getGrade()));
        vo.setRecordSourceText(QcRecordSourceEnum.getText(vo.getRecordSource()));
        return vo;
    }
}