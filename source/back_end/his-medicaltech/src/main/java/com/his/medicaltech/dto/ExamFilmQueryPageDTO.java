package com.his.medicaltech.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 胶片用量分页查询（sql/138）。
 */
@Data
public class ExamFilmQueryPageDTO {

    /** 页码 */
    @Min(value = 1, message = "页码从 1 开始")
    private Integer pageNum = 1;

    /** 每页条数 */
    @Min(value = 1, message = "每页条数至少 1")
    @Max(value = 200, message = "每页条数不能超过 200")
    private Integer pageSize = 10;

    /** 关键字：患者姓名 / 患者号 / 胶片单号 / 记录号 */
    private String keyword;

    /** 只看某一次检查的胶片 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /** 胶片状态（1-已登记 2-已打印 3-已发放 4-已作废） */
    private Integer filmStatus;

    /** 是否已记账（0-未记账 1-已记账） */
    private Integer chargeFlag;

    /** 起始日期（yyyy-MM-dd） */
    private String startDate;

    /** 截止日期（yyyy-MM-dd；服务端补 23:59:59） */
    private String endDate;
}
