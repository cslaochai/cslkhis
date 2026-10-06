package com.his.emr.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.enums.AdverseAcquiredEnum;
import com.his.common.enums.DelFlagEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TimeUtil;
import com.his.emr.dto.AdverseEventActionDTO;
import com.his.emr.dto.AdverseEventQueryPageDTO;
import com.his.emr.dto.AdverseEventUpsertDTO;
import com.his.emr.entity.BizAdverseEvent;
import com.his.emr.enums.AdverseEventStatusEnum;
import com.his.emr.enums.AdverseEventTypeEnum;
import com.his.emr.mapper.BizAdverseEventMapper;
import com.his.emr.service.AdverseEventService;
import com.his.emr.vo.AdverseEventStatsVO;
import com.his.emr.vo.AdverseEventVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 不良事件服务实现
 * <p>
 * 状态机：1 已上报待处理 → 2 处理中 → 3 已整改 → 4 已结案（不可逆）。
 * 每一步流转都留「操作人 id + 姓名 + 意见 + 时间」，这是三甲评审要看的痕迹链。
 */
@Service
@RequiredArgsConstructor
public class AdverseEventServiceImpl implements AdverseEventService {

    private final BizAdverseEventMapper eventMapper;
    private final RedisSequenceService sequenceService;

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static Long asLong(Object v) {
        if (v == null) {
            return 0L;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(v.toString());
    }

    @Override
    public PageResult<AdverseEventVO> page(AdverseEventQueryPageDTO q) {
        Page<AdverseEventVO> page = eventMapper.selectEventPage(
                new Page<>(q.getPageNum(), q.getPageSize()),
                q.getEventNo(), q.getEventType(), q.getEventLevel(), q.getStatus(),
                q.getOccurDeptId(), q.getKeyword(), q.getDateStart(), q.getDateEnd());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public AdverseEventVO getDetailById(Long id) {
        AdverseEventVO vo = eventMapper.selectEventById(id);
        if (vo == null) {
            throw new BusinessException("不良事件不存在或已删除");
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(AdverseEventUpsertDTO dto) {
        // 患者名快照：给了 patientId 就以库里的姓名为准，不信任前端（拼错患者名是低级事故）
        String patientName = null;
        if (dto.getPatientId() != null) {
            patientName = eventMapper.selectPatientName(dto.getPatientId());
            if (patientName == null) {
                throw new BusinessException("关联患者不存在：" + dto.getPatientId());
            }
        }
        String deptName = eventMapper.selectDeptName(dto.getOccurDeptId());
        if (deptName == null) {
            throw new BusinessException("发生科室不存在：" + dto.getOccurDeptId());
        }
        LocalDateTime occurTime = dto.getOccurTime().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        if (occurTime.isAfter(LocalDateTime.now())) {
            throw new BusinessException("发生时间不能晚于当前时间");
        }
        // 病区快照：护理质控的千床日率要把分子落到「事发当时所在病区」，只到科室算不出该病区该月的数
        // （sql/168 口径 b/c）。查不到=门诊或非患者事件，留空，本来就不该进病区统计。
        Long occurWardId = dto.getPatientId() == null ? null
                : eventMapper.selectWardIdAtTime(dto.getPatientId(), occurTime);
        String occurWardName = occurWardId == null ? null : eventMapper.selectWardName(occurWardId);
        // 只有压疮区分「院内获得 / 入院带入」（带入的皮肤问题不是本院造成的），其余类型服务端强制 1
        boolean admittedWith = dto.getEventType() == AdverseEventTypeEnum.PRESSURE_INJURY.getCode()
                && dto.getAcquiredFlag() != null
                && dto.getAcquiredFlag() == AdverseAcquiredEnum.ADMITTED_WITH.getCode();
        int acquiredFlag = admittedWith ? AdverseAcquiredEnum.ADMITTED_WITH.getCode()
                : AdverseAcquiredEnum.HOSPITAL_ACQUIRED.getCode();

        Long operatorId = UserUtils.getCurrentUser().getEmployeeId();
        String operatorName = UserUtils.getCurrentUser().getRealName();

        if (dto.getId() == null) {
            BizAdverseEvent e = new BizAdverseEvent();
            e.setEventNo(nextEventNo());
            e.setEventType(dto.getEventType());
            e.setEventLevel(dto.getEventLevel());
            e.setOccurDeptId(dto.getOccurDeptId());
            e.setOccurDeptName(deptName);
            e.setOccurWardId(occurWardId);
            e.setOccurWardName(occurWardName);
            e.setAcquiredFlag(acquiredFlag);
            e.setOccurTime(occurTime);
            e.setPatientId(dto.getPatientId());
            e.setPatientName(patientName);
            e.setVisitId(dto.getVisitId());
            e.setTitle(dto.getTitle().trim());
            e.setDescription(dto.getDescription().trim());
            e.setImmediateAction(trimToNull(dto.getImmediateAction()));
            e.setReporterId(operatorId);
            e.setReporterName(operatorName);
            e.setReportTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
            e.setStatus(AdverseEventStatusEnum.REPORTED.getCode());
            e.setDelFlag(DelFlagEnum.NORMAL.getCode());
            e.setCreateTime(e.getReportTime());
            e.setUpdateTime(e.getReportTime());
            if (eventMapper.insert(e) != 1) {
                throw new BusinessException("上报失败");
            }
            return e.getId();
        }

        // 修改：仅待处理状态且本人上报（状态≥2 已经进入处理留痕，改上报内容会破坏痕迹链）
        BizAdverseEvent exist = eventMapper.selectById(dto.getId());
        if (exist == null || exist.getDelFlag() != 0) {
            throw new BusinessException("不良事件不存在或已删除");
        }
        if (exist.getStatus() != null && exist.getStatus() != AdverseEventStatusEnum.REPORTED.getCode()) {
            throw new BusinessException("事件已进入处理流程（状态=" + exist.getStatus() + "），不能再修改上报内容");
        }
        if (!operatorId.equals(exist.getReporterId())) {
            throw new BusinessException("只有上报人本人可以修改上报内容");
        }
        exist.setEventType(dto.getEventType());
        exist.setEventLevel(dto.getEventLevel());
        exist.setOccurDeptId(dto.getOccurDeptId());
        exist.setOccurDeptName(deptName);
        exist.setOccurWardId(occurWardId);
        exist.setOccurWardName(occurWardName);
        exist.setAcquiredFlag(acquiredFlag);
        exist.setOccurTime(occurTime);
        exist.setPatientId(dto.getPatientId());
        exist.setPatientName(patientName);
        exist.setVisitId(dto.getVisitId());
        exist.setTitle(dto.getTitle().trim());
        exist.setDescription(dto.getDescription().trim());
        exist.setImmediateAction(trimToNull(dto.getImmediateAction()));
        exist.setUpdateTime(LocalDateTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS));
        if (eventMapper.updateById(exist) != 1) {
            throw new BusinessException("修改失败");
        }
        return exist.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(AdverseEventActionDTO dto) {
        BizAdverseEvent e = lockAndCheck(dto.getId(), AdverseEventStatusEnum.REPORTED.getCode(), "处理");
        e.setHandlerId(UserUtils.getCurrentUser().getEmployeeId());
        e.setHandlerName(UserUtils.getCurrentUser().getRealName());
        e.setHandleRemark(dto.getRemark().trim());
        e.setHandleTime(TimeUtil.nowSeconds());
        e.setStatus(AdverseEventStatusEnum.HANDLED.getCode());
        e.setUpdateTime(e.getHandleTime());
        saveStep(e, "处理");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rectify(AdverseEventActionDTO dto) {
        BizAdverseEvent e = lockAndCheck(dto.getId(), AdverseEventStatusEnum.HANDLED.getCode(), "整改");
        e.setRectifyById(UserUtils.getCurrentUser().getEmployeeId());
        e.setRectifyByName(UserUtils.getCurrentUser().getRealName());
        e.setRectifyMeasures(dto.getRemark().trim());
        e.setRectifyTime(TimeUtil.nowSeconds());
        e.setStatus(AdverseEventStatusEnum.RECTIFIED.getCode());
        e.setUpdateTime(e.getRectifyTime());
        saveStep(e, "整改");
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(AdverseEventActionDTO dto) {
        BizAdverseEvent e = lockAndCheck(dto.getId(), AdverseEventStatusEnum.RECTIFIED.getCode(), "结案");
        e.setCloseById(UserUtils.getCurrentUser().getEmployeeId());
        e.setCloseByName(UserUtils.getCurrentUser().getRealName());
        e.setVerifyRemark(dto.getRemark().trim());
        e.setCloseTime(TimeUtil.nowSeconds());
        e.setStatus(AdverseEventStatusEnum.CLOSED.getCode());
        e.setUpdateTime(e.getCloseTime());
        saveStep(e, "结案");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizAdverseEvent e = eventMapper.selectById(id);
        if (e == null || e.getDelFlag() != 0) {
            throw new BusinessException("不良事件不存在或已删除");
        }
        if (e.getStatus() != null && e.getStatus() != AdverseEventStatusEnum.REPORTED.getCode()) {
            throw new BusinessException("事件已进入处理流程，不能删除（留痕完整性要求）");
        }
        // Objects.equals 而不是 .equals()：employeeId 可能为 null（管理账号无员工档），
        // 直接 .equals 会 NPE 变成 500，看起来像服务端坏了
        if (!Objects.equals(UserUtils.getCurrentUser().getEmployeeId(), e.getReporterId())) {
            throw new BusinessException("只有上报人本人可以删除");
        }
        // ⚠ MP 全局 logic-delete-field=delFlag：updateById 不允许 set del_flag（静默跳过），
        // 软删必须走 MP 的 deleteById → UPDATE ... SET del_flag=1 WHERE id=? AND del_flag=0
        if (eventMapper.deleteById(id) != 1) {
            throw new BusinessException("删除失败");
        }
    }

    @Override
    public AdverseEventStatsVO monthStats() {
        Map<String, Object> raw = eventMapper.selectMonthStats();
        AdverseEventStatsVO vo = new AdverseEventStatsVO();
        vo.setMonthReported(asLong(raw.get("monthReported")));
        vo.setPending(asLong(raw.get("pending")));
        vo.setSentinel(asLong(raw.get("sentinel")));
        vo.setClosed(asLong(raw.get("closed")));
        return vo;
    }

    /**
     * 取单 + 加行锁 + 前置态校验（流转的并发闸门：不锁行可能同一步被两人各执行一次）
     */
    private BizAdverseEvent lockAndCheck(Long id, int expectedStatus, String action) {
        BizAdverseEvent e = eventMapper.selectByIdForUpdate(id);
        if (e == null || e.getDelFlag() != 0) {
            throw new BusinessException("不良事件不存在或已删除");
        }
        if (e.getStatus() == null || e.getStatus() != expectedStatus) {
            throw new BusinessException("当前状态不允许" + action
                    + "（期望状态=" + expectedStatus + "，实际状态=" + e.getStatus() + "）");
        }
        return e;
    }

    private void saveStep(BizAdverseEvent e, String action) {
        if (eventMapper.updateById(e) != 1) {
            throw new BusinessException(action + "失败");
        }
    }

    private String nextEventNo() {
        return sequenceService.generateAdverseEventNo();
    }
}
