package com.his.emr.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.service.RedisSequenceService;
import com.his.common.enums.DelFlagEnum;
import com.his.common.exception.BusinessException;
import com.his.emr.dto.CodeTaskAssignUpsertDTO;
import com.his.emr.dto.CodeTaskAuditDTO;
import com.his.emr.dto.CodeTaskQueryPageDTO;
import com.his.emr.dto.CodeTaskSubmitDTO;
import com.his.emr.entity.BizArchiveCodeTask;
import com.his.emr.enums.CodeTaskStatusEnum;
import com.his.emr.mapper.BizArchiveCodeTaskMapper;
import com.his.emr.service.ArchiveCodeTaskService;
import com.his.emr.vo.ArchiveCodeTaskStatsVO;
import com.his.emr.vo.ArchiveCodeTaskVO;
import com.his.system.utils.UserUtils;
import com.his.system.enums.BizTypeEnum;
import com.his.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 病案编码任务服务实现
 * <p>
 * 状态机：1 待编码 →（提交）→ 2 已提交 →（审核）→ 3 已完成 / 4 已退修；
 * 已退修可再次提交（→2），每次退修 return_count+1 留痕并站内信提醒编码员。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveCodeTaskServiceImpl implements ArchiveCodeTaskService {

    private final BizArchiveCodeTaskMapper taskMapper;
    private final RedisSequenceService sequenceService;
    private final SysMessageService sysMessageService;

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Override
    public PageResult<ArchiveCodeTaskVO> page(CodeTaskQueryPageDTO q) {
        Page<ArchiveCodeTaskVO> page = taskMapper.selectTaskPage(
                new Page<>(q.getPageNum(), q.getPageSize()),
                q.getTaskNo(), q.getStatus(), q.getCoderId(), q.getKeyword());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public ArchiveCodeTaskVO getDetailById(Long id) {
        ArchiveCodeTaskVO vo = taskMapper.selectTaskById(id);
        if (vo == null) {
            throw new BusinessException("编码任务不存在或已删除");
        }
        return vo;
    }

    @Override
    public ArchiveCodeTaskStatsVO stats() {
        return taskMapper.selectStats();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncTasks() {
        List<Map<String, Object>> unsynced = taskMapper.selectUnsyncedArchives();
        if (unsynced.isEmpty()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        int created = 0;
        for (Map<String, Object> row : unsynced) {
            BizArchiveCodeTask t = new BizArchiveCodeTask();
            t.setTaskNo(sequenceService.generateCodeTaskNo());
            t.setArchiveId(Long.parseLong(row.get("id").toString()));
            t.setRecordNo((String) row.get("record_no"));
            t.setPatientName((String) row.get("patient_name"));
            t.setDeptName((String) row.get("dept_name"));
            Object diag = row.get("diagnosis");
            t.setDiagnosis(diag == null ? null : diag.toString());
            t.setStatus(CodeTaskStatusEnum.PENDING.getCode());
            t.setReturnCount(0);
            t.setDelFlag(DelFlagEnum.NORMAL.getCode());
            t.setCreateTime(now);
            t.setUpdateTime(now);
            if (taskMapper.insert(t) == 1) {
                created++;
            }
        }
        log.info("[编码任务池] 同步完成：待编码归档 {} 份，新建任务 {} 条", unsynced.size(), created);
        return created;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assign(CodeTaskAssignUpsertDTO dto) {
        BizArchiveCodeTask t = taskMapper.selectByIdForUpdate(dto.getId());
        if (t == null || t.getDelFlag() != 0) {
            throw new BusinessException("编码任务不存在或已删除");
        }
        if (t.getStatus() == null || t.getStatus() != CodeTaskStatusEnum.PENDING.getCode()) {
            throw new BusinessException("当前状态不允许分配（期望状态=1，实际状态=" + t.getStatus() + "）");
        }
        String coderName = taskMapper.selectEmployeeName(dto.getCoderId());
        if (coderName == null) {
            throw new BusinessException("编码员不存在或已离职：" + dto.getCoderId());
        }
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        t.setCoderId(dto.getCoderId());
        t.setCoderName(coderName);
        t.setAssignTime(now);
        t.setUpdateTime(now);
        if (taskMapper.updateById(t) != 1) {
            throw new BusinessException("分配失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(CodeTaskSubmitDTO dto) {
        BizArchiveCodeTask t = taskMapper.selectByIdForUpdate(dto.getId());
        if (t == null || t.getDelFlag() != 0) {
            throw new BusinessException("编码任务不存在或已删除");
        }
        if (t.getStatus() == null || (t.getStatus() != CodeTaskStatusEnum.PENDING.getCode() && t.getStatus() != CodeTaskStatusEnum.REWORK.getCode())) {
            throw new BusinessException("当前状态不允许提交编码（期望状态=1 待编码 或 4 已退修，实际状态=" + t.getStatus() + "）");
        }
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        // 未分配的任务提交即认领：编码员自己领活是任务池的正常形态
        if (t.getCoderId() == null) {
            t.setCoderId(UserUtils.getCurrentEmployeeId());
            t.setCoderName(UserUtils.getCurrentEmployeeName());
            t.setAssignTime(now);
        }
        t.setMainIcdCode(dto.getMainIcdCode().trim());
        t.setMainIcdName(dto.getMainIcdName().trim());
        t.setOtherIcdText(trimToNull(dto.getOtherIcdText()));
        t.setSubmitTime(now);
        t.setStatus(CodeTaskStatusEnum.SUBMITTED.getCode());
        t.setUpdateTime(now);
        if (taskMapper.updateById(t) != 1) {
            throw new BusinessException("提交失败");
        }
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(CodeTaskAuditDTO dto) {
        BizArchiveCodeTask t = taskMapper.selectByIdForUpdate(dto.getId());
        if (t == null || t.getDelFlag() != 0) {
            throw new BusinessException("编码任务不存在或已删除");
        }
        if (t.getStatus() == null || t.getStatus() != CodeTaskStatusEnum.SUBMITTED.getCode()) {
            throw new BusinessException("当前状态不允许审核（期望状态=2 已提交，实际状态=" + t.getStatus() + "）");
        }
        boolean approve = Boolean.TRUE.equals(dto.getApprove());
        // B 类保留：条件必填——仅退修时要求审核意见，通过可不填
        if (!approve && (dto.getRemark() == null || dto.getRemark().isBlank())) {
            throw new BusinessException("退修必须填写审核意见");
        }
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        t.setAuditById(UserUtils.getCurrentEmployeeId());
        t.setAuditByName(UserUtils.getCurrentEmployeeName());
        t.setAuditRemark(trimToNull(dto.getRemark()));
        t.setAuditTime(now);
        if (approve) {
            t.setStatus(CodeTaskStatusEnum.DONE.getCode());
        } else {
            t.setStatus(CodeTaskStatusEnum.REWORK.getCode());
            t.setReturnCount((t.getReturnCount() == null ? 0 : t.getReturnCount()) + 1);
        }
        t.setUpdateTime(now);
        if (taskMapper.updateById(t) != 1) {
            throw new BusinessException("审核失败");
        }
        if (!approve) {
            notifyRework(t);
        }
    }

    /**
     * 退修提醒编码员（通知型：整改动作是「改编码重新提交」，无消息内闭环接口）
     */
    private void notifyRework(BizArchiveCodeTask t) {
        if (t.getCoderId() == null) {
            return;
        }
        try {
            String content = String.format(
                    "您提交的编码任务 %s（病历 %s，患者 %s）被退修，累计退修 %d 次。退修意见：%s",
                    t.getTaskNo(),
                    t.getRecordNo(),
                    t.getPatientName() == null ? "未知" : t.getPatientName(),
                    t.getReturnCount(),
                    t.getAuditRemark() == null ? "（无）" : t.getAuditRemark());
            String payload = JSONUtil.toJsonStr(new LinkedHashMap<String, Object>() {{
                put("taskNo", t.getTaskNo());
                put("recordNo", t.getRecordNo());
                put("patientName", t.getPatientName());
                put("returnCount", t.getReturnCount());
            }});
            sysMessageService.sendSystemMessage(t.getCoderId(), t.getCoderName(),
                    "编码任务退修：" + t.getTaskNo(), content,
                    BizTypeEnum.CODE_TASK.getType(), t.getId(), "warning", payload, null);
        } catch (Exception ex) {
            // 提醒失败不影响审核主流程
            log.warn("[编码任务池] 退修提醒发送失败 taskId={} coderId={}", t.getId(), t.getCoderId(), ex);
        }
    }
}
