package com.his.common.constant;

/**
 * 系统参数配置 key 的集中常量（覆盖 sys_config、sign_config 等业务参数表的 config_key）。
 * <p>
 * 原先各业务模块把 config_key 写成各自的 {@code private static final String}，散落各处且容易拼错，
 * 拼错时 {@code sysConfigMapper} 按 key 查不到配置会静默走兜底逻辑，不报错也看不出原因。
 * 所有 sys_config 的 key 统一收口到本类，业务侧只引用常量，不直接写字面量。
 */
public interface SystemConfigKeyConst {

    // ===== 急诊 =====

    /**
     * 急诊候诊超时催办的兜底接收人（配置值可以是用户名，也可以直接是员工ID）。
     * 收件人阶梯走到末级（医生 → 当班 → 科主任 → 总值班）仍无人时落到此处。
     */
    public static final String EMERGENCY_WAIT_FALLBACK_RECEIVER = "emergency.wait_fallback_receiver";

    /**
     * 急诊留观预警时长（小时）。
     */
    public static final String EMERGENCY_OBSERVATION_WARN_HOURS = "emergency.observation_warn_hours";

    /**
     * 急诊留观最大时长（小时），超过即超期。
     */
    public static final String EMERGENCY_OBSERVATION_MAX_HOURS = "emergency.observation_max_hours";

    /**
     * 急诊候诊分级催办时限的 key 前缀（如 emergency.wait_deadline_level1）。
     * 注意这是前缀，配合 {@code likeRight} / {@code startsWith} 使用，不是完整 key。
     */
    public static final String EMERGENCY_WAIT_DEADLINE_LEVEL_PREFIX = "emergency.wait_deadline_level";

    // ===== 检验危急值 =====

    /**
     * 危急值处理时限（分钟）。
     */
    public static final String LAB_CRITICAL_VALUE_DEADLINE_MINUTES = "lab.critical_value_deadline_minutes";

    /**
     * 危急值无人接收时的兜底接收人（配置值可以是用户名，也可以直接是员工ID）。
     */
    public static final String LAB_CRITICAL_VALUE_FALLBACK_RECEIVER = "lab.critical_value_fallback_receiver";

    // ===== 住院 / 床位 / 转诊 / 住院证 =====

    /**
     * 住院请假时长上限（小时）。
     */
    public static final String INPATIENT_LEAVE_MAX_HOURS = "inpatient.leave.max_hours";

    /**
     * 床位协调：待床等待预警时长（小时）。
     */
    public static final String DUTY_COORD_BED_WAIT_HOURS = "duty.coord.bed_wait_hours";

    /**
     * 床位协调：转院/转诊待处理预警时长（小时）。
     */
    public static final String DUTY_COORD_REFERRAL_PENDING_HOURS = "duty.coord.referral_pending_hours";

    /**
     * 床位最长可等待天数（超过按超期处理）。
     */
    public static final String BED_WAIT_MAX_DAYS = "bed.wait.max_days";

    /**
     * 住院证有效期（天）。
     */
    public static final String ADMISSION_ORDER_VALID_DAYS = "admission_order.valid_days";

    // ===== 电子签名 / 可信时间戳（sign_config 表） =====

    /**
     * 签名时间来源：1=本机时钟 2=院内授时 3=第三方 TSA。
     * <p>注意：这是 sign_config 表的 key（非 sys_config），但同样收口到本类统一管理。
     */
    public static final String SIGN_TIME_SOURCE = "sign.time_source";

    /**
     * 签名证书有效期（天）。
     */
    public static final String SIGN_CERT_VALID_DAYS = "sign.cert.valid_days";

    /**
     * 证书自动签发开关（true/false 或 1/0）。
     */
    public static final String SIGN_CERT_AUTO_ISSUE = "sign.cert.auto_issue";

    // ===== 医院基础信息（sys_config 表） =====

    /**
     * 医院名称。
     */
    public static final String HOSPITAL_NAME = "hospital.name";

    /**
     * 医院地址。
     */
    public static final String HOSPITAL_ADDRESS = "hospital.address";

    /**
     * 医院联系电话。
     */
    public static final String HOSPITAL_PHONE = "hospital.phone";

    /**
     * 医院邮箱。
     */
    public static final String HOSPITAL_EMAIL = "hospital.email";
}
