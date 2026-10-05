package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 护理质量检查单（护理质量检查单，sql/168）。
 *
 * <p><b>一张单 = 病区 × 月份 × 检查类别</b>（{@code uk_check_ward_month_cat}）：
 * 护理部按「单周基础护理、双周专科、每月安全与文书」轮查，同一病区同一月同一类别只能有一张单，
 * 重复检查要么并进这张单，要么改期，不允许出现两张「同月同类」把合格率算成两个数。
 *
 * <p><b>主表六个数字全部由明细求和上来</b>（抽查例数/合格例数/合格率/应得分/实得分/得分率），
 * 没有任何手填口子。原先收费域踩过「汇总列与明细对不上」的坑，这里从写入侧就断掉：
 * 服务层每次保存都重算，读侧不再 JOIN。
 *
 * <p>病区/科室/检查人姓名是<b>快照列</b>：质控单是历史事实，护士离婚后改名、病区调整归属，
 * 回看上年的检查单必须还是当时的样子。
 *
 * <p>status=2-已确认之后明细冻结（要改先退回草稿），因为已确认的数会进台账与月度通报。
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
     * 病区名称（快照）
     */
    private String wardName;
    /**
     * 病区所属科室：数据范围（岗位可见科室）按它收口
     */
    private Long deptId;
    /**
     * 科室名称（快照）
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
     * 检查人姓名（快照）
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
