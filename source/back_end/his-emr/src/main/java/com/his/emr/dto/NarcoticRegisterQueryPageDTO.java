package com.his.emr.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 麻精药品专册分页查询入参
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NarcoticRegisterQueryPageDTO extends PageParam {

    /**
     * 关键字（模糊匹配：患者姓名 / 患者号 / 处方号 / 登记号 / 药品名称 / 批号）
     * <p>
     * 条件必须下推 SQL。前端"取本页再 filter"的做法在翻页后必然漏结果，
     * 而专册的价值恰恰是"能查出某一笔"——查不出就等于没有台账。
     */
    private String keyword;

    /**
     * 特殊管理分类（1-麻醉 2-第一类精神 3-第二类精神 4-毒性），为空不过滤
     */
    private Integer specialFlag;

    /**
     * 空安瓿回收状态（0-不适用 1-待回收 2-已回收），为空不过滤
     */
    private Integer ampouleStatus;

    /**
     * 发药日期-起（含）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispenseDateStart;

    /**
     * 发药日期-止（含）
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispenseDateEnd;

    /**
     * 患者ID
     */
    private Long patientId;
}
