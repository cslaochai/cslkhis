package com.his.supplies.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * CSSD 器械包模板目录实体（99 号脚本新增）。
 *
 * <p>回收登记的器械包名称从这里选，选定后带出默认灭菌方式与组成清单。
 * template_code 仅普通索引，启用行内唯一由服务层校验（软删行不挡重号）。
 */
@Data
@TableName("biz_cssd_pack_template")
public class BizCssdPackTemplate {

    /** 器械包模板ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 包编码 */
    private String templateCode;
    /** 器械包名称 */
    private String packName;

    /** 默认灭菌方式（1-高压蒸汽 2-环氧乙烷 3-低温等离子） */
    private Integer sterilizeMethod;

    /** 状态（1-启用 0-停用） */
    private Integer status;

    /** 备注 */
    private String remark;
    /** 创建人 */
    private String createBy;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新人 */
    private String updateBy;
    /** 更新时间 */
    private LocalDateTime updateTime;
    /** 删除标志（0-正常 1-删除） */
    private Integer delFlag;
}
