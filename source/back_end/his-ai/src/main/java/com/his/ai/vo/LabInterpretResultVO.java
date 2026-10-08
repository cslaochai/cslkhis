package com.his.ai.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 检验结果解读出参
 */
@Data
public class LabInterpretResultVO {

    /**
     * 是否降级（模型本应参与 —— 组合异常场景 —— 但未成功）。
     * <p>
     * 异常项不足 {@code MODEL_MIN_ABNORMAL} 时默认不调模型走规则结论，
     * 那是设计内路径，不是降级；此时 degraded=false、source=rule。
     */
    private boolean degraded;

    /**
     * 降级原因
     */
    private String degradeReason;

    /**
     * 表达层来源：rule-规则解读 model-模型连贯解读（仅组合异常时出现）
     */
    private String source;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 检验记录号
     */
    private String recordNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别文本
     */
    private String genderText;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 检验项目（大项目）名称
     */
    private String laboratoryItemName;

    /**
     * 结果录入时间
     */
    private LocalDateTime reportTime;

    /**
     * 结果条数
     */
    private Integer itemCount;

    /**
     * 异常条数
     */
    private Integer abnormalCount;

    /**
     * 危急值条数
     */
    private Integer criticalCount;

    /**
     * 未能自动判定的条数（参考区间不可用 / 结果非数值）。
     * <p>
     * 这些项既不算异常也不算正常，必须单独计数，不能并进「正常」里。
     */
    private Integer unjudgedCount;

    /**
     * 逐项概览（确定性规则生成）
     */
    private List<LabItemOverviewVO> items = new ArrayList<>();

    /**
     * 历史趋势（确定性计算生成）
     */
    private List<LabTrendVO> trends = new ArrayList<>();

    /**
     * 趋势解读（模型生成；降级时为空）
     */
    private String trendSummary;

    /**
     * 检验结论草稿（模型生成；降级时由规则拼出异常项清单）
     */
    private String conclusion;

    /**
     * 建议草稿
     */
    private List<String> suggestions = new ArrayList<>();

    /**
     * 需重点关注的项
     */
    private List<String> attentionPoints = new ArrayList<>();

    /**
     * 是否已写回检验记录
     */
    private Boolean conclusionSaved;

    /**
     * 写回说明。未写回时明确说明「草稿未写回」，避免使用者以为已经落库。
     */
    private String saveTip;

    /**
     * 耗时（毫秒）
     */
    private Long latencyMs;
}
