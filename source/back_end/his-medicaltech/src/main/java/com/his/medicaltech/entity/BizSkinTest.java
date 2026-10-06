package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 门诊皮试记录（M10）。
 *
 * <p>判读观察窗：判读时间距皮试时间不足 15 分钟拒绝判读（服务端校验）。
 * 皮试阳性 → 关联输液单取消，座位释放。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_skin_test")
public class BizSkinTest extends BaseEntity {

    /**
     * 皮试单号
     */
    private String testNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名（快照）
     */
    private String patientName;

    /**
     * 皮试药物名称
     */
    private String drugName;

    /**
     * 来源治疗记录ID（可空）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long treatmentRecordId;

    /**
     * 皮试时间（打皮试针）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime testTime;

    /**
     * 判读结果（0-待判读 1-阴性 2-阳性）
     */
    private Integer result;

    /**
     * 判读时间（观察窗 >= 15 分钟）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resultTime;

    /**
     * 执行护士ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long nurseId;

    /**
     * 执行护士姓名（快照）
     */
    private String nurseName;
}
