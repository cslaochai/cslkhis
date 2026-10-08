package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 护理评估单（G14：压疮 Braden / 跌倒 Morse / 疼痛 NRS）。
 */
@Data
@TableName("biz_nursing_assessment")
public class BizNursingAssessment {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 评估单号 AS+yyyyMMdd+4位
     */
    private String assessNo;

    /**
     * 入院ID（入院记录的入院ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者编号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 病区名称
     */
    private String wardName;

    /**
     * 床号
     */
    private String bedNo;

    /**
     * 评估类型（his_assess_type）:1-压疮Braden 2-跌倒Morse 3-疼痛NRS 4-VTE Caprini 5-管路滑脱
     */
    private Integer assessType;

    /**
     * 总分（Braden 6~23 / Morse 0~125 / NRS 0~10 / Caprini 0~55 / 管路滑脱 0~24）
     */
    private Integer totalScore;

    /**
     * 风险等级（后端按分数段算）:1-低风险 2-中风险 3-高风险 4-极高风险
     */
    private Integer riskLevel;

    /**
     * 评分明细 JSON（量表项:得分数组）
     */
    private String itemsJson;

    /**
     * 评估时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime assessTime;

    /**
     * 评估护士ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assessNurseId;

    /**
     * 评估护士姓名
     */
    private String assessNurseName;

    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 删除标志（0-正常 1-删除）
     */
    private Integer delFlag;
    /**
     * 备注
     */
    private String remark;
}
