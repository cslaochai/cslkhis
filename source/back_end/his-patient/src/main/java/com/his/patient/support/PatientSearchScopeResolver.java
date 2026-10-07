package com.his.patient.support;

import com.his.common.util.TextUtil;
import com.his.system.utils.UserUtils;
import com.his.system.entity.CurrentUser;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 「这个岗位要不要看今日就诊」的唯一判定点。
 *
 * <p>做的判断只有一句：**当前角色有没有门诊今日业务**。有 → 今日就诊置顶并分组；
 * 没有 → 纯全院档案。
 *
 * <h3>为什么判据是角色编码，而不是「是不是医生」</h3>
 * 门诊今日业务不等于「医生」：分诊护士、前台导诊都在处理今天这拨人，而医生里也有
 * 只做住院/只看历史的。所以这里按**具体岗位**列白名单，而不是按某一类角色名去模糊匹配。
 *
 * <h3>为什么身份必须服务端取</h3>
 * 模式由 {@link #resolve()} 直接读 {@link UserUtils#getCurrentUser()}，**接口不接受前端传参**。
 * 让前端传 scope 等于把「能看多少」交给调用方自己声明：任何登录用户拿 curl 加个
 * {@code scope=TODAY_FIRST} 就能改行为，而这种参数在鉴权缺失的环境里没人会去校验。
 *
 * <h3>白名单（18 个角色里只有 4 个，与「该角色的菜单里配了门诊业务」这批角色一致）</h3>
 * <ul>
 *   <li>{@code 10013} 医生 —— 门诊医生站，今天要接谁就是本职工作</li>
 *   <li>{@code 10018} 前台导诊 —— 挂号/导诊，天天在查「这人今天来了没」</li>
 *   <li>{@code 10019} 急诊医生 —— 绿色通道，只比门诊更需要「人现在在哪」</li>
 *   <li>{@code 10020} 分诊护士 —— 分诊工作站的全部工作对象就是今日候诊队列</li>
 * </ul>
 * 刻意**不在**白名单里的：系统管理员（10012，管理视角，不该被「就诊中」的标注误导）、
 * 护士/护士长（10014/10021，病区住院为主，门诊队列不是他们的工作对象）、
 * 收费员（10015，走收费窗口自己的单据流）、药剂师/医技/病案/质控/医保/审计/院领导（纯档案）。
 * 要调整就是改这一处 {@code Set}，不存在第二处判定。
 *
 * <h3>已知边界：新增角色默认不置顶</h3>
 * 白名单外的角色（含以后新加、这里还没登记的角色）一律走 {@link PatientSearchScopeMode#ARCHIVE_ONLY}，
 * 即「宁可少置顶，也不给非门诊岗位塞噪音」。代价是：如果新角色是门诊岗而忘了登记，
 * 它不会报错、只是搜索少了置顶与分组 —— 加角色时顺手看一眼这里。
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
