package com.his.medicaltech.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 危急值站内信的业务上下文载荷（上报催办与超时升级两条消息共用）。
 *
 * <p><b>字段名是前后端契约</b>：前端 {@code src/lib/messageCatalog.js} 的
 * {@code PAYLOAD_KEYS} 白名单里 {@code patientName / itemName / criticalNo} 会被渲染成
 * 摘要 chip，改名 = 消息中心少显示一个字段。
 *
 * <p>两条消息字段不完全一样（升级那条多 {@code escalate}/{@code toLeader}），
 * 用同一个类的理由是它们描述的是<b>同一个危急值</b>：芯片区展示的事实相同，
 * 差别只在"这条是首次催办还是升级催办、催的是本人还是上级"。
 * 未用的字段留 null（序列化时为 null 的键不输出）。
 */
@Data
public class CriticalValueMessagePayloadVO implements Serializable {

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 检验项目名称
     */
    private String itemName;

    /**
     * 危急值号
     */
    private String criticalNo;

    /**
     * 危急值描述（含阈值与临床意义）
     */
    private String criticalDesc;

    /**
     * 处置时限（分钟，仅上报催办那条填）
     */
    private Integer deadlineMinutes;

    /**
     * 是否升级催办（仅超时升级那条为 true）
     */
    private Boolean escalate;

    /**
     * 是否发给科室上级（true=催科主任，false=催本人/兜底接收人）
     */
    private Boolean toLeader;
}