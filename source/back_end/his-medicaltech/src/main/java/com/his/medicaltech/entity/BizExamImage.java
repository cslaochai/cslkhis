package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 检查/检验影像帧（检查影像帧，sql/137）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_exam_image")
public class BizExamImage extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;



    /**
     * 单据类型（1-检查申请单 2-检验申请单，与报告单的报告类型同码）
     */
    private Integer bizType;

    /**
     * 申请单ID
     */
    private Long applyId;

    /**
     * 申请单号
     */
    private String applyNo;

    /**
     * 执行记录ID（冗余展示用，不作为查询条件）
     */
    private Long recordId;

    /**
     * 患者ID
     */
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
     * 检查部位（快照，检验为空）
     */
    private String bodyPart;

    /**
     * 影像模态（1-CT 2-MR 3-DR 4-超声 5-心电 6-内镜 7-其他）
     */
    private Integer modality;

    /**
     * 本申请单内帧序号（服务端 max+1）
     */
    private Integer seq;

    /**
     * 原始文件名
     */
    private String fileName;

    /**
     * 相对访问路径（uploads/examImage/yyyyMMdd/xxx.png，前端拼 /api/）
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
     * 来源（1-工作站上传 2-模拟 DICOM 导入），见 ExamImageSourceEnum
     */
    private Integer source;
}
