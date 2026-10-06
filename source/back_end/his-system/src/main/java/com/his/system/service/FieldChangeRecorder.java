package com.his.system.service;

import com.his.system.support.FieldSpec;

import java.util.List;

/**
 * 字段级修改日志落库（字段级修改日志，sql/159）。
 *
 * <p><b>一句话职责：把"改之前是啥、改之后是啥"钉进表里。</b>
 * 三本账（sql/158）只回答谁调了接口，审计日志的 content 是自由文本拼出来的，
 * 都翻不出结构化的 old→new —— 这个服务就是补那一刀的。
 *
 * <p><b>用法（业务侧三步）：</b>
 * <pre>{@code
 *   Object old = service.getById(id);          // ① 改之前先取快照
 *   service.updateXxx(dto);                    // ② 正常跑业务
 *   Object now = service.getById(id);          // ③ 改完再取一次，diff 两个真实快照
 *   fieldChangeRecorder.record("PATIENT", id, no, name, old, now, PATIENT_FIELDS);
 * }</pre>
 * 第③步取"落库后的真实值"而不是拿 DTO 直接比，是有意的：DTO 里 null 的字段在
 * MyBatis-Plus 的 updateById 下<b>不会被写</b>，拿 DTO 比会把"没传"误记成"清空成空"；
 * 而且服务端的副作用（如按身份证补出生日期）只有落库后才看得见。
 *
 * <p><b>旁路写入：</b>留痕失败只记 error 日志，绝不让业务保存跟着失败 ——
 * 但也不静默吞，出问题时日志里能看出来（与 {@code SysAuditLogService} 同一口径）。
 *
 * <p><b>这是"日志写入端"不是"审计服务"：</b>不判断什么该记什么不该记，
 * 记哪些字段由调用方用 {@link FieldSpec} 声明 —— 判业务该不该留痕是业务自己的事。
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
