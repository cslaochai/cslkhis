package com.his.operation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 手术器械/敷料清点主单（手术清点主单）—— 一台手术一份（UNIQUE apply_id）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_operation_count")
public class BizOperationCount extends BaseEntity {

    /**
     * 清点单号（QD + yyyyMMdd + 4位序号）
     */
    private String countNo;

    /**
     * 手术申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 手术申请单号
     */
    private String applyNo;

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 手术间
     */
    private String operationRoom;

    /**
     * 手术名称
     */
    private String plannedOperationName;

    /**
     * 器械（洗手）护士ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long instrumentNurseId;

    /**
     * 器械护士姓名
     */
    private String instrumentNurseName;

    /**
     * 巡回护士ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long circulateNurseId;

    /**
     * 巡回护士姓名
     */
    private String circulateNurseName;

    /**
     * 术前清点核对人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long beforeNurseId;

    /**
     * 术前清点核对人姓名
     */
    private String beforeNurseName;

    /**
     * 术前清点时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beforeTime;

    /**
     * 术前清点结果：1-一致 2-不一致（已核准基数）
     */
    private Integer beforeResult;

    /**
     * 关体前清点核对人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long closureNurseId;

    /**
     * 关体前核对人姓名
     */
    private String closureNurseName;

    /**
     * 关体前清点时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closureTime;

    /**
     * 关体前清点结果：1-一致 2-不一致（已处理）
     */
    private Integer closureResult;

    /**
     * 关体后清点核对人ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long finalNurseId;

    /**
     * 关体后核对人姓名
     */
    private String finalNurseName;

    /**
     * 关体后清点时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finalTime;

    /**
     * 关体后清点结果：1-一致 2-不一致（待处理）
     */
    private Integer finalResult;

    /**
     * 当前阶段（0-未开始 1-术前完成 2-关体前完成 3-关体后完成）
     */
    private Integer phase;

    /**
     * 状态（0-清点中 1-三轮一致完成 2-存在差异待处理 3-异常终止）
     */
    private Integer status;

    /**
     * 是否存在清点差异（0-无 1-有）
     */
    private Integer discrepancyFlag;

    /**
     * 差异说明与处理过程（差了什么、怎么处理、结论如何必须写清）
     */
    private String diffNote;
}
