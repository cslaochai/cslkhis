package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 护理质量检查单（护理质量检查单，sql/168）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_nursing_qc_check")
public class BizNursingQcCheck extends BaseEntity {

    /**
     * 检查单号 QC+yyyyMM+病区序号+类别
     */
    private String checkNo;
    /**
     * 病区ID
     */
    private Long wardId;
    /**
     * 病区名称
     */
    private String wardName;
    /**
     * 病区所属科室：数据范围（岗位可见科室）按它收口
     */
    private Long deptId;
    /**
     * 科室名称
     */
    private String deptName;
    /**
     * 检查月份 yyyy-MM（CHAR(7) 列，不做日期类型，避免时区把月份漂走）
     */
    private String checkMonth;
    /**
     * 现场检查日期
     */
    private LocalDate checkDate;
    /**
     * NursingQcCategoryEnum：1-基础护理 2-专科护理 3-安全管理 4-护理文书 5-院感防控
     */
    private Integer category;
    /**
     * 检查人员工ID
     */
    private Long inspectorId;
    /**
     * 检查人姓名
     */
    private String inspectorName;

    /**
     * 抽查总例数
     */
    private Integer sampleCount;
    /**
     * 合格总例数
     */
    private Integer qualifiedCount;
    /**
     * 合格率%=合格例数/抽查例数*100
     */
    private BigDecimal qualifiedRate;
    /**
     * 应得分
     */
    private BigDecimal fullScore;
    /**
     * 实得分
     */
    private BigDecimal totalScore;
    /**
     * 得分率%=实得分/应得分*100
     */
    private BigDecimal scoreRate;
    /**
     * NursingQcStatusEnum：1-草稿 2-已确认
     */
    private Integer status;
    /**
     * 本轮小结
     */
    private String summary;
}
