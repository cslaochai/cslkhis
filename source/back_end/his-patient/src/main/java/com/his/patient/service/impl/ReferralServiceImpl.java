package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.exception.BusinessException;
import com.his.patient.dto.ReferralDTO;
import com.his.patient.entity.BizReferral;
import com.his.patient.enums.ReferralDirectionEnum;
import com.his.patient.enums.ReferralStatusEnum;
import com.his.patient.mapper.BizReferralMapper;
import com.his.patient.service.ReferralService;
import com.his.system.service.DictCacheService;
import com.his.patient.vo.ReferralVO;
import com.his.security.UserUtils;
import com.his.system.entity.SysConfig;
import com.his.system.entity.SysMessage;
import com.his.system.enums.BizTypeEnum;
import com.his.system.mapper.SysConfigMapper;
import com.his.system.service.DutyRosterService;
import com.his.system.service.SysMessageService;
import com.his.system.vo.DutyOfficerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 双向转诊服务。
 *
 * <p>状态机：0 待确认 → 1 已确认 → 2 已完成；0/1 → 3 已取消。2 已完成是终态，任何回退都算改史实，不提供。
 * direction：1-上转（转往上级医院，to_hospital 必填）2-下转（转回基层/社区，to_hospital 必填）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralServiceImpl implements ReferralService {

    private static final String DICT_DIRECTION = "his_referral_direction";
    private static final String DICT_STATUS = "his_referral_status";
    /**
     * 待确认多久就找总值班（系统参数：duty.coord.referral_pending_hours，缺失/非法回落 2 小时）
     */
    private static final String DUTY_REFERRAL_HOURS_KEY = "duty.coord.referral_pending_hours";
    private static final int DUTY_REFERRAL_HOURS_FALLBACK = 2;
    private final BizReferralMapper referralMapper;
    private final DictCacheService dictText;
    /**
     * 全院当天谁负责：转诊挂住没人接时的兜底收口人（sql/169）
     */
    private final DutyRosterService dutyRosterService;
    private final SysMessageService sysMessageService;
    private final SysConfigMapper sysConfigMapper;

    @Transactional(rollbackFor = Exception.class)
    public ReferralVO create(ReferralDTO.Create dto) {
        int direction = dto.getDirection() == null ? ReferralDirectionEnum.UP.getCode() : dto.getDirection();
        if (!StringUtils.hasText(dto.getToHospital()) && dto.getToDeptId() == null) {
            throw new BusinessException("院际转诊必须填转入医院，院内转诊必须选转入科室");
        }
        BizReferral r = new BizReferral();
        r.setReferralNo(nextReferralNo());
        r.setPatientId(dto.getPatientId());
        r.setVisitId(dto.getVisitId());
        r.setAdmissionId(dto.getAdmissionId());
        r.setFromDeptId(dto.getFromDeptId());
        r.setToDeptId(dto.getToDeptId());
        r.setToHospital(tr(dto.getToHospital()));
        r.setReason(dto.getReason().trim());
        r.setDirection(direction);
        r.setDiagnosis(dto.getDiagnosis());
        r.setContactPhone(dto.getContactPhone());
        r.setReferralStatus(ReferralStatusEnum.PENDING.getCode());
        r.setReferralTime(toSeconds(LocalDateTime.now()));
        r.setRemark(dto.getRemark());
        r.setCreateBy(UserUtils.getCurrentEmployeeName());
        referralMapper.insert(r);
        // 转诊是跨院动作，协调人是总值班而不是开单科室：登记即让他知道，别等患者家属来问
        notifyDutyOnCreate(r);
        return toVo(r, loadDeptNames());
    }

    public IPage<ReferralVO> listPage(ReferralDTO.QueryPage q) {
        LambdaQueryWrapper<BizReferral> w = new LambdaQueryWrapper<BizReferral>()
                .eq(q.getPatientId() != null, BizReferral::getPatientId, q.getPatientId())
                .eq(q.getDirection() != null, BizReferral::getDirection, q.getDirection())
                .eq(q.getReferralStatus() != null, BizReferral::getReferralStatus, q.getReferralStatus())
                .like(StringUtils.hasText(q.getToHospital()), BizReferral::getToHospital, tr(q.getToHospital()))
                .orderByDesc(BizReferral::getReferralTime)
                .orderByDesc(BizReferral::getReferralId);
        IPage<BizReferral> page = referralMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);
        Map<Long, String> deptNames = loadDeptNames();
        return page.convert(r -> toVo(r, deptNames));
    }

    public ReferralVO getDetailById(Long referralId) {
        BizReferral r = requireReferral(referralId);
        return toVo(r, loadDeptNames());
    }

    @Transactional(rollbackFor = Exception.class)
    public ReferralVO audit(ReferralDTO.Audit dto) {
        BizReferral r = requireReferral(dto.getReferralId());
        if (!Objects.equals(r.getReferralStatus(), ReferralStatusEnum.PENDING.getCode())) {
            throw new BusinessException("只有待确认的转诊单可以确认（当前：" + statusText(r) + "）");
        }
        r.setReferralStatus(ReferralStatusEnum.CONFIRMED.getCode());
        if (dto.getToDeptId() != null) {
            r.setToDeptId(dto.getToDeptId());
        }
        r.setAuditBy(UserUtils.getCurrentEmployeeId());
        r.setAuditName(UserUtils.getCurrentEmployeeName());
        r.setAuditTime(toSeconds(LocalDateTime.now()));
        r.setAuditRemark(dto.getAuditRemark());
        r.setUpdateBy(UserUtils.getCurrentEmployeeName());
        r.setUpdateTime(r.getAuditTime());
        referralMapper.updateById(r);
        return toVo(r, loadDeptNames());
    }

    @Transactional(rollbackFor = Exception.class)
    public ReferralVO finish(ReferralDTO.Finish dto) {
        BizReferral r = requireReferral(dto.getReferralId());
        if (!Objects.equals(r.getReferralStatus(), ReferralStatusEnum.CONFIRMED.getCode())) {
            throw new BusinessException("只有已确认的转诊单可以完成（当前：" + statusText(r) + "）");
        }
        r.setReferralStatus(ReferralStatusEnum.FINISHED.getCode());
        r.setFinishTime(toSeconds(LocalDateTime.now()));
        r.setFinishRemark(dto.getFinishRemark());
        r.setUpdateBy(UserUtils.getCurrentEmployeeName());
        r.setUpdateTime(r.getFinishTime());
        referralMapper.updateById(r);
        return toVo(r, loadDeptNames());
    }

    @Transactional(rollbackFor = Exception.class)
    public ReferralVO cancel(ReferralDTO.Cancel dto) {
        BizReferral r = requireReferral(dto.getReferralId());
        if (Objects.equals(r.getReferralStatus(), ReferralStatusEnum.FINISHED.getCode())) {
            throw new BusinessException("已完成的转诊单不能取消");
        }
        if (Objects.equals(r.getReferralStatus(), ReferralStatusEnum.CANCELLED.getCode())) {
            throw new BusinessException("该转诊单已取消");
        }
        r.setReferralStatus(ReferralStatusEnum.CANCELLED.getCode());
        r.setRemark((r.getRemark() == null ? "" : r.getRemark() + "；") + "取消原因：" + dto.getCancelReason().trim());
        r.setUpdateBy(UserUtils.getCurrentEmployeeName());
        r.setUpdateTime(toSeconds(LocalDateTime.now()));
        referralMapper.updateById(r);
        return toVo(r, loadDeptNames());
    }

    // 总值班协调（sql/169）：转诊是**跨院**动作，落点是医院外的机构，
    // 任何一个科室都联系不动外院 —— 只有总值班（医务科/院办值班）能拍板联系与安排转运。

    /**
     * 转诊单登记即通知当日总值班协调。
     *
     * <p>为什么不只靠患者/开单医生自己联系：上转要联系上级医院的接诊科室并安排转运车辆，
     * 下转要联系社区卫生中心确认接收 —— 这两件事都不在开单医生的能力范围内，
     * 过去全靠打电话，打没打、谁接的，事后无从查证。落在待办里，链条才完整。
     */
    private void notifyDutyOnCreate(BizReferral r) {
        DutyOfficerVO duty = dutyRosterService.current();
        if (duty == null || duty.getEmployeeId() == null) {
            log.warn("[双向转诊] {} 已登记但当日总值班未排班，转诊协调无人接手", r.getReferralNo());
            return;
        }
        String dirText = r.getDirection() != null && r.getDirection() == ReferralDirectionEnum.DOWN.getCode() ? "下转" : "上转";
        String title = "转诊协调（" + dirText + "）：" + r.getReferralNo();
        String content = String.format(
                "转诊单 %s（%s，转入 %s）已登记，请总值班协调：%s。"
                        + "患者诊断：%s；联系电话：%s。上转请确认上级医院接诊科室与转运安排，下转请确认基层机构接收。",
                r.getReferralNo(), dirText,
                StringUtils.hasText(r.getToHospital()) ? r.getToHospital() : "院内",
                r.getReason(),
                StringUtils.hasText(r.getDiagnosis()) ? r.getDiagnosis() : "未填",
                StringUtils.hasText(r.getContactPhone()) ? r.getContactPhone() : "未填");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("referralNo", r.getReferralNo());
        payload.put("direction", dirText);
        payload.put("toHospital", r.getToHospital());
        payload.put("diagnosis", r.getDiagnosis());
        payload.put("contactPhone", r.getContactPhone());
        sysMessageService.sendSystemMessage(duty.getEmployeeId(), duty.getEmployeeName(), title, content,
                BizTypeEnum.DUTY_COORD.getType(), r.getReferralId(),
                "warning", cn.hutool.json.JSONUtil.toJsonStr(payload), 0);
    }

    /**
     * 待确认超时 → 催当日总值班（定时 + 手工补跑）。
     *
     * <p>判重按时间窗（默认 2 小时一次）而不是「一人一次」：转诊单挂住没人确认，
     * 催一次就永久静默等于放弃这个患者；按阈值反复催，直到被确认（1）或取消（3）为止。
     *
     * @return 本轮发出的待办条数
     */
    public int escalatePendingToDuty() {
        int hours = dutyReferralHours();
        LocalDateTime deadLine = LocalDateTime.now().minusHours(hours);
        List<BizReferral> pending = referralMapper.selectList(new LambdaQueryWrapper<BizReferral>()
                .eq(BizReferral::getReferralStatus, ReferralStatusEnum.PENDING.getCode())
                .lt(BizReferral::getReferralTime, deadLine)
                .orderByAsc(BizReferral::getReferralTime));
        if (pending.isEmpty()) {
            return 0;
        }
        DutyOfficerVO duty = dutyRosterService.current();
        if (duty == null || duty.getEmployeeId() == null) {
            log.warn("[双向转诊] {} 条转诊单待确认已超过 {} 小时，但当日总值班未排班，无法催办", pending.size(), hours);
            return 0;
        }
        int sent = 0;
        for (BizReferral r : pending) {
            try {
                if (sentWithinHours(r.getReferralId(), duty.getEmployeeId(), hours)) {
                    continue;
                }
                long waited = r.getReferralTime() == null ? 0
                        : java.time.Duration.between(r.getReferralTime(), LocalDateTime.now()).toHours();
                String dirText = r.getDirection() != null && r.getDirection() == ReferralDirectionEnum.DOWN.getCode() ? "下转" : "上转";
                String title = "转诊待确认超时：" + r.getReferralNo() + "（" + dirText + "，已挂 " + waited + " 小时）";
                String content = String.format(
                        "转诊单 %s（%s，转入 %s）登记后 %d 小时仍为「待确认」（阈值 %d 小时）。"
                                + "请总值班推进：联系转入方确认接收，或说明无法转诊的原因并退回开单科室。",
                        r.getReferralNo(), dirText,
                        StringUtils.hasText(r.getToHospital()) ? r.getToHospital() : "院内",
                        waited, hours);
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("referralNo", r.getReferralNo());
                payload.put("direction", dirText);
                payload.put("toHospital", r.getToHospital());
                payload.put("waitedHours", waited);
                if (sysMessageService.sendSystemMessage(duty.getEmployeeId(), duty.getEmployeeName(), title, content,
                        BizTypeEnum.DUTY_COORD.getType(), r.getReferralId(),
                        "urgent", cn.hutool.json.JSONUtil.toJsonStr(payload), 0)) {
                    sent++;
                }
            } catch (Exception ex) {
                log.error("[双向转诊] {} 催办失败：{}", r.getReferralNo(), ex.getMessage(), ex);
            }
        }
        if (sent > 0) {
            log.warn("[双向转诊] 本次向总值班发出 {} 条待确认超时待办（阈值 {} 小时）", sent, hours);
        }
        return sent;
    }

    private boolean sentWithinHours(Long referralId, Long receiverId, int hours) {
        try {
            Long cnt = sysMessageService.lambdaQuery()
                    .eq(SysMessage::getBizType, BizTypeEnum.DUTY_COORD.getType())
                    .eq(SysMessage::getBizId, referralId)
                    .eq(SysMessage::getReceiverId, receiverId)
                    // 只跟「催办类（urgent）」判重：登记时那条 warning 是通知不是催办，
                    // 把它算进来会让超时催办永远发不出去（同一 bizId 已被"发过"）。
                    .eq(SysMessage::getSeverity, "urgent")
                    .ge(SysMessage::getSendTime, LocalDateTime.now().minusHours(hours))
                    .count();
            return cnt != null && cnt > 0;
        } catch (Exception ex) {
            log.error("[双向转诊] 判重查询失败，本轮跳过 referralId={}：{}", referralId, ex.getMessage());
            return true;
        }
    }

    private int dutyReferralHours() {
        try {
            SysConfig cfg = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getConfigKey, DUTY_REFERRAL_HOURS_KEY).last("LIMIT 1"));
            if (cfg == null || !StringUtils.hasText(cfg.getConfigValue())) {
                return DUTY_REFERRAL_HOURS_FALLBACK;
            }
            int v = Integer.parseInt(cfg.getConfigValue().trim());
            return v > 0 ? v : DUTY_REFERRAL_HOURS_FALLBACK;
        } catch (Exception ex) {
            return DUTY_REFERRAL_HOURS_FALLBACK;
        }
    }

    private String tr(String s) {
        return s == null ? null : s.trim();
    }

    private LocalDateTime toSeconds(LocalDateTime t) {
        return t.truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
    }

    private BizReferral requireReferral(Long id) {
        BizReferral r = referralMapper.selectById(id);
        if (r == null || (r.getDelFlag() != null && r.getDelFlag() == 1)) {
            throw new BusinessException("转诊单不存在");
        }
        return r;
    }

    private String statusText(BizReferral r) {
        return dictText.getDicDataLabel(DICT_STATUS, r.getReferralStatus());
    }

    /**
     * 单号 REF + yyyyMMddHHmmss + 3 位随机，唯一索引兜底
     */
    private String nextReferralNo() {
        return "REF" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private Map<Long, String> loadDeptNames() {
        return referralMapper.selectDeptMap().stream()
                .filter(m -> m.get("id") != null)
                .collect(Collectors.toMap(m -> ((Number) m.get("id")).longValue(),
                        m -> m.get("deptName") == null ? "" : String.valueOf(m.get("deptName")),
                        (a, b) -> a));
    }

    private ReferralVO toVo(BizReferral r, Map<Long, String> deptNames) {
        ReferralVO vo = new ReferralVO();
        org.springframework.beans.BeanUtils.copyProperties(r, vo);
        vo.setDirectionText(dictText.getDicDataLabel(DICT_DIRECTION, r.getDirection()));
        vo.setReferralStatusText(dictText.getDicDataLabel(DICT_STATUS, r.getReferralStatus()));
        vo.setFromDeptName(r.getFromDeptId() == null ? null : deptNames.get(r.getFromDeptId()));
        vo.setToDeptName(r.getToDeptId() == null ? null : deptNames.get(r.getToDeptId()));
        // 患者快照现查（患者基本信息 / 入院记录属本域，量级单条）
        Map<String, Object> snap = referralMapper.selectPatientSnapshot(r.getPatientId(), r.getAdmissionId());
        if (snap != null) {
            vo.setPatientNo((String) snap.get("patientNo"));
            vo.setPatientName((String) snap.get("patientName"));
            vo.setAdmissionNo((String) snap.get("admissionNo"));
        }
        return vo;
    }
}
