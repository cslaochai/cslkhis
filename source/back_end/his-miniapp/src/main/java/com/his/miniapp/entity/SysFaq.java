package com.his.miniapp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 患者端常见问题。
 *
 * <p><b>答案是人工维护的固定文本，不是模型生成。</b>理由很直接：医疗场景里
 * 模型现编一句「门诊 8:00 上班」就是事故，而且不会有人去核对 —— 它看起来很通顺。
 * 所以涉及时间、价格、报销比例的一律写成引导式（"以现场公示为准 / 请咨询窗口"），
 * 种子脚本 {@code workspace/_gen_faq_seed.mjs} 里有断言挡着，写死数字直接跑不过。
 *
 * <p>{@code keywords} 里带口语同义词（「约号」「化验单」），因为患者不会用医院术语提问。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_faq")
public class SysFaq extends BaseEntity {

    /** 常见问题编号 */
    private String faqNo;

    /** 分类编码 */
    private String categoryCode;

    /** 分类名称 */
    private String categoryName;

    /** 问题 */
    private String question;

    /** 答案 */
    private String answer;

    /** 检索关键词（顿号分隔，含口语同义词） */
    private String keywords;

    /** 热门（0-否 1-是） */
    private Integer hotFlag;

    /** 查看次数 */
    private Integer viewCount;

    /** 有帮助次数 */
    private Integer helpfulCount;

    /** 没帮助次数 */
    private Integer uselessCount;

    /** 状态（0-停用 1-启用） */
    private Integer status;

    /** 排序号 */
    private Integer sortOrder;
}
