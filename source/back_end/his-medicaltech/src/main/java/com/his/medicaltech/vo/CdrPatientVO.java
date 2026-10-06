package com.his.medicaltech.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * CDR 顶部的**患者身份卡**：主档信息 + EMPI 归并情况。
 *
 * <p>为什么要带归并信息：查的是主档，时间轴里却会混进影子档案的历史数据。
 * 不把"本页含 2 份档案的数据"写在脸上，看的人会以为数据串了。
 */
@Data
@Schema(description = "CDR 患者身份卡")
public class CdrPatientVO {

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "患者ID")
    private Long patientId;

    /** 患者编号 */
    @Schema(description = "患者号")
    private String patientNo;

    /** 患者姓名 */
    @Schema(description = "姓名")
    private String patientName;

    @Schema(description = "性别文案")
    private String genderText;

    /** 年龄 */
    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "出生日期")
    private String birthDate;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "家庭住址")
    private String address;

    /** 血型 */
    @Schema(description = "血型")
    private String bloodType;

    @Schema(description = "医保类型（原样取值，库里两种写法并存）")
    private String medicalInsuranceType;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "主档ID（为空=自己就是主档）")
    private Long masterId;

    @Schema(description = "主索引状态：0-正常 1-已并入主档")
    private Integer mergeStatus;

    @Schema(description = "主索引状态文案")
    private String mergeStatusText;

    @Schema(description = "本次查询归并进来的档案数（含自己）")
    private Integer resolvedArchiveCount;

    @Schema(description = "归并进来的其他档案（影子）")
    private List<CdrArchiveVO> shadowArchives;

    @Schema(description = "关键字段完整度（%）")
    private Integer completeRate;

    @Schema(description = "缺失的关键字段中文名")
    private List<String> missingFields;
}
