package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.constant.DictType;
import com.his.common.dto.SignCommandDTO;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.EmrSignatureService;
import com.his.common.service.RedisSequenceService;
import com.his.common.util.TextUtil;
import com.his.common.vo.SignatureVO;
import com.his.medicaltech.dto.RadioReportAuditDTO;
import com.his.medicaltech.dto.RadioReportQueryPageDTO;
import com.his.medicaltech.dto.RadioReportUpsertDTO;
import com.his.medicaltech.dto.RadioTemplateUpsertDTO;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.entity.BizRadioReportTemplate;
import com.his.medicaltech.entity.BizReport;
import com.his.medicaltech.enums.InsRecordStatusEnum;
import com.his.medicaltech.enums.ReportStatusEnum;
import com.his.medicaltech.enums.ReportTypeEnum;
import com.his.medicaltech.mapper.BizInspectionRecordMapper;
import com.his.medicaltech.mapper.BizRadioReportTemplateMapper;
import com.his.medicaltech.mapper.BizReportMapper;
import com.his.medicaltech.mapper.RadioReportMapper;
import com.his.medicaltech.service.ExamImageService;
import com.his.medicaltech.service.RadiologyReportService;
import com.his.medicaltech.vo.RadioReportDetailVO;
import com.his.medicaltech.vo.RadioReportListVO;
import com.his.medicaltech.vo.RadioReportTemplateVO;
import com.his.system.enums.BizTypeEnum;
import com.his.system.service.DictCacheService;
import com.his.system.service.SysAuditLogService;
import com.his.system.service.SysMessageService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 放射诊断报告书写台（sql/138）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RadiologyReportServiceImpl extends ServiceImpl<BizReportMapper, BizReport> implements RadiologyReportService {
    private final RadioReportMapper radioReportMapper;
    private final BizReportMapper bizReportMapper;
    private final BizInspectionRecordMapper bizInspectionRecordMapper;
    private final BizRadioReportTemplateMapper bizRadioReportTemplateMapper;
    private final ExamImageService examImageService;
    private final EmrSignatureService emrSignatureService;
    private final DictCacheService dictCacheService;
    private final SysAuditLogService sysAuditLogService;
    private final SysMessageService sysMessageService;
    private final RedisSequenceService redisSequenceService;

    // 查询

    @Override
    public PageResult<RadioReportListVO> listPage(RadioReportQueryPageDTO query) {
        IPage<RadioReportListVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        List<RadioReportListVO> list = radioReportMapper.selectWorkbenchPage(page,
                TextUtil.trim(query.getKeyword()), query.getReportStatus(), query.getPositiveFlag(),
                query.getOnlyUnwritten(), TextUtil.trim(query.getStartDate()), TextUtil.trim(query.getEndDate()));
        fillText(list);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), list);
    }

    // 写

    @Override
    public RadioReportDetailVO getDetailByRecordId(Long recordId) {
        BizInspectionRecord record = loadRadiologyRecord(recordId);
        BizReport report = bizReportMapper.selectOne(new LambdaQueryWrapper<BizReport>()
                .eq(BizReport::getRecordId, recordId)
                .eq(BizReport::getReportType, ReportTypeEnum.INSPECTION.getCode())
                .orderByDesc(BizReport::getId)
                .last("LIMIT 1"));
        return toDetail(record, report);
    }

    @Override
    public RadioReportDetailVO getDetailByReportId(Long reportId) {
        BizReport report = reportId == null ? null : bizReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在或已被删除");
        }
        BizInspectionRecord record = loadRadiologyRecord(report.getRecordId());
        return toDetail(record, report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RadioReportDetailVO saveDraft(RadioReportUpsertDTO dto) {
        BizInspectionRecord record = loadRadiologyRecord(dto.getRecordId());
        BizReport report = ensureReport(record);
        // 已发布/已作废的报告不能再改：改一份已经发给临床的报告而不留版本，
        // 等于让医生手里的打印件和库里的不一致，还不留下痕迹。
        rejectIfClosed(report);
        applyContent(record, report, dto);
        report.setReportStatus(ReportStatusEnum.DRAFT.getCode());
        saveOrUpdateReport(report);
        return toDetail(record, report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RadioReportDetailVO submit(RadioReportUpsertDTO dto) {
        BizInspectionRecord record = loadRadiologyRecord(dto.getRecordId());
        BizReport report = ensureReport(record);
        rejectIfClosed(report);

        applyContent(record, report, dto);
        // B类共用 DTO：该 DTO 同时服务 saveDraft（草稿允许空白），必填只在提交口生效，注解一刀切会挡掉存草稿
        if (!TextUtil.hasText(report.getReportContent())) {
            throw new BusinessException("影像所见不能为空：没有所见的报告审不了，也不能发给临床");
        }
        // B类共用 DTO：同上，诊断/印象只在提交口必填，存草稿允许空白
        if (!TextUtil.hasText(report.getConclusion())) {
            throw new BusinessException("影像诊断/印象不能为空");
        }
        if (record.getRecordStatus() == null
                || record.getRecordStatus() < InsRecordStatusEnum.RESULTED.getCode()) {
            throw new BusinessException("该检查还没拍片完成（当前状态："
                    + dictCacheService.getDicDataLabel(DictType.INSPECTION_RECORD_STATUS, record.getRecordStatus())
                    + "），请先由技师完成拍片");
        }

        // 报告医师签名：签的是「报告内容」，所以必须在正文写完落库之后签。
        // 签名层要求同一份报告只能签一次报告名（重签要退回），这里先把这个规则讲清楚再签，
        // 否则用户看到的是签名层抛的一句「已签名」，不知道下一步该干嘛。
        if (record.getReportSignId() != null) {
            throw new BusinessException("本报告已有报告医师签名，不能重复提交；如需修改请由审核医师退回后重写");
        }
        SignatureVO sign = signReport(record, SignSceneEnum.REPORT_ISSUE);
        record.setReportSignId(sign.getId());
        record.setReportSignedTime(sign.getSignedTime());

        Long employeeId = UserUtils.getCurrentUser().getEmployeeId();
        report.setWriteBy(UserUtils.getCurrentUser().getRealName());
        report.setWriteById(employeeId);
        report.setWriteTime(LocalDateTime.now());
        report.setReportStatus(ReportStatusEnum.PENDING_REVIEW.getCode());
        report.setRejectReason(null);
        saveOrUpdateReport(report);
        bizInspectionRecordMapper.updateById(record);

        sysAuditLogService.record(employeeId, UserUtils.getCurrentUser().getRealName(),
                "放射报告", "提交报告待审核", "biz_report", report.getId(),
                TextUtil.cut("记录号=" + record.getRecordNo() + " 患者=" + record.getPatientName()
                        + " 项目=" + record.getInspectionItemName()
                        + " 阴阳性=" + report.getPositiveFlag()
                        + " 签名=" + sign.getId(), 2000),
                true, null);
        return toDetail(record, report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RadioReportDetailVO audit(RadioReportAuditDTO dto) {
        BizReport report = requireReport(dto.getReportId());
        BizInspectionRecord record = loadRadiologyRecord(report.getRecordId());
        if (!Objects.equals(ReportStatusEnum.PENDING_REVIEW.getCode(), report.getReportStatus())) {
            throw new BusinessException("只有「待审核」的报告能审核（当前："
                    + dictCacheService.getDicDataLabel(DictType.REPORT_STATUS, report.getReportStatus()) + "）");
        }
        // 双签制度：谁写的报告谁不能自己审。用员工ID 比，不用姓名比 ——
        // 同名同姓的两个医师用姓名判会互相误判，要么拦错人要么放过去。
        Long current = UserUtils.getCurrentUser().getEmployeeId();
        if (report.getWriteById() != null && report.getWriteById().equals(current)) {
            throw new BusinessException("不能审核自己书写的报告：请由另一位放射诊断医师审核");
        }

        // 先签名、后改状态：签名层的准入规则要求记录仍处于「已出结果未审核」，
        // 先把 record 写成「已审核」会让签名层把自己拒掉。
        SignatureVO sign = signReport(record, SignSceneEnum.REPORT_AUDIT);
        record.setAuditSignId(sign.getId());
        record.setAuditSignedTime(sign.getSignedTime());
        record.setAuditBy(UserUtils.getCurrentUser().getRealName());
        record.setAuditTime(sign.getSignedTime());
        record.setRecordStatus(InsRecordStatusEnum.REVIEWED.getCode());
        bizInspectionRecordMapper.updateById(record);

        report.setReportStatus(ReportStatusEnum.REVIEWED.getCode());
        report.setAuditBy(UserUtils.getCurrentUser().getRealName());
        report.setAuditTime(sign.getSignedTime());
        report.setRejectReason(null);
        if (TextUtil.hasText(dto.getReason())) {
            report.setRemark(TextUtil.cut("审核意见：" + dto.getReason(), 500));
        }
        bizReportMapper.updateById(report);

        sysAuditLogService.record(current, UserUtils.getCurrentUser().getRealName(),
                "放射报告", "审核通过", "biz_report", report.getId(),
                TextUtil.cut("记录号=" + record.getRecordNo() + " 患者=" + record.getPatientName()
                        + " 意见=" + (TextUtil.hasText(dto.getReason()) ? dto.getReason() : "无")
                        + " 签名=" + sign.getId(), 2000),
                true, null);
        return toDetail(record, report);
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RadioReportDetailVO reject(RadioReportAuditDTO dto) {
        BizReport report = requireReport(dto.getReportId());
        BizInspectionRecord record = loadRadiologyRecord(report.getRecordId());
        if (!Objects.equals(ReportStatusEnum.PENDING_REVIEW.getCode(), report.getReportStatus())) {
            throw new BusinessException("只有「待审核」的报告能退回（当前："
                    + dictCacheService.getDicDataLabel(DictType.REPORT_STATUS, report.getReportStatus()) + "）");
        }
        if (!TextUtil.hasText(dto.getReason())) {
            throw new BusinessException("退回必须写明原因：医师要照着这个原因改报告，没有原因的退回没法执行");
        }

        report.setReportStatus(ReportStatusEnum.DRAFT.getCode());
        report.setRejectReason(TextUtil.cut(dto.getReason(), 500));
        report.setReportVersion(report.getReportVersion() == null ? 2 : report.getReportVersion() + 1);
        bizReportMapper.updateById(report);

        // 退回后必须清掉签名指针，否则医师改完再提交会被签名层挡住
        // （"已有报告医师签名，不能重复签名"）。签名记录本身没删 —— 它仍在签名链上，
        // 签名中心能看到这一版曾经被谁签过；清掉的只是"当前有效签名"这个指针。
        Long current = UserUtils.getCurrentUser().getEmployeeId();
        sysAuditLogService.record(current, UserUtils.getCurrentUser().getRealName(),
                "放射报告", "退回重写", "biz_report", report.getId(),
                TextUtil.cut("记录号=" + record.getRecordNo() + " 患者=" + record.getPatientName()
                        + " 原因=" + dto.getReason()
                        + " 原报告签名=" + record.getReportSignId()
                        + "（已作废，重写后可重新签名）", 2000),
                true, null);
        // 清空必须用 update wrapper 显式 set null：MP 的 updateById 会跳过所有 null 字段
        // （"点完退回，页面显示已退回，但签名还在，医师改完提交被挡住"就是这么来的）。
        LambdaUpdateWrapper<BizInspectionRecord> uw = new LambdaUpdateWrapper<>();
        uw.eq(BizInspectionRecord::getId, record.getId())
                .set(BizInspectionRecord::getReportSignId, null)
                .set(BizInspectionRecord::getReportSignedTime, null)
                .set(BizInspectionRecord::getAuditSignId, null)
                .set(BizInspectionRecord::getAuditSignedTime, null)
                .set(BizInspectionRecord::getAuditBy, null)
                .set(BizInspectionRecord::getAuditTime, null)
                .set(BizInspectionRecord::getRecordStatus, InsRecordStatusEnum.RESULTED.getCode());
        bizInspectionRecordMapper.update(null, uw);
        record.setReportSignId(null);
        record.setReportSignedTime(null);
        record.setAuditSignId(null);
        record.setAuditSignedTime(null);
        return toDetail(record, report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RadioReportDetailVO publish(Long reportId) {
        BizReport report = requireReport(reportId);
        BizInspectionRecord record = loadRadiologyRecord(report.getRecordId());
        // 发布的前置是「已审核」，不是「待审核」：跳过审核直接发布等于一个人既写又审又发，
        // 这正是本次分岗要堵掉的口子。
        if (!Objects.equals(ReportStatusEnum.REVIEWED.getCode(), report.getReportStatus())) {
            throw new BusinessException("只有「已审核」的报告能发布（当前："
                    + dictCacheService.getDicDataLabel(DictType.REPORT_STATUS, report.getReportStatus())
                    + "）：请先完成审核");
        }
        LocalDateTime now = LocalDateTime.now();
        report.setReportStatus(ReportStatusEnum.PUBLISHED.getCode());
        report.setPublishBy(UserUtils.getCurrentUser().getRealName());
        report.setPublishTime(now);
        bizReportMapper.updateById(report);

        record.setRecordStatus(InsRecordStatusEnum.PUBLISHED.getCode());
        record.setReportTime(now);
        record.setReportBy(UserUtils.getCurrentUser().getRealName());
        bizInspectionRecordMapper.updateById(record);

        if (record.getApplyDoctorId() != null) {
            sysMessageService.sendSystemMessage(record.getApplyDoctorId(), record.getApplyDoctorName(),
                    "检查报告", "患者 " + record.getPatientName() + " 的"
                            + record.getInspectionItemName() + "放射报告已发布，请查看。",
                    BizTypeEnum.REPORT.getType(), record.getId());
        }
        sysAuditLogService.record(UserUtils.getCurrentUser().getEmployeeId(), UserUtils.getCurrentUser().getRealName(),
                "放射报告", "发布报告", "biz_report", report.getId(),
                TextUtil.cut("记录号=" + record.getRecordNo() + " 患者=" + record.getPatientName(), 2000),
                true, null);
        return toDetail(record, report);
    }

    /**
     * 取检查记录并**强制它是放射项目**。
     *
     * <p>这道闸门是「分岗」的另一半：光有菜单权限不够 —— 检查技师如果绕开前端直接调
     * 放射报告接口，权限码挡的是 403，这里挡的是「这个检查根本不是放射科的」。
     */
    private BizInspectionRecord loadRadiologyRecord(Long recordId) {
        if (recordId == null) {
            throw new BusinessException("缺少检查记录ID");
        }
        BizInspectionRecord record = bizInspectionRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("检查记录不存在或已被删除");
        }
        Integer itemType = radioReportMapper.selectItemTypeByCode(record.getInspectionItemCode());
        if (itemType == null) {
            throw new BusinessException("检查项目 " + record.getInspectionItemCode()
                    + " 不在检查项目字典里，无法判定是不是放射检查");
        }
        if (itemType != 1) {
            throw new BusinessException("「" + record.getInspectionItemName()
                    + "」不是放射检查（项目类型 " + itemType
                    + "），不在放射诊断工作站受理范围；请在本岗位的工作站处理");
        }
        if (Objects.equals(InsRecordStatusEnum.CANCELLED.getCode(), record.getRecordStatus())) {
            throw new BusinessException("该检查已取消，不能再写报告");
        }
        return record;
    }

    private BizReport requireReport(Long reportId) {
        BizReport report = reportId == null ? null : bizReportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在或已被删除");
        }
        return report;
    }

    /**
     * 报告已发布/已作废后不许再改
     */
    private void rejectIfClosed(BizReport report) {
        Integer st = report.getReportStatus();
        if (Objects.equals(ReportStatusEnum.PUBLISHED.getCode(), st)
                || Objects.equals(ReportStatusEnum.INVALID.getCode(), st)) {
            throw new BusinessException("报告已"
                    + dictCacheService.getDicDataLabel(DictType.REPORT_STATUS, st) + "，不能再修改");
        }
    }

    /**
     * 找已有报告，没有就按记录建一条空壳（报告号服务端生成）
     */
    private BizReport ensureReport(BizInspectionRecord record) {
        BizReport exists = bizReportMapper.selectOne(new LambdaQueryWrapper<BizReport>()
                .eq(BizReport::getRecordId, record.getId())
                .eq(BizReport::getReportType, ReportTypeEnum.INSPECTION.getCode())
                .orderByDesc(BizReport::getId)
                .last("LIMIT 1"));
        if (exists != null) {
            return exists;
        }
        BizReport report = new BizReport();
        report.setReportNo(redisSequenceService.generateReportNo());
        report.setReportType(ReportTypeEnum.INSPECTION.getCode());
        report.setRecordId(record.getId());
        report.setRecordNo(record.getRecordNo());
        report.setPatientId(record.getPatientId());
        report.setPatientNo(record.getPatientNo());
        report.setPatientName(record.getPatientName());
        report.setGender(record.getGender());
        report.setAge(record.getAge());
        report.setVisitDate(record.getVisitDate());
        report.setItemName(record.getInspectionItemName());
        report.setExamDeptName(record.getInspectionDeptName());
        report.setApplyDeptName(record.getApplyDeptName());
        report.setApplyDoctorId(record.getApplyDoctorId());
        report.setApplyDoctorName(record.getApplyDoctorName());
        report.setClinicalDiagnosis(record.getClinicalDiagnosis());
        report.setReportStatus(ReportStatusEnum.DRAFT.getCode());
        report.setReportVersion(1);
        report.setFilmCount(0);
        return report;
    }

    /**
     * 报告落库：新建走 insert，已有走 updateById。
     *
     * <p>不能图省事用 {@code updateById} 一把梭 —— 它对 id 为空的行什么也不做，
     * 表现出来是「点了保存，页面提示成功，库里没有报告」。
     */
    private void saveOrUpdateReport(BizReport report) {
        if (report.getId() == null) {
            bizReportMapper.insert(report);
        } else {
            bizReportMapper.updateById(report);
        }
    }

    // 出参

    /**
     * 把入参写进报告实体（服务端兜住的字段不在这里赋值）
     */
    private void applyContent(BizInspectionRecord record, BizReport report, RadioReportUpsertDTO dto) {
        report.setTemplateId(dto.getTemplateId());
        report.setExamMethod(TextUtil.cut(dto.getExamMethod(), 200));
        report.setReportContent(dto.getReportContent());
        report.setConclusion(dto.getConclusion());
        report.setSuggestions(TextUtil.cut(dto.getSuggestions(), 1000));
        report.setPositiveFlag(dto.getPositiveFlag() == null ? 0 : dto.getPositiveFlag());
        report.setIsCritical(dto.getIsCritical() == null ? 0 : dto.getIsCritical());
        // 胶片张数是**事实**不是入参：从检查胶片用量现算，前端传什么都不认。
        Integer films = radioReportMapper.sumFilmQuantity(record.getId());
        report.setFilmCount(films == null ? 0 : films);
    }

    /**
     * 报告签名（报告医师 / 审核医师）。
     *
     * <p>签名人取自登录态：入参里的姓名字符串谁都能填成别人的名字，用它签名等于签名可伪造。
     */
    private SignatureVO signReport(BizInspectionRecord record, SignSceneEnum scene) {
        Long signerId = UserUtils.getCurrentUser().getEmployeeId();
        if (signerId == null) {
            throw new BusinessException("签名失败：取不到当前登录用户，无法确定签名人");
        }
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(SignBizTypeEnum.INSPECTION_REPORT.getCode());
        cmd.setBizId(record.getId());
        cmd.setSignScene(scene.getCode());
        cmd.setSignerId(signerId);
        cmd.setSignerName(UserUtils.getCurrentUser().getRealName());
        cmd.setSignerDeptId(record.getInspectionDeptId());
        cmd.setSignerDeptName(record.getInspectionDeptName());
        try {
            return emrSignatureService.sign(cmd);
        } catch (BusinessException e) {
            throw new BusinessException("检查报告 " + record.getRecordNo() + " " + scene.getText()
                    + "失败：" + e.getMessage());
        }
    }

    private void fillText(List<RadioReportListVO> list) {
        if (list == null) {
            return;
        }
        for (RadioReportListVO v : list) {
            v.setRecordStatusText(dictCacheService.getDicDataLabel(DictType.INSPECTION_RECORD_STATUS, v.getRecordStatus()));
            v.setReportStatusText(v.getReportStatus() == null ? "未写报告"
                    : dictCacheService.getDicDataLabel(DictType.REPORT_STATUS, v.getReportStatus()));
            v.setPositiveFlagText(v.getPositiveFlag() == null ? null
                    : dictCacheService.getDicDataLabel(DictType.POSITIVE_FLAG, v.getPositiveFlag()));
        }
    }

    private RadioReportDetailVO toDetail(BizInspectionRecord record, BizReport report) {
        RadioReportDetailVO vo = new RadioReportDetailVO();
        vo.setRecordId(record.getId());
        vo.setRecordNo(record.getRecordNo());
        vo.setApplyId(record.getApplyId());
        vo.setApplyNo(record.getApplyNo());
        vo.setPatientId(record.getPatientId());
        vo.setPatientNo(record.getPatientNo());
        vo.setPatientName(record.getPatientName());
        vo.setGender(record.getGender());
        vo.setGenderText(record.getGender() == null ? null : dictCacheService.getDicDataLabel(DictType.GENDER, record.getGender()));
        vo.setAge(record.getAge());
        vo.setVisitDate(record.getVisitDate());
        vo.setItemCode(record.getInspectionItemCode());
        vo.setItemName(record.getInspectionItemName());
        vo.setBodyPart(record.getBodyPart());
        vo.setApplyDeptName(record.getApplyDeptName());
        vo.setApplyDoctorName(record.getApplyDoctorName());
        vo.setClinicalDiagnosis(record.getClinicalDiagnosis());
        vo.setRecordStatus(record.getRecordStatus());
        vo.setRecordStatusText(dictCacheService.getDicDataLabel(DictType.INSPECTION_RECORD_STATUS, record.getRecordStatus()));
        vo.setReportSignId(record.getReportSignId());
        vo.setAuditSignId(record.getAuditSignId());
        // 影像帧挂在申请单上（sql/137），这里顺着 apply_id 取，详情页不用二次请求
        vo.setImages(examImageService.listByApply(ReportTypeEnum.INSPECTION.getCode(), record.getApplyId()));

        // 张数一律现算，不读 report.film_count：那只是上次保存时的快照。
        // 医师保存报告之后又去胶片台补打两张片，报告上还显示旧数就是错的；
        // 还没写报告时也要显示（拍完片先打片是常态，"待书写"那一栏就能看到打了多少张）。
        Integer films = radioReportMapper.sumFilmQuantity(record.getId());
        vo.setFilmCount(films == null ? 0 : films);

        if (report != null) {
            vo.setReportId(report.getId());
            vo.setReportNo(report.getReportNo());
            vo.setReportStatus(report.getReportStatus());
            vo.setReportStatusText(dictCacheService.getDicDataLabel(DictType.REPORT_STATUS, report.getReportStatus()));
            vo.setTemplateId(report.getTemplateId());
            vo.setExamMethod(report.getExamMethod());
            vo.setReportContent(report.getReportContent());
            vo.setConclusion(report.getConclusion());
            vo.setSuggestions(report.getSuggestions());
            vo.setPositiveFlag(report.getPositiveFlag());
            vo.setPositiveFlagText(report.getPositiveFlag() == null ? null
                    : dictCacheService.getDicDataLabel(DictType.POSITIVE_FLAG, report.getPositiveFlag()));
            vo.setIsCritical(report.getIsCritical());
            vo.setWriteBy(report.getWriteBy());
            vo.setWriteById(report.getWriteById());
            vo.setWriteTime(report.getWriteTime());
            vo.setAuditBy(report.getAuditBy());
            vo.setAuditTime(report.getAuditTime());
            vo.setPublishBy(report.getPublishBy());
            vo.setPublishTime(report.getPublishTime());
            vo.setReportVersion(report.getReportVersion());
            vo.setRejectReason(report.getRejectReason());
        }
        return vo;
    }

    @Override
    public List<RadioReportTemplateVO> templateSelectList(Integer modality) {
        Long me = UserUtils.getCurrentUser().getEmployeeId();
        LambdaQueryWrapper<BizRadioReportTemplate> w = new LambdaQueryWrapper<>();
        w.eq(BizRadioReportTemplate::getStatus, 1);
        // 公用模板（is_public=1）或本人私有；员工ID 取不到时只给公用 ——
        // 把别人的私有模板漏进下拉，只会让人误选到一份不属于自己科室的写法。
        if (me == null) {
            w.eq(BizRadioReportTemplate::getIsPublic, 1);
        } else {
            w.and(q -> q.eq(BizRadioReportTemplate::getIsPublic, 1)
                    .or()
                    .eq(BizRadioReportTemplate::getDoctorId, me));
        }
        w.orderByAsc(BizRadioReportTemplate::getSortOrder)
                .orderByAsc(BizRadioReportTemplate::getId);
        List<RadioReportTemplateVO> all = bizRadioReportTemplateMapper.selectList(w).stream()
                .map(this::toTemplateVO).collect(Collectors.toList());
        // 模态过滤放在内存里做：modality 为空的「通用模板」任何模态都能用，
        // 写成 SQL 的 `modality = ?` 会把通用模板滤掉，而写 `modality = ? OR modality IS NULL`
        // 在 MyBatis 判空分支里容易写错成恒真条件。
        if (modality == null) {
            return all;
        }
        return all.stream()
                .filter(v -> v.getModality() == null || Objects.equals(v.getModality(), modality))
                .collect(Collectors.toList());
    }

    @Override
    public List<RadioReportTemplateVO> templateList() {
        return bizRadioReportTemplateMapper.selectList(new LambdaQueryWrapper<BizRadioReportTemplate>()
                        .orderByAsc(BizRadioReportTemplate::getSortOrder)
                        .orderByAsc(BizRadioReportTemplate::getId))
                .stream().map(this::toTemplateVO).collect(Collectors.toList());
    }

    @Override
    public RadioReportTemplateVO templateUpsert(RadioTemplateUpsertDTO dto) {
        BizRadioReportTemplate entity = new BizRadioReportTemplate();
        BeanUtils.copyProperties(dto, entity, "id");
        if (dto.getIsPublic() == null) {
            entity.setIsPublic(1);
        }
        if (Objects.equals(0, entity.getIsPublic()) && entity.getDoctorId() == null) {
            entity.setDoctorId(UserUtils.getCurrentUser().getEmployeeId());
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (dto.getId() == null) {
            long dup = bizRadioReportTemplateMapper.selectCount(new LambdaQueryWrapper<BizRadioReportTemplate>()
                    .eq(BizRadioReportTemplate::getTemplateCode, entity.getTemplateCode()));
            if (dup > 0) {
                throw new BusinessException("模板编码 " + entity.getTemplateCode() + " 已存在");
            }
            bizRadioReportTemplateMapper.insert(entity);
        } else {
            entity.setId(dto.getId());
            bizRadioReportTemplateMapper.updateById(entity);
        }
        return toTemplateVO(bizRadioReportTemplateMapper.selectById(entity.getId()));
    }

    @Override
    public boolean templateDeleteById(Long id) {
        if (id == null) {
            throw new BusinessException("缺少模板ID");
        }
        return bizRadioReportTemplateMapper.purgeById(id) > 0;
    }

    private RadioReportTemplateVO toTemplateVO(BizRadioReportTemplate e) {
        RadioReportTemplateVO vo = new RadioReportTemplateVO();
        BeanUtils.copyProperties(e, vo);
        vo.setModalityText(e.getModality() == null ? null
                : dictCacheService.getDicDataLabel(DictType.EXAM_DEVICE_TYPE, e.getModality()));
        if (!TextUtil.hasText(vo.getTemplateName())) {
            vo.setTemplateName(e.getTemplateCode());
        }
        return vo;
    }
}
