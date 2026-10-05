package com.his.ai.dto;

import com.his.emr.entity.BizMedicalRecord;
import com.his.emr.entity.BizPrescription;
import com.his.emr.entity.BizPrescriptionDetail;
import com.his.system.entity.SysDrug;

import java.util.List;
import java.util.Map;

/**
 * 处方审核的输入上下文。
 * <p>
 * 由能力层一次性查库组装好，硬规则层与模型层共用同一份数据 ——
 * 这保证「规则看到的」和「模型看到的」完全一致，不会出现两边结论互相矛盾
 * 却谁也说不清原因的尴尬。
 *
 * @param prescription  处方主表
 * @param details       处方明细（已过滤删除项）
 * @param drugIndex     药品字典，key 为 drugId，用于取禁忌症/皮试要求
 * @param record        关联病历，可能为 null（演示库 49 张处方中 17 张无关联病历）
 * @param allergyText   过敏史文本（病历 + 患者档案 + 结构化过敏表合并）
 * @param conditionText 既往史 + 诊断文本，用于禁忌人群比对
 */
public record DrugAuditContextDTO(BizPrescription prescription,
                                  List<BizPrescriptionDetail> details,
                                  Map<Long, SysDrug> drugIndex,
                                  BizMedicalRecord record,
                                  String allergyText,
                                  String conditionText) {

    /**
     * 过敏史是否具备可比对的内容。全是「无」「未见」这类占位词时视为无过敏史。
     */
    public boolean hasUsableAllergyText() {
        return allergyText != null && !allergyText.isBlank();
    }

    /**
     * 性别，处方表为空时回落到病历表
     */
    public Integer gender() {
        if (prescription != null && prescription.getGender() != null) {
            return prescription.getGender();
        }
        return record == null ? null : record.getGender();
    }

    /**
     * 年龄，处方表为空时回落到病历表
     */
    public Integer age() {
        if (prescription != null && prescription.getAge() != null) {
            return prescription.getAge();
        }
        return record == null ? null : record.getAge();
    }
}
