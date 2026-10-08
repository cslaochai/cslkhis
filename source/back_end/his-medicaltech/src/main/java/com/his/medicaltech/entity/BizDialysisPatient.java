package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 透析患者档案。
 *
 * <p>患者级唯一（uk patient_id+del_flag）：一个人只能有一份透析档案，
 * 换中心/退出后重新入透走「重新启用」而不是新建第二份。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_dialysis_patient")
public class BizDialysisPatient extends BaseEntity implements Serializable {

    /**
     * 透析号（DP + yyyyMMdd + 4 位）
     */
    private String dialysisNo;

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
     * 联系电话快照：展示接口出参必须脱敏，编辑回显走 getById 保持明文
     */
    private String phone;

    /**
     * 首次透析日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate firstDialysisDate;

    /**
     * 原发病/进入透析原因
     */
    private String cause;

    /**
     * 血管通路（1-自体内瘘 2-人工血管 3-中心静脉导管 4-动静脉外露）
     */
    private Integer accessType;

    /**
     * 通路部位
     */
    private String accessSite;

    /**
     * 透析频次（1-每周1次 2-每周2次 3-每周3次 4-每周≥4次）
     */
    private Integer dialysisFreq;

    /**
     * 档案状态（1-在透 2-暂停 3-退出）
     */
    private Integer status;

    /**
     * 暂停/退出原因（转腹透/移植/死亡/失访等，截到 200）
     */
    private String exitReason;
}
