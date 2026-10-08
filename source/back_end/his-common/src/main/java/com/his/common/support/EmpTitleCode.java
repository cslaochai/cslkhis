package com.his.common.support;

import com.his.common.util.TextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 员工职称 / 职位的字典码（存在字典数据里，字典类型职称字典 / hospital_position）。
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
        if (!TextUtil.hasText(titleCode)) {
            return false;
        }
        return SENIOR.contains(titleCode.trim());
    }
}
