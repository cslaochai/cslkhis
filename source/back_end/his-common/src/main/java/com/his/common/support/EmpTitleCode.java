package com.his.common.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 员工职称 / 职位的字典码（存在字典数据里，字典类型职称字典 / hospital_position）。
 *
 * <p><b>为什么要有这个类</b>：sql/174 之前，职称（员工表职称列）存的是中文自由文本，
 * 「找同科室的上级」这类判定就写成了 likeRight(title, "主任") 这种前缀匹配 ——
 * 一旦职称改成字典码（174 之后的唯一口径），这行 SQL 会**静默永远查不到人**：
 * 不报错、不抛异常，只是危急值超时升级和急诊候诊催办从此不再通知科主任。
 * 判定必须改成按码值，且码值口径要单点定义，不能各模块各抄一份。
 *
 * <p><b>职称号段</b>（字典类型职称字典）：1xx 初级 / 2xx 中级 / 3xx 副高 / 4xx 正高（医、药、护、技四系），
 * 501+ 为 174 新增的非卫技系列（科员、工程师、会计师、营养师、编码员…）。
 * 所以「副高及以上」= 首位为 3 或 4，且不含 5xx —— 号段本身就是层级，别再拿文案去匹配。
 *
 * <p><b>不要在这里加「按中文标签判定」的方法</b>：字典文案是会改的
 * （「主治（主管）医师」随时可能被改成「主治医师」），按文案判定等于把业务挂在可编辑的配置上。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmpTitleCode {

    /**
     * 正高：401 主任医师 / 402 主任药师 / 403 主任护师 / 404 主任技师
     */
    public static final List<String> FULL_SENIOR =
            Collections.unmodifiableList(Arrays.asList("401", "402", "403", "404"));
    /**
     * 副高：301 副主任医师 / 302 副主任药师 / 303 副主任护师 / 304 副主任技师
     */
    public static final List<String> ASSOCIATE_SENIOR =
            Collections.unmodifiableList(Arrays.asList("301", "302", "303", "304"));
    /**
     * 副高及以上（301~304 + 401~404）——「同科室上级」的判定口径。
     *
     * <p>注意：这条路原本是 likeRight(title, "主任")，只能命中正高，
     * 「副主任医师」因为不以「主任」开头被漏掉了 —— 而急诊、多数临床科室的负责人恰恰是副高。
     * 174 顺手把口径订正为副高及以上，升级通知才不会形同虚设。
     */
    public static final List<String> SENIOR =
            Collections.unmodifiableList(Arrays.asList(
                    "301", "302", "303", "304", "401", "402", "403", "404"));

    /**
     * 职称码是否为副高及以上（3xx/4xx）。null / 空 / 非卫技系列（5xx 及以上）一律 false。
     */
    public static boolean isSenior(String titleCode) {
        if (titleCode == null || titleCode.isBlank()) {
            return false;
        }
        return SENIOR.contains(titleCode.trim());
    }
}
