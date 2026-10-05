package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 死亡证明死因链明细（死亡证明死因链，sql/157）。
 *
 * <p>Ⅰ部分按 (a)(b)(c)(d) 顺序记「直接死因 → 中介原因 → 根本死因」，<b>链尾那行就是根本死因</b>；
 * {@code intervalText} 存该行「发病至死亡的时间间隔」（国标逐行必填，文本而非数值：填的是「30分钟」「10年」这类）。
 * Ⅱ部分记与死亡无因果但需登记的其他疾病。外部原因致死者链尾填 V~Y 编码。
 *
 * <p>本表<b>没有 del_flag</b>：唯一键 uk_cert_part_seq 不含 del_flag，软删行会占键，
 * 所以死因链整体替换必须走物理删（见 Mapper 的 purgeByCertId）。
 */
@Data
@TableName("biz_death_certificate_cause")
public class BizDeathCertificateCause implements Serializable {

    /**
     * 主键（雪花ID）
     */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 死亡证明ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long certId;

    /**
     * 部分（1-Ⅰ部分死因链 2-Ⅱ部分其他疾病）
     */
    private Integer part;

    /**
     * 行序：Ⅰ部分 1=a(直接死因) … 4=d(根本死因)，Ⅱ部分从 1 递增
     */
    private Integer seqNo;

    /**
     * ICD-10编码
     */
    private String icdCode;

    /**
     * 疾病或情况名称
     */
    private String icdName;

    /**
     * 发病至死亡间隔（文本）
     */
    private String intervalText;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
