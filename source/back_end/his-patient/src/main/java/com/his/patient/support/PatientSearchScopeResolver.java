package com.his.patient.support;

import com.his.common.util.TextUtil;
import com.his.system.utils.UserUtils;
import com.his.system.entity.CurrentUser;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 「这个岗位要不要看今日就诊」的唯一判定点。
 */
@Component
public class PatientSearchScopeResolver {

    /**
     * 有门诊今日业务的角色编码（见类注释；口径 = 「岗位有没有门诊今日业务」）
     */
    private static final Set<String> OUTPATIENT_TODAY_ROLES = Set.of("10013", "10018", "10019", "10020");

    /**
     * 解析当前登录用户的搜索模式。
     *
     * <p>未登录 / 没有角色时返回 {@link PatientSearchScopeMode#ARCHIVE_ONLY}：
     * 没有身份就谈不上「我的今日患者」，「我的」那一层权重（本人/本科室）本来也算不出来。
     * 这种请求通常会被鉴权挡在前面，这里只是保证不 NPE、也不假装能个性化。
     */
    public PatientSearchScopeMode resolve() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            return PatientSearchScopeMode.ARCHIVE_ONLY;
        }
        String currentRole = user.getCurrentRole();
        if (!TextUtil.hasText(currentRole)) {
            return PatientSearchScopeMode.ARCHIVE_ONLY;
        }
        return OUTPATIENT_TODAY_ROLES.contains(currentRole)
                ? PatientSearchScopeMode.TODAY_FIRST
                : PatientSearchScopeMode.ARCHIVE_ONLY;
    }

    /**
     * 当前用户是不是门诊岗位（供调用方按需使用，与 {@link #resolve()} 同一份白名单）
     */
    public boolean isOutpatientRole() {
        return resolve() == PatientSearchScopeMode.TODAY_FIRST;
    }
}
