package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 检验记录详情出参（记录 + 结果明细 + 报告）
 */
@Data
public class LaboratoryDetailVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

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
     * 逻辑删除标记：0-未删除 1-已删除
     */
    private Integer delFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 检验记录信息
     */
    private BizLaboratoryRecordVO record;

    /**
     * 检验结果明细列表
     */
    private List<BizLabResultVO> results;

    /**
     * 关联报告（可能为空）
     */
    private BizReportVO report;

    /**
     * 本申请单已挂的影像帧（按 seq 升序；简化 PACS，sql/137）。
     * 检验也能挂图（显微镜照片、血流图形），口径与检查一致，故同一个区块复用同一组件。
     */
    private List<com.his.medicaltech.vo.ExamImageVO> images;
}
