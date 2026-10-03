package com.his.appoint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 预约看板查询入参 —— 一次性取整段区间（日视图 1 天 / 周视图 7 天）的**全部**挂号。
 *
 * <p>为什么不复用 {@link AppointQueryDTO}：那个是**分页**查询，挂号记录 / 收件箱在用；
 * 看板要的是「这段区间一共挂了哪些人」—— 每格要算「已挂 N / 总号源」、以及「还有 M 人 → 看当天」，
 * 拿到的不可能是某一页。按天拆 7 次分页查询会让请求数变成 7×页数；
 * 更要命的是任一分页被截断时看板只是「就这么几张」，**不报错**
 * （2026-09-21 已踩过：单日挂号 332 条、单页 200 条，132 条静默丢失）。
 *
 * <p>区间不设上限是刻意的：看板的语义就是「这段全量」。真到几千条时，
 * 正确的方向是给看板做服务端聚合（每格只回「总数 + 前 N」），而不是让前端回头去翻页。
 */
@Data
public class AppointBoardQueryDTO {

    /**
     * 查询开始日期（就诊日，含）
     */
    @NotNull(message = "查询开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 查询结束日期（就诊日，含）
     */
    @NotNull(message = "查询结束日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 科室ID（不传 = 全部科室）
     */
    private Long deptId;

    /**
     * 医生ID
     */
    private Long doctorId;
}
