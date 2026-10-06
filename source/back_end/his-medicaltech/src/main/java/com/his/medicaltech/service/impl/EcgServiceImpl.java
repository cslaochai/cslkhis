package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.dto.SignCommandDTO;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;
import com.his.common.service.EmrSignatureService;
import com.his.common.vo.SignatureVO;
import com.his.medicaltech.dto.EcgAuditDTO;
import com.his.medicaltech.dto.EcgCollectWaveDTO;
import com.his.medicaltech.dto.EcgHolterUpsertDTO;
import com.his.medicaltech.dto.EcgMeasureUpsertDTO;
import com.his.medicaltech.dto.EcgQueryPageDTO;
import com.his.medicaltech.dto.EcgReportUpsertDTO;
import com.his.medicaltech.dto.EcgSimulateDTO;
import com.his.medicaltech.dto.EcgTemplateUpsertDTO;
import com.his.medicaltech.entity.BizEcgHolter;
import com.his.medicaltech.entity.BizEcgMeasure;
import com.his.medicaltech.entity.BizEcgTemplate;
import com.his.medicaltech.entity.BizEcgWaveform;
import com.his.medicaltech.mapper.BizEcgHolterMapper;
import com.his.medicaltech.mapper.BizEcgMeasureMapper;
import com.his.medicaltech.mapper.BizEcgTemplateMapper;
import com.his.medicaltech.mapper.BizEcgWaveformMapper;
import com.his.medicaltech.mapper.EcgMapper;
import com.his.medicaltech.service.EcgService;
import com.his.medicaltech.support.EcgWaveSimulator;
import com.his.medicaltech.vo.EcgDetailVO;
import com.his.medicaltech.vo.EcgListVO;
import com.his.medicaltech.vo.EcgTemplateVO;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.entity.BizReport;
import com.his.medicaltech.enums.InsRecordStatusEnum;
import com.his.medicaltech.enums.ReportStatusEnum;
import com.his.medicaltech.enums.ReportTypeEnum;
import com.his.medicaltech.mapper.BizInspectionRecordMapper;
import com.his.medicaltech.mapper.BizReportMapper;
import com.his.medicaltech.mapper.RadioReportMapper;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import com.his.system.service.SysAuditLogService;
import com.his.system.enums.BizTypeEnum;
import com.his.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 心电工作站（sql/173）。
 *
 * <p>「波形成文」的服务端闸门都在本类：
 * <ul>
 *   <li>报告提交必须已有波形（没有波形的报告没有临床证据）；</li>
 *   <li>ecg_type=2（Holter）的提交必须已有分析结果（Holter 动态心电）；</li>
 *   <li>不能自审（用员工ID比，不用姓名比）；</li>
 *   <li>发布必须先审核（跳过审核 = 一个人既写又审又发）。</li>
 * </ul>
 * 与放射（sql/138）同款：报告进报告单，检查记录强制校验 item_type=3。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EcgServiceImpl implements EcgService {
    /** 分页每页条数上限（技术阈值，防止前端传入超大值把库拖垮；DTO 迁 PageParam 后在此夹取） */
    private static final int MAX_PAGE_SIZE = 200;


    private final EcgMapper ecgMapper;
    private final BizReportMapper reportMapper;
    private final BizInspectionRecordMapper inspectionRecordMapper;
    private final EmrSignatureService signatureService;
    private final DictCacheService subDictText;
    private final SysAuditLogService sysAuditLogService;
    private final SysMessageService sysMessageService;
    private final BizEcgWaveformMapper waveformMapper;
    private final BizEcgMeasureMapper measureMapper;
    private final BizEcgHolterMapper holterMapper;
    private final BizEcgTemplateMapper templateMapper;
    private final EcgWaveSimulator waveSimulator;

    /** 复用放射模块的 item_type 查询（同一张检查项目字典，同一模块内共享 bean） */
    private final RadioReportMapper radioReportMapper;

    // 查询

    @Override
    public PageResult<EcgListVO> listPage(EcgQueryPageDTO query) {
        int pageNum = Math.max(query.getPageNum(), 1);
        int pageSize = Math.min(Math.max(query.getPageSize(), 1), MAX_PAGE_SIZE);
        IPage<EcgListVO> page = new Page<>(pageNum, pageSize);
        List<EcgListVO> list = ecgMapper.selectWorkbenchPage(page,
                trim(query.getKeyword()), query.getCollectPending(), query.getOnlyUnwritten(),
                query.getReportStatus(), trim(query.getStartDate()), trim(query.getEndDate()));
        fillText(list);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), list);
    }

    @Override
    public EcgDetailVO getDetailByRecordId(Long recordId) {
        EcgListVO row = loadWorkbenchRow(recordId);
        BizReport report = row.getReportId() == null ? null : reportMapper.selectById(row.getReportId());
        BizEcgWaveform wave = row.getWaveId() == null ? null : waveformMapper.selectById(row.getWaveId());
        BizEcgMeasure measure = row.getMeasureId() == null ? null : measureMapper.selectById(row.getMeasureId());
        BizEcgHolter holter = row.getHolterId() == null ? null : holterMapper.selectById(row.getHolterId());
        return toDetail(row, wave, measure, holter, report);
    }

    // 采集

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EcgDetailVO checkIn(Long recordId) {
        BizInspectionRecord record = loadEcgRecord(recordId);
        if (!Objects.equals(InsRecordStatusEnum.REGISTERED.getCode(), record.getRecordStatus())) {
            throw new BusinessException("只有「已登记」的检查能签到（当前："
                    + subDictText.getDicDataLabel("his_inspection_record_status", record.getRecordStatus()) + "）");
        }
        record.setRecordStatus(InsRecordStatusEnum.SIGNED_IN.getCode());
        record.setCheckInTime(LocalDateTime.now());
        inspectionRecordMapper.updateById(record);
        return getDetailByRecordId(recordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EcgDetailVO collectWave(EcgCollectWaveDTO dto) {
        BizInspectionRecord record = loadCollectableRecord(dto.getRecordId());
        if (!StringUtils.hasText(dto.getWaveData())) {
            throw new BusinessException("波形数据为空：设备推送内容缺失，请重新采集或走模拟采集");
        }
        return persistWave(record, dto.getEcgType(), dto.getWaveData(),
                StringUtils.hasText(dto.getDeviceNo()) ? dto.getDeviceNo() : "DEV");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EcgDetailVO simulateWave(EcgSimulateDTO dto) {
        BizInspectionRecord record = loadCollectableRecord(dto.getRecordId());
        // ecg_type 判定：24小时动态心电图（ECG002）按 Holter 采集，其余按常规
        int ecgType = record.getInspectionItemCode() != null
                && record.getInspectionItemName() != null
                && record.getInspectionItemName().contains("动态") ? 2 : 1;
        String waveData = waveSimulator.simulate(dto.getRhythmCode());
        return persistWave(record, ecgType, waveData, "SIM");
    }

    private EcgDetailVO persistWave(BizInspectionRecord record, Integer ecgType, String waveData, String deviceNo) {
        BizEcgWaveform wave = waveformMapper.selectOne(new LambdaQueryWrapper<BizEcgWaveform>()
                .eq(BizEcgWaveform::getRecordId, record.getId())
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        String me = UserUtils.getCurrentEmployeeName();
        if (wave == null) {
            wave = new BizEcgWaveform();
            wave.setWaveNo("ECG" + System.currentTimeMillis());
            wave.setRecordId(record.getId());
            wave.setRecordNo(record.getRecordNo());
            wave.setApplyId(record.getApplyId());
            wave.setApplyNo(record.getApplyNo());
            wave.setPatientId(record.getPatientId());
            wave.setPatientNo(record.getPatientNo());
            wave.setPatientName(record.getPatientName());
            wave.setCreateTime(now);
            wave.setCreateBy(me);
        }
        wave.setEcgType(ecgType == null ? 1 : ecgType);
        wave.setWaveData(waveData);
        wave.setDeviceNo(deviceNo);
        wave.setCollectBy(me);
        wave.setCollectTime(now);
        wave.setUpdateTime(now);
        wave.setUpdateBy(me);
        if (wave.getId() == null) {
            waveformMapper.insert(wave);
        } else {
            waveformMapper.updateById(wave);
        }

        // 采集完成 = 该检查已出结果（心电的「拍片」就是采集波形这一步）
        record.setRecordStatus(InsRecordStatusEnum.RESULTED.getCode());
        record.setExecuteTime(now);
        record.setExecuteBy(me);
        inspectionRecordMapper.updateById(record);

        sysAuditLogService.record(UserUtils.getCurrentEmployeeId(), me,
                "心电波形", ecgType != null && ecgType == 2 ? "Holter波形采集" : "波形采集", "biz_ecg_waveform", wave.getId(),
                cut("记录号=" + record.getRecordNo() + " 患者=" + record.getPatientName()
                        + " 类型=" + wave.getEcgType() + " 设备=" + deviceNo, 2000),
                true, null);
        return getDetailByRecordId(record.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EcgDetailVO saveMeasure(EcgMeasureUpsertDTO dto) {
        loadEcgRecord(dto.getRecordId());
        BizEcgMeasure m = measureMapper.selectOne(new LambdaQueryWrapper<BizEcgMeasure>()
                .eq(BizEcgMeasure::getRecordId, dto.getRecordId())
                .last("LIMIT 1"));
        BizEcgWaveform wave = waveformMapper.selectOne(new LambdaQueryWrapper<BizEcgWaveform>()
                .eq(BizEcgWaveform::getRecordId, dto.getRecordId())
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        String me = UserUtils.getCurrentEmployeeName();
        if (m == null) {
            m = new BizEcgMeasure();
            m.setRecordId(dto.getRecordId());
            m.setCreateTime(now);
            m.setCreateBy(me);
        }
        m.setWaveformId(wave == null ? null : wave.getId());
        m.setHr(dto.getHr());
        m.setPrMs(dto.getPrMs());
        m.setQrsMs(dto.getQrsMs());
        m.setQtMs(dto.getQtMs());
        m.setQtcMs(dto.getQtcMs());
        m.setPAxis(dto.getPAxis());
        m.setQrsAxis(dto.getQrsAxis());
        m.setTAxis(dto.getTAxis());
        m.setRhythmText(cut(dto.getRhythmText(), 100));
        m.setMeasureBy(me);
        m.setMeasureTime(now);
        m.setUpdateTime(now);
        m.setUpdateBy(me);
        if (m.getId() == null) {
            measureMapper.insert(m);
        } else {
            measureMapper.updateById(m);
        }
        return getDetailByRecordId(dto.getRecordId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EcgDetailVO saveHolter(EcgHolterUpsertDTO dto) {
        loadEcgRecord(dto.getRecordId());
        BizEcgWaveform wave = waveformMapper.selectOne(new LambdaQueryWrapper<BizEcgWaveform>()
                .eq(BizEcgWaveform::getRecordId, dto.getRecordId())
                .last("LIMIT 1"));
        BizEcgHolter h = holterMapper.selectOne(new LambdaQueryWrapper<BizEcgHolter>()
                .eq(BizEcgHolter::getRecordId, dto.getRecordId())
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        String me = UserUtils.getCurrentEmployeeName();
        if (h == null) {
            h = new BizEcgHolter();
            h.setRecordId(dto.getRecordId());
            h.setCreateTime(now);
            h.setCreateBy(me);
        }
        h.setWaveformId(wave == null ? null : wave.getId());
        h.setWearStartTime(dto.getWearStartTime());
        h.setWearEndTime(dto.getWearEndTime());
        h.setTotalBeats(dto.getTotalBeats());
        h.setAvgHr(dto.getAvgHr());
        h.setMaxHr(dto.getMaxHr());
        h.setMaxHrTime(cut(dto.getMaxHrTime(), 16));
        h.setMinHr(dto.getMinHr());
        h.setMinHrTime(cut(dto.getMinHrTime(), 16));
        h.setAfibFlag(dto.getAfibFlag());
        h.setAfibBeats(dto.getAfibBeats());
        h.setSvcCount(dto.getSvcCount());
        h.setPvcCount(dto.getPvcCount());
        h.setVtCount(dto.getVtCount());
        h.setPauseCount(dto.getPauseCount());
        h.setLongestPauseMs(dto.getLongestPauseMs());
        h.setStEpisodeCount(dto.getStEpisodeCount());
        h.setHourlyHrJson(dto.getHourlyHrJson());
        h.setAnalysisBy(me);
        h.setAnalysisTime(now);
        h.setUpdateTime(now);
        h.setUpdateBy(me);
        if (h.getId() == null) {
            holterMapper.insert(h);
        } else {
            holterMapper.updateById(h);
        }
        return getDetailByRecordId(dto.getRecordId());
    }

    // 报告

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EcgDetailVO saveDraft(EcgReportUpsertDTO dto) {
        BizInspectionRecord record = loadEcgRecord(dto.getRecordId());
        BizReport report = ensureReport(record);
        rejectIfClosed(report);
        applyContent(record, report, dto);
        report.setReportStatus(ReportStatusEnum.DRAFT.getCode());
        saveOrUpdateReport(report);
        return getDetailByRecordId(record.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EcgDetailVO submit(EcgReportUpsertDTO dto) {
        BizInspectionRecord record = loadEcgRecord(dto.getRecordId());
        BizReport report = ensureReport(record);
        rejectIfClosed(report);

        applyContent(record, report, dto);
        // B类共用 DTO：该 DTO 同时服务 saveDraft（草稿允许空白），必填只在提交口生效，注解一刀切会挡掉存草稿
        if (!StringUtils.hasText(report.getReportContent())) {
            throw new BusinessException("心电图所见不能为空：没有所见的报告审不了，也不能发给临床");
        }
        // B类共用 DTO：同上，诊断结论只在提交口必填，存草稿允许空白
        if (!StringUtils.hasText(report.getConclusion())) {
            throw new BusinessException("心电图诊断不能为空");
        }

        // 波形成文闸门：没有波形的报告没有临床证据，审不了也发不出
        BizEcgWaveform wave = waveformMapper.selectOne(new LambdaQueryWrapper<BizEcgWaveform>()
                .eq(BizEcgWaveform::getRecordId, record.getId())
                .last("LIMIT 1"));
        if (wave == null) {
            throw new BusinessException("该检查还没有波形数据：请先完成波形采集再提交报告");
        }
        // Holter 闸门：动态心电必须先有分析结果（心搏统计/事件），否则报告没有内容依据
        if (wave.getEcgType() != null && wave.getEcgType() == 2) {
            BizEcgHolter holter = holterMapper.selectOne(new LambdaQueryWrapper<BizEcgHolter>()
                    .eq(BizEcgHolter::getRecordId, record.getId())
                    .last("LIMIT 1"));
            if (holter == null) {
                throw new BusinessException("Holter 检查必须先录入动态心电分析结果（心搏统计与心律失常事件）再提交报告");
            }
        }
        if (record.getRecordStatus() == null
                || record.getRecordStatus() < InsRecordStatusEnum.RESULTED.getCode()) {
            throw new BusinessException("该检查还没完成波形采集（当前状态："
                    + subDictText.getDicDataLabel("his_inspection_record_status", record.getRecordStatus()) + "）");
        }

        if (record.getReportSignId() != null) {
            throw new BusinessException("本报告已有报告医师签名，不能重复提交；如需修改请由审核医师退回后重写");
        }
        SignatureVO sign = signReport(record, SignScene.REPORT_ISSUE);
        record.setReportSignId(sign.getId());
        record.setReportSignedTime(sign.getSignedTime());

        Long employeeId = UserUtils.getCurrentEmployeeId();
        report.setWriteBy(UserUtils.getCurrentEmployeeName());
        // 与 writeBy 并存：姓名给人看，ID 给程序判「不能自己审自己」（sql/138 同款）
        report.setWriteById(employeeId);
        report.setWriteTime(LocalDateTime.now());
        report.setReportStatus(ReportStatusEnum.PENDING_REVIEW.getCode());
        report.setRejectReason(null);
        saveOrUpdateReport(report);
        inspectionRecordMapper.updateById(record);

        sysAuditLogService.record(employeeId, UserUtils.getCurrentEmployeeName(),
                "心电报告", "提交报告待审核", "biz_report", report.getId(),
                cut("记录号=" + record.getRecordNo() + " 患者=" + record.getPatientName()
                        + " 项目=" + record.getInspectionItemName()
                        + " 阴阳性=" + report.getPositiveFlag()
                        + " 签名=" + sign.getId(), 2000),
                true, null);
        return getDetailByRecordId(record.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EcgDetailVO audit(EcgAuditDTO dto) {
        BizReport report = requireReport(dto.getReportId());
        BizInspectionRecord record = loadEcgRecord(report.getRecordId());
        if (!Objects.equals(ReportStatusEnum.PENDING_REVIEW.getCode(), report.getReportStatus())) {
            throw new BusinessException("只有「待审核」的报告能审核（当前："
                    + subDictText.getDicDataLabel("his_report_status", report.getReportStatus()) + "）");
        }
        Long current = UserUtils.getCurrentEmployeeId();
        if (Objects.equals(report.getWriteById(), current)) {
            throw new BusinessException("不能审核自己书写的报告：请由另一位心电医师审核");
        }

        // 先签名、后改状态：签名层的准入规则要求记录仍处于「已出结果未审核」
        SignatureVO sign = signReport(record, SignScene.REPORT_AUDIT);
        record.setAuditSignId(sign.getId());
        record.setAuditSignedTime(sign.getSignedTime());
        record.setAuditBy(UserUtils.getCurrentEmployeeName());
        record.setAuditTime(sign.getSignedTime());
        record.setRecordStatus(InsRecordStatusEnum.REVIEWED.getCode());
        inspectionRecordMapper.updateById(record);

        report.setReportStatus(ReportStatusEnum.REVIEWED.getCode());
        report.setAuditBy(UserUtils.getCurrentEmployeeName());
        report.setAuditTime(sign.getSignedTime());
        report.setRejectReason(null);
        if (StringUtils.hasText(dto.getReason())) {
            report.setRemark(cut("审核意见：" + dto.getReason(), 500));
        }
        reportMapper.updateById(report);

        sysAuditLogService.record(current, UserUtils.getCurrentEmployeeName(),
                "心电报告", "审核通过", "biz_report", report.getId(),
                cut("记录号=" + record.getRecordNo() + " 患者=" + record.getPatientName()
                        + " 意见=" + (StringUtils.hasText(dto.getReason()) ? dto.getReason() : "无")
                        + " 签名=" + sign.getId(), 2000),
                true, null);
        return getDetailByRecordId(record.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EcgDetailVO reject(EcgAuditDTO dto) {
        BizReport report = requireReport(dto.getReportId());
        BizInspectionRecord record = loadEcgRecord(report.getRecordId());
        if (!Objects.equals(ReportStatusEnum.PENDING_REVIEW.getCode(), report.getReportStatus())) {
            throw new BusinessException("只有「待审核」的报告能退回（当前："
                    + subDictText.getDicDataLabel("his_report_status", report.getReportStatus()) + "）");
        }
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("退回必须写明原因：医师要照着这个原因改报告");
        }

        report.setReportStatus(ReportStatusEnum.DRAFT.getCode());
        report.setRejectReason(cut(dto.getReason(), 500));
        report.setReportVersion(report.getReportVersion() == null ? 2 : report.getReportVersion() + 1);
        reportMapper.updateById(report);

        Long current = UserUtils.getCurrentEmployeeId();
        sysAuditLogService.record(current, UserUtils.getCurrentEmployeeName(),
                "心电报告", "退回重写", "biz_report", report.getId(),
                cut("记录号=" + record.getRecordNo() + " 患者=" + record.getPatientName()
                        + " 原因=" + dto.getReason(), 2000),
                true, null);
        // 退回清签名指针必须显式 set null：MP 的 updateById 会跳过所有 null 字段
        LambdaUpdateWrapper<BizInspectionRecord> uw = new LambdaUpdateWrapper<>();
        uw.eq(BizInspectionRecord::getId, record.getId())
                .set(BizInspectionRecord::getReportSignId, null)
                .set(BizInspectionRecord::getReportSignedTime, null)
                .set(BizInspectionRecord::getAuditSignId, null)
                .set(BizInspectionRecord::getAuditSignedTime, null)
                .set(BizInspectionRecord::getAuditBy, null)
                .set(BizInspectionRecord::getAuditTime, null)
                .set(BizInspectionRecord::getRecordStatus, InsRecordStatusEnum.RESULTED.getCode());
        inspectionRecordMapper.update(null, uw);
        return getDetailByRecordId(record.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EcgDetailVO publish(Long reportId) {
        BizReport report = requireReport(reportId);
        BizInspectionRecord record = loadEcgRecord(report.getRecordId());
        if (!Objects.equals(ReportStatusEnum.REVIEWED.getCode(), report.getReportStatus())) {
            throw new BusinessException("只有「已审核」的报告能发布（当前："
                    + subDictText.getDicDataLabel("his_report_status", report.getReportStatus()) + "）：请先完成审核");
        }
        LocalDateTime now = LocalDateTime.now();
        report.setReportStatus(ReportStatusEnum.PUBLISHED.getCode());
        report.setPublishBy(UserUtils.getCurrentEmployeeName());
        report.setPublishTime(now);
        reportMapper.updateById(report);

        record.setRecordStatus(InsRecordStatusEnum.PUBLISHED.getCode());
        record.setReportTime(now);
        record.setReportBy(UserUtils.getCurrentEmployeeName());
        inspectionRecordMapper.updateById(record);

        if (record.getApplyDoctorId() != null) {
            sysMessageService.sendSystemMessage(record.getApplyDoctorId(), record.getApplyDoctorName(),
                    "心电报告", "患者 " + record.getPatientName() + " 的"
                            + record.getInspectionItemName() + "报告已发布，请查看。",
                    BizTypeEnum.REPORT.getType(), record.getId());
        }
        sysAuditLogService.record(UserUtils.getCurrentEmployeeId(), UserUtils.getCurrentEmployeeName(),
                "心电报告", "发布报告", "biz_report", report.getId(),
                cut("记录号=" + record.getRecordNo() + " 患者=" + record.getPatientName(), 2000),
                true, null);
        return getDetailByRecordId(record.getId());
    }

    // 内部

    /** 取工作台行并强制它是心电项目（item_type=3），这是分岗的另一半闸门 */
    private EcgListVO loadWorkbenchRow(Long recordId) {
        if (recordId == null) {
            throw new BusinessException("缺少检查记录ID");
        }
        EcgListVO row = ecgMapper.selectWorkbenchByRecordId(recordId);
        if (row == null) {
            throw new BusinessException("检查记录不存在、已删除，或不是心电项目（不在心电工作站受理范围）");
        }
        return row;
    }

    private BizInspectionRecord loadEcgRecord(Long recordId) {
        EcgListVO row = loadWorkbenchRow(recordId);
        BizInspectionRecord record = inspectionRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("检查记录不存在或已被删除");
        }
        if (Objects.equals(InsRecordStatusEnum.CANCELLED.getCode(), record.getRecordStatus())) {
            throw new BusinessException("该检查已取消");
        }
        return record;
    }

    /** 可采集状态：2已签到/3检查中可采；4已出结果可重采（覆盖）；5/6 报告已进流程不许动波形 */
    private BizInspectionRecord loadCollectableRecord(Long recordId) {
        BizInspectionRecord record = loadEcgRecord(recordId);
        Integer st = record.getRecordStatus();
        if (Objects.equals(InsRecordStatusEnum.REGISTERED.getCode(), st)) {
            throw new BusinessException("该检查还没签到，请先签到");
        }
        if (st != null && st >= InsRecordStatusEnum.REVIEWED.getCode()) {
            throw new BusinessException("该检查已进入报告审核/发布流程，不能重新采集波形");
        }
        return record;
    }

    private BizReport requireReport(Long reportId) {
        BizReport report = reportId == null ? null : reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在或已被删除");
        }
        return report;
    }

    private void rejectIfClosed(BizReport report) {
        Integer st = report.getReportStatus();
        if (Objects.equals(ReportStatusEnum.PUBLISHED.getCode(), st)
                || Objects.equals(ReportStatusEnum.INVALID.getCode(), st)) {
            throw new BusinessException("报告已"
                    + subDictText.getDicDataLabel("his_report_status", st) + "，不能再修改");
        }
    }

    private BizReport ensureReport(BizInspectionRecord record) {
        BizReport exists = reportMapper.selectOne(new LambdaQueryWrapper<BizReport>()
                .eq(BizReport::getRecordId, record.getId())
                .eq(BizReport::getReportType, ReportTypeEnum.INSPECTION.getCode())
                .orderByDesc(BizReport::getId)
                .last("LIMIT 1"));
        if (exists != null) {
            return exists;
        }
        BizReport report = new BizReport();
        report.setReportNo("RPT" + System.currentTimeMillis());
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
        return report;
    }

    private void saveOrUpdateReport(BizReport report) {
        if (report.getId() == null) {
            reportMapper.insert(report);
        } else {
            reportMapper.updateById(report);
        }
    }

    private void applyContent(BizInspectionRecord record, BizReport report, EcgReportUpsertDTO dto) {
        report.setTemplateId(dto.getTemplateId());
        report.setReportContent(dto.getReportContent());
        report.setConclusion(dto.getConclusion());
        report.setSuggestions(cut(dto.getSuggestions(), 1000));
        report.setPositiveFlag(dto.getPositiveFlag() == null ? 0 : dto.getPositiveFlag());
        report.setIsCritical(dto.getIsCritical() == null ? 0 : dto.getIsCritical());
    }

    private SignatureVO signReport(BizInspectionRecord record, SignScene scene) {
        Long signerId = UserUtils.getCurrentEmployeeId();
        if (signerId == null) {
            throw new BusinessException("签名失败：取不到当前登录用户，无法确定签名人");
        }
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(SignBizType.INSPECTION_REPORT.getCode());
        cmd.setBizId(record.getId());
        cmd.setSignScene(scene.getCode());
        cmd.setSignerId(signerId);
        cmd.setSignerName(UserUtils.getCurrentEmployeeName());
        cmd.setSignerDeptId(record.getInspectionDeptId());
        cmd.setSignerDeptName(record.getInspectionDeptName());
        try {
            return signatureService.sign(cmd);
        } catch (BusinessException e) {
            throw new BusinessException("心电报告 " + record.getRecordNo() + " " + scene.getText()
                    + "失败：" + e.getMessage());
        }
    }

    private void fillText(List<EcgListVO> list) {
        if (list == null) {
            return;
        }
        for (EcgListVO v : list) {
            v.setRecordStatusText(subDictText.getDicDataLabel("his_inspection_record_status", v.getRecordStatus()));
            v.setReportStatusText(v.getReportStatus() == null ? "未写报告"
                    : subDictText.getDicDataLabel("his_report_status", v.getReportStatus()));
            v.setPositiveFlagText(v.getPositiveFlag() == null ? null
                    : subDictText.getDicDataLabel("his_positive_flag", v.getPositiveFlag()));
            v.setEcgTypeText(v.getEcgType() == null ? null
                    : subDictText.getDicDataLabel("his_ecg_type", v.getEcgType()));
        }
    }

    private EcgDetailVO toDetail(EcgListVO row, BizEcgWaveform wave, BizEcgMeasure measure,
                                 BizEcgHolter holter, BizReport report) {
        EcgDetailVO vo = new EcgDetailVO();
        vo.setRecordId(row.getRecordId());
        vo.setRecordNo(row.getRecordNo());
        vo.setApplyId(row.getApplyId());
        vo.setApplyNo(row.getApplyNo());
        vo.setPatientId(row.getPatientId());
        vo.setPatientNo(row.getPatientNo());
        vo.setPatientName(row.getPatientName());
        vo.setGender(row.getGender());
        vo.setGenderText(row.getGender() == null ? null : subDictText.getDicDataLabel("sys_gender", row.getGender()));
        vo.setAge(row.getAge());
        vo.setVisitDate(row.getVisitDate());
        vo.setItemCode(row.getItemCode());
        vo.setItemName(row.getItemName());
        vo.setBodyPart(row.getBodyPart());
        vo.setApplyDeptName(row.getApplyDeptName());
        vo.setApplyDoctorName(row.getApplyDoctorName());
        vo.setClinicalDiagnosis(row.getClinicalDiagnosis());
        vo.setRecordStatus(row.getRecordStatus());
        vo.setRecordStatusText(subDictText.getDicDataLabel("his_inspection_record_status", row.getRecordStatus()));
        BizInspectionRecord rec = inspectionRecordMapper.selectById(row.getRecordId());
        if (rec != null) {
            vo.setCheckInTime(rec.getCheckInTime());
            vo.setExecuteBy(rec.getExecuteBy());
            vo.setExecuteTime(rec.getExecuteTime());
        }

        if (wave != null) {
            vo.setWaveId(wave.getId());
            vo.setWaveNo(wave.getWaveNo());
            vo.setEcgType(wave.getEcgType());
            vo.setEcgTypeText(subDictText.getDicDataLabel("his_ecg_type", wave.getEcgType()));
            vo.setDeviceNo(wave.getDeviceNo());
            vo.setCollectBy(wave.getCollectBy());
            vo.setCollectTime(wave.getCollectTime());
            vo.setWaveData(wave.getWaveData());
        }
        if (measure != null) {
            vo.setMeasureId(measure.getId());
            vo.setHr(measure.getHr());
            vo.setPrMs(measure.getPrMs());
            vo.setQrsMs(measure.getQrsMs());
            vo.setQtMs(measure.getQtMs());
            vo.setQtcMs(measure.getQtcMs());
            vo.setPAxis(measure.getPAxis());
            vo.setQrsAxis(measure.getQrsAxis());
            vo.setTAxis(measure.getTAxis());
            vo.setRhythmText(measure.getRhythmText());
            vo.setMeasureBy(measure.getMeasureBy());
            vo.setMeasureTime(measure.getMeasureTime());
        }
        if (holter != null) {
            vo.setHolterId(holter.getId());
            vo.setWearStartTime(holter.getWearStartTime());
            vo.setWearEndTime(holter.getWearEndTime());
            vo.setTotalBeats(holter.getTotalBeats());
            vo.setAvgHr(holter.getAvgHr());
            vo.setMaxHr(holter.getMaxHr());
            vo.setMaxHrTime(holter.getMaxHrTime());
            vo.setMinHr(holter.getMinHr());
            vo.setMinHrTime(holter.getMinHrTime());
            vo.setAfibFlag(holter.getAfibFlag());
            vo.setAfibBeats(holter.getAfibBeats());
            vo.setSvcCount(holter.getSvcCount());
            vo.setPvcCount(holter.getPvcCount());
            vo.setVtCount(holter.getVtCount());
            vo.setPauseCount(holter.getPauseCount());
            vo.setLongestPauseMs(holter.getLongestPauseMs());
            vo.setStEpisodeCount(holter.getStEpisodeCount());
            vo.setHourlyHrJson(holter.getHourlyHrJson());
            vo.setAnalysisBy(holter.getAnalysisBy());
            vo.setAnalysisTime(holter.getAnalysisTime());
        }
        if (report != null) {
            vo.setReportId(report.getId());
            vo.setReportNo(report.getReportNo());
            vo.setReportStatus(report.getReportStatus());
            vo.setReportStatusText(subDictText.getDicDataLabel("his_report_status", report.getReportStatus()));
            vo.setTemplateId(report.getTemplateId());
            vo.setReportContent(report.getReportContent());
            vo.setConclusion(report.getConclusion());
            vo.setSuggestions(report.getSuggestions());
            vo.setPositiveFlag(report.getPositiveFlag());
            vo.setPositiveFlagText(report.getPositiveFlag() == null ? null
                    : subDictText.getDicDataLabel("his_positive_flag", report.getPositiveFlag()));
            vo.setIsCritical(report.getIsCritical());
            vo.setWriteBy(report.getWriteBy());
            vo.setWriteTime(report.getWriteTime());
            vo.setAuditBy(report.getAuditBy());
            vo.setAuditTime(report.getAuditTime());
            vo.setPublishBy(report.getPublishBy());
            vo.setPublishTime(report.getPublishTime());
            vo.setReportVersion(report.getReportVersion());
            vo.setRejectReason(report.getRejectReason());
        }
        // 双签留痕取自检查记录（签名指针在记录上，不在报告上）
        if (rec != null) {
            vo.setReportSignId(rec.getReportSignId());
            vo.setReportSignedTime(rec.getReportSignedTime());
            vo.setAuditSignId(rec.getAuditSignId());
            vo.setAuditSignedTime(rec.getAuditSignedTime());
        }
        return vo;
    }

    @Override
    public List<EcgTemplateVO> templateSelectList(Integer ecgType) {
        LambdaQueryWrapper<BizEcgTemplate> w = new LambdaQueryWrapper<>();
        w.eq(BizEcgTemplate::getStatus, 1);
        w.orderByAsc(BizEcgTemplate::getSortOrder).orderByAsc(BizEcgTemplate::getId);
        List<EcgTemplateVO> all = templateMapper.selectList(w).stream()
                .map(this::toTemplateVO).collect(Collectors.toList());
        // 与放射同款：ecgType 过滤在内存做，通用模板（ecgType 为空）任何类型都能用
        if (ecgType == null) {
            return all;
        }
        return all.stream()
                .filter(v -> v.getEcgType() == null || Objects.equals(v.getEcgType(), ecgType))
                .collect(Collectors.toList());
    }

    @Override
    public List<EcgTemplateVO> templateList() {
        return templateMapper.selectList(new LambdaQueryWrapper<BizEcgTemplate>()
                        .orderByAsc(BizEcgTemplate::getSortOrder)
                        .orderByAsc(BizEcgTemplate::getId))
                .stream().map(this::toTemplateVO).collect(Collectors.toList());
    }

    @Override
    public EcgTemplateVO templateUpsert(EcgTemplateUpsertDTO dto) {
        BizEcgTemplate entity = new BizEcgTemplate();
        BeanUtils.copyProperties(dto, entity, "id");
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (dto.getId() == null) {
            long dup = templateMapper.selectCount(new LambdaQueryWrapper<BizEcgTemplate>()
                    .eq(BizEcgTemplate::getTemplateCode, entity.getTemplateCode()));
            if (dup > 0) {
                throw new BusinessException("模板编码 " + entity.getTemplateCode() + " 已存在");
            }
            templateMapper.insert(entity);
        } else {
            entity.setId(dto.getId());
            templateMapper.updateById(entity);
        }
        return toTemplateVO(templateMapper.selectById(entity.getId()));
    }

    @Override
    public boolean templateDeleteById(Long id) {
        if (id == null) {
            throw new BusinessException("缺少模板ID");
        }
        return templateMapper.purgeById(id) > 0;
    }

    private EcgTemplateVO toTemplateVO(BizEcgTemplate e) {
        EcgTemplateVO vo = new EcgTemplateVO();
        BeanUtils.copyProperties(e, vo);
        vo.setEcgTypeText(e.getEcgType() == null ? null
                : subDictText.getDicDataLabel("his_ecg_type", e.getEcgType()));
        if (vo.getTemplateName() == null || vo.getTemplateName().isEmpty()) {
            vo.setTemplateName(e.getTemplateCode());
        }
        return vo;
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String cut(String s, int max) {
        if (s == null) {
            return null;
        }
        String v = s.trim();
        return v.length() <= max ? v : v.substring(0, max);
    }
}
