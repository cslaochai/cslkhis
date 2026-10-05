package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 体检结果明细实体（体检结果明细，登记时按套餐项目预生成空行）。
 */
@Data
@TableName("biz_checkup_result")
public class BizCheckupResult {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 体检登记ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 项目类别（1-检验 2-检查 3-一般）
     */
    private Integer itemType;

    /**
     * 参考范围
     */
    private String refStandard;

    /**
     * 结果值/所见
     */
    private String resultValue;

    /**
     * 异常标志（0-正常 1-异常 2-待查）
     */
    private Integer abnormalFlag;

    /**
     * 单项小结/建议
     */
    private String summaryText;

    /**
     * 检查/检验医师
     */
    private String checkerName;

    /**
     * 结果录入时间
     */
    private LocalDateTime resultTime;

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
