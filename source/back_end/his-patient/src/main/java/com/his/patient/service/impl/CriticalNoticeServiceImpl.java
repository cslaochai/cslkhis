package com.his.patient.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.dto.SignCommandDTO;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.EmrSignatureService;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.common.vo.SignatureVO;
import com.his.patient.dto.CriticalNoticeDTO;
import com.his.patient.entity.BizCriticalNotice;
import com.his.patient.enums.NoticeStatusEnum;
import com.his.patient.mapper.BizCriticalNoticeMapper;
import com.his.patient.service.CriticalNoticeService;
import com.his.patient.vo.CriticalNoticeVO;
import com.his.system.entity.CurrentUser;
import com.his.system.provider.DeptScopeProvider;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 病危重通知服务实现（sql/161）。
 *
 * <p>口径：
 * <ol>
 *   <li><b>签发即电子签名</b>：通知是法定告知凭证，落款人（签名主体）必须是当前登录职工，
 *       医师名只能由系统带出、不接受前端冒充；签名失败随签发事务一起回滚，
 *       不允许「发出去但没签名」的中间态。</li>
 *   <li><b>签收三要素缺一不可</b>：签收人姓名 + 与患者关系（法定必填） + 手写签名图；
 *       关系不在字典码表内直接拒 —— 「家属」两个字在诉讼里不等于被授权的人。</li>
 *   <li><b>已签收不许作废</b>：患方签字的告知事实不能事后蒸发，登记错误走备注/重开新单纠偏；
 *       已签发且有有效签名的作废须先在签名中心作废签名（作废留痕之后再作废单据）。</li>
 *   <li>患者一般项目全部服务端按住院重查快照，不采信前端字符串。</li>
 *   <li>在院事实是签发前提（admit_status=1）：人已出院再签发病危通知是编造告知。</li>
 *   <li>同一患者多次病情变化可开多张通知（不设「一住院一张」闸），待签收口径由统计卡呈现。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CriticalNoticeServiceImpl extends ServiceImpl<BizCriticalNoticeMapper, BizCriticalNotice> implements CriticalNoticeService {
    private static final int DIAG_MAX = 500;
    private static final int TEXT_MAX = 1000;
    private static final int REASON_MAX = 500;
    private static final int NAME_MAX = 50;
    private static final int PHONE_MAX = 20;
    /**
     * 手写签名 dataURL 上限 512KB（canvas PNG 正常几十 KB，兜住恶意大串）
     */
    private static final int SIGNATURE_MAX = 512 * 1024;
    /**
     * 神志合法码（字典 his_notice_consciousness）
     */
    private static final Set<Integer> CONSCIOUSNESS = Set.of(1, 2, 3, 4, 9);
    /**
     * 签收人关系合法码（字典 his_notice_relation）
     */
    private static final Set<Integer> RELATIONS = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 99);
    private final DeptScopeProvider deptScopeProvider;
    private final BizCriticalNoticeMapper bizCriticalNoticeMapper;
    private final RedisSequenceService redisSequenceService;
    private final EmrSignatureService emrSignatureService;

    // 查询

    // 填写 / 签发 / 签收 / 作废 / 打印

    @Override
    public PageResult<CriticalNoticeVO.Row> listPage(CriticalNoticeDTO.QueryPage query) {
        CriticalNoticeDTO.QueryPage q = query == null ? new CriticalNoticeDTO.QueryPage() : query;
        Page<CriticalNoticeVO.Row> page = new Page<>(q.getPageNum(), q.getPageSize());
        List<Long> deptIds = scopedDeptIds(q.getDeptId());
        List<CriticalNoticeVO.Row> records = bizCriticalNoticeMapper.selectNoticePage(page,
                TextUtil.trimToNull(q.getKeyword()), q.getNoticeType(), q.getNoticeStatus(),
                TimeUtil.dayStart(q.getStartDate()), TimeUtil.dayEnd(q.getEndDate()), q.getDeptId(), deptIds);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public CriticalNoticeVO.Detail getDetailById(Long id) {
        CriticalNoticeVO.Detail detail = bizCriticalNoticeMapper.selectNoticeDetail(id);
        if (detail == null) {
            throw new BusinessException("病危重通知单不存在或已删除");
        }
        return detail;
    }

    // 内部

    @Override
    public CriticalNoticeVO.Base base(Long admissionId) {
        CriticalNoticeVO.Base base = bizCriticalNoticeMapper.selectAdmissionBase(admissionId);
        if (base == null) {
            throw new BusinessException("住院记录不存在");
        }
        return base;
    }

    @Override
    public List<CriticalNoticeVO.Inpatient> inpatients(String keyword, Integer limit) {
        int n = limit == null || limit <= 0 ? 200 : Math.min(limit, 200);
        return bizCriticalNoticeMapper.selectInpatientCandidates(TextUtil.trimToNull(keyword), scopedDeptIds(null), n);
    }

    @Override
    public List<CriticalNoticeVO.DoctorOption> doctorOptions() {
        return bizCriticalNoticeMapper.selectDoctorOptions();
    }

    @Override
    public CriticalNoticeVO.Stats stats() {
        CriticalNoticeVO.Stats stats = bizCriticalNoticeMapper.selectStats(scopedDeptIds(null));
        return stats == null ? new CriticalNoticeVO.Stats() : stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(CriticalNoticeDTO.Upsert dto) {
        CriticalNoticeVO.Base snapshot = bizCriticalNoticeMapper.selectAdmissionBase(dto.getAdmissionId());
        if (snapshot == null) {
            throw new BusinessException("住院记录不存在");
        }
        if (!Objects.equals(snapshot.getAdmitStatus(), AdmitStatusEnum.IN_HOSPITAL.getCode())) {
            throw new BusinessException("患者已不在院，不能填写病危重通知（告知必须发生在住院期间）");
        }
        // 写路径与签发/作废同一口径收口：不校验就能给别科患者开单，等于绕过了岗位数据范围
        assertDeptAccessible(snapshot.getDeptId());
        LocalDateTime now = TimeUtil.nowSeconds();
        LocalDateTime notifyTime = TimeUtil.toSeconds(dto.getNotifyTime());
        // 保留（类别③）：告知时间要落在「入院之后、此刻之前」，是时间轴业务规则，不是入参是否为空
        if (notifyTime.isAfter(now)) {
            throw new BusinessException("告知时间不能晚于当前时间");
        }
        if (snapshot.getAdmitTime() != null && notifyTime.isBefore(TimeUtil.toSeconds(snapshot.getAdmitTime()))) {
            throw new BusinessException("告知时间不能早于入院时间");
        }
        // 保留（类别③）：神志码值必须命中字典取值
        if (dto.getConsciousnessStatus() == null || !CONSCIOUSNESS.contains(dto.getConsciousnessStatus())) {
            throw new BusinessException("患者神志取值不合法（见字典 his_notice_consciousness）");
        }

        BizCriticalNotice notice;
        boolean isNew = dto.getId() == null;
        if (isNew) {
            notice = new BizCriticalNotice();
            notice.setNoticeNo(redisSequenceService.generateCriticalNoticeNo());
            notice.setAdmissionId(dto.getAdmissionId());
            notice.setNoticeStatus(NoticeStatusEnum.DRAFT.getCode());
            notice.setSignStatus(ObjectSignStatusEnum.UNSIGNED.getCode());
            notice.setPrintCount(0);
        } else {
            notice = requireNotice(dto.getId());
            if (!Objects.equals(notice.getAdmissionId(), dto.getAdmissionId())) {
                throw new BusinessException("通知单不允许改挂到另一次住院");
            }
            if (!Objects.equals(NoticeStatusEnum.DRAFT.getCode(), notice.getNoticeStatus())) {
                throw new BusinessException(BizCriticalNotice.statusText(notice.getNoticeStatus())
                        + "的通知单不能修改；已签发要改先在「签名中心」作废签名并走作废重开");
            }
            requireUnsigned(notice);
        }

        // 一般项目一律取服务端快照，不采信前端
        notice.setPatientId(snapshot.getPatientId());
        notice.setPatientName(snapshot.getPatientName());
        notice.setPatientNo(TextUtil.cutToNull(snapshot.getPatientNo(), 32));
        notice.setGender(snapshot.getGender());
        notice.setAge(snapshot.getAge());
        notice.setDeptId(snapshot.getDeptId());
        notice.setDeptName(TextUtil.cutToNull(snapshot.getDeptName(), 100));
        notice.setWardName(TextUtil.cutToNull(snapshot.getWardName(), 64));
        notice.setBedNo(TextUtil.cutToNull(snapshot.getBedNo(), 16));
        notice.setAdmissionNo(TextUtil.cutToNull(snapshot.getAdmissionNo(), 32));

        notice.setNoticeType(dto.getNoticeType());
        notice.setConsciousnessStatus(dto.getConsciousnessStatus());
        notice.setClinicalDiagnosis(TextUtil.cut(TextUtil.trimToNull(dto.getClinicalDiagnosis()), DIAG_MAX));
        notice.setConditionDesc(TextUtil.cut(TextUtil.trimToNull(dto.getConditionDesc()), TEXT_MAX));
        notice.setWarningMatters(TextUtil.cut(TextUtil.trimToNull(dto.getWarningMatters()), TEXT_MAX));
        notice.setDoctorMeasures(TextUtil.cutToNull(dto.getDoctorMeasures(), TEXT_MAX));
        notice.setNotifyTime(notifyTime);
        if (isNew || notice.getDoctorId() == null) {
            Long me = UserUtils.getCurrentUser().getEmployeeId();
            notice.setDoctorId(me != null ? me : dto.getDoctorId());
            // 保留（类别②）：校验对象是服务端快照/登录上下文带出的医师名，不是入参字段，注解覆盖不到
            notice.setDoctorName(TextUtil.cut(TextUtil.requireTrimmed(
                    TextUtil.hasText(notice.getDoctorName()) ? notice.getDoctorName()
                            : UserUtils.getCurrentUser().getRealName(), "告知医师不能为空"), NAME_MAX));
        }
        applyWitness(notice, dto.getWitnessDoctorId(), dto.getWitnessDoctorName());
        notice.setRemark(TextUtil.cutToNull(dto.getRemark(), REASON_MAX));
        saveNotice(notice, isNew);
        return notice.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issue(CriticalNoticeDTO.Issue dto) {
        BizCriticalNotice notice = requireNotice(dto.getId());
        if (!Objects.equals(NoticeStatusEnum.DRAFT.getCode(), notice.getNoticeStatus())) {
            throw new BusinessException("只有「" + BizCriticalNotice.statusText(NoticeStatusEnum.DRAFT.getCode())
                    + "」的通知单能签发，当前为「" + BizCriticalNotice.statusText(notice.getNoticeStatus()) + "」");
        }
        requireUnsigned(notice);
        assertDeptAccessible(notice.getDeptId());
        CriticalNoticeVO.Base snapshot = bizCriticalNoticeMapper.selectAdmissionBase(notice.getAdmissionId());
        if (snapshot == null || !Objects.equals(snapshot.getAdmitStatus(), AdmitStatusEnum.IN_HOSPITAL.getCode())) {
            throw new BusinessException("患者已不在院，不能签发病危重通知（告知必须发生在住院期间）");
        }

        // 落款人 = 当前登录职工：法定签名的责任主体不许前端冒充
        Long me = UserUtils.getCurrentUser().getEmployeeId();
        String myName = UserUtils.getCurrentUser().getRealName();
        if (me == null || !TextUtil.hasText(myName)) {
            throw new BusinessException("当前登录账号未绑定员工档案，无法以医师身份签发");
        }
        if (notice.getDoctorId() != null && !Objects.equals(notice.getDoctorId(), me)) {
            throw new BusinessException("该通知单由「" + notice.getDoctorName() + "」填写，"
                    + "电子签名的落款人必须是本人，请用填写医师的账号签发");
        }
        notice.setDoctorId(me);
        notice.setDoctorName(TextUtil.cut(myName, NAME_MAX));

        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(SignBizTypeEnum.CRITICAL_NOTICE.getCode());
        cmd.setBizId(notice.getId());
        cmd.setSignScene(SignSceneEnum.NOTICE_ISSUE.getCode());
        cmd.setSignerId(me);
        cmd.setSignerName(notice.getDoctorName());
        CurrentUser user = UserUtils.getCurrentUser();
        if (user != null) {
            cmd.setSignerDeptId(user.getDeptId());
            cmd.setSignerDeptName(user.getDeptName());
        }
        cmd.setSignerTitle(TextUtil.cutToNull(bizCriticalNoticeMapper.selectEmployeeTitle(me), 50));
        cmd.setClientIp(dto.getClientIp());
        cmd.setRemark("病危重通知签发（" + BizCriticalNotice.typeText(notice.getNoticeType()) + "）");
        try {
            SignatureVO sig = emrSignatureService.sign(cmd);
            // 锚点三件套必须在这里一起写：provider 的 applySignAnchor 已把 sign_status 置 1，
            // 但本方法随后用**签发前读出的实体**整行 updateById，漏写就把它冲回 0（签名失效、单据像没锁）。
            notice.setSignStatus(ObjectSignStatusEnum.SIGNED.getCode());
            notice.setSignId(sig.getId());
            notice.setSignedTime(sig.getSignedTime());
        } catch (BusinessException e) {
            throw new BusinessException("通知单签发失败（" + notice.getNoticeNo() + "）：" + e.getMessage());
        }
        notice.setNoticeStatus(NoticeStatusEnum.ISSUED.getCode());
        notice.setIssueTime(TimeUtil.nowSeconds());
        saveNotice(notice, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acknowledge(CriticalNoticeDTO.Acknowledge dto) {
        BizCriticalNotice notice = requireNotice(dto.getId());
        if (!Objects.equals(NoticeStatusEnum.ISSUED.getCode(), notice.getNoticeStatus())) {
            throw new BusinessException("只有「已签发」等待签收的通知单能签收，当前为「"
                    + BizCriticalNotice.statusText(notice.getNoticeStatus()) + "」");
        }
        assertDeptAccessible(notice.getDeptId());
        // 保留（类别③）：关系码值必须命中字典（null 只是「没选」的一种），注解只能管到非空
        if (dto.getSignerRelation() == null || !RELATIONS.contains(dto.getSignerRelation())) {
            throw new BusinessException("签收人与患者的关系取值不合法（见字典 his_notice_relation，法定必填）");
        }
        String signature = TextUtil.trimToNull(dto.getSignerSignature());
        if (!signature.startsWith("data:image/png;base64,")) {
            throw new BusinessException("手写签名必须是画板生成的 PNG 图片");
        }
        if (signature.length() > SIGNATURE_MAX) {
            throw new BusinessException("手写签名图片过大，请清空画板后重新签名");
        }
        notice.setSignerName(TextUtil.cut(TextUtil.trimToNull(dto.getSignerName()), NAME_MAX));
        notice.setSignerRelation(dto.getSignerRelation());
        notice.setSignerIdCard(TextUtil.cutToNull(TextUtil.trimToNull(dto.getSignerIdCard()), 20));
        notice.setSignerPhone(TextUtil.cutToNull(TextUtil.trimToNull(dto.getSignerPhone()), PHONE_MAX));
        notice.setSignerSignature(signature);
        notice.setAcknowledgeTime(TimeUtil.nowSeconds());
        notice.setNoticeStatus(NoticeStatusEnum.ACKED.getCode());
        saveNotice(notice, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void voidNotice(CriticalNoticeDTO.VoidNotice dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizCriticalNotice notice = requireNotice(dto.getId());
        if (Objects.equals(NoticeStatusEnum.ACKED.getCode(), notice.getNoticeStatus())) {
            throw new BusinessException("已签收的通知单承载患方签字的告知事实，不允许作废；"
                    + "登记错误请在备注纠偏并按规范重开新单");
        }
        if (Objects.equals(NoticeStatusEnum.VOIDED.getCode(), notice.getNoticeStatus())) {
            throw new BusinessException("该通知单已作废");
        }
        if (Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), notice.getSignStatus())) {
            throw new BusinessException("该通知单已电子签名锁定，请先在「签名中心」作废签名，再作废单据（作废留痕）");
        }
        assertDeptAccessible(notice.getDeptId());
        notice.setNoticeStatus(NoticeStatusEnum.VOIDED.getCode());
        notice.setVoidReason(TextUtil.cut(TextUtil.trimToNull(dto.getVoidReason()), REASON_MAX));
        notice.setVoidBy(operatorUser.getRealName());
        notice.setVoidTime(TimeUtil.nowSeconds());
        saveNotice(notice, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void print(CriticalNoticeDTO.Print dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizCriticalNotice notice = requireNotice(dto.getId());
        if (!Objects.equals(NoticeStatusEnum.ACKED.getCode(), notice.getNoticeStatus())) {
            throw new BusinessException("只有「已签收」的通知单打印回执（未签收的告知尚未闭环）");
        }
        notice.setPrinterName(operatorUser.getRealName());
        notice.setPrintCount(NumUtil.orDefault(notice.getPrintCount(), 0) + 1);
        notice.setLastPrintTime(TimeUtil.nowSeconds());
        saveNotice(notice, false);
    }

    private void applyWitness(BizCriticalNotice notice, Long witnessId, String witnessNameArg) {
        String name = TextUtil.trimToNull(witnessNameArg);
        if (witnessId == null && name == null) {
            notice.setWitnessDoctorId(null);
            notice.setWitnessDoctorName(null);
            return;
        }
        if (Objects.equals(witnessId, notice.getDoctorId())
                || (name != null && name.equals(notice.getDoctorName()))) {
            throw new BusinessException("见证医师不能与告知医师是同一人（见证的意义在于第二双眼）");
        }
        notice.setWitnessDoctorId(witnessId);
        notice.setWitnessDoctorName(TextUtil.cutToNull(name, NAME_MAX));
    }

    private BizCriticalNotice requireNotice(Long id) {
        BizCriticalNotice notice = id == null ? null : bizCriticalNoticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("病危重通知单不存在或已删除");
        }
        return notice;
    }

    /**
     * 签名即锁定：有效签名挂着就不许改内容（同申请单口径）
     */
    private void requireUnsigned(BizCriticalNotice notice) {
        if (Objects.equals(ObjectSignStatusEnum.SIGNED.getCode(), notice.getSignStatus())) {
            throw new BusinessException("通知单 " + notice.getNoticeNo() + " 已电子签名（签名即锁定），不允许直接修改；"
                    + "请先在「签名中心」作废该签名（作废会留痕并解除锁定）");
        }
    }

    private void assertDeptAccessible(Long deptId) {
        if (!deptScopeProvider.canAccessDept(deptId)) {
            throw new BusinessException("该通知单所属科室不在当前岗位的数据范围内");
        }
    }

    /**
     * 返回 null=不受限；非空=收口科室集合（显式 deptId 由 DeptScopeProvider 校验越权）
     */
    private List<Long> scopedDeptIds(Long requestedDeptId) {
        Long resolved = deptScopeProvider.resolveDeptId(requestedDeptId);
        if (resolved != null) {
            return null;
        }
        Set<Long> allowed = deptScopeProvider.allowedDeptIds();
        return allowed == null ? null : List.copyOf(allowed);
    }

    private void saveNotice(BizCriticalNotice notice, boolean isNew) {
        if (isNew) {
            bizCriticalNoticeMapper.insert(notice);
        } else {
            bizCriticalNoticeMapper.updateById(notice);
        }
    }

}
