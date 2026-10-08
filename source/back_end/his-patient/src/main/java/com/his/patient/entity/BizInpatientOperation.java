package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 住院手术操作明细（病案首页手术明细）—— 病案首页的手术侧
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inpatient_operation")
public class BizInpatientOperation extends BaseEntity {

    /**
     * 入院ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 来源手术申请单ID（手术申请单的ID）。
     *
     * <p><b>非空 = 手术闭环系统回写</b>，病案首页表单不得删除或覆盖这一行；
     * 为空 = 表单手工录入，随便改。这一列是"系统回写行"的唯一可信标记 ——
     * 不靠 remark 文本前缀判断，文本会被改、会被人抄，列不会。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 序号（主要手术固定为 1）
     */
    private Integer seqNo;

    /**
     * 是否主要手术（0-否 1-是）
     */
    private Integer isMain;

    /**
     * 手术操作编码（ICD-9-CM-3）
     */
    private String operationCode;

    /**
     * 手术操作名称
     */
    private String operationName;

    /**
     * 手术日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationDate;

    /**
     * 手术级别（1-一级 2-二级 3-三级 4-四级）
     */
    private Integer operationLevel;

    /**
     * 切口等级（0-0类 1-Ⅰ类 2-Ⅱ类 3-Ⅲ类）
     */
    private Integer incisionLevel;

    /**
     * 麻醉方式（1-全麻 2-椎管内 3-神经阻滞 4-局麻 5-其他）
     */
    private Integer anesthesiaType;

    /**
     * 主刀医师ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long surgeonId;

    /**
     * 主刀医师姓名
     */
    private String surgeonName;

    /**
     * 助手姓名（多人逗号分隔）
     */
    private String assistantName;

    /**
     * 手术依据（手术记录中的支持性描述）
     */
    private String operationBasis;
}
