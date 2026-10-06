package com.his.patient.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.util.TimeUtil;
import com.his.common.base.PageResult;
import com.his.common.service.RedisSequenceService;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.patient.dto.DeathCertificateDTO;
import com.his.patient.entity.BizDeathCertificate;
import com.his.patient.entity.BizDeathCertificateCause;
import com.his.patient.enums.DeathCausePartEnum;
import com.his.patient.enums.DeathCertReportEnum;
import com.his.patient.enums.DeathCertStatusEnum;
import com.his.patient.enums.DeathPlaceEnum;
import com.his.patient.mapper.BizDeathCertificateCauseMapper;
import com.his.patient.mapper.BizDeathCertificateMapper;
import com.his.patient.service.DeathCertificateService;
import com.his.patient.vo.DeathCertificateVO;
import com.his.system.utils.UserUtils;
import com.his.system.enums.BizTypeEnum;
import com.his.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 死亡证明服务实现。
 *
 * <p>口径（与建表注释同源，sql/157）：
 * <ol>
 *   <li><b>死亡事实的唯一来源是出院办理</b>：本服务不写出院，只读出院记录的死亡标记=1。
 *       填表可以贴着死亡当时写（草稿/已审核），但<b>签发</b>是对外法定凭证，签发时该住院必须已办死亡离院，
 *       且证明的死亡时间与出院时间是同一时点（不许两处各编一个时间）。
 *       反向约束在出院流程里（{@code InpatientServiceImpl.discharge} 调
 *       {@link #assertDischargeConsistent}）。</li>
 *   <li><b>不做「未开证不许出院」的硬拦</b>：现实中家属常数日后才来办证、死因要等尸检报告，
 *       硬拦等于把出院闭环打死。改为待开证榜 + 上报时限催报（同传染病报卡口径）。</li>
 *   <li>一次住院只允许一张<b>有效</b>证明（草稿/已审核/已开具都算占用）；改错走「作废原证 + 重开新证」，
 *       新证 {@code orig_cert_id} 指向原证，原证内容与死因链一律不改（法定文书留痕）。</li>
 *   <li>一般项目（姓名/性别/民族/出生日期/身份证/职业/婚姻/科室病区床位）全部服务端按住院重查快照，
 *       不采信前端——证明要能脱离患者档案独立回看，也不能被前端改名换号。</li>
 *   <li>根本死因必须等于死因链Ⅰ部分链尾那行（ICD-10 死因链选择规则的最低要求），写入时服务端校验。</li>
 *   <li>死因监测上报：当前<b>不对接外部平台</b>，「上报」= 组装标准报文落库留痕，
 *       真实对接时 {@link #report} 是唯一替换点（换成 http 客户端 + 回执落库），接口面与报文结构不变。</li>
 *   <li>上报时限 report_deadline = 死亡时间 + {@value #REPORT_DEADLINE_DAYS} 日，
 *       是<b>院内自定口径</b>（真实时限以属地要求为准），只用于欠账榜与催报排序，换口径只改这一个常量。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeathCertificateServiceImpl implements DeathCertificateService {

    /**
     * 上报时限天数（院内口径，见类注释第 7 条）
     */
    private static final int REPORT_DEADLINE_DAYS = 10;

    private static final int DIAG_MAX = 500;
    private static final int ICD_CODE_MAX = 32;
    private static final int ICD_NAME_MAX = 200;
    private static final int INTERVAL_MAX = 50;
    private static final int REASON_MAX = 500;
    private static final int UNIT_MAX = 100;

    /**
     * 死因链Ⅰ部分最多 (a)(b)(c)(d) 四行
     */
    private static final int CHAIN_MAX_ROWS = 4;

    /**
     * 单次催报扫描上限（同传染病报卡，防止一次拉爆内存）
     */
    private static final int NOTIFY_BATCH = 200;

    /**
     * 上报报文里的时间统一空格分隔（全项目入参与展示同一口径，不留 ISO 的 T 分隔去二次转义）
     */
    private static final DateTimeFormatter PAYLOAD_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BizDeathCertificateMapper certMapper;
    private final BizDeathCertificateCauseMapper causeMapper;
    private final RedisSequenceService redisSequenceService;
    private final SysMessageService sysMessageService;

    // 查询

    private static boolean editable(Integer status) {
        return Objects.equals(status, DeathCertStatusEnum.DRAFT.getCode())
                || Objects.equals(status, DeathCertStatusEnum.AUDITED.getCode());
    }

    private static String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return DeathCertStatusEnum.labelOrUnknown(status);
    }

    /**
     * 死因链入参校验：part 只能是 1/2，行序不能重复，Ⅰ部分最多 4 行且必须从 1 连续。
     * 全部在服务端拦——撞 uk_cert_part_seq 会直接 Duplicate entry 变成 500，用户看不出是哪里填错。
     */
    private static List<DeathCertificateDTO.CauseRow> normalizeCauses(List<DeathCertificateDTO.CauseRow> input) {
        List<DeathCertificateDTO.CauseRow> rows = input == null ? List.of() : input;
        if (rows.size() > 30) {
            throw new BusinessException("死因链行数过多（Ⅰ部分最多 4 行，Ⅱ部分按需填写）");
        }
        List<DeathCertificateDTO.CauseRow> chain = new ArrayList<>();
        List<DeathCertificateDTO.CauseRow> other = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        for (DeathCertificateDTO.CauseRow row : rows) {
            Integer part = row.getPart() == null ? DeathCausePartEnum.CHAIN.getCode() : row.getPart();
            if (part != DeathCausePartEnum.CHAIN.getCode() && part != DeathCausePartEnum.OTHER.getCode()) {
                throw new BusinessException("死因链部分只能是Ⅰ部分或Ⅱ部分");
            }
            if (row.getSeqNo() == null || row.getSeqNo() < 1) {
                throw new BusinessException("死因链行序必须从 1 开始");
            }
            if (!keys.add(part + ":" + row.getSeqNo())) {
                throw new BusinessException("死因链同一部分里行序重复（" + part + "-" + row.getSeqNo() + "）");
            }
            (part == DeathCausePartEnum.CHAIN.getCode() ? chain : other).add(row);
        }
        if (chain.size() > CHAIN_MAX_ROWS) {
            throw new BusinessException("死因链Ⅰ部分最多 (a)(b)(c)(d) 四行");
        }
        for (DeathCertificateDTO.CauseRow row : chain) {
            if (row.getSeqNo() > chain.size()) {
                throw new BusinessException("死因链Ⅰ部分行序必须连续（a→b→c→d），不能空跳");
            }
        }
        List<DeathCertificateDTO.CauseRow> sorted = new ArrayList<>(chain);
        sorted.sort((a, b) -> Integer.compare(a.getSeqNo(), b.getSeqNo()));
        sorted.addAll(other);
        return sorted;
    }

    /**
     * 根本死因＝死因链Ⅰ部分链尾那行（ICD-10 选择规则）。
     * 前端没填就自动按链尾带出；填了不一致直接拒——这一列是死因统计的唯一归口，容不得各写一套。
     */
    private static void applyUnderlying(BizDeathCertificate cert, List<DeathCertificateDTO.CauseRow> chainTailFirst) {
        DeathCertificateDTO.CauseRow tail = null;
        for (DeathCertificateDTO.CauseRow row : chainTailFirst) {
            if (Objects.equals(row.getPart(), DeathCausePartEnum.CHAIN.getCode())) {
                tail = row;
            }
        }
        if (tail == null) {
            return;
        }
        String tailCode = trimToNull(tail.getIcdCode());
        if (!StringUtils.hasText(cert.getUnderlyingIcdCode())) {
            cert.setUnderlyingIcdCode(cutToNull(tailCode, ICD_CODE_MAX));
            cert.setUnderlyingIcdName(cutToNull(tail.getIcdName(), ICD_NAME_MAX));
            return;
        }
        if (tailCode != null && !tailCode.equals(cert.getUnderlyingIcdCode())) {
            throw new BusinessException("根本死因必须等于死因链Ⅰ部分最后一行（链尾是 " + tailCode
                    + " " + tail.getIcdName() + "）；若链填错了请调整顺序，不要把根本死因单独改成别的编码");
        }
        cert.setUnderlyingIcdName(cutToNull(tail.getIcdName(), ICD_NAME_MAX));
    }

    /**
     * 时限派生值由服务端算好，前端只渲染不自己比时间
     */
    private static void fillDeadline(DeathCertificateVO.Row row) {
        LocalDateTime deadline = row.getReportDeadline();
        boolean issued = Objects.equals(row.getCertStatus(), DeathCertStatusEnum.ISSUED.getCode());
        boolean reported = Objects.equals(row.getReportStatus(), DeathCertReportEnum.DONE.getCode());
        row.setOverdue(issued && !reported && deadline != null && deadline.isBefore(LocalDateTime.now()));
        row.setRemainHours(deadline == null ? null : ChronoUnit.HOURS.between(LocalDateTime.now(), deadline));
    }

    // 填写 / 审核 / 签发 / 作废 / 重开 / 打印

    /**
     * 时间序列化成报文体里的固定格式，null 保持 null（不写 "null" 字符串）
     */
    private static String ts(LocalDateTime time) {
        return time == null ? null : time.format(PAYLOAD_TS);
    }

    private static String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    private static Integer ageAt(LocalDate birthDate, LocalDateTime deathTime) {
        if (birthDate == null || deathTime == null) {
            return null;
        }
        int years = Period.between(birthDate, deathTime.toLocalDate()).getYears();
        return years < 0 ? null : years;
    }

    private static String requireText(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new BusinessException(message);
        }
        return text.trim();
    }

    private static LocalDateTime atStart(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    /**
     * 按日期过滤必须补全天边界：datetime 恒大于当日 00:00:00 字符串，直接用 'yyyy-MM-dd' 会把当天全滤掉
     */
    private static LocalDateTime atEnd(LocalDate date) {
        return date == null ? null : date.atTime(23, 59, 59);
    }

    // 死因监测上报（外发段预留：当前组装报文落库留痕）

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    // 出院流程前置校验（反向约束）

    private static Integer flag(Integer value) {
        return value == null ? 0 : (Objects.equals(value, 1) ? 1 : 0);
    }

    // 内部

    private static int nvl(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private static String trimToNull(String text) {
        return StringUtils.hasText(text) ? text.trim() : null;
    }

    private static String cutToNull(String text, int max) {
        return StringUtils.hasText(text) ? cut(text.trim(), max) : null;
    }

    private static String cut(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }

    @Override
    public PageResult<DeathCertificateVO.Row> listPage(DeathCertificateDTO.QueryPage query) {
        DeathCertificateDTO.QueryPage q = query == null ? new DeathCertificateDTO.QueryPage() : query;
        Page<DeathCertificateVO.Row> page = new Page<>(nvl(q.getPageNum(), 1), nvl(q.getPageSize(), 10));
        List<DeathCertificateVO.Row> records = certMapper.selectCertPage(page, trimToNull(q.getKeyword()),
                q.getCertStatus(), q.getReportStatus(), q.getDeathPlace(), q.getDeathDeptId(),
                atStart(q.getStartDate()), atEnd(q.getEndDate()), q.getOverdue());
        records.forEach(DeathCertificateServiceImpl::fillDeadline);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public PageResult<DeathCertificateVO.PendingRow> pendingListPage(DeathCertificateDTO.QueryPage query) {
        DeathCertificateDTO.QueryPage q = query == null ? new DeathCertificateDTO.QueryPage() : query;
        Page<DeathCertificateVO.PendingRow> page = new Page<>(nvl(q.getPageNum(), 1), nvl(q.getPageSize(), 10));
        List<DeathCertificateVO.PendingRow> records = certMapper.selectPendingPage(page,
                trimToNull(q.getKeyword()), atStart(q.getStartDate()), atEnd(q.getEndDate()));
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public DeathCertificateVO.Detail getDetailById(Long id) {
        DeathCertificateVO.Detail detail = certMapper.selectCertDetail(id);
        if (detail == null) {
            throw new BusinessException("死亡证明不存在或已删除");
        }
        detail.setCauses(causeMapper.selectByCertId(id));
        return detail;
    }

    @Override
    public DeathCertificateVO.PatientSnapshot admissionBase(Long admissionId) {
        DeathCertificateVO.PatientSnapshot snapshot = certMapper.selectPatientSnapshot(admissionId);
        if (snapshot == null) {
            throw new BusinessException("住院记录不存在");
        }
        return snapshot;
    }

    @Override
    public DeathCertificateVO.Stats stats() {
        DeathCertificateVO.Stats stats = certMapper.selectStats();
        return stats == null ? new DeathCertificateVO.Stats() : stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(DeathCertificateDTO.Upsert dto) {
        DeathCertificateVO.PatientSnapshot snapshot = certMapper.selectPatientSnapshot(dto.getAdmissionId());
        if (snapshot == null) {
            throw new BusinessException("住院记录不存在");
        }
        if (dto.getDeathTime().isAfter(now())) {
            throw new BusinessException("死亡时间不能晚于当前时间");
        }
        if (snapshot.getAdmitTime() != null && dto.getDeathTime().isBefore(TimeUtil.toSeconds(snapshot.getAdmitTime()))) {
            throw new BusinessException("死亡时间不能早于入院时间");
        }
        Integer deathPlace = dto.getDeathPlace();
        if (Objects.equals(snapshot.getAdmitStatus(), AdmitStatusEnum.IN_HOSPITAL.getCode()) && deathPlace != DeathPlaceEnum.HOSPITAL.getCode()) {
            throw new BusinessException("患者仍在院，死亡地点必须为「医院」");
        }

        BizDeathCertificate cert;
        boolean isNew = dto.getId() == null;
        if (isNew) {
            if (certMapper.countActiveByAdmission(dto.getAdmissionId(), null) > 0) {
                throw new BusinessException("该次住院已有死亡证明（一张住院只允许一张有效证明，改错请作废后重开）");
            }
            cert = new BizDeathCertificate();
            cert.setCertNo(redisSequenceService.generateDeathCertNo());
            cert.setAdmissionId(dto.getAdmissionId());
            cert.setCertStatus(DeathCertStatusEnum.DRAFT.getCode());
            cert.setReportStatus(DeathCertReportEnum.NONE.getCode());
            cert.setPrintCount(0);
            cert.setOrigCertId(null);
        } else {
            cert = requireCert(dto.getId());
            if (!Objects.equals(cert.getAdmissionId(), dto.getAdmissionId())) {
                throw new BusinessException("死亡证明不允许改挂到另一次住院");
            }
            if (!editable(cert.getCertStatus())) {
                throw new BusinessException(statusText(cert.getCertStatus()) + "的证明不能修改，改错请作废后重开");
            }
            if (certMapper.countActiveByAdmission(dto.getAdmissionId(), cert.getId()) > 0) {
                throw new BusinessException("该次住院已有另一张死亡证明");
            }
        }

        // 一般项目一律取快照，不采信前端
        cert.setPatientId(snapshot.getPatientId());
        cert.setPatientName(snapshot.getPatientName());
        cert.setGender(snapshot.getGender());
        cert.setNation(cutToNull(snapshot.getNation(), UNIT_MAX));
        cert.setBirthDate(snapshot.getBirthDate());
        cert.setAge(ageAt(snapshot.getBirthDate(), dto.getDeathTime()));
        cert.setIdCard(cutToNull(snapshot.getIdCard(), 18));
        cert.setOccupation(cutToNull(snapshot.getOccupation(), UNIT_MAX));
        cert.setMaritalStatus(snapshot.getMaritalStatus());
        cert.setDeathTime(TimeUtil.toSeconds(dto.getDeathTime()));
        cert.setDeathPlace(deathPlace);
        applyDeathDept(cert, deathPlace, dto.getDeathDeptId(), snapshot);
        cert.setClinicalDiagnosis(cut(trimToNull(dto.getClinicalDiagnosis()), DIAG_MAX));
        // 根本死因先接住前端值，再由 applyUnderlying 按链尾校验/带出。
        // 不在这里落地，编辑时 cert 上挂着的是库里的旧值，改完死因链就永远对不上链尾（改一次报一次错）。
        cert.setUnderlyingIcdCode(cutToNull(dto.getUnderlyingIcdCode(), ICD_CODE_MAX));
        cert.setUnderlyingIcdName(cutToNull(dto.getUnderlyingIcdName(), ICD_NAME_MAX));
        cert.setPastHistory(cutToNull(dto.getPastHistory(), DIAG_MAX));
        cert.setAutopsyFlag(flag(dto.getAutopsyFlag()));
        cert.setAutopsyResult(cutToNull(dto.getAutopsyResult(), DIAG_MAX));
        cert.setRelativeName(cutToNull(dto.getRelativeName(), 50));
        cert.setRelativeRelation(cutToNull(dto.getRelativeRelation(), 20));
        cert.setRelativePhone(cutToNull(dto.getRelativePhone(), 20));
        cert.setPhysicianId(dto.getPhysicianId() != null ? dto.getPhysicianId() : UserUtils.getCurrentUser().getEmployeeId());
        // ②非web入口口径：校验对象是「入参姓名 或 当前登录人」的合并值，不是纯 DTO 字段，注解表达不了
        cert.setPhysicianName(cut(requireText(
                StringUtils.hasText(dto.getPhysicianName()) ? dto.getPhysicianName() : UserUtils.getCurrentUser().getRealName(),
                "填表医师不能为空"), 50));
        cert.setFillTime(dto.getFillTime() != null ? TimeUtil.toSeconds(dto.getFillTime()) : (isNew ? now() : cert.getFillTime()));
        cert.setRemark(cutToNull(dto.getRemark(), DIAG_MAX));
        // 时限起算点是死亡时间，每次保存都按最新死亡时间重算（改了死亡时间时限必须跟着走）
        cert.setReportDeadline(cert.getDeathTime().plusDays(REPORT_DEADLINE_DAYS));

        List<DeathCertificateDTO.CauseRow> causes = normalizeCauses(dto.getCauses());
        applyUnderlying(cert, causes);

        saveCert(cert);

        // 死因链整体替换：本表无 del_flag，唯一键不含 del_flag，必须先物理删再插（软删会占键）
        causeMapper.purgeByCertId(cert.getId());
        String operator = currentOperator();
        LocalDateTime createTime = now();
        for (DeathCertificateDTO.CauseRow row : causes) {
            BizDeathCertificateCause cause = new BizDeathCertificateCause();
            cause.setCertId(cert.getId());
            cause.setPart(row.getPart());
            cause.setSeqNo(row.getSeqNo());
            cause.setIcdCode(cutToNull(row.getIcdCode(), ICD_CODE_MAX));
            cause.setIcdName(cut(trimToNull(row.getIcdName()), ICD_NAME_MAX));
            cause.setIntervalText(cutToNull(row.getIntervalText(), INTERVAL_MAX));
            cause.setCreateBy(operator);
            cause.setCreateTime(createTime);
            causeMapper.insert(cause);
        }
        return cert.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(DeathCertificateDTO.Audit dto) {
        BizDeathCertificate cert = requireCert(dto.getId());
        if (!Objects.equals(cert.getCertStatus(), DeathCertStatusEnum.DRAFT.getCode())) {
            throw new BusinessException("只有草稿状态的证明可以提交审核（当前：" + statusText(cert.getCertStatus()) + "）");
        }
        cert.setReviewerId(UserUtils.getCurrentUser().getEmployeeId());
        cert.setReviewerName(currentOperator());
        cert.setReviewTime(now());
        cert.setReviewOpinion(cutToNull(dto.getOpinion(), REASON_MAX));
        cert.setCertStatus(DeathCertStatusEnum.AUDITED.getCode());
        saveCert(cert);
    }

    /**
     * 签发＝对外出具。三件事必须同时成立：已审核、该住院已办死亡离院且时间对得上、死因链与根本死因齐备。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issue(DeathCertificateDTO.Issue dto) {
        BizDeathCertificate cert = requireCert(dto.getId());
        if (!Objects.equals(cert.getCertStatus(), DeathCertStatusEnum.AUDITED.getCode())) {
            throw new BusinessException("只有「已审核」的证明可以签发（当前：" + statusText(cert.getCertStatus()) + "）");
        }
        DeathCertificateVO.DischargeSnapshot discharge = certMapper.selectDeathDischarge(cert.getAdmissionId());
        if (discharge == null) {
            throw new BusinessException("该住院尚未办理「死亡」离院，不能签发死亡证明（先走出院办理，离院方式选「死亡」）");
        }
        if (discharge.getDischargeTime() != null
                && !TimeUtil.toSeconds(discharge.getDischargeTime()).equals(cert.getDeathTime())) {
            throw new BusinessException("死亡时间与死亡离院时间不一致：出院办理记录为 "
                    + discharge.getDischargeTime() + "，两者必须是同一时点，请把证明的死亡时间改成它");
        }
        if (causeMapper.countChainRows(cert.getId()) == 0) {
            throw new BusinessException("签发前必须填写死因链Ⅰ部分（直接死因→…→根本死因）");
        }
        if (!StringUtils.hasText(cert.getUnderlyingIcdCode())) {
            throw new BusinessException("签发前必须确定根本死因（ICD-10），死因统计只认这一列");
        }
        if (causeMapper.countChainRowsMissingIcd(cert.getId()) > 0) {
            throw new BusinessException("签发前死因链Ⅰ部分每行的 ICD-10 编码必须补全");
        }
        if (Objects.equals(cert.getDeathPlace(), DeathPlaceEnum.HOSPITAL.getCode()) && cert.getDeathDeptId() == null) {
            throw new BusinessException("医院内死亡必须填写死亡科室");
        }
        cert.setCertStatus(DeathCertStatusEnum.ISSUED.getCode());
        cert.setIssueTime(now());
        cert.setDischargeId(discharge.getDischargeId());
        cert.setReportDeadline(cert.getDeathTime().plusDays(REPORT_DEADLINE_DAYS));
        saveCert(cert);
        // 病案首页「死亡患者尸检」以证明为准回写（国家标准首页项目，两处各填必然漂移）
        certMapper.updateSummaryAutopsyFlag(cert.getAdmissionId(), cert.getAutopsyFlag());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidCert(DeathCertificateDTO.VoidCert dto) {
        BizDeathCertificate cert = requireCert(dto.getId());
        if (Objects.equals(cert.getCertStatus(), DeathCertStatusEnum.VOIDED.getCode())) {
            throw new BusinessException("该证明已作废，无需重复作废");
        }
        cert.setCertStatus(DeathCertStatusEnum.VOIDED.getCode());
        cert.setVoidReason(cut(trimToNull(dto.getReason()), REASON_MAX));
        cert.setVoidBy(currentOperator());
        cert.setVoidTime(now());
        saveCert(cert);
    }

    /**
     * 重开：复制被作废原证的一般项目与死因链成一张新草稿，{@code orig_cert_id} 指向原证。
     * 原证内容与死因链一律不动（法定文书留痕，同收费四层红冲口径）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long reissue(Long origCertId) {
        BizDeathCertificate orig = requireCert(origCertId);
        if (!Objects.equals(orig.getCertStatus(), DeathCertStatusEnum.VOIDED.getCode())) {
            throw new BusinessException("只有已作废的证明才能重开（当前：" + statusText(orig.getCertStatus()) + "）");
        }
        if (certMapper.countActiveByAdmission(orig.getAdmissionId(), null) > 0) {
            throw new BusinessException("该次住院已有新的有效证明，请直接修改它");
        }
        BizDeathCertificate cert = new BizDeathCertificate();
        cert.setCertNo(redisSequenceService.generateDeathCertNo());
        cert.setAdmissionId(orig.getAdmissionId());
        cert.setPatientId(orig.getPatientId());
        cert.setPatientName(orig.getPatientName());
        cert.setGender(orig.getGender());
        cert.setNation(orig.getNation());
        cert.setBirthDate(orig.getBirthDate());
        cert.setAge(orig.getAge());
        cert.setIdCard(orig.getIdCard());
        cert.setOccupation(orig.getOccupation());
        cert.setMaritalStatus(orig.getMaritalStatus());
        cert.setDeathTime(orig.getDeathTime());
        cert.setDeathPlace(orig.getDeathPlace());
        cert.setDeathDeptId(orig.getDeathDeptId());
        cert.setDeathDeptName(orig.getDeathDeptName());
        cert.setDeathWardName(orig.getDeathWardName());
        cert.setDeathBedNo(orig.getDeathBedNo());
        cert.setClinicalDiagnosis(orig.getClinicalDiagnosis());
        cert.setUnderlyingIcdCode(orig.getUnderlyingIcdCode());
        cert.setUnderlyingIcdName(orig.getUnderlyingIcdName());
        cert.setPastHistory(orig.getPastHistory());
        cert.setAutopsyFlag(orig.getAutopsyFlag());
        cert.setAutopsyResult(orig.getAutopsyResult());
        cert.setRelativeName(orig.getRelativeName());
        cert.setRelativeRelation(orig.getRelativeRelation());
        cert.setRelativePhone(orig.getRelativePhone());
        cert.setPhysicianId(orig.getPhysicianId());
        cert.setPhysicianName(orig.getPhysicianName());
        cert.setFillTime(now());
        cert.setCertStatus(DeathCertStatusEnum.DRAFT.getCode());
        cert.setReportStatus(DeathCertReportEnum.NONE.getCode());
        cert.setReportDeadline(cert.getDeathTime().plusDays(REPORT_DEADLINE_DAYS));
        cert.setPrintCount(0);
        cert.setOrigCertId(orig.getId());
        cert.setRemark("由 " + orig.getCertNo() + " 作废后重开");
        saveCert(cert);

        for (DeathCertificateVO.CauseVO row : causeMapper.selectByCertId(orig.getId())) {
            BizDeathCertificateCause cause = new BizDeathCertificateCause();
            cause.setCertId(cert.getId());
            cause.setPart(row.getPart());
            cause.setSeqNo(row.getSeqNo());
            cause.setIcdCode(row.getIcdCode());
            cause.setIcdName(row.getIcdName());
            cause.setIntervalText(row.getIntervalText());
            cause.setCreateBy(currentOperator());
            cause.setCreateTime(now());
            causeMapper.insert(cause);
        }
        return cert.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void print(DeathCertificateDTO.Print dto) {
        BizDeathCertificate cert = requireCert(dto.getId());
        if (!Objects.equals(cert.getCertStatus(), DeathCertStatusEnum.ISSUED.getCode())) {
            throw new BusinessException("只有「已开具」的证明才打印（" + statusText(cert.getCertStatus()) + "的表样不能作为凭证）");
        }
        cert.setPrinterName(currentOperator());
        cert.setPrintCount((cert.getPrintCount() == null ? 0 : cert.getPrintCount()) + 1);
        cert.setLastPrintTime(now());
        saveCert(cert);
    }

    /**
     * 上报：组装标准报文落 {@code report_payload} 并置已上报。
     * 真实对接时本方法是唯一替换点——把「落 payload」换成「http 客户端发送 + 回执落 report_no」，接口面与报文结构不变。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String report(Long id) {
        BizDeathCertificate cert = requireCert(id);
        if (!Objects.equals(cert.getCertStatus(), DeathCertStatusEnum.ISSUED.getCode())) {
            throw new BusinessException("未签发的证明不能上报（当前：" + statusText(cert.getCertStatus()) + "）");
        }
        if (Objects.equals(cert.getReportStatus(), DeathCertReportEnum.DONE.getCode())) {
            throw new BusinessException("该证明已上报，报文已冻结（订正请作废重开后再报）");
        }
        if (!StringUtils.hasText(cert.getUnderlyingIcdCode())) {
            throw new BusinessException("根本死因编码为空，无法上报");
        }
        String payload = buildReportPayload(cert);
        cert.setReportStatus(DeathCertReportEnum.DONE.getCode());
        cert.setReportPayload(payload);
        cert.setReportTime(now());
        // 当前不对接外部平台：回执号即证明编号本身（再造一个 "DC" 前缀会得到 DCDC2024... 这种看着像 bug 的号）
        cert.setReportNo(cert.getCertNo());
        cert.setReportError(null);
        saveCert(cert);
        return payload;
    }

    @Override
    public int notifyOverdue() {
        List<BizDeathCertificate> overdue = certMapper.selectOverdueForNotify(NOTIFY_BATCH);
        int sent = 0;
        for (BizDeathCertificate r : overdue) {
            try {
                long lateDays = ChronoUnit.DAYS.between(r.getReportDeadline(), LocalDateTime.now());
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("certId", String.valueOf(r.getId()));
                payload.put("certNo", r.getCertNo());
                payload.put("patientName", r.getPatientName());
                payload.put("deadline", String.valueOf(r.getReportDeadline()));
                payload.put("lateDays", lateDays);
                boolean ok = sysMessageService.sendSystemMessage(r.getPhysicianId(), r.getPhysicianName(),
                        "死亡证明逾期未上报：" + r.getCertNo(),
                        r.getPatientName() + "（" + r.getCertNo() + "）已开具但超过上报时限 " + lateDays + " 天，请尽快上报死因监测",
                        BizTypeEnum.DEATH_CERT.getType(), r.getId(), "warning", JSONUtil.toJsonStr(payload), 0);
                if (ok) {
                    r.setNotifyTime(now());
                    certMapper.updateById(r);
                    sent++;
                }
            } catch (Exception ex) {
                log.warn("[死亡证明] 催报发送失败 certId={} physicianId={}", r.getId(), r.getPhysicianId(), ex);
            }
        }
        if (!overdue.isEmpty()) {
            log.info("[死亡证明] 时限催报扫描完成：逾期 {} 张，发送催报 {} 条", overdue.size(), sent);
        }
        return sent;
    }

    @Override
    public void assertDischargeConsistent(Long admissionId, LocalDateTime dischargeTime) {
        Long certId = certMapper.selectActiveIdByAdmission(admissionId);
        if (certId == null || dischargeTime == null) {
            return;
        }
        BizDeathCertificate cert = requireCert(certId);
        if (cert.getDeathTime() != null && !TimeUtil.toSeconds(dischargeTime).equals(cert.getDeathTime())) {
            throw new BusinessException("该住院已有死亡证明（" + cert.getCertNo() + "），出院时间必须等于证明的死亡时间 "
                    + cert.getDeathTime() + "：同一个时点，不许两处编");
        }
    }

    private BizDeathCertificate requireCert(Long id) {
        BizDeathCertificate cert = id == null ? null : certMapper.selectById(id);
        if (cert == null) {
            throw new BusinessException("死亡证明不存在或已删除");
        }
        return cert;
    }

    /**
     * 医院内死亡记科室/病区/床位（快照优先取病案首页留档）；院外死亡不编院内科室
     */
    private void applyDeathDept(BizDeathCertificate cert, Integer deathPlace, Long deptId,
                                DeathCertificateVO.PatientSnapshot snapshot) {
        if (deathPlace != DeathPlaceEnum.HOSPITAL.getCode()) {
            cert.setDeathDeptId(null);
            cert.setDeathDeptName(null);
            cert.setDeathWardName(null);
            cert.setDeathBedNo(null);
            return;
        }
        Long useDeptId = deptId != null ? deptId : snapshot.getDeptId();
        if (useDeptId == null) {
            throw new BusinessException("医院内死亡必须选择死亡科室");
        }
        cert.setDeathDeptId(useDeptId);
        // 死亡科室可以与出院时所在科室不同（死于 ICU、办出院在普通病房），名字服务端查，不采信前端字符串
        cert.setDeathDeptName(Objects.equals(useDeptId, snapshot.getDeptId())
                ? cutToNull(snapshot.getDeptName(), UNIT_MAX)
                : cutToNull(certMapper.selectDeptName(useDeptId), UNIT_MAX));
        cert.setDeathWardName(cutToNull(snapshot.getWardName(), UNIT_MAX));
        cert.setDeathBedNo(cutToNull(snapshot.getBedNo(), 16));
    }

    private void saveCert(BizDeathCertificate cert) {
        if (cert.getId() == null) {
            certMapper.insert(cert);
        } else if (certMapper.updateById(cert) <= 0) {
            throw new BusinessException("死亡证明保存失败，请重试");
        }
    }

    /**
     * 死因监测上报报文。字段名按《居民死亡医学证明（推断）书》调查记录逐项对齐，
     * 死因链按 Ⅰ(a~d)/Ⅱ 分组带上，回执侧要的就是这一份。
     */
    private String buildReportPayload(BizDeathCertificate cert) {
        List<Map<String, Object>> chain = new ArrayList<>();
        List<Map<String, Object>> other = new ArrayList<>();
        for (DeathCertificateVO.CauseVO row : causeMapper.selectByCertId(cert.getId())) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("seqNo", row.getSeqNo());
            item.put("icdCode", row.getIcdCode());
            item.put("name", row.getIcdName());
            item.put("interval", row.getIntervalText());
            (Objects.equals(row.getPart(), DeathCausePartEnum.OTHER.getCode()) ? other : chain).add(item);
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msgType", "DEATH_CERT_REPORT");
        payload.put("certNo", cert.getCertNo());
        payload.put("name", cert.getPatientName());
        payload.put("gender", cert.getGender());
        payload.put("nation", cert.getNation());
        payload.put("birthDate", cert.getBirthDate() == null ? null : cert.getBirthDate().toString());
        payload.put("age", cert.getAge());
        payload.put("idCard", cert.getIdCard());
        payload.put("occupation", cert.getOccupation());
        payload.put("maritalStatus", cert.getMaritalStatus());
        payload.put("deathTime", ts(cert.getDeathTime()));
        payload.put("deathPlace", cert.getDeathPlace());
        payload.put("deathDept", cert.getDeathDeptName());
        payload.put("clinicalDiagnosis", cert.getClinicalDiagnosis());
        payload.put("underlyingCause", Map.of("icdCode", nullToEmpty(cert.getUnderlyingIcdCode()),
                "name", nullToEmpty(cert.getUnderlyingIcdName())));
        payload.put("causeChainPartI", chain);
        payload.put("causeChainPartII", other);
        payload.put("autopsyFlag", cert.getAutopsyFlag());
        payload.put("autopsyResult", cert.getAutopsyResult());
        payload.put("relative", Map.of("name", nullToEmpty(cert.getRelativeName()),
                "relation", nullToEmpty(cert.getRelativeRelation()),
                "phone", nullToEmpty(cert.getRelativePhone())));
        payload.put("physician", cert.getPhysicianName());
        payload.put("fillTime", ts(cert.getFillTime()));
        payload.put("issueTime", ts(cert.getIssueTime()));
        payload.put("reportDeadline", ts(cert.getReportDeadline()));
        return JSONUtil.toJsonStr(payload);
    }

    private String currentOperator() {
        return UserUtils.getCurrentUser().getRealName();
    }
}
