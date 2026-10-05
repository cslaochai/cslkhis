package com.his.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 字段级修改日志（字段级修改日志，建表见 sql/159）。
 *
 * <p><b>这张表回答的问题只有一个：谁把「哪个对象的哪个字段」从什么值改成了什么值。</b>
 * sql/158 的三本账回答的是"谁调了哪个接口 / 谁登录了 / 谁对哪个对象做了什么"，
 * 但审计日志的 content 是自由文本，翻不出 old→new —— 检查问"改之前是什么"只能翻 binlog。
 *
 * <p><b>一张表装所有对象</b>（靠 {@code bizType} 区分），不给每个对象复制一张 log 表：
 * 复制的后果就是仓库里那三张口径不一的遗产（病历 / 住院文书 / 患者合并各一套列），
 * 查一次问题要跳三个页面。现在统一在这张表里，一个页签按对象类型筛。
 *
 * <p><b>不建外键、不反向依赖业务模块</b>：只存 bizType + bizId + 名称快照。
 * 对象被删了，凭 bizName 还是认得出当年改的是谁 —— 这正是留痕表要的效果。
 *
 * <p><b>只读</b>：与三本账一样不提供删除接口（等保三级 8.1.4.3），归档走 DBA 按 change_time 转储。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_field_change_log")
public class SysFieldChangeLog extends BaseEntity {

    /**
     * 对象类型：PATIENT-患者 USER-系统用户 EMPLOYEE-员工 MEDICAL_RECORD-病历 INPATIENT_RECORD-住院文书
     */
    private String bizType;

    /**
     * 对象ID（字符串存，各对象主键类型不一）
     */
    private String bizId;

    /**
     * 对象编号快照（患者号/工号/病历号）
     */
    private String bizNo;

    /**
     * 对象名称快照（患者姓名/用户名）
     */
    private String bizName;

    /**
     * 字段英文名
     */
    private String fieldName;

    /**
     * 字段中文名（审计员看的是这个）
     */
    private String fieldLabel;

    /**
     * 变更前值（直接标识符已打码）
     */
    private String oldValue;

    /**
     * 变更后值（直接标识符已打码）
     */
    private String newValue;

    /**
     * 变更类型：INSERT-建档 UPDATE-修改 ACTION-操作留痕
     */
    private String changeType;

    /**
     * 批次号：同一次保存的多个字段共用一个
     */
    private String batchNo;

    /**
     * 操作人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作人科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 操作人科室名称
     */
    private String deptName;

    /**
     * 变更时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime changeTime;
}
