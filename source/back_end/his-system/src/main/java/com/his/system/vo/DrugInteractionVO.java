package com.his.system.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药物相互作用知识条目（列表/详情出参）
 */
@Data
public class DrugInteractionVO {

    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 成分关键字A
     */
    private String componentA;

    /**
     * 成分关键字B
     */
    private String componentB;

    /**
     * 成分对归一化键（服务端生成，界面只读）
     */
    private String pairKey;

    /**
     * 严重度（1-禁忌 2-慎用）
     */
    private Integer severity;

    /**
     * 相互作用后果（审方提示正文）
     */
    private String interactionDesc;

    /**
     * 处理建议（换药/减量/监测什么指标）
     */
    private String suggestion;

    /**
     * 状态（1-启用 0-停用）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 两个成分在本院药品字典里各能命中多少条药品
     * <p>0 表示这条知识暂时打不到任何药（如「西地那非」本院未收录）—— 属知识储备而非数据错，
     * 但没有这个计数，维护者无法区分「写错了关键字」和「字典还没进这个药」。
     */
    private Integer drugHitsA;

    private Integer drugHitsB;
}
