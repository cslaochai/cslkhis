package com.his.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 申请状态枚举（适用于检验/检查申请单）。
 *
 * <p><b>口径收口（2026-09-20 批次E）</b>：本枚举**只管「申请单自己的状态」**，
 * 只有三个合法值：1 已提交 / 2 已缴费 / 6 已取消。
 *
 * <p>历史上它把「执行态」（3 已采样 / 4 检验中 / 5 已出报告）也塞了进来，
 * 结果是同一个列有两套码值在打架：
 * <ul>
 *   <li>码值 2 在申请单口径是「已缴费」，在检查执行口径是「已签到」，
 *       而候诊队列的状态码 2 又是「候诊中」 —— 三处含义完全不同；</li>
 *   <li>医技侧 {@code checkIn()} 把申请单写成 2 并注释「已签到」，
 *       收费侧 {@code processPayment()} 把申请单写成 2 并注释「已缴费」，
 *       同一个 2 被两个模块当成两件事写。</li>
 * </ul>
 *
 * <p><b>现在执行进度只由执行记录表达</b>：检查记录的记录状态列
 * （1已登记/2已签到/3检查中/4已出结果/5已审核/6已发布/7已取消）与
 * 检验记录的记录状态列（1已登记/2已采样/3已接收/4检测中/5已出结果/6已审核/7已发布/8已取消）。
 * 医技侧**不再回写申请单**，申请单只被「缴费 / 退费 / 取消」三个动作推动。
 *
 * <p>3 / 4 / 5 三个常量只为兼容存量数据与既有引用而保留，标 {@code @Deprecated}，
 * 新代码不得写入；存量已由 sql/51-检查检验申请状态收口.sql 订正为 2。
 */
@Getter
@AllArgsConstructor
public enum ApplyStatusEnum {

    /**
     * 1-已提交（已开单，未缴费）
     */
    SUBMITTED(1, "已提交"),

    /**
     * 2-已缴费
     */
    PAID(2, "已缴费"),

    /**
     * 3-已采样（<b>废弃</b>，执行态请读检验记录的记录状态）
     */
    @Deprecated
    SAMPLED(3, "已采样"),

    /**
     * 4-检验中（<b>废弃</b>，执行态请读执行记录）
     */
    @Deprecated
    TESTING(4, "检验中"),

    /**
     * 5-已出报告（<b>废弃</b>，执行态请读执行记录）
     */
    @Deprecated
    REPORTED(5, "已出报告"),

    /**
     * 6-已取消
     */
    CANCELLED(6, "已取消"),

    /**
     * 未知状态（兜底处理，防止解析异常）
     */
    UNKNOWN(0, "未知状态");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态描述
     */
    private final String label;

    /**
     * 根据状态码获取对应的枚举实例
     *
     * @param code 状态码
     * @return 对应的枚举实例，若未匹配则返回 UNKNOWN
     */
    public static ApplyStatusEnum fromCode(int code) {
        for (ApplyStatusEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return UNKNOWN;
    }

    /**
     * 展示用码值 → 文案。null 或不在枚举内（脏数据）一律返回空串，不回落到合法文案、也不暴露「未知(n)」。
     * 0（UNKNOWN 占位）同样按「无文案」渲染。
     */
    public static String getText(Integer code) {
        ApplyStatusEnum status = code == null ? null : fromCode(code);
        return status == null || status == UNKNOWN ? "" : status.label;
    }

    /**
     * 异常 / 审计 / 合规用码值 → 文案。null 或不在枚举内返回「未知(n)」，保留原始码值便于排查
     * （fromCode 对脏值返回 UNKNOWN 占位，这里还原为「未知(原始码)」而不是「未知状态」）。
     */
    public static String labelOrUnknown(Integer code) {
        ApplyStatusEnum status = code == null ? null : fromCode(code);
        if (status == null || status == UNKNOWN) {
            return code == null ? "未知" : "未知(" + code + ")";
        }
        return status.label;
    }
}