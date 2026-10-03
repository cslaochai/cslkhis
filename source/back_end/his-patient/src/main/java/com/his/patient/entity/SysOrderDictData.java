package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医嘱基础字典行（给药途径 / 用药频次 / 剂量单位，落在通用字典表字典数据，sql/142）。
 *
 * <p><b>为什么 his-patient 自己有一份字典数据的实体</b>：
 * 医嘱字典的写权限是 {@code ipd:orderDict:*}（挂在住院业务菜单下，科室自己能维护途径/频次），
 * 而通用字典接口 {@code /system/dict/dataUpsert} 要的是 {@code system:dict:add} ——
 * 为了能在这页保存就把系统字典写权限发给医生，等于把全院所有字典的改写权一起交出去。
 * 所以这里用一张只认三种 dict_type 的窄口实体自己写库，写完主动刷 {@code DictCacheService} 的缓存，
 * 既不扩散权限，也不让医生站下拉读到 24 小时前的旧值。
 *
 * <p><b>字段与表完全对齐</b>（多一列就会让全表 select 变 500）：只映射字典数据真实存在的列。
 */
@Data
@TableName("sys_dict_data")
public class SysOrderDictData implements Serializable {

    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 字典类型 */
    private String dictType;

    /** 字典标签 */
    private String dictLabel;

    /** 字典值 */
    private String dictValue;

    /** 排序号 */
    private Integer dictSort;

    /** 表格回显样式 */
    private String listClass;

    /** 是否默认（0-否 1-是） */
    private Integer isDefault;

    /**
     * 1-启用 0-停用（停用后下拉不再出现，但历史医嘱里的值仍能渲染出文案）
     */
    private Integer status;

    /** 备注 */
    private String remark;

    /**
     * 1-系统内置 2-自定义（医嘱字典页新增的都算自定义）
     */
    private Integer dictSource;

    /** 创建人 */
    private String createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 删除标志（0-正常 1-删除） */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;
}
