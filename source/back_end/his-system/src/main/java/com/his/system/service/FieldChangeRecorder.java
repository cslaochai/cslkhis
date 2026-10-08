package com.his.system.service;

import com.his.system.support.FieldSpec;

import java.util.List;

/**
 * 字段级修改日志落库（字段级修改日志，sql/159）。
 */
public interface FieldChangeRecorder {

    /**
     * 变更类型：建档（只有新值）
     */
    String TYPE_INSERT = "INSERT";

    /**
     * 变更类型：修改（新旧都有）
     */
    String TYPE_UPDATE = "UPDATE";

    /**
     * 变更类型：操作留痕（没有字段级新旧值，只记"做了什么"）
     */
    String TYPE_ACTION = "ACTION";

    /**
     * 比对两个对象快照并落库。
     *
     * @param bizType 业务类型（PATIENT / EMPLOYEE …）
     * @param bizId   业务主键
     * @param bizNo   业务单号（可空）
     * @param bizName 业务名称（可空）
     * @param oldObj  变更前快照（建档传 null）
     * @param newObj  变更后快照（从库里重新查出来的真实值，不是 DTO）
     * @param specs   要记哪些字段，由调用方声明
     * @return 批次号；没有任何字段发生变化时返回 null（没有变化就不该产生日志行）
     */
    String record(String bizType, Object bizId, String bizNo, String bizName,
                  Object oldObj, Object newObj, List<FieldSpec> specs);

    /**
     * 记一条非字段级的操作留痕（创建 / 提交 / 归档 / 作废这类"对对象做了什么"）。
     *
     * <p>与 {@code SysAuditLogService} 的分工：那本账记"谁对哪个对象做了什么"，
     * 这本账记"哪个字段变了什么值"。两边都写不重复 —— 前者是人读的一句话，
     * 后者是可核对的结构化 diff，检查时各看各的。
     */
    void recordAction(String bizType, Object bizId, String bizNo, String bizName, String operation);
}
