package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TimeUtil;
import com.his.medicaltech.dto.*;
import com.his.medicaltech.entity.BizTransfusionApply;
import com.his.medicaltech.entity.BizTransfusionApprove;
import com.his.medicaltech.entity.BizTransfusionBag;
import com.his.medicaltech.enums.*;
import com.his.medicaltech.mapper.BizTransfusionApplyMapper;
import com.his.medicaltech.mapper.BizTransfusionApproveMapper;
import com.his.medicaltech.mapper.BizTransfusionBagMapper;
import com.his.medicaltech.service.TransfusionApplyService;
import com.his.medicaltech.support.TransfusionCheckItems;
import com.his.medicaltech.support.TransfusionRules;
import com.his.medicaltech.vo.TransfusionApplyVO;
import com.his.medicaltech.vo.TransfusionBagVO;
import com.his.patient.entity.BizAdmission;
import com.his.patient.entity.BizInpatientRecord;
import com.his.patient.entity.BizPatient;
import com.his.patient.service.BizPatientService;
import com.his.patient.service.InpatientRecordService;
import com.his.patient.service.InpatientService;
import com.his.patient.vo.CodeOptionVO;
import com.his.patient.vo.WardVO;
import com.his.system.entity.CurrentUser;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 住院输血闭环服务实现（P4.4）。
 *
 * <p>本类固化了这些<b>至少踩过一次或一定会被追问</b>的点：
 *
 * <ol>
 *   <li><b>ABO 相容性按品种分流，红细胞与血浆方向相反</b>
 *       （{@code TransfusionRules.isAboCompatible}）：把两张表混成一张，
 *       就会把 O 型血浆发给 A 型患者 —— 溶血。这是本类最不能出错的一段。</li>
 *   <li><b>Rh 阴性受血者必须输 Rh 阴性血</b>，对所有品种生效（血小板不做 ABO 硬拦，
 *       但 Rh 一律硬拦）。</li>
 *   <li><b>配血不合不可发血</b>：{@code crossmatch_status=3} 时发血端点直接拒。
 *       流程状态会停在「待配血」—— 所以配血状态是**独立的一列**，
 *       不能让"配了、但不合"显示成"还没配"。</li>
 *   <li><b>未配齐不可发血</b>：申请 2 袋只配到 1 袋就发血，输注量对不上申请量。</li>
 *   <li><b>一袋血只能给一个人</b>：血袋号跨申请单唯一（应用层校验，不用 DB UNIQUE ——
 *       本表是逻辑删除，UNIQUE 会与"删过的行还留着"打架）。</li>
 *   <li><b>输注前必须双人核对</b>，且两个护士不能是同一个人；
 *       1~6 项必核项缺一不可（输血是唯一要求双人核对的护理操作）。</li>
 *   <li><b>完成才回写，且一个事务里回写病历 + 首页标志</b>：回写失败整笔回滚 ——
 *       不允许"状态已完成、病历里查不到"。</li>
 *   <li><b>输血反应上报不回改历史状态</b>：反应可能在输注后数小时才发现，
 *       补登记只置 {@code has_reaction}，把已完成改回输注中就是改历史。</li>
 *   <li><b>不回填患者档案血型</b>：患者基本信息.blood_type 由建档/体检维护，
 *       输血科不越权改主数据；不一致时写 remark 提示而不是阻断（档案常是旧的）。</li>
 *   <li><b>时间一律截到秒</b>：库表 DATETIME(0) 会四舍五入，不截就"写进去的 ≠ 读回来的"。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransfusionApplyServiceImpl implements TransfusionApplyService {
    /**
     * 入院状态：在院
     */
    private static final int ADMITTED = 1;
    /**
     * 病历文书类型：11-输血记录（本闭环完成时由系统回写）
     */
    private static final int RECORD_TYPE_TRANSFUSION = 11;
    /**
     * 文书状态：已提交（输血记录一落库就是正式文书，不留在草稿箱）
     */
    private static final int RECORD_STATUS_SUBMITTED = 2;
    /**
     * 已配血/已发血后多久没往下走算"卡住"（查询时算，不落状态列）
     */
    private static final long STALLED_HOURS = 24;

    private final BizTransfusionApplyMapper applyMapper;

    private final BizTransfusionBagMapper bagMapper;

    private final BizTransfusionApproveMapper approveRecordMapper;

    private final BizPatientService bizPatientService;

    private final InpatientService inpatientService;

    private final InpatientRecordService inpatientRecordService;

    private DictCacheService dictCacheService;

    // 查询

    /**
     * 追加备注（不覆盖已有内容；超 500 截断，避免超长直接 SQL 报错）
     */
    private static String mergeRemark(String origin, String append) {
        String text = StringUtils.hasText(origin)
                ? (StringUtils.hasText(append) ? origin + "；" + append : origin)
                : append;
        if (text != null && text.length() > 500) {
            return text.substring(0, 497) + "…";
        }
        return text;
    }

    private static String textOr(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private static LocalDateTime now() {
        return TimeUtil.nowSeconds();
    }

    /**
     * 下界宽松解析：{@code yyyy-MM-dd} → 当天 00:00:00；带时分秒则原样使用（含）
     */
    private static String normalizeFrom(String raw) {
        Parsed p = parse(raw);
        return p == null ? null : p.from;
    }

    /**
     * 上界宽松解析：{@code yyyy-MM-dd} → 次日 00:00:00（不含，覆盖当天最后一秒）
     */
    private static String normalizeTo(String raw) {
        Parsed p = parse(raw);
        return p == null ? null : p.to;
    }

    private static Parsed parse(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String s = raw.trim();
        try {
            if (s.length() == 10) {
                LocalDate d = LocalDate.parse(s);
                return new Parsed(d.atStartOfDay().format(DateFormats.DATETIME),
                        d.plusDays(1).atStartOfDay().format(DateFormats.DATETIME));
            }
            LocalDateTime t = LocalDateTime.parse(s, DateFormats.DATETIME);
            return new Parsed(t.format(DateFormats.DATETIME), t.format(DateFormats.DATETIME));
        } catch (DateTimeParseException e) {
            // 明确报格式问题，不静默忽略、也不让它变成 500
            throw new BusinessException("时间格式不正确：" + raw + "（应为 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）");
        }
    }

    @Override
    public IPage<TransfusionApplyVO> listPage(TransfusionApplyQueryPageDTO query) {
        if (query == null) {
            query = new TransfusionApplyQueryPageDTO();
        }
        query.setApplyDateFrom(normalizeFrom(query.getApplyDateFrom()));
        query.setApplyDateTo(normalizeTo(query.getApplyDateTo()));
        query.setPatientAbo(BloodTypeEnum.normalizeAbo(query.getPatientAbo()));
        IPage<TransfusionApplyVO> page = applyMapper.selectApplyPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
        // 列表不逐行查血袋（N+1），只算进度；血袋明细在详情接口给
        page.getRecords().forEach(vo -> decorate(vo, false));
        return page;
    }

    // 一、申请（待配血）

    @Override
    public TransfusionApplyVO getDetailById(Long applyId) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (applyId == null) {
            throw new BusinessException("输血申请单ID不能为空");
        }
        TransfusionApplyVO vo = applyMapper.selectApplyById(applyId);
        if (vo == null) {
            throw new BusinessException("输血申请单不存在");
        }
        decorate(vo, true);
        vo.setApproveRecords(approveListByApply(applyId));
        return vo;
    }

    // 一·五、用血分级审批（sql/93：通过 / 驳回 / 流水 / 统计）

    @Override
    public List<TransfusionApplyVO> listByAdmission(Long admissionId) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (admissionId == null) {
            throw new BusinessException("入院ID不能为空");
        }
        List<TransfusionApplyVO> list = applyMapper.selectByAdmission(admissionId);
        list.forEach(vo -> decorate(vo, false));
        return list;
    }

    @Override
    public long countUnfinished(Long admissionId) {
        return applyMapper.countUnfinished(admissionId);
    }

    @Override
    public List<CodeOptionVO> componentOptions() {
        List<CodeOptionVO> list = new ArrayList<>();
        BloodComponentEnum.options().forEach((code, label) -> list.add(new CodeOptionVO(code, label)));
        return list;
    }

    // 二、配血（待配血 → 已配血；逐袋录入，ABO/Rh 硬拦）

    @Override
    public List<TransfusionApplyVO.CheckItem> checkItems() {
        List<TransfusionApplyVO.CheckItem> list = new ArrayList<>();
        TransfusionCheckItems.all().forEach((code, label) -> {
            TransfusionApplyVO.CheckItem item = new TransfusionApplyVO.CheckItem();
            item.setCode(code);
            item.setLabel(label);
            item.setRequired(TransfusionCheckItems.REQUIRED.contains(code));
            list.add(item);
        });
        return list;
    }

    // 三、发血（已配血 → 已发血）

    @Override
    public List<String> reactionTypes() {
        return TransfusionReactionTypeEnum.options();
    }

    // 四、开始输注（已发血 → 输注中；双人核对）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(TransfusionApplyUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        // D类：码值合法性（空值与非 A/B/O/AB 一并拒绝），不是单纯「没填」，DTO 注解放不下
        if (!BloodTypeEnum.isValidAbo(dto.getPatientAbo())) {
            throw new BusinessException("受血者 ABO 血型不能为空且必须是 A/B/O/AB 之一（当前="
                    + dto.getPatientAbo() + "）");
        }
        // D类：码值合法性（阳/阴归一校验，空值与非法值同文案拒绝），DTO 注解放不下
        if (!RhTypeEnum.isValidRh(dto.getPatientRh())) {
            throw new BusinessException("受血者 Rh 血型不能为空（阳/阴）——Rh 阴性属稀有血型，直接决定备血方案");
        }
        if (dto.getBagCount() == null || dto.getBagCount() < 1) {
            throw new BusinessException("申请袋数必须大于 0（没有袋数的用血申请无法配血）");
        }
        if (dto.getBagCount() > 20) {
            throw new BusinessException("申请袋数不合法（单次申请最多 20 袋，当前=" + dto.getBagCount()
                    + "）；大量用血请走大量输血审批流程");
        }

        BizAdmission admission = inpatientService.getAdmissionById(dto.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在");
        }
        if (!Objects.equals(ADMITTED, admission.getAdmitStatus())) {
            throw new BusinessException("该患者当前不是「在院」状态，不能申请输血（已出院的住院不能开输血单）");
        }
        BizPatient patient = bizPatientService.getById(admission.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在");
        }

        String abo = BloodTypeEnum.normalizeAbo(dto.getPatientAbo());
        String rh = RhTypeEnum.normalizeRh(dto.getPatientRh());

        boolean create = dto.getId() == null;
        // 修改分支的旧审批状态/旧折算量（字段覆盖前必须先捕获，否则"量变了要重审"永远判不出来）
        Integer oldApproveStatus = null;
        Integer oldAmountMl = null;

        BizTransfusionApply entity;
        if (create) {
            entity = new BizTransfusionApply();
            entity.setAdmissionId(dto.getAdmissionId());
            entity.setAdmissionNo(admission.getAdmissionNo());
            entity.setPatientId(admission.getPatientId());
            entity.setPatientNo(patient.getPatientNo());
            entity.setPatientName(patient.getPatientName());
            entity.setGender(patient.getGender());
            entity.setAge(patient.getAge());
            entity.setApplyDeptId(admission.getDeptId());
            entity.setApplyDeptName(deptNameOf(admission.getDeptId()));
            entity.setApplyWardName(wardNameOf(admission.getWardId()));
            entity.setApplyBedNo(bedNoOf(admission.getBedId()));
            entity.setApplyDoctorId(operatorUser.getEmployeeId());
            entity.setApplyDoctorName(operatorUser.getRealName());
            entity.setApplyTime(now());
            entity.setApplyNo(nextApplyNo());
            entity.setTransfusionStatus(TransfusionStatusEnum.PENDING_CROSSMATCH.getCode());
            entity.setCrossmatchStatus(TransfusionCrossmatchStatusEnum.PENDING.getCode());
            entity.setHasReaction(0);
        } else {
            entity = mustGet(dto.getId());
            oldApproveStatus = entity.getApproveStatus();
            oldAmountMl = entity.getAmountMl();
            if (!Objects.equals(TransfusionStatusEnum.PENDING_CROSSMATCH.getCode(), entity.getTransfusionStatus())) {
                throw new BusinessException("输血单 " + entity.getApplyNo() + " 当前状态为「"
                        + TransfusionStatusEnum.labelOrUnknown(entity.getTransfusionStatus())
                        + "」，只有「待配血」可以修改申请内容（已配血后血袋已定型，改请先取消）");
            }
            if (!Objects.equals(entity.getAdmissionId(), dto.getAdmissionId())) {
                throw new BusinessException("不允许把输血单改挂到另一次住院上");
            }
            // 改品种/袋数会让已配的血袋对不上，这里直接禁止（要改先取消重开）
            if (bagMapper.countByApply(entity.getId()) > 0
                    && (!Objects.equals(entity.getBloodComponent(), dto.getBloodComponent())
                    || !Objects.equals(entity.getBagCount(), dto.getBagCount()))) {
                throw new BusinessException("该输血单已配了 "
                        + bagMapper.countByApply(entity.getId())
                        + " 袋血，不能再改血液品种或申请袋数（改请先取消本单重新申请）");
            }
        }

        // 重复申请：同一住院 + 同一品种，不允许并存两条未完成申请（四核对的"重复"）
        // 注意"已完成"不在此列：一个患者多批次输同一种血是常规操作（今天 2U、明天再 2U）
        if (applyMapper.countUnfinishedSameComponent(dto.getAdmissionId(), dto.getBloodComponent(),
                entity.getId()) > 0) {
            throw new BusinessException("该住院已有一条未完成的「"
                    + BloodComponentEnum.labelOrUnknown(dto.getBloodComponent())
                    + "」用血申请，请先完成或取消后再发起（同一种血申请两次属于重复）");
        }

        entity.setPatientAbo(abo);
        entity.setPatientRh(rh);
        entity.setBloodComponent(dto.getBloodComponent());
        entity.setComponentSpec(dto.getComponentSpec());
        entity.setBagCount(dto.getBagCount());
        entity.setPlannedAmount(dto.getPlannedAmount());
        entity.setAmountUnit(dto.getAmountUnit());
        entity.setTransfusionPurpose(dto.getTransfusionPurpose());
        entity.setIndication(dto.getIndication());
        entity.setPreHb(dto.getPreHb());
        entity.setPreHct(dto.getPreHct());
        entity.setPrePlt(dto.getPrePlt());
        entity.setTransfusionHistory(dto.getTransfusionHistory());
        entity.setReactionHistory(dto.getReactionHistory());
        entity.setPregnancyHistory(dto.getPregnancyHistory());
        entity.setIsEmergency(dto.getIsEmergency() == null ? 0 : dto.getIsEmergency());

        // 用血分级审批（sql/93）：折算毫升 → 服务端推导级别 → 决定审批状态
        // 修改重提的规则：已通过(1) 的单子，申请量变了必须重审（防"审的 400ml 发的 1200ml"）；
        // 已驳回(2)的单子修改重提视为重新申请（清驳回原因、回到待审批）；
        // 急诊单的初始/重置状态是"急诊待补审"(3) —— 可以先配血发血，事后必须补办手续。
        Integer amountMl = TransfusionRules.amountToMl(dto.getPlannedAmount(), dto.getAmountUnit());
        entity.setApproveLevel(TransfusionRules.approveLevelOf(amountMl));
        boolean emergency = Objects.equals(1, entity.getIsEmergency());
        if (create) {
            entity.setAmountMl(amountMl);
            entity.setApproveStatus(emergency
                    ? TransfusionApproveStatusEnum.MAKEUP_PENDING.getCode() : TransfusionApproveStatusEnum.PENDING.getCode());
            entity.setApproveMakeup(0);
        } else {
            Integer oldStatus = oldApproveStatus;
            boolean amountChanged = !Objects.equals(amountMl, oldAmountMl);
            if (Objects.equals(TransfusionApproveStatusEnum.REJECTED.getCode(), oldStatus)) {
                entity.setAmountMl(amountMl);
                entity.setApproveStatus(emergency
                        ? TransfusionApproveStatusEnum.MAKEUP_PENDING.getCode() : TransfusionApproveStatusEnum.PENDING.getCode());
                entity.setApproveRejectReason(null);
            } else if (Objects.equals(TransfusionApproveStatusEnum.APPROVED.getCode(), oldStatus) && amountChanged) {
                entity.setAmountMl(amountMl);
                entity.setApproveStatus(emergency
                        ? TransfusionApproveStatusEnum.MAKEUP_PENDING.getCode() : TransfusionApproveStatusEnum.PENDING.getCode());
                entity.setApproveTime(null);
                entity.setApproveMakeup(0);
            } else {
                // 待审批/补审中：状态保持，仅刷新折算量与级别
                entity.setAmountMl(amountMl);
            }
        }

        // 与患者档案血型不一致：**不阻断**（档案常是旧的/未查的），但必须让人看见
        String archiveAbo = BloodTypeEnum.normalizeAbo(patient.getBloodType());
        String warn = null;
        if (archiveAbo != null && !archiveAbo.equals(abo)) {
            warn = "⚠ 与患者档案血型(" + archiveAbo + ")不一致，以本次鉴定为准，请核实";
            log.warn("输血申请血型与档案不一致 admissionId={} 档案={} 本次={}",
                    dto.getAdmissionId(), archiveAbo, abo);
        }
        String remark = StringUtils.hasText(dto.getRemark()) ? dto.getRemark() : null;
        if (warn != null) {
            remark = StringUtils.hasText(remark) ? remark + "；" + warn : warn;
        }
        entity.setRemark(remark);

        if (create) {
            applyMapper.insert(entity);
        } else {
            applyMapper.updateById(entity);
            // MP updateById 默认忽略 null 字段 —— "清空驳回原因/通过时间"落不进库，
            // 必须用 UpdateWrapper 显式 set null（否则重提的单子还挂着旧驳回原因）
            boolean wasRejected = Objects.equals(TransfusionApproveStatusEnum.REJECTED.getCode(), oldApproveStatus);
            boolean reApproveNeeded = !wasRejected
                    && Objects.equals(TransfusionApproveStatusEnum.APPROVED.getCode(), oldApproveStatus)
                    && !Objects.equals(amountMl, oldAmountMl);
            if (wasRejected || reApproveNeeded) {
                applyMapper.update(null, new LambdaUpdateWrapper<BizTransfusionApply>()
                        .eq(BizTransfusionApply::getId, entity.getId())
                        .set(BizTransfusionApply::getApproveRejectReason, null)
                        .set(reApproveNeeded, BizTransfusionApply::getApproveTime, null));
            }
        }
        log.info("{}输血申请 applyNo={} admissionId={} 血型={} 品种={} 袋数={} 紧急={} 申请人={}",
                create ? "发起" : "修改", entity.getApplyNo(), entity.getAdmissionId(),
                TransfusionRules.bloodTypeText(abo, rh),
                BloodComponentEnum.getText(entity.getBloodComponent()),
                entity.getBagCount(), entity.getIsEmergency(), operatorUser.getRealName());
        return entity.getApplyNo();
    }

    // 五、完成（输注中 → 已完成；回写输血记录病历 + 首页标志）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String approve(TransfusionApproveDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizTransfusionApply entity = mustGet(dto.getApplyId());
        Integer status = entity.getApproveStatus();
        if (!Objects.equals(TransfusionApproveStatusEnum.PENDING.getCode(), status)
                && !Objects.equals(TransfusionApproveStatusEnum.MAKEUP_PENDING.getCode(), status)) {
            throw new BusinessException("输血单 " + entity.getApplyNo() + " 的审批状态为「"
                    + TransfusionApproveStatusEnum.labelOrUnknown(status)
                    + "」，只有「待审批」或「急诊待补审」可以提交审批结论");
        }
        if (dto.getApproveResult() == 2 && !StringUtils.hasText(dto.getOpinion())) {
            throw new BusinessException("驳回必须写明原因（申请人要知道改什么才能重新提交）");
        }

        LocalDateTime now = now();
        Long approverId = operatorUser.getEmployeeId();
        // 职称快照：审批那一刻的职称定格（事后升职称不改历史记录）
        String approverTitle = approveRecordMapper.selectEmpTitle(approverId);
        boolean makeup = Objects.equals(TransfusionApproveStatusEnum.MAKEUP_PENDING.getCode(), status);

        BizTransfusionApprove record = new BizTransfusionApprove();
        record.setApplyId(entity.getId());
        record.setApplyNo(entity.getApplyNo());
        record.setApproveLevel(entity.getApproveLevel() == null ? 1 : entity.getApproveLevel());
        record.setApproveResult(dto.getApproveResult());
        record.setApproverId(approverId);
        record.setApproverName(operatorUser.getRealName());
        record.setApproverTitle(approverTitle);
        record.setOpinion(StringUtils.hasText(dto.getOpinion()) ? dto.getOpinion().trim() : null);
        record.setIsMakeup(makeup ? 1 : 0);
        record.setCreateTime(now.truncatedTo(ChronoUnit.SECONDS));
        approveRecordMapper.insert(record);

        if (dto.getApproveResult() == 1) {
            entity.setApproveStatus(TransfusionApproveStatusEnum.APPROVED.getCode());
            entity.setApproveTime(now.truncatedTo(ChronoUnit.SECONDS));
            entity.setApproveMakeup(makeup ? 1 : 0);
            entity.setApproveRejectReason(null);
        } else {
            entity.setApproveStatus(TransfusionApproveStatusEnum.REJECTED.getCode());
            entity.setApproveRejectReason(dto.getOpinion().trim());
        }
        applyMapper.updateById(entity);
        if (dto.getApproveResult() == 1) {
            // MP updateById 忽略 null → 驳回原因清空必须显式 set null
            applyMapper.update(null, new LambdaUpdateWrapper<BizTransfusionApply>()
                    .eq(BizTransfusionApply::getId, entity.getId())
                    .set(BizTransfusionApply::getApproveRejectReason, null));
        }
        log.info("用血审批 applyNo={} 结论={} 级别={} 补审={} 审批人={}",
                entity.getApplyNo(), dto.getApproveResult() == 1 ? "通过" : "驳回",
                entity.getApproveLevel(), makeup, operatorUser.getRealName());
        return entity.getApplyNo();
    }

    // 六、输血反应上报（仅已完成且尚未上报；不回改历史状态）

    @Override
    public List<TransfusionApplyVO.ApproveRecord> approveListByApply(Long applyId) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (applyId == null) {
            throw new BusinessException("输血申请单ID不能为空");
        }
        List<BizTransfusionApprove> records = approveRecordMapper.selectByApply(applyId);
        List<TransfusionApplyVO.ApproveRecord> rows = new ArrayList<>();
        for (BizTransfusionApprove r : records) {
            TransfusionApplyVO.ApproveRecord row = new TransfusionApplyVO.ApproveRecord();
            row.setId(r.getId());
            row.setApproveLevel(r.getApproveLevel());
            row.setApproveLevelText(dictCacheService.getDicDataLabel("biz_medicaltech_transfusionApproveLevelEnum", r.getApproveLevel()));
            row.setApproveResult(r.getApproveResult());
            row.setApproveResultText(Objects.equals(1, r.getApproveResult()) ? "通过" : "驳回");
            row.setApproverId(r.getApproverId());
            row.setApproverName(r.getApproverName());
            row.setApproverTitle(r.getApproverTitle());
            row.setOpinion(r.getOpinion());
            row.setIsMakeup(r.getIsMakeup());
            row.setApproveTime(r.getCreateTime());
            rows.add(row);
        }
        return rows;
    }

    // 七、取消（仅待配血 / 已配血 / 已发血）

    @Override
    public TransfusionApplyVO.ApproveStats approveStats() {
        TransfusionApplyVO.ApproveStats stats = new TransfusionApplyVO.ApproveStats();
        stats.setPending(applyMapper.selectCount(
                new LambdaQueryWrapper<BizTransfusionApply>()
                        .eq(BizTransfusionApply::getApproveStatus, TransfusionApproveStatusEnum.PENDING.getCode())));
        stats.setApproved(applyMapper.selectCount(
                new LambdaQueryWrapper<BizTransfusionApply>()
                        .eq(BizTransfusionApply::getApproveStatus, TransfusionApproveStatusEnum.APPROVED.getCode())));
        stats.setRejected(applyMapper.selectCount(
                new LambdaQueryWrapper<BizTransfusionApply>()
                        .eq(BizTransfusionApply::getApproveStatus, TransfusionApproveStatusEnum.REJECTED.getCode())));
        stats.setMakeupPending(applyMapper.selectCount(
                new LambdaQueryWrapper<BizTransfusionApply>()
                        .eq(BizTransfusionApply::getApproveStatus, TransfusionApproveStatusEnum.MAKEUP_PENDING.getCode())));
        List<TransfusionApplyVO.LevelCount> byLevel = new ArrayList<>();
        for (int level = 1; level <= 3; level++) {
            TransfusionApplyVO.LevelCount lc = new TransfusionApplyVO.LevelCount();
            lc.setApproveLevel(level);
            lc.setApproveLevelText(dictCacheService.getDicDataLabel("biz_medicaltech_transfusionApproveLevelEnum", level));
            lc.setCount(applyMapper.selectCount(
                    new LambdaQueryWrapper<BizTransfusionApply>()
                            .eq(BizTransfusionApply::getApproveStatus, TransfusionApproveStatusEnum.APPROVED.getCode())
                            .eq(BizTransfusionApply::getApproveLevel, level)));
            byLevel.add(lc);
        }
        stats.setByLevel(byLevel);
        return stats;
    }

    // 回写：输血记录病历

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String crossmatch(TransfusionCrossmatchDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizTransfusionApply entity = mustGet(dto.getApplyId());
        if (!Objects.equals(TransfusionStatusEnum.PENDING_CROSSMATCH.getCode(), entity.getTransfusionStatus())) {
            throw new BusinessException("输血单 " + entity.getApplyNo() + " 当前状态为「"
                    + TransfusionStatusEnum.labelOrUnknown(entity.getTransfusionStatus())
                    + "」，只有「待配血」可以录入配血结果");
        }
        // 用血分级审批闸门（sql/93）：已通过才放行；急诊补审中可以先配（事后必须补办手续）
        if (!TransfusionRules.approveGateOpen(entity.getApproveStatus(), entity.getIsEmergency())) {
            throw new BusinessException("输血单 " + entity.getApplyNo() + " 的用血审批为「"
                    + TransfusionApproveStatusEnum.labelOrUnknown(entity.getApproveStatus())
                    + "」，未通过审批不能配血（申请量 "
                    + (entity.getAmountMl() == null ? "待折算" : entity.getAmountMl() + "ml")
                    + "，属「" + dictCacheService.getDicDataLabel("biz_medicaltech_transfusionApproveLevelEnum", entity.getApproveLevel()) + "」审核签发范围）");
        }
        BizAdmission admission = inpatientService.getAdmissionById(entity.getAdmissionId());
        if (admission == null || !Objects.equals(ADMITTED, admission.getAdmitStatus())) {
            throw new BusinessException("该患者已不在院，不能再配血（已出院的住院不能继续用血流程）");
        }

        // 已配血袋数 + 本次提交数不得超过申请袋数
        long existing = bagMapper.countByApply(entity.getId());
        Set<String> submittedNos = new HashSet<>();
        // 已存在的袋号（重配同一袋时是 update 不是 insert，不占新增额度）
        Map<String, BizTransfusionBag> existingByNo = new HashMap<>();
        for (BizTransfusionBag b : bagMapper.selectByApply(entity.getId())) {
            existingByNo.put(b.getBagNo(), b);
        }
        int newCount = 0;
        List<String> problems = new ArrayList<>();

        LocalDateTime now = now();
        Long empId = operatorUser.getEmployeeId();
        String empName = operatorUser.getRealName();

        for (TransfusionCrossmatchDTO.BagDTO bag : dto.getBags()) {
            String bagNo = bag.getBagNo() == null ? null : bag.getBagNo().trim();
            if (!submittedNos.add(bagNo)) {
                throw new BusinessException("本次提交的血袋号「" + bagNo + "」重复");
            }
            if (!BloodTypeEnum.isValidAbo(bag.getBagAbo())) {
                throw new BusinessException("血袋 " + bagNo + " 的 ABO 血型不合法（应为 A/B/O/AB）");
            }
            if (!RhTypeEnum.isValidRh(bag.getBagRh())) {
                throw new BusinessException("血袋 " + bagNo + " 的 Rh 血型不合法（应为 阳/阴）");
            }
            if (bag.getExpireDate() == null) {
                throw new BusinessException("血袋 " + bagNo + " 的有效期必填（超期血袋不得输注）");
            }
            if (bag.getExpireDate().isBefore(LocalDate.now())) {
                throw new BusinessException("血袋 " + bagNo + " 已于 " + bag.getExpireDate()
                        + " 过期，不得用于输注");
            }
            if (bag.getBloodComponent() != null
                    && !Objects.equals(entity.getBloodComponent(), bag.getBloodComponent())) {
                throw new BusinessException("血袋 " + bagNo + " 的血液品种（"
                        + BloodComponentEnum.labelOrUnknown(bag.getBloodComponent())
                        + "）与申请单（" + BloodComponentEnum.labelOrUnknown(entity.getBloodComponent())
                        + "）不一致，不允许发不同品种的血");
            }
            // 一袋血只能给一个人（跨申请单校验）
            if (bagMapper.countByBagNo(bagNo, entity.getId()) > 0) {
                problems.add("血袋 " + bagNo + " 已挂在另一张输血单上（一袋血只能给一个人）");
                continue;
            }
            // **ABO / Rh 相容性硬拦**（本闭环最要紧的一条）
            String reason = TransfusionRules.incompatibleReason(
                    entity.getBloodComponent(), entity.getPatientAbo(), entity.getPatientRh(),
                    bagNo, bag.getBagAbo(), bag.getBagRh());
            if (reason != null) {
                problems.add(reason);
                continue;
            }
            if (!existingByNo.containsKey(bagNo)) {
                newCount++;
            }
        }
        if (!problems.isEmpty()) {
            throw new BusinessException("配血被拒（输血相容性校验不通过）：" + String.join("；", problems));
        }
        if (existing + newCount > entity.getBagCount()) {
            throw new BusinessException("配血袋数超出申请袋数：申请 " + entity.getBagCount()
                    + " 袋，已配 " + existing + " 袋，本次新增 " + newCount + " 袋");
        }

        // 落库
        for (TransfusionCrossmatchDTO.BagDTO bag : dto.getBags()) {
            String bagNo = bag.getBagNo().trim();
            BizTransfusionBag exist = existingByNo.get(bagNo);
            BizTransfusionBag row = exist == null ? new BizTransfusionBag() : exist;
            if (exist == null) {
                row.setApplyId(entity.getId());
                row.setAdmissionId(entity.getAdmissionId());
                row.setPatientId(entity.getPatientId());
                row.setBagNo(bagNo);
            }
            row.setDonorNo(bag.getDonorNo());
            row.setBagAbo(BloodTypeEnum.normalizeAbo(bag.getBagAbo()));
            row.setBagRh(RhTypeEnum.normalizeRh(bag.getBagRh()));
            row.setBloodComponent(bag.getBloodComponent() == null
                    ? entity.getBloodComponent() : bag.getBloodComponent());
            row.setSpec(bag.getSpec());
            row.setAmount(bag.getAmount());
            row.setAmountUnit(bag.getAmountUnit());
            row.setSourceBank(bag.getSourceBank());
            row.setCollectDate(bag.getCollectDate());
            row.setExpireDate(bag.getExpireDate());
            row.setCrossmatchMain(bag.getCrossmatchMain());
            row.setCrossmatchSide(bag.getCrossmatchSide());
            row.setCrossmatchResult(bag.getCrossmatchResult());
            row.setCrossmatchTime(now);
            row.setCrossmatchDoctorId(empId);
            row.setCrossmatchDoctorName(empName);
            // 配血结论相合才置"已配血"；不合的袋停在"待配血"，等换血源重配
            row.setBagStatus(Objects.equals(1, bag.getCrossmatchResult())
                    ? BloodBagStatusEnum.CROSSMATCHED.getCode() : BloodBagStatusEnum.PENDING.getCode());
            if (StringUtils.hasText(bag.getRemark())) {
                row.setRemark(bag.getRemark());
            }
            if (exist == null) {
                bagMapper.insert(row);
            } else {
                bagMapper.updateById(row);
            }
        }

        long total = bagMapper.countByApply(entity.getId());
        long incompatible = bagMapper.countIncompatible(entity.getId());

        if (incompatible > 0) {
            // 有不合的袋：流程状态**保持待配血**（不许发血），但配血状态必须显性化。
            //
            // 注意这里**不抛异常**：全库只有这一种"部分成功"的场景。
            // 「配血不合」不是一个失败的请求，而是一个**成功的业务结果**——
            // 血袋确实配了、结果就是不合，这条事实必须留在库里（追溯与统计都要用它）。
            // 抛异常会触发事务回滚，把刚录的血袋行一起抹掉，
            // 于是"配了但不合"变成"看起来根本没配过"，安全隐患被自己藏起来。
            // ABO/Rh 不相容则相反：那是操作错误，在入口整批回滚，不留痕。
            entity.setCrossmatchStatus(TransfusionCrossmatchStatusEnum.INCOMPATIBLE.getCode());
            if (StringUtils.hasText(dto.getCrossmatchNote())) {
                entity.setRemark(mergeRemark(entity.getRemark(), dto.getCrossmatchNote()));
            }
            applyMapper.updateById(entity);
            log.warn("配血存在不合 applyNo={} 不合袋数={} 总数={}", entity.getApplyNo(), incompatible, total);
            return "已录入配血结果：共 " + total + " 袋，其中 " + incompatible
                    + " 袋交叉配血不合，本单不能发血；请更换血源重新配血，或取消本单";
        }

        if (total >= entity.getBagCount()) {
            entity.setCrossmatchStatus(TransfusionCrossmatchStatusEnum.ALL_MATCHED.getCode());
            entity.setCrossmatchDoctorId(empId);
            entity.setCrossmatchDoctorName(empName);
            entity.setCrossmatchTime(now);
            entity.setTransfusionStatus(TransfusionStatusEnum.CROSSMATCHED.getCode());
            if (StringUtils.hasText(dto.getCrossmatchNote())) {
                entity.setRemark(mergeRemark(entity.getRemark(), dto.getCrossmatchNote()));
            }
            applyMapper.updateById(entity);
            log.info("配血完成 applyNo={} 血袋 {} 袋全部相合 配血人={}",
                    entity.getApplyNo(), total, empName);
            return "配血完成：" + total + " 袋全部相合，等待发血";
        }

        // 只配了一部分：等血站后续到货再配，流程停在「待配血」
        entity.setCrossmatchStatus(TransfusionCrossmatchStatusEnum.PARTIAL.getCode());
        if (StringUtils.hasText(dto.getCrossmatchNote())) {
            entity.setRemark(mergeRemark(entity.getRemark(), dto.getCrossmatchNote()));
        }
        applyMapper.updateById(entity);
        log.info("配血中（未配齐）applyNo={} 已配 {}/{} 袋 操作人={}",
                entity.getApplyNo(), total, entity.getBagCount(), empName);
        return "已录入配血结果：" + total + "/" + entity.getBagCount()
                + " 袋相合，尚未配齐，继续配血后方可发血";
    }

    // 展示态

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issue(TransfusionIssueDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizTransfusionApply entity = mustGet(dto.getApplyId());
        if (Objects.equals(TransfusionStatusEnum.PENDING_CROSSMATCH.getCode(), entity.getTransfusionStatus())) {
            // 同是「待配血」，原因可能完全不同：一次都还没配 vs 配了但结论是不合。
            // 对发血的人来说后者是更硬的结论（血源已经取来过、结果相斥），文案必须分开 ——
            // 否则会让人以为"还没配，再等等就好"，而实际上这批血永远不能发给这个患者。
            if (Objects.equals(TransfusionCrossmatchStatusEnum.INCOMPATIBLE.getCode(), entity.getCrossmatchStatus())) {
                throw new BusinessException("输血单 " + entity.getApplyNo()
                        + " 存在交叉配血不合的血袋，不能发血（血源与受血者相斥，"
                        + "请更换血源重新配血，或取消本单）");
            }
            throw new BusinessException("输血单 " + entity.getApplyNo()
                    + " 尚未配血完成，不能发血（相合性未知的血不能发给患者）");
        }
        if (!Objects.equals(TransfusionStatusEnum.CROSSMATCHED.getCode(), entity.getTransfusionStatus())) {
            throw new BusinessException("输血单 " + entity.getApplyNo() + " 当前状态为「"
                    + TransfusionStatusEnum.labelOrUnknown(entity.getTransfusionStatus())
                    + "」，只有「已配血」可以发血");
        }
        if (!Objects.equals(TransfusionCrossmatchStatusEnum.ALL_MATCHED.getCode(), entity.getCrossmatchStatus())) {
            throw new BusinessException("输血单 " + entity.getApplyNo() + " 的配血状态为「"
                    + TransfusionCrossmatchStatusEnum.labelOrUnknown(entity.getCrossmatchStatus())
                    + "」，不允许发血（必须全部相合且配齐）");
        }
        // 用血分级审批闸门（sql/93）第二道：配血口放行过急诊补审的单子，发血口同样要拦常规未批
        if (!TransfusionRules.approveGateOpen(entity.getApproveStatus(), entity.getIsEmergency())) {
            throw new BusinessException("输血单 " + entity.getApplyNo() + " 的用血审批为「"
                    + TransfusionApproveStatusEnum.labelOrUnknown(entity.getApproveStatus())
                    + "」，未通过审批不能发血");
        }
        BizAdmission admission = inpatientService.getAdmissionById(entity.getAdmissionId());
        if (admission == null || !Objects.equals(ADMITTED, admission.getAdmitStatus())) {
            throw new BusinessException("该患者已不在院，不能发血");
        }

        LocalDateTime now = now();
        String note = dto.getIssueRemark();
        List<BizTransfusionBag> bags = bagMapper.selectByApply(entity.getId());
        for (BizTransfusionBag bag : bags) {
            bag.setBagStatus(BloodBagStatusEnum.ISSUED.getCode());
            bag.setIssueTime(now);
            if (StringUtils.hasText(note)) {
                bag.setRemark(mergeRemark(bag.getRemark(), note));
            }
            bagMapper.updateById(bag);
        }
        entity.setIssueDoctorId(operatorUser.getEmployeeId());
        entity.setIssueDoctorName(operatorUser.getRealName());
        entity.setIssueTime(now);
        entity.setTransfusionStatus(TransfusionStatusEnum.ISSUED.getCode());
        if (StringUtils.hasText(dto.getRemark())) {
            entity.setRemark(mergeRemark(entity.getRemark(), dto.getRemark()));
        }
        applyMapper.updateById(entity);
        log.info("发血 applyNo={} 血袋 {} 袋 发血人={}", entity.getApplyNo(), bags.size(), operatorUser.getRealName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startInfusion(TransfusionStartDTO dto) {
        BizTransfusionApply entity = mustGet(dto.getApplyId());
        if (!Objects.equals(TransfusionStatusEnum.ISSUED.getCode(), entity.getTransfusionStatus())) {
            throw new BusinessException("输血单 " + entity.getApplyNo() + " 当前状态为「"
                    + TransfusionStatusEnum.labelOrUnknown(entity.getTransfusionStatus())
                    + "」，只有「已发血」可以开始输注"
                    + (Objects.equals(TransfusionStatusEnum.CROSSMATCHED.getCode(), entity.getTransfusionStatus())
                    ? "（血还没发出来，输注的是什么无从追溯）" : ""));
        }
        if (Objects.equals(dto.getCheckNurseId(), dto.getCheckNurse2Id())) {
            throw new BusinessException("双人核对的两个护士不能是同一个人"
                    + "（写同一个人等于没有双人核对，这是飞检直接判不通过的一条）");
        }
        Set<Integer> codes;
        try {
            codes = TransfusionCheckItems.parse(dto.getCheckItems());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        List<String> missing = TransfusionCheckItems.missingRequired(codes);
        if (!missing.isEmpty()) {
            throw new BusinessException("输血前核对必核项未完成：" + String.join("；", missing)
                    + "。这 6 项少核任何一项，双人核对就不成立");
        }

        BizAdmission admission = inpatientService.getAdmissionById(entity.getAdmissionId());
        if (admission == null || !Objects.equals(ADMITTED, admission.getAdmitStatus())) {
            throw new BusinessException("该患者已不在院，不能开始输注");
        }

        LocalDateTime start = TimeUtil.toSeconds(dto.getInfusionStartTime());
        LocalDateTime now = now();
        String nurse1 = employeeNameOf(dto.getCheckNurseId());
        String nurse2 = employeeNameOf(dto.getCheckNurse2Id());

        List<BizTransfusionBag> bags = bagMapper.selectByApply(entity.getId());
        for (BizTransfusionBag bag : bags) {
            bag.setBagStatus(BloodBagStatusEnum.INFUSED.getCode());
            bagMapper.updateById(bag);
        }

        entity.setCheckItems(TransfusionCheckItems.serialize(codes));
        entity.setCheckNote(dto.getCheckNote());
        entity.setCheckNurseId(dto.getCheckNurseId());
        entity.setCheckNurseName(nurse1);
        entity.setCheckNurse2Id(dto.getCheckNurse2Id());
        entity.setCheckNurse2Name(nurse2);
        entity.setCheckTime(now);
        entity.setInfusionNurseId(dto.getInfusionNurseId() != null ? dto.getInfusionNurseId() : dto.getCheckNurseId());
        entity.setInfusionNurseName(employeeNameOf(entity.getInfusionNurseId()));
        entity.setInfusionStartTime(start);
        entity.setInfusionSpeed(dto.getInfusionSpeed());
        entity.setObservation(dto.getObservation());
        entity.setTransfusionStatus(TransfusionStatusEnum.INFUSING.getCode());
        if (StringUtils.hasText(dto.getRemark())) {
            entity.setRemark(mergeRemark(entity.getRemark(), dto.getRemark()));
        }
        applyMapper.updateById(entity);
        log.info("开始输注 applyNo={} 开始={} 双人核对={}/{} 核对项={} 执行护士={}",
                entity.getApplyNo(), start.format(DateFormats.DATETIME), nurse1, nurse2,
                entity.getCheckItems(), entity.getInfusionNurseName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(TransfusionFinishDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizTransfusionApply entity = mustGet(dto.getApplyId());
        if (Objects.equals(TransfusionStatusEnum.FINISHED.getCode(), entity.getTransfusionStatus())) {
            throw new BusinessException("输血单 " + entity.getApplyNo() + " 已完成，不能重复回写"
                    + "（重复执行会产生第二份输血记录）");
        }
        if (!Objects.equals(TransfusionStatusEnum.INFUSING.getCode(), entity.getTransfusionStatus())) {
            throw new BusinessException("输血单 " + entity.getApplyNo() + " 当前状态为「"
                    + TransfusionStatusEnum.labelOrUnknown(entity.getTransfusionStatus())
                    + "」，未开始输注不能登记完成");
        }
        LocalDateTime end = TimeUtil.toSeconds(dto.getInfusionEndTime());
        if (entity.getInfusionStartTime() != null && !end.isAfter(entity.getInfusionStartTime())) {
            throw new BusinessException("输注结束时间必须晚于开始时间（"
                    + entity.getInfusionStartTime().format(DateFormats.DATETIME) + "）");
        }
        if (dto.getActualAmount() == null || dto.getActualAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("实际输注量必须大于 0（输完了却不知道输了多少，记录等于没写）");
        }

        BizAdmission admission = inpatientService.getAdmissionById(entity.getAdmissionId());
        if (admission == null) {
            throw new BusinessException("入院记录不存在，无法回写（admissionId=" + entity.getAdmissionId() + "）");
        }
        if (!Objects.equals(ADMITTED, admission.getAdmitStatus())) {
            throw new BusinessException("该患者已出院，不能再登记输血完成（输血是住院期间发生的事件；"
                    + "出院后发现漏登记，请走病案质控缺陷流程补记）");
        }

        // 归档闸门：已归档的病案首页不能再改
        if (inpatientService.isSummaryArchived(entity.getAdmissionId())) {
            throw new BusinessException("该住院的病案首页已归档，不能再回写输血记录（归档数据不可改）");
        }

        LocalDateTime now = now();

        // 回写输血记录病历
        BizInpatientRecord record = writeBackRecord(entity, admission, dto, end, now);

        inpatientService.markSummaryTransfused(entity.getAdmissionId());

        entity.setInfusionEndTime(end);
        entity.setActualAmount(dto.getActualAmount());
        entity.setObservation(dto.getObservation());
        entity.setEfficacyEval(dto.getEfficacyEval());
        entity.setPostHb(dto.getPostHb());
        entity.setPostHct(dto.getPostHct());
        entity.setPostPlt(dto.getPostPlt());
        entity.setFinishDoctorId(operatorUser.getEmployeeId());
        entity.setFinishDoctorName(operatorUser.getRealName());
        entity.setFinishTime(now);
        entity.setRecordId(record.getId());
        entity.setTransfusionStatus(TransfusionStatusEnum.FINISHED.getCode());
        if (StringUtils.hasText(dto.getRemark())) {
            entity.setRemark(mergeRemark(entity.getRemark(), dto.getRemark()));
        }
        applyMapper.updateById(entity);

        log.info("输血完成 applyNo={} 输注 {}~{} 实际量={} 病历号={} 录入人={}",
                entity.getApplyNo(),
                entity.getInfusionStartTime() == null ? "-" : entity.getInfusionStartTime().format(DateFormats.DATETIME),
                end.format(DateFormats.DATETIME), dto.getActualAmount(), record.getRecordNo(), operatorUser.getRealName());
    }

    // 工具

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportReaction(TransfusionReactionDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizTransfusionApply entity = mustGet(dto.getApplyId());
        if (!Objects.equals(TransfusionStatusEnum.FINISHED.getCode(), entity.getTransfusionStatus())) {
            throw new BusinessException("输血单 " + entity.getApplyNo() + " 当前状态为「"
                    + TransfusionStatusEnum.labelOrUnknown(entity.getTransfusionStatus())
                    + "」，只有「已完成」的输血单可以补报输血反应"
                    + "（这样不会为了记反应而把已完成的单改回输注中 —— 那是改历史）");
        }
        if (Objects.equals(1, entity.getHasReaction())) {
            throw new BusinessException("输血单 " + entity.getApplyNo() + " 已上报过输血反应（"
                    + entity.getReactionType() + "），不能重复上报");
        }

        entity.setHasReaction(1);
        entity.setReactionType(dto.getReactionType());
        entity.setReactionDesc(dto.getReactionDesc());
        entity.setReactionHandle(dto.getReactionHandle());
        entity.setReactionReporterId(operatorUser.getEmployeeId());
        entity.setReactionReporterName(operatorUser.getRealName());
        entity.setReactionTime(now());
        if (StringUtils.hasText(dto.getRemark())) {
            entity.setRemark(mergeRemark(entity.getRemark(), dto.getRemark()));
        }
        applyMapper.updateById(entity);
        log.warn("输血反应上报 applyNo={} admissionId={} 类型={} 上报人={}",
                entity.getApplyNo(), entity.getAdmissionId(), dto.getReactionType(), operatorUser.getRealName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(TransfusionCancelDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizTransfusionApply entity = mustGet(dto.getApplyId());
        if (Objects.equals(TransfusionStatusEnum.CANCELLED.getCode(), entity.getTransfusionStatus())) {
            throw new BusinessException("输血单 " + entity.getApplyNo() + " 已取消，不能重复取消");
        }
        if (Objects.equals(TransfusionStatusEnum.FINISHED.getCode(), entity.getTransfusionStatus())) {
            throw new BusinessException("输血单 " + entity.getApplyNo()
                    + " 已完成，不能取消；已完成的输血已经写进病历与首页，取消它就是销毁证据");
        }
        if (Objects.equals(TransfusionStatusEnum.INFUSING.getCode(), entity.getTransfusionStatus())) {
            throw new BusinessException("输血单 " + entity.getApplyNo()
                    + " 正在输注中，不能取消（血已经进入患者体内）；如出现不良反应请登记完成并上报反应");
        }
        entity.setTransfusionStatus(TransfusionStatusEnum.CANCELLED.getCode());
        entity.setCancelReason(dto.getCancelReason());
        entity.setCancelDoctorId(operatorUser.getEmployeeId());
        entity.setCancelDoctorName(operatorUser.getRealName());
        entity.setCancelTime(now());
        applyMapper.updateById(entity);
        log.info("取消输血申请 applyNo={} 原因={} 操作人={}",
                entity.getApplyNo(), dto.getCancelReason(), operatorUser.getRealName());
    }

    /**
     * 把这次输血回写成一份住院病历（类型=输血记录，状态直接「已提交」）。
     *
     * <p>签名的医生是<b>申请输血的经治医师</b>，不是录入人 —— 用"谁点的按钮"当签名，
     * 会让病历里的医师签名与病案首页打架（同手术记录用主刀签名的道理）。
     *
     * <p>结构化要素按 {@code RecordStructuredFields.TRANSFUSION_ELEMENTS} 那三项写：
     * 输血成分与量（remark）、输血经过（course_note）、疗效评估与反应处理（treatment_plan）。
     * 这三项必然同时写下，所以系统回写的输血记录结构化率是 100% —— 这是事实，不是凑分。
     */
    private BizInpatientRecord writeBackRecord(BizTransfusionApply entity, BizAdmission admission,
                                               TransfusionFinishDTO dto,
                                               LocalDateTime end, LocalDateTime now) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizPatient patient = bizPatientService.getById(admission.getPatientId());
        if (patient == null) {
            throw new BusinessException("患者不存在，无法回写输血记录");
        }
        List<BizTransfusionBag> bags = bagMapper.selectByApply(entity.getId());

        StringBuilder course = new StringBuilder();
        course.append("输血成分：")
                .append(BloodComponentEnum.labelOrUnknown(entity.getBloodComponent()));
        if (StringUtils.hasText(entity.getComponentSpec())) {
            course.append("（").append(entity.getComponentSpec()).append("）");
        }
        course.append("，共 ").append(bags.size()).append(" 袋\n");
        course.append("受血者血型：")
                .append(TransfusionRules.bloodTypeText(entity.getPatientAbo(), entity.getPatientRh()))
                .append('\n');
        if (!bags.isEmpty()) {
            course.append("血袋：");
            List<String> nos = new ArrayList<>();
            for (BizTransfusionBag b : bags) {
                nos.add(b.getBagNo() + "（"
                        + TransfusionRules.bloodTypeText(b.getBagAbo(), b.getBagRh()) + "）");
            }
            course.append(String.join("、", nos)).append('\n');
        }
        if (entity.getInfusionStartTime() != null) {
            long minutes = Math.max(0, Duration.between(entity.getInfusionStartTime(), end).toMinutes());
            course.append("输注时间：").append(entity.getInfusionStartTime().format(DateFormats.DATETIME))
                    .append(" ~ ").append(end.format(DateFormats.DATETIME))
                    .append("（").append(TransfusionRules.durationText(minutes)).append("）\n");
        }
        if (StringUtils.hasText(entity.getInfusionSpeed())) {
            course.append("滴速：").append(entity.getInfusionSpeed()).append('\n');
        }
        course.append("实际输注量：").append(dto.getActualAmount())
                .append(StringUtils.hasText(entity.getAmountUnit()) ? " " + entity.getAmountUnit() : "")
                .append('\n');
        if (StringUtils.hasText(entity.getCheckNurseName())) {
            course.append("双人核对：").append(entity.getCheckNurseName())
                    .append("、").append(textOr(entity.getCheckNurse2Name(), "未记录"))
                    .append("（").append(TransfusionCheckItems.summaryText(entity.getCheckItems())).append("）\n");
        }
        if (StringUtils.hasText(entity.getTransfusionPurpose())) {
            course.append("输血目的：").append(entity.getTransfusionPurpose()).append('\n');
        }
        course.append("输注过程观察：").append(dto.getObservation());

        StringBuilder plan = new StringBuilder();
        plan.append("疗效评估：").append(textOr(dto.getEfficacyEval(), "未填写（见复查指标）"));
        if (dto.getPostHb() != null || dto.getPostHct() != null || dto.getPostPlt() != null) {
            plan.append("\n输血后复查：");
            if (dto.getPostHb() != null) {
                plan.append("Hb ").append(dto.getPostHb()).append(" g/L ");
            }
            if (dto.getPostHct() != null) {
                plan.append("HCT ").append(dto.getPostHct()).append("% ");
            }
            if (dto.getPostPlt() != null) {
                plan.append("PLT ").append(dto.getPostPlt()).append("×10^9/L");
            }
        }
        if (Objects.equals(1, entity.getHasReaction())) {
            plan.append("\n输血反应：").append(entity.getReactionType())
                    .append("；").append(textOr(entity.getReactionDesc(), ""))
                    .append("；处理：").append(textOr(entity.getReactionHandle(), ""));
        } else {
            plan.append("\n输血反应：无（上报时限内未出现不良反应）");
        }

        BizInpatientRecord record = new BizInpatientRecord();
        record.setAdmissionId(admission.getAdmissionId());
        record.setPatientId(admission.getPatientId());
        record.setPatientNo(patient.getPatientNo());
        record.setPatientName(patient.getPatientName());
        record.setGender(patient.getGender());
        record.setAge(patient.getAge());
        record.setAgeUnit(1);
        record.setDeptId(admission.getDeptId());
        record.setDeptName(deptNameOf(admission.getDeptId()));
        record.setWardId(admission.getWardId());
        record.setWardName(wardNameOf(admission.getWardId()));
        record.setBedNo(bedNoOf(admission.getBedId()));
        record.setRecordType(RECORD_TYPE_TRANSFUSION);
        record.setRecordTitle("输血记录");
        record.setRecordTime(end);
        record.setCourseNote(course.toString());
        record.setTreatmentPlan(plan.toString());
        // remark 承载"输血成分与量"这个结构化要素（同时保留来源单号，便于倒查）
        record.setRemark("系统回写：输血申请单号 " + entity.getApplyNo()
                + "，输血成分 " + BloodComponentEnum.getText(entity.getBloodComponent())
                + "，共 " + bags.size() + " 袋"
                + (entity.getPlannedAmount() == null ? "" :
                "，申请总量 " + entity.getPlannedAmount()
                        + textOr(entity.getAmountUnit(), ""))
                + "，受血者血型 "
                + TransfusionRules.bloodTypeText(entity.getPatientAbo(), entity.getPatientRh())
                + (Objects.equals(1, entity.getIsEmergency()) ? "，紧急用血" : ""));
        record.setRecordStatus(RECORD_STATUS_SUBMITTED);
        // 签名 = 申请输血的经治医师；缺失才回落到录入人（宁可记"谁录的"，也不留空签名）
        record.setDoctorId(entity.getApplyDoctorId() != null ? entity.getApplyDoctorId() : operatorUser.getEmployeeId());
        record.setDoctorName(StringUtils.hasText(entity.getApplyDoctorName())
                ? entity.getApplyDoctorName() : operatorUser.getRealName());
        record.setSubmitTime(now);
        // 病历号取号与落库归病历文书的写入方（输血侧只负责把这次输血写成文书内容）
        return inpatientRecordService.appendClosedLoopRecord(record);
    }

    /**
     * @param withBags 是否加载血袋明细（详情接口 true、列表 false —— 列表逐行查血袋就是 N+1）
     */
    private void decorate(TransfusionApplyVO vo, boolean withBags) {
        vo.setTransfusionStatusText(TransfusionStatusEnum.getText(vo.getTransfusionStatus()));
        vo.setCrossmatchStatusText(TransfusionCrossmatchStatusEnum.getText(vo.getCrossmatchStatus()));
        vo.setBloodComponentText(BloodComponentEnum.getText(vo.getBloodComponent()));
        vo.setBloodTypeText(TransfusionRules.bloodTypeText(vo.getPatientAbo(), vo.getPatientRh()));
        vo.setIsEmergencyText(vo.getIsEmergency() == null ? "—" : (vo.getIsEmergency() == 1 ? "紧急" : "常规"));
        vo.setGenderText(SysGenderEnum.getText(vo.getGender()));
        vo.setAdmitStatusText(AdmitStatusEnum.getText(vo.getAdmitStatus()));
        vo.setHasReactionText(Objects.equals(1, vo.getHasReaction()) ? "有反应（已上报）" : "未上报反应");
        vo.setApproveStatusText(TransfusionApproveStatusEnum.getText(vo.getApproveStatus()));
        vo.setApproveLevelText(dictCacheService.getDicDataLabel("biz_medicaltech_transfusionApproveLevelEnum", vo.getApproveLevel()));
        vo.setCheckItemsText(TransfusionCheckItems.summaryText(vo.getCheckItems()));
        vo.setCheckItemOptions(checkItems());
        vo.setReactionTypeOptions(TransfusionReactionTypeEnum.options());

        // 已配袋数来自投影里的子查询（列表逐行查血袋就是 N+1）
        int matched = vo.getMatchedBagCount() == null ? 0 : vo.getMatchedBagCount();
        vo.setMatchedBagCount(matched);
        if (vo.getBagCount() == null) {
            vo.setBagProgressText("—");
        } else if (Objects.equals(TransfusionCrossmatchStatusEnum.INCOMPATIBLE.getCode(), vo.getCrossmatchStatus())) {
            vo.setBagProgressText("已配 " + matched + "/" + vo.getBagCount() + " 袋，存在配血不合");
        } else if (matched >= vo.getBagCount()) {
            vo.setBagProgressText("全部相合 " + matched + "/" + vo.getBagCount());
        } else {
            vo.setBagProgressText("已配 " + matched + "/" + vo.getBagCount() + " 袋");
        }

        boolean pending = Objects.equals(TransfusionStatusEnum.PENDING_CROSSMATCH.getCode(), vo.getTransfusionStatus());
        boolean crossmatched = Objects.equals(TransfusionStatusEnum.CROSSMATCHED.getCode(), vo.getTransfusionStatus());
        boolean issued = Objects.equals(TransfusionStatusEnum.ISSUED.getCode(), vo.getTransfusionStatus());
        boolean infusing = Objects.equals(TransfusionStatusEnum.INFUSING.getCode(), vo.getTransfusionStatus());
        boolean finished = Objects.equals(TransfusionStatusEnum.FINISHED.getCode(), vo.getTransfusionStatus());

        vo.setCanEdit(pending);
        vo.setCanCrossmatch(pending);
        boolean approvable = pending
                && (Objects.equals(TransfusionApproveStatusEnum.PENDING.getCode(), vo.getApproveStatus())
                || Objects.equals(TransfusionApproveStatusEnum.MAKEUP_PENDING.getCode(), vo.getApproveStatus()));
        vo.setCanApprove(approvable);
        vo.setCanIssue(crossmatched
                && Objects.equals(TransfusionCrossmatchStatusEnum.ALL_MATCHED.getCode(), vo.getCrossmatchStatus()));
        vo.setCanStart(issued);
        vo.setCanFinish(infusing);
        // 输注中(3)与已完成(4)不可取消：血已经进入患者体内，取消它是销毁证据
        vo.setCanCancel(pending || crossmatched || issued);
        vo.setCanReportReaction(finished && !Objects.equals(1, vo.getHasReaction()));

        if (vo.getInfusionStartTime() != null && vo.getInfusionEndTime() != null) {
            long minutes = Math.max(0, Duration.between(
                    vo.getInfusionStartTime(), vo.getInfusionEndTime()).toMinutes());
            vo.setDurationMinutes(minutes);
            vo.setDurationText(TransfusionRules.durationText(minutes));
        }
        vo.setWaitText(waitText(vo, pending, crossmatched, issued, infusing));

        LocalDateTime now = now();
        boolean stalled = false;
        String stalledText = null;
        if (crossmatched && vo.getCrossmatchTime() != null
                && now.isAfter(vo.getCrossmatchTime().plusHours(STALLED_HOURS))) {
            stalled = true;
            stalledText = "配血完成已超过 " + STALLED_HOURS + " 小时仍未发血";
        }
        if (issued && vo.getIssueTime() != null
                && now.isAfter(vo.getIssueTime().plusHours(STALLED_HOURS))) {
            stalled = true;
            stalledText = "发血已超过 " + STALLED_HOURS + " 小时仍未输注";
        }
        if (finished && vo.getRecordId() == null) {
            // 链断了的假数据：状态"已完成"却没有病历锚点。必须能看见，不允许静默。
            stalled = true;
            stalledText = "已完成但回写链不完整（病历ID 缺失），请核查";
        }
        vo.setStalled(stalled);
        vo.setStalledText(stalledText);

        if (withBags) {
            List<TransfusionBagVO> bagVOs = new ArrayList<>();
            for (BizTransfusionBag b : bagMapper.selectByApply(vo.getId())) {
                bagVOs.add(toBagVO(b));
            }
            vo.setBags(bagVOs);
        }
    }

    private TransfusionBagVO toBagVO(BizTransfusionBag b) {
        TransfusionBagVO vo = new TransfusionBagVO();
        vo.setId(b.getId());
        vo.setApplyId(b.getApplyId());
        vo.setBagNo(b.getBagNo());
        vo.setDonorNo(b.getDonorNo());
        vo.setBagAbo(b.getBagAbo());
        vo.setBagRh(b.getBagRh());
        vo.setBloodTypeText(TransfusionRules.bloodTypeText(b.getBagAbo(), b.getBagRh()));
        vo.setBloodComponent(b.getBloodComponent());
        vo.setBloodComponentText(BloodComponentEnum.getText(b.getBloodComponent()));
        vo.setSpec(b.getSpec());
        vo.setAmount(b.getAmount());
        vo.setAmountUnit(b.getAmountUnit());
        vo.setSourceBank(b.getSourceBank());
        vo.setCollectDate(b.getCollectDate());
        vo.setExpireDate(b.getExpireDate());
        // 过期与否是"查询时算"的：血袋躺着也会过期，落一个布尔值进库第二天就是错的
        vo.setExpired(b.getExpireDate() != null && b.getExpireDate().isBefore(LocalDate.now()));
        vo.setCrossmatchMain(b.getCrossmatchMain());
        vo.setCrossmatchSide(b.getCrossmatchSide());
        vo.setCrossmatchResult(b.getCrossmatchResult());
        vo.setCrossmatchResultText(CrossmatchResultEnum.getText(b.getCrossmatchResult()));
        vo.setCrossmatchTime(b.getCrossmatchTime());
        vo.setCrossmatchDoctorName(b.getCrossmatchDoctorName());
        vo.setBagStatus(b.getBagStatus());
        vo.setBagStatusText(BloodBagStatusEnum.getText(b.getBagStatus()));
        vo.setIssueTime(b.getIssueTime());
        vo.setRemark(b.getRemark());
        return vo;
    }

    private String waitText(TransfusionApplyVO vo, boolean pending, boolean crossmatched,
                            boolean issued, boolean infusing) {
        LocalDateTime now = now();
        if (pending && vo.getApplyTime() != null) {
            long m = Math.max(0, Duration.between(vo.getApplyTime(), now).toMinutes());
            return "申请后已等待 " + TransfusionRules.durationText(m) + " 未完成配血";
        }
        if (crossmatched && vo.getCrossmatchTime() != null) {
            return "配血完成已 " + TransfusionRules.durationText(
                    Duration.between(vo.getCrossmatchTime(), now).toMinutes()) + "，尚未发血";
        }
        if (issued && vo.getIssueTime() != null) {
            return "已发血 " + TransfusionRules.durationText(
                    Duration.between(vo.getIssueTime(), now).toMinutes()) + "，尚未开始输注";
        }
        if (infusing && vo.getInfusionStartTime() != null) {
            return "输注中，已进行 " + TransfusionRules.durationText(
                    Duration.between(vo.getInfusionStartTime(), now).toMinutes());
        }
        return null;
    }

    private BizTransfusionApply mustGet(Long applyId) {
        BizTransfusionApply entity = applyMapper.selectById(applyId);
        if (entity == null) {
            throw new BusinessException("输血申请单不存在");
        }
        return entity;
    }

    /**
     * 科室名（取不到就返回"未知科室(ID=x)"，绝不编一个科室名）
     */
    private String deptNameOf(Long deptId) {
        if (deptId == null) {
            return null;
        }
        String name = applyMapper.selectDeptName(deptId);
        return StringUtils.hasText(name) ? name : "未知科室(ID=" + deptId + ")";
    }

    private String wardNameOf(Long wardId) {
        if (wardId == null) {
            return null;
        }
        WardVO ward = inpatientService.getWardById(wardId);
        return ward == null ? "未知病区(ID=" + wardId + ")" : ward.getWardName();
    }

    private String bedNoOf(Long bedId) {
        if (bedId == null) {
            return null;
        }
        return inpatientService.getBedNoById(bedId);
    }

    /**
     * 员工姓名（服务端查名，不信任前端传来的姓名 —— 姓名是可以随便伪造的字符串）
     */
    private String employeeNameOf(Long empId) {
        if (empId == null) {
            return null;
        }
        String name = applyMapper.selectEmployeeName(empId);
        return StringUtils.hasText(name) ? name : "未知员工(ID=" + empId + ")";
    }

    /**
     * 时间统一截到秒，保证「写进去的 = 读回来的」（库表是 DATETIME(0)，MySQL 会四舍五入）
     */

    private String nextApplyNo() {
        String prefix = "SX" + LocalDate.now().format(DateFormats.COMPACT_DATE);
        long seq = applyMapper.countByNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    /**
     * 留痕一律用**员工ID**（不是用户的ID），与医嘱/站内信/手术同一口径
     */
    private record Parsed(String from, String to) {
    }
}