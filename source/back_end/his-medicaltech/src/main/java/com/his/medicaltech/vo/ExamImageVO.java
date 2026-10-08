package com.his.medicaltech.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 影像帧出参（一页一条帧，阅片器按 seq 顺序铺开）
 */
@Data
public class ExamImageVO {

    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 1-检查 2-检验
     */
    private Integer bizType;

    /**
     * 申请单ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyId;

    /**
     * 申请单号
     */
    private String applyNo;

    /**
     * 执行记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 检查/检验项目名称
     */
    private String itemName;

    /**
     * 检查部位
     */
    private String bodyPart;

    /**
     * 影像模态（1-CT 2-MR 3-DR 4-超声 5-心电 6-内镜 7-其他）
     */
    private Integer modality;

    /**
     * 模态文案（字典 his_exam_device_type 现取，不在 VO 里写死中文）
     */
    private String modalityText;

    /**
     * 本申请单内的帧序号
     */
    private Integer seq;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 相对路径（uploads/examImage/…），前端拼 /api/ 前缀访问
     */
    private String fileUrl;

    /**
     * 文件字节数
     */
    private Long fileSize;

    /**
     * 文件MIME类型
     */
    private String mimeType;

    /**
     * 来源（1-工作站上传 2-模拟）
     */
    private Integer source;

    private String sourceText;

    /**
     * 上传人
     */
    private String createBy;

    /**
     * 上传时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
