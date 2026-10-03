package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统参数配置
 * <p>
 * 注意：系统参数表没有 create_time / update_time / del_flag 列，
 * 因此不能继承 BaseEntity（否则逻辑删除会拼出 del_flag = 0 导致 SQL 报错）。
 */
@Data
@TableName("sys_config")
public class SysConfig implements Serializable {

    /**
     * 配置ID
     */
    @TableId(value = "config_id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long configId;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 类型：0-系统 1-业务
     */
    private Integer configType;

    /**
     * 是否系统内置：0-否 1-是
     */
    private Integer isSystem;

    /**
     * 备注
     */
    private String remark;
}
