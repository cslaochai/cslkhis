package com.his.common.enums;

/**
 * 医保类型枚举
 * <p>settlementType 与医保政策配置的结算方式保持一致：
 * 2-城镇职工医保 3-城乡居民医保 4-公费医疗</p>
 */
public enum MedicalInsuranceTypeEnum {

    URBAN_EMPLOYEE("城镇职工医保", 2),
    URBAN_RESIDENT("城乡居民医保", 3),
    FREE_MEDICAL("公费医疗", 4),
    COMMERCIAL("商业医疗保险", 0);

    private final String name;
    /**
     * 对应的医保政策结算类型（0 表示暂无对应政策，视为全额自付）
     */
    private final Integer settlementType;

    MedicalInsuranceTypeEnum(String name, Integer settlementType) {
        this.name = name;
        this.settlementType = settlementType;
    }

    /**
     * 解析患者档案中的医保类型（历史数据存在多种叫法，统一归一到标准类型）
     * <p>如「城镇职工基本医疗保险」「在职职工」→ 城镇职工医保；
     * 「城镇居民基本医疗保险」「新型农村合作医疗」「城乡居民」→ 城乡居民医保</p>
     *
     * @param patientInsuranceType 患者档案中的医保类型原文
     * @return 归一后的枚举，无法识别返回 null
     */
    public static MedicalInsuranceTypeEnum parse(String patientInsuranceType) {
        if (patientInsuranceType == null || patientInsuranceType.trim().isEmpty()) {
            return null;
        }
        String type = patientInsuranceType.trim();
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
