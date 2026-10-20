package com.his.spi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单张申请单的执行进度。
 */
@Data
@NoArgsConstructor
public class ExecStatusVO {
    /**
     * 申请单 ID
     */
    private Long applyId;

    /**
     * 执行记录 ID（检查记录 / 检验记录）
     */
    private Long execRecordId;

    /**
     * 执行记录状态码（检查：1已登记…7已取消；检验：1已登记…8已取消）
     */
    private Integer execStatus;

    /**
     * 面向医生的进度文案（「已缴费待执行」「检查中」「已出结果」…）。
     *
     * <p>由医技模块给出而不是让医生站按码值自己翻译 —— 检查与检验的
     * 记录状态是**两套不同的码表**（同一码值含义不同），
     * 让调用方各自翻译必然翻错。</p>
     */
    private String execStatusText;

    /**
     * 是否已开始执行（检查：已签到起；检验：已采样起）。
     * 用于判断申请单能否删除 / 能否按普通退费处理。
     */
    private Boolean started;

    /**
     * 是否有未作废的危急值记录。
     */
    private Boolean critical;
}