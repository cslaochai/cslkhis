package com.his.pharmacy.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/** 抗菌药物别名行 */
@Data
public class AntibioticAliasVO {

    /** 主键 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 药品ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long drugId;

    /** 药品编码 */
    private String drugCode;

    /** 药品目录名 */
    private String drugName;

    /** 别名 */
    private String aliasName;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 备注 */
    private String remark;
}
