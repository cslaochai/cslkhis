package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.constant.SystemConfigKeyConst;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.ObjectSignStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.dto.InpatientLeaveDTO;
import com.his.patient.entity.BizInpatientLeave;
import com.his.patient.enums.LeaveStatusEnum;
import com.his.patient.mapper.BizInpatientLeaveMapper;
import com.his.patient.service.InpatientLeaveService;
import com.his.patient.vo.InpatientLeaveVO;
import com.his.system.dto.SignCommandDTO;
import com.his.system.entity.CurrentUser;
import com.his.system.entity.SysConfig;
import com.his.system.mapper.SysConfigMapper;
import com.his.system.provider.DeptScopeService;
import com.his.system.service.EmrSignatureService;
import com.his.system.service.RedisSequenceService;
import com.his.system.utils.UserUtils;
import com.his.system.vo.SignatureVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 住院请假/离院服务实现（sql/162）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientLeaveServiceImpl extends ServiceImpl<BizInpatientLeaveMapper, BizInpatientLeave> implements InpatientLeaveService {
    private static final int REASON_MAX = 500;
    private static final int DEST_MAX = 200;
    private static final int NOTE_MAX = 500;
    private static final int NAME_MAX = 50;
    private static final int PHONE_MAX = 20;
    private static final int ADVICE_MAX = 1000;
    /**
     * 手写签名 dataURL 上限 512KB（canvas PNG 正常几十 KB，兜住恶意大串）
     */
    private static final int SIGNATURE_MAX = 512 * 1024;
    /**
     * 与患者关系合法码（字典 his_notice_relation，与病危重通知同一码表）
     */
    private static final Set<Integer> RELATIONS = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 99);
    /**
     * 超期联系结果合法码（字典 his_leave_contact）
     */
    private static final Set<Integer> CONTACT_RESULTS = Set.of(1, 2, 3);
    /**
     * 上报对象合法码（字典 his_leave_report）
     */
    private static final Set<Integer> REPORT_TOS = Set.of(1, 2, 3);
    /**
     * 配置缺失时的兜底上限（小时）。宁可保守，也不回落成「无上限」。
     */
    private static final int MAX_HOURS_FALLBACK = 72;
    private final DeptScopeService deptScopeService;
    private final BizInpatientLeaveMapper bizInpatientLeaveMapper;
    private final RedisSequenceService redisSequenceService;
    private final EmrSignatureService emrSignatureService;
    private final SysConfigMapper sysConfigMapper;

    // 查询

    private static LocalDateTime parse(String text) {
        if (text == null || text.isEmpty()) {
            return LocalDateTime.MIN;
        }
        return LocalDateTime.parse(text.length() > 19 ? text.substring(0, 19) : text,
                DateFormats.DATETIME);
    }

    // 填写 / 审批 / 离院 / 销假 / 取消 / 超期处置 / 打印

    @Override
    public PageResult<InpatientLeaveVO.Row> listPage(InpatientLeaveDTO.QueryPage query) {
        Page<InpatientLeaveVO.Row> page = new Page<>(query.getPageNum(), query.getPageSize());
        List<Long> deptIds = scopedDeptIds(query.getDeptId());
        List<InpatientLeaveVO.Row> records = bizInpatientLeaveMapper.selectLeavePage(page,
                TextUtil.trimToNull(query.getKeyword()), query.getLeaveType(), query.getLeaveStatus(), query.getOverdueOnly(),
                TimeUtil.dayStart(query.getStartDate()), TimeUtil.dayEnd(query.getEndDate()), query.getDeptId(), deptIds);
        for (InpatientLeaveVO.Row row : records) {
            applyOverdue(row);
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public InpatientLeaveVO.Detail getDetailById(Long id) {
        // 详情同样是数据范围内的东西：手写签名图、患方姓名关系、医师意见都在里面，读侧必须收口
        InpatientLeaveVO.Detail detail = bizInpatientLeaveMapper.selectLeaveDetail(id, scopedDeptIds(null));
        if (detail == null) {
            throw new BusinessException("请假单不存在、已删除，或不在当前岗位的数据范围内");
        }
        applyOverdue(detail);
        applyActions(detail);
        return detail;
    }

    // 内部

    @Override
    public InpatientLeaveVO.Base base(Long admissionId) {
        InpatientLeaveVO.Base base = bizInpatientLeaveMapper.selectAdmissionBase(admissionId);
        if (base == null) {
            throw new BusinessException("住院记录不存在");
        }
        return base;
    }

    @Override
    public List<InpatientLeaveVO.Inpatient> inpatients(String keyword, Integer limit) {
        int n = limit == null || limit <= 0 ? 200 : Math.min(limit, 200);
        return bizInpatientLeaveMapper.selectInpatientCandidates(TextUtil.trimToNull(keyword), scopedDeptIds(null), n);
    }

    @Override
    public InpatientLeaveVO.Stats stats() {
        InpatientLeaveVO.Stats stats = bizInpatientLeaveMapper.selectStats(scopedDeptIds(null));
        return stats == null ? new InpatientLeaveVO.Stats() : stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(InpatientLeaveDTO.Upsert dto) {
        InpatientLeaveVO.Base snapshot = bizInpatientLeaveMapper.selectAdmissionBase(dto.getAdmissionId());
        if (snapshot == null) {
            throw new BusinessException("住院记录不存在");
        }
        if (!Objects.equals(snapshot.getAdmitStatus(), AdmitStatusEnum.IN_HOSPITAL.getCode())) {
            throw new BusinessException("患者已不在院，不能填写请假单（请假必须发生在住院期间）");
        }
        LocalDateTime expectedLeave = TimeUtil.toSeconds(dto.getExpectedLeaveTime());
        LocalDateTime expectedReturn = TimeUtil.toSeconds(dto.getExpectedReturnTime());
        if (expectedLeave.isAfter(expectedReturn)) {
            throw new BusinessException("预计离院时间不能晚于预计返回时间");
        }
        // 注意：Base.admitTime 是 SQL DATE_FORMAT 出的字符串，必须先 parse 才能与时间比较
        LocalDateTime admitTime = TextUtil.hasText(snapshot.getAdmitTime()) ? parse(snapshot.getAdmitTime()) : null;
        if (admitTime != null && expectedLeave.isBefore(TimeUtil.toSeconds(admitTime))) {
            throw new BusinessException("预计离院时间不能早于入院时间（人还没入院就开始请假是编造事实）");
        }
        long hours = java.time.Duration.between(expectedLeave, expectedReturn).toHours();
        long maxHours = resolveMaxHours();
        if (hours > maxHours) {
            throw new BusinessException("单次请假时长 " + hours + " 小时超过上限 " + maxHours
                    + " 小时（系统参数 inpatient.leave.max_hours）；「请假外出」不得变成变相出院，请改期或走出院流程");
        }
        // 在途唯一：同一住院同时只允许一条进行中（1/2/3）请假单
        List<BizInpatientLeave> actives = selectActiveLeaves(dto.getAdmissionId());

        BizInpatientLeave leave;
        boolean isNew = dto.getId() == null;
        if (isNew) {
            if (!actives.isEmpty()) {
                BizInpatientLeave active = actives.get(0);
                throw new BusinessException("该患者已有一张进行中的请假单（" + active.getLeaveNo() + "，"
                        + BizInpatientLeave.statusText(active.getLeaveStatus()) + "），销假/取消后才能再申请");
            }
            leave = new BizInpatientLeave();
            leave.setLeaveNo(redisSequenceService.generateInpatientLeaveNo());
            leave.setAdmissionId(dto.getAdmissionId());
            leave.setLeaveStatus(LeaveStatusEnum.PENDING.getCode());
            leave.setSignStatus(ObjectSignStatusEnum.UNSIGNED.getCode());
            leave.setPrintCount(0);
        } else {
            leave = requireLeave(dto.getId());
            if (!Objects.equals(leave.getAdmissionId(), dto.getAdmissionId())) {
                throw new BusinessException("请假单不允许改挂到另一次住院");
            }
            if (!Objects.equals(LeaveStatusEnum.PENDING.getCode(), leave.getLeaveStatus())) {
                throw new BusinessException(BizInpatientLeave.statusText(leave.getLeaveStatus())
                        + "的请假单不能修改；已批准的单如患方不再外出，请走「取消」");
            }
        }

        // 一般项目一律取服务端快照，不采信前端
        leave.setPatientId(snapshot.getPatientId());
        leave.setPatientName(snapshot.getPatientName());
        leave.setPatientNo(TextUtil.cutToNull(snapshot.getPatientNo(), 32));
        leave.setGender(snapshot.getGender());
        leave.setAge(snapshot.getAge());
        leave.setDeptId(snapshot.getDeptId());
        leave.setDeptName(TextUtil.cutToNull(snapshot.getDeptName(), 100));
        leave.setWardName(TextUtil.cutToNull(snapshot.getWardName(), 64));
        leave.setBedNo(TextUtil.cutToNull(snapshot.getBedNo(), 16));
        leave.setAdmissionNo(TextUtil.cutToNull(snapshot.getAdmissionNo(), 32));

        leave.setLeaveType(dto.getLeaveType());
        leave.setReason(TextUtil.cut(TextUtil.trimToNull(dto.getReason()), REASON_MAX));
        leave.setDestination(TextUtil.cut(TextUtil.trimToNull(dto.getDestination()), DEST_MAX));
        leave.setCompanionName(TextUtil.cut(TextUtil.trimToNull(dto.getCompanionName()), NAME_MAX));
        if (dto.getCompanionRelation() != null && !RELATIONS.contains(dto.getCompanionRelation())) {
            throw new BusinessException("随行人与患者的关系取值不合法（见字典 his_notice_relation）");
        }
        leave.setCompanionRelation(dto.getCompanionRelation());
        // 随行人电话：列表 VO 不出联系方式（AGENTS §5）、详情只出脱敏值，前端**无法原样回显明文**，
        // 于是编辑草稿时这一格必然是空的。若照旧强校验，用户点「编辑」什么都不改直接保存会 400
        // —— 现象像「编辑功能坏了」。口径：编辑时留空＝沿用原值（电话是必填项，没有「清空」语义）；
        // 新建时留空＝参数错误。
        // B-条件必填：编辑留空＝沿用原值，只有新建（原值也为空）才报错，DTO 注解表达不了这层分支，保留
        if (TextUtil.hasText(dto.getCompanionPhone())) {
            leave.setCompanionPhone(TextUtil.cut(dto.getCompanionPhone().trim(), PHONE_MAX));
        } else if (!TextUtil.hasText(leave.getCompanionPhone())) {
            throw new BusinessException("随行人联系电话不能为空");
        }
        leave.setExpectedLeaveTime(expectedLeave);
        leave.setExpectedReturnTime(expectedReturn);
        if (isNew) {
            leave.setApplyTime(TimeUtil.nowSeconds());
            CurrentUser operatorUser = UserUtils.getCurrentUser();
            if (operatorUser == null) {
                throw new BusinessException("当前用户信息不存在");
            }
            leave.setApplyBy(TextUtil.cutToNull(operatorUser.getRealName(), 64));
        }
        leave.setRemark(TextUtil.cutToNull(dto.getRemark(), REASON_MAX));
        saveLeave(leave, isNew);
        return leave.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(InpatientLeaveDTO.Approve dto) {
        BizInpatientLeave leave = requireLeave(dto.getId());
        if (!Objects.equals(LeaveStatusEnum.PENDING.getCode(), leave.getLeaveStatus())) {
            throw new BusinessException("只有「待审批」的请假单能审批，当前为「"
                    + BizInpatientLeave.statusText(leave.getLeaveStatus()) + "」");
        }
        // 审批必须患者在院：人已出院再批一张外出单是编造医疗行为
        InpatientLeaveVO.Base snapshot = bizInpatientLeaveMapper.selectAdmissionBase(leave.getAdmissionId());
        if (snapshot == null || !Objects.equals(snapshot.getAdmitStatus(), AdmitStatusEnum.IN_HOSPITAL.getCode())) {
            throw new BusinessException("患者已不在院，不能审批请假单（请假必须发生在住院期间）");
        }
        assertDeptAccessible(leave.getDeptId());

        // 落款人 = 当前登录职工：批准/拒绝都是医疗决定，责任主体不许前端冒充
        CurrentUser currentUser = UserUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Long me = currentUser.getEmployeeId();
        String myName = currentUser.getRealName();
        if (me == null || !TextUtil.hasText(myName)) {
            throw new BusinessException("当前登录账号未绑定员工档案，无法以医师身份审批");
        }

        boolean contentSaved = false;
        if (Boolean.TRUE.equals(dto.getAllow())) {
            // ①条件必填：仅批准分支必填，@NotNull 会把合法的拒绝请求挡成 400
            String advice = TextUtil.requireTrimmed(dto.getDoctorAdvice(), "批准必须填写医师意见（病情评估：是否允许外出、外出期间注意事项）");
            // 医师意见＋审批医师必须**先落库**再签名：provider 的 load() 是从库里读记录算摘要的，
            // 先签名后落库会让「批的是什么、谁批的」在签名那一刻是空值，事后验签必报内容已变更。
            // （同 sql/161：那里 doctorName 在签发前已在库且强制同一个人，所以天然不会漂）
            leave.setDoctorAdvice(TextUtil.cut(advice, ADVICE_MAX));
            leave.setRejectReason(null);
            leave.setDoctorId(me);
            leave.setDoctorName(TextUtil.cut(myName, NAME_MAX));
            saveLeave(leave, false);
            contentSaved = true;

            // 批准即电子签名：签名失败随审批事务回滚，不允许「批了但没签名」的中间态
            SignCommandDTO cmd = new SignCommandDTO();
            cmd.setBizType(SignBizTypeEnum.INPATIENT_LEAVE.getCode());
            cmd.setBizId(leave.getId());
            cmd.setSignScene(SignSceneEnum.LEAVE_APPROVE.getCode());
            cmd.setSignerId(me);
            cmd.setSignerName(myName);
            CurrentUser user = UserUtils.getCurrentUser();
            if (user != null) {
                cmd.setSignerDeptId(user.getDeptId());
                cmd.setSignerDeptName(user.getDeptName());
            }
            cmd.setSignerTitle(TextUtil.cutToNull(bizInpatientLeaveMapper.selectEmployeeTitle(me), 50));
            cmd.setClientIp(dto.getClientIp());
            cmd.setRemark("住院请假单审批（" + BizInpatientLeave.typeText(leave.getLeaveType()) + "）");
            try {
                SignatureVO sig = emrSignatureService.sign(cmd);
                // 锚点三件套必须在这里一起写：provider 的 applySignAnchor 已把 sign_status 置 1，
                // 但下面还要用**签名前读出的实体**整行 updateById，漏写就把它冲回 0（签名失效、单据像没锁）。
                leave.setSignStatus(ObjectSignStatusEnum.SIGNED.getCode());
                leave.setSignId(sig.getId());
                leave.setSignedTime(sig.getSignedTime() == null ? null
                        : TimeUtil.toSeconds(sig.getSignedTime()));
            } catch (BusinessException e) {
                throw new BusinessException("请假单审批签名失败（" + leave.getLeaveNo() + "）：" + e.getMessage());
            }
            leave.setLeaveStatus(LeaveStatusEnum.APPROVED.getCode());
        } else {
            leave.setLeaveStatus(LeaveStatusEnum.REJECTED.getCode());
            // ①条件必填：仅拒绝分支必填理由，@NotNull 会把合法的批准请求挡成 400
            leave.setRejectReason(TextUtil.cut(TextUtil.requireTrimmed(dto.getRejectReason(), "拒绝必须填写理由（写清病情为什么不允许外出）"), REASON_MAX));
            leave.setDoctorAdvice(TextUtil.cutToNull(dto.getDoctorAdvice(), ADVICE_MAX));
            leave.setDoctorId(me);
            leave.setDoctorName(TextUtil.cut(myName, NAME_MAX));
        }
        leave.setApproveTime(TimeUtil.nowSeconds());
        // contentSaved 分支已写过一次，这里补写状态/时间；幂等无害
        saveLeave(leave, false);
        if (contentSaved) {
            log.info("住院请假单已批准并电子签名 leaveNo={} signId={}", leave.getLeaveNo(), leave.getSignId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmLeave(InpatientLeaveDTO.Confirm dto) {
        BizInpatientLeave leave = requireLeave(dto.getId());
        if (!Objects.equals(LeaveStatusEnum.APPROVED.getCode(), leave.getLeaveStatus())) {
            throw new BusinessException("只有「已批准」的请假单能登记离院，当前为「"
                    + BizInpatientLeave.statusText(leave.getLeaveStatus()) + "」（未批准不可放人）");
        }
        InpatientLeaveVO.Base snapshot = bizInpatientLeaveMapper.selectAdmissionBase(leave.getAdmissionId());
        if (snapshot == null || !Objects.equals(snapshot.getAdmitStatus(), AdmitStatusEnum.IN_HOSPITAL.getCode())) {
            throw new BusinessException("患者已不在院，不能登记离院");
        }
        assertDeptAccessible(leave.getDeptId());
        // 患方承诺三要素缺一不可 —— 「回去出事责任界定」靠的就是这张签字
        // D-业务规则：码值合法性（非空已由 DTO @NotNull 收口，这里挡的是选了非法码值的请求）
        if (!RELATIONS.contains(dto.getConfirmRelation())) {
            throw new BusinessException("确认人与患者的关系取值不合法（见字典 his_notice_relation，责任界定必填）");
        }
        String signature = TextUtil.trimToNull(dto.getConfirmSignature());
        if (!signature.startsWith("data:image/png;base64,")) {
            throw new BusinessException("手写签名必须是画板生成的 PNG 图片");
        }
        if (signature.length() > SIGNATURE_MAX) {
            throw new BusinessException("手写签名图片过大，请清空画板后重新签名");
        }
        LocalDateTime actualLeave = dto.getActualLeaveTime() == null ? TimeUtil.nowSeconds() : TimeUtil.toSeconds(dto.getActualLeaveTime());
        if (actualLeave.isAfter(TimeUtil.nowSeconds())) {
            throw new BusinessException("实际离院时间不能晚于当前时间");
        }
        if (leave.getExpectedLeaveTime() != null
                && actualLeave.isBefore(leave.getExpectedLeaveTime().minusHours(2))) {
            throw new BusinessException("实际离院时间比预计离院时间早 2 小时以上，请核对（登记错了会造成在途时间虚增）");
        }
        leave.setConfirmName(TextUtil.cut(TextUtil.trimToNull(dto.getConfirmName()), NAME_MAX));
        leave.setConfirmRelation(dto.getConfirmRelation());
        leave.setConfirmPhone(TextUtil.cut(TextUtil.trimToNull(dto.getConfirmPhone()), PHONE_MAX));
        leave.setConfirmSignature(signature);
        leave.setConfirmTime(actualLeave);
        leave.setActualLeaveTime(actualLeave);
        leave.setLeaveStatus(LeaveStatusEnum.LEFT.getCode());
        saveLeave(leave, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmBack(InpatientLeaveDTO.Back dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizInpatientLeave leave = requireLeave(dto.getId());
        if (!Objects.equals(LeaveStatusEnum.LEFT.getCode(), leave.getLeaveStatus())) {
            throw new BusinessException("只有「已离院」的请假单能销假，当前为「"
                    + BizInpatientLeave.statusText(leave.getLeaveStatus()) + "」");
        }
        assertDeptAccessible(leave.getDeptId());
        LocalDateTime now = TimeUtil.nowSeconds();
        if (leave.getActualLeaveTime() != null && now.isBefore(leave.getActualLeaveTime())) {
            throw new BusinessException("返回时间不能早于实际离院时间");
        }
        leave.setActualReturnTime(now);
        leave.setReturnNote(TextUtil.cutToNull(dto.getReturnNote(), NOTE_MAX));
        leave.setReturnBy(TextUtil.cutToNull(operatorUser.getRealName(), 64));
        leave.setLeaveStatus(LeaveStatusEnum.RETURNED.getCode());
        saveLeave(leave, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(InpatientLeaveDTO.Cancel dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizInpatientLeave leave = requireLeave(dto.getId());
        if (Objects.equals(LeaveStatusEnum.LEFT.getCode(), leave.getLeaveStatus())) {
            throw new BusinessException("已离院的请假单不能取消（人已经出去了，事实不能蒸发）——请等患者返回后销假");
        }
        if (!Objects.equals(LeaveStatusEnum.PENDING.getCode(), leave.getLeaveStatus())
                && !Objects.equals(LeaveStatusEnum.APPROVED.getCode(), leave.getLeaveStatus())) {
            throw new BusinessException("只有「待审批/已批准」的请假单能取消，当前为「"
                    + BizInpatientLeave.statusText(leave.getLeaveStatus()) + "」");
        }
        assertDeptAccessible(leave.getDeptId());
        leave.setLeaveStatus(LeaveStatusEnum.CANCELLED.getCode());
        leave.setCancelReason(TextUtil.cut(TextUtil.trimToNull(dto.getCancelReason()), REASON_MAX));
        leave.setCancelBy(TextUtil.cutToNull(operatorUser.getRealName(), 64));
        leave.setCancelTime(TimeUtil.nowSeconds());
        saveLeave(leave, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordContact(InpatientLeaveDTO.Contact dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizInpatientLeave leave = requireLeave(dto.getId());
        if (!Objects.equals(LeaveStatusEnum.LEFT.getCode(), leave.getLeaveStatus())) {
            throw new BusinessException("只有「已离院」的请假单记录超期处置，当前为「"
                    + BizInpatientLeave.statusText(leave.getLeaveStatus()) + "」");
        }
        if (!isOverdue(leave)) {
            throw new BusinessException("该单尚未超期（预计返回时间未到），无需超期处置");
        }
        if (dto.getContactResult() == null || !CONTACT_RESULTS.contains(dto.getContactResult())) {
            throw new BusinessException("联系结果取值不合法（见字典 his_leave_contact）");
        }
        if (dto.getReportTo() == null || !REPORT_TOS.contains(dto.getReportTo())) {
            throw new BusinessException("上报对象取值不合法（见字典 his_leave_report；联系不上必须升级上报）");
        }
        leave.setOverdueContactResult(dto.getContactResult());
        leave.setOverdueContactNote(TextUtil.cutToNull(dto.getContactNote(), NOTE_MAX));
        leave.setOverdueContactTime(TimeUtil.nowSeconds());
        leave.setOverdueContactBy(TextUtil.cutToNull(operatorUser.getRealName(), 64));
        leave.setReportTo(dto.getReportTo());
        saveLeave(leave, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void print(InpatientLeaveDTO.Print dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizInpatientLeave leave = requireLeave(dto.getId());
        if (!Objects.equals(LeaveStatusEnum.LEFT.getCode(), leave.getLeaveStatus())
                && !Objects.equals(LeaveStatusEnum.RETURNED.getCode(), leave.getLeaveStatus())) {
            throw new BusinessException("只有「已离院/已返回」的请假单打印承诺书（未离院的是没有患方签字的半张纸）");
        }
        leave.setPrinterName(operatorUser.getRealName());
        leave.setPrintCount(NumUtil.orDefault(leave.getPrintCount(), 0) + 1);
        leave.setLastPrintTime(TimeUtil.nowSeconds());
        saveLeave(leave, false);
    }

    private List<BizInpatientLeave> selectActiveLeaves(Long admissionId) {
        return bizInpatientLeaveMapper.selectList(new LambdaQueryWrapper<BizInpatientLeave>()
                .eq(BizInpatientLeave::getAdmissionId, admissionId)
                .in(BizInpatientLeave::getLeaveStatus,
                        LeaveStatusEnum.PENDING.getCode(), LeaveStatusEnum.APPROVED.getCode(), LeaveStatusEnum.LEFT.getCode())
                .orderByDesc(BizInpatientLeave::getId)
                .last("LIMIT 5"));
    }

    private BizInpatientLeave requireLeave(Long id) {
        BizInpatientLeave leave = id == null ? null : bizInpatientLeaveMapper.selectById(id);
        if (leave == null) {
            throw new BusinessException("请假单不存在或已删除");
        }
        return leave;
    }

    /**
     * 单次请假时长上限：系统参数缺失/非法回落 72，不回落成「无上限」
     */
    private long resolveMaxHours() {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, SystemConfigKeyConst.INPATIENT_LEAVE_MAX_HOURS));
        if (config == null || !TextUtil.hasText(config.getConfigValue())) {
            log.warn("未配置 {}，请假时长上限按兜底值 {} 小时", SystemConfigKeyConst.INPATIENT_LEAVE_MAX_HOURS, MAX_HOURS_FALLBACK);
            return MAX_HOURS_FALLBACK;
        }
        try {
            int hours = Integer.parseInt(config.getConfigValue().trim());
            if (hours <= 0) {
                log.warn("配置 {} = {} 非法（必须为正数），按兜底值 {} 小时", SystemConfigKeyConst.INPATIENT_LEAVE_MAX_HOURS, config.getConfigValue(), MAX_HOURS_FALLBACK);
                return MAX_HOURS_FALLBACK;
            }
            return hours;
        } catch (NumberFormatException e) {
            log.warn("配置 {} = {} 不是数字，按兜底值 {} 小时", SystemConfigKeyConst.INPATIENT_LEAVE_MAX_HOURS, config.getConfigValue(), MAX_HOURS_FALLBACK);
            return MAX_HOURS_FALLBACK;
        }
    }

    /**
     * 超期未归：expectedReturnTime 已过且状态=已离院（查询时算，不落状态列）
     */
    private boolean isOverdue(BizInpatientLeave leave) {
        return Objects.equals(LeaveStatusEnum.LEFT.getCode(), leave.getLeaveStatus())
                && leave.getExpectedReturnTime() != null
                && leave.getExpectedReturnTime().isBefore(LocalDateTime.now());
    }

    /**
     * 台账/详情行的超期展示态（时间字段在 SQL 已格式化为字符串，这里现算）
     */
    private void applyOverdue(InpatientLeaveVO.Row row) {
        boolean overdue = Objects.equals(LeaveStatusEnum.LEFT.getCode(), row.getLeaveStatus())
                && row.getExpectedReturnTime() != null
                && parse(row.getExpectedReturnTime()).isBefore(LocalDateTime.now());
        row.setOverdue(overdue);
        row.setOverdueHours(overdue
                ? java.time.Duration.between(parse(row.getExpectedReturnTime()), LocalDateTime.now()).toHours()
                : 0L);
    }

    private void applyOverdue(InpatientLeaveVO.Detail detail) {
        boolean overdue = Objects.equals(LeaveStatusEnum.LEFT.getCode(), detail.getLeaveStatus())
                && detail.getExpectedReturnTime() != null
                && parse(detail.getExpectedReturnTime()).isBefore(LocalDateTime.now());
        detail.setOverdue(overdue);
        detail.setOverdueHours(overdue
                ? java.time.Duration.between(parse(detail.getExpectedReturnTime()), LocalDateTime.now()).toHours()
                : 0L);
    }

    /**
     * 动作可用性全部由后端给出，前端不自判状态（同转科/输血口径）
     */
    private void applyActions(InpatientLeaveVO.Detail d) {
        Integer st = d.getLeaveStatus();
        boolean pending = Objects.equals(LeaveStatusEnum.PENDING.getCode(), st);
        boolean approved = Objects.equals(LeaveStatusEnum.APPROVED.getCode(), st);
        boolean left = Objects.equals(LeaveStatusEnum.LEFT.getCode(), st);
        d.setCanEdit(pending && !Boolean.TRUE.equals(d.getOverdue()));
        d.setCanApprove(pending);
        d.setCanLeave(approved);
        d.setCanBack(left);
        d.setCanCancel(pending || approved);
        d.setCanContact(left && Boolean.TRUE.equals(d.getOverdue()));
        d.setCanPrint(left || Objects.equals(LeaveStatusEnum.RETURNED.getCode(), st));
    }

    private void assertDeptAccessible(Long deptId) {
        if (!deptScopeService.canAccessDept(deptId)) {
            throw new BusinessException("该请假单所属科室不在当前岗位的数据范围内");
        }
    }

    /**
     * 返回 null=不受限；非空=收口科室集合（显式 deptId 由 DeptScopeProvider 校验越权）。
     *
     * <p><b>空授权要用哨兵</b>：受限但一个科室都没配时若直接返回空集合，动态 SQL 会拼出
     * {@code dept_id IN ()}，MySQL 报语法错误被兜成 500 —— 看上去像后端挂了，其实是「什么都看不到」。
     */
    private List<Long> scopedDeptIds(Long requestedDeptId) {
        Long resolved = deptScopeService.resolveDeptId(requestedDeptId);
        if (resolved != null) {
            return null;
        }
        Set<Long> allowed = deptScopeService.allowedDeptIds();
        if (allowed == null) {
            return null;
        }
        return allowed.isEmpty() ? List.of(-1L) : List.copyOf(allowed);
    }

    private void saveLeave(BizInpatientLeave leave, boolean isNew) {
        if (isNew) {
            bizInpatientLeaveMapper.insert(leave);
        } else {
            bizInpatientLeaveMapper.updateById(leave);
        }
    }

}
