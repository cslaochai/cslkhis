package com.his.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.ai.dto.PatientMedicationGuideDTO;
import com.his.ai.entity.SysDrugGuide;
import com.his.ai.enums.DrugFrequencyEnum;
import com.his.ai.mapper.SysDrugGuideMapper;
import com.his.ai.service.PatientMedicationGuideCapability;
import com.his.ai.vo.PatientMedicationGuideVO;
import com.his.ai.vo.PatientMedicationItemVO;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.emr.entity.BizPrescription;
import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.mapper.BizPrescriptionDetailMapper;
import com.his.emr.mapper.BizPrescriptionMapper;
import com.his.patient.service.PatientGuardianService;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 患者端用药说明实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientMedicationGuideCapabilityImpl implements PatientMedicationGuideCapability {

    /**
     * 处方状态：已发药
     */
    private static final int STATUS_DISPENSED = 4;

    /**
     * 处方类型：3-中药饮片处方
     */
    private static final int TYPE_HERB = 3;

    /**
     * 煎服方式：1-代煎 2-自煎
     */
    private static final int DECOCT_BY_HOSPITAL = 1;

    /**
     * 医嘱没写单次剂量时的兜底：不猜，交给医生/药师
     */
    private static final String DOSAGE_UNKNOWN = "按医生交代服用";

    /**
     * 固定免责提示。
     * <p>
     * 少了这段，「连服 7 天」会被读成"吃完 7 天就一定好了"。
     * 与报告解读同理：这句话是这个能力能上线的<b>前提条件</b>，不是形式主义。
     */
    private static final String ADVICE =
            "以上是按医生这次处方整理的服用方法，不能代替医生和药师的交代。"
                    + "服药后如果出现皮疹、恶心呕吐、腹泻等不舒服，请先停药并及时联系医生或药师。";

    /**
     * 漏服处理：通用安全科普，与具体药品无关，所以放在公共位置而不是按药品判定
     */
    private static final String CAUTION_MISSED =
            "漏服时：想起来就补一次；如果已经快到下一次吃药的时间，就跳过这次，不要一次吃两份。";

    private final BizPrescriptionMapper bizPrescriptionMapper;

    private final BizPrescriptionDetailMapper bizPrescriptionDetailMapper;

    private final SysDrugGuideMapper sysDrugGuideMapper;

    private final PatientGuardianService patientGuardianService;

    /**
     * 单次剂量：优先用法用量原句，其次单次剂量，都没有就直说不知道。
     * <p>
     * 用法用量（{@code usage_dosage}）在库里常见的是「每次0.5g」这类完整表述，
     * 比单列的单次剂量更不容易断章取义，所以它优先。
     */
    private static String dosageText(BizPrescriptionDetail detail, boolean herb) {
        if (herb) {
            return "一剂";
        }
        if (TextUtil.hasText(detail.getUsageDosage())) {
            return detail.getUsageDosage().trim();
        }
        if (TextUtil.hasText(detail.getSingleDosage())) {
            return "每次 " + detail.getSingleDosage().trim();
        }
        return DOSAGE_UNKNOWN;
    }

    // ---------------------------------------------------------------- 组装层

    private static String quantityText(BizPrescriptionDetail detail, boolean herb) {
        BigDecimal quantity = detail.getQuantity();
        if (quantity == null) {
            return null;
        }
        String unit = TextUtil.hasText(detail.getUnit()) ? detail.getUnit().trim() : "";
        // 中药按剂：处方主表的剂数才是"几副药"，明细 quantity 是每味的克数，不能混着显示
        return herb ? null : quantity.stripTrailingZeros().toPlainString() + unit;
    }

    /**
     * 疗程：长处方取长处方天数，中药取剂数，其余取疗程天数
     */
    private static String courseText(BizPrescriptionDetail detail, boolean herb, BizPrescription prescription) {
        if (herb) {
            Integer doses = prescription.getDoseCount();
            if (doses != null && doses > 0) {
                return "共 " + doses + " 剂";
            }
            return null;
        }
        Integer longDays = prescription.getLongPrescriptionDays();
        if (Integer.valueOf(1).equals(prescription.getIsLongPrescription()) && longDays != null && longDays > 0) {
            return "长处方，共 " + longDays + " 天用量";
        }
        Integer duration = detail.getDuration();
        return duration != null && duration > 0 ? "连服 " + duration + " 天" : null;
    }

    /**
     * 注意事项：全部由药品字典的客观属性 + 处方医嘱判定，逐条列、不合并、不推断。
     */
    private static List<String> cautions(BizPrescriptionDetail detail, SysDrugGuide drug) {
        List<String> cautions = new ArrayList<>();
        if (Integer.valueOf(1).equals(detail.getIsSkinTest())) {
            cautions.add("这个药需要先做皮试，请配合护士完成皮试后再用药。");
        }
        if (drug != null) {
            if (Integer.valueOf(1).equals(drug.getIsColdChain())) {
                cautions.add("需要冷藏（2~8℃）：取药后请尽快放进冰箱冷藏室，不要冷冻，也不要贴在冰箱壁上。");
            } else if (TextUtil.hasText(drug.getStorageCondition())) {
                cautions.add("储存要求：" + drug.getStorageCondition().trim());
            }
            if (drug.getSpecialFlag() != null && drug.getSpecialFlag() > 0) {
                cautions.add("这是特殊管理药品，请严格按医生交代服用，不要转给他人。");
            }
            if (drug.getAntibioticLevel() != null && drug.getAntibioticLevel() > 0) {
                cautions.add("这是抗菌药，请按医生开的疗程服完，不要觉得好转就自行停药。");
            }
        }
        cautions.add(CAUTION_MISSED);
        return cautions;
    }

    /**
     * 频次白话化。
     * <p>
     * 库里实测值混着中文（「一日三次」「每日2次」）和英文缩写（「qd」），
     * 患者看缩写只会去百度。这里只做同义改写与汉字数字转阿拉伯数字，
     * <b>查不到就原样返回</b> —— 猜错频次等于告诉患者多吃一倍。
     */
    private static String frequencyText(String frequency) {
        if (!TextUtil.hasText(frequency)) {
            return null;
        }
        String raw = frequency.trim();
        String hit = DrugFrequencyEnum.getText(raw);
        if (TextUtil.hasText(hit)) {
            return hit;
        }
        String text = raw.replace("一日", "每天").replace("每日", "每天");
        text = cnDigitsToArabic(text);
        // 「每天3次」读起来像一串字符，汉字与数字之间两侧都补空格
        return text.replaceAll("(?<=[\\u4e00-\\u9fa5])(?=\\d)", " ")
                .replaceAll("(?<=\\d)(?=[\\u4e00-\\u9fa5])", " ");
    }

    private static String cnDigitsToArabic(String text) {
        String result = text;
        String[] cn = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        for (int i = 1; i < cn.length; i++) {
            result = result.replace(cn[i], String.valueOf(i));
        }
        return result;
    }

    // ---------------------------------------------------------------- 词典层

    private static String prescriptionTypeText(Integer type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case 1 -> "西药处方";
            case 2 -> "中成药处方";
            case 3 -> "中药饮片处方";
            default -> "";
        };
    }

    @Override
    public PatientMedicationGuideVO execute(PatientMedicationGuideDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || user.getPatientId() == null) {
            throw new BusinessException("未获取到就诊人身份，请重新登录");
        }

        BizPrescription prescription = bizPrescriptionMapper.selectById(dto.getPrescriptionId());
        if (prescription == null) {
            throw new BusinessException("处方不存在：" + dto.getPrescriptionId());
        }
        if (patientGuardianService.patientScopeViolated(prescription.getPatientId())) {
            // 不区分「不存在」和「无权查看」，避免被用来探测处方是否存在
            throw new BusinessException("处方不存在或无权查看：" + dto.getPrescriptionId());
        }

        List<BizPrescriptionDetail> details = bizPrescriptionDetailMapper.selectList(
                new LambdaQueryWrapper<BizPrescriptionDetail>()
                        .eq(BizPrescriptionDetail::getPrescriptionId, prescription.getId())
                        .orderByAsc(BizPrescriptionDetail::getId));
        if (details == null || details.isEmpty()) {
            throw new BusinessException("该处方没有药品明细，无法生成用药说明");
        }

        PatientMedicationGuideVO vo = new PatientMedicationGuideVO();
        vo.setPrescriptionId(prescription.getId());
        vo.setPrescriptionNo(prescription.getPrescriptionNo());
        vo.setVisitDate(prescription.getVisitDate());
        vo.setDeptName(prescription.getDeptName());
        vo.setDoctorName(prescription.getDoctorName());
        vo.setPrescriptionTypeText(prescriptionTypeText(prescription.getPrescriptionType()));
        vo.setDispensed(Integer.valueOf(STATUS_DISPENSED).equals(prescription.getPrescriptionStatus()));

        boolean herb = Integer.valueOf(TYPE_HERB).equals(prescription.getPrescriptionType());
        List<PatientMedicationItemVO> items = new ArrayList<>();
        for (BizPrescriptionDetail detail : details) {
            items.add(toItem(detail, drugOf(detail), herb, prescription));
        }
        vo.setItems(items);
        vo.setAdvice(ADVICE);
        return vo;
    }

    private PatientMedicationItemVO toItem(BizPrescriptionDetail detail, SysDrugGuide drug,
                                           boolean herb, BizPrescription prescription) {
        PatientMedicationItemVO item = new PatientMedicationItemVO();
        item.setDrugName(detail.getDrugName());
        item.setSpecification(detail.getSpecification());
        item.setDosageForm(detail.getDosageForm());
        item.setQuantityText(quantityText(detail, herb));
        item.setDosageText(dosageText(detail, herb));
        item.setFrequencyText(frequencyText(detail.getFrequency()));
        item.setRouteText(TextUtil.hasText(detail.getRoute()) ? detail.getRoute().trim() : null);
        item.setCourseText(courseText(detail, herb, prescription));
        List<String> cautions = cautions(detail, drug);
        if (herb && Integer.valueOf(DECOCT_BY_HOSPITAL).equals(prescription.getDecoctFlag())) {
            // 代煎的中药是真空袋装好的，患者最容易犯的错是拿回家再煮一遍
            cautions.add(0, "这副药由医院代煎，拿到的是可以直接喝的袋装药液，不用再自己煮。");
        }
        item.setCautions(cautions);
        item.setSpecText(drug == null || !TextUtil.hasText(drug.getUsageDosage())
                ? null : drug.getUsageDosage().trim());
        return item;
    }

    private SysDrugGuide drugOf(BizPrescriptionDetail detail) {
        if (detail.getDrugId() != null) {
            SysDrugGuide byId = sysDrugGuideMapper.selectGuideById(detail.getDrugId());
            if (byId != null) {
                return byId;
            }
        }
        return TextUtil.hasText(detail.getDrugCode())
                ? sysDrugGuideMapper.selectGuideByCode(detail.getDrugCode().trim()) : null;
    }
}
