package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.DelFlagEnum;
import com.his.common.enums.InsRecordStatusEnum;
import com.his.common.enums.ReportStatusEnum;
import com.his.common.enums.ReportTypeEnum;
import com.his.common.util.TextUtil;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.entity.BizReport;
import com.his.medicaltech.mapper.BizInspectionRecordMapper;
import com.his.medicaltech.mapper.BizReportMapper;
import com.his.medicaltech.service.InspectionSimulateService;
import com.his.spi.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 检查设备仿真回传实现。
 *
 * <p>仿真口径：患者签到（状态 2 已签到 / 3 检查中）后，模拟设备排队采集、生成检查所见与结论，
 * 回写检查记录并建一张待审核报告。不生成影像图片，报告先停在「待审核」，由诊断医师在工作站复核签名，
 * 与真实设备通道一致 —— 仿真只负责把结果文本推到待审队列，不替医师审核定稿。
 *
 * <p>操作人归属：定时任务无登录态，按系统任务豁免口径记 {@code system:deviceSim}（employeeId 0L），
 * 不冒充真人医师。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InspectionSimulateServiceImpl implements InspectionSimulateService {

    /** 仿真设备通道操作人名（系统任务豁免值，留痕可追溯） */
    private static final String OPERATOR_NAME = "system:deviceSim";
    /** 系统任务操作人 employeeId 口径同 RobotDispatchQueueTask：0L = 系统通道 */
    private static final Long OPERATOR_ID = 0L;
    /** 仿真出异常结论的概率（其余为未见异常） */
    private static final double ABNORMAL_RATIO = 0.15;
    /** 阴阳性：3-未见异常；2-阳性 */
    private static final int FLAG_NORMAL = 3;
    private static final int FLAG_ABNORMAL = 2;

    private final BizInspectionRecordMapper bizInspectionRecordMapper;
    private final BizReportMapper bizReportMapper;
    private final RedisSequenceService redisSequenceService;

    @Override
    public List<Long> pendingIds(int maxPerRound) {
        List<BizInspectionRecord> pending = bizInspectionRecordMapper.selectList(
                new LambdaQueryWrapper<BizInspectionRecord>()
                        .eq(BizInspectionRecord::getDelFlag, DelFlagEnum.NORMAL.getCode())
                        .in(BizInspectionRecord::getRecordStatus,
                                InsRecordStatusEnum.SIGNED_IN.getCode(),
                                InsRecordStatusEnum.CHECKING.getCode())
                        .and(w -> w.isNull(BizInspectionRecord::getResultDescription)
                                .or().eq(BizInspectionRecord::getResultDescription, ""))
                        .orderByAsc(BizInspectionRecord::getCheckInTime)
                        .last("LIMIT " + Math.max(1, maxPerRound)));
        return pending.stream().map(BizInspectionRecord::getId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean simulateOne(Long recordId) {
        BizInspectionRecord record = bizInspectionRecordMapper.selectById(recordId);
        if (record == null || (record.getDelFlag() != null
                && record.getDelFlag() == DelFlagEnum.DELETED.getCode())) {
            return false;
        }
        // 二次校验：并发下可能已被真人工作站处理过，不再覆盖
        Integer status = record.getRecordStatus();
        if (!InsRecordStatusEnum.SIGNED_IN.is(status) && !InsRecordStatusEnum.CHECKING.is(status)) {
            return false;
        }
        if (TextUtil.hasText(record.getResultDescription())) {
            return false;
        }

        Finding finding = Finding.of(record.getInspectionItemName(), record.getBodyPart());
        LocalDateTime now = LocalDateTime.now();

        record.setExecuteTime(now);
        record.setExecuteBy(OPERATOR_NAME);
        record.setReportBy(OPERATOR_NAME);
        record.setResultDescription(finding.description());
        record.setResultConclusion(finding.conclusion());
        record.setRecordStatus(InsRecordStatusEnum.RESULTED.getCode());
        bizInspectionRecordMapper.updateById(record);

        BizReport report = ensureReport(record);
        report.setReportContent(finding.description());
        report.setConclusion(finding.conclusion());
        report.setSuggestions(TextUtil.cutToNull(finding.suggestion(), 1000));
        report.setPositiveFlag(finding.positiveFlag());
        report.setIsCritical(0);
        report.setWriteBy(OPERATOR_NAME);
        report.setWriteById(OPERATOR_ID);
        report.setWriteTime(now);
        report.setReportStatus(ReportStatusEnum.PENDING_REVIEW.getCode());
        if (report.getId() == null) {
            report.setReportNo(redisSequenceService.generateReportNo());
            report.setReportVersion(1);
            report.setFilmCount(0);
            bizReportMapper.insert(report);
        } else {
            bizReportMapper.updateById(report);
        }
        return true;
    }

    /** 取该检查单已有的检查报告；没有则按记录快照新建（待审核，不签 CA） */
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
        return report;
    }

    /**
     * 仿真结果模板：按项目名称 / 部位关键词归类，正常模板与轻度异常模板各备若干条随机抽取。
     * 异常只取临床常见的轻度改变，不生成危重结论；影像图片不在仿真范围内。
     */
    private record Finding(String description, String conclusion, String suggestion, int positiveFlag) {

        static Finding of(String itemName, String bodyPart) {
            String text = TextUtil.nullToEmpty(itemName) + " " + TextUtil.nullToEmpty(bodyPart);
            boolean abnormal = ThreadLocalRandom.current().nextDouble() < ABNORMAL_RATIO;
            Category category = match(text);
            return abnormal ? category.abnormal() : category.normal();
        }

        private static Category match(String text) {
            if (containsAny(text, "心", "心电")) {
                return Category.HEART;
            }
            if (containsAny(text, "头颅", "脑")) {
                return Category.HEAD;
            }
            if (containsAny(text, "胸", "肺")) {
                return Category.CHEST;
            }
            if (containsAny(text, "腹", "胃", "肠", "肝", "胆", "胰", "脾", "肾", "上消化")) {
                return Category.ABDOMEN;
            }
            if (containsAny(text, "骨", "关节", "四肢", "颈", "腰", "脊柱")) {
                return Category.LIMB;
            }
            return Category.GENERIC;
        }

        private static boolean containsAny(String text, String... keys) {
            for (String key : keys) {
                if (text.contains(key)) {
                    return true;
                }
            }
            return false;
        }
    }

    /** 部位分类：正常 / 异常两套文案 */
    private enum Category {

        CHEST(
                List.of(
                        "胸廓对称，气管居中。双肺野清晰，肺纹理走行自然，未见明显斑片、结节及肿块影；双侧肺门不大，心影大小形态在正常范围，双侧膈面光滑，肋膈角锐利。",
                        "双肺纹理稍增多，未见实质性浸润灶；心影形态正常，双膈面光整，肋膈角锐利。"),
                "胸部未见明显异常。",
                List.of(
                        "右下肺野可见斑片状密度增高影，边缘模糊；余肺野清晰，双侧肋膈角尚锐利，心影形态正常。",
                        "双肺纹理增多紊乱，左肺野可见小片状模糊影；心膈未见明显异常。"),
                "肺部感染性病变可能，建议结合临床症状，必要时复查胸片或进一步胸部 CT 检查。",
                "建议呼吸科随诊，必要时进一步胸部 CT 检查。"),
        HEAD(
                List.of(
                        "脑实质内未见明显异常密度/信号影，灰白质分界清晰；脑室系统大小形态正常，脑沟、脑裂无增宽，中线结构居中。",
                        "颅骨骨质结构完整，颅内未见明显占位及出血灶，中线结构居中。"),
                "头颅未见明显异常。",
                List.of(
                        "右侧额叶可见一小片状低密度影，边界欠清；脑室系统未见明显扩张，中线结构居中。",
                        "基底节区可见点状缺血灶，余脑实质未见明显异常信号，中线居中。"),
                "缺血灶可能，建议结合临床，必要时行头颅 MRI 进一步检查。",
                "建议神经内科随诊，控制血压、血脂，必要时复查头颅 CT/MRI。"),
        HEART(
                List.of(
                        "窦性心律，心律齐，各导联波形形态及电压未见明显异常。",
                        "心律规整，心率在正常范围，未见明显异常波形及 ST-T 段改变。"),
                "心电图未见明显异常。",
                List.of(
                        "部分导联 ST-T 段轻度改变，心律齐。",
                        "窦性心律，偶发室上性早搏，部分导联 T 波低平。"),
                "提示心肌复极异常可能，建议结合临床症状及既往心电图对比。",
                "建议心内科就诊，必要时行动态心电图及心脏超声检查。"),
        ABDOMEN(
                List.of(
                        "肝胆胰脾肾形态大小未见明显异常，实质回声均匀，未见占位性病变；腹腔未见明显积液及游离气体。",
                        "胃腔充盈可，管壁层次清晰，未见明显增厚及占位；余腹部脏器未见明显异常。"),
                "腹部脏器未见明显异常。",
                List.of(
                        "胆囊壁稍毛糙，腔内未见明显强回声团；余肝胆胰脾未见明显异常。",
                        "肝脏回声稍增粗，未见明确占位；门静脉内径正常。"),
                "胆囊炎征象/肝实质回声改变，请结合临床症状。",
                "建议消化科随诊，清淡饮食，必要时复查腹部超声。"),
        LIMB(
                List.of(
                        "关节对位良好，骨质结构完整，骨皮质连续，关节间隙未见明显狭窄；周围软组织未见明显肿胀。",
                        "骨质密度均匀，未见骨折线及骨质破坏，关节关系正常。"),
                "骨关节未见明显异常。",
                List.of(
                        "骨质连续性未见中断，关节对位可；局部软组织稍肿胀。",
                        "关节间隙轻度变窄，边缘可见少许骨质增生改变。"),
                "软组织损伤/退行性改变可能，建议结合外伤史及症状。",
                "建议骨科随诊，对症处理，必要时复查影像。"),
        GENERIC(
                List.of("本次检查所见未见明显异常改变。"),
                "未见明显异常。",
                List.of("本次检查可见一处轻度异常改变，详见检查描述；建议结合临床综合判断。"),
                "请结合临床症状及既往检查结果综合判断，必要时复查。",
                "建议相关科室随诊，必要时进一步检查。");

        private final List<String> normalDescriptions;
        private final String normalConclusion;
        private final List<String> abnormalDescriptions;
        private final String abnormalConclusion;
        private final String abnormalSuggestion;

        Category(List<String> normalDescriptions, String normalConclusion,
                 List<String> abnormalDescriptions, String abnormalConclusion, String abnormalSuggestion) {
            this.normalDescriptions = normalDescriptions;
            this.normalConclusion = normalConclusion;
            this.abnormalDescriptions = abnormalDescriptions;
            this.abnormalConclusion = abnormalConclusion;
            this.abnormalSuggestion = abnormalSuggestion;
        }

        Finding normal() {
            return new Finding(pick(normalDescriptions), normalConclusion, null, FLAG_NORMAL);
        }

        Finding abnormal() {
            return new Finding(pick(abnormalDescriptions), abnormalConclusion, abnormalSuggestion, FLAG_ABNORMAL);
        }

        private static String pick(List<String> options) {
            return options.get(ThreadLocalRandom.current().nextInt(options.size()));
        }
    }
}
