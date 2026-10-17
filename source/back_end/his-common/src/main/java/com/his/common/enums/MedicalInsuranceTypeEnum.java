package com.his.common.enums;

/**
 * 医保类型枚举
 */
public enum MedicalInsuranceTypeEnum {

    /** 自费：无医保，全额自付（政策险种键 1，findPolicy 对 1 直接返回无政策） */
    SELF_PAY("自费", 1),
    URBAN_EMPLOYEE("城镇职工医保", 2),
    URBAN_RESIDENT("城乡居民医保", 3),
    FREE_MEDICAL("公费医疗", 4),
    /** 商业医疗保险：不走医保统筹，暂无对应政策，视为全额自付 */
    COMMERCIAL("商业医疗保险", 0);

    private final String name;
    /**
     * 对应的医保政策结算类型（费别/险种键，与医保政策档案里的结算类型对齐；
     * 0 表示暂无对应政策，视为全额自付）
     */
    private final Integer settlementType;

    MedicalInsuranceTypeEnum(String name, Integer settlementType) {
        this.name = name;
        this.settlementType = settlementType;
    }

    /**
     * 解析患者档案中的费别（参保类型），历史数据存在多种叫法，统一归一到标准类型。
     * <p>「自费」→ 自费；「城镇职工基本医疗保险」「在职职工」→ 城镇职工医保；
     * 「城镇居民基本医疗保险」「新型农村合作医疗」「城乡居民」→ 城乡居民医保；
     * 「公费」→ 公费医疗；「商业」→ 商业医疗保险。</p>
     *
     * @param patientInsuranceType 患者档案中的费别原文
     * @return 归一后的枚举；空串或无法识别返回 null（调用方按无医保/自费处理）
     */
    public static MedicalInsuranceTypeEnum fromCode(String patientInsuranceType) {
        if (patientInsuranceType == null || patientInsuranceType.trim().isEmpty()) {
            return null;
        }
        String type = patientInsuranceType.trim();
        if (type.contains("自费")) {
            return SELF_PAY;
        }
        if (type.contains("公费")) {
            return FREE_MEDICAL;
        }
        if (type.contains("商业")) {
            return COMMERCIAL;
        }
        if (type.contains("职工")) {
            return URBAN_EMPLOYEE;
        }
        if (type.contains("居民") || type.contains("农村") || type.contains("新农合")) {
            return URBAN_RESIDENT;
        }
        return null;
    }

    /**
     * 是否为可走医保统筹的费别（职工/居民/公费；自费、商业保险、未知均不进统筹）
     */
    public boolean isPoolCovered() {
        return settlementType != null && settlementType >= 2 && settlementType <= 4;
    }

    /**
     * 根据医保类型名称获取枚举
     */
    public static MedicalInsuranceTypeEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (MedicalInsuranceTypeEnum item : values()) {
            if (item.name.equals(name)) {
                return item;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public Integer getSettlementType() {
        return settlementType;
    }
}
