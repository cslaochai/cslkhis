package com.his.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 数据字典类型编码（dictType）的唯一来源。
 *
 * <p>值必须与库内字典类型逐字一致：写错**不报错** —— 字典行取不到时出参文案是空串，
 * 现象与「这个码值没有文案」一模一样，比业务 bug 更难发现。加常量前先确认库里真有这个类型。
 *
 * <p>常量名 = 字典类型编码去掉 {@code his_}／{@code sys_} 前缀后大写，所以按名字就能 grep 到字典，
 * 不需要第二张对照表。
 *
 * <p>只放代码里真用到的字典类型：库里另有几百个是后台字典页供操作员自取的，不进来 ——
 * 那等于用一份副本冒充全量，改库时没人会同步这里。
 *
 * <p>后端拿码值做分支判断的封闭集合不属本类（那些按规范落 Java 枚举）；本类只管
 * 「码值集合由操作员在后台增删、后端只翻译文案」这一类。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DictType {

    /** 入院病情 */
    public static final String ADMIT_CONDITION = "his_admit_condition";

    /** 抗菌药物处方权状态 */
    public static final String ANTIBIOTIC_AUTH_STATUS = "his_antibiotic_auth_status";

    /** 抗菌药物分级 */
    public static final String ANTIBIOTIC_LEVEL = "his_antibiotic_level";

    /** 围手术期给药时机 */
    public static final String ANTIBIOTIC_TIMING = "his_antibiotic_timing";

    /** 护理评估风险等级 */
    public static final String ASSESS_RISK_LEVEL = "his_assess_risk_level";

    /** 审计日志状态 */
    public static final String AUDIT_LOG_STATUS = "his_audit_log_status";

    /** 血液品种 */
    public static final String BLOOD_COMPONENT = "his_blood_component";

    /** 血袋状态 */
    public static final String BLOOD_INVENTORY_STATUS = "his_blood_inventory_status";

    /** Rh 血型 */
    public static final String BLOOD_RH = "his_blood_rh";

    /** 血液来源 */
    public static final String BLOOD_SOURCE_TYPE = "his_blood_source_type";

    /** 血型 */
    public static final String BLOOD_TYPE = "his_blood_type";

    /** 卡片类型 */
    public static final String CARD_TYPE = "his_card_type";

    /** 医保合规审核类型 */
    public static final String COMPLIANCE_AUDIT_TYPE = "his_compliance_audit_type";

    /** 配血方法 */
    public static final String CROSSMATCH_METHOD = "his_crossmatch_method";

    /** 配血结论(血袋) */
    public static final String CROSSMATCH_RESULT = "his_crossmatch_result";

    /** 配血状态(申请单) */
    public static final String CROSSMATCH_STATUS = "his_crossmatch_status";

    /** CSSD灭菌方式 */
    public static final String CSSD_STERIL_METHOD = "his_cssd_steril_method";

    /** 出院带药发药状态 */
    public static final String DISCHARGE_DRUG_STATUS = "his_discharge_drug_status";

    /** 剂量单位 */
    public static final String DOSE_UNIT = "his_dose_unit";

    public static final String DUTY_LOG_TYPE = "his_duty_log_type";

    /** 心电类型 */
    public static final String ECG_TYPE = "his_ecg_type";

    /** 内镜麻醉方式 */
    public static final String ENDOSCOPY_ANESTHESIA = "his_endoscopy_anesthesia";

    /** 内镜类型 */
    public static final String ENDOSCOPY_TYPE = "his_endoscopy_type";

    /** 医技检查状态（内镜/超声） */
    public static final String ENDOUS_STATUS = "his_endous_status";

    /** 设备类别 */
    public static final String EQUIPMENT_CATEGORY = "his_equipment_category";

    /** 设备状态 */
    public static final String EQUIPMENT_STATUS = "his_equipment_status";

    /** 检查预约状态 */
    public static final String EXAM_APPOINT_STATUS = "his_exam_appoint_status";

    /** 检查设备预约状态 */
    public static final String EXAM_DEVICE_STATUS = "his_exam_device_status";

    /** 检查设备类别 */
    public static final String EXAM_DEVICE_TYPE = "his_exam_device_type";

    /** 胶片状态 */
    public static final String FILM_STATUS = "his_film_status";

    /** 性别 */
    public static final String GENDER = "sys_gender";

    /** 切口愈合等级 */
    public static final String INCISION_LEVEL = "his_incision_level";

    /** 目标性监测类型 */
    public static final String INFECTION_MONITOR_TYPE = "his_infection_monitor_type";

    /** 住院医嘱来源 */
    public static final String INPATIENT_ORDER_SOURCE = "his_inpatient_order_source";

    /** 检查申请状态 */
    public static final String INSPECTION_APPLY_STATUS = "his_inspection_apply_status";

    /** 检查记录状态 */
    public static final String INSPECTION_RECORD_STATUS = "his_inspection_record_status";

    /** 室间质评比对结论 */
    public static final String LIS_EQA_COMPARE_STATUS = "his_lis_eqa_compare_status";

    /** 室间质评判定口径 */
    public static final String LIS_EQA_JUDGE_MODE = "his_lis_eqa_judge_mode";

    /** 室间质评批次状态 */
    public static final String LIS_EQA_PLAN_STATUS = "his_lis_eqa_plan_status";

    /** 室间质评判定结果 */
    public static final String LIS_EQA_RESULT_STATUS = "his_lis_eqa_result_status";

    /** 室间质评盲样状态 */
    public static final String LIS_EQA_SAMPLE_STATUS = "his_lis_eqa_sample_status";

    /** 失控处理状态 */
    public static final String LIS_QC_HANDLE_STATUS = "his_lis_qc_handle_status";

    /** 质控水平 */
    public static final String LIS_QC_LEVEL = "his_lis_qc_level";

    /** 质控结果状态 */
    public static final String LIS_QC_STATUS = "his_lis_qc_status";

    /** 登录状态 */
    public static final String LOGIN_STATUS = "his_login_status";

    /** 婚姻状况 */
    public static final String MARITAL_STATUS = "his_marital_status";

    /** 护理级别 */
    public static final String NURSING_LEVEL = "his_nursing_level";

    /** 护理班次 */
    public static final String NURSING_SHIFT = "his_nursing_shift";

    /** 手术级别 */
    public static final String OPERATION_LEVEL = "his_operation_level";

    /** 操作日志业务类型 */
    public static final String OPER_BUSINESS_TYPE = "his_oper_business_type";

    /** 操作日志状态 */
    public static final String OPER_STATUS = "his_oper_status";

    /** 医嘱执行状态 */
    public static final String ORDER_EXEC_STATUS = "his_order_exec_status";

    /** 用药频次 */
    public static final String ORDER_FREQ = "his_order_freq";

    /** 给药途径 */
    public static final String ORDER_ROUTE = "his_order_route";

    /** 病理蜡块状态 */
    public static final String PATHOLOGY_BLOCK_STATUS = "his_pathology_block_status";

    /** 病理检查类型 */
    public static final String PATHOLOGY_EXAM_TYPE = "his_pathology_exam_type";

    /** 病理状态 */
    public static final String PATHOLOGY_STATUS = "his_pathology_status";

    /** 与患者关系 */
    public static final String PATIENT_RELATION = "sys_patient_relation";

    /** 患者类型 */
    public static final String PATIENT_TYPE = "his_patient_type";

    /** 报告阴阳性 */
    public static final String POSITIVE_FLAG = "his_positive_flag";

    /** 预交金流水类型 */
    public static final String PREPAY_TYPE = "his_prepay_type";

    /** 采购审批状态 */
    public static final String PURCHASE_APPROVAL_STATUS = "his_purchase_approval_status";

    /** 质控检查结果 */
    public static final String QC_RESULT = "his_qc_result";

    /** 质控状态 */
    public static final String QC_STATUS = "his_qc_status";

    /** 转诊方向 */
    public static final String REFERRAL_DIRECTION = "his_referral_direction";

    /** 转诊状态 */
    public static final String REFERRAL_STATUS = "his_referral_status";

    /** 报告状态 */
    public static final String REPORT_STATUS = "his_report_status";

    /** 病案上报台账状态 */
    public static final String STAT_REPORT_STATUS = "his_stat_report_status";

    /** 病案统计上报类型 */
    public static final String STAT_REPORT_TYPE = "his_stat_report_type";

    /** 技术授权方式 */
    public static final String TECH_AUTH_TYPE = "his_tech_auth_type";

    /** 治疗申请状态 */
    public static final String TREATMENT_APPLY_STATUS = "his_treatment_apply_status";

    /** 治疗按次计费状态 */
    public static final String TREATMENT_CHARGE_STATUS = "his_treatment_charge_status";

    /** 治疗执行状态 */
    public static final String TREATMENT_EXEC_STATUS = "his_treatment_exec_status";

    /** 治疗项目类型 */
    public static final String TREATMENT_ITEM_TYPE = "his_treatment_item_type";

    /** 治疗记录状态 */
    public static final String TREATMENT_RECORD_STATUS = "his_treatment_record_status";

    /** 超声类型 */
    public static final String ULTRASOUND_TYPE = "his_ultrasound_type";

    public static final String VTE_BASIS = "his_vte_basis";

    public static final String VTE_OUTCOME = "his_vte_outcome";

    /** 医疗废物类别 */
    public static final String WASTE_TYPE = "his_waste_type";
}
