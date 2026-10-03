package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.medicaltech.vo.ExamImageVO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 检查记录详情出参（记录 + 报告）
 */
@Data
public class InspectionDetailVO {
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
     * 检查记录信息
     */
    private BizInspectionRecordVO record;

    /**
     * 关联报告（可能为空）
     */
    private BizReportVO report;

    /**
     * 本申请单已挂的影像帧（按 seq 升序；简化 PACS，sql/137）。
     *
     * <p>报告还没有时也必须拿得到影像 —— 技师是「先拍片、后写报告」，
     * 只把影像挂在报告出参上，工作站拍完片就看不见自己刚传的图。
     */
    private List<ExamImageVO> images;
}
