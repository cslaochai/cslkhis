package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.support.EmpTitleCode;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.medicaltech.dto.CriticalValueHandleDTO;
import com.his.medicaltech.dto.CriticalValueQueryPageDTO;
import com.his.medicaltech.dto.CriticalValueReceiveDTO;
import com.his.medicaltech.entity.BizCriticalValue;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.enums.CriticalTypeEnum;
import com.his.medicaltech.enums.CriticalValueStatusEnum;
import com.his.medicaltech.enums.NotifyStatusEnum;
import com.his.medicaltech.mapper.BizCriticalValueMapper;
import com.his.medicaltech.service.CriticalValueService;
import com.his.medicaltech.support.LabCriticalValueRules;
import com.his.medicaltech.vo.BizCriticalValueVO;
import com.his.medicaltech.vo.CriticalValueMessagePayloadVO;
import com.his.medicaltech.vo.CriticalValueStatsVO;
import com.his.system.entity.*;
import com.his.system.enums.BizTypeEnum;
import com.his.system.mapper.SysConfigMapper;
import com.his.system.mapper.SysEmployeeMapper;
import com.his.system.mapper.SysUserMapper;
import com.his.system.service.SysMessageService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 检验危急值闭环服务实现。
 * <p>
 * <b>为什么「超时」不落库而实时算：</b> 超时是「当前时间 &gt; 处置时限」这个随时间变化的事实，
 * 一旦存成状态位，就必须有人定时去改它；没人改就会出现「明明超时了还显示未超时」。
 * 显示态一律由查询时计算，只把固定的 {@code deadline_time} 落库。
 * <p>
 * <b>本服务不做的事</b>：不阻断检验结果录入、不修改结果值、不调用任何模型。
 * 危急值上报失败（例如站内信发不出去）绝不能让人白录一遍结果，
 * 所以通知失败只标记 {@code notifyStatus=0} 并记日志，事务不回滚。
 * <p>
 * <b>通知去向必须留痕</b>：申请医师ID 为空的检验单是合法存在的
 * （外部导入、无开单医生的补录结果），此时绝不能只写一行日志就丢掉通知 ——
 * 日志没人看，而 {@code notifyStatus=0} 又说不清原因。处理办法是：
 * 先落到 {@code lab.critical_value_fallback_receiver} 配置的兜底接收人，
 * 连兜底都没有时把原因写进备注，让「没通知到人」在列表页看得见。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CriticalValueServiceImpl extends ServiceImpl<BizCriticalValueMapper, BizCriticalValue>
        implements CriticalValueService {

    private static final String SOURCE_RULE = "RULE";

    private static final String NO_PREFIX = "WJ";

    private static final String DEADLINE_CONFIG_KEY = "lab.critical_value_deadline_minutes";

    private static final int DEADLINE_FALLBACK_MINUTES = 30;

    /**
     * 无开单医生时的兜底接收人配置键。
     * <p>
     * 取值可以是<b>用户名</b>（推荐，可读）或<b>员工ID</b>（数字）。
     * 为空表示不配置兜底 —— 此时通知不发送，但会把原因写进危急值记录的备注。
     */
    private static final String FALLBACK_RECEIVER_CONFIG_KEY = "lab.critical_value_fallback_receiver";

    private static final long CONFIG_CACHE_TTL_MS = 300_000L;

    private final SysMessageService sysMessageService;

    private final SysConfigMapper sysConfigMapper;

    private final SysUserMapper sysUserMapper;

    private final SysEmployeeMapper sysEmployeeMapper;

    private final com.his.medicaltech.mapper.BizLaboratoryRecordMapper recordMapper;
    private final AtomicLong configLoadedAt = new AtomicLong(0L);
    private volatile Integer cachedDeadlineMinutes;

    // 识别与上报

    private static boolean isOverdue(BizCriticalValue entity) {
        Integer status = entity.getStatus();
        if (status == null || (status != CriticalValueStatusEnum.PENDING.getCode() && status != CriticalValueStatusEnum.RECEIVED.getCode())) {
            return false;
        }
        return entity.getDeadlineTime() != null && LocalDateTime.now().isAfter(entity.getDeadlineTime());
    }

    private static String buildResultText(BizCriticalValue entity) {
        StringBuilder builder = new StringBuilder();
        builder.append(entity.getResultValue() == null ? "" : entity.getResultValue());
        if (TextUtil.hasText(entity.getResultUnit())) {
            builder.append(' ').append(entity.getResultUnit());
        }
        if (entity.getCriticalType() != null) {
            builder.append(entity.getCriticalType() == CriticalTypeEnum.HIGH.getCode() ? " ↑" : " ↓");
        }
        return builder.toString();
    }

    /**
     * 状态文案。
     * <p>
     * 「1」的文案必须是「待接收」而不是「未处理」—— 后者是通用措辞，
     * 而这里是闭环流转的第一环（已上报、尚未被医护接收），
     * 与列表页的筛选项、统计卡片、DB 列注释都取同一个口径。
     */
    private static String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        CriticalValueStatusEnum e = CriticalValueStatusEnum.getByCode(status);
        return e != null ? e.getDescription() : "未知";
    }

    private static LocalDateTime startOfDay(String text) {
        return TimeUtil.dayStart(LocalDate.parse(text.trim(), DateFormats.DATE));
    }

    private static LocalDateTime endOfDay(String text) {
        return TimeUtil.dayEnd(LocalDate.parse(text.trim(), DateFormats.DATE));
    }

    // 查询

    private static String buildNo() {
        String timestamp = LocalDateTime.now().format(DateFormats.COMPACT_DATETIME);
        String tail = String.format("%04d", (int) (Math.random() * 10_000));
        return NO_PREFIX + timestamp + tail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int detectAndReport(BizLaboratoryRecord record, List<BizLabResult> results) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        if (record == null || results == null || results.isEmpty()) {
            return 0;
        }
        int deadlineMinutes = deadlineMinutes();
        LocalDateTime now = LocalDateTime.now();
        List<BizCriticalValue> hits = new ArrayList<>();

        for (BizLabResult result : results) {
            Optional<LabCriticalValueRules.Hit> hit = LabCriticalValueRules.check(
                    result.getLaboratoryItemCode(), result.getLaboratoryItemName(),
                    result.getResultUnit(), result.getResultValue());
            if (hit.isEmpty()) {
                continue;
            }
            LabCriticalValueRules.Hit value = hit.get();
            BizCriticalValue entity = new BizCriticalValue();
            entity.setCriticalNo(buildNo());
            entity.setRecordId(record.getId());
            entity.setRecordNo(record.getRecordNo());
            entity.setPatientId(record.getPatientId());
            entity.setPatientNo(record.getPatientNo());
            entity.setPatientName(record.getPatientName());
            entity.setGender(record.getGender());
            entity.setAge(record.getAge());
            entity.setItemCode(result.getLaboratoryItemCode());
            entity.setItemName(result.getLaboratoryItemName());
            entity.setResultValue(result.getResultValue());
            entity.setResultUnit(result.getResultUnit());
            entity.setReferenceRange(result.getReferenceRange());
            entity.setCriticalType(value.type());
            entity.setThresholdText(TextUtil.cut(value.threshold(), 100));
            entity.setCriticalDesc(TextUtil.cut(value.description(), 300));
            entity.setReportDeptId(record.getLaboratoryDeptId());
            entity.setReportDeptName(record.getLaboratoryDeptName());
            entity.setReportBy(operatorUser.getRealName());
            entity.setReportTime(now);
            entity.setDeadlineTime(now.plusMinutes(deadlineMinutes));
            entity.setNotifyStatus(NotifyStatusEnum.NONE.getCode());
            entity.setStatus(CriticalValueStatusEnum.PENDING.getCode());
            entity.setSource(SOURCE_RULE);
            entity.setCreateBy(operatorUser.getRealName());
            hits.add(entity);
        }

        if (hits.isEmpty()) {
            return 0;
        }

        for (BizCriticalValue entity : hits) {
            save(entity);
        }
        log.warn("[危急值] 检验记录 {} 识别出 {} 条危急值：{}", record.getRecordNo(), hits.size(),
                hits.stream().map(BizCriticalValue::getCriticalDesc).toList());

        // 通知放在写库之后：先保证「上报」这件事成立，再考虑「通知到人」
        for (BizCriticalValue entity : hits) {
            notifyDoctor(record, entity);
        }
        return hits.size();
    }

    /**
     * 通知开单医生。失败不回滚上报记录 —— 上报是事实，通知是手段，
     * 手段没成不能把事实抹掉，否则连「有没有上报过」都查不到了。
     * <p>
     * 收件人取申请医师ID，它是<b>员工ID</b>（全系统「医生ID」的统一口径，
     * 来自 {@code currentUser.getEmployeeId()}），站内信的 {@code receiver_id} 也按员工ID存，
     * 两侧一致。这里不要改成用户的ID。
     * <p>
     * 开单医生缺失时依次尝试：兜底接收人 → 放弃并留痕。三种结果都会写备注，
     * 让人能从列表页区分「已通知本人 / 已通知兜底 / 根本没通知出去」。
     */
    private void notifyDoctor(BizLaboratoryRecord record, BizCriticalValue entity) {
        Long receiverId = record.getApplyDoctorId();
        String receiverName = record.getApplyDoctorName();
        boolean viaFallback = false;

        if (receiverId == null) {
            Receiver fallback = fallbackReceiver();
            if (fallback == null) {
                markRemark(entity, "未发送站内信：检验单无开单医生，且未配置兜底接收人（"
                        + FALLBACK_RECEIVER_CONFIG_KEY + "）");
                return;
            }
            receiverId = fallback.employeeId();
            receiverName = fallback.name();
            viaFallback = true;
        }

        try {
            String title = "危急值提醒：" + entity.getItemName();
            String content = String.format(
                    "患者 %s（%s）的检验项目「%s」出现危急值：%s。请立即查看并处置，处置时限 %d 分钟。危急值号 %s。",
                    entity.getPatientName(),
                    SysGenderEnum.getText(entity.getGender()),
                    entity.getItemName(),
                    entity.getCriticalDesc(),
                    deadlineMinutes(),
                    entity.getCriticalNo());
            boolean sent = sysMessageService.sendSystemMessage(
                    receiverId, receiverName, title, content,
                    BizTypeEnum.CRITICAL.getType(), entity.getId(),
                    "urgent",
                    cn.hutool.json.JSONUtil.toJsonStr(firstPayload(entity)),
                    0);
            if (sent) {
                entity.setNotifyStatus(NotifyStatusEnum.SENT.getCode());
                entity.setNotifyTime(LocalDateTime.now());
                if (viaFallback) {
                    markRemark(entity, "开单医生缺失，已改发兜底接收人「" + receiverName + "」");
                } else {
                    updateById(entity);
                }
            } else {
                markRemark(entity, "未发送站内信：站内信服务返回失败，接收人「" + receiverName + "」");
            }
        } catch (Exception ex) {
            log.error("[危急值] {} 站内信通知失败，上报记录仍然保留", entity.getCriticalNo(), ex);
            markRemark(entity, "未发送站内信：通知过程异常（" + ex.getClass().getSimpleName() + "）");
        }
    }

    // 超时升级

    /**
     * 兜底接收人。配置值可以是用户名，也可以直接是员工ID。
     * <p>
     * 读配置失败/没配/解析不出来一律返回 {@code null}，由调用方按「没通知出去」留痕 ——
     * 兜底逻辑本身不能再有静默失败。
     */
    private Receiver fallbackReceiver() {
        try {
            SysConfig config = sysConfigMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>()
                            .eq(SysConfig::getConfigKey, FALLBACK_RECEIVER_CONFIG_KEY)
                            .last("LIMIT 1"));
            if (config == null || !TextUtil.hasText(config.getConfigValue())) {
                return null;
            }
            String raw = config.getConfigValue().trim();
            SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUserName, raw)
                    .last("LIMIT 1"));
            if (user != null) {
                if (user.getEmpId() == null) {
                    log.warn("[危急值] 兜底接收人 {} 没有关联员工档案，无法接收站内信", raw);
                    return null;
                }
                String name = TextUtil.hasText(user.getRealName()) ? user.getRealName() : user.getUserName();
                return new Receiver(user.getEmpId(), name);
            }
            // 兼容直接填员工ID
            return new Receiver(Long.parseLong(raw), "兜底接收人");
        } catch (Exception ex) {
            log.warn("[危急值] 读取兜底接收人配置 {} 失败：{}", FALLBACK_RECEIVER_CONFIG_KEY, ex.getMessage());
            return null;
        }
    }

    /**
     * 把通知去向写进备注。
     * <p>
     * 写入本身也要兜住异常：这里是「事后记账」，不能因为记不上账而把已经完成的
     * 危急值上报拖成失败。
     */
    private void markRemark(BizCriticalValue entity, String remark) {
        try {
            entity.setRemark(TextUtil.cut(remark, 500));
            updateById(entity);
        } catch (Exception ex) {
            log.warn("[危急值] {} 通知留痕写入失败，原因为：{}", entity.getCriticalNo(), remark);
        }
    }

    @Override
    public PageResult<BizCriticalValueVO> listPage(CriticalValueQueryPageDTO dto) {
        LambdaQueryWrapper<BizCriticalValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getPatientId() != null, BizCriticalValue::getPatientId, dto.getPatientId())
                .eq(dto.getStatus() != null, BizCriticalValue::getStatus, dto.getStatus())
                .eq(dto.getCriticalType() != null, BizCriticalValue::getCriticalType, dto.getCriticalType());

        if (TextUtil.hasText(dto.getKeyword())) {
            String keyword = dto.getKeyword().trim();
            wrapper.and(w -> w.like(BizCriticalValue::getCriticalNo, keyword)
                    .or().like(BizCriticalValue::getPatientName, keyword)
                    .or().like(BizCriticalValue::getItemName, keyword));
        }

        // 日期参数先判空再解析 —— MyBatis-Plus 的 condition 重载会提前求值 value，
        // 把 startOfDay(null) 写在参数里一样会炸（本项目已踩过一次）。
        String startDate = dto.getStartDate();
        if (TextUtil.hasText(startDate)) {
            wrapper.ge(BizCriticalValue::getReportTime, startOfDay(startDate));
        }
        String endDate = dto.getEndDate();
        if (TextUtil.hasText(endDate)) {
            wrapper.le(BizCriticalValue::getReportTime, endOfDay(endDate));
        }

        if (Boolean.TRUE.equals(dto.getOverdueOnly())) {
            wrapper.in(BizCriticalValue::getStatus, CriticalValueStatusEnum.PENDING.getCode(), CriticalValueStatusEnum.RECEIVED.getCode())
                    .lt(BizCriticalValue::getDeadlineTime, LocalDateTime.now());
        }

        wrapper.orderByAsc(BizCriticalValue::getStatus)
                .orderByDesc(BizCriticalValue::getReportTime);

        Page<BizCriticalValue> page = page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<BizCriticalValueVO> records = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public BizCriticalValueVO getById(Long criticalValueId) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (criticalValueId == null) {
            throw new BusinessException("危急值ID不能为空");
        }
        BizCriticalValue entity = super.getById(criticalValueId);
        if (entity == null) {
            throw new BusinessException("危急值记录不存在：" + criticalValueId);
        }
        return toVO(entity);
    }

    @Override
    public CriticalValueStatsVO stats() {
        LocalDateTime monthStart = TimeUtil.dayStart(YearMonth.now().atDay(1));
        LocalDateTime now = LocalDateTime.now();

        CriticalValueStatsVO vo = new CriticalValueStatsVO();
        vo.setMonthTotal(count(new LambdaQueryWrapper<BizCriticalValue>()
                .ge(BizCriticalValue::getReportTime, monthStart)));
        vo.setPending(count(new LambdaQueryWrapper<BizCriticalValue>()
                .eq(BizCriticalValue::getStatus, CriticalValueStatusEnum.PENDING.getCode())));
        vo.setReceived(count(new LambdaQueryWrapper<BizCriticalValue>()
                .eq(BizCriticalValue::getStatus, CriticalValueStatusEnum.RECEIVED.getCode())));
        vo.setHandled(count(new LambdaQueryWrapper<BizCriticalValue>()
                .eq(BizCriticalValue::getStatus, CriticalValueStatusEnum.HANDLED.getCode())));
        vo.setOverdue(count(new LambdaQueryWrapper<BizCriticalValue>()
                .in(BizCriticalValue::getStatus, CriticalValueStatusEnum.PENDING.getCode(), CriticalValueStatusEnum.RECEIVED.getCode())
                .isNotNull(BizCriticalValue::getDeadlineTime)
                .lt(BizCriticalValue::getDeadlineTime, now)));

        long handled = vo.getHandled() == null ? 0L : vo.getHandled();
        if (handled > 0) {
            long inTime = count(new LambdaQueryWrapper<BizCriticalValue>()
                    .eq(BizCriticalValue::getStatus, CriticalValueStatusEnum.HANDLED.getCode())
                    .isNotNull(BizCriticalValue::getHandleTime)
                    .isNotNull(BizCriticalValue::getDeadlineTime)
                    .apply("handle_time <= deadline_time"));
            vo.setTimelyRate(BigDecimal.valueOf(inTime)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(handled), 1, RoundingMode.HALF_UP));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receive(CriticalValueReceiveDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizCriticalValue entity = require(dto.getCriticalValueId());
        if (entity.getStatus() != CriticalValueStatusEnum.PENDING.getCode()) {
            throw new BusinessException("当前状态不允许接收（状态：" + statusText(entity.getStatus()) + "）");
        }
        entity.setStatus(CriticalValueStatusEnum.RECEIVED.getCode());
        entity.setReceiveBy(TextUtil.hasText(dto.getReceiveBy()) ? dto.getReceiveBy() : operatorUser.getRealName());
        entity.setReceiveTime(LocalDateTime.now());
        entity.setUpdateBy(operatorUser.getRealName());
        return updateById(entity);
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handle(CriticalValueHandleDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizCriticalValue entity = require(dto.getCriticalValueId());
        if (entity.getStatus() != CriticalValueStatusEnum.PENDING.getCode() && entity.getStatus() != CriticalValueStatusEnum.RECEIVED.getCode()) {
            throw new BusinessException("当前状态不允许处置（状态：" + statusText(entity.getStatus()) + "）");
        }
        entity.setStatus(CriticalValueStatusEnum.HANDLED.getCode());
        entity.setHandleBy(TextUtil.hasText(dto.getHandleBy()) ? dto.getHandleBy() : operatorUser.getRealName());
        entity.setHandleTime(LocalDateTime.now());
        entity.setHandleMeasure(TextUtil.cut(dto.getHandleMeasure(), 500));
        // 未显式接收就直接处置时，把接收人也补上：闭环链条不能断在中间
        if (!TextUtil.hasText(entity.getReceiveBy())) {
            entity.setReceiveBy(entity.getHandleBy());
            entity.setReceiveTime(entity.getHandleTime());
        }
        entity.setUpdateBy(operatorUser.getRealName());
        boolean ok = updateById(entity);
        if (ok) {
            // 消息侧待办联动：危急值处置完成 → 该危急值的站内信 handle_status 置 1。
            // 缺了这一步，医生在消息工作台处置完了，收件箱里还挂着「待处理」。
            // 收件箱里可能有 fallback 接收人的消息，全部联动（都指向同一次危急值）。
            sysMessageService.update(new LambdaUpdateWrapper<SysMessage>()
                    .eq(SysMessage::getBizType, BizTypeEnum.CRITICAL.getType())
                    .eq(SysMessage::getBizId, entity.getId())
                    .eq(SysMessage::getHandleStatus, 0)
                    .set(SysMessage::getHandleStatus, 1));
        }
        return ok;
    }

    /**
     * 超时升级主流程：每条「已超时仍未处置」的危急值只升级一次。
     * <p>
     * 收件人两路：① 该危急值发过的全部消息收件人（含兜底接收人，二次催办）；
     * ② 原收件人同科室、职称带「主任」的在职员工（上级督促）。
     * ① 查不到时退回检验单开单医生；连开单医生都没有 → remark 留痕并置已升级
     * （每 5 分钟一轮的扫描不能为一条永远找不到人的记录空转）。
     * <p>
     * 单条失败不中断整批：升级是催办手段，一条炸了不能拖累其他条。
     */
    @Override
    public int escalateOverdue() {
        List<BizCriticalValue> overdue = list(new LambdaQueryWrapper<BizCriticalValue>()
                .in(BizCriticalValue::getStatus,
                        CriticalValueStatusEnum.PENDING.getCode(), CriticalValueStatusEnum.RECEIVED.getCode())
                .isNotNull(BizCriticalValue::getDeadlineTime)
                .lt(BizCriticalValue::getDeadlineTime, LocalDateTime.now())
                .eq(BizCriticalValue::getEscalateStatus, 0));
        int escalated = 0;
        for (BizCriticalValue entity : overdue) {
            try {
                if (escalateOne(entity)) {
                    escalated++;
                }
            } catch (Exception ex) {
                log.error("[危急值] {} 超时升级失败，下轮扫描不再重试（标记已置）", entity.getCriticalNo(), ex);
                markEscalated(entity, "超时升级异常（" + ex.getClass().getSimpleName() + "）");
            }
        }
        if (escalated > 0) {
            log.warn("[危急值] 超时升级完成：本次升级 {} 条", escalated);
        }
        return escalated;
    }

    /**
     * 升级单条。返回 true 表示真的发了升级通知。
     */
    private boolean escalateOne(BizCriticalValue entity) {
        // 收件人第一路：该危急值已发消息的全部收件人（开单医生/兜底接收人）
        List<Long> receiverIds = new ArrayList<>(sysMessageService.receiverIdsOfBiz(
                BizTypeEnum.CRITICAL.getType(), entity.getId()));

        // 查不到任何消息收件人：退回检验单开单医生
        if (receiverIds.isEmpty() && entity.getRecordId() != null) {
            BizLaboratoryRecord record = recordMapper.selectById(entity.getRecordId());
            if (record != null && record.getApplyDoctorId() != null) {
                receiverIds.add(record.getApplyDoctorId());
            }
        }

        // 上级第二路：原收件人同科室、职称带「主任」的在职员工（去重后不与原收件人重复发）
        List<Long> leaderIds = findDeptLeaders(receiverIds);

        if (receiverIds.isEmpty() && leaderIds.isEmpty()) {
            markEscalated(entity, "超时升级未发送：找不到任何接收人（无消息记录、无开单医生、无同科室主任）");
            return false;
        }

        String overdueMinutes = String.valueOf(java.time.Duration.between(
                entity.getDeadlineTime(), LocalDateTime.now()).toMinutes());
        String title = "危急值超时升级：" + entity.getItemName();
        String base = String.format(
                "危急值已超时 %s 分钟未处置：患者 %s 的「%s」%s（危急值号 %s，报告时间 %s）。",
                overdueMinutes, entity.getPatientName(), entity.getItemName(),
                buildResultText(entity), entity.getCriticalNo(),
                entity.getReportTime() == null ? "-" : entity.getReportTime().toLocalDate());

        int sent = 0;
        for (Long id : receiverIds) {
            sent += sendEscalation(id, title,
                    base + "已超出处置时限，请立即处置并在站内信完成闭环。", entity, 0);
        }
        for (Long id : leaderIds) {
            sent += sendEscalation(id, title,
                    base + "您科室危急值已超时未处置，请督促相关医生尽快处置。", entity, 1);
        }

        markEscalated(entity, sent > 0
                ? "超时升级已发送：催办 " + receiverIds.size() + " 人、上级 " + leaderIds.size() + " 人"
                : "超时升级发送失败：站内信服务均返回失败");
        return sent > 0;
    }

    /**
     * 同科室上级：取第一路收件人的科室，找该科室副高及以上职称的在职员工
     * （301~304 副主任、401~404 主任，口径单点在 {@link EmpTitleCode#SENIOR}）。
     * 收件人科室全查不到时返回空 —— 升级宁缺勿滥，不猜「大概是谁」。
     *
     * <p>⚠ 2026-09-28（sql/174）：职称已从中文自由文本改为职称字典码，
     * 这里原来是 likeRight(title, "主任")，改完之后会**静默永远查不到人**
     * （不报错、只是升级通知再也不发给科主任）。现在按码值集合判定。
     */
    private List<Long> findDeptLeaders(List<Long> receiverIds) {
        List<Long> leaders = new ArrayList<>();
        for (Long id : receiverIds) {
            SysEmployee doctor = sysEmployeeMapper.selectById(id);
            if (doctor == null || doctor.getDeptId() == null) {
                continue;
            }
            List<SysEmployee> candidates = sysEmployeeMapper.selectList(
                    new LambdaQueryWrapper<SysEmployee>()
                            .eq(SysEmployee::getDeptId, doctor.getDeptId())
                            .in(SysEmployee::getTitle, EmpTitleCode.SENIOR)
                            .eq(SysEmployee::getStatus, 1));
            for (SysEmployee e : candidates) {
                if (e.getId() != null && !receiverIds.contains(e.getId()) && !leaders.contains(e.getId())) {
                    leaders.add(e.getId());
                }
            }
            if (!leaders.isEmpty()) {
                break; // 同一危急值只报一次上级，多个原收件人同科室时不重复扫
            }
        }
        return leaders;
    }

    /**
     * 发升级消息。toLeader 只影响 payload 标记，闭环联动复用 handle() 的业务标识匹配。
     */
    private int sendEscalation(Long receiverId, String title, String content,
                               BizCriticalValue entity, int toLeader) {
        try {
            SysEmployee emp = sysEmployeeMapper.selectById(receiverId);
            String name = emp != null && TextUtil.hasText(emp.getEmpName()) ? emp.getEmpName() : "站内用户";
            boolean ok = sysMessageService.sendSystemMessage(
                    receiverId, name, title, content,
                    BizTypeEnum.CRITICAL.getType(), entity.getId(),
                    "urgent",
                    cn.hutool.json.JSONUtil.toJsonStr(escalatePayload(entity, toLeader == 1)),
                    0);
            return ok ? 1 : 0;
        } catch (Exception ex) {
            log.warn("[危急值] {} 升级消息发送失败（收件人 {}）：{}", entity.getCriticalNo(), receiverId, ex.getMessage());
            return 0;
        }
    }

    /**
     * 首次上报催办的载荷。
     *
     * <p>带处置时限是这一条特有的：医生要据此判断"现在去处置来不来得及"。
     */
    private CriticalValueMessagePayloadVO firstPayload(BizCriticalValue entity) {
        CriticalValueMessagePayloadVO payload = new CriticalValueMessagePayloadVO();
        payload.setPatientName(entity.getPatientName());
        payload.setItemName(entity.getItemName());
        payload.setCriticalNo(entity.getCriticalNo());
        payload.setCriticalDesc(entity.getCriticalDesc());
        payload.setDeadlineMinutes(deadlineMinutes());
        return payload;
    }

    /**
     * 超时升级的载荷。{@code toLeader} 区分"催本人"与"催科主任"，
     * 两条消息的接收人不同，渲染上要能看出来这是上级督办而不是本人待办。
     */
    private CriticalValueMessagePayloadVO escalatePayload(BizCriticalValue entity, boolean toLeader) {
        CriticalValueMessagePayloadVO payload = new CriticalValueMessagePayloadVO();
        payload.setPatientName(entity.getPatientName());
        payload.setItemName(entity.getItemName());
        payload.setCriticalNo(entity.getCriticalNo());
        payload.setCriticalDesc(entity.getCriticalDesc());
        payload.setEscalate(true);
        payload.setToLeader(toLeader);
        return payload;
    }

    /**
     * 升级标记落库 + remark 留痕。标记必须置上：每条只升级一次，不能被每 5 分钟的扫描重复轰炸。
     */
    private void markEscalated(BizCriticalValue entity, String remark) {
        try {
            BizCriticalValue patch = new BizCriticalValue();
            patch.setId(entity.getId());
            patch.setEscalateStatus(1);
            patch.setEscalateTime(LocalDateTime.now());
            patch.setRemark(TextUtil.cut(remark, 500));
            updateById(patch);
        } catch (Exception ex) {
            log.error("[危急值] {} 升级标记写入失败", entity.getCriticalNo(), ex);
        }
    }

    @Override
    public boolean deleteById(Long criticalValueId) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (criticalValueId == null) {
            throw new BusinessException("危急值ID不能为空");
        }
        return removeById(criticalValueId);
    }

    private BizCriticalValue require(Long criticalValueId) {
        // C类：内部按主键捞单的公共闸口，入参非请求 DTO，Bean Validation 够不到，保留
        if (criticalValueId == null) {
            throw new BusinessException("危急值ID不能为空");
        }
        BizCriticalValue entity = super.getById(criticalValueId);
        if (entity == null) {
            throw new BusinessException("危急值记录不存在：" + criticalValueId);
        }
        return entity;
    }

    private long count(LambdaQueryWrapper<BizCriticalValue> wrapper) {
        return baseMapper.selectCount(wrapper);
    }

    private BizCriticalValueVO toVO(BizCriticalValue entity) {
        BizCriticalValueVO vo = new BizCriticalValueVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setGenderText(SysGenderEnum.getText(entity.getGender()));
        vo.setStatusText(statusText(entity.getStatus()));
        vo.setCriticalTypeText(CriticalTypeEnum.getText(entity.getCriticalType()));
        vo.setResultText(buildResultText(entity));
        vo.setOverdue(isOverdue(entity));
        return vo;
    }

    /**
     * 处置时限（分钟），取自系统参数，读不到用 30 分钟兜底。
     */
    private int deadlineMinutes() {
        long now = System.currentTimeMillis();
        Integer local = cachedDeadlineMinutes;
        if (local != null && now - configLoadedAt.get() <= CONFIG_CACHE_TTL_MS) {
            return local;
        }
        int minutes = DEADLINE_FALLBACK_MINUTES;
        try {
            SysConfig config = sysConfigMapper.selectOne(
                    new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, DEADLINE_CONFIG_KEY).last("LIMIT 1"));
            if (config != null && TextUtil.hasText(config.getConfigValue())) {
                minutes = Integer.parseInt(config.getConfigValue().trim());
            }
        } catch (Exception ex) {
            log.warn("[危急值] 读取处置时限配置失败，使用默认 {} 分钟", DEADLINE_FALLBACK_MINUTES);
        }
        cachedDeadlineMinutes = minutes;
        configLoadedAt.set(now);
        return minutes;
    }

    /**
     * 兜底接收人（员工ID + 姓名）
     */
    private record Receiver(Long employeeId, String name) {
    }
}
