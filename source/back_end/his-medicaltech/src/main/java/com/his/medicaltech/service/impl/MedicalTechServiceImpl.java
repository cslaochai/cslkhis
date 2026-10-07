package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.dto.SignCommandDTO;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.exception.BusinessException;
import com.his.common.service.EmrSignatureService;
import com.his.common.vo.SignatureVO;
import com.his.emr.entity.BizInspectionApply;
import com.his.emr.entity.BizLaboratoryApply;
import com.his.emr.mapper.BizInspectionApplyMapper;
import com.his.emr.mapper.BizLaboratoryApplyMapper;
import com.his.medicaltech.dto.LabResultItemSaveDTO;
import com.his.medicaltech.dto.LabResultSaveDTO;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.entity.BizLaboratoryRecord;
import com.his.medicaltech.entity.BizReport;
import com.his.medicaltech.enums.InsRecordStatusEnum;
import com.his.medicaltech.enums.LabRecordStatusEnum;
import com.his.medicaltech.enums.ReportStatusEnum;
import com.his.medicaltech.enums.ReportTypeEnum;
import com.his.medicaltech.mapper.BizInspectionRecordMapper;
import com.his.medicaltech.mapper.BizLabResultMapper;
import com.his.medicaltech.mapper.BizLaboratoryRecordMapper;
import com.his.medicaltech.mapper.BizReportMapper;
import com.his.medicaltech.service.CriticalValueService;
import com.his.medicaltech.service.MedicalTechService;
import com.his.medicaltech.support.LabAbnormalJudge;
import com.his.medicaltech.support.LabReferenceRange;
import com.his.medicaltech.support.LabReferenceRangeResolver;
import com.his.medicaltech.vo.*;
import com.his.system.entity.CurrentUser;
import com.his.system.enums.BizTypeEnum;
import com.his.system.service.SysMessageService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 医技服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalTechServiceImpl extends ServiceImpl<BizInspectionRecordMapper, BizInspectionRecord> implements MedicalTechService {

    /**
     * 执行记录号自增序号（迁移自 his-charge 的 EXECUTION_SEQ）
     */
    private static final java.util.concurrent.atomic.AtomicInteger EXECUTION_SEQ =
            new java.util.concurrent.atomic.AtomicInteger(0);

    private final BizInspectionRecordMapper inspectionRecordMapper;
    private final BizLaboratoryRecordMapper laboratoryRecordMapper;
    private final BizLabResultMapper labResultMapper;
    private final BizReportMapper reportMapper;
    private final BizInspectionApplyMapper inspectionApplyMapper;
    private final BizLaboratoryApplyMapper laboratoryApplyMapper;
    private final SysMessageService sysMessageService;
    private final LabReferenceRangeResolver referenceRangeResolver;
    private final CriticalValueService criticalValueService;
    private final EmrSignatureService signatureService;
    /**
     * 放射分岗（sql/138）：只有它知道某个检查项目是不是放射（检查项目字典的项目类型）
     */
    private final com.his.medicaltech.mapper.RadioReportMapper radioReportMapper;
    /**
     * 简化 PACS（sql/137）：详情出参顺带带出影像帧，工作站不必二次请求
     */
    private final com.his.medicaltech.service.ExamImageService examImageService;

    private static String truncateForNote(String text) {
        if (text == null) {
            return null;
        }
        String value = text.trim();
        return value.length() <= 200 ? value : value.substring(0, 200);
    }

    @Override
    public PageResult<BizInspectionRecord> selectInspectionRecordPage(Long patientId, Long inspectionDeptId, int pageNum, int pageSize) {
        LambdaQueryWrapper<BizInspectionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizInspectionRecord::getPatientId, patientId)
                .eq(inspectionDeptId != null, BizInspectionRecord::getInspectionDeptId, inspectionDeptId)
                .orderByDesc(BizInspectionRecord::getCreateTime);
        Page<BizInspectionRecord> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public List<BizInspectionRecord> selectInspectionRecordList(Long patientId, Long inspectionDeptId) {
        LambdaQueryWrapper<BizInspectionRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizInspectionRecord::getPatientId, patientId)
                .eq(inspectionDeptId != null, BizInspectionRecord::getInspectionDeptId, inspectionDeptId)
                .orderByDesc(BizInspectionRecord::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public PageResult<BizInspectionRecordVO> selectInspectionRecordPageVO(Long patientId, Long inspectionDeptId,
                                                                          int pageNum, int pageSize) {
        PageResult<BizInspectionRecord> page = selectInspectionRecordPage(patientId, inspectionDeptId, pageNum, pageSize);
        return PageResult.of(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getPages(),
                toInspectionListVo(page.getRecords()));
    }

    @Override
    public List<BizInspectionRecordVO> selectInspectionRecordListVO(Long patientId, Long inspectionDeptId) {
        return toInspectionListVo(selectInspectionRecordList(patientId, inspectionDeptId));
    }

    /**
     * 列表口径的裸拷贝：不带 itemType 等详情专用扩展字段，与旧 Controller 行为一致
     */
    private List<BizInspectionRecordVO> toInspectionListVo(List<BizInspectionRecord> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream().map(entity -> {
            BizInspectionRecordVO vo = new BizInspectionRecordVO();
            BeanUtils.copyProperties(entity, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public InspectionDetailVO getInspectionDetail(Long recordId) {
        BizInspectionRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("检查记录不存在");
        }

        // 查询关联报告
        LambdaQueryWrapper<BizReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.eq(BizReport::getRecordId, recordId);
        BizReport report = reportMapper.selectOne(reportWrapper);

        InspectionDetailVO vo = new InspectionDetailVO();
        vo.setRecord(toInspectionRecordVO(record));
        vo.setReport(toReportVO(report));
        vo.setImages(examImageService.listByApply(ReportTypeEnum.INSPECTION.getCode(), record.getApplyId()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkIn(Long recordId) {
        BizInspectionRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("检查记录不存在");
        }
        record.setCheckInTime(LocalDateTime.now());
        record.setRecordStatus(2);
        this.updateById(record);
        // 批次E（E4 口径收口）：这里原本回写申请单 apply_status=2 并注释「已签到」，
        // 而收费侧同一个 2 的语义是「已缴费」—— 同一个码值被两个模块当成两件事写。
        // 现在执行进度只由本表的 record_status 表达，医技侧不再回写申请单。
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startInspection(Long recordId) {
        BizInspectionRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("检查记录不存在");
        }
        record.setRecordStatus(3); // 检查中
        this.updateById(record);
        // 批次E：不再回写申请单（执行进度只在本表 record_status 上）
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeInspection(Long recordId, String executeBy, String resultDescription,
                                     String resultConclusion, String suggestions) {
        BizInspectionRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("检查记录不存在");
        }
        // sql/138 分岗闸门：放射项目的报告不许从这里产生。
        // 光在前端把「录入」按钮藏掉不算分岗 —— 接口还在那儿，技师换个调用方式照样能写诊断。
        Integer itemType = radioReportMapper.selectItemTypeByCode(record.getInspectionItemCode());
        if (itemType != null && itemType == 1) {
            throw new BusinessException("「" + record.getInspectionItemName()
                    + "」是放射检查，报告由放射诊断医师在「放射诊断工作站」书写；"
                    + "技师在这里只做「拍片完成」");
        }
        // sql/173 分岗闸门：心电项目的报告也不许从这里产生 —— 心电没有「录入一段文字」就能完事的报告，
        // 它必须先有波形（以及 Holter 分析），全流程在「心电工作站」闭环。
        if (itemType != null && itemType == 3) {
            throw new BusinessException("「" + record.getInspectionItemName()
                    + "」是心电检查，请在「心电工作站」完成签到、波形采集与报告书写");
        }
        record.setExecuteTime(LocalDateTime.now());
        record.setExecuteBy(executeBy);
        record.setResultDescription(resultDescription);
        record.setResultConclusion(resultConclusion);
        record.setRecordStatus(InsRecordStatusEnum.RESULTED.getCode()); // 已出结果
        this.updateById(record);

        // 创建检查报告记录
        BizReport report = new BizReport();
        report.setReportNo("RPT" + System.currentTimeMillis());
        report.setReportType(ReportTypeEnum.INSPECTION.getCode()); // 检查报告
        report.setRecordId(recordId);
        report.setRecordNo(record.getRecordNo());
        report.setPatientId(record.getPatientId());
        report.setPatientNo(record.getPatientNo());
        report.setPatientName(record.getPatientName());
        report.setGender(record.getGender());
        report.setAge(record.getAge());
        report.setVisitDate(record.getVisitDate());
        report.setItemName(record.getInspectionItemName());
        report.setExamDeptName(record.getInspectionDeptName());
        report.setApplyDoctorId(record.getApplyDoctorId());
        report.setApplyDeptName(record.getApplyDeptName());
        report.setApplyDoctorName(record.getApplyDoctorName());
        report.setClinicalDiagnosis(record.getClinicalDiagnosis());
        report.setReportContent(resultDescription);
        report.setConclusion(resultConclusion);
        report.setSuggestions(suggestions);
        report.setReportStatus(ReportStatusEnum.PENDING_REVIEW.getCode());
        reportMapper.insert(report);

        // 批次E（E4）：不再回写申请单 apply_status。原来这里把申请单写成「已出报告」，
        // 而申请单口径只有 1已提交/2已缴费/6已取消 —— 「已出报告」是执行态，
        // 它由本表 record_status=4 与报告单的报告状态共同表达。

        // 报告医师签名：必须放在"结果与报告都落库之后" —— 被签内容包括检查描述与检查结论，
        // 而签名层是重新从库里读的，先签等于签出一份空报告。
        SignatureVO sign = signInspectionReport(record, false);
        record.setReportSignId(sign.getId());
        record.setReportSignedTime(sign.getSignedTime());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean finishShoot(Long recordId) {
        BizInspectionRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("检查记录不存在");
        }
        Integer itemType = radioReportMapper.selectItemTypeByCode(record.getInspectionItemCode());
        if (itemType == null) {
            throw new BusinessException("检查项目 " + record.getInspectionItemCode()
                    + " 不在检查项目字典里，无法判定是不是放射检查");
        }
        if (itemType != 1) {
            throw new BusinessException("「" + record.getInspectionItemName()
                    + "」不是放射检查（项目类型 " + itemType + "），请直接用「录入」书写结果");
        }
        if (record.getRecordStatus() != null
                && record.getRecordStatus() >= InsRecordStatusEnum.RESULTED.getCode()) {
            throw new BusinessException("该检查已是「"
                    + InsRecordStatusEnum.getByCode(record.getRecordStatus()).getDesc()
                    + "」，不要重复提交拍片完成");
        }
        record.setExecuteTime(LocalDateTime.now());
        record.setExecuteBy(UserUtils.getCurrentUser().getRealName());
        record.setRecordStatus(InsRecordStatusEnum.RESULTED.getCode());
        this.updateById(record);
        // 不建报告、不签名：拍片是技师的活，诊断结论是医师的活（sql/138）
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditInspection(Long recordId, String auditBy) {
        BizInspectionRecord record = this.getById(recordId);
        if (record == null) {
            throw new BusinessException("检查记录不存在");
        }
        // 先签名、后改状态：签名层的准入规则要求记录处于「已出结果」，先把状态写成
        // 「已审核」会让签名层把自己拒掉（"已审核的不能重复签"）。
        SignatureVO sign = signInspectionReport(record, true);
        record.setAuditSignId(sign.getId());
        record.setAuditSignedTime(sign.getSignedTime());
        record.setAuditBy(auditBy);
        // 审核时间取签名时刻，保证「审核时间」与「签名时间」是同一瞬间（否则两处差几百毫秒）
        record.setAuditTime(sign.getSignedTime());
        record.setRecordStatus(InsRecordStatusEnum.REVIEWED.getCode()); // 已审核
        this.updateById(record);

        // 更新关联的报告状态为已审核
        LambdaQueryWrapper<BizReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.eq(BizReport::getRecordId, recordId);
        BizReport report = reportMapper.selectOne(reportWrapper);
        if (report != null) {
            report.setReportStatus(ReportStatusEnum.PUBLISHED.getCode()); // 已审核
            report.setAuditBy(auditBy);
            report.setAuditTime(LocalDateTime.now());
            reportMapper.updateById(report);
        }

        // 发送通知给开单医生
        sendNotification(record.getApplyDoctorId(), record.getApplyDoctorName(), BizTypeEnum.INSPECTION.getType(), "检查报告",
                "患者 " + record.getPatientName() + " 的" + record.getInspectionItemName() + "检查报告已出，请查看。",
                recordId);
        return true;
    }

    @Override
    public PageResult<BizLaboratoryRecord> selectLaboratoryRecordPage(Long patientId, Long laboratoryDeptId, int pageNum, int pageSize) {
        LambdaQueryWrapper<BizLaboratoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizLaboratoryRecord::getPatientId, patientId)
                .eq(laboratoryDeptId != null, BizLaboratoryRecord::getLaboratoryDeptId, laboratoryDeptId)
                .orderByDesc(BizLaboratoryRecord::getCreateTime);

        Page<BizLaboratoryRecord> page = laboratoryRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public List<BizLaboratoryRecord> selectLaboratoryRecordList(Long patientId, Long laboratoryDeptId) {
        LambdaQueryWrapper<BizLaboratoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizLaboratoryRecord::getPatientId, patientId)
                .eq(laboratoryDeptId != null, BizLaboratoryRecord::getLaboratoryDeptId, laboratoryDeptId)
                .orderByDesc(BizLaboratoryRecord::getCreateTime);
        return laboratoryRecordMapper.selectList(wrapper);
    }

    @Override
    public PageResult<BizLaboratoryRecordVO> selectLaboratoryRecordPageVO(Long patientId, Long laboratoryDeptId,
                                                                          int pageNum, int pageSize) {
        PageResult<BizLaboratoryRecord> page = selectLaboratoryRecordPage(patientId, laboratoryDeptId, pageNum, pageSize);
        return PageResult.of(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getPages(),
                toLaboratoryListVo(page.getRecords()));
    }

    @Override
    public List<BizLaboratoryRecordVO> selectLaboratoryRecordListVO(Long patientId, Long laboratoryDeptId) {
        return toLaboratoryListVo(selectLaboratoryRecordList(patientId, laboratoryDeptId));
    }

    private List<BizLaboratoryRecordVO> toLaboratoryListVo(List<BizLaboratoryRecord> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list.stream().map(this::toLaboratoryRecordVO).collect(Collectors.toList());
    }

    @Override
    public LaboratoryDetailVO getLaboratoryDetail(Long recordId) {
        BizLaboratoryRecord record = laboratoryRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("检验记录不存在");
        }

        // 查询检验结果
        LambdaQueryWrapper<BizLabResult> resultWrapper = new LambdaQueryWrapper<>();
        resultWrapper.eq(BizLabResult::getRecordId, recordId)
                .orderByAsc(BizLabResult::getSortOrder);
        List<BizLabResult> results = labResultMapper.selectList(resultWrapper);

        LaboratoryDetailVO result = new LaboratoryDetailVO();
        result.setRecord(toLaboratoryRecordVO(record));
        result.setResults(results.stream().map(this::toLabResultVO).collect(Collectors.toList()));

        // 查询关联报告
        LambdaQueryWrapper<BizReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.eq(BizReport::getRecordId, recordId);
        BizReport report = reportMapper.selectOne(reportWrapper);
        result.setReport(toReportVO(report));
        result.setImages(examImageService.listByApply(ReportTypeEnum.LAB_TEST.getCode(), record.getApplyId()));
        return result;
    }

    /**
     * 检查记录实体转 VO
     */
    private BizInspectionRecordVO toInspectionRecordVO(BizInspectionRecord record) {
        if (record == null) {
            return null;
        }
        BizInspectionRecordVO vo = new BizInspectionRecordVO();
        BeanUtils.copyProperties(record, vo);
        // sql/138：顺带告诉前端这条检查是不是放射（决定技师看到的是「拍片完成」还是「录入」）。
        // 项目字典里查不到的老项目返回 null，前端按「非放射」处理 —— 宁可让它走老流程，
        // 也不要因为字典缺一条就把人卡在"不知道该点哪个"上。
        vo.setItemType(radioReportMapper.selectItemTypeByCode(record.getInspectionItemCode()));
        return vo;
    }

    /**
     * 检验记录实体转 VO
     */
    private BizLaboratoryRecordVO toLaboratoryRecordVO(BizLaboratoryRecord record) {
        if (record == null) {
            return null;
        }
        BizLaboratoryRecordVO vo = new BizLaboratoryRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    /**
     * 检验结果实体转 VO
     * <p>
     * {@code abnormalFlagText} 必须在后端算：未判定时 {@code abnormalFlag} 也是 0，
     * 与「正常」同值，前端拿 flag 写三目判断就会把「不知道」显示成「正常」。
     * 语义只在这里定义一次，所有页面（检验工作站、医生工作站）共用。
     */
    private BizLabResultVO toLabResultVO(BizLabResult result) {
        if (result == null) {
            return null;
        }
        BizLabResultVO vo = new BizLabResultVO();
        BeanUtils.copyProperties(result, vo);
        vo.setAbnormalFlagText(LabAbnormalJudge.getText(result.getAbnormalFlag(), result.getJudgeNote()));
        return vo;
    }

    /**
     * 报告实体转 VO
     */
    private BizReportVO toReportVO(BizReport report) {
        if (report == null) {
            return null;
        }
        BizReportVO vo = new BizReportVO();
        BeanUtils.copyProperties(report, vo);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveResult(LabResultSaveDTO labResultSaveDTO) {
        // 查询检验记录，获取检验项目信息
        BizLaboratoryRecord record = laboratoryRecordMapper.selectById(labResultSaveDTO.getRecordId());
        if (record == null) {
            throw new BusinessException("检验记录不存在");
        }

        List<LabResultItemSaveDTO> itemList = labResultSaveDTO.getResults();
        List<BizLabResult> results = new ArrayList<>();
        if (itemList != null) {
            for (LabResultItemSaveDTO item : itemList) {
                BizLabResult result = new BizLabResult();
                // 优先使用前端传入的值，如果没有则使用检验记录中的值
                result.setLaboratoryItemId(item.getLaboratoryItemId() != null ? item.getLaboratoryItemId() : record.getLaboratoryItemId());
                result.setLaboratoryItemCode(item.getLaboratoryItemCode() != null ? item.getLaboratoryItemCode() : record.getLaboratoryItemCode());
                result.setLaboratoryItemName(item.getLaboratoryItemName() != null ? item.getLaboratoryItemName() : record.getLaboratoryItemName());
                result.setResultValue(item.getResultValue());
                result.setResultUnit(item.getResultUnit());
                result.setReferenceRange(item.getReferenceRange());
                // 异常标志不再信前端传值：真正的判定统一在 inputLabResult 里做，
                // 那里能拿到 record.gender（参考区间有性别分支时必需）。
                // 这里只保留前端传入值作为「无法判定时的兜底」。
                result.setAbnormalFlag(item.getAbnormalFlag());
                result.setAbnormalDesc(item.getAbnormalDesc());
                results.add(result);
            }
        }
        boolean success = this.inputLabResult(record.getId(),
                labResultSaveDTO.getExecuteBy(), results, labResultSaveDTO.getDiagnosis(), labResultSaveDTO.getSuggestions());
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveSpecimen(Long recordId, String receiveBy) {
        BizLaboratoryRecord record = laboratoryRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("检验记录不存在");
        }
        record.setReceiveTime(LocalDateTime.now());
        record.setReceiveBy(receiveBy);
        record.setRecordStatus(3);
        laboratoryRecordMapper.updateById(record);
        // 批次E：不再回写申请单（医技侧只维护自己的执行记录状态）
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean inputLabResult(Long recordId, String executeBy, List<BizLabResult> results,
                                  String diagnosis, String suggestions) {
        BizLaboratoryRecord record = laboratoryRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("检验记录不存在");
        }

        record.setExecuteTime(LocalDateTime.now());
        record.setExecuteBy(executeBy);
        record.setDiagnosis(diagnosis);
        record.setSuggestions(suggestions);
        record.setRecordStatus(LabRecordStatusEnum.RESULTED.getCode());
        laboratoryRecordMapper.updateById(record);

        // 保存检验结果
        for (BizLabResult result : results) {
            result.setRecordId(recordId);
            result.setRecordNo(record.getRecordNo());
            if (result.getLaboratoryItemCode() == null) {
                result.setLaboratoryItemCode(record.getLaboratoryItemCode());
            }
            if (result.getLaboratoryItemName() == null) {
                result.setLaboratoryItemName(record.getLaboratoryItemName());
            }
            judgeAbnormal(result, record);
            labResultMapper.insert(result);
        }

        // 危急值识别与上报。放在结果落库之后、状态流转之前 ——
        // 它是旁路动作，任何失败都不能影响「结果已录入」这个事实。
        criticalValueService.detectAndReport(record, results);

        // 批次E（E4）：不再回写申请单 apply_status。「已出结果」由本表 record_status=5 表达，
        // 申请单口径只有 1已提交/2已缴费/6已取消。原来这里还顺手抛了「未能找到相应的检验记录」——
        // 那是把一个纯执行动作和申请单的存在性绑死，申请单被清理后连结果都录不进去。

        // 报告医师签名：必须放在检验结果明细落库之后 —— 被签内容包含检验结果明细，
        // 只签一个 record 头等于签了个空信封（把"白细胞 1.0"改成"10.0"照样验得过去）。
        SignatureVO sign = signLabReport(record, false);
        record.setReportSignId(sign.getId());
        record.setReportSignedTime(sign.getSignedTime());
        return true;
    }

    /**
     * 后台自动判定异常标志。
     * <p>
     * <b>为什么必须由后台判定：</b> 此前 {@code abnormal_flag} 完全依赖前端传入、缺省 0，
     * 后台从不判定 —— 实测库里「白细胞计数 = 1（参考 4-10）」这种结果 flag 仍是 0，
     * 也就是说后台从来没有能力发现异常结果，而且不会报任何错。
     * <p>
     * 三点刻意的保守设计：
     * <ol>
     *   <li><b>只在判定得出结果时覆盖</b>。判定不出来（区间无法解析 / 结果非数值）时
     *       保留前端传入值，并把原因写进 {@code judgeNote} ——
     *       把「不知道」静默写成「正常」是这类逻辑最危险的失败方式。</li>
     *   <li>参考区间优先用报告上的区间，为空才查主数据；<b>不静默改写医院报告上的口径</b>。</li>
     *   <li>带性别分支的区间（如男120-160/女110-150）在性别缺失时不判定，
     *       取男取女都可能漏诊。</li>
     * </ol>
     */
    private void judgeAbnormal(BizLabResult result, BizLaboratoryRecord record) {
        try {
            LabReferenceRange range = referenceRangeResolver.resolve(
                    result.getReferenceRange(),
                    result.getLaboratoryItemCode(),
                    result.getLaboratoryItemName(),
                    record.getGender());
            LabAbnormalJudge.Verdict verdict = LabAbnormalJudge.judge(result.getResultValue(), range);

            if (verdict.judged()) {
                result.setAbnormalFlag(verdict.flag());
                // 判定为正常时把描述清空：留着旧的「偏高」会与 flag 自相矛盾
                result.setAbnormalDesc(verdict.flag() == LabAbnormalJudge.NORMAL
                        ? null : verdict.description());
                result.setJudgeNote(truncateForNote("自动判定，判据 " + range.describe()));
            } else {
                // 判定不出来时保留前端传入值；前端也没传就留 null（「未知」），
                // 而不是填 0（「正常」）。0 是一个结论，null 才是「没有结论」——
                // 把「不知道」写成「正常」正是这类逻辑最危险的失败方式。
                result.setJudgeNote(truncateForNote(verdict.note()));
            }
        } catch (Exception ex) {
            // 判定是个增强动作，炸了也不能让「录入检验结果」失败
            log.error("[检验判定] 项目 {} 自动判定异常失败，保留原值",
                    result.getLaboratoryItemName(), ex);
            result.setJudgeNote("自动判定异常，保留原值");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditLaboratory(Long recordId, String auditBy) {
        BizLaboratoryRecord record = laboratoryRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("检验记录不存在");
        }
        // 先签名、后改状态（同 auditInspection 的理由：签名层要求"已出结果未审核"）
        SignatureVO sign = signLabReport(record, true);
        record.setAuditSignId(sign.getId());
        record.setAuditSignedTime(sign.getSignedTime());
        record.setAuditBy(auditBy);
        record.setAuditTime(sign.getSignedTime());
        record.setRecordStatus(LabRecordStatusEnum.RELEASED.getCode());
        laboratoryRecordMapper.updateById(record);

        // 发送通知给开单医生
        sendNotification(record.getApplyDoctorId(), record.getApplyDoctorName(), BizTypeEnum.REPORT.getType(), "检验报告",
                "患者 " + record.getPatientName() + " 的" + record.getLaboratoryItemName() + "检验结果已出，请查看。",
                recordId);
        return true;
    }

    /**
     * 发送站内通知给医生
     */
    private void sendNotification(Long receiverId, String receiverName, String bizType, String title, String content, Long bizId) {
        if (receiverId == null) {
            return;
        }
        sysMessageService.sendSystemMessage(receiverId, receiverName, title, content, bizType, bizId);
    }

    /**
     * 检查报告签名（{@code audit=false} 报告医师 / {@code audit=true} 审核医师）
     */
    private SignatureVO signInspectionReport(BizInspectionRecord r, boolean audit) {
        return signReport(SignBizTypeEnum.INSPECTION_REPORT.getCode(), r.getId(), r.getRecordNo(),
                audit ? SignSceneEnum.REPORT_AUDIT : SignSceneEnum.REPORT_ISSUE,
                r.getInspectionDeptId(), r.getInspectionDeptName(), "检查报告");
    }

    /**
     * 检验报告签名（同上）
     */
    private SignatureVO signLabReport(BizLaboratoryRecord r, boolean audit) {
        return signReport(SignBizTypeEnum.LAB_REPORT.getCode(), r.getId(), r.getRecordNo(),
                audit ? SignSceneEnum.REPORT_AUDIT : SignSceneEnum.REPORT_ISSUE,
                r.getLaboratoryDeptId(), r.getLaboratoryDeptName(), "检验报告");
    }

    /**
     * 报告签名的统一入口。
     *
     * <p><b>签名人取自登录态，不取入参里的 {@code executeBy / auditBy}</b>：
     * 那两个字段是前端传的姓名字符串（{@code String}），谁都能填成别人的名字 ——
     * 用它签名等于签名可随意伪造，不可否认性为零。
     *
     * <p>签名失败一律抛出、让事务回滚：**一份"没有医师签名却显示已出结果"的报告
     * 比一次执行失败危险得多** —— 它会安静地流到临床。
     */
    private SignatureVO signReport(Integer bizType, Long bizId, String bizNo, SignSceneEnum scene,
                                   Long deptId, String deptName, String bizLabel) {
        Long signerId = UserUtils.getCurrentUser().getEmployeeId();
        if (signerId == null) {
            throw new BusinessException(bizLabel + " " + bizNo + " 签名失败：取不到当前登录用户，无法确定签名人");
        }
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(bizType);
        cmd.setBizId(bizId);
        cmd.setSignScene(scene.getCode());
        cmd.setSignerId(signerId);
        cmd.setSignerName(UserUtils.getCurrentUser().getRealName());
        cmd.setSignerDeptId(deptId);
        cmd.setSignerDeptName(deptName);
        try {
            return signatureService.sign(cmd);
        } catch (BusinessException e) {
            throw new BusinessException(bizLabel + " " + bizNo + " " + scene.getText() + "失败：" + e.getMessage());
        }
    }

    @Override
    public PageResult<BizReport> selectReportPage(Long patientId, Integer reportType, Integer reportStatus,
                                                  int pageNum, int pageSize) {
        LambdaQueryWrapper<BizReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizReport::getPatientId, patientId)
                .eq(reportType != null, BizReport::getReportType, reportType)
                .eq(reportStatus != null, BizReport::getReportStatus, reportStatus)
                .orderByDesc(BizReport::getCreateTime);

        Page<BizReport> page = reportMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public PageResult<BizReportVO> selectReportPageVO(Long patientId, Integer reportType, Integer reportStatus,
                                                      int pageNum, int pageSize) {
        PageResult<BizReport> page = selectReportPage(patientId, reportType, reportStatus, pageNum, pageSize);
        List<BizReportVO> voList = new ArrayList<>();
        if (page.getRecords() != null) {
            voList = page.getRecords().stream().map(this::toReportVO).collect(Collectors.toList());
        }
        return PageResult.of(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getPages(), voList);
    }

    @Override
    public BizReport getReportDetail(Long reportId) {
        return reportMapper.selectById(reportId);
    }

    @Override
    public BizReportVO getReportDetailVO(Long reportId) {
        BizReport report = getReportDetail(reportId);
        if (report == null) {
            return null;
        }
        BizReportVO vo = toReportVO(report);
        vo.setImages(examImageService.listByReportId(report.getId()));
        vo.setLabItems(loadLabItems(report));
        return vo;
    }

    /**
     * 检验报告带出结果明细（项目 / 结果值 / 参考区间 / 异常判定）。
     *
     * <p>院内工作站与患者端小程序共用：检查报告（reportType=1）没有逐项结果，返回空列表。
     * 异常文案走 {@link #toLabResultVO} 统一算，前端不要拿 abnormalFlag 自己写三目。
     */
    private List<BizLabResultVO> loadLabItems(BizReport report) {
        if (report.getRecordId() == null || !ReportTypeEnum.LAB_TEST.getCode().equals(report.getReportType())) {
            return List.of();
        }
        return labResultMapper.selectList(new LambdaQueryWrapper<BizLabResult>()
                        .eq(BizLabResult::getRecordId, report.getRecordId())
                        .orderByAsc(BizLabResult::getSortOrder)
                        .orderByAsc(BizLabResult::getId))
                .stream().map(this::toLabResultVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishReport(Long reportId, String publishBy) {
        BizReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报告不存在");
        }
        report.setReportStatus(4);
        report.setPublishBy(publishBy);
        report.setPublishTime(LocalDateTime.now());
        boolean ok = reportMapper.updateById(report) > 0;
        if (ok) {
            // 订阅消息口子（小程序一期）：报告出具通知。通道未启用/患者未注册时实现内部留痕/跳过，
            // 发送失败绝不让发布回滚 —— 通知只是「让患者更快知道」。
            try {
                sysMessageService.sendWechatToPatient(report.getPatientId(), "report_ready",
                        "pages/report/report",
                        Map.of("报告名称", report.getItemName() == null ? "" : report.getItemName()),
                        "报告出具通知",
                        "您的报告「" + (report.getItemName() == null ? "" : report.getItemName()) + "」已出具，请查看",
                        "report", report.getId());
            } catch (Exception e) {
                log.warn("报告出具订阅消息发送失败 reportId={} err={}", reportId, e.getMessage());
            }
        }
        return ok;
    }

    // 标本管理

    @Override
    public PageResult<BizLaboratoryRecord> selectSpecimenPage(Long patientId, Integer recordStatus,
                                                              String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<BizLaboratoryRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, BizLaboratoryRecord::getPatientId, patientId)
                .eq(recordStatus != null, BizLaboratoryRecord::getRecordStatus, recordStatus)
                .and(keyword != null && !keyword.isEmpty(), w -> w
                        .like(BizLaboratoryRecord::getSpecimenNo, keyword)
                        .or().like(BizLaboratoryRecord::getPatientName, keyword)
                        .or().like(BizLaboratoryRecord::getLaboratoryItemName, keyword)
                        .or().like(BizLaboratoryRecord::getPatientNo, keyword))
                .orderByDesc(BizLaboratoryRecord::getCreateTime);

        Page<BizLaboratoryRecord> page = laboratoryRecordMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    @Override
    public PageResult<BizLaboratoryRecordVO> selectSpecimenPageVO(Long patientId, Integer recordStatus,
                                                                  String keyword, int pageNum, int pageSize) {
        PageResult<BizLaboratoryRecord> page = selectSpecimenPage(patientId, recordStatus, keyword, pageNum, pageSize);
        return PageResult.of(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getPages(),
                toLaboratoryListVo(page.getRecords()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignBarcode(Long recordId, String specimenNo) {
        BizLaboratoryRecord record = laboratoryRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("检验记录不存在");
        }
        record.setSpecimenNo(specimenNo);
        record.setSpecimenStatus(1); // 已分配条码
        laboratoryRecordMapper.updateById(record);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sampleSpecimen(Long recordId, String sampleBy) {
        BizLaboratoryRecord record = laboratoryRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("检验记录不存在");
        }
        record.setSampleTime(LocalDateTime.now());
        record.setSampleBy(sampleBy);
        record.setRecordStatus(2); // 已采样
        record.setSpecimenStatus(2); // 已采集
        laboratoryRecordMapper.updateById(record);
        // 批次E：不再回写申请单。原来这里把申请单写成「已缴费（2）」并注释「已缴费/已采样」，
        // 在「采样」这个动作里去改「缴费」字段，是这次口径收口要消灭的典型写法。
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectSpecimen(Long recordId, String reason) {
        BizLaboratoryRecord record = laboratoryRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("检验记录不存在");
        }
        record.setSpecimenStatus(99); // 异常/退回
        record.setSuggestions(reason);
        laboratoryRecordMapper.updateById(record);
        return true;
    }

    @Override
    public SpecimenStatsVO getSpecimenStats() {
        // 今日标本总数
        LambdaQueryWrapper<BizLaboratoryRecord> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(BizLaboratoryRecord::getCreateTime, java.time.LocalDate.now().atStartOfDay());
        long todayCount = laboratoryRecordMapper.selectCount(todayWrapper);

        // 各状态数量
        long pendingSample = laboratoryRecordMapper.selectCount(new LambdaQueryWrapper<BizLaboratoryRecord>()
                .eq(BizLaboratoryRecord::getRecordStatus, 1));
        long sampled = laboratoryRecordMapper.selectCount(new LambdaQueryWrapper<BizLaboratoryRecord>()
                .eq(BizLaboratoryRecord::getRecordStatus, 2));
        long testing = laboratoryRecordMapper.selectCount(new LambdaQueryWrapper<BizLaboratoryRecord>()
                .in(BizLaboratoryRecord::getRecordStatus, 3, 4));
        long abnormal = laboratoryRecordMapper.selectCount(new LambdaQueryWrapper<BizLaboratoryRecord>()
                .eq(BizLaboratoryRecord::getSpecimenStatus, 99));

        SpecimenStatsVO stats = new SpecimenStatsVO();
        stats.setTodayCount(todayCount);
        stats.setPendingSample(pendingSample);
        stats.setSampled(sampled);
        stats.setTesting(testing);
        stats.setAbnormal(abnormal);
        return stats;
    }

    // 缴费驱动建执行记录（批次E：从 his-charge 迁入并补幂等）

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long ensureInspectionRecordFromApply(Long applyId) {
        if (applyId == null) {
            throw new BusinessException("检查申请单ID为空，无法生成检查记录");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        // 幂等闸门：同一张申请单只允许有一条检查记录。
        // getOne(wrapper, false) 不抛异常，存量重复数据不会把缴费直接打挂。
        BizInspectionRecord exists = this.getOne(new LambdaQueryWrapper<BizInspectionRecord>()
                .eq(BizInspectionRecord::getApplyId, applyId)
                .orderByAsc(BizInspectionRecord::getId), false);
        if (exists != null) {
            // 退费后再缴费：**同一个申请单 = 同一个工作项**，复活原来那条，而不是再造一条。
            // 造新条会让「一张申请单对应几条检查记录」这个不确定性继续存在，
            // 检查科叫号、结果回填、报告关联就都得靠"猜哪一条是对的"。
            if (InsRecordStatusEnum.CANCELLED.getCode().equals(exists.getRecordStatus())) {
                this.update(new LambdaUpdateWrapper<BizInspectionRecord>()
                        .eq(BizInspectionRecord::getId, exists.getId())
                        .set(BizInspectionRecord::getRecordStatus, InsRecordStatusEnum.REGISTERED.getCode())
                        .set(BizInspectionRecord::getCancelTime, null)
                        .set(BizInspectionRecord::getCancelReason, null));
                log.info("[检查记录] 申请单 {} 的检查记录 {} 曾被取消，本次缴费将其复活为「已登记」",
                        applyId, exists.getRecordNo());
                return exists.getId();
            }
            log.info("[检查记录] 申请单 {} 已存在检查记录 {}（id={}），跳过重复生成",
                    applyId, exists.getRecordNo(), exists.getId());
            return exists.getId();
        }

        BizInspectionApply apply = inspectionApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("未能找到对应的检查申请数据");
        }

        BizInspectionRecord record = new BizInspectionRecord();
        record.setRecordNo(genNo("IR"));
        record.setApplyId(apply.getId());
        record.setApplyNo(apply.getApplyNo());
        record.setPatientId(apply.getPatientId());
        record.setPatientNo(apply.getPatientNo());
        record.setPatientName(apply.getPatientName());
        record.setGender(apply.getGender());
        record.setAge(apply.getAge());
        record.setVisitDate(apply.getVisitDate());
        record.setApplyDeptId(apply.getDeptId());
        record.setApplyDeptName(apply.getDeptName());
        record.setApplyDoctorId(apply.getDoctorId());
        record.setApplyDoctorName(apply.getDoctorName());
        record.setInspectionItemId(apply.getInspectionItemId());
        record.setInspectionItemCode(apply.getInspectionItemCode());
        record.setInspectionItemName(apply.getInspectionItemName());
        record.setInspectionDeptId(apply.getInspectionDeptId());
        record.setInspectionDeptName(apply.getInspectionDeptName());
        record.setBodyPart(apply.getBodyPart());
        record.setInspectionPurpose(apply.getInspectionPurpose());
        record.setClinicalDiagnosis(apply.getClinicalDiagnosis());
        record.setPrice(apply.getPrice());
        record.setRecordStatus(InsRecordStatusEnum.REGISTERED.getCode()); // 1-已登记
        record.setCreateBy(operatorUser.getRealName());
        record.setCreateTime(LocalDateTime.now());
        inspectionRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long ensureLaboratoryRecordFromApply(Long applyId) {
        if (applyId == null) {
            throw new BusinessException("检验申请单ID为空，无法生成检验记录");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizLaboratoryRecord exists = laboratoryRecordMapper.selectOne(new LambdaQueryWrapper<BizLaboratoryRecord>()
                .eq(BizLaboratoryRecord::getApplyId, applyId)
                .orderByAsc(BizLaboratoryRecord::getId)
                .last("LIMIT 1"));
        if (exists != null) {
            if (LabRecordStatusEnum.CANCELLED.getCode() == (exists.getRecordStatus() == null ? -1 : exists.getRecordStatus())) {
                laboratoryRecordMapper.update(null, new LambdaUpdateWrapper<BizLaboratoryRecord>()
                        .eq(BizLaboratoryRecord::getId, exists.getId())
                        .set(BizLaboratoryRecord::getRecordStatus, LabRecordStatusEnum.REGISTERED.getCode())
                        .set(BizLaboratoryRecord::getCancelTime, null)
                        .set(BizLaboratoryRecord::getCancelReason, null)
                        .set(BizLaboratoryRecord::getSpecimenStatus, 1));
                log.info("[检验记录] 申请单 {} 的检验记录 {} 曾被取消，本次缴费将其复活为「已登记」",
                        applyId, exists.getRecordNo());
                return exists.getId();
            }
            log.info("[检验记录] 申请单 {} 已存在检验记录 {}（id={}），跳过重复生成",
                    applyId, exists.getRecordNo(), exists.getId());
            return exists.getId();
        }

        BizLaboratoryApply apply = laboratoryApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BusinessException("未能找到对应的检验申请数据");
        }

        BizLaboratoryRecord record = new BizLaboratoryRecord();
        record.setRecordNo(genNo("LR"));
        record.setApplyId(apply.getId());
        record.setApplyNo(apply.getApplyNo());
        record.setPatientId(apply.getPatientId());
        record.setPatientNo(apply.getPatientNo());
        record.setPatientName(apply.getPatientName());
        record.setGender(apply.getGender());
        record.setAge(apply.getAge());
        record.setVisitDate(apply.getVisitDate());
        record.setApplyDeptId(apply.getDeptId());
        record.setApplyDeptName(apply.getDeptName());
        record.setApplyDoctorId(apply.getDoctorId());
        record.setApplyDoctorName(apply.getDoctorName());
        record.setLaboratoryItemId(apply.getLaboratoryItemId());
        record.setLaboratoryItemCode(apply.getLaboratoryItemCode());
        record.setLaboratoryItemName(apply.getLaboratoryItemName());
        record.setLaboratoryDeptId(apply.getLaboratoryDeptId());
        record.setLaboratoryDeptName(apply.getLaboratoryDeptName());
        record.setSpecimenType(apply.getSpecimenType());
        record.setPrice(apply.getPrice());
        record.setRecordStatus(LabRecordStatusEnum.REGISTERED.getCode()); // 1-已登记
        record.setCreateBy(operatorUser.getRealName());
        record.setCreateTime(LocalDateTime.now());
        laboratoryRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelInspectionByApplyId(Long applyId, String reason) {
        if (applyId == null) {
            return false;
        }
        BizInspectionRecord record = this.getOne(new LambdaQueryWrapper<BizInspectionRecord>()
                .eq(BizInspectionRecord::getApplyId, applyId)
                .orderByAsc(BizInspectionRecord::getId), false);
        if (record == null) {
            return false;
        }
        if (InsRecordStatusEnum.CANCELLED.getCode().equals(record.getRecordStatus())) {
            return false;
        }
        // 已开始执行的检查不允许靠「退费」抹掉：检查已经做了、报告可能已经出了，
        // 那不是退费能解决的问题，得走作废/冲红流程。这里宁可报错也不静默放过。
        if (record.getRecordStatus() != null
                && record.getRecordStatus() >= InsRecordStatusEnum.CHECKING.getCode()) {
            throw new BusinessException("检查记录 " + record.getRecordNo() + " 已开始执行（"
                    + InsRecordStatusEnum.getByCode(record.getRecordStatus()).getDesc()
                    + "），不允许退费；如确需取消请走检查作废流程");
        }
        this.update(new LambdaUpdateWrapper<BizInspectionRecord>()
                .eq(BizInspectionRecord::getId, record.getId())
                .set(BizInspectionRecord::getRecordStatus, InsRecordStatusEnum.CANCELLED.getCode())
                .set(BizInspectionRecord::getCancelTime, LocalDateTime.now())
                .set(BizInspectionRecord::getCancelReason, reason));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelLaboratoryByApplyId(Long applyId, String reason) {
        if (applyId == null) {
            return false;
        }
        BizLaboratoryRecord record = laboratoryRecordMapper.selectOne(new LambdaQueryWrapper<BizLaboratoryRecord>()
                .eq(BizLaboratoryRecord::getApplyId, applyId)
                .orderByAsc(BizLaboratoryRecord::getId)
                .last("LIMIT 1"));
        if (record == null) {
            return false;
        }
        if (record.getRecordStatus() != null
                && record.getRecordStatus() == LabRecordStatusEnum.CANCELLED.getCode()) {
            return false;
        }
        // 检验口径：2 已采样之后就算「已经做了」（标本都抽了），只能走作废
        if (record.getRecordStatus() != null
                && record.getRecordStatus() >= LabRecordStatusEnum.SAMPLED.getCode()) {
            throw new BusinessException("检验记录 " + record.getRecordNo() + " 已开始执行（"
                    + LabRecordStatusEnum.getByCode(record.getRecordStatus()).getDescription()
                    + "），不允许退费；如确需取消请走检验作废流程");
        }
        laboratoryRecordMapper.update(null, new LambdaUpdateWrapper<BizLaboratoryRecord>()
                .eq(BizLaboratoryRecord::getId, record.getId())
                .set(BizLaboratoryRecord::getRecordStatus, LabRecordStatusEnum.CANCELLED.getCode())
                .set(BizLaboratoryRecord::getSpecimenStatus, 6) // 已退回
                .set(BizLaboratoryRecord::getCancelTime, LocalDateTime.now())
                .set(BizLaboratoryRecord::getCancelReason, reason));
        return true;
    }

    /**
     * 生成执行记录号：前缀 + yyyyMMddHHmmss + 4 位进程内自增。
     *
     * <p>与 his-charge 原来的写法保持一致（迁移前是 IR/LR + 时间戳 + EXECUTION_SEQ），
     * 只是把序号放在本模块，不再由收费模块持有。
     */
    private String genNo(String prefix) {
        return prefix + LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", EXECUTION_SEQ.incrementAndGet() % 10000);
    }

}
