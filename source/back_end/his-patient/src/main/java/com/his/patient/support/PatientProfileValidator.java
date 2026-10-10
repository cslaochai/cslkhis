package com.his.patient.support;

import com.his.common.util.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.patient.entity.BizPatient;

import java.time.LocalDate;
import java.util.Set;

/**
 * 患者主档建档 / 修改的写入口校验。
 */
public final class PatientProfileValidator {

    /**
     * 性别合法码值：1-男 2-女 9-未知（性别字典口径，与员工性别同套）
     */
    public static final Set<Integer> GENDER_CODES = Set.of(1, 2, 9);

    /**
     * 性别码值说明（拼进报错消息里，省得调用方去翻文档）
     */
    public static final String GENDER_HINT = "1-男 2-女 9-未知";

    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CODES = "10X98765432".toCharArray();

    /**
     * 主档建档（患者管理页「新增患者」/ 挂号页「新增患者」）。
     * 姓名、性别、身份证必填；身份证要过校验位。
     */
    public static void validateForCreate(BizPatient p) {
        if (p == null) {
            throw new BusinessException("患者信息不能为空");
        }
        requireName(p);
        requireGender(p);
        if (!TextUtil.hasText(p.getIdCard())) {
            throw new BusinessException("身份证号不能为空");
        }
        String idCard = p.getIdCard().trim();
        if (!isFormatLegalIdCard(idCard)) {
            throw new BusinessException("身份证号格式不正确：应为 18 位（前 17 位数字，末位为数字或 X）");
        }
        if (!isBirthDateLegalInIdCard(idCard)) {
            throw new BusinessException("身份证号中的出生日期不存在，请核对");
        }
        if (!isChecksumLegalIdCard(idCard)) {
            throw new BusinessException("身份证号校验位不正确，请核对后重新输入");
        }
        checkPhoneIfPresent(p);
    }

    /**
     * 主档修改。身份证在此放宽为「只验 18 位格式」——存量库里 32 条身份证是造数时编的，
     * 校验位不成立；若按新增的强度卡，这些老档连改个电话都保存不了。
     * 真要治理这些假号，走数据质量规则的 {@code IDCARD-FORMAT-INVALID}，而不是把写入口堵死。
     */
    public static void validateForUpdate(BizPatient p) {
        if (p == null) {
            throw new BusinessException("患者信息不能为空");
        }
        requireName(p);
        requireGender(p);
        if (!TextUtil.hasText(p.getIdCard())) {
            throw new BusinessException("身份证号不能为空");
        }
        if (!isFormatLegalIdCard(p.getIdCard().trim())) {
            throw new BusinessException("身份证号格式不正确：应为 18 位（前 17 位数字，末位为数字或 X）");
        }
        checkPhoneIfPresent(p);
    }

    /**
     * 急诊建档。急诊会遇到无名、无证、无手机号的三无患者 —— 此时硬要身份证
     * 只会把人挡在门外（或逼出乱编号）。但姓名和性别不能省：检验参考区间、
     * 性别专属诊断都依赖它，所以这两项仍然必填。
     */
    public static void validateForEmergency(BizPatient p) {
        if (p == null) {
            throw new BusinessException("患者信息不能为空");
        }
        requireName(p);
        requireGender(p);
        checkPhoneIfPresent(p);
        if (TextUtil.hasText(p.getIdCard()) && !isFormatLegalIdCard(p.getIdCard().trim())) {
            throw new BusinessException("身份证号格式不正确：应为 18 位（前 17 位数字，末位为数字或 X）");
        }
    }

    private static void requireName(BizPatient p) {
        if (!TextUtil.hasText(p.getPatientName())) {
            throw new BusinessException("患者姓名不能为空");
        }
    }

    private static void requireGender(BizPatient p) {
        if (p.getGender() == null) {
            throw new BusinessException("性别不能为空（" + GENDER_HINT + "）");
        }
        if (!GENDER_CODES.contains(p.getGender())) {
            throw new BusinessException("性别取值不合法：" + p.getGender() + "（仅支持 " + GENDER_HINT + "）");
        }
    }

    private static void checkPhoneIfPresent(BizPatient p) {
        if (TextUtil.hasText(p.getPhone()) && !isLegalPhone(p.getPhone().trim())) {
            throw new BusinessException("手机号格式不正确：应为 11 位手机号（1 开头）");
        }
    }

    /**
     * 18 位格式：前 17 位数字，末位数字或 X/x
     */
    public static boolean isFormatLegalIdCard(String idCard) {
        return idCard != null && idCard.matches("^[0-9]{17}[0-9Xx]$");
    }

    /**
     * 身份证第 7-14 位的出生日期是否真实存在。
     * 单独成一个判断，是为了报错能指到点子上 —— 否则「19990230」这种号只会被告知
     * "校验位不正确"，录入的人拿着真证件也看不出哪里错了。
     */
    public static boolean isBirthDateLegalInIdCard(String idCard) {
        if (!isFormatLegalIdCard(idCard)) {
            return false;
        }
        try {
            LocalDate.parse(idCard.substring(6, 14), DateFormats.STRICT_COMPACT_DATE);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * GB 11643 校验位。
     * 只用来卡新增，不用于修改（见 {@link #validateForUpdate} 的说明）。
     */
    public static boolean isChecksumLegalIdCard(String idCard) {
        if (!isFormatLegalIdCard(idCard)) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (idCard.charAt(i) - '0') * WEIGHTS[i];
        }
        return CHECK_CODES[sum % 11] == Character.toUpperCase(idCard.charAt(17));
    }

    /**
     * 11 位手机号（1 开头，第二位 3-9）
     */
    public static boolean isLegalPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    /**
     * 从身份证号里取出出生日期（格式不合法、或日期不存在时返回 null）。
     *
     * <p>建档时身份证是必填、出生日期是选填 —— 不带出来的话，新建档案会出现两个问题：
     * 列表上的年龄/出生日期是空的（年龄由 birth_date 算），而且 EMPI 的分级匹配里
     * 「同名 + 同性别 + 同出生日期 = L2」这一档永远命不中，重复档案只能靠姓名+手机号发现。
     * 身份证是权威来源，用它把 birth_date 补齐。
     */
    public static LocalDate birthDateOfIdCard(String idCard) {
        if (idCard == null) {
            return null;
        }
        String s = idCard.trim();
        if (!isBirthDateLegalInIdCard(s)) {
            return null;
        }
        try {
            return LocalDate.parse(s.substring(6, 14), DateFormats.STRICT_COMPACT_DATE);
        } catch (Exception e) {
            return null;
        }
    }
}
