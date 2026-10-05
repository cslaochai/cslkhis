package com.his.operation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * PACU Aldrete 评分入参（五项各 0~2 分）。
 *
 * <p><b>不接收总分</b>：总分由服务端逐项相加。让前端填总分意味着
 * "我可以写一个 10 分然后出室"，出室标准就成了摆设。
 */
@Data
public class PacuScoreDTO implements Serializable {

    @NotNull(message = "PACU 记录ID不能为空")
    private Long pacuId;

    /**
     * 肌力/活动（0-无 1-两肢可动 2-四肢可动）
     */
    @NotNull(message = "请评肌力/活动")
    private Integer scoreActivity;

    /**
     * 呼吸：0-需辅助通气 1-呼吸浅/受限 2-深呼吸可咳嗽
     */
    @NotNull(message = "请评呼吸")
    private Integer scoreRespiration;

    /**
     * 血压：0-±50mmHg以上波动 1-±20~50 2-±20 以内
     */
    @NotNull(message = "请评血压波动")
    private Integer scoreCirculation;

    /**
     * 意识（0-无反应 1-可唤醒 2-完全清醒）
     */
    @NotNull(message = "请评意识")
    private Integer scoreConsciousness;

    /**
     * 氧合（0-吸氧下<90% 1-吸氧下>90% 2-空气下>92%）
     */
    @NotNull(message = "请评氧合")
    private Integer scoreSpo2;

    /**
     * 清醒程度（1-完全清醒 2-嗜睡可唤醒 3-未清醒）
     */
    private Integer awareness;

    /**
     * 氧疗方式
     */
    private String oxygenTherapy;

    /**
     * 镇痛方式
     */
    private String analgesia;

    /**
     * 并发症：0-无 1-有（=1 必填说明）
     */
    private Integer complicationFlag;

    /**
     * 并发症经过与处理
     */
    private String complicationNote;

    /**
     * 备注
     */
    private String remark;
}
